/*******************************************************************************
 * Copyright (c) 2006 IBM 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andy Clement - initial API and implementation
 *******************************************************************************/
package org.aspectj.testing;

import java.io.File;

import org.aspectj.apache.bcel.classfile.JavaClass;
import org.aspectj.apache.bcel.util.ClassPath;
import org.aspectj.apache.bcel.util.SyntheticRepository;

/**
 * Not quite the right place for this class..
 */
public class Utils {
	
	private final static boolean debugVerification=false;
	
//	/**
//	 * Performs verification of a class - the supplied class is expected to exist in the sandbox
//	 * directory so typically this is called after a small compile step has been invoked to build it.
//	 * @param ajc 
//	 */
//	public static String verifyClass(Ajc ajc, String clazzname) {
//		JavaClass jc = null;
//		try {
//			jc = getClassFrom(ajc.getSandboxDirectory().getAbsolutePath(),clazzname);
//		} catch (ClassNotFoundException cnfe) {
//			return "Could not find "+clazzname+" in the sandbox: "+ajc.getSandboxDirectory();
//		}
//		if (jc==null) return "Could not find class "+clazzname;
//		Repository.setRepository(jc.getRepository());
//		Verifier v = VerifierFactory.getVerifier("mypackage.MyAspect"); 
//		VerificationResult vr = v.doPass1();
//		if (debugVerification) System.err.println(vr);
//		
//		if (vr.getStatus()!=VerificationResult.VERIFIED_OK)
//			return "Verification not ok: "+vr;
//		vr = v.doPass2();
//		if (debugVerification) System.err.println(vr);
//		if (vr.getStatus()!=VerificationResult.VERIFIED_OK)
//			return "Verification not ok: "+vr;
//		Method[] ms = jc.getMethods();
//		for (int i = 0; i < ms.length; i++) {
//			if (debugVerification) System.err.println("Pass3a for "+ms[i]);
//			vr = v.doPass3a(i);
//			if (debugVerification) System.err.println(vr);		
//			if (vr.getStatus()!=VerificationResult.VERIFIED_OK)
//				return "Verification not ok: "+vr;
//			if (debugVerification) System.err.println("Pass3b for "+ms[i]);
//			vr = v.doPass3b(i);
//			if (debugVerification) System.err.println(vr);
//			if (vr.getStatus()!=VerificationResult.VERIFIED_OK)
//				return "Verification not ok: "+vr;
//		}
//		return null;
//	}
	
	public static JavaClass getClassFrom(String frompath,String clazzname) throws ClassNotFoundException {
		SyntheticRepository repos = createRepos(frompath);
		return repos.loadClass(clazzname);
	}

	public static SyntheticRepository createRepos(String cpentry) {
		ClassPath cp = new ClassPath(
				cpentry+File.pathSeparator+
				System.getProperty("java.class.path"));
		return SyntheticRepository.getInstance(cp);
	}	
}
