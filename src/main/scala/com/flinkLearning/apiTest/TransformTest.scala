package com.flinkLearning.apiTest

import org.apache.flink.streaming.api.scala._

/**
  * 基本转换和聚合转换算子
  */

object TransformTest {
  def main(args: Array[String]): Unit = {

    val env = StreamExecutionEnvironment.getExecutionEnvironment

    val streamFromFile = env.readTextFile("F:\\learning\\FlinkLearning\\FlinkTest\\src\\main\\resources\\sensor.txt")

    val dataStream = streamFromFile.map(data => {
      //将数据源切分
      val dataArray = data.split(",")
      //SensorReading对象方式返回
      SensorReading(dataArray(0).trim,dataArray(1).trim.toLong,dataArray(2).trim.toDouble)
    })

    //keyBy后得到keyedStream
    dataStream.keyBy(_.id)
      //sum后又回到dataStream
      //.sum(2)
      //输出当前传感器最新的温度+10，而时间戳是上一次数据的时间+1
        .reduce((x,y) => SensorReading(x.id,x.time + 1,y.temperature + 10))
        .print()
    env.execute("transform test")
  }
}
