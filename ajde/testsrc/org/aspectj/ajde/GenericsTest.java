/*
 * Created on Jan 27, 2005
  */
package org.aspectj.ajde;

import org.aspectj.asm.AsmManager;

/**
 * @author Mik Kersten
 */
public class GenericsTest extends AjdeTestCase {
    
    private AsmManager manager = null;
	private static final String CONFIG_FILE_PATH = "../bug-83565/build.lst";
 
	public void testBuild() {	
//	    assertTrue("build success", doSynchronousBuild(CONFIG_FILE_PATH));	
	}
	
	protected void setUp() throws Exception {
		super.setUp("examples");
		manager = AsmManager.getDefault();
	}
    
}
