/* -*- Mode: Java; -*-

Copyright (c) Xerox Corporation 1998-2002.  All rights reserved.

Use and copying of this software and preparation of derivative works based
upon this software are permitted.  Any distribution of this software or
derivative works must comply with all applicable United States export control
laws.
 
This software is made available AS IS, and Xerox Corporation makes no warranty
about the software, its performance or its conformity to any specification.

|<---            this code is formatted to fit into 80 columns             --->|
|<---            this code is formatted to fit into 80 columns             --->|
|<---            this code is formatted to fit into 80 columns             --->|

*/

package coordination;

import java.util.*;  //!!!

/**
 * The Coordinator class provides the basic functionality for synchronizing
 * and coordinating different threads upon entering and exiting methods.
 * It can be used in two different ways:
 * 1) by instantiating regular coordinator objects that are used by aspects; or
 * 2) by extending it (sub-classing) with coordinator aspects.
 * <P>
 * Method invocations are the smallest units for defining critical sections 
 * and pre-conditions. The use of coordinators, either regular objects or aspect
 * instances, should always end up by invoking guardedEntry(...) in a
 * before weave and guardedExit(...) in an after weave for all methods that 
 * need coordination. guardedEntry and guardedExit are the methods that
 * actually manage the synchronization and coordination constraints given
 * by their parameters and by pre-existent exclusion markers.
 * <P>
 * The synchronization of threads for the execution of critical section 
 * methods in an object is done by marking those methods as self- and/or 
 * mutually-exclusive (addSelfex, addMutex).
 * Just by itself,  addSelfex("M") does not enforce the self-exclusion
 * of method M - enforcement is done by invoking guardedEntry before
 * M is executed. Similarly, addMutex(new String[] {"M1", "M2"}) does 
 * not enforce the mutual exclusion between methods M1 and M2.
 * <P>
 * A guardedEntry on a method that has been marked as self-exclusive 
 * ensures that the method is executed in the invoked object by only one thread 
 * at a time. A guardedEntry on a method that has been marked has mutually-
 * exclusive with other methods ensures that the execution of that method
 * by a thread in the invoked object temporarily blocks the execution by
 * other threads of the methods that are in the same mutex set.
 * <P>
 * The coordination of threads, i.e. their explicit suspension and 
 * resumption, is done through the use of pre-conditions and coordination 
 * actions that are passed as parameters to guardedEntry and guardedExit
 * with the form of anonymous classes.
 */
