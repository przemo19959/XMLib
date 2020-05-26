package processors;

import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import services.XMLService;

@RequiredArgsConstructor
@Getter
public class ServiceDTO {
	private final Types types;
	private final Elements elements;
	private final XMLService xmlService;
}
