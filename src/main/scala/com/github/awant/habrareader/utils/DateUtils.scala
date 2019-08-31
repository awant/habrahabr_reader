package com.github.awant.habrareader.utils

import java.text.SimpleDateFormat
import java.util.{Calendar, Date}

import scala.concurrent.duration.FiniteDuration

object DateUtils {
  def currentDate: Date = Calendar.getInstance().getTime
  def convertToStr(date: Date, fmt: String = "yyyy-MM-dd HH:mm:ssZ"): String = new SimpleDateFormat(fmt).format(date)
  def currentDateStr(fmt: String = "yyyy-MM-dd HH:mm:ssZ"): String = convertToStr(currentDate, fmt)
  def convertToDate(date: String, fmt: String = "yyyy-MM-dd HH:mm:ssZ"): Date = new SimpleDateFormat(fmt).parse(date)
  def add(date: Date, delta: FiniteDuration): Date = {
    val cal = Calendar.getInstance()
    cal.setTime(date)
    cal.add(Calendar.SECOND, delta.toSeconds.toInt)
    cal.getTime
  }
}
