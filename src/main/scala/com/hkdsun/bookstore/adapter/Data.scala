package com.hkdsun.bookstore

import reactivemongo.api._
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.api.commands.{ WriteResult, UpdateWriteResult }
import reactivemongo.bson._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Success

trait DataLayerBase {
  type T

  val collection: BSONCollection

  def insert(t: T): Future[WriteResult] = {
    collection.insert(t)
  }

  def findById(id: String): Future[Option[T]] = {
    collection.find(BSONDocument("_id" -> BSONObjectID(id))).one[T]
  }

  def all: Future[List[T]] = {
    collection.find(BSONDocument()).cursor[T]().collect[List]()
  }

  def upsert(t: T): Future[UpdateWriteResult]

  implicit val reader: BSONDocumentReader[T]
  implicit val writer: BSONDocumentWriter[T]
}

trait BookDal extends DataLayerBase {
  type T = Book

  def upsert(book: Book): Future[UpdateWriteResult] = {
    val selector = BSONDocument("title" -> book.title)
    collection.update(selector, book, upsert = true)
  }

  def findByTitle(title: String): Future[Option[Book]] = {
    collection.find(BSONDocument("title" -> title)).one[Book]
  }

  def findByAuthors(authors: List[String]): Future[Option[Book]] = {
    collection.find(BSONDocument("authors" -> authors)).one[Book]
  }

  def countByPath(path: String): Future[Int] = {
    collection.count(Some(BSONDocument("filename" -> path)))
  }

  implicit val reader: BSONDocumentReader[Book] = new BSONDocumentReader[Book] {
    def read(bson: BSONDocument): Book = {
      Book(id = bson.getAs[BSONObjectID]("_id").map(_.stringify),
        title = bson.getAs[String]("title").getOrElse(throw new NoSuchFieldException("that document didn't have a title")),
        description = bson.getAs[String]("description").getOrElse(throw new NoSuchFieldException("that document didn't have a description")),
        authors = bson.getAs[List[String]]("authors").toList.flatten,
        isbn = bson.getAs[String]("isbn").getOrElse(throw new NoSuchFieldException("that document didn't have a ISBN")),
        filename = bson.getAs[String]("filename").getOrElse(throw new NoSuchFieldException("that document didn't have a filename"))
      )
    }
  }
  implicit val writer: BSONDocumentWriter[Book] = new BSONDocumentWriter[Book] {
    override def write(t: Book): BSONDocument = BSONDocument(
      "title" -> t.title,
      "description" -> t.description,
      "authors" -> t.authors,
      "isbn" -> t.isbn,
      "filename" -> t.filename
    )
  }
}

object BookDalProduction extends BookDal {
  val collection: BSONCollection = MongoDB("books")
}
