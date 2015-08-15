package com.hkdsun.bookstore.service

import akka.actor._
import com.hkdsun.bookstore.domain._
import com.hkdsun.bookstore.domain.BookProtocol._
import com.hkdsun.bookstore.adapter._
import com.hkdsun.bookstore.boot._
import spray.httpx.SprayJsonSupport._
import spray.json._
import spray.routing.HttpService
import org.bson.types.ObjectId

class RestServiceActor extends Actor
    with BookRouter
    with SearchRouter
    with DiscoveryRouter {

  implicit def actorRefFactory = context

  val frontend = getFromResourceDirectory("app")
  val api = pathPrefix("api") { bookRoute ~ searchRoute ~ discoveryRouter }

  def router: Receive = runRoute(api ~ frontend)
  def management: Receive = {
    case ShutdownSignal ⇒
      self ! PoisonPill
  }

  def receive = management orElse router
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
          actorRefFactory.actorSelection("/user/app-manager/discovery-service") ! StartDiscovery()
          "Started discovery.. please wait for results"
        }
      }
    } ~
      path("shutdown") {
        get {
          complete {
            actorRefFactory.actorSelection("/user/app-manager") ! ShutdownSignal()
            "System shutting down"
          }
        }
      }
}
