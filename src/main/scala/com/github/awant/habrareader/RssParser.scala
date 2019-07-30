package com.github.awant.habrareader

import com.github.awant.habrareader.Implicits._

import scala.io.Source
import scala.xml.XML


object RssParser {

  /** may block thread or throw exceptions */
  def loadPosts(url: String): Seq[HabraPost] = parse(getTextFromUrl(url))

  def getTextFromUrl(url: String): String = Source.fromURL(url).use(_.getLines().mkString("\n"))

  def parse(text: String): Seq[HabraPost] = {
    val root = XML.loadString(text)

    val items = root \ "channel" \ "item"

    items.toList.map { item =>
      val link = (item \ "guid").text

      HabraPost(
        id = link.filter(_.isDigit).toInt,
        link = link,
        title = (item \ "title").text,
        description = (item \ "description").text,
        author = (item \ "creator").text,
        categories = (item \ "category").map(_.text).toSet
      )
    }
  }
}
