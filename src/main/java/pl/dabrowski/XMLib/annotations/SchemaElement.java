package pl.dabrowski.XMLib.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Collection;

/**
 * Field annotated with that annotation is treated as xs:element tag in generated XSD file. Some attributes and elements of this xs:element are controlled through annotation fields. "maxOccurs"
 * attribute is controlled automatically based on annotated field type. If type is subtype of {@link Collection}, then maxOccurs=unbounded, otherwise it's 1. If annotated field is of type in which
 * other fields are annotated by {@link SchemaElement} or {@link SchemaAttribute}, then xs:complexType and xs:sequence are added automatically.
 */
@Target(value = ElementType.FIELD)
@Retention(RetentionPolicy.SOURCE)
public @interface SchemaElement {
	/**
	 * Value that will be assigned to "name" attribute of xs:element tag. By default its annotated field name.
	 */
	public String name() default "";
	/**
	 * Value that will be assigned to xs:documentation tag of xs:element tag. By default its empty, so that there is not documentation.
	 */
	public String documentation() default "";
	/**
	 * Value that will be assigned to "minOccurs" attribute of xs:element tag. By default its false, so minOccurs=0.
	 */
	public boolean required() default false;
}
