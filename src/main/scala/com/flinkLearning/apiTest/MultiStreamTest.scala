package com.flinkLearning.apiTest

import org.apache.flink.streaming.api.scala._

object MultiStreamTest {
  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment

    val streamFromFile = env.readTextFile("F:\\learning\\FlinkLearning\\FlinkTest\\src\\main\\resources\\sensor.txt")

    val dataStream = streamFromFile.map(data => {
      //将数据源切分
      val dataArray = data.split(",")
      //SensorReading对象方式返回
      SensorReading(dataArray(0).trim,dataArray(1).trim.toLong,dataArray(2).trim.toDouble)
    })

    //1.多流转换算子
    val splitStream = dataStream.split( data => {
      //Seq是列表，适合存有序重复数据，进行快速插入/删除元素等场景
      //按照源数据中温度高低将数据分流
      if ( data.temperature > 30)
        Seq("high")
      else Seq("low")
    } )

    //提取高温流
    val high = splitStream.select("high")

    //提取低温流
    val low = splitStream.select("low")

    //提取所有流
//    val all = splitStream.select("high","low")
//
//    high.print("high")
//    low.print("low")
//    all.print("all")

    //2.合并两条流
//    val warning = high.map( data => (data.id,data.temperature) )
//    //使用connect连接
//    val connectedStreams = warning.connect(low)
//
//    //在connectstream中，可以将两条流分别处理
//    val coMapDataStream = connectedStreams.map(
//      warningData => (warningData._1,warningData._2,"warning"),
//      lowData => ( lowData.id, "healthy" )
//    )

    //3.合并多条流(Union)
    val unionStream = high.union(low).union(high)




    env.execute("mulitStream")

  }
}
