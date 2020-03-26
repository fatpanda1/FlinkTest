package com.datamargin.breakPointTime.utils

import java.text.SimpleDateFormat
import java.util.Date

object TimeUtils {
  //新建时间对象
  private val df = new SimpleDateFormat("yyy-MM-dd HH:mm:ss")

  //计算两时间差（分钟）
  def timeDiff(preDate: String, laterDate: String): Long = {
    //将时间字符串转为时间格式对象
    val preFormat = df.parse(preDate).getTime
    val laterFormat = df.parse(laterDate).getTime
    val minDiff = (laterFormat - preFormat) / 60000
    minDiff
  }

  //计算当前时间-输入时间（分钟）
  def timeNowDiff(date: String): Long = {
    val now = new Date().getTime
    val dateFormat = df.parse(date).getTime
    (now - dateFormat)/60000
  }

//  def main(args: Array[String]): Unit = {
//    println(timeDiff("2020-03-24 15:16:00", "2020-03-25 3:16:03"))
//    println(timeNowDiff("2020-03-24 12:00:00"))
//  }
}
