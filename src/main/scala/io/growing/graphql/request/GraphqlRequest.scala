package io.growing.graphql.request

/**
 *
 * @author liguobin@growingio.com
 * @version 1.0,2020/7/6
 */
class GraphqlRequest[T](operationName: String, variables: Option[GraphqlRequestBody[T]] = None, graphqlQuery: String) extends Request {

  override def toString: String = super.toString

}
