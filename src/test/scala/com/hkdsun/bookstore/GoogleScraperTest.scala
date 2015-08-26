package com.hkdsun.bookstore

import akka.actor.{ Props, Actor, ActorSystem }
import akka.testkit.{ TestKit, TestActorRef, ImplicitSender }
import org.scalatest.{ WordSpecLike, BeforeAndAfterAll }
import org.scalatest.Matchers
import scala.concurrent.Await
import scala.concurrent.duration._
import com.hkdsun.bookstore.utils.{ MongoTest, AkkaTestBase }

class GoogleScraperTest extends AkkaTestBase {
  val scraper = GoogleScraper("The little prince - Antoine de Saint-Exupery; Irene Testot-Fer.mobi")

  "Google Scraper" should {
    "get the correct title for the little prince" in {
      Await.result(scraper.title, 5 seconds).get.contains("Wordsworth Collection") should be(true)
    }
    "get the correct authors for the little prince" in {
      Await.result(scraper.authors, 5 seconds).get should contain("Antoine de Saint-Exupery")
      Await.result(scraper.authors, 5 seconds).get should contain("Irene Testot-Ferry")
    }
    "get the correct description for the little prince" in {
      Await.result(scraper.description, 5 seconds).get.contains("The Little Prince is a classic tale of equal appeal to children and adults") should be(true)
    }
  }
}
