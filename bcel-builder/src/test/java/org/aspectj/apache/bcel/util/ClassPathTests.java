package org.aspectj.apache.bcel.util;

import java.io.IOException;

import org.aspectj.apache.bcel.classfile.tests.BcelTestCase;
import org.aspectj.apache.bcel.util.ClassPath.ClassFile;

public class ClassPathTests extends BcelTestCase {

	public void testJava9ImageFile() throws IOException {
		String sunbootClasspath = System.getProperty("sun.boot.class.path");
		if (sunbootClasspath==null || !sunbootClasspath.contains(".jimage")) {
			// Not java9
			return;
		}
		ClassPath cp = new ClassPath(sunbootClasspath);
		ClassFile cf = cp.getClassFile("java/lang/Object");
		assertNotNull(cf);
		assertTrue(cf.getSize()>0);
		assertTrue(cf.getTime()>0);
	}
}
