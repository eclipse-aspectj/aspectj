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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.aspectj.apache.bcel.classfile.JavaClass;
import org.aspectj.apache.bcel.util.ClassPath;
import org.aspectj.apache.bcel.util.SyntheticRepository;
import org.aspectj.bridge.IMessage;
import org.aspectj.tools.ajc.AjcTestCase;
import org.aspectj.tools.ajc.CompilationResult;

public abstract class TestUtils extends AjcTestCase {
	protected static final boolean verbose = false;
	protected File baseDir;
	
	protected CompilationResult binaryWeave(String inpath,String insource,int expErrors,int expWarnings) {
		return binaryWeave(inpath,insource,expErrors,expWarnings,false);
	}

	protected CompilationResult binaryWeave(String inpath, String insource,int expErrors,int expWarnings,boolean xlinterror) {
		return binaryWeave(inpath,insource,expErrors,expWarnings,xlinterror,(String[])null);
	}
	
	protected CompilationResult binaryWeave(String inpath, String insource,int expErrors,int expWarnings,String extraOption) {
		return binaryWeave(inpath,insource,expErrors,expWarnings,false,extraOption);
	}

	protected CompilationResult binaryWeave(String inpath, String insource,int expErrors,int expWarnings,boolean xlinterror,String extraOption) {
		return binaryWeave(inpath, insource, expErrors, expWarnings,xlinterror,new String[] {extraOption});
	}

	protected CompilationResult binaryWeave(String inpath, String insource,int expErrors,int expWarnings,boolean xlinterror,String[] extraOptions) {
		String[] args = null;
		if (xlinterror) {
			if (extraOptions!=null && extraOptions.length > 0) { 
				String[] firstargs = new String[] {"-inpath",inpath,insource,"-showWeaveInfo","-proceedOnError","-Xlint:warning"};
				args = new String[firstargs.length + extraOptions.length];
				System.arraycopy(firstargs,0,args,0,firstargs.length);
				System.arraycopy(extraOptions,0,args,firstargs.length,extraOptions.length);
			}
			else 
				args = new String[] {"-inpath",inpath,insource,"-showWeaveInfo","-proceedOnError","-Xlint:warning"};
		} else {
			if (extraOptions!=null && extraOptions.length>0) { 
				String[] firstargs = new String[] {"-inpath",inpath,insource,"-showWeaveInfo","-proceedOnError"};
				args = new String[firstargs.length + extraOptions.length];
				System.arraycopy(firstargs,0,args,0,firstargs.length);
				System.arraycopy(extraOptions,0,args,firstargs.length,extraOptions.length);
			}
			else
				args = new String[] {"-inpath",inpath,insource,"-showWeaveInfo","-proceedOnError"};
		}
		CompilationResult result = ajc(baseDir,args);
		if (verbose || result.hasErrorMessages()) System.out.println(result);
		assertTrue("Expected "+expErrors+" errors but got "+result.getErrorMessages().size()+":\n"+
				   formatCollection(result.getErrorMessages()),result.getErrorMessages().size()==expErrors);
		assertTrue("Expected "+expWarnings+" warnings but got "+result.getWarningMessages().size()+":\n"+
				   formatCollection(result.getWarningMessages()),result.getWarningMessages().size()==expWarnings);
		return result;
	}
	

	private String formatCollection(Collection s) {
		StringBuffer sb = new StringBuffer();
		for (Iterator iter = s.iterator(); iter.hasNext();) {
			Object element = (Object) iter.next();
			sb.append(element).append("\n");
		}
		return sb.toString();
	}
	
	protected List getWeavingMessages(List msgs) {
		List result = new ArrayList();
		for (Iterator iter = msgs.iterator(); iter.hasNext();) {
			IMessage element = (IMessage) iter.next();
			if (element.getKind()==IMessage.WEAVEINFO) {
				result.add(element.toString());
			}
		}
		return result;
	}

	protected void verifyWeavingMessagesOutput(CompilationResult cR,String[] expected) {
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
	
	  public SyntheticRepository createRepos(File cpentry) {
		ClassPath cp = new ClassPath(cpentry+File.pathSeparator+System.getProperty("java.class.path"));
		return SyntheticRepository.getInstance(cp);
	  }
	  
	  protected JavaClass getClassFrom(File where,String clazzname) throws ClassNotFoundException {
		SyntheticRepository repos = createRepos(where);
		return repos.loadClass(clazzname);
	  }
}
