aspect TraceCalls extends Trace of eachJVM() {
    pointcut targets(): 
	(within(java..*) || within(javax..*) || within(org.aspectj..*)) &&
	(calls(* *(..)) || calls(new(..)));
}
