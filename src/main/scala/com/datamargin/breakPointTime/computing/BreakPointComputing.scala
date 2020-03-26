package com.datamargin.breakPointTime.computing

import java.util
import java.util.Properties

import com.datamargin.breakPointTime.sink.BuyToRedisSink
import com.datamargin.breakPointTime.utils.{GsonUtils, RedisUtils, TimeUtils}
import com.google.gson.{Gson, JsonElement, JsonObject, JsonParser}
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.streaming.api.functions.ProcessFunction
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.connectors.kafka.{FlinkKafkaConsumer011, FlinkKafkaProducer011}
import org.apache.flink.util.{Collector, StringUtils}
object BreakPointComputing {

  //获取flink流处理执行环境
  private val env = StreamExecutionEnvironment.getExecutionEnvironment
  //添加kafka配置文件
  private val kafkaProp = new Properties()
  kafkaProp.setProperty("bootstrap.servers", "private001:9092")
  kafkaProp.setProperty("group.id", "consumer-group")
  kafkaProp.setProperty("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
  kafkaProp.setProperty("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
  kafkaProp.setProperty("auto.offset.reset", "latest")

  private val toCycle = 1
  private val toOutput = 2

  /**
    * 购买事件处理
    */
  def buyComputing: Unit = {

    //获取购买事件topic的流
    val buyStream = env.addSource(new FlinkKafkaConsumer011[String](
      "BPE_BUY",
      new SimpleStringSchema(),
      kafkaProp
    ))

    val buyOut = buyStream.map(data => {
      val jsonObject = new JsonParser().parse(data).getAsJsonObject
      val uid = jsonObject.get("uid").getAsString
      (uid, data)
    })

    //将购买时间数据通过自定义sink写入redis
    buyOut.addSink(new BuyToRedisSink)
  }

  /**
    * 点击事件处理
    */
  def clickComputing: Unit = {

    //获取点击事件topic和循环topic
    val list: util.ArrayList[String] = new util.ArrayList[String]()
    list.add("BPE_CLICK")
    list.add("BPE_CYCLE")

    //获取点击事件和循环topic的流
    val clickStream = env.addSource(new FlinkKafkaConsumer011[String](
      list,
      new SimpleStringSchema(),
      kafkaProp
    ))

    //标识sideOutPut的Tag
    //outputTag收集当前时间对和点击时间差大于一小时的数据
    val nowAndClickMore = new OutputTag[JsonObject]("nowAndClickMore")
    //compareTag收集当前时间对和点击时间差小于一小时的数据
    val nowAndClickLess = new OutputTag[JsonObject]("nowAndClickLess")

    //将源数据提取点击时间并和当前时间做计算,并将源数据转换为json对象
    val tokenStream = clickStream.map(data => {
      val jsonObject = new JsonParser().parse(data).getAsJsonObject
      val time = jsonObject.get("time").getAsString
      //获取当前时间和点击时间的时间差
      val diff: Long = TimeUtils.timeDiff(time,"2020-03-26 15:00:00")
      println("第一步切分" + (diff, jsonObject))
      //输出时间差和转换完成的json对象
      (diff, jsonObject)
    })
      //根据时间差将数据分流，大于一小时则放入nowAndClickMore,小于一小时则放在nowAndClickLess
      .process(new ProcessFunction[(Long, JsonObject), JsonObject] {
      override def processElement(i: (Long, JsonObject), context: ProcessFunction[(Long, JsonObject), JsonObject]#Context, collector: Collector[JsonObject]): Unit = {
        if (i._1 > 60) {
          context.output(nowAndClickMore, i._2)
        }
        else {
          context.output(nowAndClickLess, i._2)
        }
      }
    })

    //大于一小时的数据处理
    //将outputTag中的数据转换为字符串以便输出
    tokenStream.getSideOutput(nowAndClickMore).map(data => {
      println(data.toString)
      data.toString
    })
      //将时间差大于一小时的数据流输出到BPE_OUTPUT Topic中
      .addSink(new FlinkKafkaProducer011[String](
        "private001:9092",
        "BPE_OUTPUT",
        new SimpleStringSchema()
      ))

    //标识sideOutPut的Tag
    //clickAndBUYtoCycle收集和购买事件没有匹配的点击事件的数据
    val clickAndBUYtoCycle = new OutputTag[String]("clickAndBUYtoCycle")
    //clickAndBUYtoOutput收集购买事件和点击事件相差大于一小时的数据
    val clickAndBUYtoOutput = new OutputTag[String]("clickAndBUYtoOutput")

    //小于一小时的数据则与redis中的购买数据做对比
//    val nowLessTestStream = tokenStream.getSideOutput(nowAndClickLess).map(_.toString).print()
    val nowAndClickLessStream = tokenStream.getSideOutput(nowAndClickLess)
      .map(data => {
        //获取点击数据中的关键字
        val clickUid = data.get("uid").getAsString
        val clickTime = data.get("time").getAsString
        //获取redis连接
        val jedis = RedisUtils.getJedisObject
        val buyData = jedis.get("BUY_" + clickUid)
        //判断是否有和点击数据对应的
        if (StringUtils.isNullOrWhitespaceOnly(buyData)){
          //没有对应数据则输出到BPE_CYCLE
          (toCycle,data.toString)
        }else {
          //有对应数据则获取购买时间，并比对点击时间和购买时间
          val buyJson = new JsonParser().parse(buyData).getAsJsonObject
          val buyTime = buyJson.get("time").getAsString
          println("购买时间" + buyTime)
          //购买时间-点击时间大于60分钟,输出到BPE_OUTPUT
          if (TimeUtils.timeDiff(clickTime,buyTime) > 60){
            println("输出click时间" + data.toString)
            (toOutput,data.toString)
          } else {
            //小于60分钟则丢弃数据
            (0,"")
          }
        }
      })
      //将流分为toCycle和toOutput
      .process(new ProcessFunction[(Int,String), String] {
        override def processElement(i: (Int, String), context: ProcessFunction[(Int, String), String]#Context, collector: Collector[String]): Unit = {
          if (i._1 == toCycle){
            context.output(clickAndBUYtoCycle,i._2)
          }
          if (i._1 == toOutput){
            context.output(clickAndBUYtoOutput,i._2)
          }
        }
      })

    //点击事件没有对应的购买事件输出到BPE_CYCLE
    nowAndClickLessStream
      .getSideOutput(clickAndBUYtoCycle)
      .addSink(new FlinkKafkaProducer011[String](
        "private001:9092",
        "BPE_CYCLE",
        new SimpleStringSchema()
      ))

    //点击时间和购买时间差大于一小时输出到BPE_OUTPUT
    nowAndClickLessStream
      .getSideOutput(clickAndBUYtoOutput)
      .addSink(new FlinkKafkaProducer011[String](
        "private001:9092",
        "BPE_OUTPUT",
        new SimpleStringSchema()
      ))
  }

  /**
    * Main
    *
    * @param args
    */
  def main(args: Array[String]): Unit = {
    //购买事件
    buyComputing
    //点击事件
    clickComputing
    //注册job
    env.execute("BreakPointEvent")
  }
}
