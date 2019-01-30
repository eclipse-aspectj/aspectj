/********************************************************************
 * Copyright (c) 2005 Contributors. All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: IBM Corporation - initial API and implementation 
 * 				 Helen Hawkins   - iniital version
 *******************************************************************/
package org.aspectj.tools.ajdoc;

import java.io.File;


public class EnumTest extends AjdocTestCase {
	
	/**
	 * Test for pr122728 - no StringOutOfBoundsException
	 * when processing an Enum
	 */
    public void testEnum() throws Exception {
    	initialiseProject("pr122728");
		File[] files = {new File(getAbsoluteProjectDir() + "/src/pack/MyEnum.java")};
		runAjdoc("private","1.5",files);
    }

	/**
	 * Test for pr122728 - no StringOutOfBoundsException
	 * when processing an Enum 
	 */
    public void testInlinedEnum() throws Exception {
    	initialiseProject("pr122728");
		File[] files = {new File(getAbsoluteProjectDir() + "/src/pack/ClassWithInnerEnum.java")};
		runAjdoc("private","1.5",files);
    }
    
	/**
	 * Test for pr122728 - no StringOutOfBoundsException
	 * when processing an Enum
	 */
    public void testEnumWithMethods() throws Exception {
    	initialiseProject("pr122728");
		File[] files = {new File(getAbsoluteProjectDir() + "/src/pack/EnumWithMethods.java")};
		runAjdoc("private","1.5",files);
    }
}
