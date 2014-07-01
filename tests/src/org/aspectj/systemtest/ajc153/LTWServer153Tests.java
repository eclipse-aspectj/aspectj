package org.aspectj.systemtest.ajc153;

import java.io.File;

import junit.framework.Test;

import org.aspectj.testing.XMLBasedAjcTestCase;

public class LTWServer153Tests extends XMLBasedAjcTestCase {

	public static Test suite() {
		return loadSuite(LTWServer153Tests.class);
	}

	protected File getSpecFile() {
	    return getClassResource("ajc153.xml");
	}
    
  	public void testHandleDuplicateConfiguration_pr157474 () {
  		runTest("TestServer with duplicate configuration");
  	}

}
