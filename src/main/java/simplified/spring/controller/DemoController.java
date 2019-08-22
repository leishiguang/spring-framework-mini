package simplified.spring.controller;

import simplified.spring.annotation.Autowired;
import simplified.spring.annotation.Controller;
import simplified.spring.annotation.RequestMapping;
import simplified.spring.annotation.RequestParam;
import simplified.spring.service.DemoService;

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
