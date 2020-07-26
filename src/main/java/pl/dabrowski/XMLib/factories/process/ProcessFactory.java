package pl.dabrowski.XMLib.factories.process;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

import javax.lang.model.element.Element;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import pl.dabrowski.XMLib.strategies.process.ProcessStrategy;
import pl.dabrowski.XMLib.utils.ServiceDTO;

@RequiredArgsConstructor
public class ProcessFactory {
	private static final String NO_STRATEGY_ERROR = "Annotation doesn't have strategy!";
	private final ServiceDTO serviceDTO;

	private static final Map<Class<? extends Annotation>, Class<? extends ProcessStrategy>> MAP;
	static {
		MAP = new HashMap<>();
	}
	
	//@formatter:off
	public static void register(Class<? extends Annotation> key, Class<? extends ProcessStrategy> value) {MAP.put(key, value);}
	//@formatter:on

	@SneakyThrows
	public ProcessStrategy getInstance(Element annotatedElement) {
		Annotation a = null;
		for(Class<? extends Annotation> annotation:MAP.keySet()) {
			if((a = annotatedElement.getAnnotation(annotation)) != null)
				return MAP.get(annotation)//
					.getDeclaredConstructor(ServiceDTO.class, Annotation.class, Element.class)//
					.newInstance(serviceDTO, a, annotatedElement);
		}
		throw new IllegalArgumentException(NO_STRATEGY_ERROR);
	}
}
