
package coordination;


import java.lang.String;

class Selfex implements Exclusion {
    String methodName;
    Thread thread;
    int count = 0;

    Selfex (String _methodName) {
	methodName = _methodName;
    }

    public boolean testExclusion (String _methodName) {
	if (count == 0)
	    return(true);
	return (thread == Thread.currentThread());
    }

    public void enterExclusion (String _methodName) {
	count++;
	thread = Thread.currentThread();    // note that if count wasn't 0
	// we aren't changing thread
    }

    public void exitExclusion (String _methodName) {
	count--;
	if (count == 0)                   // not stricly necessary, but...
	    thread = null;
    }

    public void printNames() {
	System.out.println("Selfex name: " + methodName);
    }

}
