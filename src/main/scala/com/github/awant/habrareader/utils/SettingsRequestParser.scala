package com.github.awant.habrareader.utils

import com.github.awant.habrareader.utils.ChangeCommand.ChangeCommand

object ChangeCommand extends Enumeration {
  type ChangeCommand = Value
  val RESET, CLEAR, SET, UNKNOWN = Value
}

case class SettingsRequestCmd(cmd: ChangeCommand, args: Seq[String] = Seq.empty, err: String = "")


object SettingsRequestParser {
  val availableSetCmds: Seq[String] = Seq(
    "excludedAuthor",
    "excludedCategory",
    "author",
    "category"
  )
  def wrongCommand(err: String): SettingsRequestCmd = SettingsRequestCmd(ChangeCommand.UNKNOWN, err = err)

  private def parseSetCmd(rawArgs: Seq[String]): SettingsRequestCmd = {
    if (rawArgs.length != 2) wrongCommand("'set' command should have one argument")
    else {
      var cmd = rawArgs.head.substring(3)
      cmd = cmd(0).toLower + cmd.tail
      if (!availableSetCmds.contains(cmd)) wrongCommand("the command not available")
      else {
        SettingsRequestCmd(ChangeCommand.SET, Seq(cmd, rawArgs.tail.head))
      }
    }
  }

  def parse(rawCmd: String): SettingsRequestCmd = rawCmd match {
    case "reset" => SettingsRequestCmd(ChangeCommand.RESET)
    case "clear" => SettingsRequestCmd(ChangeCommand.CLEAR)
    case _ =>
      if (rawCmd.startsWith("set")) parseSetCmd(rawCmd.split("\\s+"))
      else wrongCommand("the command should start with the 'set' key word")
  }
}
