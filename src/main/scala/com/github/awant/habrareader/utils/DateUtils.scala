package com.github.awant.habrareader.utils

import java.text.SimpleDateFormat
import java.util.{Calendar, Date}

import io.circe.syntax._
import io.circe.{Decoder, Encoder}

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

  def addDays(date: Date, days: Int): Date = {
    val cal = Calendar.getInstance()
    cal.setTime(date)
    cal.add(Calendar.DATE, days)
    cal.getTime
  }

  def getLast(left: Date, right: Date): Date =
    if (left.after(right))
      left
    else
      right

  def getMax(dates: Seq[Date]): Date = {
    require(dates.nonEmpty)
    var maxDate = dates.head
    dates.tail.foreach{ date => if (date.after(maxDate)) maxDate = date }
    maxDate
  }

  def yesterday: Date = addDays(currentDate, -1)

  implicit val dateEncoder: Encoder[Date] = (date: Date) => DateUtils.convertToStr(date).asJson

  implicit val dateDecoder: Decoder[Date] = Decoder[String].map(DateUtils.convertToDate(_))
}
