#!/bin/bash

: '
关闭网关控制台服务
'
readonly program="`basename $0`"
readonly run=/bin/sh
# jdk安装路径&服务实例路径
readonly jdk_home=/usr/local/jdk
readonly service_name=teambeit-gateway-dashboard
readonly service_home=/root/gateway/dashboard
readonly service_stdout_log=/root/gateway/dashboard/stdout.log
# 启动实例超时时间
readonly shutdown_timeout=5

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

# 关闭实例服务
function service_stop() {
  # jdk安装目录必须先存在
  if [ ! -d ${jdk_home} ]; then
    fatal "jdk home '${jdk_home}' not found."
  fi
  # 服务实例目录必须先存在
  if [ ! -d ${service_home} ]; then
    fatal "service directory '${service_home}' not found."
  fi
  # 检查服务实例是否已经启动
  local cmd="ps auxw | grep java | grep -w Dsvr=${service_name} | grep -v grep | grep -v $0 | awk '{print \$2;}'"
  local pid=$($run -c "$cmd")
  # 指定的服务实例没有启动
  if [ -z "$pid" ]; then
    print_warn "service ${service_name} not found!"
    return 0
  fi
  print_info "stoping service ${service_name}\c"
  # 定时判断实例是否关闭成功
  local i=0
  while (( $i < $shutdown_timeout )); do
    pid=$($run -c "$cmd")
    # 输出进度条
    test -z ${pid} && i='' && break
    printf "."
    i=$(expr $i + 1)
    # 关闭服务实例，先正常关闭3次，关闭失败再强制关闭
    if (( $i < 3 )); then
      kill -15 $pid
    else
      kill -9 $pid
    fi
    sleep 1
  done
  echo ""
  if [ -z "$pid" ]; then
    print_info "stop service ${service_name} success."
	else
    fatal "stop service ${service} failed! Please check log file in ${service_stdout_log}"
	fi
}

# 程序入口
# 检查操作系统是否支持
uname | grep '^Linux' -q || fatal "Error: $program only support Linux, not support `uname` yet!"
service_stop
exit 0
