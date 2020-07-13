package io.growing.graphql.route.defaults

import akka.http.scaladsl.server.Route
import io.growing.graphql.JsonMarshallers

/**
 *
 * @author liguobin@growingio.com
 * @version 1.0,2020/7/10
 */
trait HttpRouter extends JsonMarshallers {

  def route: Route

}