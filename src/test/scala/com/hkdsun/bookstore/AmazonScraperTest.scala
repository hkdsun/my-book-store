package com.hkdsun.bookstore

import akka.actor.{ Props, Actor, ActorSystem }
import akka.testkit.{ TestKit, TestActorRef, ImplicitSender }
import org.scalatest.{ WordSpecLike, BeforeAndAfterAll }
import org.scalatest.Matchers
import com.hkdsun.bookstore.utils._

class AmazonScraperTest extends TestKit(ActorSystem("EventSourceSpec")) with WordSpecLike with Matchers with BeforeAndAfterAll {
  override def afterAll() = system.shutdown()

  "AmazonScraper" should {
    "get the correct title for the Ninth Configuration" in {
      val scraper = AmazonScraper("the ninth configuration")
      scraper.title should contain("The Ninth Configuration")
    }
    "get the correct author for the Ninth Configuration" in {
      val scraper = AmazonScraper("the ninth configuration")
      scraper.authors.get should contain("William Peter Blatty")
    }
    "get the correct description for the Ninth Configuration" in {
      val scraper = AmazonScraper("the ninth configuration")
      scraper.description.get.contains("Hidden away in a brooding Gothic manor in the deep woods is Center Eighteen") should be(true)
    }
  }
}
