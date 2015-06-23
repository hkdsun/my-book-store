package com.hkdsun.bookstore.service

import akka.actor.Actor
import com.hkdsun.bookstore.domain._
import com.hkdsun.bookstore.domain.BookProtocol._
import spray.httpx.SprayJsonSupport._
import spray.json._
import spray.routing.HttpService

class RestServiceActor extends Actor with BookRouter with SearchRouter {

  implicit def actorRefFactory = context

  def receive: Receive = runRoute(bookRoute ~ searchRoute)
}

trait BookRouter extends HttpService {
  val bookRoute = 
    path("book" / Segment) { bookId =>
      get {
        complete {
          val author = Author(firstName = "Hormoz", lastName = "K")
          val book = Book(title = s"BookId $bookId", author = author, isbn = "123456")
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
