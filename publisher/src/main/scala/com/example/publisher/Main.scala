package com.example.publisher

import java.time.{LocalDate, ZoneId}

import scala.util.{Failure, Success, Try}

import com.example.common.Model.Protocol
import com.example.common.ModelJsonProtocol
import com.rabbitmq.client.ConnectionFactory
import com.typesafe.config.ConfigFactory
import spray.json._

object Main extends App with ModelJsonProtocol {
  val config = ConfigFactory.load()
  config.checkValid(ConfigFactory.defaultReference(), "publisher")

  val rabbitMQHost = config.getString("publisher.rabbitMQHost")
  val rabbitMQPort = config.getInt("publisher.rabbitMQPort")

  println("publisher start")

  val factory = new ConnectionFactory()
  factory.setHost(rabbitMQHost)
  factory.setPort(rabbitMQPort)

  val connection = factory.newConnection()

  val channel = connection.createChannel()

  channel.exchangeDeclare("test", "fanout", false, true, null)

  Seq.range(1, 4).foreach { i =>
    val p =
      Protocol(s"Hello. No$i", LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toEpochSecond)

    Try(channel.basicPublish("test", "", null, p.toJson.compactPrint.getBytes)) match {
      case Success(_) =>
        println(s"[INFO] published. msg: $p")
      case Failure(ex) =>
        println(s"[ERROR] ${ex.getMessage}")
    }
  }

  channel.close()
  connection.close()

  println("publisher stop")
}
