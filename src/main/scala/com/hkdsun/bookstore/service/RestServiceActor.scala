package com.hkdsun.bookstore.service

import akka.actor.Actor
import com.hkdsun.bookstore.domain._
import com.hkdsun.bookstore.domain.BookProtocol._
import com.hkdsun.bookstore.adapter._
import spray.httpx.SprayJsonSupport._
import spray.json._
import spray.routing.HttpService

class RestServiceActor extends Actor 
  with BookRouter 
  with SearchRouter 
  with AuthorRouter {

  implicit def actorRefFactory = context

  def receive: Receive = runRoute(bookRoute ~ authorRoute ~ searchRoute)
}

trait BookRouter extends HttpService {
  val bookRoute = 
    path("book" / Segment) { bookId =>
      get {
        complete {
          BookDal.find(bookId)
        }
      } 
    } ~
    path("book") {
      get {
        complete {
          BookDal.all
        }
      } ~
      post {
          entity(as[String]) { source =>
              complete {
                  val json = source.parseJson
                  val book = json.convertTo[Book]
                  BookDal.save(book).toString
              }
          }
      }
    }
}

trait AuthorRouter extends HttpService {
  val authorRoute =
    path("author" / Segment) { authorId =>
      get {
        complete {
          AuthorDal.find(authorId)
        }
      } 
    } ~
    path("author") {
      get {
        complete {
          AuthorDal.all
        }
      } ~
      post {
          entity(as[String]) { source =>
              complete {
                  val json = source.parseJson
                  val author = json.convertTo[Author]
                  AuthorDal.save(author).toString
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
