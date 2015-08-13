package com.hkdsun.bookstore.service

import akka.actor._
import com.hkdsun.bookstore.domain._
import com.hkdsun.bookstore.domain.BookProtocol._
import com.hkdsun.bookstore.adapter._
import spray.httpx.SprayJsonSupport._
import spray.json._
import spray.routing.HttpService
import org.bson.types.ObjectId

class RestServiceActor extends Actor
    with BookRouter
    with SearchRouter
    with DiscoveryRouter {

  implicit def actorRefFactory = context

  def receive: Receive = runRoute(bookRoute ~ searchRoute ~ discoveryRouter)
}

trait BookRouter extends HttpService {
  val bookRoute =
    path("book" / Segment) { bookId ⇒
      get {
        complete {
          try {
            val oid = new ObjectId(bookId)
            BookDal.find(oid) match {
              case None ⇒
                ErrorResponse(Some("Book search"), "Book not found")
              case Some(book) ⇒
                book
            }
          } catch {
            case err: IllegalArgumentException ⇒ ErrorResponse(Some("Book search"), "Invalid ID")
          }
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
            entity(as[String]) { source ⇒
              complete {
                val json = source.parseJson
                val book = json.convertTo[Book]
                BookDal.save(book).toString
              }
            }
          }
      }
}

trait SearchRouter extends HttpService {
  val searchRoute =
    path("search" / Segment) { searchParm ⇒
      get {
        complete {
          s"searching for book: $searchParm...\nPlease wait"
        }
      }
    } ~
      path("archive" / Segment) { date ⇒
        get {
          complete {
            s"coming soon..."
          }
        }
      }
}

trait DiscoveryRouter extends HttpService {
  implicit def actorRefFactory: ActorRefFactory
  val discoveryRouter =
    path("discover") {
      get {
        complete {
          actorRefFactory.actorSelection("/user/discovery-service") ! StartDiscovery()
          "Started discovery.. please wait for results"
        }
      }
    }
}
