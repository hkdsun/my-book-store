package com.hkdsun.bookstore.domain

import spray.json._
import org.bson.types.ObjectId

object BookProtocol extends DefaultJsonProtocol {
  implicit val authorFormat: RootJsonFormat[Author] = jsonFormat2(Author)
  implicit val bookFormat: RootJsonFormat[Book] = jsonFormat5(Book)
  implicit val errorFormat: RootJsonFormat[ErrorResponse] = jsonFormat2(ErrorResponse)
}

case class Book(
  val id: Option[String] = None,
  val title: String,
  val description: String,
  val authors: List[Author],
  val isbn: String)

case class Author(
  val id: Option[String] = None,
  val name: String)

case class ErrorResponse(
  val error_source: Option[String],
  val reason: String)
