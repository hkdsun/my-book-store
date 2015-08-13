package com.hkdsun.bookstore.utils

import org.htmlcleaner.{ HtmlCleaner, TagNode }
import java.net.{ URL, URLEncoder }
import java.io.IOException

trait XmlScraper {
  val cleaner: HtmlCleaner = new HtmlCleaner
  val rootNode: Option[TagNode] = tryToClean(5, queryUrl)

  def tryToClean(retry: Int, url: URL): Option[TagNode] = retry match {
    case 0 ⇒
      None
    case n ⇒
      try {
        Some(cleaner.clean(url))
      } catch {
        case e: IOException ⇒
          Thread.sleep(200)
          tryToClean(n - 1, url)
        case e: Exception ⇒
          println(s"Trouble connecting to server while getting result page: ${e.getMessage}")
          throw e
      }
  }

  def title: Option[String]
  def authors: Option[List[String]]
  def isbn: Option[String]
  def queryUrl: URL
}

class AmazonScraper(query: String) extends XmlScraper {
  val listNode: Option[Array[TagNode]] = rootNode.map(_.getElementsByName("li", true).filter(li ⇒ li.getAttributeByName("class") != null && li.getAttributeByName("class").contains("result-item")))
  val resultNode: Option[TagNode] = listNode.flatMap(_.headOption)

  def detailsNode: Option[TagNode] = {
    val detailsnode: Option[Array[TagNode]] = resultNode.map(_.getElementsByName("a", true).filter(a ⇒ a.getAttributeByName("class") != null && a.getAttributeByName("class").contains("detail-page")))
    val href: Option[TagNode] = detailsnode.flatMap(_.headOption)
    val detailsurl: Option[String] = href.map(_.getAttributeByName("href"))
    val root: Option[TagNode] = detailsurl match {
      case None ⇒
        None
      case Some(url) ⇒
        tryToClean(5, new URL(url))
    }
    root
  }

  def title = {
    val elements = detailsNode.map(_.getElementsByName("h1", true))
    val titles = elements.map(_.filter(a ⇒ a.getAttributeByName("class") != null && a.getAttributeByName("class").contains("productTitle")))
    titles.flatMap(_.headOption.map(_.getText.toString))
  }

  def authors = {
    val elements: Option[Array[TagNode]] = detailsNode.map(_.getElementsByName("a", true))
    val authors: Option[List[String]] = elements.map(_.filter(a ⇒ a.getAttributeByName("class") != null && a.getAttributeByName("class").contains("contributorNameID"))).map(_.map(_.getText.toString).toList)
    authors
  }

  def description = {
    val elements: Option[Array[TagNode]] = detailsNode.map(_.getElementsByName("div", true))
    val descriptionNodes: Option[Array[TagNode]] = elements.map(_.filter(a ⇒ a.getAttributeByName("id") != null && a.getAttributeByName("id").contains("bookDesc_iframe_wrapper")))
    descriptionNodes.flatMap(_.headOption.map(_.getText.toString))
  }

  def isbn = ???

  def queryUrl = new URL("http://www.amazon.com/s/" + URLEncoder.encode(s"?url=search-alias%3Ddigital-text&field-keywords=$query", "UTF-8"))
}

object AmazonScraper {
  def apply(query: String) = new AmazonScraper(query)
}
