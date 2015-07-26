package com.hkdsun.bookstore.service

import akka.actor.Actor
import com.hkdsun.bookstore.utils.{ EbookFile }

trait BookFinder extends Actor {
  val baseUrl: String
  def findBook(query: String): DiscoveryResult

  def receive: Receive = {
    case DiscoveryQuery(Some(title), Some(author), Some(isbn)) ⇒ findBook(s"$title by ${author.mkString(", ")} $isbn")
    case DiscoveryQuery(Some(title), Some(author), None)       ⇒ findBook(s"$title by ${author.mkString(", ")}")
    case DiscoveryQuery(Some(title), None, None)               ⇒ findBook(title)
    case DiscoveryQuery(None, None, Some(isbn))                ⇒ findBook(isbn)
  }
}
