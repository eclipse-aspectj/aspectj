package org.aspectj.systemtest.ajc150.ltw;

import java.io.File;

import junit.framework.Test;

import org.aspectj.testing.XMLBasedAjcTestCase;

public class LTWServerTests extends XMLBasedAjcTestCase {

	public static Test suite() {
		return loadSuite(LTWServerTests.class);
	}

	protected File getSpecFile() {
		return new File("../tests/src/org/aspectj/systemtest/ajc150/ltw/ltw.xml");
	}
    
  	public void testServerWithHelloWorld () {
  		runTest("TestServer with HelloWorld");
  	}
    
  	public void testServerWithParentAndChild () {
  		runTest("TestServer with Parent and Child");
  	}

}
