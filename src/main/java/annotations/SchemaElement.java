package annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(value= ElementType.FIELD)
@Retention(RetentionPolicy.SOURCE)
public @interface SchemaElement {
	//TODO - 26 maj 2020:poprawić, żeby nazwa była pobierana automatycznie z oznaczonego pola
	public String name();
	public String documentation() default "";
	public boolean required() default false;
}
