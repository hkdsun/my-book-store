package com.hkdsun.bookstore

import akka.actor._
import java.io.File
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import com.typesafe.scalalogging.LazyLogging

import scala.util.{ Failure, Success }

case class StartDiscovery()
case class DiscoverBook(file: EbookFile)
case class DiscoveryResult(result: Option[Book])
case class DiscoveryRequest(queryString: Option[String], path: String)
case class DiscoveryFailed(request: DiscoveryRequest, cause: Throwable)

/*
 * This actor is in charge of starting the discovery hierarchy
 * For now, I'm deciding that it will just read the root dir off
 * the config but later we could easily add case classes that
 * will fire off discovery for a different root. Possibly when
 * support for multiple libraries is added
 */
class DiscoveryServiceActor extends Actor with Configuration with LazyLogging {

  val manager = context.actorOf(DiscoveryManagerActor.props)

  def receive: Receive = {
    case StartDiscovery() ⇒ {
      logger.info("Starting discovery")
      val files = FileTools.getEbooks(rootDirectory)
      for (file ← files) {
        manager ! DiscoverBook(file)
      }
    }
    case ShutdownSignal() ⇒
      self ! PoisonPill
  }
}

/* 
 * This actor is in charge of handling a list of files that 
 * need to be discovered. It aggregates results by firing off
 * chilren that discover the book on e.g. Amazon
 */
class DiscoveryManagerActor extends Actor with LazyLogging {
  import BookDalProduction._

  val manager = context.actorOf(IdentifierManagerActor.props)

  def receive: Receive = {
    case discoverMessage @ DiscoverBook(file) ⇒
      countByPath(file.abspath.toString).onSuccess {
        case i: Int if i > 0 ⇒
        //logger.info(s"Skipping file ${file.abspath.toString} since it's already discovered")
        case i: Int ⇒
          manager ! discoverMessage
      }
  }
}

object DiscoveryManagerActor {
  def props: Props = Props(new DiscoveryManagerActor)
}

/*
 * This actor is in charge of deciding which result makes the
 * most sense or, in other words, is the most complete
 * Its children are BookFinders that grab results from different
 * services. I'm working on screen scraping Amazon search results
 * for the time being. Similar to how Calibre does it.
 * I'm going to leave this pretty dumb at the moment
 */
class IdentifierManagerActor extends Actor with Stash with Configuration with LazyLogging {
  import scala.collection.mutable.Set
  import scala.collection.mutable.Map
  import BookDalProduction._

  val workers: Set[ActorRef] = Set.empty
  val requests: Map[DiscoveryRequest, Int] = Map.empty

  def receive = {
    case DiscoverBook(file: EbookFile) ⇒
      val worker = context.actorOf(AmazonBookFinder.props(self)(context.system))
      val query = Some(file.filename)
      val request = DiscoveryRequest(query, file.abspath.toString)
      val count = requests.getOrElseUpdate(request, 0)
      if (count < retryLimit) {
        requests += request -> (count + 1)
        context.watch(worker)
        workers += worker
        worker ! request
      } else {
        self ! DiscoveryFailed(request, ScrapingRetryLimitReached(s"Discovery retry limit reached for query: $query"))
      }
    case request @ DiscoveryRequest(query, path) ⇒
      val count = requests.getOrElseUpdate(request, 0)
      requests += request -> (count + 1)
      if (count < retryLimit) {
        val worker = context.actorOf(GoogleAmazonBookFinder.props(self)(context.system))
        context.watch(worker)
        workers += worker
        worker ! request
      } else {
        self ! DiscoveryFailed(request, ScrapingRetryLimitReached(s"Discovery retry limit reached for query: $query"))
      }
    case DiscoveryResult(f: Option[Book]) ⇒
      f.map(book ⇒ upsert(book))
    case DiscoveryFailed(request, e: BookNotFoundException) ⇒
      self ! request
    case Terminated(a) ⇒
      workers -= a
  }
}

object IdentifierManagerActor {
  def props: Props = Props(new IdentifierManagerActor)
}
