## 手写简化版 spring framework

300 行代码提炼 spring 核心原理

<p align="center">
	<a target="_blank" href="https://github.com/leishiguang/spring-framework-mini/blob/master/LICENSE">
		<img src="https://img.shields.io/apm/l/vim-mode.svg?color=yellow" ></img>
	</a>
	<a target="_blank" href="https://www.oracle.com/technetwork/java/javase/downloads/index.html">
		<img src="https://img.shields.io/badge/JDK-1.8+-green.svg" ></img>
	</a>
</p>

------------------


### 构建方式

1. 导入 build.gradle 工程；
2. ide 中的 servlet 容器（如 tomcat）中，添加本项目的 web 包；
3. 启动 servlet 容器；

### 运行效果

启动后，在浏览器输入：localhost:8080/hello?name=world
即可看到浏览器返回：hello world

如图所示：

![image](https://raw.githubusercontent.com/leishiguang/spring-framework-mini/master/docs/images/helloWorld.png)

### 原理解析

目录结构

```
src/main
├─java
│  └─simplified
│      └─spring
│          ├─annotation 注解支持
│          │      Autowired.java 
│          │      Controller.java
│          │      RequestMapping.java
│          │      RequestParam.java
│          │      Service.java
│          │
│          ├─controller 
│          │      DemoController.java 演示用的Controller
│          │
│          ├─service
│          │  │  DemoService.java 演示用的Service
│          │  │
│          │  └─impl
│          │          DemoServiceImpl.java 演示用的Service实现
│          │
│          └─servlet
│                  DispatcherServlet.java Servlet入口
│
├─resources
│      application.properties
│
└─webapp
    └─WEB-INF
            web.xml
```