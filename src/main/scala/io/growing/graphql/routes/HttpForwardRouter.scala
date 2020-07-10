package io.growing.graphql.routes

import akka.http.scaladsl.server.Route
import io.growing.graphql.{ GraphqlExecution, RestOperation }
import io.growing.graphql.request.RestRequest
import spray.json.JsValue

import scala.concurrent.ExecutionContext

/**
 * 转发接口
 *
 * @author liguobin@growingio.com
 * @version 1.0,2020/7/10
 */
trait HttpForwardRouter extends HttpSupport with GraphqlExecution {

  override implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

  override def route: Route = {
    path(ProjectMatcher.getRestUriPathMather / ResourceMatcher / ResourceIdMatcher) { (projectId, typ, id) =>
      concat(
        get {
          headerValueByName(authKey) { authValue =>
            //查询一个
            val req = RestRequest(restOperation = RestOperation.GET, resource = typ,
              contextParams = projectId.fold(Map("id" -> id))(pid => Map("id" -> id, "projectId" -> pid, authKey -> authValue)))
            val ret = executeRequest(req.toGraphqlRequest())
            responseJsonData(ret)
          }
        }
          ~ delete {
          headerValueByName(authKey) { authValue =>
            //删除一个
            val req = RestRequest(restOperation = RestOperation.DELETE, resource = typ,
              contextParams = projectId.fold(Map("id" -> id))(pid => Map("id" -> id, "projectId" -> pid, authKey -> authValue)))
            val ret = executeRequest(req.toGraphqlRequest())
            responseJsonData(ret)
          }
        }
          ~ put {
          headerValueByName(authKey) { authValue =>
            //更新
            decodeRequest {
              entity(as[JsValue]) { requestBody =>
                //需要组合requestBody和queryParams
                val req = RestRequest(restOperation = RestOperation.UPDATE, requestBody = Some(requestBody),
                  resource = typ, contextParams = projectId.fold(Map("id" -> id))(pid => Map("id" -> id, "projectId" -> pid, authKey -> authValue)))
                val ret = executeRequest(req.toGraphqlRequest())
                responseJsonData(ret)
              }
            }
          }
        }
      )
    } ~
      path(ProjectMatcher.getRestUriPathMather / ResourceMatcher) { (projectId, typ) =>
        concat(
          get {
            headerValueByName(authKey) { authValue =>
              //查询所有资源的列表
              val req = RestRequest(restOperation = RestOperation.GET, resource = typ,
                contextParams = projectId.fold(Map.empty[String, String])(pid => Map("projectId" -> pid, authKey -> authValue)))
              val ret = executeRequest(req.toGraphqlRequest())
              responseJsonData(ret)
            }
          }
            ~
            post {
              headerValueByName(authKey) { authValue =>
                //创建
                decodeRequest {
                  entity(as[JsValue]) { requestBody =>
                    val req = RestRequest(restOperation = RestOperation.CREATE, requestBody = Some(requestBody),
                      resource = typ, contextParams = projectId.fold(Map.empty[String, String])(pid => Map("projectId" -> pid, authKey -> authValue)))
                    val ret = executeRequest(req.toGraphqlRequest())
                    responseJsonData(ret)

                  }
                }
              }
            }
            ~
            delete {
              //批量删除
              headerValueByName(authKey) { authValue =>
                decodeRequest {
                  entity(as[JsValue]) { requestBody =>
                    val req = RestRequest(restOperation = RestOperation.DELETE, requestBody = Some(requestBody),
                      resource = typ, contextParams = projectId.fold(Map.empty[String, String])(pid => Map("projectId" -> pid, authKey -> authValue)), isBatch = true)
                    val ret = executeRequest(req.toGraphqlRequest())
                    responseJsonData(ret)

                  }
                }
              }
            }
            ~
            put {
              //批量更新
              headerValueByName(authKey) { authValue =>
                decodeRequest {
                  entity(as[JsValue]) { requestBody =>
                    val req = RestRequest(restOperation = RestOperation.UPDATE, requestBody = Some(requestBody),
                      resource = typ, contextParams = projectId.fold(Map.empty[String, String])(pid => Map("projectId" -> pid, authKey -> authValue)), isBatch = true)
                    val ret = executeRequest(req.toGraphqlRequest())
                    responseJsonData(ret)

                  }
                }
              }
            }
        )
      }
  }

}
