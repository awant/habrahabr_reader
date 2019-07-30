package com.github.awant.habrareader

import scala.util.{Failure, Success, Try}

object Implicits {

  implicit class CloseableExt[T <: AutoCloseable](private val a: T) extends AnyVal {

    def use[R](func: T => R): R = {
      Try(func(a)) match {
        case Success(result) =>
          a.close()
          result
        case Failure(exception) =>
          Try(a.close()).failed.foreach(ex => exception.addSuppressed(ex))
          throw exception
      }
    }
  }

}
