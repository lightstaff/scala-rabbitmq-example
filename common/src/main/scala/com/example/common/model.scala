package com.example.common

import spray.json._

object Model {

  final case class Protocol(message: String, timestamp: Long)

}

trait ModelJsonProtocol extends DefaultJsonProtocol {

  import Model._

  implicit val protocolFormat: RootJsonFormat[Protocol] = jsonFormat2(Protocol)

}
