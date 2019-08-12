package com.github.awant.habrareader.habr

import java.text.SimpleDateFormat
import java.util.{Date, Locale, TimeZone}

import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.scraper.ContentExtractors.{element, elementList}

import scala.io.Source
import scala.util.Try
import scala.xml.XML

import com.github.awant.habrareader.Implicits._
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.dsl.DSL._

object HabrParser {

  /** may block thread or throw exceptions */
  def loadPosts(url: String): Seq[HabrArticle] = parseRss(getTextFromUrl(url))

  /** may block thread or throw exceptions */
  def loadHtml(url: String): HabrArticle = parseHtml(getTextFromUrl(url))

  def getTextFromUrl(url: String): String = Source.fromURL(url).use(_.getLines().mkString("\n"))

  def parseRss(text: String): Seq[HabrArticle] = {
    val root = XML.loadString(text)

    val items = root \ "channel" \ "item"

    items.toList.map { item =>
      val link = (item \ "guid").text

      HabrArticle(
        id = link.filter(_.isDigit).toInt,
        link = link,
        title = (item \ "title").text,
        description = (item \ "description").text,
        date = Option(parseDate((item \ "pubDate").text)),
        author = (item \ "creator").text,
        categories = (item \ "category").map(_.text).toSet,
        fullText = None,
        rating = None,
      )
    }
  }

  def parseHtml(htmlText: String): HabrArticle = {
    val browser = JsoupBrowser()
    val doc = browser.parseString(htmlText)

    val metaAttributes = (doc.head >> elementList("meta")).map(_.attrs)

    val description: String = metaAttributes.find { attrs =>
      attrs.get("name").contains("description") && attrs.contains("content")
    }.map(_ ("content")).get.trim

    val categories: Set[String] = metaAttributes.find { attrs =>
      attrs.get("name").contains("keywords") && attrs.contains("content")
    }.map(_ ("content")).get.split(", ").toSet

    val link: String = metaAttributes.find { attrs =>
      attrs.get("property").contains("og:url")
    }.map(_ ("content")).get

    val id: Int = link.split("/").filter(_.nonEmpty).last.toInt
    val author: String = doc >> text(".post__meta .user-info__nickname")

    val views: Int = {
      val s: String = doc >> text(".post-stats__views")
      if (s.endsWith("k")) {
        (s.replace(',', '.').substring(0, s.size - 1).toDouble * 1000).toInt
      } else {
        s.toInt
      }
    }

    val commentsCount = Try {
      (doc >> text(".post-stats__comments-count")).toInt
      // isn't exist if no comments
    }.getOrElse(0)

    val addedToBookmarks = doc >> text(".bookmark__counter")

    val rating = {
      val string = (doc >> element(".voting-wjt__counter")).attr("title")
      // Total rating 79: ↑43 and ↓36
      val arr = string.split(Array('↑', '↓')).map(s => s.filter(_.isDigit).toInt)
      val upvotes = arr(1)
      val downvotes = arr(2)
      ArticleStatistics(
        upVotes = upvotes,
        downVotes = downvotes,
        viewsCount = views,
        commentsCount = commentsCount,
        bookmarksCount = addedToBookmarks.toInt
      )
    }

    val title = doc >> text(".post__title-text")
    val fullText = doc >> text(".post_full")

    HabrArticle(
      id = id,
      link = link,
      title = title,
      description = description,
      author = author,
      date = None,
      categories = categories,
      fullText = Option(fullText),
      rating = Option(rating),
    )
  }

  def parseDate(s: String): Date = dateFormat.parse(s)

  private val dateFormat = new SimpleDateFormat("EEE, dd MMM yyy HH:mm:ss 'GMT'", Locale.ENGLISH) {
    setTimeZone(TimeZone.getTimeZone("GMT"))
  }
}
