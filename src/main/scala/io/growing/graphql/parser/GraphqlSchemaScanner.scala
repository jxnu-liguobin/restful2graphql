package io.growing.graphql.parser


import java.nio.file.Paths
import java.util.regex.Pattern

import com.google.common.base.Charsets
import com.google.common.reflect.ClassPath
import org.apache.commons.lang3.StringUtils

import scala.collection.JavaConverters._

/**
 * 用于将多个不同业务的schema合并到一个graphql中，以便后续生成*.gql
 *
 * 若本身schema已经在一个文件中，则不需要此实现
 *
 * @author liguobin@growingio.com
 * @version 1.0,2020/7/6
 */
class GraphqlSchemaScanner(override val classPath: ClassPath, root: String) extends ScannerSupport {

  private[this] lazy val queryRegex = Pattern.compile("""type ?\w+Query ?\{""")
  private[this] lazy val mutationRegex = Pattern.compile("""type ?\w+Mutation ?\{""")

  /**
   * 读取所有src/main/resources/graphql（gio默认的位置）下的schema
   *
   * 这是我们都有的分离模式，没有使用这种分离写法的话，可以将所有schema放进src/main/resources/all.schema（默认）中
   *
   * @return
   */
  def scanSchema(): String = {
    val prefix = Paths.get(root).toString + "/"
    val (schemas, resolvers) = getAllResources(prefix, ".graphql").partition(_.getResourceName.contains(".schema."))
    val (queries, mutations, results) = (new StringBuilder, new StringBuilder, new StringBuilder)
    resolvers.foreach { r ⇒
      var flag: Option[Boolean] = None
      val lines = r.asCharSource(Charsets.UTF_8).readLines().asScala
      lines.foreach {
        case line: String if queryRegex.matcher(line).matches() ⇒ flag = Some(true)
        case line: String if mutationRegex.matcher(line).matches() ⇒ flag = Some(false)
        case line: String if line.contains("}") ⇒ flag = None
        case line: String ⇒
          flag match {
            case Some(true) ⇒ queries.append(line).append(StringUtils.LF)
            case Some(false) ⇒ mutations.append(line).append(StringUtils.LF)
            case _ ⇒
          }
      }
    }
    results.append("type Query {\n").append(queries).append("}\n\n").append("type Mutation {\n").append(mutations).append("}\n\n")
    schemas.foreach(r ⇒ results.append(r.asCharSource(Charsets.UTF_8).read()).append(StringUtils.LF))
    results.toString()
  }
}