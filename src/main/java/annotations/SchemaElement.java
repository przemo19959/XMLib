package annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(value= ElementType.FIELD)
@Retention(RetentionPolicy.SOURCE)
public @interface SchemaElement {
	public String name() default "";
	public String documentation() default "";
	public boolean required() default false;
}
