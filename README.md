# BLOG系统的部署

## docker

我们要有docker的环境，在docker上创建mysql的镜像，redis的镜像，接着构建我们的程序镜像

如果是ubuntu，直接

```shell
apt install docker.io
```

## docker-compose

```shell
apt install docker-compose
```

创建docker-compose.yml文件

```yaml
version: "2"
services:
    mysql:
        container_name: "sob-blog-system-mysql"
        network_mode: "host"
        environment:
            MYSQL_ROOT_PASSWORD: "123"
            MYSQL_USER: "feng" #不可以使用root，会报错
            MYSQL_PASS: "123"
        image: "mysql:5.7.39"
        restart: always
        ports:
            - 3306:3306
        volumes:
            - "/home/feng/docker/mysql/db:/var/lib/mysql"
            - "/home/feng/docker/mysql/conf:/etc/mysql"
            - "/home/feng/docker/mysql/log:/var/log/mysql"
```

创建镜像，第一次会去拉取镜像

创建容器

```shell
docker-compose up -d
```



创建完镜像以后，用SQLyog远程登录一下mysql数据库

一是为了检查是否可以连接成功

二是为了执行sql脚本，创建数据库

## redis

创建docker-compose.yml文件

```yaml
version: '2'
services:
    redis:
      image: redis
      container_name: sob-blog-system-redis
      command: redis-server --requirepass 123456
      restart: always
      ports:
        - "6379:6379"
      volumes:
        - "/home/feng/docker/redis/data:/data"
```

创建容器

```shell
docker-compose up -d
```

创建成功后，可以用RedisDesktopManager连接测试一下。

## nginx

创建docker-compose.yml

```yaml
version: "2"
services:
    nginx:
        container_name: "sob-blog-system-nginx"
        network_mode: "host"
        image: "nginx:stable"
        restart: always
        ports:
            - 80:80
            - 443:443
        volumes:
            - "/home/feng/docker/nginx/conf/nginx.conf:/etc/nginx/nginx.conf"
            - "/home/feng/docker/nginx/wwwroot:/usr/share/nginx/wwwroot"
            - "/home/feng/docker/nginx/log:/var/log/nginx"
```

配置文件

nginx.conf

```shell
server {
    listen       80;
    server_name  localhost;
    
    location / {
        root   /usr/share/nginx/wwwroot;
        index  index.html index.htm;
    }
}
```

创建容器

```shell
docker-compose up -d
```

## 部署后台程序

这里少了测试的环节，应该是要测试完再上线的哦。

生产环境准备

- mysql
- redis
- 编写生产环境的配置文件
- 禁止swagger-ui可用
- 创建图片上传的目录
- 编译程序
- 构建镜像
- 部署程序

## 构建docker镜像

创建在docker目录下创建文件夹sobweb，将打包后的jar包拷贝到该目录下，chmod -777；

在sobweb目录下创建Dockerfile文件

1. Dockerfile

   ```shell
   # 基于这个镜像构建
   FROM openjdk:8-jre
   # 作者
   MAINTAINER feng
   RUN mkdir /usr/app
   # 创建图片上传的路径
   RUN mkdir /usr/app/upload
   # 复制程序到内部
   ADD feng-blog-1.0.0.jar /usr/app
   # 切换工作目录
   WORKDIR /usr/app
   # 暴露容器的端口，通知Docker容器在运行时监听指定的网络端口
   EXPOSE 2020
   # 镜像运行时执行
   ENTRYPOINT ["java","-jar","feng-blog-1.0.0.jar"]
   ```

2. 构建镜像，在当前目录下执行构建命令

   ```shell
   docker build -t feng_blog:1.0 .
   ```

   默认使用当前目录下的Dockerfile进行编译镜像，编译后的镜像名为feng_blog:1.0

3. 运行镜像

   ```shell
   docker run -p 2020:2020 --name=feng_blog --restart=always -d -v /home/feng/docker/images:/usr/app/upload  feng_blog:1.0
   ```

-p 宿主机端口:容器端口：代表把容器里的2020端口映射给宿主机的2020端口。这样做了映射之后，可以通过2020端口访问该项目的主页。

**springboot配置文件application.yml里配置的server:port端口号要跟容器的端口号一致**

## 技术介绍

**后端：** SpringBoot + nginx + docker + SpringSecurity + Swagger2 + Mysql + Redis 

##  开发环境

| 说明              | 开发工具              |
| ----------------- | --------------------- |
| Java开发工具IDE   | IDEA                  |
| API调试工具       | Postman               |
| MySQL远程连接工具 | SQLyog                |
| Redis远程连接工具 | Redis Desktop Manager |
| Linux远程连接工具 | X-shell               |
| Linux文件上传工具 | filezilla             |

| 开发环境 | 版本               |
| -------- | ------------------ |
| JDK      | 1.8                |
| MySQL    | 5.7.39             |
| Redis    | latest             |
| nginx    | stable             |
| Ubuntu   | Ubuntu 20.04.4 LTS |