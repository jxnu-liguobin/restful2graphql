package io.growing.graphql.route.defaults

import akka.http.scaladsl.model.{ ContentTypes, HttpEntity }
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route

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
