package com.hkdsun.bookstore

import akka.actor.{ Props, Actor, ActorSystem }
import akka.testkit.{ TestKit, TestActorRef, ImplicitSender }
import com.hkdsun.bookstore.utils.{ MongoTest, AkkaTestBase }
import com.typesafe.scalalogging.LazyLogging
import org.scalatest.{ WordSpecLike, BeforeAndAfterAll }
import org.scalatest.Matchers
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.bson.BSONDocument
import scala.concurrent.Await
import scala.concurrent.duration._

class DatabaseTest extends MongoTest with LazyLogging {
  object BookDalTest extends BookDal {
    val collection: BSONCollection = mongoCollection
  }

  import BookDalTest._

  "Mongo driver" should {
    "be able to insert a book into the database and query it" in {
      val book = Book(title = "Test Book", description = "Hello testing", authors = List("Author1"), isbn = "Not implemented")
      insert(book).w
      findByTitle("Test Book").w.get.copy(id = None) should be(book)
    }

    "be able to upsert a book into the database and query it" in {
      val book = Book(title = "Test Book", description = "Hello testing", authors = List("Author1"), isbn = "Not implemented")
      val book2 = book.copy(description = "changed")
      insert(book).w
      upsert(book2).w
      findByTitle("Test Book").w.get.copy(id = None) should be(book2)
    }

    "be able to find a book with a single/multiple author" in {
      val book = Book(title = "Test Book", description = "Hello testing", authors = List("Author1"), isbn = "Not implemented")
      val book2 = book.copy(title = "Test Book 2", authors = List("Author1", "Author2"))
      upsert(book).w
      upsert(book2).w
      findByAuthors(List("Author1")).w.get.copy(id = None) should be(book)
      findByAuthors(List("Author2")).w.get.copy(id = None) should be(book2)
    }
  }
}