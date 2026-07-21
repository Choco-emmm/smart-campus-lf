# Build stage
FROM node:20-alpine AS build-stage
WORKDIR /app
COPY package*.json ./
# 配置 npm 淘宝镜像加速安装
RUN npm config set registry https://registry.npmmirror.com && npm install
COPY . .
RUN npm run build

# Production stage
FROM nginx:alpine AS production-stage
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build-stage /app/dist /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
