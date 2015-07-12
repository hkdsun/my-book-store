package com.hkdsun.bookstore.service

import akka.actor.Actor
import com.hkdsun.bookstore.config.Configuration
import com.hkdsun.bookstore.domain._
import java.io.File
import com.hkdsun.bookstore.utils.{ FileTools, EbookFile }

case class StartDiscovery(path: String)
case class FindBook(file: EbookFile)
case class FoundBook(result: Book)

class DiscoveryServiceActor extends Actor with Configuration {
  override def preStart = self ! StartDiscovery(rootDirectory)

  def receive: Receive = {
    case StartDiscovery(path) ⇒ {
      val files = FileTools.getEbooks(path)
      for (EbookFile(name, _, _) ← files) {
        println(s"file: $name")
      }
    }
  }
}

class DiscoveryManagerActor extends Actor {
  def receive: Receive = {
    case FindBook(EbookFile(filename, _, path)) ⇒

  }
}

class BookFinderActor extends Actor {

}
