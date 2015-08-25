package com.hkdsun.bookstore.utils

import akka.actor.ActorSystem
import akka.testkit.TestKit
import com.hkdsun.bookstore.Configuration
import org.scalatest._
import reactivemongo.api.MongoDriver
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.bson.BSONDocument

import scala.concurrent.duration.Duration
import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContext, Await, Future }

class AkkaTestBase extends TestKit(ActorSystem("BookStoreTestSpec")) with WordSpecLike with Matchers with BeforeAndAfterAll with BeforeAndAfter {
  implicit val defaultTimeout: Duration = 20 seconds
  implicit val ec: ExecutionContext = system.dispatcher

  implicit class TestFuture[A](self: Future[A]) {
    def w(implicit timeout: Duration): A = Await.result(self, timeout)
  }

  override def afterAll() = system.shutdown()
}

trait MongoTest extends AkkaTestBase with Configuration {
  val driver = new MongoDriver
  val connection = driver.connection(List(dbHost))
  val db = connection(dbName)
  val mongoCollection = db[BSONCollection]("testCollection")

  override def beforeAll() {
    super.beforeAll()
    mongoCollection.remove(BSONDocument()).w
  }

  override def afterAll() {
    driver.close()
    super.afterAll()
  }

  after {
    mongoCollection.remove(BSONDocument()).w
  }
}
