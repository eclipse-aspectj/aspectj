/*******************************************************************************
 * Copyright (c) 2005 IBM 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *    Andy Clement - initial API and implementation
 *******************************************************************************/
package org.aspectj.systemtest.ajc150;

import java.io.File;
import java.util.Iterator;
import java.util.List;

import org.aspectj.bridge.IMessage;
import org.aspectj.tools.ajc.CompilationResult;



public class PerTypeWithinTests extends TestUtils {

  protected void setUp() throws Exception {
	super.setUp();
	baseDir = new File("../tests/java5/pertypewithin");
  }
  
  /**
   * First few tests:
   * 
   * Five types p.A, p.B, p.C, q.D, q.E and an aspect a.X.
   * 
   * The aspect is pertypewithin(p..*) - this should match A,B,C but not D,E.
   * 
   * Aspect instances should be accessible for A,B,C but not D,E.
   * The aspect instances for A,B,C should be different.
   * 
   * hasAspect(), aspectOf() should work.
   * 
   * We test these assumptions in A,B,C,D,E. 
   */
  public void testDoesItWorkAtAll() {
	CompilationResult cR=ajc(baseDir,new String[]{"A.java","B.java","C.java","D.java","Main.java","X.java"});
  	assertTrue("Expected no errors:"+cR,!cR.hasErrorMessages());
	// if (verbose) { System.err.println(cR); System.err.println(cR.getStandardError());}
  	RunResult rR = run("p.A");
  	// if (verbose) {System.err.println(rR.getStdErr());}
  	assertTrue("Expected a report from the aspect about 2 calls to sayhi():"+rR.getStdErr(),
  			   rR.getStdErr().indexOf("callcount = 2")!=-1);
  }
  
  public void testCheckHasAspectWorks() {
	CompilationResult cR=ajc(baseDir,new String[]{"A.java","B.java","C.java","D.java","Main.java","X.java"});
  	assertTrue("Expected no errors:"+cR,!cR.hasErrorMessages());
	// if (verbose) { System.err.println(cR); System.err.println(cR.getStandardError());}
  	RunResult rR = run("p.A");
  	rR = run("p.B");
  	// if (verbose) {System.err.println(rR.getStdErr());}
  	assertTrue("Expected a report from the aspect about 3 calls to sayhi():"+rR.getStdErr(),
  			   rR.getStdErr().indexOf("callcount = 3")!=-1);
  }
  
  public void testCheckAspectOfWorks() {
	CompilationResult cR=ajc(baseDir,new String[]{"A.java","B.java","C.java","D.java","Main.java","X.java"});
  	assertTrue("Expected no errors:"+cR,!cR.hasErrorMessages());
	// if (verbose) { System.err.println(cR); System.err.println(cR.getStandardError());}
  	RunResult rR = run("p.A");
  	rR = run("p.C");
  	// if (verbose) {System.err.println(rR.getStdErr());}
  	
  }  
  
  /**
   * Aspects Q and R match P with a pertypewithin() - they shouldn't clash in any way
   *
   */
  public void testTwoAspectsHittingOneType() {
	CompilationResult cR=ajc(baseDir,new String[]{"P.java","Q.java","R.java"});
  	assertTrue("Expected no errors:"+cR,!cR.hasErrorMessages());
	// if (verbose) { System.err.println(cR); System.err.println(cR.getStandardError());}
  	RunResult rR = run("P");
  	// if (verbose) {System.err.println(rR.getStdErr());}  	
  	assertTrue("Expected message about Q reporting 2: "+rR.getStdErr(),
  			rR.getStdErr().indexOf("Q reporting 2")!=-1);
  	assertTrue("Expected message about R reporting 3: "+rR.getStdErr(),
  			rR.getStdErr().indexOf("R reporting 3")!=-1);
  }
  
  /**
   * Checks the use of pertypewithin() doesn't result in extra join points (i.e. the
   * infrastructure is properly hidden in ajc$ or synthetic members)
   */
  public void testPervasivenessOfWeaving() {
  	CompilationResult cR = ajc(baseDir,new String[]{"U.java","-showWeaveInfo"});
  	List l = cR.getInfoMessages();
  	int cnt = 0;
  	for (Iterator iter = l.iterator(); iter.hasNext();) {
		IMessage element = (IMessage) iter.next();
		if (element.getKind()==IMessage.WEAVEINFO) {
			//System.err.println(element);
			cnt++;
		}
	}
  	int weavingMessagesFromNormalDeploymentModel = cnt;
  	//System.err.println(cnt);

    cR = ajc(baseDir,new String[]{"V.java","-showWeaveInfo"});
  	l = cR.getInfoMessages();
  	cnt = 0;
  	for (Iterator iter = l.iterator(); iter.hasNext();) {
		IMessage element = (IMessage) iter.next();
		if (element.getKind()==IMessage.WEAVEINFO) {
			//System.err.println(element);
			cnt++;
		}
	}
  	int weavingMessagesFromPerTypeWithin = cnt;
  	//System.err.println(cnt);
  	if (weavingMessagesFromNormalDeploymentModel!=weavingMessagesFromPerTypeWithin)
  		fail("Expected same number of messages regardless of perclause but got "+
  				weavingMessagesFromNormalDeploymentModel+" and "+weavingMessagesFromPerTypeWithin);
		
  }
  
  public void testBinaryWeaving_ClassesAreBinary() {
  	// Compile the 'ordinary' class G.java into classes
  	CompilationResult cR = ajc(baseDir,new String[]{"G.java","-d","classes2"});
  	setShouldEmptySandbox(false);
  	// Compile the aspect with G.class as input, should be binary woven correctly
  	cR = ajc(baseDir,new String[]{"H.java","-inpath","classes2"});
  	RunResult rR = run("G");
  	assertTrue("Expected aspect related message 'advice running' in output from G",
  			rR.getStdErr().indexOf("advice running")!=-1);
  	setShouldEmptySandbox(true);
  }
  
  public void testBinaryWeaving_AspectsAreBinary() {
  	// Compile the aspect H.java into classes3
  	CompilationResult cR = ajc(baseDir,new String[]{"H.java","-outjar","aspects.jar"});
  	setShouldEmptySandbox(false);
  	// Compile the class with H.class as aspectpath, should be binary woven correctly
  	cR = ajc(baseDir,new String[]{"G.java","-aspectpath","aspects.jar"});
  	RunResult rR = run("G");
  	assertTrue("Expected aspect related message 'advice running' in output from G",
  			rR.getStdErr().indexOf("advice running")!=-1);
  	setShouldEmptySandbox(true);
  }
  
  // binary weaving case ...
}