package com.hkdsun.bookstore.boot

import akka.actor.{Props, ActorSystem}
import akka.io.IO
import spray.can.Http
import com.hkdsun.bookstore.config.Configuration
import com.hkdsun.bookstore.service._


object Boot extends App with Configuration {

  implicit val system = ActorSystem("rest-book-store-system")

  val restService = system.actorOf(Props[RestServiceActor], "rest-endpoint")

  // start HTTP server with rest service actor as a handler
  IO(Http) ! Http.Bind(restService, serviceHost, servicePort)
}
