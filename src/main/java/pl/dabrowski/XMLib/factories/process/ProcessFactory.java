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
	private final ServiceDTO serviceDTO;

	private static final Map<Class<? extends Annotation>, Class<? extends ProcessStrategy>> map;
	static {
		map = new HashMap<>();
	}

	public static void register(Class<? extends Annotation> key, Class<? extends ProcessStrategy> value) {
		map.put(key, value);
	}

	@SneakyThrows
	public ProcessStrategy getInstance(Element annotatedElement) {
		Annotation a = null;
		for(Class<? extends Annotation> annotation:map.keySet()) {
			if((a = annotatedElement.getAnnotation(annotation)) != null)
				return map.get(annotation)//
					.getDeclaredConstructor(ServiceDTO.class, Annotation.class, Element.class)//
					.newInstance(serviceDTO, a, annotatedElement);
		}
		throw new IllegalArgumentException("Annotation doesn't have strategy!");
	}
}
