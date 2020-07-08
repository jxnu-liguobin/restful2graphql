package io.growing.graphql

/**
 * restful 请求的方法映射到资源的操作上
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
