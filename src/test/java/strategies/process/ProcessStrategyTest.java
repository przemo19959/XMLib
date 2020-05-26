package strategies.process;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.lang.model.element.Element;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.google.testing.compile.CompilationRule;

import processors.ServiceDTO;
import strategies.process.impl.SchemaElementStrategy;

public class ProcessStrategyTest {
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
	public void test1() throws Exception  {
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
}
