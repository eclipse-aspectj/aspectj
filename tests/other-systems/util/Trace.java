import java.io.Serializable;

abstract aspect Trace {
    abstract pointcut targets();

    /*
     * toString() can throw exceptions, so we'll print
     * the java.lang.Class instead.
     */

    before (): targets() {
	System.out.println("entering " + thisJoinPoint);
    }
    after (): targets() {
	System.out.println("exiting "  +
			   thisJoinPointStaticPart);
    }

    after () throwing (Throwable t): targets() {
	System.out.println("throwing "  + t);
    }

    after () throwing (java.io.IOException ioe): targets() {
	System.out.println("throwing "  + ioe);
    }

    after () returning (Object o): targets() {
	System.out.println("returning "  + (o!=null ? o.getClass() : null));
    }


    private static int initCounter() {
	return 0;
    }

    //private int Serializable.counter = initCounter();
}
