package com.datamargin.breakPointTime.producer

import java.util.Properties

import com.datamargin.breakPointTime.utils.GsonUtils
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}


object BuyProducer {

  def main(args: Array[String]):Unit={

    val gsonUtils = new GsonUtils
    var gsonStr = ""

    val topic = "BPE_BUY"
    val brokers = "private001:9092"

    val props=new Properties()
    props.setProperty("bootstrap.servers", brokers)
    props.setProperty("group.id", "consumer-group")
    props.setProperty("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    props.setProperty("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    props.setProperty("auto.offset.reset", "latest")

    val producer = new KafkaProducer[String, String](props)

    for ( i <- 1 to 10){
      val uid: String = "uid" + i
      val procode: String = "procode" + i
      val time: String = "2020-03-26" + " " + (10 + i) + ":00:00"

      gsonStr = gsonUtils.getJson(uid,procode,time)
      val rcd = new ProducerRecord[String, String](topic, gsonStr)

      producer.send(rcd)
    }
    // 这里必须要调结束，否则kafka那边收不到消息
    producer.close()
  }

}
