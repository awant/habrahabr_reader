package com.github.awant.habrareader

import java.io.File

import org.scalatest.FunSuite

import scala.io.Source

class RssParserTest extends FunSuite {

  test("testParse") {
    val file = new File(getClass.getClassLoader.getResource("exampleOfHabrRss.xml").getFile)

    assert(file.exists())

    val text = Source.fromFile(file).getLines().mkString("\n")
    val result = RssParser.parse(text)

    assert(result.nonEmpty)

    val first = result.head

    assert(first.id == 461617)
    assert(first.link == "https://habr.com/ru/post/461617/")
    assert(first.description.startsWith("День добрый, Хабр! <br/>"))
    assert(first.author == "TimurBidzhiev")
    assert(first.categories.contains("Управление проектами"))
    assert(first.categories.contains("обучение"))
    assert(first.categories.contains("стартапы"))
    assert(first.categories.size == 7)
  }
}
