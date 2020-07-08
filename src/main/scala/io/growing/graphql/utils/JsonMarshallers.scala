package io.growing.graphql.utils

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.{ DefaultJsonProtocol, JsonFormat, JsString, JsValue }

/**
 * spray-json 解析json string
 *
 * @author liguobin@growingio.com
 * @version 1.0,2020/7/8
 */
object JsonMarshallers extends DefaultJsonProtocol with SprayJsonSupport {

  implicit val stringParser = new JsonFormat[String] {

    def write(x: String): JsString = JsString(x)

    def read(value: JsValue): String = value match {
      case JsString(x) => x
      case x => throw new RuntimeException(s"Unexpected type %s on parsing of JsString".format(x.getClass.getName))
    }
  }
}