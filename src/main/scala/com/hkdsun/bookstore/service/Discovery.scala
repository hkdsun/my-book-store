package com.hkdsun.bookstore.service

import akka.actor.{ Actor, Props }
import com.hkdsun.bookstore.config.Configuration
import com.hkdsun.bookstore.domain._
import java.io.File
import com.hkdsun.bookstore.utils.{ FileTools, EbookFile }

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
class IdentifierManagerActor extends Actor {
  def receive: Receive = {
    case DiscoverBook(file: EbookFile) ⇒ {

    }
  }
}

object IdentifierManagerActor {
  def props: Props = Props(new IdentifierManagerActor)
}

class AmazonBookFinder extends BookFinder {
  val baseUrl = "http://www.amazon.com/s/?url=search-alias%3Ddigital-text&field-keywords="

  def findBook(query: String) = {
    DiscoveryResult(None)
  }
}
