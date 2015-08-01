package com.hkdsun.bookstore.service

import akka.actor.{ Actor, Props }
import com.hkdsun.bookstore.utils.{ EbookFile }
import com.hkdsun.bookstore.domain._
import com.hkdsun.bookstore.utils._

trait BookFinder extends Actor {
  def findBook(query: String): DiscoveryResult

  def receive: Receive = {
    case DiscoveryQuery(Some(title), Some(author), Some(isbn)) ⇒ sender ! findBook(s"$title by ${author.mkString(", ")} $isbn")
    case DiscoveryQuery(Some(title), Some(author), None)       ⇒ sender ! findBook(s"$title by ${author.mkString(", ")}")
    case DiscoveryQuery(Some(title), None, None)               ⇒ sender ! findBook(title)
    case DiscoveryQuery(None, None, Some(isbn))                ⇒ sender ! findBook(isbn)
  }
}

class AmazonBookFinder extends BookFinder {
  def findBook(query: String) = {
    val a = AmazonScraper(query)
    DiscoveryResult(Some(Book(title = a.title.getOrElse("Unknown"), authors = a.authors.getOrElse(List("Unknown")).map(auth ⇒ Author(name = auth)), isbn = "Not implemented")))
  }
}

object AmazonBookFinder {
  def props = Props(new AmazonBookFinder)
}
