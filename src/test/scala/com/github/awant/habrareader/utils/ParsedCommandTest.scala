package com.github.awant.habrareader.utils

import org.scalatest.FunSuite

class ParsedCommandTest extends FunSuite {

  test("testUnapply") {
    "/cmd text" match {
      case ParsedCommand(cmd, text) =>
        assert(cmd == "/cmd")
        assert(text == "text")
    }

    "/c t" match {
      case ParsedCommand(cmd, text) =>
        assert(cmd == "/c")
        assert(text == "t")
    }
  }

}
