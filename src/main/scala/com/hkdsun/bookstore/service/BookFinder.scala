package com.hkdsun.bookstore.service

import akka.actor.Actor
import com.hkdsun.bookstore.utils.{ EbookFile }

trait BookFinder extends Actor {
  def findBook(query: DiscoveryQuery): DiscoveryResult
}
