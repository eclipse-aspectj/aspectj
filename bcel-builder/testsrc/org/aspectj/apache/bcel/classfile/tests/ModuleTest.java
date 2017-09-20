/* *******************************************************************
 * Copyright (c) 2016-2017 Contributors
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 * ******************************************************************/

package org.aspectj.apache.bcel.classfile.tests;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

import org.aspectj.apache.bcel.Constants;
import org.aspectj.apache.bcel.classfile.Attribute;
import org.aspectj.apache.bcel.classfile.ClassParser;
import org.aspectj.apache.bcel.classfile.JavaClass;
import org.aspectj.apache.bcel.classfile.Module;
import org.aspectj.apache.bcel.classfile.Module.Export;
import org.aspectj.apache.bcel.classfile.Module.Open;
import org.aspectj.apache.bcel.classfile.Module.Provide;
import org.aspectj.apache.bcel.classfile.Module.Require;
import org.aspectj.apache.bcel.classfile.Module.Uses;
import org.aspectj.apache.bcel.classfile.SourceFile;

/**
 * http://cr.openjdk.java.net/~mr/jigsaw/spec/lang-vm.html
 * 
 * @author Andy Clement
 */
public class ModuleTest extends BcelTestCase {

	public void testLoadSimpleModuleClass() throws Exception {
		String moduleFilename = "testdata/modules/one/module-info.class";
		ClassParser classParser = new ClassParser(moduleFilename);
		JavaClass javaClass = classParser.parse();
		assertNotNull(javaClass);
		assertEquals(Constants.MAJOR_1_9,javaClass.getMajor());
		assertEquals(Constants.MINOR_1_9,javaClass.getMinor());
		assertEquals(Constants.ACC_MODULE,javaClass.getModifiers()); 
		assertEquals(0,javaClass.getSuperclassNameIndex());
		assertEquals(0,javaClass.getInterfaceIndices().length);
		assertEquals(0,javaClass.getFields().length);
		assertEquals(0,javaClass.getMethods().length);
		Attribute[] attrs = javaClass.getAttributes();
		assertEquals(2,attrs.length);
		SourceFile sourceFile = (SourceFile) getAttribute(attrs,Constants.ATTR_SOURCE_FILE);
		Module moduleAttr = (Module) getAttribute(attrs, Constants.ATTR_MODULE);
		byte[] originalData = moduleAttr.getBytes();
		String[] requiredModuleNames = moduleAttr.getRequiredModuleNames();
		assertEquals(1,requiredModuleNames.length);
		assertEquals("java.base",requiredModuleNames[0]);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		moduleAttr.dump(new DataOutputStream(baos));
		byte[] newData = baos.toByteArray();
		// The 6 offset here is because the newdata includes the 2byte cpool pointer for the name 'Module'
		// and the 4byte int length field for the attribute data
		if (newData.length!=originalData.length+6) {
			fail("Expected the length of the original attribute ("+originalData.length+") to match the new written length ("+newData.length+")");
		}
		for (int i=0;i<originalData.length;i++) {
			if (originalData[i]!=newData[i+6]) {
				fail("byte mismatch at position "+i+" of "+newData.length);
			}
		}
	}
	

	public void testRequires() throws Exception {
		Module moduleAttr = getModuleAttribute("testdata/modules/two/d/module-info.class");
		Require[] requires = moduleAttr.getRequires();
		assertEquals(4, requires.length);
		assertEquals("requires mandated java.base 9",requires[0].toString());
		assertEquals("requires a.b.c",requires[1].toString());
		assertEquals("requires static b.c.d",requires[2].toString());
		assertEquals("requires transitive c.d.e",requires[3].toString());
		assertEquals("java.base",requires[0].getModuleName());
		assertEquals("a.b.c",requires[1].getModuleName());
		assertEquals("b.c.d",requires[2].getModuleName());
		assertEquals("c.d.e",requires[3].getModuleName());
	}
	
	public void testExports() throws Exception {
		Module moduleAttr = getModuleAttribute("testdata/modules/two/e/module-info.class");
		Export[] exports = moduleAttr.getExports();
		assertEquals(3, exports.length);
		assertEquals("exports com.foo1", exports[0].toString());
		assertEquals("exports com.foo2 to a.b.c",exports[1].toString());
		assertEquals("exports com.foo3 to a.b.c, b.c.d",exports[2].toString());
		assertEquals("com/foo1",exports[0].getPackage());
		assertEquals("com/foo2",exports[1].getPackage());
		assertEquals("com/foo3",exports[2].getPackage());
		assertEquals("a.b.c",exports[1].getToModuleNames()[0]);
		assertEquals("a.b.c",exports[2].getToModuleNames()[0]);
		assertEquals("b.c.d",exports[2].getToModuleNames()[1]);
	}
	
	public void testOpens() throws Exception {
		Module moduleAttr = getModuleAttribute("testdata/modules/two/h/module-info.class");
		Open[] opens = moduleAttr.getOpens();
		assertEquals(3, opens.length);
		assertEquals("opens com.foo1", opens[0].toString());
		assertEquals("opens com.foo2 to a.b.c", opens[1].toString());
		assertEquals("opens com.foo3 to a.b.c, b.c.d", opens[2].toString());
	}
	
	public void testUses() throws Exception {
		Module moduleAttr = getModuleAttribute("testdata/modules/two/f/module-info.class");
		Uses[] uses = moduleAttr.getUses();
		assertEquals(1,uses.length);
		assertEquals("com/foo1/I1",uses[0].getTypeName());
		assertEquals("uses com.foo1.I1",uses[0].toString());
	}
	
	public void testProvides() throws Exception {
		Module moduleAttr = getModuleAttribute("testdata/modules/two/g/module-info.class");
		Provide[] provides = moduleAttr.getProvides();
		assertEquals(2,provides.length);
		assertEquals("provides com.foo1.I1 with com.foo1.C1",provides[0].toString());
		assertEquals("provides com.foo2.I2 with com.foo2.C2",provides[1].toString());
		assertEquals("com/foo1/I1",provides[0].getProvidedType());
		assertEquals("com/foo1/C1",provides[0].getWithTypeStrings()[0]);
		assertEquals("com/foo2/I2",provides[1].getProvidedType());
		assertEquals("com/foo2/C2",provides[1].getWithTypeStrings()[0]);
	}

	// ---
	
	private Module getModuleAttribute(String moduleInfoClass) throws Exception {
		ClassParser classParser = new ClassParser(moduleInfoClass);
		JavaClass javaClass = classParser.parse();
		return (Module)getAttribute(javaClass.getAttributes(), Constants.ATTR_MODULE);
	}
	
}
