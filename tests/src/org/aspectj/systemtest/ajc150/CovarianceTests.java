/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Andy Clement - initial implementation
 *******************************************************************************/
package org.aspectj.systemtest.ajc150;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import junit.framework.Test;

import org.aspectj.bridge.IMessage;
import org.aspectj.testing.XMLBasedAjcTestCase;
import org.aspectj.tools.ajc.AjcTestCase;
import org.aspectj.tools.ajc.CompilationResult;

/*

class Car {}

class FastCar extends Car {}

class Super {
  Car getCar() {
    return new Car();
  }
}

class Sub extends Super {
  FastCar getCar() {
    return new FastCar();
  }
}

public class CovBaseProgram01 {
  public static void main(String[] argv) {
    new CovBaseProgram01().run();
  }

  public void run() {
    Super instance_super = new Super();
    Sub   instance_sub   = new Sub();

    Car c1 = instance_super.getCar(); // Line 26
    Car c2 = instance_sub.getCar(); // Line 27
  }
}

// Line26: callJPs: call(Car Super.getCar())
// Line27: callJPs: call(FastCar Sub.getCar()) call(Car Super.getCar())

 */

/**
 * Covariance is simply where a type overrides some inherited implementation and narrows the return type.
 */
public class CovarianceTests extends AjcTestCase {

	private boolean verbose = false;

	
	/**
	 * call(* getCar()) should match both
	 */
	public void testCOV001() {
		CompilationResult cR = binaryWeave("CovBaseProgram01.jar","CovAspect01.aj",0,0);
		verifyOutput(cR,new String[]{
				"weaveinfo Type 'CovBaseProgram01' (CovBaseProgram01.java:26) advised by before advice from 'CovAspect01' (CovAspect01.aj:5)",
				"weaveinfo Type 'CovBaseProgram01' (CovBaseProgram01.java:27) advised by before advice from 'CovAspect01' (CovAspect01.aj:5)"
		});
	}

	
	/**
	 * call(* Super.getCar()) should match both
	 * 
	 * This test required a change to the compiler.  When we are looking at signatures and comparing them we walk up
	 * the hierarchy looking for supertypes that declare the same method.  The problem is that in the comparison for
	 * whether to methods are compatible we were including the return type - this meant 'Car getCar()' on Super was
	 * different to 'FastCar getCar()' on Sub - it thought they were entirely different methods.  In fact the return
	 * type is irrelevant here, we just want to make sure the names and the parameter types are the same - so I
	 * added a parameterSignature to the Member class that looks like '()' where the full signature looks like
	 * '()LFastCar;' (which includes the return type).  If the full signature comparison fails then it looks at the
	 * parameter signature - I did it that way to try and preserve some performance.  I haven't changed the
	 * definition of 'signature' for a member as trimming the return type off it seems rather serious !
	 * 
	 * What might break:
	 * - 'matches' can now return true for things that have different return types - I guess whether this is a problem
	 *   depends on what the caller of matches is expecting, their code will have been written before covariance was
	 *   a possibility.  All the tests pass so I'll leave it like this for now.
	 */
	public void testCOV002() {
		CompilationResult cR = binaryWeave("CovBaseProgram01.jar","CovAspect02.aj",0,0);
		verifyOutput(cR,new String[]{
				"weaveinfo Type 'CovBaseProgram01' (CovBaseProgram01.java:26) advised by before advice from 'CovAspect02' (CovAspect02.aj:5)",
				"weaveinfo Type 'CovBaseProgram01' (CovBaseProgram01.java:27) advised by before advice from 'CovAspect02' (CovAspect02.aj:5)"
		});
	}
	
	/**
	 * call(Car getCar()) should match both
	 * 
	 * Had to implement proper covariance support here...
	 */
	public void testCOV003() {
		CompilationResult cR = binaryWeave("CovBaseProgram01.jar","CovAspect03.aj",0,0);
		
		verifyOutput(cR,new String[]{
				"weaveinfo Type 'CovBaseProgram01' (CovBaseProgram01.java:26) advised by before advice from 'CovAspect03' (CovAspect03.aj:5)",
				"weaveinfo Type 'CovBaseProgram01' (CovBaseProgram01.java:27) advised by before advice from 'CovAspect03' (CovAspect03.aj:5)"
		});
	}
	
