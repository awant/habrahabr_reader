package com.github.awant.habrareader

import java.text.SimpleDateFormat
import java.util.{Date, Locale, TimeZone}

import com.github.awant.habrareader.Implicits._

import scala.io.Source
import scala.xml.XML


object HabrParser {

  /** may block thread or throw exceptions */
  def loadPosts(url: String): Seq[HabrArticle] = parseRss(getTextFromUrl(url))

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
        date = parseDate((item \ "pubDate").text),
        author = (item \ "creator").text,
        categories = (item \ "category").map(_.text).toSet,
        fullText = None,
        rating = None,
      )
    }
  }

  def parseHtml(text: String): HabrArticle = {
    ???
  }

  def parseDate(s: String): Date = dateFormat.parse(s)

  private val dateFormat = new SimpleDateFormat("EEE, dd MMM yyy HH:mm:ss 'GMT'", Locale.ENGLISH) {
    setTimeZone(TimeZone.getTimeZone("GMT"))
  }
}
