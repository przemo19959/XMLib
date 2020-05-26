package strategies.process;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.google.testing.compile.CompilationRule;

import processors.ServiceDTO;
import strategies.process.impl.SchemaElementStrategy;

public class ProcessStrategyTest {
	private static final boolean LOG=false;
	
	@Rule
	public CompilationRule rule = new CompilationRule();
	private Elements elements;
	private Types types;

	private ProcessStrategy processStrategy;

	@Before
	public void setup() {
		elements = rule.getElements();
		types = rule.getTypes();
		ServiceDTO serviceDTO = new ServiceDTO(null, types, elements, null);
		processStrategy = new SchemaElementStrategy(serviceDTO, null, null);
	}

	@Test
	public void test1() throws Exception {
		Element example = elements.getTypeElement(List.class.getCanonicalName());
		assertTrue(processStrategy.isCollectionType(example));
		example = elements.getTypeElement(Set.class.getCanonicalName());
		assertTrue(processStrategy.isCollectionType(example));
	}

	@Test
	public void test2() throws Exception {
		Element example = elements.getTypeElement(Number.class.getCanonicalName());
		assertFalse(processStrategy.isCollectionType(example));
		example = elements.getTypeElement(String.class.getCanonicalName());
		assertFalse(processStrategy.isCollectionType(example));
		example = elements.getTypeElement(Map.class.getCanonicalName());
		assertFalse(processStrategy.isCollectionType(example));
	}

	@Test
	public void test3() throws Exception {
		Element example = elements.getTypeElement(Worker.class.getCanonicalName());
		List<? extends Element> members = example.getEnclosedElements();
		ElementKind[] kindEquals= {ElementKind.FIELD,ElementKind.FIELD,ElementKind.FIELD,ElementKind.CONSTRUCTOR};
		ElementKind[] kindTypeEquals= {ElementKind.CLASS,null,ElementKind.ENUM,null};
		
		for(int i=0;i<members.size();i++) {
			Element tmp2 = members.get(i);
			Element tmp = types.asElement(tmp2.asType());
			log(tmp2.toString()+", "+tmp2.getKind() + ", " + (tmp != null ? tmp.getKind() : "NULL"));
			assertTrue(processStrategy.equalsKind(tmp2, kindEquals[i]));
			if(kindTypeEquals[i]!=null)
				assertTrue(processStrategy.equalsTypeKind(tmp2, kindTypeEquals[i]));
			else
				assertFalse(processStrategy.equalsTypeKind(tmp2, kindTypeEquals[i]));
		}
	}
	
	private void log(String msg) {
		if(LOG)
			System.out.println(msg);
	}
}
