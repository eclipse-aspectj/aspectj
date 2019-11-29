/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Andy Clement - initial implementation
 *******************************************************************************/
package org.aspectj.systemtest.ajc152;

import java.net.URL;

import org.aspectj.testing.XMLBasedAjcTestCase;

import junit.framework.Test;

/**
 * Work items, phase #1: lock()/unlock() x expose new joinpoints x parse new pcds x fix tjp string x preventing double unlock()
 * messages/markers in structure model x error messages appropriate for attempting to use around advice on synchronization join
 * points x making the use of lock/unlock conditional on an -Xjoinpoints:synchronization x activating the -Xjoinpoints options from
 * LTW configurations rather than through batch/AJDT x ensure the lock/unlock joinpoints only appear when
 * -Xjoinpoints:synchronization specified TAG: Completion of PHASE1
 * 
 * 
 * Work items, phase #2: transformation
 * 
 * Design: transform all synchronized methods: public synchronized void m() { ... } => public void m() { synchronized (this) { ... }
 * }
 * 
 * x transforming synchronized methods x matching execution(synchronized * *(..)) for transformed code x warning message for
 * execution() hitting a synchronized method x ensure verifier runs over generated code (done by just executing the code as part of
 * the test spec) - Ant task support for -Xjoinpoints TAG: Completion of PHASE2
 * 
 * 
 * TAG: Finished
 * 
 * Future work items: - optimize matching for transformed methods since we *know* the type we are locking on - supporting type
 * pattern in lock() unlock() - this is not entirely trivial as kinded pointcuts do not usually have any residue - weaving messages
 * include 'unusual' strings for the join points, not the same as revealed by thisJoinPoint.getSignature() in code - handler is
 * probably similar - documentation - lazy translation of synchronized methods, rather than eager - applying execution(* *(..))
 * correctly to transformed methods (i.e. inside lock/unlock) - use knowledge of type containing synchronized methods to optimize
 * matching of (un)lock() - not always needing residue - line number table is incorrect for transformed code (lock joinpoint has no
 * line number)
 * 
 * Notes: IllegalMonitorStateException Thrown to indicate that a thread has attempted to wait on an object's monitor or to notify
 * other threads waiting on an object's monitor without owning the specified monitor.
 * 
 * around advice won't work on SUN VMs (may be a bug that it does work on other VMs) since the monitor instructions are extracted to
 * a separate method for proceed() calls and yet the VM seems to want them paired inside a single method.
 * 
 * Really we only need to restrict the use of around advice on synchronization join points if the advice uses proceed() - but
 * policing that is a little tough because (DOH) the AdviceAttribute field 'proceedCallSignatures' is never filled in (which records
 * how many proceeds occur in the advice) - see where it isnt filled in at AdviceDeclaration.resolveStatements() in the loop that
 * goes over the proceedCalls list.
 * 
 * 
 * Problems: - Can't run it on a 1.2.1 runtime - just not practical
 * 
 * 
 * Method transformation, example:
 * 
 * public synchronized void m(); Code: Stack=2, Locals=1, Args_size=1 0: getstatic #2; //Field
 * java/lang/System.err:Ljava/io/PrintStream; 3: ldc #3; //String hello 5: invokevirtual #4; //Method
 * java/io/PrintStream.println:(Ljava/lang/String;)V 8: getstatic #2; //Field java/lang/System.err:Ljava/io/PrintStream; 11: ldc #5;
 * //String world 13: invokevirtual #4; //Method java/io/PrintStream.println:(Ljava/lang/String;)V 16: return LineNumberTable: line
 * 4: 0 line 5: 8 line 6: 16
 * 
 * public void m2(); Code: Stack=2, Locals=3, Args_size=1 0: aload_0 1: dup 2: astore_1 3: monitorenter 4: getstatic #2; //Field
 * java/lang/System.err:Ljava/io/PrintStream; 7: ldc #3; //String hello 9: invokevirtual #4; //Method
 * java/io/PrintStream.println:(Ljava/lang/String;)V 12: getstatic #2; //Field java/lang/System.err:Ljava/io/PrintStream; 15: ldc
 * #5; //String world 17: invokevirtual #4; //Method java/io/PrintStream.println:(Ljava/lang/String;)V 20: aload_1 21: monitorexit
 * 22: goto 30 25: astore_2 26: aload_1 27: monitorexit 28: aload_2 29: athrow 30: return Exception table: from to target type 4 22
 * 25 any 25 28 25 any
 * 
 * Factors affecting transformation: - LDC in Java5 supports referring to a class literal, e.g. Foo.class whereas before Java5, it
 * did not. This means if generating the synchronized() block for a static method from a preJava5 class then we have to generate a
 * lot of crap to build the class object for locking and unlocking. The object is also stored in a local field of the type (if we
 * follow the pattern of JDT/JAVAC)
 */

