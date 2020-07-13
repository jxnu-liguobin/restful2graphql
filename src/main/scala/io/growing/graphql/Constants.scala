package io.growing.graphql

import com.google.gson.GsonBuilder
import okhttp3.MediaType

/**
 *
 * @author liguobin@growingio.com
 * @version 1.0,2020/7/9
 */
object Constants {


  val defaultCharset = "utf8"
  val json = MediaType.parse("application/json; charset=utf-8")


  //常用请求头
  val XUserId = "X-User-Id"
  val XRequestId = "X-Request-Id"
  val XInnerUserId = "X-Inner-User-Id"
  val XRealIP = "X-Real-IP"

  lazy val gson = new GsonBuilder().serializeNulls().create()


}
