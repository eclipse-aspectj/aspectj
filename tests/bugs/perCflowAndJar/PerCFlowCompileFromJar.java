public abstract aspect PerCFlowCompileFromJar percflow( topOfFlow() ){
	
	private boolean thisAspectInstanceIsDead = false; 

	protected abstract pointcut entryPoint();
	protected pointcut topOfFlow(): entryPoint() && !cflowbelow( entryPoint() );

	after() : topOfFlow() {
		this.killThisAspectInstance();
	}

	protected void killThisAspectInstance(){
		if (thisAspectInstanceIsDead)
			throw new IllegalStateException("This aspect instance has been used and can't be used again.");
		else
			thisAspectInstanceIsDead = true;
	}	
}


