package com.hkdsun.bookstore

import akka.actor._
import com.hkdsun.bookstore.BookProtocol._
import reactivemongo.bson.BSONObjectID
import spray.httpx.SprayJsonSupport._
import spray.json._
import spray.routing.HttpService
import spray.routing.directives.OnSuccessFutureMagnet
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.util.{ Failure, Success }

class RestServiceActor extends Actor
    with BookRouter
    with SearchRouter
    with DiscoveryRouter {

  implicit def actorRefFactory = context
  implicit val ec = context.dispatcher

  val frontend = get {
    compressResponse()(getFromResourceDirectory("app/dist")) ~
      path("") {
        getFromResource("app/dist/index.html")
      }
  }
  val api = pathPrefix("api") { bookRoute ~ searchRoute ~ discoveryRouter }

  def router: Receive = runRoute(api ~ frontend)
  def management: Receive = {
    case ShutdownSignal ⇒
      self ! PoisonPill
  }

  def receive = management orElse router
}

trait BookRouter extends HttpService {
  implicit val ec: ExecutionContext

  val bookRoute =
    path("book" / Segment) { bookId ⇒
      get {
        val bookFuture = BookDalProduction.findById(bookId).mapTo[Option[Book]]
        onSuccess(OnSuccessFutureMagnet(bookFuture)) {
          case Some(book) ⇒
            complete(book)
          case None ⇒
            complete(ErrorResponse(Some("Book search"), "Book not found"))
        }
      }
    } ~
      path("book") {
        get {
          complete {
            BookDalProduction.all
          }
        } ~
          post {
            entity(as[String]) { source ⇒
              complete {
                val json = source.parseJson
                val book = json.convertTo[Book]
                BookDalProduction.insert(book).toString
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
