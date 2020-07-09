package io.growing.graphql

import akka.actor.ActorSystem
import akka.http.scaladsl.model.{ ContentTypes, HttpEntity }
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import io.growing.dryad.ServiceProvider
import io.growing.dryad.portal.Schema
import io.growing.graphql.request.RestRequest
import io.growing.graphql.utils.{ Config, GraphqlScanner }
import io.growing.graphql.utils.JsonMarshallers._
import spray.json._

import scala.concurrent.Future
import scala.io.StdIn

/**
 * 使用akkahttp转发请求
 *
 * @author liguobin@growingio.com
 * @version 1.0,2020/7/7
 */
object Rest2GraphqlForwardServer extends App {

  import scala.util.{ Failure, Success, Try }

  private val (host, port) = Config.getRestfulServerConfig()
  private[this] val serviceProvider = ServiceProvider()
  private[this] val prefixs = Config.getRestUriPrefix().tail.split("/")
  private[this] val restUriPattern1 = "/" + Seq(prefixs(0), prefixs(1)).mkString("/") + "/([\\w]+)"
  private[this] val restUriPattern2 = "/" + Seq(prefixs(0), prefixs(1)).mkString("/") + "/([\\w]+)/([\\w]+)"
  private[this] val authKey = Config.getAuthKey()

  private val responseJsonData: Future[String] => Route = (result: Future[String]) => onSuccess(result) { r => complete(HttpEntity(ContentTypes.`application/json`, r)) }

  private[this] implicit lazy val operationQueryMappings: Map[String, String] = GraphqlScanner.createOperationQueryMappings()

  private[this] implicit val system = ActorSystem()
  private[this] implicit val dispatcher = system.dispatcher
  private[this] implicit val materializer = ActorMaterializer()

  private[this] val routes: Route = {
    path(ProjectMatcher.getRestUriPathMather / ResourceMatcher / ResourceIdMatcher) { (projectId, typ, id) =>
      concat(
        get {
          headerValueByName(authKey) { authValue =>
            //查询一个
            val req = RestRequest(restOperation = RestOperation.GET, resource = typ,
              queryParams = projectId.fold(Map("id" -> id))(pid => Map("id" -> id, "projectId" -> pid, authKey -> authValue)))
            val ret = GraphqlExecution.executeRequest(req.toGraphqlRequest())
            responseJsonData(ret)
          }
        }
          ~ delete {
          headerValueByName(authKey) { authValue =>
            //删除一个
            val req = RestRequest(restOperation = RestOperation.DELETE, resource = typ,
              queryParams = projectId.fold(Map("id" -> id))(pid => Map("id" -> id, "projectId" -> pid, authKey -> authValue)))
            val ret = GraphqlExecution.executeRequest(req.toGraphqlRequest())
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
                  resource = typ, queryParams = projectId.fold(Map("id" -> id))(pid => Map("id" -> id, "projectId" -> pid, authKey -> authValue)))
                val ret = GraphqlExecution.executeRequest(req.toGraphqlRequest())
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
                queryParams = projectId.fold(Map.empty[String, String])(pid => Map("projectId" -> pid, authKey -> authValue)))
              val ret = GraphqlExecution.executeRequest(req.toGraphqlRequest())
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
                      resource = typ, queryParams = projectId.fold(Map.empty[String, String])(pid => Map("projectId" -> pid, authKey -> authValue)))
                    val ret = GraphqlExecution.executeRequest(req.toGraphqlRequest())
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
                      resource = typ, queryParams = projectId.fold(Map.empty[String, String])(pid => Map("projectId" -> pid, authKey -> authValue)), isBatch = true)
                    val ret = GraphqlExecution.executeRequest(req.toGraphqlRequest())
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
                      resource = typ, queryParams = projectId.fold(Map.empty[String, String])(pid => Map("projectId" -> pid, authKey -> authValue)), isBatch = true)
                    val ret = GraphqlExecution.executeRequest(req.toGraphqlRequest())
                    responseJsonData(ret)

                  }
                }
              }
            }
        )
      }

  }

  //健康检查
  private[this] val healthyRoute: Route = path("healthy-check") {
    get {
      complete(HttpEntity(ContentTypes.`application/json`, "imok"))
    }
  }

  private val bindingFuture: Future[Http.ServerBinding] = Http().bindAndHandle(routes ~ healthyRoute, host, port)

  println(
    """
      |  ________                    .__           .__    ___________                                    .___
      | /  _____/___________  ______ |  |__   _____|  |   \_   _____/____________  _  _______ _______  __| _/
      |/   \  __\_  __ \__  \ \____ \|  |  \ / ____/  |    |    __)/  _ \_  __ \ \/ \/ /\__  \\_  __ \/ __ |
      |\    \_\  \  | \// __ \|  |_> >   Y  < <_|  |  |__  |     \(  <_> )  | \/\     /  / __ \|  | \/ /_/ |
      | \______  /__|  (____  /   __/|___|  /\__   |____/  \___  / \____/|__|    \/\_/  (____  /__|  \____ |
      |        \/           \/|__|        \/    |__|           \/                            \/           \/
      |""".stripMargin)

  if (Config.enableServiceRegister()) {
    Try(serviceProvider.register(Schema.HTTP -> Seq(restUriPattern1, restUriPattern2))) match {
      case Success(_) =>
        println("Service register success")
      case Failure(exception) => println(s"Service register failure: ${exception.getLocalizedMessage}")
    }
  } else {
    println("disabled Service register")

  }

  StdIn.readLine() // let it run until user presses return

  bindingFuture.flatMap(_.unbind()).onComplete { _ =>
    if (Config.enableServiceRegister()) {
      serviceProvider.deregister()
    }
    system.terminate()
  }
}
