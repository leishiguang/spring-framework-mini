package simplified.spring.annotation;

import java.lang.annotation.*;

/**
 * RequestMapping注解支持
 *
 * @author leishiguang
 * @since v1.0
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequestMapping {
	String value() default "";
}
