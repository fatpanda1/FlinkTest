package com.flinkLearning.apiTest

import org.apache.flink.api.common.functions.FilterFunction
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment

object FlinkUDF {
  def main(args: Array[String]): Unit = {
    //获取执行环境
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    //从文件中读取数据
    val streamTextFile = env.readTextFile("F:\\learning\\FlinkLearning\\FlinkTest\\src\\main\\resources\\sensor.txt")

  }
}
