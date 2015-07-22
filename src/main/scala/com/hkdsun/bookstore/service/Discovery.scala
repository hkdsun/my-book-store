package com.hkdsun.bookstore.service

import akka.actor.Actor
import com.hkdsun.bookstore.config.Configuration
import com.hkdsun.bookstore.domain._
import java.io.File
import com.hkdsun.bookstore.utils.{ FileTools, EbookFile }

case class StartDiscovery(path: String)
case class DiscoverBook(file: EbookFile)
case class DicoveryResult(result: Option[Book])
case class DiscoveryQuery(title: String, authors: List[String], isbn: Option[String])


class DiscoveryServiceActor extends Actor with Configuration {
  override def preStart = self ! StartDiscovery(rootDirectory)

  def receive: Receive = {
    case StartDiscovery(path) ⇒ {
      val files = FileTools.getEbooks(path)
      for (file ← files) {
      }
    }
  }
}

class DiscoveryManagerActor extends Actor {
  def receive: Receive = {
    case books: List[DiscoverBook] =>
      for (book <- books) {
      }
  }
}

object DiscoveryManagerActor {
  def props: Props = Props(new DiscoveryManagerActor)
}

class IdentifierManagerActor extends Actor {
  def receive: Receive = {
    case DiscoverBook(file: EbookFile) {
    }
  }
}

object IdentifierManagerActor {
  def props: Props = Props(new IdentifierManagerActor)
}

class AmazonBookFinder extends Actor {
  def receive: Receive = {
    case DiscoveryQuery(_,_,isbn) =>
    case DiscoveryQuery(title,author,isbn) =>
    case DiscoveryQuery(title,author,None) =>
    case DiscoveryQuery(title,None,None) =>
  }
}
