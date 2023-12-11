package layer.annotations;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Documented()
@Target(ElementType.TYPE)
public @interface LayerClass {
	String name() default "";
	
	String implement() default "";
}
