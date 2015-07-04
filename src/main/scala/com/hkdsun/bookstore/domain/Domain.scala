package com.hkdsun.bookstore.domain

import spray.json._

object BookProtocol extends DefaultJsonProtocol {
  implicit val authorFormat: RootJsonFormat[Author] = jsonFormat3(Author)
  implicit val bookFormat: RootJsonFormat[Book] = jsonFormat4(Book)
  implicit val errorFormat: RootJsonFormat[ErrorResponse] = jsonFormat2(ErrorResponse)
}

case class Book(
  val id: Option[String] = None, 
  val title: String, 
  val author: Author, 
  val isbn: String)

case class Author(
  val id: Option[String] = None, 
  val firstName: String, 
  val lastName: String)

case class ErrorResponse(
  val logger: Option[String],
  val reason: String)
