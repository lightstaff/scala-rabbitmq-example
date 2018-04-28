package com.example.consumer

import scala.io.StdIn
import scala.util.{Failure, Success, Try}

import com.example.common.Model.Protocol
import com.example.common.ModelJsonProtocol
import com.rabbitmq.client.{AMQP, ConnectionFactory, DefaultConsumer, Envelope}
import com.typesafe.config.ConfigFactory
import spray.json._

object Main extends App with ModelJsonProtocol {
  // コンフィグロード
  val config = ConfigFactory.load()
  config.checkValid(ConfigFactory.defaultReference(), "consumer")

  val rabbitMQHost = config.getString("consumer.rabbitMQHost")
  val rabbitMQPort = config.getInt("consumer.rabbitMQPort")

  println("consumer start. press enter to stop")

  // ファクトリを使うらしい・・・
  val factory = new ConnectionFactory()
  factory.setHost(rabbitMQHost)
  factory.setPort(rabbitMQPort)

  // コネクションを繋いで・・・
  val connection = factory.newConnection()

  // チャンネル開いて・・・
  val channel = connection.createChannel()

  // Exchangeを作って・・・
  channel.exchangeDeclare("test", "fanout", false, true, null)

  // Queueを作って・・・
  val queName = channel.queueDeclare().getQueue

  // QueueとExchangeをBindして・・・
  channel.queueBind(queName, "test", "")

  // Consumerを定義して・・・
  val consumer = new DefaultConsumer(channel) {
    override def handleDelivery(consumerTag: String,
                                envelope: Envelope,
                                properties: AMQP.BasicProperties,
                                body: Array[Byte]): Unit = {
      val str = new String(body, "UTF-8")
      Try(str.parseJson.convertTo[Protocol]) match {
        case Success(p) =>
          println(s"[INFO] consumed. tag: ${envelope.getDeliveryTag}, msg: $p")
        case Failure(ex) =>
          println(s"[ERROR] ${ex.getMessage}")
      }
    }
  }

  // チャンネルにConsumerを設定して・・・
  channel.basicConsume(queName, true, consumer)

  // 待機して・・・
  StdIn.readLine()

  // チャンネルを閉じて・・・
  channel.close()

  // コネクションを閉じて・・・
  connection.close()

  println("consumer stop")
}
