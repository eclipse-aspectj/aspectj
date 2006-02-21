

public abstract aspect World {
	public abstract pointcut monitoredOperation();
	
	after() : monitoredOperation() {
		System.out.println("World");
	}
}
