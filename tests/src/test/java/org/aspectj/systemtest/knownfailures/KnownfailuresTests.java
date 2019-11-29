/* *******************************************************************
* Copyright (c) 2005 Contributors
* All rights reserved.
* This program and the accompanying materials are made available
* under the terms of the Eclipse Public License v1.0
* which accompanies this distribution and is available at
* http://www.eclipse.org/legal/epl-v10.html 
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
