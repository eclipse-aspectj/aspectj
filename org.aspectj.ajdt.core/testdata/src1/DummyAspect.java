public abstract aspect DummyAspect {
	public abstract pointcut traced(Object o);
	
	before (Object exc): traced(exc) {
	}
}
