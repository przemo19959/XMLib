package annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.SOURCE)
public @interface SchemaAttribute {
	public String name() default "";
	public boolean required() default false;
	public String documentation() default "";
	public String pattern() default "";
	public boolean unique() default false;
}
