package com.hkdsun.bookstore.domain

case class Book(val id: Option[Long], val title: String, val author: Author, val isbn: String)
case class Author(val id: Option[Long], val firstName: String, val lastName: String)

object Book {
  def json(book: Book) = {
    val json = 
      ("book" ->
        ("id" -> book.id) ~
        ("title" -> book.title) ~
        ("author" -> book.author) ~
        ("isbn" -> book.isbn))
    compact(render(json))
  }
}

object Author {

}

