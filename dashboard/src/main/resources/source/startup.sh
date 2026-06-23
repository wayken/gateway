#!/bin/bash

: '
启动网关控制台服务
'
readonly program="`basename $0`"
readonly run=/bin/sh
# jdk安装路径&服务实例路径
readonly jdk_home=/usr/local/jdk
readonly service_name=teambeit-gateway-dashboard
readonly service_home=/root/gateway/dashboard
readonly service_stdout_log=/root/gateway/dashboard/stdout.log
readonly service_gc_log=/root/gateway/dashboard/gc.log
readonly service_application_config=teambeit-gateway-dashboard.yaml
# 默认jvm参数
readonly default_jvmoption=(
  -server
  -XX:+PrintGCDateStamps
  -XX:+PrintGCDetails
  -Djava.nio.channels.spi.SelectorProvider=sun.nio.ch.EPollSelectorProvider
  -Xms64m
  -Xmx64m
  -XX:ReservedCodeCacheSize=32m
  -Xss512k
  -XX:+UseG1GC
  -XX:ParallelGCThreads=2
  -XX:ConcGCThreads=2
)
# 启动实例超时时间
readonly start_timeout=5

function print_info() {
  local message=$1
  echo -e "[\033[0;36mInfo\033[0m]: " ${message}
}

function print_warn() {
  local message=$1
  echo -e "[\033[0;33mWarn\033[0m]: " ${message}
}

function fatal() {
  local message=$1
  echo -e "[\033[0;31mError\033[0m]: " ${message}
  exit 1
}

# 启动实例服务
function service_start() {
  # jdk安装目录必须先存在
  if [ ! -d ${jdk_home} ]; then
    fatal "jdk home '${jdk_home}' not found."
  fi
  # 服务实例目录必须先存在
  if [ ! -d ${service_home} ]; then
    fatal "service directory '${service_home}' not found."
  fi
  # 服务日志目录必须先存在
  local service_log_directory=$(dirname ${service_stdout_log})
  if [ ! -d ${service_log_directory} ]; then
    mkdir -p ${service_logpath}
  fi
  # 检查服务实例是否已经启动
  local cmd="ps auxw | grep java | grep -w Dsvr=${service_name} | grep -v grep | grep -v $0 | wc -l"
  local count=$($run -c "$cmd")
  if [[ $count > 0 ]]; then
    print_warn "service ${service_name} already running, start aborted!"
    return 0
  fi
  # 启动服务
  local confargs="-c ${service_home}/${service_application_config}"
  print_info "starting ${service_name} service\c"
  ${jdk_home}/bin/java -Dsvr=${service_name} ${default_jvmoption[*]} -Xloggc:${service_gc_log} -jar ${service_home}/${service_name}.jar ${confargs} > ${service_stdout_log} 2>&1 &
  # 定时判断实例是否启动成功
  sleep 1
  local i=0
  while [[ $i < $start_timeout ]]; do
    count=$($run -c "$cmd")
    if [[ $count > 0 ]]; then
      i=''
      break
    fi
    printf "."
    i=$(expr $i + 1)
    sleep 1
  done
  echo ""
  if test -z "$i" ; then
    print_info "start service ${service_name} success."
  else
    fatal "start service ${service_name} failed! Please check log file in ${service_stdout_log}"
  fi
}

# 程序入口
# 检查操作系统是否支持
uname | grep '^Linux' -q || fatal "Error: $program only support Linux, not support `uname` yet!"
service_start $@
exit 0
