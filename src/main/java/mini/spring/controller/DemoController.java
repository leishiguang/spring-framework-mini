package mini.spring.controller;

import mini.spring.annotation.Autowired;
import mini.spring.annotation.Controller;
import mini.spring.annotation.RequestMapping;
import mini.spring.annotation.RequestParam;
import mini.spring.service.DemoService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * DemoController
 *
 * @author leishiguang
 * @since v1.0
 */
@Controller
@RequestMapping("/")
public class DemoController {

	@Autowired
	private DemoService demoService;

	@RequestMapping("/hello")
	public void hello(HttpServletRequest req, HttpServletResponse resp,
					  @RequestParam("name") String name) throws IOException {
		String result = demoService.hello(name);
		resp.getWriter().write(result);
	}

}
