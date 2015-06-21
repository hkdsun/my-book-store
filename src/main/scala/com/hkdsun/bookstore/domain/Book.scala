package com.hkdsun.bookstore.domain

case class Book(val id: Option[Long], val title: String, val author: Author, val isbn: String)
