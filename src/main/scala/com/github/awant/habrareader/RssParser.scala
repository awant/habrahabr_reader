package com.github.awant.habrareader

import scala.io.Source


import com.github.awant.habrareader.Implicits._


object RssParser {

	/** may block thread or throw exceptions */
	def loadPosts(url: String): Seq[HabraPost] = parse(getTextFromUrl(url))

	def getTextFromUrl(url: String): String = Source.fromURL(url).use(_.mkString("\n"))

	def parse(text: String): Seq[HabraPost] = ??? // todo
}
