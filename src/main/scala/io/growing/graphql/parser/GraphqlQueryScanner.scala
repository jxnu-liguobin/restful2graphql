package io.growing.graphql.parser

import java.nio.file.Paths
import java.util

import com.google.common.base.Charsets
import com.google.common.reflect.ClassPath
import io.growing.graphql.Config
import org.apache.commons.lang3.StringUtils

import scala.collection.JavaConverters._

/**
 * 读取生成的所有gql文件并排除字段
 *
 * @author liguobin@growingio.com
 * @version 1.0,2020/7/6
 */
class GraphqlQueryScanner(override val classPath: ClassPath, root: String) extends ScannerSupport {

  private val operations: util.HashMap[String, String] = new util.HashMap[String, String]()

  /**
   * 解析所有gql语句，并与fetcher映射，入内存
   *
   * @return
   */
  def scanQuery(): Map[String, String] = {
    val prefix = Paths.get(root).toString + "/"
    val gql = getAllResources(prefix, ".gql").filter(_.getResourceName.contains(".gql"))
    gql.map { r ⇒
      val query = r.asCharSource(Charsets.UTF_8).readLines().asScala
      val key = query.head.split(" ")(1).split("\\(")(0).replace("{", "")
      val excludeFieldQuery = Config.getExcludeFields(key)
      val neqQuery = if (excludeFieldQuery.nonEmpty) {
        var countLeft = 0
        var countRight = 0
        var flag = false
        query.map {
          q =>
            if (excludeFieldQuery.contains(q.trim) && !q.contains("{")) {
              ""
            } else if (!flag && q.contains("{") && excludeFieldQuery.contains(q.trim.replace("{", ""))) {
              countLeft += 1
              flag = true
              ""
            } else if (flag) {
              if (q.contains("{")) {
                countLeft += 1
              }
              if (q.contains("}")) {
                countRight += 1
              }
              if (countLeft >= countRight) "" else q
            } else {
              q
            }
        }
      } else query
      operations.put(key, neqQuery.filter(f => StringUtils.isNoneBlank(f)).mkString("\n"))
      query
    }.toSeq

    operations.asScala.toMap
  }

}