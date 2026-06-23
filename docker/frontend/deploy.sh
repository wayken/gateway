#!/bin/bash

# Gateway Frontend Docker Build and Run Script

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 项目信息
PROJECT_NAME="gateway-frontend"
IMAGE_NAME="gateway-frontend:latest"
CONTAINER_NAME="gateway-frontend"

# 函数：打印彩色信息
function print_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

function print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

function print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 函数：构建Docker镜像
function build_image() {
    print_info "Starting to build Docker image..."
    docker build -t $IMAGE_NAME -f Dockerfile ../../
    if [ $? -eq 0 ]; then
        print_info "Docker image built successfully!"
    else
        print_error "Docker image build failed!"
        exit 1
    fi
}

# 函数：停止并删除现有容器
function stop_container() {
    if [ "$(docker ps -q -f name=$CONTAINER_NAME)" ]; then
        print_info "Stopping running container..."
        docker stop $CONTAINER_NAME
    fi
    
    if [ "$(docker ps -aq -f name=$CONTAINER_NAME)" ]; then
        print_info "Removing existing container..."
        docker rm $CONTAINER_NAME
    fi
}

# 函数：运行容器
function run_container() {
    print_info "Starting new container..."
    docker run -d \
        --name $CONTAINER_NAME \
        -p 8229:80 \
        --restart unless-stopped \
        $IMAGE_NAME
    
    if [ $? -eq 0 ]; then
        print_info "Container started successfully!"
        print_info "Frontend application URL: http://0.0.0.0:8229"
    else
        print_error "Container startup failed!"
        exit 1
    fi
}

# 函数：查看日志
function show_logs() {
    print_info "Showing container logs..."
    docker logs -f $CONTAINER_NAME
}

# 函数：显示帮助信息
function show_help() {
    echo "Usage: $0 [option]"
    echo ""
    echo "Options:"
    echo "  build     Build Docker image"
    echo "  rebuild   Rebuild Docker image (stop and remove existing container first)"
    echo "  run       Run container (build image first)"
    echo "  stop      Stop container"
    echo "  logs      Show container logs"
    echo "  clean     Clean up images and containers"
    echo "  help      Show this help information"
}

# 函数：清理资源
clean_up() {
    print_warning "Cleaning up Docker resources..."
    stop_container
    if [ "$(docker images -q $IMAGE_NAME)" ]; then
        docker rmi $IMAGE_NAME
        print_info "Image deleted"
    fi
}

# 主逻辑
case "$1" in
    build)
        build_image
        ;;
    rebuild)
        clean_up
        build_image
        ;;
    run)
        stop_container
        run_container
        ;;
    stop)
        stop_container
        ;;
    logs)
        show_logs
        ;;
    clean)
        clean_up
        ;;
    help|*)
        show_help
        ;;
esac
