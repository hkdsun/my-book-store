package com.hkdsun.bookstore

import akka.actor._
import scala.concurrent.{ Future, ExecutionContext }
import scala.concurrent.ExecutionContext.Implicits.global
import com.typesafe.scalalogging.LazyLogging

trait BookFinder extends Actor {
  def findBook(query: String, filename: String)(implicit ec: ExecutionContext): Future[Option[Book]]

  def receive: Receive = {
    case DiscoveryQuery(Some(title), Some(author), Some(isbn), path) ⇒ sender ! DiscoveryResult(findBook(s"$title by ${author.mkString(", ")} $isbn", path))
    case DiscoveryQuery(Some(title), Some(author), None, path)       ⇒ sender ! DiscoveryResult(findBook(s"$title by ${author.mkString(", ")}", path))
    case DiscoveryQuery(Some(title), None, None, path)               ⇒ sender ! DiscoveryResult(findBook(title, path))
    case DiscoveryQuery(None, None, Some(isbn), path)                ⇒ sender ! DiscoveryResult(findBook(isbn, path))
  }
}

class AmazonBookFinder(implicit system: ActorSystem) extends BookFinder with LazyLogging {
  def findBook(query: String, filename: String)(implicit ec: ExecutionContext): Future[Option[Book]] = {
    val scraper = AmazonScraper(query)
    for {
      title ← scraper.title
      authors ← scraper.authors
      description ← scraper.description
      defined = title.isDefined && authors.isDefined && description.isDefined
    } yield {
      if (defined) {
        //TODO implement ISBN
        Some(Book(title = title.get, description = description.get, authors = authors.get, isbn = "Not Implemented", filename = filename))
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
