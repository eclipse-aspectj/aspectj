package pkg;

public abstract aspect AbstractBeanConfigurerAspect {

	// advice starts on line 6
	after() returning : beanCreation() {
	}
	
	protected abstract pointcut beanCreation();

	before() : beanCreation() {
		
	}	
}