public class SynchronizationTests extends XMLBasedAjcTestCase {

	// testing the new join points for monitorenter/monitorexit
	public void testTheBasics_1() {
		runTest("basic");
	}

	public void testTheBasics_2() {
		runTest("basic - within");
	}

	public void testTheBasics_3() {
		runTest("basic - within plus args");
	}

	public void testTheBasics_4() {
		runTest("basic - within plus this");
	} // this null in static context

	public void testTheBasics_5() {
		runTest("basic - within plus target");
	} // target null in static context?

	// testing parsing of the new PCDs lock/unlock
	public void testParsing_1() {
		runTest("parsing - lock");
	}

	public void testParsing_2() {
		runTest("parsing - unlock");
	}

	public void testParsing_errors_1() {
		runTest("parsing - error - lock");
	}

	public void testParsing_errors_2() {
		runTest("parsing - error - unlock");
	}

	// testing parsing and matching with the new PCDs
	public void testParsingAndMatching_1() {
		runTest("parsing and matching - lock and static context");
	}

	public void testParsingAndMatching_2() {
		runTest("parsing and matching - unlock and static context");
	}

	public void testParsingAndMatching_3() {
		runTest("parsing and matching - lock and non-static context");
	}

	public void testParsingAndMatching_4() {
		runTest("parsing and matching - unlock and non-static context");
	}

	public void testParsingAndMatching_5() {
		runTest("parsing and matching - lock and non-static context");
	}

	public void testParsingAndMatching_6() {
		runTest("parsing and matching - unlock and non-static context");
	}

	// using the new PCDs in a LTW environment
	public void testUsingWithLTW_MissingFlag_1() {
		runTest("using lock with LTW - missing flag");
	}

	public void testUsingWithLTW_MissingFlag_2() {
		runTest("using unlock with LTW - missing flag");
	}

	public void testUsingWithLTW_1() {
		runTest("using lock with LTW");
	}

	public void testUsingWithLTW_2() {
		runTest("using unlock with LTW");
	}

	// multiple PCDs
	public void testCombiningPCDs_1() {
		runTest("combining pcds - lock and this");
	}

	public void testCombiningPCDs_2() {
		runTest("combining pcds - unlock and this");
	}

	// useful examples
	public void testUseful_1() {
		runTest("a useful program");
	} // just uses within/args - matching the (un)lock jps

	public void testUseful_2() {
		runTest("a useful program - with lock");
	} // uses lock/args

	// all the methods of thisJoinPoint
	public void testThisJoinPoint_1() {
		runTest("thisjoinpoint - monitor entry");
	}

	public void testThisJoinPoint_2() {
		runTest("thisjoinpoint - monitor exit");
	}

	public void testDoubleMessagesOnUnlock() {
		// AsmManager.setReporting("c:/foo.txt",true,true,true,true);
		runTest("prevent double unlock weaving messages and model contents");
		// checkModel1();
	}

	// targetting 1.2 runtime - signature creation code in LazyClassGen.initializeTjp may not work

	// different advice kinds
	public void testBeforeAdvice_1() {
		runTest("before advice - lock");
	}

	public void testBeforeAdvice_2() {
		runTest("before advice - unlock");
	}

	public void testAfterAdvice_1() {
		runTest("after advice - lock");
	}

	public void testAfterAdvice_2() {
		runTest("after advice - unlock");
	}

	public void testAroundAdvice_1() {
		runTest("around advice - lock");
	}

	public void testAroundAdvice_2() {
		runTest("around advice - unlock");
	}

	public void testLockingTJP() {
		runTest("obtaining locked object through getArgs");
	}

	// binary weaving?

	// nested locking/unlocking

	// --- helpers

	// Half finished - could check there is only one relationship for unlock() rather than two - but
	// that seems to be the case anyway (peculiar...)
	// private void checkModel1() {
	// // Verifies only one unlock relationship, not two
	// IProgramElement unlockNode =
	// AsmManager.getDefault().getHierarchy().findElementForLabel(AsmManager.getDefault().getHierarchy().getRoot(),
	// IProgramElement.Kind.CODE,"unlock(void java.lang.Object.<unlock>(java.lang.Object))");
	// assertTrue("Couldn't find the unlock node",unlockNode!=null);
	// List l = AsmManager.getDefault().getRelationshipMap().get(unlockNode);
	// assertTrue("should be one entry :"+l,l!=null && l.size()==1);
	// IRelationship ir = (IRelationship)l.get(0);
	// System.err.println(ir);
	// List targs = ir.getTargets();
	// System.err.println(targs.size());
	// System.err.println(targs.get(0));
	// }

	// ---
	public static Test suite() {
		return XMLBasedAjcTestCase.loadSuite(SynchronizationTests.class);
	}

	protected URL getSpecFile() {
		return getClassResource("synchronization.xml");
	}

}
