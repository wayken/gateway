# 基于官方 Maven 镜像，包含 JDK
FROM maven:3.8.7-eclipse-temurin-17 AS build

# 设置工作目录
WORKDIR /app

# 拉取 teambeit-cloud 源码并安装到本地 Maven 仓库
RUN git clone https://github.com/wayken/cloud.git /tmp/teambeit-cloud \
    && cd /tmp/teambeit-cloud \
    && mvn clean install -DskipTests

# 拷贝父目录pom.xml到 /app
COPY pom.xml /app/pom.xml
# 拷贝 kernel 项目源码到 /app/kernel
COPY kernel /app/kernel

# 编译 kernel 项目（指定工作目录）
WORKDIR /app/kernel
RUN mvn clean package -DskipTests

# 运行阶段，使用更轻量的 JDK 镜像
FROM openjdk:17-jdk-slim
WORKDIR /app

# 拷贝编译好的 jar 包和lib目录到运行镜像
COPY --from=build /app/kernel/target/*.jar teambeit-gateway-kernel.jar
COPY --from=build /app/kernel/target/lib /app/lib

# 拷贝配置文件到镜像
COPY docker/kernel/gateway.yaml ./

# 创建配置和数据相关目录
RUN mkdir -p /app/data /app/logs

# 设置JVM参数
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC"

# 启动服务，指定 -c gateway.yaml
CMD java $JAVA_OPTS -jar /app/teambeit-gateway-kernel.jar -c /app/gateway.yaml
