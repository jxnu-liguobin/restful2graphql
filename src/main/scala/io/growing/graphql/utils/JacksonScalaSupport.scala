package io.growing.graphql.utils


import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.databind.{ DeserializationFeature, ObjectMapper, SerializationFeature }
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper


/**
 *
 * @author liguobin@growingio.com
 * @version 1.0,2020/7/6
 */
object JacksonScalaSupport {

  lazy val mapper: ObjectMapper = {
    val mapper = new ObjectMapper() with ScalaObjectMapper
    mapper.setSerializationInclusion(Include.NON_NULL)
    mapper.setSerializationInclusion(Include.NON_ABSENT)
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    mapper.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false)
    mapper.registerModule(DefaultScalaModule)
    mapper
  }
}