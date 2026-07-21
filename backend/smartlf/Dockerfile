# Build stage
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /build
COPY pom.xml .
# 配置 Maven 阿里云镜像加速下载依赖
RUN mkdir -p /root/.m2 \
    && echo '<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"><mirrors><mirror><id>aliyun</id><name>aliyun maven</name><url>http://maven.aliyun.com/repository/public</url><mirrorOf>central</mirrorOf></mirror></mirrors></settings>' > /root/.m2/settings.xml
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests

# Run stage
FROM eclipse-temurin:21-jre
RUN apt-get update && apt-get install -y --no-install-recommends curl && rm -rf /var/lib/apt/lists/*

WORKDIR /app
RUN mkdir -p uploads
COPY --from=build /build/target/*.jar app.jar

ENV SERVER_PORT=18080
EXPOSE 18080

# 使用 swagger 文档页作为健康检查，确保应用真正启动成功
HEALTHCHECK --interval=10s --timeout=5s --start-period=60s --retries=5 \
    CMD curl -f http://localhost:18080/doc.html || exit 1

ENTRYPOINT ["java", \
    "-Xms512m", "-Xmx1g", \
    "-XX:+UseG1GC", \
    "-XX:+ExitOnOutOfMemoryError", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-Duser.timezone=Asia/Shanghai", \
    "-jar", "app.jar"]
