package com.hkdsun.bookstore.service

import akka.actor.{ Actor, Props, Stash, ActorRef }
import com.hkdsun.bookstore.config.Configuration
import com.hkdsun.bookstore.domain._
import java.io.File
import com.hkdsun.bookstore.utils._

case class StartDiscovery(path: String)
case class DiscoverBook(file: EbookFile)
case class DiscoveryResult(result: Option[Book])
case class DiscoveryQuery(title: Option[String], authors: Option[List[String]], isbn: Option[String])

/*
 * This actor is in charge of starting the discovery hierarchy
 * For now, I'm deciding that it will just read the root dir off
 * the config but later we could easily add case classes that
 * will fire off discovery for a different root. Possibly when
 * support for multiple libraries is added
 */
class DiscoveryServiceActor extends Actor with Configuration {
  override def preStart = self ! StartDiscovery(rootDirectory)

  def receive: Receive = {
    case StartDiscovery(path) ⇒ {
      val files = FileTools.getEbooks(path)
      for (file ← files) {
        context.actorOf(DiscoveryManagerActor.props) ! DiscoverBook(file)
      }
    }
  }
}

/* 
 * This actor is in charge of handling a list of files that 
 * need to be discovered. It aggregates results by firing off
 * chilren that discover the book on e.g. Amazon
 */
class DiscoveryManagerActor extends Actor {
  def receive: Receive = {
    case a @ DiscoverBook(file) ⇒
      context.actorOf(IdentifierManagerActor.props) ! a
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
 */
class IdentifierManagerActor extends Actor with Stash with Configuration {
  import scala.collection.mutable.Set

  val workers: Set[ActorRef] = Set.empty

  def limit: Int = config.getInt("discovery.max-connections")

  def waiting: Receive = {
    case DiscoveryResult(Some(book)) ⇒
      workers -= sender
      println(s"Found book: $book")
      context.become(working)
    case _ ⇒
      stash()
  }

  def working: Receive = {
    case DiscoverBook(file: EbookFile) ⇒
      val worker = context.actorOf(AmazonBookFinder.props)
      workers += worker
      worker ! DiscoveryQuery(Some(file.filename), None, None)
      if (workers.size > limit)
        context.become(waiting)
    case DiscoveryResult(Some(book)) ⇒
      workers -= sender
      println(s"Found book: $book")
  }

  def receive = working
}

object IdentifierManagerActor {
  def props: Props = Props(new IdentifierManagerActor)
}

