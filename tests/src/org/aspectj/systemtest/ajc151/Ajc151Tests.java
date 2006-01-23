/*******************************************************************************
 * Copyright (c) 2006 IBM 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *    Andy Clement - initial API and implementation
 *******************************************************************************/
package org.aspectj.systemtest.ajc151;

import java.io.File;

import junit.framework.Test;

import org.aspectj.testing.XMLBasedAjcTestCase;

public class Ajc151Tests extends org.aspectj.testing.XMLBasedAjcTestCase {
	  
  public void testMemberTypesInGenericTypes_pr122458()    { runTest("member types in generic types");}
  public void testMemberTypesInGenericTypes_pr122458_2()  { runTest("member types in generic types - 2");}
  public void testNPEOnDeclareAnnotation_pr123695() { runTest("Internal nullptr exception with complex declare annotation");}
  public void testHasMemberPackageProblem_pr124105() { runTest("hasMember problems with packages");}
  public void testDifferentNumbersofTVars_pr124803() { runTest("generics and different numbers of type variables");}
  public void testDifferentNumbersofTVars_pr124803_2() { runTest("generics and different numbers of type variables - classes");}
  
  /////////////////////////////////////////
  public static Test suite() {
    return XMLBasedAjcTestCase.loadSuite(Ajc151Tests.class);
  }

  protected File getSpecFile() {
    return new File("../tests/src/org/aspectj/systemtest/ajc151/ajc151.xml");
  }
  
}