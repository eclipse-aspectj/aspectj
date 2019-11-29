package org.aspectj.systemtest.ajc153;

import java.net.URL;

import org.aspectj.testing.XMLBasedAjcTestCase;

import junit.framework.Test;

public class LTWServer153Tests extends XMLBasedAjcTestCase {

	public static Test suite() {
		return loadSuite(LTWServer153Tests.class);
	}

	protected URL getSpecFile() {
	    return getClassResource("ajc153.xml");
	}
    
  	public void testHandleDuplicateConfiguration_pr157474 () {
  		runTest("TestServer with duplicate configuration");
  	}

}
