/*******************************************************************************
 * Copyright (c) 2008 Contributors 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andy Clement - initial API and implementation
 *******************************************************************************/
package org.aspectj.systemtest.ajc162;

import java.io.File;

import junit.framework.Test;

import org.aspectj.testing.XMLBasedAjcTestCase;

public class Ajc162Tests extends org.aspectj.testing.XMLBasedAjcTestCase {
	
	// AspectJ1.6.2	 
	public void testAnnoValueBinding_pr246264() { runTest("anno value binding"); }
	public void testAroundAdviceProceed_pr211607() { runTest("around advice proceed"); }
//	public void testAdvisingInterfaces_pr163005() { runTest("advising joinpoints in interfaces"); }
	public void testChainedItds_pr176905() { runTest("chained itds"); }
	public void testDecAtAnnoDecP_pr198341() { runTest("dec atanno and decp"); }
//	public void testStarInAnnoStyle_pr209951() { runTest("asterisk in at aj pointcut"); }
	public void testMissingMarkers_pr197720() { runTest("missing markers on inherited annotated method"); }
	public void testLostGenericsSigOnItd_pr211146() { runTest("lost generic sig on itd"); }
	public void testLostGenericsSigOnItd_pr211146_2() { runTest("lost generic sig on itd - 2"); }
	public void testLostGenericsSigOnItd_pr211146_3() { runTest("lost generic sig on itd - 3"); }
	public void testLostGenericsSigOnItd_pr211146_4() { runTest("lost generic sig on itd - 4"); }
	public void testLostGenericsSigOnItd_pr211146_5() { runTest("lost generic sig on itd - 5"); }
	public void testMissingContext_pr194429() { runTest("missing context"); }
	public void testWarningsForLimitations_pr210114() { runTest("warnings for limitations"); }
	public void testPTW_pr244830() { runTest("ptw initFailureCause"); }
	public void testGenericItdsOverrides_pr222648() { runTest("generic itds - overrides"); }
	public void testGenericItdsOverrides_pr222648_2() { runTest("generic itds - overrides - 2"); }
    public void testItdCallingGenericMethod_pr145391() { runTest("itd calling generic method");}
    public void testItdCallingGenericMethod_pr145391_2() { runTest("itd calling generic method - 2");}
	public void testPublicPointcut_pr239539() { runTest("public pointcut"); }
	public void testGenericDecp_pr241047() { runTest("generic decp"); }
	public void testGenericDecp_pr241047_2() { runTest("generic decp - 2"); }
	public void testGenericItds_pr242797_1() { runTest("generic itds - 1"); }
	public void testGenericItds_pr242797_2() { runTest("generic itds - 2"); }
	public void testGenericItds_pr242797_3() { runTest("generic itds - 3"); }
	public void testPrivilegedGenerics_pr240693() { runTest("privileged generics"); }
//	public void testParamAnnosPipelining_pr241847() { runTest("param annos pipelining");}
//	public void testParamAnnoInner_pr241861() { runTest("param annotation inner class"); }
	public void testAnnotationDecp_pr239441() { runTest("annotation decp"); }
	public void testAtAspectJPerTarget_pr198181() { runTest("ataspectj ltw pertarget"); }
	public void testAnnotationValueDecp_pr238992() { runTest("annotation value decp"); }
	public void testAnnotationValueDecp_pr238992_2() { runTest("annotation value decp - 2"); }
	public void testAnnotationValueDecp_pr238992_3() { runTest("annotation value decp - 3"); }
	public void testAnnotationValueDecp_pr238992_4() { runTest("annotation value decp - 4"); }
	public void testAnnotationValueDecp_pr238992_5() { runTest("annotation value decp - 5"); }
	
	/*
	 * test plan
	 * execution(* *(..,String,..))
	 * args(..,String,..)
	 * @args(..,Foo,..)
	 * 
	 */
//	public void testParameterSubsettingMatching_pr233718_Matching() { runTest("parameter subsetting - matching");}
//	public void testParameterSubsettingMatching_pr233718_ArgsMatching() { runTest("parameter subsetting - args matching");}
//	public void testParameterSubsettingMatching_pr233718_ArgsBinding() { runTest("parameter subsetting - args binding");}

	public static Test suite() {
      return XMLBasedAjcTestCase.loadSuite(Ajc162Tests.class);
    }

    protected File getSpecFile() {
      return new File("../tests/src/org/aspectj/systemtest/ajc162/ajc162.xml");
    }
  
}