public aspect ConcreteA extends AbstractAudit {
	protected pointcut audited(): execution(* Target.runA(..));
}
