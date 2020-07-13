package io.growing.graphql.route.defaults

import io.growing.graphql.GraphqlExecution

/**
 * 该接口用于HTTP接收请求后，在本地调用graphql
 *
 * @author liguobin@growingio.com
 * @version 1.0,2020/7/10
 */
trait HttpLocalRouter extends HttpLocalSupport with GraphqlExecution {

  //TODO

}
