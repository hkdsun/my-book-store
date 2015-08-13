package com.hkdsun.bookstore.boot

import akka.actor._
import akka.io.IO
import spray.can.Http
import com.hkdsun.bookstore.config.Configuration
import com.hkdsun.bookstore.service._

object Boot extends App with Configuration {
  val system = ActorSystem("rest-book-store-system")
  val manager = system.actorOf(Props[AppManagementActor], "app-manager")
  manager ! StartRestService()
  manager ! StartDiscoveryService()
}

case class StartRestService()
case class StartDiscoveryService()
case class ShutdownSignal()

class AppManagementActor extends Actor with Configuration {
  implicit val system = context.system
  def receive = {
    case StartRestService() ⇒
      val restService = context.actorOf(Props[RestServiceActor], "rest-endpoint")
      // start HTTP server with rest service actor as a handler
      IO(Http) ! Http.Bind(restService, serviceHost, servicePort)
    case StartDiscoveryService() ⇒
      val discoveryService = context.actorOf(Props[DiscoveryServiceActor], "discovery-service")
    case ShutdownSignal() ⇒
      context.children.foreach(a ⇒ a ! ShutdownSignal())
      self ! PoisonPill
  }

  override def postStop = system.shutdown()
}
