/* *******************************************************************
 * Copyright (c) 2019 Contributors
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 * ******************************************************************/
package org.aspectj.weaver.bcel;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.aspectj.apache.bcel.classfile.ClassFormatException;
import org.aspectj.apache.bcel.classfile.ClassParser;
import org.aspectj.apache.bcel.classfile.JavaClass;
import org.aspectj.bridge.AbortException;
import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.IMessage.Kind;
import org.aspectj.bridge.IMessageHandler;
import org.aspectj.util.LangUtil;
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.bcel.ClassPathManager.ClassFile;

import junit.framework.TestCase;

/**
 * Should run these tests 3 times on each JDK level (8, 9, 11). On each one 3 of the
 * tests should pass indicating that JDK can successfully access system types in 
 * each JDK level.
 * 
 * @author Andy Clement
 */
public class ClasspathManagerTestCase extends TestCase {
	
	// Works on my machine where all jvms under ~/jvms
	private static String java18_rtjar = findJvm("j.*18.*","rt.jar");
	private static String java9_jrtfsjar = findJvm("j.*9.*","jrt-fs.jar");;
	private static String java11_jrtfsjar = findJvm("j.*11.*","jrt-fs.jar");;
	
	private static String findJvm(String pattern, String jar) {
		String start = System.getProperty("user.home")+"/jvms";
		if (new File(start).isDirectory()) {
			for (File f : new File(start).listFiles()) {
				if (f.isDirectory() && Pattern.matches(pattern, f.getName())) {
					File result = walk(f, jar);
					if (result != null) {
						System.out.println("For " + pattern + " found " + result.getAbsolutePath());
						return result.getAbsolutePath();
					}
				}
			}
		}
		return null;
	}
	
	private static File walk(File dir, String jar) {
		File[] fs = dir.listFiles();
		if (fs!=null) {
			for (File f: fs) {
				if (f.getName().equals(jar)) {
					return f;
				} else if (f.isDirectory()) {
					File s = walk(f, jar);
					if (s!=null) {
						return s;
					}
				}
			}
		}
		return null;
	}
	
	public void testInstructions() {
		System.out.println("This test is really only for standalone usage as it need executing on multiple JDK levels");
	}

	public void xtestSanity18accessing18RTJAR() throws IOException {
    	if (LangUtil.getVmVersion()>8) fail("Must be Java 8");
    	List<String> classpath = new ArrayList<>();
    	classpath.add(java18_rtjar);
    	ClassPathManager cpm = new ClassPathManager(classpath, new MH());
    	ClassFile t = cpm.find(UnresolvedType.forSignature("Ljava/lang/Object;"));
    	assertNotNull(t);	
    }
    
    public void xtestJava18accessing11JRT() throws ClassFormatException, IOException {
    	if (LangUtil.getVmVersion()>8) fail("Must be Java 8");
    	List<String> classpath = new ArrayList<>();
    	classpath.add(java11_jrtfsjar);
    	ClassPathManager cpm = new ClassPathManager(classpath, new MH());
    	ClassFile t = cpm.find(UnresolvedType.forSignature("Ljava/lang/String;"));
    	assertNotNull(t);
    	ClassParser classParser = new ClassParser(t.getInputStream(),t.getPath());
    	JavaClass clazz = classParser.parse();
    	// isBlank() exists on Java 11
    	long c = Arrays.asList(clazz.getMethods()).stream().filter(m -> m.getName().equals("isBlank")).count();
    	assertEquals(1,c);
    }

    public void xtestJava18accessing19JRT() throws ClassFormatException, IOException {
    	if (LangUtil.getVmVersion()>8) fail("Must be Java 8");
    	List<String> classpath = new ArrayList<>();
    	classpath.add(java9_jrtfsjar);
    	ClassPathManager cpm = new ClassPathManager(classpath, new MH());
    	ClassFile t = cpm.find(UnresolvedType.forSignature("Ljava/lang/String;"));
    	assertNotNull(t);
    	ClassParser classParser = new ClassParser(t.getInputStream(),t.getPath());
    	JavaClass clazz = classParser.parse();
    	// isBlank() exists on Java 11, but not on Java9
    	long c = Arrays.asList(clazz.getMethods()).stream().filter(m -> m.getName().equals("isBlank")).count();
    	assertEquals(0,c);
    }

    
    public void xtestSanity19accessing18RTJAR() throws IOException {
    	assertEquals(9.0,LangUtil.getVmVersion());
    	List<String> classpath = new ArrayList<>();
    	classpath.add(java18_rtjar);
    	ClassPathManager cpm = new ClassPathManager(classpath, new MH());
    	ClassFile t = cpm.find(UnresolvedType.forSignature("Ljava/lang/Object;"));
    	assertNotNull(t);	
    }
    
