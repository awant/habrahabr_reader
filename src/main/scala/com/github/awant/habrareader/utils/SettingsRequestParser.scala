package com.github.awant.habrareader.utils

import com.github.awant.habrareader.utils.ChangeCommand.ChangeCommand

object ChangeCommand extends Enumeration {
  type ChangeCommand = Value
  val RESET, CLEAR, SET, UNKNOWN = Value
}

case class SettingsRequestCmd(cmd: ChangeCommand, args: Seq[String] = Seq.empty)


object SettingsRequestParser {
  val availableSetCmds: Seq[String] = Seq(
    "setExcludedAuthor",
    "setExcludedCategory",
    "setAuthor",
    "setCategory"
  )
  val unknownSettingsRequestCmd = SettingsRequestCmd(ChangeCommand.UNKNOWN)

  private def parseSetCmd(rawArgs: Seq[String]): SettingsRequestCmd = {
    if (rawArgs.length != 2) unknownSettingsRequestCmd
    else {
      val cmd = rawArgs.head
      if (!availableSetCmds.contains(cmd)) unknownSettingsRequestCmd
      else {
        val cmdExt = StringUtils.decapitalize(cmd.substring(ChangeCommand.SET.toString.length))
        SettingsRequestCmd(ChangeCommand.SET, Seq(cmdExt, rawArgs.tail.head))
      }
    }
  }

  def parse(rawCmd: String): SettingsRequestCmd = rawCmd match {
    case "reset" => SettingsRequestCmd(ChangeCommand.RESET)
    case "clear" => SettingsRequestCmd(ChangeCommand.CLEAR)
    case _ =>
      if (rawCmd.startsWith("set")) parseSetCmd(rawCmd.split("\\s+"))
      else unknownSettingsRequestCmd
  }
}
