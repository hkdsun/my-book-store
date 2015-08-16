package com.hkdsun.bookstore.service

import akka.actor._
import scala.concurrent.{ Future, ExecutionContext }
import com.hkdsun.bookstore.utils.{ EbookFile }
import com.hkdsun.bookstore.domain._
import com.hkdsun.bookstore.utils._
import scala.concurrent.ExecutionContext.Implicits.global

trait BookFinder extends Actor {
  def findBook(query: String)(implicit ec: ExecutionContext): Future[Option[Book]]

  def receive: Receive = {
    case DiscoveryQuery(Some(title), Some(author), Some(isbn)) ⇒ sender ! DiscoveryResult(findBook(s"$title by ${author.mkString(", ")} $isbn"))
    case DiscoveryQuery(Some(title), Some(author), None)       ⇒ sender ! DiscoveryResult(findBook(s"$title by ${author.mkString(", ")}"))
    case DiscoveryQuery(Some(title), None, None)               ⇒ sender ! DiscoveryResult(findBook(title))
    case DiscoveryQuery(None, None, Some(isbn))                ⇒ sender ! DiscoveryResult(findBook(isbn))
  }
}

class AmazonBookFinder(implicit system: ActorSystem) extends BookFinder {
  def findBook(query: String)(implicit ec: ExecutionContext): Future[Option[Book]] = {
    val scraper = AmazonScraper(query)
    val book = Future {
      val title = scraper.title
      val authors = scraper.authors
      val desc = scraper.description

      if (title.isDefined && authors.isDefined && desc.isDefined)
        Some(Book(title = title.get, authors = authors.get.map(n ⇒ Author(name = n)), description = desc.get, isbn = "not implemented"))
      else
        None
    }
    book
  }
}

object AmazonBookFinder {
  def props(implicit system: ActorSystem) = Props(new AmazonBookFinder)
}
