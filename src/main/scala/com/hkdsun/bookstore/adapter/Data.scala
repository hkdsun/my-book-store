package com.hkdsun.bookstore.adapter

import com.hkdsun.bookstore.domain._
import com.mongodb.casbah.commons.MongoDBObject

class BookDal {

  val conn = MongoFactory.getConnection

  def saveBook(book: Book) = {
    val bookObj = buildMongoDbObject(book)
    val result = MongoFactory.getCollection(conn).save(bookObj)
    val id = bookObj.getAs[org.bson.types.ObjectId]("_id").get
    id
  }

  def findBook(id: String) = {
    var q = MongoDBObject("_id" -> new org.bson.types.ObjectId(id))
    val collection = MongoFactory.getCollection(conn)
    val result = collection findOne q

    val bookResult = result.get

    val book = Book(
      id = Some(bookResult.as[org.bson.types.ObjectId]("_id").toString()),
      title = bookResult.as[String]("title"),
      author = bookResult.as[Author]("author"),
      isbn = bookResult.as[String])

    book 
  }

  private def buildMongoDbObject(book: Book): MongoDBObject = {
    val builder = MongoDBObject.newBuilder
    builder += "title" -> book.title
    builder += "author" -> book.author
    builder += "isbn" -> Some(book.isbn).getOrElse("")
    builder.result
  }
}
