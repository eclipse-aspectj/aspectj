package org.aspectj.systemtest.ajc150.ataspectj.coverage;

import java.io.File;

import junit.framework.Test;

import org.aspectj.testing.XMLBasedAjcTestCase;


public class CoverageTests extends org.aspectj.testing.AutowiredXMLBasedAjcTestCase {


 public static Test suite() {
   return org.aspectj.testing.AutowiredXMLBasedAjcTestCase.loadSuite(CoverageTests.class);
 }

 protected File getSpecFile() {
   return new File("../tests/src/org/aspectj/systemtest/ajc150/ataspectj/coverage/coverage.xml");
 }

}
