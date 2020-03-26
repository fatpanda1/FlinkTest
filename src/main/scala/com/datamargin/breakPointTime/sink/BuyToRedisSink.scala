package com.datamargin.breakPointTime.sink

import com.datamargin.breakPointTime.utils.RedisUtils
import com.google.gson.JsonObject
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.functions.sink.{RichSinkFunction, SinkFunction}
import redis.clients.jedis.Jedis

class BuyToRedisSink extends RichSinkFunction[(String,String)] {

  private var jedis: Jedis = null

  /**
    * open方法在sink第一次启动时调用，一般用于sink的初始化操作
    */
  override def open(parameters: Configuration): Unit = {
    //建立redis连接
    jedis = RedisUtils.getJedisObject
  }

  /**
    * invoke方法是sink数据处理逻辑的方法，source端传来的数据都在invoke方法中进行处理
    * 其中invoke方法中第一个参数类型与RichSinkFunction<String>中的泛型对应。第二个参数
    * 为一些上下文信息
    */
  override def invoke(value: (String,String), context: SinkFunction.Context[_]): Unit = {
    //写入redis的key值为前缀+uid
    val redisKey = "BUY_" + value._1
    //写入redis的value为将数据源全量写入
    val redisValue = value._2
    //写入并设置过期时间为1.5h
    jedis.setex(redisKey,5400,redisValue)
  }

  /**
    * close方法在sink结束时调用，一般用于资源的回收操作
    */
  override def close(): Unit = {
    //关闭连接
    jedis.close()
  }

}
