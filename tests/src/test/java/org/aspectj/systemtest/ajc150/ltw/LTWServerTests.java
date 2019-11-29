package org.aspectj.systemtest.ajc150.ltw;

import org.aspectj.testing.XMLBasedAjcTestCase;

import junit.framework.Test;

public class LTWServerTests extends XMLBasedAjcTestCase {

	public static Test suite() {
		return loadSuite(LTWServerTests.class);
	}

	protected java.net.URL getSpecFile() {
		return getClassResource("ltw.xml");
	}
    
  	public void testServerWithHelloWorld () {
  		runTest("TestServer with HelloWorld");
  	}
    
  	public void testServerWithParentAndChild () {
  		runTest("TestServer with Parent and Child");
  	}

}
