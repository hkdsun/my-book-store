package com.hkdsun.bookstore.utils

import com.hkdsun.bookstore.config.Configuration
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

case class CleanUrl(c: HtmlCleaner, u: URL)

class CleanerActor extends Actor with Configuration {
  var retry = 0

  def receive = {
    case CleanUrl(c: HtmlCleaner, u: URL) ⇒ {
      val cleaned = try {
        sender ! Some(c.clean(u))
      } catch {
        case e: Exception if retry < retryLimit ⇒
          retry = retry + 1
          context.system.scheduler.scheduleOnce(retryInterval milliseconds, self, CleanUrl(c, u))
        case e: Throwable ⇒
          sender ! None
          println(s"Trouble cleaning url: ${e.getMessage}")
          throw e
      }
    }
  }
}

abstract class XmlScraper(implicit system: ActorSystem) extends Configuration {
  val cleaner: HtmlCleaner = new HtmlCleaner
  val rootNode: Option[TagNode] = Await.result(tryToClean(queryUrl), 120 seconds)

  def tryToClean(url: URL)(implicit arf: ActorRefFactory): Future[Option[TagNode]] = arf.actorOf(Props(new CleanerActor())).ask(CleanUrl(cleaner, url))(120 seconds).mapTo[Option[TagNode]]

  def title: Option[String]
  def authors: Option[List[String]]
  def isbn: Option[String]
  def queryUrl: URL
}

class AmazonScraper(query: String)(implicit system: ActorSystem) extends XmlScraper {
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
        Await.result(tryToClean(new URL(url)), 120 seconds)
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
  def apply(query: String)(implicit system: ActorSystem) = new AmazonScraper(query)
}
