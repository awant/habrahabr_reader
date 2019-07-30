package com.github.awant.habrareader

import java.io.File
import java.util.{Calendar, Date, TimeZone}

import org.scalatest.FunSuite

import scala.io.Source
import com.github.awant.habrareader.Implicits._

class RssParserTest extends FunSuite {

	test("testParse") {
		val file = new File(getClass.getClassLoader.getResource("exampleOfHabrRss.xml").getFile)

		assert(file.exists())

		val text = Source.fromFile(file).use {
			_.getLines().mkString("\n")
		}
		val result = RssParser.parse(text)

		assert(result.nonEmpty)

		val first = result.head

		assert(first.id == 461617)
		assert(first.link == "https://habr.com/ru/post/461617/")
		assert(first.description.startsWith("День добрый, Хабр! <br/>"))
		assert(first.author == "TimurBidzhiev")
		assert(first.date.before(new Date()))
		assert(first.categories.contains("Управление проектами"))
		assert(first.categories.contains("обучение"))
		assert(first.categories.contains("стартапы"))
		assert(first.categories.size == 7)
	}

	test("parseDate") {
		val string = "Sun, 28 Jul 2019 13:23:13 GMT"
		val date = RssParser.parseDate(string)
		val c = Calendar.getInstance(TimeZone.getTimeZone("GMT"))
		c.setTime(date)

		assert(c.get(Calendar.DAY_OF_MONTH) == 28)
		assert(c.get(Calendar.MONTH) == Calendar.JULY)
		assert(c.get(Calendar.YEAR) == 2019)
		assert(c.get(Calendar.HOUR_OF_DAY) == 13)
		assert(c.get(Calendar.MINUTE) == 23)
		assert(c.get(Calendar.SECOND) == 13)
	}
}
