# 基于官方 Maven 镜像，包含 JDK
FROM maven:3.8.7-eclipse-temurin-17 AS build

WORKDIR /app

# 拉取 teambeit-cloud 源码并安装到本地 Maven 仓库
RUN git clone https://github.com/wayken/cloud.git /tmp/teambeit-cloud \
    && cd /tmp/teambeit-cloud \
    && mvn clean install -DskipTests

# 拷贝父目录pom.xml到 /app
COPY pom.xml /app/pom.xml
COPY kernel /app/kernel
COPY dashboard /app/dashboard

# 先 install 根目录 POM（teambeit-gateway），再 install kernel
WORKDIR /app
RUN mvn clean install -DskipTests

WORKDIR /app/kernel
RUN mvn clean install -DskipTests

WORKDIR /app/dashboard
RUN mvn clean package -DskipTests

# 运行阶段，使用更轻量的 JDK 镜像
FROM openjdk:17-jdk-slim
WORKDIR /app

# 拷贝编译好的 jar 包和lib目录到运行镜像
COPY --from=build /app/dashboard/target/*.jar teambeit-gateway-dashboard.jar
COPY --from=build /app/dashboard/target/lib /app/lib

# 拷贝配置文件到镜像
COPY docker/dashboard/gateway-dashboard.yaml ./

RUN mkdir -p /app/data /app/logs

ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC"

CMD java $JAVA_OPTS -jar /app/teambeit-gateway-dashboard.jar -c /app/gateway-dashboard.yaml
