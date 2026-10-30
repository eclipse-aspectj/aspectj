public aspect ConcreteC extends AbstractAudit {
	protected pointcut audited(): execution(* Target.runC(..));
}
