package com.flinkLearning.wc

import org.apache.flink.api.scala._
import org.apache.flink.api.scala.ExecutionEnvironment

//批处理代码
object WordCount {
  def main(args: Array[String]): Unit = {
    //创建一个批处理的执行环境
    val env = ExecutionEnvironment.getExecutionEnvironment

    //从文件中读取数据
    val inputPath = "F:\\learning\\FlinkLearning\\FlinkTest\\src\\main\\resources\\hello.txt"
    val inputDataSet = env.readTextFile(inputPath)

    //分词之后做count
    val wordCountDataSet = inputDataSet.flatMap(_.split(" "))
      .map((_,1))
      .groupBy(0)
      .sum(1)
      .print()
  }
}
