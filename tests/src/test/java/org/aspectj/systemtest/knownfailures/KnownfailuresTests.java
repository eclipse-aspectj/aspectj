/* *******************************************************************
* Copyright (c) 2005 Contributors
* All rights reserved.
* This program and the accompanying materials are made available
* under the terms of the Eclipse Public License v 2.0
* which accompanies this distribution and is available at
* https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
*
* Contributors:
*   Andrew Huff             initial implementation
* ******************************************************************/
package org.aspectj.systemtest.knownfailures;

import org.aspectj.testing.XMLBasedAjcTestCase;

import junit.framework.Test;

public class KnownfailuresTests extends org.aspectj.testing.XMLBasedAjcTestCase {


 public static Test suite() {
   return XMLBasedAjcTestCase.loadSuite(KnownfailuresTests.class);
 }

 protected java.net.URL getSpecFile() {
	 return getClassResource("knownFailures.xml");
//   return new File("../tests/src/org/aspectj/systemtest/knownfailures/knownfailures.xml").toURI().toURL();
 }


 public void test001(){
   runTest("NullPointerException in jdt when using generics and inpath");
   // the NPE goes away if you don't use generics
 }

}
