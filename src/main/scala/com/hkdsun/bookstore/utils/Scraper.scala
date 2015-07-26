package com.hkdsun.bookstore.utils

import org.htmlcleaner.HtmlCleaner
import java.net.{ URL, URLEncoder }

trait XmlScraper {
  val cleaner = new HtmlCleaner
  val rootNode = cleaner.clean(queryUrl)
  def title: String
  def authors: List[String]
  def isbn: String
  def queryUrl: URL
}

class AmazonScraper(query: String) extends XmlScraper {
  val listNode = rootNode.getElementsByName("li", true).filter(li ⇒ li.getAttributeByName("class") != null && li.getAttributeByName("class").contains("result-item"))
  val resultNode = listNode.head

  def title = {
    val elements = resultNode.getElementsByName("h2", true)
    val titles = elements.filter(a ⇒ a.getAttributeByName("class") != null && a.getAttributeByName("class").contains("title"))
    titles.head.getText.toString
  }

  def authors = {
    val elements = resultNode.getElementsByName("div", true)
    val byString = elements.map(_.getText.toString).filter(_.startsWith("by")).head
    val re = """by (.*)""".r
    val re2 = """by (.*) and (.*)""".r
    println(byString.toString)
    val authors = byString match {
      case re2(a, b) ⇒ List(a, b)
      case re(a)     ⇒ List(a)
      case _         ⇒ throw new Exception("wth was that author?")
    }
    authors
  }

  def isbn = ???

  def queryUrl = new URL("http://www.amazon.com/s/" + URLEncoder.encode(s"?url=search-alias%3Ddigital-text&field-keywords=$query", "UTF-8"))
}

object AmazonScraper {
  def apply(query: String) = new AmazonScraper(query)
}
