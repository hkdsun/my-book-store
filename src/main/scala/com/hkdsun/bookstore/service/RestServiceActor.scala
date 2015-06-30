package com.hkdsun.bookstore.service

import akka.actor.Actor
import com.hkdsun.bookstore.domain._
import com.hkdsun.bookstore.domain.BookProtocol._
import com.hkdsun.bookstore.adapter._
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
          import BookDal._
          findBook(bookId)
        }
      } 
    } ~
    path("book") {
      get {
        complete {
          "shows everything"
        }
      } ~
      post {
          entity(as[String]) { source =>
              complete {
                  import BookDal._
                  val json = source.parseJson
                  val book = json.convertTo[Book]
                  saveBook(book).toString
              }
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
