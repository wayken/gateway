# 🚀 TeamBeit Gateway

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Java](https://img.shields.io/badge/Java-8+-green.svg)](https://www.oracle.com/java/)
[![Netty](https://img.shields.io/badge/Netty-4.x-red.svg)](https://netty.io/)

> 基于 Teambeit-Cloud 框架构建的高性能云原生 API 网关，设计思路参考 Cloudflare Gateway，专为现代微服务架构而设计。

## ✨ 核心特性

### 🎯 核心功能
- **开箱即用** - 零配置快速启动，提供完整的管理界面
- **高性能架构** - 基于 Netty 异步框架，异步网络请求处理，慢响应不拖垮网关
- **动态路由** - 支持运行时动态添加、删除、修改路由配置
- **反向代理** - 完整的反向代理功能，支持负载均衡
- **无状态设计** - 支持水平扩展，云原生友好

### 🔒 安全防护
- **Web 应用防火墙 (WAF)** - 内置 SQL 注入、XSS、命令注入等攻击防护
- **鉴权认证** - 支持 JWT、后端接口等多种鉴权策略
- **IP 访问控制** - 基于地理位置的 IP 访问限制
- **流量限速** - 多维度流量控制和限速策略

### 🔧 高级功能
- **插件系统** - 热插拔插件架构，支持自定义插件开发
- **缓存机制** - 智能缓存策略，提升响应性能  
- **请求重写** - 支持 URL 重写和请求参数转换
- **灰度发布** - 支持 A/B 测试和金丝雀发布
- **监控告警** - 全面的监控指标和日志审计
- **证书管理** - HTTPS 证书自动管理和续订

### 🌐 AI 集成
- **AI 服务商集成** - 统一的 AI 服务接入层
- **智能路由** - 基于 AI 的智能流量分发
- **API 协议转换** - 支持不同 AI 服务商 API 的协议适配

## 🏗️ 架构设计

### 核心架构
```
网关 Gateway
  ├── 域 Zone (域名隔离)
  │   ├── 插件 Plugin (功能扩展)
  │   ├── 路由 Route (请求匹配)
  │   └── 上游 Upstream (后端服务)
  └── 管理面板 Dashboard
```

### 技术栈
- **核心**: Java 8+, Netty 4.x
- **配置中心**: Zookeeper, 文件系统 (NFS/EFS)
- **缓存**: Redis
- **服务发现**: Zookeeper
- **协议**: HTTP/HTTPS
- **监控**: 自定义监控 API, Grafana 兼容

## 🚀 快速开始

### 环境要求
- Java 8 或更高版本
- Maven 3.6+
- Zookeeper
- Redis (可选，用于缓存和限流)
- [Teambeit Cloud Microservices Framework 1.0+](https://github.com/wayken/cloud)

### 安装部署

#### 1. 安装依赖框架
```bash
git clone https://github.com/wayken/cloud
cd cloud
mvn clean install -DskipTests
```

#### 2. 下载源码，编译项目
```bash
git clone https://github.com/wayken/gateway
cd gateway
mvn clean package -DskipTests
```

#### 3. 启动网关服务
```bash
# 启动核心网关
java -jar kernel/target/teambeit-gateway-kernel.jar -c config/gateway.yaml

# 启动管理面板
java -jar dashboard/target/teambeit-gateway-dashboard.jar -c config/gateway-dashboard.yaml
```

#### 4. 访问管理界面
打开浏览器访问：`http://localhost:8895`

## 🔌 插件开发

### 自定义插件

#### 1. 创建插件类
```java
@Plugin(name = "custom-plugin", priority = 2000)
public class CustomPlugin extends PluginAdapter {
    public CustomPlugin() {
        super("custom-plugin");
    }
    
    @Override
    public React<PluginResult> preFilter(GatewayHttpRequest request, 
                                       GatewayHttpResponse response, 
                                       Zone zone, Route route) {
        // 实现自定义逻辑
        return React.just(PluginResult.SUCCESS);
    }
}
```


#### 2. docker 部署
```bash
cd docker
# 编译镜像并运行
sh deploy.sh build
sh deploy.sh run
# 停止容器
sh deploy.sh stop
```

## 📊 监控运维

### Grafana 集成
网关提供 Prometheus 兼容的监控指标，可直接接入 Grafana 进行监控可视化：

- 请求量统计
- 响应时间分布
- 错误率监控
- 流量 Top 监控
- 用户 Agent 分析

## 🤝 贡献指南

我们欢迎社区贡献！请参考以下步骤：

1. **Fork** 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交变更 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 创建 **Pull Request**

### 开发规范
- 遵循 Java 编码规范
- 添加单元测试
- 更新相关文档
- 保持向后兼容性

## 📝 许可证

本项目采用 [Apache License 2.0](LICENSE) 许可证 - 详情请查看 LICENSE 文件。

## 🙏 致谢

感谢以下项目的启发和参考：
- [Cloudflare Gateway](https://www.cloudflare.com/) - 设计思路参考
- [Nginx](https://nginx.org/) - 配置理念参考
- [Kong](https://konghq.com/) - 插件架构参考

## 📞 联系我们

- **问题反馈**: [GitHub Issues](https://github.com/your-org/teambeit-gateway/issues)
- **功能建议**: [GitHub Discussions](https://github.com/your-org/teambeit-gateway/discussions)
- **安全问题**: security@your-org.com

---

⭐ 如果这个项目对你有帮助，请给我们一个 Star！
