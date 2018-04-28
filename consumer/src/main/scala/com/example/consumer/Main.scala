package com.example.consumer

import scala.io.StdIn
import scala.util.{Failure, Success, Try}

import com.example.common.Model.Protocol
import com.example.common.ModelJsonProtocol
import com.rabbitmq.client.{AMQP, ConnectionFactory, DefaultConsumer, Envelope}
import com.typesafe.config.ConfigFactory
import spray.json._

object Main extends App with ModelJsonProtocol {
  val config = ConfigFactory.load()
  config.checkValid(ConfigFactory.defaultReference(), "consumer")

  val rabbitMQHost = config.getString("consumer.rabbitMQHost")
  val rabbitMQPort = config.getInt("consumer.rabbitMQPort")

  println("consumer start. press enter to stop")

  val factory = new ConnectionFactory()
  factory.setHost(rabbitMQHost)
  factory.setPort(rabbitMQPort)

  val connection = factory.newConnection()

  val channel = connection.createChannel()

  channel.exchangeDeclare("test", "fanout", false, true, null)

  val queName = channel.queueDeclare().getQueue

  channel.queueBind(queName, "test", "")

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

  channel.basicConsume(queName, true, consumer)

  StdIn.readLine()

  channel.close()
  connection.close()

  println("consumer stop")
}
