package com.flinkLearning.apiTest

import com.google.gson.JsonObject
import org.apache.flink.streaming.api.functions.ProcessFunction
import org.apache.flink.streaming.api.scala._
import org.apache.flink.util.Collector

object SideOutPutTest {
  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    val streamTextFile = env.readTextFile("F:\\learning\\FlinkLearning\\FlinkTest\\src\\main\\resources\\sensor.txt")
    val tag1 = new OutputTag[String]("tag1")
    //compareTag收集当前时间对和点击时间差小于一小时的数据
    val tag2 = new OutputTag[String]("tag2")
    val tokenStream = streamTextFile.map( data => {
      //将数据源切分
      val dataArray = data.split(",")
      //SensorReading对象方式返回
      SensorReading(dataArray(0).trim,dataArray(1).trim.toLong,dataArray(2).trim.toDouble)
    } )
      .process( new ProcessFunction[SensorReading,String] {
        override def processElement(i: SensorReading, context: ProcessFunction[SensorReading, String]#Context, collector: Collector[String]): Unit = {
          if (i.temperature > 10){
            context.output(tag1,i.id)
          }
          else {
            context.output(tag2,i.id)
          }
        }
      } )

    tokenStream.getSideOutput(tag1).print("tag1")
    tokenStream.getSideOutput(tag2).print("tag2")

    env.execute("sideOutputTest")

  }
}
