package io.growing.graphql.parser

import java.io.{ BufferedWriter, File, FileWriter }

import com.google.common.reflect.ClassPath
import io.growing.graphql.Config

/**
 * 解析生成的所有gql文件
 *
 * @author liguobin@growingio.com
 * @version 1.0,2020/7/6
 */
trait GraphqlParser {

  /**
   * 构造fetcher key 与query之间的映射
   *
   * @return
   */
  def createOperationQueryMappings(): Map[String, String] = {
    //将我们独立的分离写法合并到all.graphql中
    val tmp = new File(Config.getSchemaPath())
    if (tmp.exists()) {
      val query = new GraphqlQueryScanner(ClassPath.from(this.getClass.getClassLoader), Config.getGQLPath())
      query.scanQuery()
    } else {
      //先合并，再解析
      val schema = new GraphqlSchemaScanner(ClassPath.from(this.getClass.getClassLoader), "graphql")
      val res = schema.scanSchema()
      tmp.createNewFile
      val fw = new FileWriter(tmp.getAbsoluteFile)
      val bw = new BufferedWriter(fw)
      bw.write("# create auto, do not edit it\n")
      bw.write(res)
      bw.flush()
      val query = new GraphqlQueryScanner(ClassPath.from(this.getClass.getClassLoader), Config.getGQLPath())
      query.scanQuery()
    }
  }
}

//合并schema，gio特有
//其他使用，schema放入all.graphql中即可
object GraphqlParser extends App {

  val schema = new GraphqlSchemaScanner(ClassPath.from(this.getClass.getClassLoader), "graphql")
  val res = schema.scanSchema()
  val file = new File(Config.getSchemaPath())
  if (!file.exists) file.createNewFile
  val fw = new FileWriter(file.getAbsoluteFile)
  val bw = new BufferedWriter(fw)
  bw.write("# create auto, do not edit it\n")
  bw.write(res)
  bw.flush()
  println(res)


  val query = new GraphqlQueryScanner(ClassPath.from(this.getClass.getClassLoader), Config.getGQLPath())
  val ret = query.scanQuery()
  println(ret)
}