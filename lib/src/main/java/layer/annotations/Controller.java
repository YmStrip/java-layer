package layer.annotations;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Documented()
@Target(ElementType.TYPE)
public @interface Controller {
	String name() default "";
}
