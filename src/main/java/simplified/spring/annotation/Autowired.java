package simplified.spring.annotation;

import java.lang.annotation.*;

/**
 * Autowired注解支持
 *
 * @author leishiguang
 * @since v1.0
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Autowired {
	String value() default "";
}
