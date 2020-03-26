package com.flinkLearning.wc

import org.apache.flink.streaming.api.scala._

object StreamWordCount {
  def main(args: Array[String]): Unit = {
    //创建一个流处理的执行环境
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    //接收socket数据流
    val textDataStream = env.socketTextStream("localhost",7777)
    //逐一读取数据，打散后进行wordcount
    textDataStream.flatMap(x => x.split("\\s"))
      .filter(_.nonEmpty)
      .map((_,1))
      .keyBy(0) //datastream中没有groupby，使用keyby进行分组
      .sum(1)
      .print()

    //执行任务
    env.execute("stream wordcount job")
  }
}
