aspect MyTrace extends Trace {
	public pointcut traced(Object foo): this(foo) && execution(* doit(..));
	
	after(Object o) returning (Object ret): traced(o) {
		System.out.println("exit: " /*+ thisJoinPoint.getSignature()*/ + " with " + ret);
	}
}