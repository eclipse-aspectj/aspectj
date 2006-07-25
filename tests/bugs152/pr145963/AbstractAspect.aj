package pkg;

public abstract aspect AbstractAspect {

	public abstract pointcut abstractPCD();
	
	before() : abstractPCD() {
	}
	
}
