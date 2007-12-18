package org.aspectj.apache.bcel.classfile.tests;

import java.io.File;

import org.aspectj.apache.bcel.classfile.JavaClass;
import org.aspectj.apache.bcel.util.ClassPath;
import org.aspectj.apache.bcel.util.SyntheticRepository;

public class ClassPrinter extends BcelTestCase {

	public static void main(String[] args) throws ClassNotFoundException {
		new ClassPrinter().run(args);
	}
	
	public void run(String[] args) throws ClassNotFoundException {
		ClassPath cp = new ClassPath(args[0]+File.pathSeparator+System.getProperty("java.class.path"));
		SyntheticRepository sr =  SyntheticRepository.getInstance(cp);
		JavaClass clazz = sr.loadClass(args[1]);
		System.err.println(clazz.toString());
	}
}
