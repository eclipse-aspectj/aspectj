public aspect ConcreteAspect extends AbstractAspect {
	pointcut scope(): call(* foo(..));
}