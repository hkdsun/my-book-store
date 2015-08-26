package com.hkdsun.bookstore

import akka.actor._
import scala.concurrent.{ Future, ExecutionContext }
import scala.util.{ Failure, Success }
import scala.concurrent.ExecutionContext.Implicits.global
import com.typesafe.scalalogging.LazyLogging

case class BookNotFoundException(message: String) extends Exception(message)
case class ScrapingRetryLimitReached(message: String) extends Exception(message)

trait BookFinder extends Actor with LazyLogging {
  val ima: ActorRef //Identifiermanageractor
  def scraperProvider(query: String): XmlScraper

  def receive: Receive = {
    case request @ DiscoveryRequest(Some(title), path) ⇒
      val bookF = findBook(title, path)
      bookF.onComplete {
        case Success(a @ Some(book)) ⇒
          ima ! DiscoveryResult(a)
        case Success(None) ⇒
          //logger.debug(s"""Couldn't find book for query "$title" and path "$path"""")
          ima ! DiscoveryFailed(request, BookNotFoundException(s"""Couldn't find book for query "$title" and path "$path""""))
        case Failure(e) ⇒
          //logger.debug(s"""Failure trying to find book for query "$title" and path "$path"""")
          ima ! DiscoveryFailed(request, e)
      }
  }

  def findBook(query: String, filename: String)(implicit ec: ExecutionContext): Future[Option[Book]] = {
    val scraper = scraperProvider(query)
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
        //logger.debug(s"An incomplete book was ignored - filename: $filename - title: $title - author: $authors - description : $description")
        None
      }
    }
  }
}

class AmazonBookFinder(val ima: ActorRef)(implicit system: ActorSystem) extends BookFinder {
  def scraperProvider(query: String) = AmazonScraper(query)
}

class GoogleAmazonBookFinder(val ima: ActorRef)(implicit system: ActorSystem) extends BookFinder {
  def scraperProvider(query: String) = GoogleScraper(query)
}

object AmazonBookFinder {
  def props(ima: ActorRef)(implicit system: ActorSystem) = Props(new AmazonBookFinder(ima))
}

object GoogleAmazonBookFinder {
  def props(ima: ActorRef)(implicit system: ActorSystem) = Props(new GoogleAmazonBookFinder(ima))
}
