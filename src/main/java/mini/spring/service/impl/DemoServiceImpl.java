package mini.spring.service.impl;

import mini.spring.annotation.Service;
import mini.spring.service.DemoService;

/**
 * DemoServiceImpl
 *
 * @author leishiguang
 * @since v1.0
 */
@Service
public class DemoServiceImpl implements DemoService {
	@Override
	public String hello(String name) {
		return "hello "+name;
	}
}
