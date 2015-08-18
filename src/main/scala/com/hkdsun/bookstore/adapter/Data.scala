package com.hkdsun.bookstore.adapter

import com.hkdsun.bookstore.domain._
import com.mongodb.casbah.Imports._
import org.bson.types.ObjectId

trait DataLayerBase {
  type T
  val coll: String

  lazy val collection = MongoFactory.getCollection(coll)

  def save(t: T) = {
    collection.save(make(t))
  }

  def find(id: ObjectId): Option[T] = {
    collection.findOneByID(id) match {
      case Some(obj) ⇒
        make(Some(obj))
      case None ⇒
        None
    }
  }

  def all: List[T] = collection.find.toList.map { obj ⇒
    make(Some(obj)).get
  }

  def make(t: T): MongoDBObject
  def make(obj: Option[DBObject]): Option[T]

}

object BookDal extends DataLayerBase {
  type T = Book
  val coll = "books"

  def make(book: Book): MongoDBObject = {
    val builder = MongoDBObject.newBuilder
    builder += "title" -> book.title
    builder += "authors" -> AuthorDal.makeList(book.authors)
    builder += "isbn" -> Some(book.isbn).getOrElse("")
    builder += "description" -> book.description
    builder.result
  }

  def make(obj: Option[DBObject]): Option[Book] = obj.map { book ⇒
    Book(id = book.getAs[ObjectId]("_id").map(_.toString),
      title = book.as[String]("title"),
      authors = AuthorDal.makeList(book.getAs[List[DBObject]]("authors")).get,
      description = book.as[String]("description"),
      isbn = book.as[String]("isbn"))
  }

  def findByTitle(title: String): Option[ObjectId] = {
    val builder = MongoDBObject.newBuilder
    builder += "title" -> title
    collection.findOne(builder.result).map(_.as[ObjectId]("_id"))
  }
}

object AuthorDal extends DataLayerBase {
  type T = Author
  val coll = "books"

  def make(obj: Option[DBObject]): Option[Author] = obj.map { auth ⇒
    Author(id = auth.getAs[ObjectId]("_id").map(_.toString),
      name = auth.as[String]("name"))
  }

  def makeList(obj: Option[List[DBObject]]): Option[List[Author]] = obj.map { list ⇒
    list.map { auth ⇒
      make(Some(auth)).getOrElse(Author(name = "Unknown"))
    }
  }

  def makeList(authors: List[Author]): MongoDBList = {
    val builder = MongoDBList.newBuilder
    authors.map { auth ⇒
      builder += make(auth)
    }
    builder.result
  }

  def make(author: Author): MongoDBObject = {
    val builder = MongoDBObject.newBuilder
    builder += "name" -> author.name
    builder.result
  }
}
