package com.hkdsun.bookstore.service

import akka.actor.Actor
import com.hkdsun.bookstore.domain._
import net.liftweb.json._
import spray.httpx.Json4sSupport
import spray.routing.HttpService

class RestServiceActor extends Actor with BookRouter with SearchRouter {

  implicit def actorRefFactory = context

  implicit val formats = DefaultFormats

  def receive: Receive = runRoute(bookRoute ~ searchRoute)
}

trait BookRouter extends HttpService {
  val bookRoute = 
    path("list" / Segment) { bookId =>
      get {
        complete {
          val author = Author(id = Some(1234), firstName = "Hormoz", lastName = "K")
          val book = Book(id = Some(1234), title = s"BookId $bookId", author = author, isbn = "123456")
          import net.liftweb.json.JsonDSL._
          val json =
            ("entry" ->
              ("author" ->
                ("firstname" -> author.firstName) ~
                ("lastname" -> author.lastName)) ~
              ("book" ->
                ("title" -> book.title)))
          compact(render(json))
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
