#!/bin/bash

: '
重启网关控制台服务
'
readonly script_path=$(cd "$(dirname "$0")"; pwd)

sh ${script_path}/shutdown.sh
sh ${script_path}/startup.sh
exit 0
