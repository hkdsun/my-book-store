package com.hkdsun.bookstore.adapter

import com.hkdsun.bookstore.domain._
import com.mongodb.casbah.Imports._

object BookDal {

  lazy val conn = MongoFactory.getConnection
  lazy val collection = MongoFactory.getCollection(conn)

  def save(book: Book) = {
    val bookObj = buildMongoDbObject(book)
    val result = MongoFactory.getCollection(conn).save(bookObj)
    val id = bookObj.getAs[org.bson.types.ObjectId]("_id").get
    id
  }


  def find(id: String): Option[Book] = {
    val q = MongoDBObject("_id" -> new org.bson.types.ObjectId(id))
    val dbobj = collection findOne q

    dbobj match {
      case Some(obj) =>
        val author = obj.getAs[DBObject]("author")
        println(s"AUTHOR:\n$author")
        println(s"OBJ:\n$obj")
        val book = Book(
          id = Some(obj.as[org.bson.types.ObjectId]("_id").toString),
          title = obj.as[String]("title"),
          author = AuthorDal.make(author).get,
          isbn = obj.as[String]("isbn"))
        Some(book)
      case None =>
        None
    }
  }

  def all: List[Book] = collection.find.toList.map{ obj => 
      println(s"OBJECT ISSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSS\n $obj")
    val author = obj.getAs[DBObject]("author")
    Book(
      id = Some(obj.as[org.bson.types.ObjectId]("_id").toString),
      title = obj.as[String]("title"),
      author = AuthorDal.make(author).get,
      isbn = obj.as[String]("isbn")
    )
  }

  def buildMongoDbObject(book: Book): MongoDBObject = {
    val builder = MongoDBObject.newBuilder
    builder += "title" -> book.title
    builder += "author" -> AuthorDal.buildMongoDbObject(book.author)
    builder += "isbn" -> Some(book.isbn).getOrElse("")
    builder.result
  }
}

object AuthorDal {
  lazy val conn = MongoFactory.getConnection
  lazy val collection = MongoFactory.getCollection(conn)

  def make(obj: Option[DBObject]): Option[Author] = obj.map { auth =>
    Author (firstName = auth.as[String]("firstName"),
      lastName = auth.as[String]("lastName"))
  }

  def save(author: Author) = {
    val obj = buildMongoDbObject(author)
    val result = MongoFactory.getCollection(conn).save(obj)
    val id = obj.getAs[org.bson.types.ObjectId]("_id").get
    id
  }


  def find(id: String): Option[Author] = {
    var q = MongoDBObject("_id" -> new org.bson.types.ObjectId(id))
    val result = collection findOne q

    val authorResult = result.get

    val author = Author(
      id = Some(authorResult.as[org.bson.types.ObjectId]("_id").toString),
      firstName = authorResult.as[String]("firstName"),
      lastName = authorResult.as[String]("lastName")
    )

    //TODO Make this return an option and refactor accordingly
    Some(author)
  }

  def all: List[Author] = collection.find.toList.map{ obj => 
    Author(
      id = Some(obj.as[org.bson.types.ObjectId]("_id").toString),
      firstName = obj.as[String]("firstName"),
      lastName = obj.as[String]("lastName")
    )
  }

  def buildMongoDbObject(author: Author): MongoDBObject = {
    val builder = MongoDBObject.newBuilder
    builder += "firstName" -> author.firstName
    builder += "lastName" -> author.lastName
    builder.result
  }

}
