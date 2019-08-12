package com.github.awant.habrareader.utils

object ParsedCommand {

  def unapply(text: String): Option[(String, String)] = {
    val pos = text.indexOf(' ')
    if (pos == -1 || text.size <= pos + 1) {
      None
    } else {
      val cmd = text.substring(0, pos)
      val remaining = text.substring(pos + 1, text.size)
      Option((cmd, remaining))
    }
  }
}
