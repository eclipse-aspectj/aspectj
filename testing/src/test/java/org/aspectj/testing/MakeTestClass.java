/*
 * Created on 02-Aug-2004
 *
 */
package org.aspectj.testing;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.digester.Digester;

/**
 * @author colyer
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class MakeTestClass {

	private static final String HEADER = 
		"/* *******************************************************************\n" +
		" * Copyright (c) 2004 IBM Corporation\n" +
		" * All rights reserved.\n" + 
		" * This program and the accompanying materials are made available\n" + 
		" * under the terms of the Eclipse Public License v1.0\n" + 
		" * which accompanies this distribution and is available at\n" + 
		" * http://www.eclipse.org/legal/epl-v10.html \n" + 
		" * \n" +  
		" * ******************************************************************/\n" +
		"package org.aspectj.systemtest.XXX;\n" +
        "\n" +
		"import java.io.File;\n" +
		"import junit.framework.Test;\n" +
		"import org.aspectj.testing.XMLBasedAjcTestCase;\n" +
		"\n" +
		"public class ";

	private static final String BODY_1 = 
		" extends org.aspectj.testing.XMLBasedAjcTestCase {\n" +
		"\n" +
		"  public static Test suite() {\n" +
		"    return XMLBasedAjcTestCase.loadSuite(";
	
	private static final String BODY_2 =
		".class);\n" +
		"  }\n" +
		"\n" +
		"  protected File getSpecFile() {\n" +
		"    return new File(\"";
	
	private static final String BODY_3 =
		"\");\n" +
		"  }\n";
	
	private static final String FOOTER =
		"}\n";
	
	private List<AjcTest> tests = new ArrayList<>();
	private String className;
	private String suiteFile;
	
	public static void main(String[] args) throws Exception {
		new MakeTestClass(args[0],args[1]).makeTestClass();
	}
	
	public MakeTestClass(String className, String suiteFile)throws Exception {
		this.className = className;
		this.suiteFile = suiteFile;
		Digester d = getDigester();
		InputStreamReader isr = new InputStreamReader(new FileInputStream(suiteFile));
		d.parse(isr);
	}
	
	public void addTest(AjcTest test) {
		tests.add(test);
	}
	
	public void makeTestClass() throws Exception {
		FileOutputStream fos = new FileOutputStream(className + ".java");
		PrintStream out = new PrintStream(fos);
		out.print(HEADER);
		out.print(className);
		out.print(BODY_1);
		out.print(className);
		out.print(BODY_2);
		out.print(suiteFile);
		out.print(BODY_3);
		out.println();
		int testNo = 1;
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMinimumIntegerDigits(3);
		for (AjcTest test: tests) {
			out.println();
			out.print("  public void test");
			out.print(nf.format(testNo++));
			out.println("(){");
			out.println("    runTest(\"" + test.getTitle() + "\");");
			out.println("  }");
		}
		out.println();
		out.println(FOOTER);
		out.close();
	}
	
	private Digester getDigester() {
		Digester digester = new Digester();
		digester.push(this);
		digester.addObjectCreate("suite/ajc-test",AjcTest.class);
		digester.addSetProperties("suite/ajc-test");
		digester.addSetNext("suite/ajc-test","addTest","org.aspectj.testing.AjcTest");
		return digester;
	}
}
