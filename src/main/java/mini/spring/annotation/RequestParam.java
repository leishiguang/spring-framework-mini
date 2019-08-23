package mini.spring.annotation;

import java.lang.annotation.*;

/**
 * RequestParam注解支持
 *
 * @author leishiguang
 * @since v1.0
 */
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequestParam {
	String value() default "";
}
