public aspect ConcreteAspectWithITD extends AbstractSuperAspectWithInterface {
	protected pointcut scope () :
		!within(AbstractSuperAspectWithInterface+);
}