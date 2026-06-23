# 网关控制面板

为了方便用户管理而提供的网关控制面板，用户可以通过网关控制面板查看网关的状态、配置网关的参数、查看网关的日志等。

# 原理

1. 网关控制面板通过调用网关的 `Admin API` 来获取网关的状态、配置网关的参数、查看网关的日志等。
2. 操作网关相关资源，是通过调用网关的 `Admin API` 来操作的，比如：添加路由、删除路由、更新路由等。
3. 操作资源的本质是将数据统一存储到注册中心服务中，网关通过订阅注册中心服务中的数据来动态更新路由。
4. 注册中心服务是一个独立的服务，具体可以使用 `etcd`、`consul`、`zookeeper`、`fs`(仅用于开发环境) 等。
5. 所有网关规则都要先绑定路由，路由是网关规则的载体，每次插件进行过滤时，都会先判断路由是否匹配，匹配成功后才会继续进行插件过滤。

# 接口规范

1. 每一个 `URI` 代表一种资源
2. 客户端使用`GET`（查询）、`POST`（增加）、`PUT`（更新）、`DELETE`（删除）四个表示操作方式的动词对服务端资源进行操作
3. 客户端通过操作资源的 `URI` 来操作资源

# 参考链接
[java two factor](https://github.com/samdjstevens/java-totp)

# 接口列表

1. [获取网关状态](##获取网关状态)

## 获取网关状态
- URI: /api/v1/gateway/status
- Method: GET
- Request:
  - 无
- Response:
  - status: string

## 获取路由列表
- URI: /api/v1/gateway/routes
- Method: GET
- Request:
  - 无
- Response:
  - routes: array

## 获取路由信息
- URI: /api/v1/gateway/route/:id
- Method: GET
- Request:
  - id: string
- Response:
  - route: object


## 添加路由
- URI: /api/v1/gateway/route
- Method: PUT
- Request:
  - object
- Response:
  - object

示例：
```
curl -X PUT -H "Content-Type: application/json" http://localhost:8895/api/v1/gateway/route -d '{
  "host": "*",
  "path": "/hello",
  "method": "GET",
  "upstream_id": 1
}'
```

## 更新路由
- URI: /api/v1/gateway/route/:id
- Method: POST
- Request:
  - id: string
  - object
- Response:
  - object

示例：

```
curl -X POST -H "Content-Type: application/json" http://localhost:8895/api/v1/gateway/route/1 -d '{
  "host": "*",
  "path": "/hello",
  "method": "GET",
  "upstream_id": 1
}'
```

## 删除路由
- URI: /api/v1/gateway/route/:id
- Method: DELETE
- Request:
  - id: string
- Response:
  - object

示例：

```
curl -X DELETE http://localhost:8895/api/v1/gateway/route/1
```

## 通过传递的参数判断走哪个路由
- URI: /api/v1/gateway/route/decide
- Method: GET
- Request:
  - host: string
  - path: string
  - method: string
- Response:
  - route: string
