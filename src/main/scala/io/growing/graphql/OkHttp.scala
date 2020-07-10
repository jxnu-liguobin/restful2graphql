package io.growing.graphql

import java.util
import java.util.concurrent.TimeUnit

import okhttp3.{ OkHttpClient, Protocol }

/**
 * http调用，用来将请求转发到graphql服务器
 *
 * @author liguobin@growingio.com
 * @version 1.0,2020/7/6
 */
object OkHttp {


  private lazy val defaultTimeout: Long = TimeUnit.MINUTES.toMillis(1)
  lazy val client: OkHttpClient = buildClient(defaultTimeout, defaultTimeout, defaultTimeout)

  def buildClient(readTimeout: Long, writeTimeout: Long, connectTimeout: Long): OkHttpClient = {
    new OkHttpClient.Builder()
      .readTimeout(readTimeout, TimeUnit.MILLISECONDS)
      .writeTimeout(writeTimeout, TimeUnit.MILLISECONDS)
      .connectTimeout(connectTimeout, TimeUnit.MILLISECONDS)
      .protocols(util.Arrays.asList(Protocol.HTTP_1_1, Protocol.HTTP_2))
      .build()
  }
}
