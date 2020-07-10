graphql-expand
--

## 概述

**目的：像使用 restful api 一样使用 graphql api**

基于纯 graphql 的后端服务，提供的 Open API 也是一个 graphql 接口。
对于不熟悉的人来说，可读性不高，使用不便，与现有广泛存在的 restful API 差异明显。

目前发现，将 restful API 转发到 graphql API，是一种比较折中的方案。

针对通用 restful 接口，其中 requestBody 是可选，每个资源有以下七个独立接口，分四种HTTP方法类型：

实际 URI 只有两种格式

1. GET      /forward/projects/:project_id/:resource/:resource_id
2. GET      /forward/projects/:project_id/:resources
3. PUT      /forward/projects/:project_id/:resource/:resource_id             (requestBody)
4. PUT      /forward/projects/:project_id/:resources             (requestBody 有id)
5. POST     /forward/projects/:project_id/:resource             (requestBody)
6. DELETE   /forward/projects/:project_id/:resource/:resource_id 
7. DELETE   /forward/projects/:project_id/:resources          (requestBody)

- resource => graphql field operationName
- requestBody => graphql field variables
- resource_id => graphql fetcher param 

查询单个、更新单个、删除单个必须有 resource_id 参数，其他批量操作使用 requestBody。

两者比较：

将 restful 根据 URI 转化成 graphql

**优点**
1. 性能损失小，只增加一些 if else 来判断，但可能多一次 requestBody 解析
2. 对原 graphql 代码无侵入性
3. 对于公开接口而言，可读性高（针对不熟悉 graphql 的人）
4. 使得对资源的缓存容易在外部实现

**缺点**
1. 丢失类型安全和错误校验功能，错误最终只能在 graphql 处理时才能判断，不能在 restful 转化到 graphql 时准确判断
2. restful 处理转化到 graphql 时，丢失 graphql 的可选择性获取字段，组装等功能
3. RequestBody 无法提前进行校验
4. 需要一个新的转发服务
5. 由于 graphql 必须指定返回字段，所以使用 restful 转换时，只能返回所有字段，也就是不具备选择功能
6. 订阅功能缺失

**难点**
* query 拼接，可以使用自动生成工具，减小难度
* restful 映射到 graphql query
* 如何忽略生成的 gql 中的多余字段
* restful api 需要注册到服务注册中心供其他服务能调用（不关心被调用的 graphql api）

**注册实现**

* 采用 dryad，仅注册两个 URI `/forawrd/projects/([\w]+), /forawrd/projects/([\w]+)/([\w]+)`

## 使用技术

* akka-http
* spray-json
* okhttp
* dryad


目前只支持，标准 result api 的 crud 转发到 graphql 的 mutation 和 query 

1. 查询一个
2. 查询所有
3. 更新
4. 创建
5. 删除
6. 批量删除
7. 批量更新

批量操作，未测

目前 graphql 没有版本划分，暂不考虑

## 示例

首先利用前端代码生成 graphql query 语句，每个 gql 对应服务端的一个 data fetcher 

- 使用 https://github.com/timqian/gql-generator 前端工具，自动生成 gql。
- 执行 gqlg --schemaFilePath src/main/resources/all.graphql --destDirPath src/main/resources/gql --depthLimit 5

在 all.graphql 中是所有需要转发的 graphql schmea。最终会在 gql/ 目录下生成所有 *.gql 语句。

> 这里自动生成的语句实际会有很多的多余字段，需要排除掉。

1. 所有 graphql schema 放在 all.graphql 中
2. 启动 Rest2GraphqlForwardServer.scala
3. 使用 restful 请求 graphql

**使用 restful 完成 crud**

- GET http://localhost:8080/forward/projects/WlGk4Daj/userVariables 
    - 将会使用 HTTP 调用 graphql api: `userVariables: [UserVariable]`
- POST http://localhost:8080/forward/projects/WlGk4Daj/userVariable 
    - 将会使用 HTTP 调用 graphql api: `createUserVariable(userVariable: VariableInput!): UserVariable!`
    - requestBody 
```json
{
    "userVariable": {
        "name": "测试graphql",
        "key": "test_132",
        "valueType": "int",
        "description": "132"
    }
}
```
如果使用 graphql ，那么这个请求体长这样：
```json
{
    "operationName": "createUserVariable",
    "variables": {
        "userVariable": {
            "name": "测试graphql",
            "key": "test_132",
            "valueType": "int",
            "description": "132"
        }
    },
    "query": "mutation createUserVariable($userVariable: VariableInput!) {\n  createUserVariable(userVariable: $userVariable) {\n    name\n    __typename\n  }\n}\n"
}
```
对用户来说，query 的拼写是痛苦的。

- DELETE http://localhost:8080/forward/projects/WlGk4Daj/userVariable/y9pmLdQm # y9pmLdQm是一个HashId
    - 将会使用 HTTP 调用 graphql api: `deleteUserVariable(id: HashId!): Boolean!`
- PUT http://localhost:8080/forward/projects/WlGk4Daj/userVariable/y9pmLdQm
    - 将会使用 HTTP 调用 graphql api: `updateUserVariable(id: HashId!, userVariable: VariableInput!): UserVariable!`
    - requestBody 当 id 字段在路径参数和 requestBody 都存在时，只会使用路径参数的
```json
{
    "userVariable": {
        "name": "测试graphql",
        "key": "test_132",
        "valueType": "int",
        "description": "132"
    }
}
```

## 可选配置

```
graphql {

  #1.直接将schema放在里面，或者其他schema合并后放到这里面，不要与 *.gql 在同目录，会被覆盖
  schema.path = "src/main/resources/all.graphql"
  exclude {

   # 因为定义的是必须存在，查出的数可能为空，不兼容，需要排除，对于嵌套逻辑只需要排除最外层
   # 之所以配置是这样的，是因为生成的 graphql query 就是空格隔开，这样不需要的字段，直接粘贴复制到这里即可
   userVariables = "projectId type valueType"
   createUserVariable = "id projectId key type description isSystem creatorId createdAt updaterId updatedAt creator updater valueType "
   updateSegment = "projectId description compute scheduler creatorId createdAt updaterId updatedAt creator updater createdBy detector"
   createSegment = "projectId description compute scheduler creatorId createdAt updaterId updatedAt creator updater createdBy detector"
  }

  # graphql服务的地址
  # url = "http://localhost:8086/projects/%s/graphql"

  # 此地址的 graphql 请求是经过 gateway 的
  url = "http://gdp-dev.growingio.com/graphql"

  # graphql鉴权请求头的key，应该在 restful 请求时携带服务器的InternalToken，这里默认使用 cookie （可以使用 token 或者 cookie）
  auth {
    # 必须传
    key = "Cookie"
  }
}

# dryad框架的参数
dryad {

  enabled = false

  namespace = "gio-graphql-forawrd"
  group = "k8s-datatest"

  provider = "io.growing.dryad.consul.provider.ConsulConfigProvider"
  registry = "io.growing.dryad.consul.registry.ConsulServiceRegistry"

  service {
    http {
      # 转发接口的预定义前缀，目前只支持一个预定义路径参数
      prefix = "/forward/projects/:project_id"
      port = 8080
      pattern = "/.*"
      check {
        url = "/healthy-check"
        interval = 5s
      }
    }
  }

  consul {
    host = "ci-consul.infra.growingio.com"
    port = 80
    username = "x"
    password = "x"
  }

}
```