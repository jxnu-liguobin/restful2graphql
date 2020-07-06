package io.growing.graphql

/**
 *
 * @author liguobin@growingio.com
 * @version 1.0,2020/7/6
 */
object RestMethod extends Enumeration {

  type HttpMethod = Value

  val POST = Value("post")
  val DELETE = Value("delete")
  val PUT = Value("put")
  val GET = Value("get")
}
