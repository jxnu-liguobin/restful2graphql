package io.growing.graphql.routes

import akka.http.scaladsl.model.{ ContentTypes, HttpEntity }
import akka.http.scaladsl.server.Route
import io.growing.graphql.Rest2GraphqlForwardServer.{ complete, get, path }

/**
 * 健康检查接口
 *
 * 没有使用依赖注入
 *
 * @author liguobin@growingio.com
 * @version 1.0,2020/7/10
 */
trait HealthyRouter {

  def healthyRoute(): Route = {
    path("healthy-check") {
      get {
        complete(HttpEntity(ContentTypes.`application/json`, "imok"))
      }
    }
  }

}
