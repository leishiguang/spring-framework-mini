package simplified.spring.annotation;

import java.lang.annotation.*;

/**
 * Controller注解支持
 *
 * @author leishiguang
 * @since v1.0
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Controller {
	String value() default "";
}
