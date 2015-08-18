package com.hkdsun.bookstore.service

import akka.actor._
import scala.concurrent.{ Future, ExecutionContext }
import com.hkdsun.bookstore.utils.{ EbookFile }
import com.hkdsun.bookstore.domain._
import com.hkdsun.bookstore.utils._
import scala.concurrent.ExecutionContext.Implicits.global
import com.typesafe.scalalogging.LazyLogging

trait BookFinder extends Actor {
  def findBook(query: String)(implicit ec: ExecutionContext): Future[Option[Book]]

  def receive: Receive = {
    case DiscoveryQuery(Some(title), Some(author), Some(isbn)) ⇒ sender ! DiscoveryResult(findBook(s"$title by ${author.mkString(", ")} $isbn"))
    case DiscoveryQuery(Some(title), Some(author), None)       ⇒ sender ! DiscoveryResult(findBook(s"$title by ${author.mkString(", ")}"))
    case DiscoveryQuery(Some(title), None, None)               ⇒ sender ! DiscoveryResult(findBook(title))
    case DiscoveryQuery(None, None, Some(isbn))                ⇒ sender ! DiscoveryResult(findBook(isbn))
  }
}

class AmazonBookFinder(implicit system: ActorSystem) extends BookFinder with LazyLogging {
  def findBook(query: String)(implicit ec: ExecutionContext): Future[Option[Book]] = {
    val scraper = AmazonScraper(query)
    for {
      title ← scraper.title
      authors ← scraper.authors
      description ← scraper.description
      defined = title.isDefined && authors.isDefined && description.isDefined
    } yield {
      if (defined) {
        Some(Book(title = title.get, description = description.get, authors = authors.get.map(a ⇒ Author(name = a)), isbn = "Not Implemented"))
      } else {
        logger.warn(s"An incomplete book was ignored - title: $title - author: $authors - description : $description")
        None
      }
    }
  }
}

object AmazonBookFinder {
  def props(implicit system: ActorSystem) = Props(new AmazonBookFinder)
}
