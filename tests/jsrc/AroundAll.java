aspect AroundAll {
    pointcut targets(Object o) returns Object:
	executions(!abstract !native * *(..)) && instanceof(o) && (within(java..*) || within(javax..*) || within(org.aspectj..*));

    around(Object thisObj) returns Object: targets(thisObj) {
	    if (true) {
		throw new RuntimeException("not meant to run");
	    } 
	    return proceed(thisObj);
    }
}
