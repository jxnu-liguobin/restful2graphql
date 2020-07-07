package io.growing.graphql

import akka.http.scaladsl.model.{ ContentTypes, HttpEntity }
import akka.http.scaladsl.server.{ HttpApp, Route }
import io.growing.graphql.request.RestRequest
import io.growing.graphql.utils.GraphqlScanner

import scala.concurrent.Future

/**
 *
 * @author liguobin@growingio.com
 * @version 1.0,2020/7/7
 */
object ForwardServer extends HttpApp with App {

  val responseJsonData: Future[String] => Route = (result: Future[String]) => onSuccess(result) { r => complete(HttpEntity(ContentTypes.`application/json`, r)) }

  implicit lazy val operationQueryMappings: Map[String, String] = GraphqlScanner.createOperationQueryMappings()

  override def routes: Route =
    path("rest" / ResourceTypeMatcher / ResourceIdMatcher) { (typ, id) =>
      concat(
        get {
          //查询一个
          val req = RestRequest(restOperation = RestOperation.GET, resource = typ, queryParams = Map("id" -> id))
          val ret = GraphqlExecution.executeRequest(req.toGraphqlRequest())
          responseJsonData(ret)
        }
          ~ delete {
          //删除一个
          val req = RestRequest(restOperation = RestOperation.DELETE, resource = typ, queryParams = Map("id" -> id))
          val ret = GraphqlExecution.executeRequest(req.toGraphqlRequest())
          responseJsonData(ret)
        }
          ~ put {
          //更新
          decodeRequest {
            entity(as[String]) { requestBody =>
              //需要组合requestBody和queryParams
              val req = RestRequest(restOperation = RestOperation.UPDATE, requestBody = Some(requestBody), resource = typ, queryParams = Map("id" -> id))
              val ret = GraphqlExecution.executeRequest(req.toGraphqlRequest())
              responseJsonData(ret)
            }
          }
        }
      )
    } ~
      path("rest" / ResourceTypeMatcher) { typ =>
        concat(
          get {
            //查询列表，批量查询
            decodeRequest {
              entity(as[String]) { requestBody =>
                val req = RestRequest(restOperation = RestOperation.GET, requestBody =  Some(requestBody), resource = typ, queryParams = Map.empty, isBatch = true)
                val ret = GraphqlExecution.executeRequest(req.toGraphqlRequest())
                responseJsonData(ret)
              }
            }
          }
            ~
            post {
              //创建
              decodeRequest {
                entity(as[String]) { requestBody =>
                  val req = RestRequest(restOperation = RestOperation.CREATE, requestBody = Some(requestBody), resource = typ, queryParams = Map.empty)
                  val ret = GraphqlExecution.executeRequest(req.toGraphqlRequest())
                  responseJsonData(ret)

                }
              }
            }
            ~
            delete {
              //批量删除
              decodeRequest {
                entity(as[String]) { requestBody =>
                  val req = RestRequest(restOperation = RestOperation.DELETE, requestBody = Some(requestBody), resource = typ, queryParams = Map.empty, isBatch = true)
                  val ret = GraphqlExecution.executeRequest(req.toGraphqlRequest())
                  responseJsonData(ret)

                }
              }
            }
        )
      }

  ForwardServer.startServer("localhost", 8080)
}
