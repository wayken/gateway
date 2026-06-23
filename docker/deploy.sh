#!/bin/bash

# 网关 Docker 部署脚本
# 支持一键构建、启动、停止、重启、查看日志、状态、清理等操作

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 打印带颜色的消息
function print_message() {
    echo -e "${2}${1}${NC}"
}

# 检查命令是否存在
check_command() {
    if ! command -v $1 &> /dev/null; then
        print_message "Error: $1 command not found, please install $1 first" $RED
        exit 1
    fi
}

# 显示帮助信息
function show_help() {
    echo "Gateway Docker Deployment Script"
    echo ""
    echo "Usage: $0 [option]"
    echo ""
    echo "Options:"
    echo "  build         Build images"
    echo "  run           Run services"
    echo "  stop          Stop services"
    echo "  logs          Show logs"
    echo "  status        Show service status"
    echo "  clean         Remove images and containers"
    echo "  help          Show this help information"
    echo ""
}

# 构建镜像
function build_image() {
    print_message "Starting to build gateway image..." $BLUE
    
    docker compose build
    
    print_message "Image build completed!" $GREEN
}

# 启动服务（完整版）
function run_services() {
    print_message "Starting gateway services (full version)..." $BLUE
    
    # 启动服务
    docker-compose up -d
    
    print_message "Services started!" $GREEN
    print_message "Gateway Service: http://0.0.0.0:8080" $BLUE
}

# 停止服务
function stop_services() {
    print_message "Stopping gateway services..." $YELLOW

    if [ -f docker-compose.yml ]; then
        docker-compose down
    fi
    
    print_message "Services stopped!" $GREEN
}

# 查看日志
function show_logs() {
    if [ -z "$2" ]; then
        docker-compose -f docker-compose.yml logs -f
    else
        docker-compose -f docker-compose.yml logs -f $2
    fi
}

# 查看服务状态
function show_status() {
    print_message "Gateway service status:" $BLUE
    
    # 检查完整版服务
    docker-compose ps
}

# 清理镜像和容器
function clean_all() {
    print_message "Cleaning up Docker resources..." $YELLOW
    
    # 停止服务
    stop_services
    
    # 删除镜像，只删除frontend、dashboard、kernel相关镜像
    docker images | grep -E "gateway-frontend|gateway-dashboard|gateway-kernel" | awk '{print $3}' | xargs -r docker rmi -f

    print_message "Cleanup completed!" $GREEN
}

# 检查Docker环境
function check_docker() {
    check_command docker
    check_command docker-compose

    # 检查Docker是否在运行
    if ! docker info &> /dev/null; then
        print_message "Error: Docker service is not running, please start Docker first" $RED
        exit 1
    fi
}

# 主函数
function main() {
    # 检查Docker环境
    check_docker
    
    case "${1:-help}" in
        build)
            clean_all
            build_image
        ;;
        run)
            stop_services
            run_services
            ;;
        stop)
            stop_services
            ;;
        logs)
            show_logs $@
            ;;
        status)
            show_status
            ;;
        clean)
            clean_all
            ;;
        help|*)
            show_help
            ;;
    esac
}

# 运行主函数
main $@
