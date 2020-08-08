package pl.dabrowski.XMLib.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotations causes XSD file to be created based on annotated class.
 * 
 * @author hex
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface GenerateSchema {
	/**
	 * Path relative to project base directory, where XSD file will be created. By default it is project base directory (where pom.xml is, if Maven is used). For directory separation "/" sign must be
	 * used, example: /schemas/. Dots "." will be interpreted as part of directory name.
	 */
	public String path() default "";
	/**
	 * Class annotated with {@link GenerateSchema} is automatically root xs:element of generated XSD file. By default name of that element is "root".
	 */
	public SchemaElement rootElement() default @SchemaElement(name = "root");
}
