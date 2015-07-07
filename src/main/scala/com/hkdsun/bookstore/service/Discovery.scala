package com.hkdsun.bookstore.service

import akka.actor.Actor
import com.hkdsun.bookstore.config.Configuration
import java.io.File

case class StartDiscovery(path: String)

class DiscoveryServiceActor extends Actor with Configuration {
  override def preStart = self ! StartDiscovery(rootDirectory)

  def receive: Receive = {
    case StartDiscovery(path) => {
      val file = new File(path)
      val authors = file.listFiles.filter(_.isDirectory).map(_.getName).foreach(println(_))
    }
  }
}
