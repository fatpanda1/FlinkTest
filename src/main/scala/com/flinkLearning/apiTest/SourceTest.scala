package com.flinkLearning.apiTest

import java.util.Properties

import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.streaming.api.functions.source.SourceFunction
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer011

object SourceTest {
  def main(args: Array[String]): Unit = {
    //获取执行环境
    val env = StreamExecutionEnvironment.getExecutionEnvironment

    //1.从自定义的集合中读取数据
//    val streamCollection = env.fromCollection(List(
//        SensorReading("sensor_1", 1547718199, 35.80018327300259),
//        SensorReading("sensor_6", 1547718201, 15.402984393403084),
//        SensorReading("sensor_7", 1547718202, 6.720945201171228),
//        SensorReading("sensor_10", 1547718205, 38.101067604893444)
//    ))

    //2.从文件中读取数据
//    val streamTextFile = env.readTextFile("F:\\learning\\FlinkLearning\\FlinkTest\\src\\main\\resources\\sensor.txt")

    //3.从自定义类型中读取
//    env.fromElements(1,"name")

    /**
      * 4.从kafka中读取数据
      */
    val properties = new Properties()
    properties.setProperty("bootstrap.servers", "private001:9092")
    properties.setProperty("group.id", "consumer-group")
    properties.setProperty("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
    properties.setProperty("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
    properties.setProperty("auto.offset.reset", "latest")

    /**
      * 添加kafka源
      * 参数：topic  序列化工具 配置对象
      */
    val streamKafka = env.addSource(new FlinkKafkaConsumer011[String](
      "sensorTest",
      new SimpleStringSchema(),
      properties
    ))



    //sink
//    streamCollection.print("streamCollection").setParallelism(1)
//    streamCollection.print("streamTextFile").setParallelism(1)
    streamKafka.print("StreamKafka").setParallelism(1)

    //执行
    env.execute("sourceJob")
  }
}

//温度传感器样例类
case class SensorReading(id: String, time: Long, temperature: Double)