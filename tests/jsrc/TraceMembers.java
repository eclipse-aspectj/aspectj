aspect TraceMembers extends Trace of eachJVM() {
    pointcut targets(): 
	(within(java..*) || within(javax..*) || within(org.aspectj..*)) &&
        executions(!abstract !native * *(..));
}
