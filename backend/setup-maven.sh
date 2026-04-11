#!/bin/bash
# Maven Wrapper 下载脚本

cd ~/.qclaw/workspace/contract-management-system/backend

# 创建必要的目录
mkdir -p .mvn/wrapper

# 下载 maven-wrapper.properties
cat > .mvn/wrapper/maven-wrapper.properties << 'EOF'
distributionUrl=https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/3.9.6/apache-maven-3.9.6-bin.zip
wrapperUrl=https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.2.0/maven-wrapper-3.2.0.jar
EOF

# 下载 maven-wrapper.jar
curl -sL "https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.2.0/maven-wrapper-3.2.0.jar" -o .mvn/wrapper/maven-wrapper.jar

# 创建 mvnw 脚本
cat > mvnw << 'SCRIPT'
#!/bin/sh
# Maven Wrapper
exec java -jar "$(dirname "$0")/.mvn/wrapper/maven-wrapper.jar" "$@"
SCRIPT

chmod +x mvnw

echo "Maven Wrapper 创建完成！"
echo "运行 ./mvnw spring-boot:run 启动后端"
