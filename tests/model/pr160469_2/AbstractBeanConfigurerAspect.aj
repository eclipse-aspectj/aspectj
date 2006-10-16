package pkg;

public abstract aspect AbstractBeanConfigurerAspect {
	
	protected abstract pointcut beanCreation();

	declare warning : beanCreation() : "warning";
	
}
