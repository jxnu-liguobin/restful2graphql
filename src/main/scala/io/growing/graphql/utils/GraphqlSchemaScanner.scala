package io.growing.graphql.utils

import java.io.{ BufferedWriter, File, FileWriter }
import java.nio.file.Paths
import java.util
import java.util.regex.Pattern

import com.google.common.base.Charsets
import com.google.common.reflect.ClassPath
import org.apache.commons.lang3.StringUtils

import scala.collection.JavaConverters._

/**
 *
 * @author liguobin@growingio.com
 * @version 1.0,2020/7/6
 */
class GraphqlSchemaScanner(classPath: ClassPath, root: String) {

  private[this] lazy val queryRegex = Pattern.compile("""type ?\w+Query ?\{""")
  private[this] lazy val mutationRegex = Pattern.compile("""type ?\w+Mutation ?\{""")

  /**
   * 读取所有src/main/resources/graphql下的schema
   *
   * 这是我们都有的分离模式，没有使用这种分离写法的话，可以将所有schema放进src/main/resources/all.schema中
   *
   * @return
   */
  def scanSchema(): String = {
    val prefix = Paths.get(root).toString + "/"
    val (schemas, resolvers) = getAllResources(prefix).partition(_.getResourceName.contains(".schema."))
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

  private[graphql] def getAllResources(prefix: String): Set[ClassPath.ResourceInfo] = {
    val resourceClass = classOf[ClassPath.ResourceInfo]
    classPath.getResources.asScala.filter { r ⇒
      lazy val name = r.getResourceName
      r.getClass == resourceClass && name.startsWith(prefix) && name.endsWith(".graphql")
    }.toSet
  }

}

class GraphqlQueryScanner(classPath: ClassPath, root: String) {

  private val operations: util.HashMap[String, String] = new util.HashMap[String, String]()

  /**
   * 解析所有gql语句，并与fetcher映射，入内存
   *
   * @return
   */
  def scanQuery(): Map[String, String] = {
    val prefix = Paths.get(root).toString + "/"
    val gql = getAllResources(prefix).filter(_.getResourceName.contains(".gql"))
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
              println(s"start: $countLeft, end $countRight")
              if(countLeft >= countRight) "" else q
            } else {
              q
            }
        }
      } else query
      operations.put(key, neqQuery.mkString("\n"))
      query
    }.toSeq

    operations.asScala.toMap
  }

  private[graphql] def getAllResources(prefix: String): Set[ClassPath.ResourceInfo] = {
    val resourceClass = classOf[ClassPath.ResourceInfo]
    classPath.getResources.asScala.filter { r ⇒
      lazy val name = r.getResourceName
      r.getClass == resourceClass && name.startsWith(prefix) && name.endsWith(".gql")
    }.toSet
  }

}

object GraphqlScanner {

  /**
   * 构造fetcher key 与query之间的映射
   *
   * @return
   */
  def createOperationQueryMappings(): Map[String, String] = {
    //将我们独立的分离写法合并到all.graphql中
    val tmp = new File("src/main/resources/all.graphql")
    if (tmp.exists()) {
      val query = new GraphqlQueryScanner(ClassPath.from(this.getClass.getClassLoader), "gql")
      query.scanQuery()
    } else {
      val schema = new GraphqlSchemaScanner(ClassPath.from(this.getClass.getClassLoader), "graphql")
      val res = schema.scanSchema()
      tmp.createNewFile
      val fw = new FileWriter(tmp.getAbsoluteFile)
      val bw = new BufferedWriter(fw)
      bw.write("# create auto, do not edit it\n")
      bw.write(res)
      bw.flush()
      val query = new GraphqlQueryScanner(ClassPath.from(this.getClass.getClassLoader), "gql")
      query.scanQuery()
    }
  }
}

object GraphqlScannerTest extends App {

  val schema = new GraphqlSchemaScanner(ClassPath.from(this.getClass.getClassLoader), "graphql")
  val res = schema.scanSchema()
  val file = new File("src/main/resources/all.graphql")
  if (!file.exists) file.createNewFile
  val fw = new FileWriter(file.getAbsoluteFile)
  val bw = new BufferedWriter(fw)
  bw.write("# create auto, do not edit it\n")
  bw.write(res)
  bw.flush()
  println(res)


  val query = new GraphqlQueryScanner(ClassPath.from(this.getClass.getClassLoader), "gql")
  val ret = query.scanQuery()
  println(ret)
}