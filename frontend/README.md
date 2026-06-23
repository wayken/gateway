 产品

关管理平台

# 开发时间

- 2023.09.18: 初始化项目 `npm init vite@latest` √

# 开发说明

- 要求 NodeJS 版本：v18.16.0+

# 开发计划

1. 点击路由列表可以展现上游和扩展数据 √
2. 支持规则拖动排序
3. 支持黑白名单规则配置 √
4. 支持路由、上游从指定ID复制
5. 规则为IP时操作为等于、不等于、包含、不包含，在列表中，不在列表中 √
6. 支持数据保存成草稿，记录在数据库中， 不写入配置中心
7. 支持请求追踪，通过输入URL来展现匹配的路径+WAF规则+限速规则+缓存规则
8. 支持请求消息队列和消费接口
9. 新增IP地理插件 √
10. 支持不同的异常规则配置告警
11. waf/cache高级匹配支持请求后端服务来判断是否匹配规则
12. 路由匹配支持选择按国家匹配 √
13. 实现规则展现
14. 实现自定义模板
15. 支持站点分组
16. 支持网关集群信息查看、证书、静态目录等
17. 新增系统日志，采用拦截器注解

### 参考链接

- [图标列表](https://www.svgrepo.com/collection/solar-broken-line-icons/1)
- [logo图标](https://www.svgrepo.com/svg/375342/cloud-api-gateway)
- [模拟HTTP请求工具](https://www.bejson.com/network/profession_request_tools/)
- [nginx ui](https://nginxui.com/)
- [SamWaf](https://gitee.com/samwaf/SamWaf)
- [ip2region地理位置](https://github.com/alading89/ip2region-xdb)
- [WAF 防护效果测试工具](https://github.com/chaitin/blazehttp)
- [API Park网关](https://demo.apipark.com/)
- [Higress网关](https://demo.higress.io/)
