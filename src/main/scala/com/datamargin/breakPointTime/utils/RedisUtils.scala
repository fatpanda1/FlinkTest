package com.datamargin.breakPointTime.utils

import redis.clients.jedis.Jedis

object RedisUtils {
  val host: String = "private001"
  val port: Int = 6379

  def getJedisObject = {
    val jedis: Jedis = new Jedis(host,port)
    jedis
  }
}
