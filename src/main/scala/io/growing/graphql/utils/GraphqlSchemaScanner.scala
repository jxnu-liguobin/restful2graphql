package io.growing.graphql.utils

import java.io.{ BufferedWriter, File, FileWriter }
import java.nio.file.Paths
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

  def scan(): String = {
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

object GraphqlSchemaScannerTest extends App {

  val scanner = new GraphqlSchemaScanner(ClassPath.from(this.getClass.getClassLoader), "graphql")
  val ret = scanner.scan()
  val file = new File("src/main/scala/io/growing/graphql/schema/all.graphql")
  if (!file.exists) file.createNewFile
  val fw = new FileWriter(file.getAbsoluteFile)
  val bw = new BufferedWriter(fw)
  bw.write("# create auto, do not edit it\n")
  bw.write(ret)
  bw.flush()
  println(ret)

}