package com.hkdsun.bookstore

import akka.actor.{ Props, Actor, ActorSystem }
import akka.testkit.{ TestKit, TestActorRef, ImplicitSender }
import org.scalatest.{ WordSpecLike, BeforeAndAfterAll }
import org.scalatest.Matchers
import scala.concurrent.Await
import scala.concurrent.duration._
import com.hkdsun.bookstore.utils.{ MongoTest, AkkaTestBase }

class AmazonScraperTest extends AkkaTestBase {
  "AmazonScraper" should {
    "get the correct title for the Ninth Configuration" in {
      val scraper = AmazonScraper("the ninth configuration")
      Await.result(scraper.title, 5 seconds) should contain("The Ninth Configuration")
    }
    "get the correct author for the Ninth Configuration" in {
      val scraper = AmazonScraper("the ninth configuration")
      Await.result(scraper.authors, 5 seconds).get should be(List("William Peter Blatty"))
    }
    "get the correct description for the Ninth Configuration" in {
      val scraper = AmazonScraper("the ninth configuration")
      Await.result(scraper.description, 5 seconds).get.contains("Hidden away in a brooding Gothic manor in the deep woods is Center Eighteen") should be(true)
    }
  }
}