	/**
	 * *** Different base program, where Sub does not extend Super.
	 * call(Car Super.getCar()) should only match first call to getCar()
	 */
	public void testCOV004() {
		CompilationResult cR = binaryWeave("CovBaseProgram02.jar","CovAspect04.aj",0,0);
		verifyOutput(cR,new String[]{
				"weaveinfo Type 'CovBaseProgram02' (CovBaseProgram02.java:30) advised by before advice from 'CovAspect04' (CovAspect04.aj:5)"
		});
	}	

	/**
	 * *** Original base program
	 * call(Car Super.getCar()) should match both
	 */
	public void testCOV005() {
		CompilationResult cR = binaryWeave("CovBaseProgram01.jar","CovAspect05.aj",0,0);
		verifyOutput(cR,new String[]{
				"weaveinfo Type 'CovBaseProgram01' (CovBaseProgram01.java:26) advised by before advice from 'CovAspect05' (CovAspect05.aj:5)",
				"weaveinfo Type 'CovBaseProgram01' (CovBaseProgram01.java:27) advised by before advice from 'CovAspect05' (CovAspect05.aj:5)"
		});
	}	

	/**
	 * call(Car Sub.getCar()) should not match anything
	 */
	public void testCOV006() {
		CompilationResult cR = binaryWeave("CovBaseProgram01.jar","CovAspect06.aj",0,1);
		verifyOutput(cR,new String[]{/* no expected output! */});
		assertTrue("Expected one xlint warning message for line 26, but got: "+cR.getWarningMessages(),
				cR.getWarningMessages().size()==1 && ((IMessage)cR.getWarningMessages().get(0)).toString().indexOf("26")!=-1);

	}	

	/**
	 * call(Car+ Sub.getCar()) should match 2nd call with xlint for the 1st call
	 */
	public void testCOV007() {
		CompilationResult cR = binaryWeave("CovBaseProgram01.jar","CovAspect07.aj",0,1);
		verifyOutput(cR,new String[]{
				"weaveinfo Type 'CovBaseProgram01' (CovBaseProgram01.java:27) advised by before advice from 'CovAspect07' (CovAspect07.aj:5)"
		});
		assertTrue("Expected one xlint warning message for line 26, but got: "+cR.getWarningMessages(),
				cR.getWarningMessages().size()==1 && ((IMessage)cR.getWarningMessages().get(0)).toString().indexOf("26")!=-1);
	}	

	/**
	 * *** aspect now contains two pointcuts and two pieces of advice
	 * call(FastCar getCar()) matches on 2nd call
	 * call(FastCar Sub.getCar()) matches on 2nd call
	 */
	public void testCOV008() {
		CompilationResult cR = binaryWeave("CovBaseProgram01.jar","CovAspect08.aj",0,0);
		verifyOutput(cR,new String[]{
				"weaveinfo Type 'CovBaseProgram01' (CovBaseProgram01.java:27) advised by before advice from 'CovAspect08' (CovAspect08.aj:11)",
                "weaveinfo Type 'CovBaseProgram01' (CovBaseProgram01.java:27) advised by before advice from 'CovAspect08' (CovAspect08.aj:5)"
		});
	}	
	
	/**
	 * call(FastCar Super.getCar()) matches nothing
	 */
	public void testCOV009() {
		CompilationResult cR = binaryWeave("CovBaseProgram01.jar","CovAspect09.aj",0,0);
		verifyOutput(cR,new String[]{/* No matches */});
		assertTrue("Expected no warnings but got: "+cR.getWarningMessages(),cR.getWarningMessages().size()==0);
	}	
	
