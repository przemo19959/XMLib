package pl.dabrowski.XMLib.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Field annotated with that annotation is treated as xs:attribute tag in generated XSD file. 
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.SOURCE)
public @interface SchemaAttribute {
	/**
	 * Value that will be assigned to "name" attribute of xs:attribute tag. By default its annotated field name.
	 */
	public String name() default "";
	/**
	 * Value that will be assigned to "use" attribute of xs:attribute tag. By default its false, so value is optional.
	 */
	public boolean required() default false;
	/**
	 * Value that will be assigned to xs:documentation tag of xs:attribute tag. By default its empty, so that there is not documentation.
	 */
	public String documentation() default "";
	/**
	 * Value that will be assigned to xs:pattern tag within xs:attribute tag. By default its empty, so no restriction is set.
	 */
	public String pattern() default "";
	/**
	 * Value controls whether attribute value across enclosing xs:element is unique. By default it's not unique. 
	 */
	public boolean unique() default false;
}
