## 手写简化版 spring framework

300 行代码提炼 spring 核心原理

### 构建方式

1. 导入 build.gradle 工程；
2. ide 中的 servlet 容器（如 tomcat）中，添加本项目的 web 包；
3. 启动 servlet 容器；

### 运行效果

启动后，在浏览器输入：localhost:8080/hello?name=world
即可看到浏览器返回：hello world

如图所示：

![image](https://github.com/leishiguang/spring-framework-mini/docs/images/helloWorld.png)

### 原理解析


