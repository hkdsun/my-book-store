package com.hkdsun.bookstore.service

import akka.actor.Actor
import spray.httpx.Json4sSupport
import spray.routing.HttpService
import spray.json._
import DefaultJsonProtocol._
import com.hkdsun.bookstore.domain.Book

class RestServiceActor extends Actor with BookRouter with SearchRouter {
  implicit def json4sFormats: Formats = DefaultFormats

  implicit def actorRefFactory = context

  def receive: Receive = runRoute(bookRoute ~ searchRoute)
}

trait BookRouter extends HttpService {
  val bookRoute = 
    path("list" / Segment) { bookId =>
      get {
        complete {
          val book = Book(id = bookId, title = s"BookId $bookId", author = "Hormoz", isbn = "123456")
          book
        }
      }
    }
}

trait SearchRouter extends HttpService {
  val searchRoute =
    path("search" / Segment) { searchParm =>
      get {
        complete {
          s"searching for book: $searchParm...\nPlease wait"
        }
      }
    } ~
      path("archive" / Segment) { date =>
        get {
          complete {
            s"coming soon..."
          }
        }
      }
}
