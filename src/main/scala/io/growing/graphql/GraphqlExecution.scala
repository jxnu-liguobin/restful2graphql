package io.growing.graphql

import java.io.IOException
import java.util.{ Objects, UUID }

import com.google.gson.GsonBuilder
import com.typesafe.scalalogging.LazyLogging
import graphql.{ ExecutionInput, ExecutionResult, GraphQL, GraphQLContext }
import graphql.execution.ExecutionId
import io.growing.graphql.request.GraphqlRequest
import okhttp3._
import spray.json.{ JsNumber, JsObject, JsString }

import scala.concurrent.{ ExecutionContext, Future, Promise }

/**
 * graphql执行器，包括远程和本地
 *
 * @author liguobin@growingio.com
 * @version 1.0,2020/7/7
 */
trait GraphqlExecution extends LazyLogging {

  //因为有main方法，全局变量加载拿不到值，必须加lazy
  private[this] lazy val gson = new GsonBuilder().serializeNulls().create()

  def executeRequest(request: GraphqlRequest): Future[String] = {
    val body = request.toString
    logger.info(s"graphql request: \n$body")
    val url = request.getExecuteUrl()
    val rb = new Request.Builder().url(url).addHeader(request.getAuthToken()._1, request.getAuthToken()._2)
      .post(RequestBody.create(body, Constants.json))
    val promise = Promise[String]

    OkHttp.client.newCall(rb.build()).enqueue(new Callback {

      override def onFailure(call: Call, e: IOException): Unit = {
        promise.failure(e)
      }

      override def onResponse(call: Call, response: Response): Unit = {
        if (response.isSuccessful) {
          val bytes = response.body().bytes()
          promise.success(new String(bytes, Constants.charset))
        } else {
          logger.error(response.toString)
          //将错误信息返回
          val error = JsObject.apply(Map("code" -> JsNumber(response.code()), "message" -> JsString(response.message())))
          promise.success(error.prettyPrint)
        }
      }
    })
    promise.future
  }

  //未测试
  @unchecked
  private def buildExecution(request: GraphqlRequest): ExecutionInput = {
    val context = GraphQLContext.newContext()
    //将request中的参数都放在execution中传递
    for ((k, v) <- request.getContextParams) {
      context.of(k, v)
    }
    val execution = ExecutionInput.newExecutionInput(request.toString).context(context)
    val executionId = UUID.randomUUID().getLeastSignificantBits.toHexString
    execution.executionId(ExecutionId.from(executionId))
    val varargs = gson.fromJson(request.getVariables(), classOf[java.util.Map[String, Object]])
    if (Objects.nonNull(varargs)) {
      execution.variables(varargs)
    }
    execution.build()
  }


  /**
   * 以jar形式引入依赖，执行本地的graphql，而不是使用转发
   *
   * @param graphQL
   * @param request
   * @param ec
   * @return
   */
  //未测试
  @unchecked
  def executeLocal(graphQL: GraphQL, request: GraphqlRequest)(implicit ec: ExecutionContext): Future[String] = {
    val execution = buildExecution(request)
    val future = graphQL.executeAsync(execution)
    val promise = Promise[ExecutionResult]
    future.whenComplete((t: ExecutionResult, u: Throwable) ⇒ {
      if (Objects.nonNull(u)) {
        promise.failure(u)
      } else {
        promise.success(t)
      }
    })
    promise.future.map(r => gson.toJson(r))
  }

}
