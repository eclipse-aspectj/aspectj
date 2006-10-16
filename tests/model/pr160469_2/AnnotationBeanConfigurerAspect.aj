package pkg;

public aspect AnnotationBeanConfigurerAspect extends AbstractBeanConfigurerAspect {
	
	protected pointcut beanCreation() : initialization(*.new(..)) && !within(pkg.*);
}
