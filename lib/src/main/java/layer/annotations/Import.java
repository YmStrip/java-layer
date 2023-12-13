package layer.annotations;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Documented()
@Target(ElementType.FIELD)
public @interface Import {
	String name();
	
	Class require() default Import.class;
}
