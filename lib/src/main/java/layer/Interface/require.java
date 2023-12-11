package layer.Interface;

import java.lang.annotation.*;

public
@Retention(RetentionPolicy.RUNTIME)
@Documented()
@Target(ElementType.FIELD) @interface require {
	String name() default "";
}