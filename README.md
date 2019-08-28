## 300 行代码提炼 spring framework 核心原理

本项目包含 IoC、DI、MVC 模块，
另有姊妹篇 **[spring-framework-simplified](https://github.com/leishiguang/spring-framework-simplified)** 对 beanFactory、HandlerMapper、动态视图解析、AOP 有更深入的解析

<p>
	<a target="_blank" href="https://github.com/leishiguang/spring-framework-mini/blob/master/LICENSE">
		<img src="https://img.shields.io/apm/l/vim-mode.svg?color=yellow" ></img>
	</a>
	<a target="_blank" href="https://www.oracle.com/technetwork/java/javase/downloads/index.html">
		<img src="https://img.shields.io/badge/JDK-1.8+-green.svg" ></img>
	</a>
</p>

---

### 构建方式

1. 导入 build.gradle 工程；
2. 添加 web 包至 servlet 容器（如 tomcat）；
3. 启动 servlet 容器；

### 运行效果

在控制台看到末尾出现日志 `Mini Spring Framework is init.` 即表示启动成功。
此时在浏览器输入：localhost:8080/hello?name=world
即可看到浏览器返回：hello world

如图所示：

![image](https://raw.githubusercontent.com/leishiguang/spring-framework-mini/master/docs/images/helloWorld.png)

### 原理解析

重点在于 DispatcherServlet 的初始化过程，分为如下几个步骤：
1. 加载配置文件：获取扫描包的路径；
2. 扫描相关类：将添加了 @Service 或 @Controller 注解的类名，保存在 List 容器中；
3. 初始化扫描到的类：以 beanName 作为键值，在 HashMap 中保存初始化好的类对象，即放入 IoC 容器中；
4. 完成依赖注入：对增加了 @Autowired 注解的属性，注入 IoC 容器中的 bean；
5. 初始化 HandlerMapping：将 @Controller 类中，增加了 @RequestMapping 注解的方法，依据 url 配置，加入到 HandlerMapping 中；

初始化完毕，请求进来时：
1. 首先到达 doGet 或者 doPost 方法，并交由 doDispatch 方法进行处理；
2. 依据 url 从 HandlerMapping 中获取对应的 Handler；
3. 从 Handler 中取得对应方法的参数列表；
4. 按 Handler 的参数顺序，从 req 中获取到参数值；
5. 反射执行 Handler 中保存的方法，获得返回值；
6. resp 写出返回值，完成请求；

当然，真正的 spring framework 要复杂许多，这儿主要是了解 spring 的基本设计思路，以及设计模式的应用。

更深入了解 spring framework 原理：**[spring-framework-simplified](https://github.com/leishiguang/spring-framework-simplified)**

### 目录结构

```
src/main
├─java
│  └─mini
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

最后祝大家生活愉快~