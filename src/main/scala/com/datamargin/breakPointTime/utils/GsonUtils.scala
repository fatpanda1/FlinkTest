package com.datamargin.breakPointTime.utils

import com.google.gson.Gson

class GsonUtils {

  private val gson = new Gson

  def getJson(uid:String,procode:String,time:String): String = {
    val click = JsonStringClass(uid,procode,time)
    gson.toJson(click)
  }

}

case class JsonStringClass(uid: String, procode: String, time: String)