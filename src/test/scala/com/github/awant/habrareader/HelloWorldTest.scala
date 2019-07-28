package com.github.awant.habrareader

import org.scalatest.FunSuite

class HelloWorldTest extends FunSuite {

	test("testHelloMessage") {
		assert(HelloWorld.helloMessage == "Hello world!")
	}

}