	/**
	 * call(Car+ getCar()) matches both
	 */
	public void testCOV010() {
		CompilationResult cR = binaryWeave("CovBaseProgram01.jar","CovAspect10.aj",0,0);
		verifyOutput(cR,new String[]{
				"weaveinfo Type 'CovBaseProgram01' (CovBaseProgram01.java:26) advised by before advice from 'CovAspect10' (CovAspect10.aj:5)",
				"weaveinfo Type 'CovBaseProgram01' (CovBaseProgram01.java:27) advised by before advice from 'CovAspect10' (CovAspect10.aj:5)"
		});
	}	

	//--------------------------------------------------------------------------------
	//--------------------------------------------------------------------------------
	//--------------------------------------------------------------------------------
	
	private File baseDir;
	
	protected void setUp() throws Exception {
		super.setUp();
		baseDir = new File("../tests/java5/covariance");
	}
	
	private CompilationResult binaryWeave(String inpath, String insource,int expErrors,int expWarnings) {
		String[] args = new String[] {"-inpath",inpath,insource,"-showWeaveInfo"};
		CompilationResult result = ajc(baseDir,args);
		if (verbose || result.hasErrorMessages()) System.out.println(result);
		assertTrue("Expected "+expErrors+" errors but got "+result.getErrorMessages().size()+":\n"+
				   formatCollection(result.getErrorMessages()),result.getErrorMessages().size()==expErrors);
		assertTrue("Expected "+expWarnings+" warnings but got "+result.getWarningMessages().size()+":\n"+
				   formatCollection(result.getWarningMessages()),result.getWarningMessages().size()==expWarnings);
		return result;
	}
	
	private List getWeavingMessages(List msgs) {
		List result = new ArrayList();
		for (Iterator iter = msgs.iterator(); iter.hasNext();) {
			IMessage element = (IMessage) iter.next();
			if (element.getKind()==IMessage.WEAVEINFO) {
				result.add(element.toString());
			}
		}
		return result;
	}

	private void verifyOutput(CompilationResult cR,String[] expected) {
		List weavingmessages = getWeavingMessages(cR.getInfoMessages());
		dump(weavingmessages);
		for (int i = 0; i < expected.length; i++) {
			boolean found = weavingmessages.contains(expected[i]);
			if (found) {
				weavingmessages.remove(expected[i]);
			} else {
				System.err.println(dump(getWeavingMessages(cR.getInfoMessages())));
				fail("Expected message not found.\nExpected:\n"+expected[i]+"\nObtained:\n"+dump(getWeavingMessages(cR.getInfoMessages())));
			}
		}
		if (weavingmessages.size()!=0) {
			fail("Unexpected messages obtained from program:\n"+dump(weavingmessages));
		}
	}
	
	private String formatCollection(Collection s) {
		StringBuffer sb = new StringBuffer();
		for (Iterator iter = s.iterator(); iter.hasNext();) {
			Object element = (Object) iter.next();
			sb.append(element).append("\n");
		}
		return sb.toString();
	}
	
	private static Set split(String input) {
		Set l = new HashSet();
		int idx = 0;
		while (input.indexOf("]",idx)!=-1) {
			int nextbreak = input.indexOf("]",idx);
			String s = input.substring(idx,nextbreak+1);
			
			l.add(s);
			idx = input.indexOf("[",nextbreak+1);
			if (idx==-1) break;
		}
		return l;
	}
	
	private void copyFile(String fromName) {
		copyFile(fromName,fromName);
	}
	
	private void copyFile(String from,String to) {
		try {
	  	  org.aspectj.util.FileUtil.copyFile(new File(baseDir + File.separator + from),
	  			          new File(ajc.getSandboxDirectory(),to));
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	
	private String dump(List l) {
		StringBuffer sb = new StringBuffer();
		int i =0;
		sb.append("--- Weaving Messages ---\n");
		for (Iterator iter = l.iterator(); iter.hasNext();) {
			sb.append(i+") "+iter.next()+"\n");
		}
		sb.append("------------------------\n");
		return sb.toString();
	}
}
