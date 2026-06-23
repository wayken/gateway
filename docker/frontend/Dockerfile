# 使用Node.js 18的官方镜像作为基础镜像
FROM node:18-alpine AS builder

# 设置工作目录
WORKDIR /app

# 设置npm镜像源以加速包下载
RUN npm config set registry https://registry.npmmirror.com

# 复制package.json和package-lock.json（如果存在）
COPY frontend/package*.json ./

# 安装依赖
RUN npm ci

# 复制源代码
COPY frontend/ .

# 构建应用
RUN npm run build

# 使用nginx作为生产环境的web服务器
FROM nginx:alpine AS production

# 复制自定义的nginx配置
COPY docker/frontend/nginx.conf /etc/nginx/nginx.conf

# 从构建阶段复制构建好的应用到nginx的html目录
COPY --from=builder /app/dist /usr/share/nginx/html

# 暴露端口
EXPOSE 80

# 启动nginx
CMD ["nginx", "-g", "daemon off;"]
