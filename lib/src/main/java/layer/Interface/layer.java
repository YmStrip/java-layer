package layer.Interface;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Documented()
@Target(ElementType.TYPE)
public @interface layer {
	String name() default "";
	
	String implement() default "";
}
