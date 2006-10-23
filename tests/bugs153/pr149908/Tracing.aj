public aspect Tracing {

	private int _callDepth = -1;

    pointcut tracePoints() : !within(Tracing);

    before() : tracePoints() {
            _callDepth++; print("Before", thisJoinPoint);
    }

    after() : tracePoints() {
            print("After", thisJoinPoint);
            _callDepth--;
    }

    private void print(String prefix, Object message) {
            for(int i = 0, spaces = _callDepth * 2; i < spaces; i++) {
                    //MyPrint.print(" ","");
            }

            System.out.println(prefix + ": " + message);
    }

	
}
