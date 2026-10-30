public aspect ConcreteB extends AbstractAudit {
	protected pointcut audited(): execution(* Target.runB(..));
}