    public void xtestJava19accessing11JRT() throws ClassFormatException, IOException {
    	assertEquals(9.0,LangUtil.getVmVersion());
    	List<String> classpath = new ArrayList<>();
    	classpath.add(java11_jrtfsjar);
    	ClassPathManager cpm = new ClassPathManager(classpath, new MH());
    	ClassFile t = cpm.find(UnresolvedType.forSignature("Ljava/lang/String;"));
    	assertNotNull(t);
    	ClassParser classParser = new ClassParser(t.getInputStream(),t.getPath());
    	JavaClass clazz = classParser.parse();
    	// isBlank() exists on Java 11
    	long c = Arrays.asList(clazz.getMethods()).stream().filter(m -> m.getName().equals("isBlank")).count();
    	assertEquals(1,c);
    }

    public void xtestJava19accessing19JRT() throws ClassFormatException, IOException {
    	assertEquals(9.0,LangUtil.getVmVersion());
    	List<String> classpath = new ArrayList<>();
    	classpath.add(java9_jrtfsjar);
    	ClassPathManager cpm = new ClassPathManager(classpath, new MH());
    	ClassFile t = cpm.find(UnresolvedType.forSignature("Ljava/lang/String;"));
    	assertNotNull(t);
    	ClassParser classParser = new ClassParser(t.getInputStream(),t.getPath());
    	JavaClass clazz = classParser.parse();
    	// isBlank() exists on Java 11, but not on Java9
    	long c = Arrays.asList(clazz.getMethods()).stream().filter(m -> m.getName().equals("isBlank")).count();
    	assertEquals(0,c);
    }
    
    public void xtestSanity11accessing18RTJAR() throws IOException {
    	assertEquals(11.0,LangUtil.getVmVersion());
    	List<String> classpath = new ArrayList<>();
    	classpath.add(java18_rtjar);
    	ClassPathManager cpm = new ClassPathManager(classpath, new MH());
    	ClassFile t = cpm.find(UnresolvedType.forSignature("Ljava/lang/Object;"));
    	assertNotNull(t);	
    }
    
    public void xtestJava11accessing11JRT() throws ClassFormatException, IOException {
    	assertEquals(11.0,LangUtil.getVmVersion());
    	List<String> classpath = new ArrayList<>();
    	classpath.add(java11_jrtfsjar);
    	ClassPathManager cpm = new ClassPathManager(classpath, new MH());
    	ClassFile t = cpm.find(UnresolvedType.forSignature("Ljava/lang/String;"));
    	assertNotNull(t);
    	ClassParser classParser = new ClassParser(t.getInputStream(),t.getPath());
    	JavaClass clazz = classParser.parse();
    	// isBlank() exists on Java 11
    	long c = Arrays.asList(clazz.getMethods()).stream().filter(m -> m.getName().equals("isBlank")).count();
    	assertEquals(1,c);
    }

    public void xtestJava11accessing19JRT() throws ClassFormatException, IOException {
    	assertEquals(11.0,LangUtil.getVmVersion());
    	List<String> classpath = new ArrayList<>();
    	classpath.add(java9_jrtfsjar);
    	ClassPathManager cpm = new ClassPathManager(classpath, new MH());
    	ClassFile t = cpm.find(UnresolvedType.forSignature("Ljava/lang/String;"));
    	assertNotNull(t);
    	ClassParser classParser = new ClassParser(t.getInputStream(),t.getPath());
    	JavaClass clazz = classParser.parse();
    	// isBlank() exists on Java 11, but not on Java9
    	long c = Arrays.asList(clazz.getMethods()).stream().filter(m -> m.getName().equals("isBlank")).count();
    	assertEquals(0,c);
    }
    
    static class MH implements IMessageHandler {

		@Override
		public boolean handleMessage(IMessage message) throws AbortException {
			System.out.println(message);
			return false;
		}

		@Override
		public boolean isIgnoring(Kind kind) {
			return false;
		}

		@Override
		public void dontIgnore(Kind kind) {
		}

		@Override
		public void ignore(Kind kind) {
		}
    	
    }
}
