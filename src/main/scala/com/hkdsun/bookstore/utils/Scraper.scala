package com.hkdsun.bookstore

import org.htmlcleaner.{ HtmlCleaner, TagNode, CleanerProperties }
import org.htmlcleaner.Utils._
import java.net.{ URL, URLEncoder }
import java.io.IOException
import scala.collection.JavaConversions._
import akka.actor._
import akka.pattern.ask
import scala.concurrent.duration._
import scala.concurrent.{ Await, Future }
import scala.concurrent.ExecutionContext.Implicits.global
import com.typesafe.scalalogging.LazyLogging
import net.ruippeixotog.scalascraper.browser.Browser
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.dsl.DSL.Parse._
import org.jsoup.nodes.{ Element, Document }

abstract class XmlScraper(implicit system: ActorSystem) extends Configuration {
  val browser = new Browser
  val doc = browser.get(queryUrl)

  def title: Future[Option[String]]
  def authors: Future[Option[List[String]]]
  def description: Future[Option[String]]
  def isbn: Future[Option[String]]
  def queryUrl: String
}

class GoogleScraper(query: String)(implicit system: ActorSystem) extends XmlScraper with LazyLogging {
  import java.util.regex.Pattern

  val els = doc >> elements("a")
  val listOfUrls = (els >> attrs("href")("a")).toList.filter { a ⇒
    val matcher = Pattern.compile(".*http://www.amazon.*com.*").matcher(a)
    matcher.matches()
  }

  val urls = listOfUrls.map { a ⇒
    val matcher = Pattern.compile("(.*?)(http://www.amazon.com.*?)(&.*)").matcher(a)
    val matches = matcher.matches()
    matcher.group(2)
  }

  val amazonRoots = urls.map { url ⇒
    try {
      Some(browser.get(url))
    } catch {
      case e: Throwable ⇒
        logger.debug(s"Couldn't open Amazon URL: $url - ${e.getMessage}")
        None
    }
  }.flatten

  def title: Future[Option[String]] = Future {
    amazonRoots.map { root ⇒
      root >?> text("#productTitle")
    }.headOption.flatten
  }

  def authors: Future[Option[List[String]]] = Future {
    amazonRoots.map { root ⇒
      val els = root >> elements("span")
      val spanEls = els >> elements(".author")
      (spanEls >?> texts("a")).map(_.toList).filterNot(str ⇒ str.contains("Visit Amazon's") || str.contains("search results") || str.contains("Learn about Author Central"))
    }.head
  }

  def description: Future[Option[String]] = Future {
    amazonRoots.map { root ⇒
      val els = root >> elements("noscript")
      els >?> text("div")
    }.head
  }

  def isbn: Future[Option[String]] = ???

  def queryUrl = "https://encrypted.google.com/search?q=" + URLEncoder.encode(query, "UTF-8")
}

object GoogleScraper {
  def apply(query: String)(implicit system: ActorSystem) = new GoogleScraper(query)
}

class AmazonScraper(query: String)(implicit system: ActorSystem) extends XmlScraper {
  val listOfResults = (doc >> elements("li")) >> elements(".s-result-item")
  val firstResult = (listOfResults >> elements("a")) >> attrs("href")(".s-access-detail-page")
  val resultPageUrl = firstResult.headOption
  val detailsPage = resultPageUrl.map(browser.get(_))

  def title: Future[Option[String]] = Future {
    (detailsPage >?> text("#productTitle")).flatten
  }

  def authors: Future[Option[List[String]]] = Future {
    (detailsPage >> elements("span") >> elements(".contributorNameID") >> texts(".a-link-normal")).map(_.toList).filterNot(str ⇒ str.contains("Visit Amazon's") || str.contains("search results") || str.contains("Learn about Author Central"))
  }

  def description: Future[Option[String]] = Future {
    (detailsPage >> elements("noscript")) >> text("div")
  }

  def isbn = ???

  def queryUrl = "http://www.amazon.com/s/" + URLEncoder.encode(s"?url=search-alias%3Ddigital-text&field-keywords=$query", "UTF-8")
}

object AmazonScraper {
  def apply(query: String)(implicit system: ActorSystem) = new AmazonScraper(query)
}
