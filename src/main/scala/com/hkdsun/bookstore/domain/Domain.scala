package com.hkdsun.bookstore.domain

import spray.json._

object BookProtocol extends DefaultJsonProtocol {
  implicit val authorFormat: RootJsonFormat[Author] = jsonFormat3(Author)
  implicit val bookFormat: RootJsonFormat[Book] = jsonFormat4(Book)
}

case class Book(
  val id: Option[String] = None, 
  val title: String, 
  val author: Author, 
  val isbn: String)
case class Author(
  val id: Option[Long] = None, 
  val firstName: String, 
  val lastName: String)

