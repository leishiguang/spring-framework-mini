package simplified.spring.servlet;

import simplified.spring.annotation.*;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Servlet 初始化入口
 *
 * @author leishiguang
 * @since v1.0
 */
public class DispatcherServlet extends HttpServlet {

	/**
	 * 保存 application.properties 配置文件中的内容
	 */
	private Properties contextConfig = new Properties();

	/**
	 * 保存扫描到的所有类名
	 */
	private List<String> classNames = new ArrayList<>();

	/**
	 * Ioc 容器，这儿简化，仅用 Hash 表示
	 */
	private Map<String, Object> ioc = new HashMap<>();

	/**
	 * 保存 url -> Method 的对应关系
	 */
	private List<Handler> handlerMapping = new ArrayList<>();

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		this.doPost(req, resp);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		try {
			doDispatch(req, resp);
		} catch (Exception e) {
			e.printStackTrace();
			resp.getWriter().write("500 Exception " + Arrays.toString(e.getStackTrace()));
		}
	}

	@Override
	public void init(ServletConfig config) {
		//加载配置文件
		doLoadConfig(config.getInitParameter("contextConfigLocation"));
		//扫描相关类
		doScanner(contextConfig.getProperty("scanPackage"));
		//初始化扫描到的类，把他们放入 Ioc 容器中
		doInstance();
		//完成依赖注入
		doAutowired();
		//初始化 HandlerMapping
		initHandlerMapping();
		System.out.println("Mini Spring Framework is init.");
	}


	private void doDispatch(HttpServletRequest req, HttpServletResponse resp) throws IOException, InvocationTargetException, IllegalAccessException {
		Handler handler = getHandler(req);
		if (handler == null) {
			resp.getWriter().write("404 Not Found！");
			return;
		}
		//获得方法的行参列表
		Class<?>[] paramTypes = handler.getParamTypes();
		Object[] paramValues = new Object[paramTypes.length];
		Map<String, String[]> params = req.getParameterMap();
		for (Map.Entry<String, String[]> param : params.entrySet()) {
			String value = Arrays.toString(param.getValue())
					.replaceAll("[\\[\\]]", "")
					.replaceAll("\\s", ",");
			if (!handler.paramIndexMapping.containsKey(param.getKey())) {
				continue;
			}
			int index = handler.paramIndexMapping.get(param.getKey());
			paramValues[index] = convert(paramTypes[index], value);
		}
		if (handler.paramIndexMapping.containsKey(HttpServletRequest.class.getName())) {
			int reqIndex = handler.paramIndexMapping.get(HttpServletRequest.class.getName());
			paramValues[reqIndex] = req;
		}
		if (handler.paramIndexMapping.containsKey(HttpServletResponse.class.getName())) {
			int reqIndex = handler.paramIndexMapping.get(HttpServletResponse.class.getName());
			paramValues[reqIndex] = resp;
		}
		Object returnValue = handler.method.invoke(handler.controller,paramValues);
		if(returnValue == null || returnValue instanceof Void){
			return;
		}
		resp.getWriter().write(returnValue.toString());
	}


	/**
	 * 加载配置文件
	 *
	 * @param contextConfigLocation contextConfigLocation
	 */
	private void doLoadConfig(String contextConfigLocation) {
		//直接通过类路径，找到 Spring 主配置文件所在路径
		//将其读取出来，并放到 Properties 中
		try (InputStream fis = this.getClass().getClassLoader().getResourceAsStream(contextConfigLocation)) {
			if (fis == null) {
				throw new NullPointerException("读取配置不能为空");
			}
			contextConfig.load(fis);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 扫描相关类
	 *
	 * @param scanPackage 要扫描的包路径
	 */
	private void doScanner(String scanPackage) {
		URL url = this.getClass().getClassLoader().getResource("/" + scanPackage.replaceAll("\\.", "/"));
		assert url != null;
		File classPath = new File(url.getFile());
		for (File file : Objects.requireNonNull(classPath.listFiles())) {
			if (file.isDirectory()) {
				doScanner(scanPackage + "." + file.getName());
			} else {
				if (!file.getName().endsWith(".class")) {
					continue;
				}
				String className = scanPackage + "." + file.getName().replaceAll(".class", "");
				classNames.add(className);
			}
		}
	}

	/**
	 * 类似于 bean 初始化，为 DI 注入做准备
	 */
	private void doInstance() {
		assert classNames.size() != 0;
		for (String className : classNames) {
			Class<?> clazz = null;
			try {
				clazz = Class.forName(className);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			//加了注解的类需要初始化
			assert clazz != null;
			if (clazz.isAnnotationPresent(Controller.class)) {
				Object instance = null;
				try {
					instance = clazz.newInstance();
				} catch (InstantiationException | IllegalAccessException e) {
					e.printStackTrace();
				}
				//bean的命名格式以小写开头
				String beanName = toLowerFirstCase(clazz.getSimpleName());
				ioc.put(beanName, instance);
			} else if (clazz.isAnnotationPresent(Service.class)) {
				//自定义bean名称
				Service service = clazz.getAnnotation(Service.class);
				String beanName = service.value();
				if ("".equals(beanName)) {
					beanName = toLowerFirstCase(clazz.getSimpleName());
				}
				Object instance = null;
				try {
					instance = clazz.newInstance();
				} catch (InstantiationException | IllegalAccessException e) {
					e.printStackTrace();
				}
				ioc.put(beanName, instance);
				//根据类型自动赋值
				for (Class<?> i : clazz.getInterfaces()) {
					if (ioc.containsKey(i.getName())) {
						throw new RuntimeException("The '" + i.getName() + "' is exists!");
					}
					//把接口的类型直接当成key
					ioc.put(i.getName(), instance);
				}
			}
		}
	}

	/**
	 * 首字母小写
	 *
	 * @param simpleName 首字母要变成小写的字符
	 * @return 首字母转为小写
	 */
	private String toLowerFirstCase(String simpleName) {
		char[] chars = simpleName.toCharArray();
		chars[0] = Character.toLowerCase(chars[0]);
		return String.valueOf(chars);
	}

	/**
	 * 执行 DI 注入
	 */
	private void doAutowired() {
		assert ioc.size() != 0;
		for (Map.Entry<String, Object> entry : ioc.entrySet()) {
			//获取所有的字段，包括 private、protected、public 类型的
			//正常来讲，普通的 OOP 编程只能获得 public 类型的字段
			Field[] fields = entry.getValue().getClass().getDeclaredFields();
			for (Field field : fields) {
				if (!field.isAnnotationPresent(Autowired.class)) {
					continue;
				}
				Autowired autowired = field.getAnnotation(Autowired.class);
				//如果用户没有自定义 beanName，默认就根据类型注入
				String beanNeme = autowired.value();
				if ("".equals(beanNeme)) {
					//这儿的 beanName 是接口类的名字
					beanNeme = toLowerFirstCase(field.getType().getName());
				}
				//如果是public以外的类型，只要加了 @Autowired 注解都要强制赋值
				field.setAccessible(true);
				try {
					field.set(entry.getValue(), ioc.get(beanNeme));
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * 初始化 HandlerMapping
	 */
	private void initHandlerMapping() {
		assert ioc.size() != 0;
		for (Map.Entry<String, Object> entry : ioc.entrySet()) {
			Class<?> clazz = entry.getValue().getClass();
			if (!clazz.isAnnotationPresent(Controller.class)) {
				continue;
			}
			//保存在类上面的 RequestMapping 和类上面的 url 配置
			String baseUrl = "";
			if (clazz.isAnnotationPresent(RequestMapping.class)) {
				RequestMapping requestMapping = clazz.getAnnotation(RequestMapping.class);
				baseUrl = requestMapping.value();
			}
			//默认获取所有的 public 类型的方法，以及 Method 的 url 配置
			for (Method method : clazz.getMethods()) {
				if (!method.isAnnotationPresent(RequestMapping.class)) {
					continue;
				}
				//映射 url
				RequestMapping requestMapping = method.getAnnotation(RequestMapping.class);
				String url = ("/" + baseUrl + "/" + requestMapping.value()).replaceAll("/+", "/");
				Pattern pattern = Pattern.compile(url);
				handlerMapping.add(new Handler(entry.getValue(), method, pattern));
				System.out.println("Mapped : " + url + " -> " + method);
			}
		}
	}

	/**
	 * 获取对应 Handler
	 */
	private Handler getHandler(HttpServletRequest req){
		assert handlerMapping.size() > 0;
		String url = req.getRequestURI();
		String contextPath = req.getContextPath();
		url = url.replace(contextPath,"").replaceAll("/+","/");
		for(Handler handler:handlerMapping){
			Matcher matcher = handler.pattern.matcher(url);
			if(matcher.matches()){
				return handler;
			}
		}
		return null;
	}

	/**
	 * 由于 HTTP 基于字符串协议，url 传过来的参数都是 String 类型的
	 * 只需要把 String 转换为任意类型
	 */
	private Object convert(Class<?> type, String value) {
		if (Integer.class == type) {
			return Integer.valueOf(value);
		}
		// 如果还有 double 或者其它类型的参数，继续增加 if
		// 可以使用策略模式进行优化，这儿略。
		return value;
	}

	/**
	 * 内部类，Handler 记录 Controller 中 RequestMapping 和 Method 的对应关系
	 */
	private static class Handler {
		/**
		 * 保存方法对应的实例
		 */
		private Object controller;

		/**
		 * 保存映射的方法
		 */
		private Method method;

		/**
		 * 正则表达式
		 */
		private Pattern pattern;


		/**
		 * 参数顺序
		 */
		private Map<String, Integer> paramIndexMapping;

		Handler(Object controller, Method method, Pattern pattern) {
			this.controller = controller;
			this.method = method;
			this.pattern = pattern;
			paramIndexMapping = new HashMap<>(6);
			putParamIndexMapping(method);
		}

		/**
		 * 提取方法中加了注解的参数，以及 Request 和 Response 参数
		 */
		private void putParamIndexMapping(Method method) {
			Annotation[][] pa = method.getParameterAnnotations();
			for (int i = 0; i < pa.length; i++) {
				for (Annotation a : pa[i]) {
					if (a instanceof RequestParam) {
						String paramName = ((RequestParam) a).value();
						if (!"".equals(paramName.trim())) {
							paramIndexMapping.put(paramName, i);
						}
					}
				}
			}
			//提取方法中的 request 和 response 参数
			Class<?>[] paramsTypes = method.getParameterTypes();
			for (int i = 0; i < paramsTypes.length; i++) {
				Class<?> type = paramsTypes[i];
				if (type == HttpServletRequest.class || type == HttpServletResponse.class) {
					paramIndexMapping.put(type.getName(), i);
				}
			}
		}

		/**
		 * 获取方法所有需要的参数
		 */
		private Class<?>[] getParamTypes() {
			return method.getParameterTypes();
		}
	}

}
