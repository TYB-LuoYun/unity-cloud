#!/bin/sh

# 设置默认值（如果变量未定义）
SERVER="${SERVER:-app.jar}"
ENV="${ENV:-dev}"
JVM_XMS="${JVM_XMS:-1g}"
JVM_XMX="${JVM_XMX:-1g}"
JVM_XMN="${JVM_XMN:-512m}"
JVM_MS="${JVM_MS:-128m}"
JVM_MMS="${JVM_MMS:-320m}"

echo "启动参数:"
echo "ENV=$ENV  Xms=$JVM_XMS  Xmx=$JVM_XMX  Xmn=$JVM_XMN  Metaspace=$JVM_MS/$JVM_MMS"

# 拼接 JAVA 参数
JAVA_OPT="-Xms${JVM_XMS} -Xmx${JVM_XMX} -Xmn${JVM_XMN}"
JAVA_OPT="${JAVA_OPT} -XX:MetaspaceSize=${JVM_MS} -XX:MaxMetaspaceSize=${JVM_MMS}"
JAVA_OPT="${JAVA_OPT} -Dspring.profiles.active=${ENV}"
JAVA_OPT="${JAVA_OPT} -XX:-OmitStackTraceInFastThrow"
JAVA_OPT="${JAVA_OPT} -XX:+HeapDumpOnOutOfMemoryError"
JAVA_OPT="${JAVA_OPT} -XX:HeapDumpPath=/logs/java_heapdump.hprof"

# NACOS 配置（按需添加）
[ -n "$NACOS_ADDR" ] && JAVA_OPT="${JAVA_OPT} -Dspring.cloud.nacos.server-addr=${NACOS_ADDR}"
[ -n "$NACOS_USERNAME" ] && JAVA_OPT="${JAVA_OPT} -Dspring.cloud.nacos.username=${NACOS_USERNAME}"
[ -n "$NACOS_PASSWORD" ] && JAVA_OPT="${JAVA_OPT} -Dspring.cloud.nacos.password=${NACOS_PASSWORD}"

echo "启动命令: java ${JAVA_OPT} -jar ${SERVER}"
exec java ${JAVA_OPT} -jar ${SERVER}