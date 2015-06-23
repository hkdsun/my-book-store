package com.hkdsun.bookstore.service

import akka.actor.Actor
import com.hkdsun.bookstore.domain._
import spray.httpx.Json4sSupport
import spray.routing.HttpService
import spray.json._
import spray.httpx.SprayJsonSupport._
import com.hkdsun.bookstore.domain.BookProtocol._

class RestServiceActor extends Actor with BookRouter with SearchRouter {

  implicit def actorRefFactory = context

  def receive: Receive = runRoute(bookRoute ~ searchRoute)
}

trait BookRouter extends HttpService {
  val bookRoute = 
    path("list" / Segment) { bookId =>
      get {
        complete {
          val author = Author(id = Some(5678), firstName = "Hormoz", lastName = "K")
          val book = Book(id = Some(1234), title = s"BookId $bookId", author = author, isbn = "123456")
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
