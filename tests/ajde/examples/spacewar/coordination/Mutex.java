
package coordination;

import java.lang.String;


class Mutex implements Exclusion {
    String[] methodNames;
    MethodState[] methodStates;

    String prettyName;
  
    Mutex (String[] _methodNames) {
	methodNames = _methodNames;
	methodStates = new MethodState[methodNames.length];
	for (int i = 0; i < methodNames.length; i++) {
	    methodStates[i] = new MethodState();
	}
    }

    private boolean isMethodIn (String _methodName) {
	for (int i = 0; i < methodNames.length; i++) {
	    if (_methodName.equals(methodNames[i])) 
		return(true);
	}
	return(false);
    }

    private MethodState getMethodState (String _methodName) {
	for (int i = 0; i < methodNames.length; i++) {
	    if (_methodName.equals(methodNames[i])) 
		return(methodStates[i]);
	}
	return(null);
    }

    public boolean testExclusion (String _methodName) {
	Thread ct = Thread.currentThread();
	//
	// Loop through each of the other methods in this exclusion set, to be sure
	// that no other thread is running them.  Note that we have to be careful
	// about selfex.
	// 
	for (int i = 0; i < methodNames.length; i++) {
	    if (!_methodName.equals(methodNames[i])) {
		if (methodStates[i].hasOtherThreadThan(ct))
		    return(false);
	    }
	}
	return (true);
    }

    public void enterExclusion (String _methodName) {
	MethodState methodState = getMethodState(_methodName);
	methodState.enterInThread(Thread.currentThread());
    }

    public void exitExclusion (String _methodName) {
	MethodState methodState = getMethodState(_methodName);
	methodState.exitInThread(Thread.currentThread());
    }

    public void printNames() {
	System.out.print("Mutex names: ");
	for (int i = 0; i < methodNames.length; i++)
	    System.out.print(methodNames[i] + " ");
	System.out.println();
    }
}
