package com.hkdsun.bookstore

import spray.json._

object BookProtocol extends DefaultJsonProtocol {
  implicit val bookFormat: RootJsonFormat[Book] = jsonFormat6(Book)
  implicit val errorFormat: RootJsonFormat[ErrorResponse] = jsonFormat2(ErrorResponse)
}

case class Book(id: Option[String] = None,
                title: String,
                description: String,
                authors: List[String],
                isbn: String,
                filename: String)

case class ErrorResponse(error_source: Option[String],
                         reason: String)
