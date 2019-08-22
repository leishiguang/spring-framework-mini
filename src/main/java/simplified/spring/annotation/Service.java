package simplified.spring.annotation;

import java.lang.annotation.*;

/**
 * Service注解支持
 *
 * @author leishiguang
 * @since v1.0
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Service {
	String value() default "";
}
