aspect AroundAll {
    pointcut targets(Object o):
	execution(!abstract !native * *(..)) && this(o) && Pcds.withinMe();

    Object around(Object thisObj): targets(thisObj) {
	    if (true) {
		throw new RuntimeException("not meant to run");
	    } 
	    return proceed(thisObj);
    }
}