public abstract aspect Coordinator {
    private Hashtable methods = null;
    private Vector exclusions = null;
  
    abstract protected pointcut synchronizationPoint();

    public Coordinator() {
	methods = new Hashtable();
	exclusions = new Vector(5);
    }

    before (): synchronizationPoint() {
	this.guardedEntry(thisJoinPointStaticPart.getSignature().getName());
    }
  
    after (): synchronizationPoint() {
	this.guardedExit(thisJoinPointStaticPart.getSignature().getName());
    }

    /**
     * Takes a multi-part method name (eg "BoundedBuffer.put")
     * and marks that method as self-exclusive.
     * No checks are made with respect to the existence of the method
     * whose name is given.
     */
    public synchronized void addSelfex(String methName) {
	Selfex sex = new Selfex (methName);

	// update db of all exclusions in this coordinator
	exclusions.addElement(sex);

	// update local info in method
	Method aMeth = getOrSetMethod(methName);
	aMeth.addExclusion(sex);
    }

    /**
     * Takes a multi-part method name (e.g. "BoundedBuffer.put")
     * and removes that method from the list of self-exclusive methods.
     */
    public synchronized void removeSelfex(String methName) {
	for (int i = 0; i < exclusions.size(); i++) {
	    Exclusion sex = (Exclusion)exclusions.elementAt(i);
	    if ((sex instanceof Selfex) &&
		(((Selfex)sex).methodName.equals(methName))) {

		// update db of all exclusions in this coordinator 
		exclusions.removeElementAt(i);

		// update local info in method
		Method aMeth = getOrSetMethod(methName);
		aMeth.removeExclusion(sex);
	    }
	}
    }

    /**
     * Takes an array of multi-part method names and marks those
     * methods as mutually exclusive.
     * No checks are made with respect to the existence of the methods
     * whose names are given.
     */
    public synchronized void addMutex(String[] methNames) {
	Mutex mux = new Mutex(methNames);

	// update db of all exclusions in this coordinator
	exclusions.addElement(mux);

	// update local info in each method
	for (int i = 0; i < methNames.length; i++) {
	    Method aMeth = getOrSetMethod(methNames[i]);
	    aMeth.addExclusion(mux);
	}
    }

    /**
     * Takes an array of multi-part method names that correspond
     * to an existing mutex set and remove the mutual exclusion constraint.
     * If the given mutex set does not exist, removeMutex does nothing.
     */
    public synchronized void removeMutex(String[] methNames) {
	for (int i = 0; i < exclusions.size(); i++) {
	    Exclusion mux = (Exclusion)exclusions.elementAt(i);
	    if (mux instanceof Mutex) {
		boolean same = true;
		for (int j = 0; j < methNames.length; j++)
		    if (!methNames[j].equals(((Mutex)mux).methodNames[j]))
			same = false;
		if (same) {
		    // update db of all exclusions in this coordinator
		    exclusions.removeElementAt(i);

		    // update local info in each method involved
		    for (int j = 0; j < methNames.length; j++) {
			Method aMeth = getOrSetMethod(methNames[j]);
			aMeth.removeExclusion(mux);
		    }
		}
	    }
	}
    }

    /**
     * This method is the guard for enforcing all synchronization and
     * coordination constraints of a given method, and it should be called
     * just before the method is executed.
     * In this form, only the method name is given. The only constraints
     * checked are the exclusion constraints.
     * If the method was previousely marked as selfex (through addSelfex),
     * guardedEntry ensures that the method is executed only when no other 
     * thread is executing it.
     * If the method was previousely marked as being in one or more mutex
     * sets, guardedEntry ensures that the method is executed only when no other 
     * thread is executing any of the methods with which the give method is
     * mutexed.
     */
    public synchronized void guardedEntry(String methName) {
	guardedEntry(methName,  new Condition() {
		public boolean checkit() {
		    return true;
		}
	    }, null);
    }

    /**
     * Just like guardedEntry(String methName), but the given method is executed
     * only when the given condition is true.
     * guardedEntry is the guard for enforcing all synchronization and
     * coordination constraints of a given method, and it should be called
     * just before the method is executed.
     * In this form, the method name is given along with a condition. 
     * The constraints checked are the exclusion constraints and whether
     * the given condition is true.
     * If the method was previousely marked as selfex (through addSelfex),
     * guardedEntry ensures that the method is executed only when no other 
     * thread is executing it.
     * If the method was previousely marked as being in one or more mutex
     * sets, guardedEntry ensures that the method is executed only when no other 
     * thread is executing any of the methods with which the give method is
     * mutexed.
     * If the condition is false, guardedEntry suspends the current thread.
     * That thread remains suspended until the condition becomes true, in
     * which case all constraints are rechecked before the method is executed.
     * When all exclusion constraints are checked and the given condition is
     * true, the given method is executed.
     */
    public synchronized void guardedEntry(String methName, Condition condition) {
	guardedEntry(methName, condition, null);
    }

    /**
     * Just like guardedEntry(String methName), but with an additional
     * coordination action that is executed before the given method is
     * executed.
     * guardedEntry is the guard for enforcing all synchronization and
     * coordination constraints of a given method, and it should be called
     * just before the method is executed.
     * In this form, the method name is given along with a coordination action. 
     * The only constraints checked are the exclusion constraints.
     * If the method was previousely marked as selfex (through addSelfex),
     * guardedEntry ensures that the method is executed only when no other 
     * thread is executing it.
     * If the method was previousely marked as being in one or more mutex
     * sets, guardedEntry ensures that the method is executed only when no other 
     * thread is executing any of the methods with which the give method is
     * mutexed.
     * The given coordination action is executed just before the given method 
     * is executed.
     */
    public synchronized void guardedEntry(String methName, 
					  CoordinationAction action) {
	guardedEntry(methName, new Condition() {
		public boolean checkit() {
		    return true;
		}
	    }, 
		     action);
    }

    /**
     * Just like guardedEntry(String methName), but the given method is executed
     * only when the given condition is true; the additional
     * coordination action that is executed before the given method is
     * executed.
     * guardedEntry is the guard for enforcing all synchronization and
     * coordination constraints of a given method, and it should be called
     * just before the method is executed.
     * In this form, the method name is given along with a condition and
     * a coordination action. 
     * The constraints checked are the exclusion constraints and whether the
     * given condition is true.
     * If the method was previousely marked as selfex (through addSelfex),
     * guardedEntry ensures that the method is executed only when no other 
     * thread is executing it.
     * If the method was previousely marked as being in one or more mutex
     * sets, guardedEntry ensures that the method is executed only when no other 
     * thread is executing any of the methods with which the give method is
     * mutexed.
     * If the condition is false, guardedEntry suspends the current thread.
     * That thread remains suspended until the condition becomes true, in
     * which case all constraints are rechecked before the method is executed.
     * When all exclusion constraints are checked and the given condition is
     * true, the given method is executed.
     * The given coordination action is executed just before the given method 
     * is executed.
     */
    public synchronized void guardedEntry(String methName, 
					  Condition condition,
					  CoordinationAction action) {
	Method aMeth = getOrSetMethod(methName);
	boolean canGo = false;

	// test pre-conditions for entering the method
	while (!canGo) {
	    canGo = true;
	    for (int i = 0; i < aMeth.exes.size() && canGo; i++)
		if (!((Exclusion)aMeth.exes.elementAt(i)).testExclusion(aMeth.name)) {
		    canGo = false;
		}
	    if (canGo && !condition.checkit()) {
		canGo = false;
	    }
	    if (!canGo)
		try {
		    wait();
		} catch (InterruptedException e) { }
	}

	// OK.
	enterMethod(aMeth, action);
    }

    /**
     * This method is similar to guardedEntry, but it takes
     * an additional parameter - the milliseconds after which any suspension
     * will abort with a timeout.
     */
    public synchronized void guardedEntryWithTimeout(String methName,
						     long millis) 
				 throws TimeoutException {
	guardedEntryWithTimeout(methName, new Condition() {
		public boolean checkit() {
		    return true;
		}
	    }, null, millis);
    }

    /**
     * This method is similar to guardedEntry, but it takes
     * an additional parameter - the milliseconds after which any suspension
     * will abort with a timeout.
     */
    public synchronized void guardedEntryWithTimeout(String methName, 
						     Condition condition,
						     long millis) 
				 throws TimeoutException {
	guardedEntryWithTimeout(methName, condition, null, millis);
    }

    /**
     * This method is similar to guardedEntry, but it takes
     * an additional parameter - the milliseconds after which any suspension
     * will abort with a timeout.
     */
    public synchronized void guardedEntryWithTimeout(String methName, 
						     CoordinationAction action,
						     long millis) 
				 throws TimeoutException {
	guardedEntryWithTimeout(methName, new Condition() {
		public boolean checkit() {
		    return true;
		}
	    }, action, millis);
    }

    /**
     * This method is similar to guardedEntry, but it takes
     * an additional parameter - the milliseconds after which any suspension
     * will abort with a timeout.
     */
    public synchronized void guardedEntryWithTimeout(String methName, 
						     Condition condition,
						     CoordinationAction action,
						     long millis) 
				 throws TimeoutException {

	Method aMeth = getOrSetMethod(methName);
	boolean canGo = false;
	long waitTime = millis;
	long startTime = System.currentTimeMillis();

	// test pre-conditions for entering the method
	while (!canGo) {
	    canGo = true;
	    for (int i = 0; i < aMeth.exes.size() && canGo; i++)
		if ((!((Exclusion)aMeth.exes.elementAt(i)).testExclusion(aMeth.name)) ||
		    (!condition.checkit())) {
		    canGo = false;
		}
	    if (!canGo) {
		try {
		    wait(waitTime);
		} catch (InterruptedException e) {}

		long now = System.currentTimeMillis();
		long timeSoFar = now - startTime;
		if (timeSoFar >= millis) // timeout!
		    throw new TimeoutException(timeSoFar);
		else // adjust time
		    waitTime = millis - timeSoFar;
	    }
	}

	// OK.
	enterMethod(aMeth, action);
    }

    /**
     * This method provides the means for updating all synchronization and
     * coordination state after the execution of a given method, and it should be 
     * called after the method is executed.
     * In this form, only the method name is given.
     * The synchronization state for self- and mutual-exclusion is 
     * automatically upadted.
     */
    public synchronized void guardedExit(String methName) {
	guardedExit(methName, null);
    }

    /**
     * Just like guardedExit(String methName) but with an additional
     * coordination action that is executed.
     * guardedExit provides the means for updating all synchronization and
     * coordination state after the execution of a given method, and it should be 
     * called after the method is executed.
     * In this form, the method name is given along with a coordination action.
     * The synchronization state for self- and mutual-exclusion is 
     * automatically upadted.
     * The given coordination action is executed.
     */
    public synchronized void guardedExit(String methName, 
					 CoordinationAction action) {
	Method aMeth = getOrSetMethod(methName);

	for (int i = 0; i < aMeth.exes.size(); i++)
	    ((Exclusion)aMeth.exes.elementAt(i)).exitExclusion(methName);
	if (action != null) action.doit();
	notifyAll();
    }

    private Method getOrSetMethod(String methName) {
	Method aMeth = null;
	if (!methods.containsKey(methName)) {
	    methods.put(methName, (aMeth = new Method(methName)));
	}
	else {
	    aMeth = (Method) methods.get(methName);
	}
	return aMeth;
    }

    private void enterMethod(Method aMeth, CoordinationAction action) {
	for (int i = 0; i < aMeth.exes.size(); i++)
	    ((Exclusion)aMeth.exes.elementAt(i)).enterExclusion(aMeth.name);

	if (action != null) action.doit();
    }



}

class Method {
  String name;
  Vector exes = new Vector(3);

  Method(String n) {
    name = n;
  }

  void addExclusion(Exclusion ex) {
    exes.addElement(ex);
  }

  void removeExclusion(Exclusion ex) {
    for (int i = 0; i < exes.size(); i++) {
      if (exes.elementAt(i) == ex)
	exes.removeElementAt(i);
    }
  }
}

