package ras;

public abstract aspect FFDC {

	protected abstract pointcut ffdcScope ();
	
	before() : ffdcScope() {
		
	}
	
}
