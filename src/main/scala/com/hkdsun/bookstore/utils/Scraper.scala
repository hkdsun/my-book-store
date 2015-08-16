package com.hkdsun.bookstore.utils

import org.htmlcleaner.{ HtmlCleaner, TagNode, CleanerProperties }
import org.htmlcleaner.Utils._
import java.net.{ URL, URLEncoder }
import java.io.IOException
import scala.collection.JavaConversions._

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

  def title: Option[String] = {
    val elements = detailsNode.map(_.getElementsByName("span", true))
    val titles = elements.map(_.filter(a ⇒ a.getAttributeByName("id") != null && a.getAttributeByName("id").contains("productTitle")))
    titles.flatMap(_.headOption.map(tit ⇒ escapeHtml(tit.getText.toString, new CleanerProperties())))
  }

  def authors: Option[List[String]] = {
    val elements: Option[Array[TagNode]] = detailsNode.map(_.getElementsByName("a", true))
    val authors: Option[List[String]] = elements.map(_.filter(a ⇒ a.getAttributeByName("class") != null && a.getAttributeByName("class").contains("contributorNameID"))).map(_.map(t ⇒ escapeHtml(t.getText.toString, new CleanerProperties())).toList)
    authors
  }

  def description: Option[String] = {
    val elements: Option[Array[TagNode]] = detailsNode.map(_.getElementsByName("div", true))
    val subelements: Option[Array[TagNode]] = elements.flatMap(_.headOption.map(_.getElementsByName("noscript", true)))
    // This will be helpful when things change
    //detailsNode.get.getAllElementsList(true).toList.filter(a ⇒ a.getText.toString.contains("Hidden away") && a.getText.toString.length < 5000).foreach(a ⇒ println(s"Found node ${a.getName}:${a.getAttributes} and parent ${a.getParent}:${a.getAttributes} and grandparent ${a.getParent.getParent}:${a.getParent.getParent.getAttributes}"))
    subelements.map(sub ⇒ escapeHtml(sub.foldLeft("")((a, z) ⇒ z.getText.toString.trim + a), new CleanerProperties()))
  }

  def isbn = ???

  def queryUrl = new URL("http://www.amazon.com/s/" + URLEncoder.encode(s"?url=search-alias%3Ddigital-text&field-keywords=$query", "UTF-8"))
}

object AmazonScraper {
  def apply(query: String) = new AmazonScraper(query)
}
