构建镜像
docker build -t unity-app .


测试运行
docker run --rm -it --name unity-container -p 7825:7825  unity-app

运行容器
docker rm -f unity-container
docker run --name unity-container -p 7825:7825 -d unity-app


查看日志
docker logs -f my-app
