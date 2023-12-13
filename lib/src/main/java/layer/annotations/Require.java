package layer.annotations;

import java.lang.annotation.*;

public
@Retention(RetentionPolicy.RUNTIME)
@Documented()
@Target(ElementType.FIELD) @interface Require {
	String name() default "";
	Class require() default Require.class;
}