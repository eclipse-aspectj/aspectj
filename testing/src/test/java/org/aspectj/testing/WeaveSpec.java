/* *******************************************************************
 * Copyright (c) 2005 IBM Corporation
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Adrian Colyer, 
 * ******************************************************************/
package org.aspectj.testing;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import org.aspectj.tools.ajc.AjcTestCase;
import org.aspectj.tools.ajc.CompilationResult;

/**
 * @author colyer
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class WeaveSpec extends CompileSpec {

	private String classesFiles;
	private String aspectsFiles;
	private List<File> classFilesFromClasses;

	/* (non-Javadoc)
	 * @see org.aspectj.testing.ITestStep#execute(org.aspectj.tools.ajc.AjcTestCase)
	 */
	public void execute(AjcTestCase inTestCase) {
		String failMessage = "test \"" + getTest().getTitle() + "\" failed";
		try {
			File base = new File(getBaseDir());
			classFilesFromClasses = new ArrayList<>();
			setFiles(classesFiles);
			String[] args = buildArgs();
			CompilationResult result = inTestCase.ajc(base,args);
			inTestCase.assertNoMessages(result,failMessage);
			File sandbox = inTestCase.getSandboxDirectory();
			createJar(sandbox,"classes.jar",true);
			
			inTestCase.setShouldEmptySandbox(false); 
			setFiles(aspectsFiles);
			String options = getOptions();
			if (options == null) {
				setOptions(""); 
			}
			setClasspath("classes.jar");
			args = buildArgs();
			result = inTestCase.ajc(base,args);
			inTestCase.assertNoMessages(result,failMessage);
			createJar(sandbox,"aspects.jar",false);
			
			args = buildWeaveArgs();
			inTestCase.setShouldEmptySandbox(false); 
			result = inTestCase.ajc(base,args);
			AjcTestCase.MessageSpec messageSpec = buildMessageSpec();
			inTestCase.assertMessages(result,failMessage,messageSpec);
			inTestCase.setShouldEmptySandbox(false); // so subsequent steps in same test see my results
		} catch (IOException e) {
			AjcTestCase.fail(failMessage + " " + e);
		}
	}

	public void setClassesFiles(String files) {
		this.classesFiles = files;
	}
	
	public void setAspectsFiles(String files) {
		this.aspectsFiles = files;
	}

	/**
	 * Find all the .class files under the dir, package them into a jar file,
	 * and then delete them.
	 * @param inDir
	 * @param name
	 */
	private void createJar(File inDir, String name, boolean isClasses) throws IOException {
		File outJar = new File(inDir,name);
		FileOutputStream fos = new FileOutputStream(outJar);
		JarOutputStream jarOut = new JarOutputStream(fos);
		List<File> classFiles = new ArrayList<>();
		List<File> toExclude = isClasses ? Collections.<File>emptyList() : classFilesFromClasses;
		collectClassFiles(inDir,classFiles,toExclude);
		if (isClasses) classFilesFromClasses = classFiles;
		String prefix = inDir.getPath() + File.separator;
		for (File f: classFiles) {
			String thisPath = f.getPath();
			if (thisPath.startsWith(prefix)) {
				thisPath = thisPath.substring(prefix.length());
			}
			JarEntry entry = new JarEntry(thisPath);
			jarOut.putNextEntry(entry);
			copyFile(f,jarOut);
			jarOut.closeEntry();
		}
		jarOut.flush();
		jarOut.close();
	}
	
	private void collectClassFiles(File inDir, List<File> inList, List<File> toExclude) {
		File[] contents = inDir.listFiles();
		for (File content : contents) {
			if (content.getName().endsWith(".class")) {
				if (!toExclude.contains(content)) {
					inList.add(content);
				}
			} else if (content.isDirectory()) {
				collectClassFiles(content, inList, toExclude);
			}
		}
	}
	
	private void copyFile(File f, OutputStream dest) throws IOException {
		FileInputStream fis = new FileInputStream(f);
		byte[] buf = new byte[4096];
		int read = -1;
		while((read = fis.read(buf)) != -1) {
			dest.write(buf,0,read);
		}
		fis.close();
	}
	
	private String[] buildWeaveArgs() {
		StringBuffer args = new StringBuffer();
		if (getOptions() != null) {
			StringTokenizer strTok = new StringTokenizer(getOptions(),",");
			while (strTok.hasMoreTokens()) {
				args.append(strTok.nextToken());
				args.append(" ");
			}
		}
		args.append("-inpath ");
		args.append("classes.jar");
		args.append(File.pathSeparator);
		args.append("aspects.jar");
		args.append(" ");
		args.append("-aspectpath ");
		args.append("aspects.jar");
		String argumentString = args.toString();
		StringTokenizer strTok = new StringTokenizer(argumentString," ");
		String[] ret = new String[strTok.countTokens()];
		for (int i = 0; i < ret.length; i++) {
			ret[i] = strTok.nextToken();
		}
		return ret;
	}
	
}
