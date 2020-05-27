package pl.dabrowski.XMLib.strategies.process;

import pl.dabrowski.XMLib.annotations.SchemaAttribute;

public class Worker {
	@SchemaAttribute
	private String name;
	private int age;
	@SchemaAttribute
	private WorkerType workerType;
	
	public int getAge() {
		return age;
	}
}
