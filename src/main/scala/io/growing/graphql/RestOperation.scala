package io.growing.graphql

/**
 *
 * @author liguobin@growingio.com
 * @version 1.0,2020/7/6
 */
object RestOperation extends Enumeration {

  type RestOperation = Value

  val CREATE = Value("create")
  val DELETE = Value("delete")
  val UPDATE = Value("update")
  val GET = Value("get")
}
