package com.hkdsun.bookstore.service

import akka.actor.Actor
import com.hkdsun.bookstore.config.Configuration
import java.io.File
import com.hkdsun.bookstore.utils.{ FileTools, EbookFile }

case class StartDiscovery(path: String)

class DiscoveryServiceActor extends Actor with Configuration {
  override def preStart = self ! StartDiscovery(rootDirectory)

  def receive: Receive = {
    case StartDiscovery(path) => {
      val files = FileTools.getEbooks(path)
      for(EbookFile(name,_,_) <- files){
        println(s"file: $name")
      }
    }
  }
}
