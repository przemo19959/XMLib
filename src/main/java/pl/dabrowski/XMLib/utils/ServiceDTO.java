package pl.dabrowski.XMLib.utils;

import javax.annotation.processing.Messager;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic.Kind;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import pl.dabrowski.XMLib.services.XMLService;

@RequiredArgsConstructor
@Getter
public class ServiceDTO {
	private final Messager messager;
	private final Types types;
	private final Elements elements;
	private final XMLService xmlService;
	
	public void log(String msg) {
		messager.printMessage(Kind.NOTE, msg);
	}
}
