package strategies.process;

import annotations.SchemaAttribute;

public class Worker {
	@SchemaAttribute
	private String name;
	private int age;
	@SchemaAttribute
	private WorkerType workerType;
}
