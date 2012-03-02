/********************************************************************
 * Copyright (c) 2006 Contributors. All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *    Adrian Colyer      initial implementation
 *    Helen Hawkins      Converted to new interface (bug 148190)
 *******************************************************************/
package org.aspectj.systemtest.incremental.tools;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.aspectj.ajdt.internal.core.builder.AjState;
import org.aspectj.asm.IProgramElement;
import org.aspectj.asm.IRelationship;
import org.aspectj.asm.IRelationshipMap;
import org.aspectj.testing.util.FileUtil;

public class AbstractMultiProjectIncrementalAjdeInteractionTestbed extends AjdeInteractionTestbed {

	public static boolean VERBOSE = false;

	public static void dumptree(IProgramElement node, int indent) {
		for (int i = 0; i < indent; i++) {
			System.out.print(" ");
		}
		String loc = "";
		if (node != null) {
			if (node.getSourceLocation() != null) {
				loc = Integer.toString(node.getSourceLocation().getLine());
			}
		}
		// System.out.println(node + "  [" + (node == null ? "null" : node.getKind().toString()) + "] " + loc);
		System.out.println(node + "  [" + (node == null ? "null" : node.getKind().toString()) + "] " + loc
				+ (node == null ? "" : " hid:" + node.getHandleIdentifier()));
		if (node != null) {
			// for (int i = 0; i < indent; i++)
			// System.out.print(" ");
			// System.out.println("  hid is " + node.getHandleIdentifier());
			// Map m = ((ProgramElement) node).kvpairs;
			// if (m != null) {
			// Set keys = m.keySet();
			// for (Iterator iterator = keys.iterator(); iterator.hasNext();) {
			// Object object = (Object) iterator.next();
			//
			// for (int i = 0; i < indent; i++)
			// System.out.print(" ");
			// System.out.println("kvp: " + object + " = " + m.get(object));
			// }
			// }
			for (Iterator<IProgramElement> i = node.getChildren().iterator(); i.hasNext();) {
				dumptree( i.next(), indent + 2);
			}
		}
	}

	protected void setUp() throws Exception {
		super.setUp();
		AjdeInteractionTestbed.VERBOSE = VERBOSE;
		AjState.FORCE_INCREMENTAL_DURING_TESTING = true;
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		AjState.FORCE_INCREMENTAL_DURING_TESTING = false;
	}

	public void build(String projectName) {
		constructUpToDateLstFile(projectName, "build.lst");
		doBuild(projectName);
		if (AjdeInteractionTestbed.VERBOSE)
			printBuildReport(projectName);
	}

	public int getRelationshipCount(String project) {
		IRelationshipMap relmap = getModelFor(project).getRelationshipMap();
		int ctr = 0;
		Set<String> entries = relmap.getEntries();
		for (Iterator<String> iter = entries.iterator(); iter.hasNext();) {
			String hid = (String) iter.next();
			List<IRelationship> rels = relmap.get(hid);
			for (Iterator<IRelationship> iterator = rels.iterator(); iterator.hasNext();) {
				ctr+=iterator.next().getTargets().size();
			}
		}
		return ctr;
	}

	public void fullBuild(String projectName) {
		constructUpToDateLstFile(projectName, "build.lst");
		doFullBuild(projectName);
		if (AjdeInteractionTestbed.VERBOSE)
			printBuildReport(projectName);
	}

	private void constructUpToDateLstFile(String pname, String configname) {
		File projectBase = new File(sandboxDir, pname);
		File toConstruct = new File(projectBase, configname);
		List<String> filesForCompilation = new ArrayList<String>();
		collectUpFiles(projectBase, projectBase, filesForCompilation);

		try {
			FileOutputStream fos = new FileOutputStream(toConstruct);
			DataOutputStream dos = new DataOutputStream(fos);
			for (String file: filesForCompilation) {
				dos.writeBytes(file + "\n");
			}
			dos.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	private void collectUpFiles(File location, File base, List<String> collectionPoint) {
		String contents[] = location.list();
		if (contents == null)
			return;
		for (int i = 0; i < contents.length; i++) {
			String string = contents[i];
			File f = new File(location, string);
			if (f.isDirectory()) {
				collectUpFiles(f, base, collectionPoint);
			} else if (f.isFile() && (f.getName().endsWith(".aj") || f.getName().endsWith(".java"))) {
				String fileFound;
				try {
					fileFound = f.getCanonicalPath();
					String toRemove = base.getCanonicalPath();
					if (!fileFound.startsWith(toRemove))
						throw new RuntimeException("eh? " + fileFound + "   " + toRemove);
					collectionPoint.add(fileFound.substring(toRemove.length() + 1));// +1 captures extra separator
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Fill in the working directory with the project base files, from the 'base' folder.
	 */
	protected void initialiseProject(String p) {
		File projectSrc = new File(testdataSrcDir + File.separatorChar + p + File.separatorChar + "base");
		File destination = new File(getWorkingDir(), p);
		if (!destination.exists()) {
			destination.mkdir();
		}
		copy(projectSrc, destination);// ,false);
		// create the AjCompiler instance associated with this project
		// (has id of the form c:\temp\ajcSandbox\<workspace_name>\<project_name>)
		CompilerFactory.getCompilerForProjectWithDir(sandboxDir + File.separator + p);
	}

	/**
	 * Applies an overlay onto the project being tested - copying the contents of the specified overlay directory.
	 */
	public void alter(String projectName, String overlayDirectory) {
		File projectSrc = new File(testdataSrcDir + File.separatorChar + projectName + File.separatorChar + overlayDirectory);
		File destination = new File(getWorkingDir(), projectName);

		if (AjdeInteractionTestbed.VERBOSE)
			System.out.println("Altering project " + projectName);
		copy(projectSrc, destination);
	}

	/**
	 * Copy the contents of some directory to another location - the copy is recursive.
	 */
	protected void copy(File from, File to) {
		String contents[] = from.list();
		if (contents == null)
			return;
		for (int i = 0; i < contents.length; i++) {
			String string = contents[i];
			File f = new File(from, string);
			File t = new File(to, string);

			if (f.isDirectory() && !f.getName().startsWith("inc")) {
				t.mkdir();
				copy(f, t);
			} else if (f.isFile()) {
				StringBuffer sb = new StringBuffer();
				// if (VERBOSE) System.err.println("Copying "+f+" to "+t);
				FileUtil.copyFile(f, t, sb);
				if (sb.length() != 0) {
					System.err.println(sb.toString());
				}
			}
		}
	}

	/**
	 * Count the number of times a specified aspectName appears in the default aop.xml file and compare with the expected number of
	 * occurrences. If just want to count the number of aspects mentioned within the file then pass "" for the aspectName,
	 * otherwise, specify the name of the aspect interested in.
	 */
	protected void checkXMLAspectCount(String projectName, String aspectName, int expectedOccurrences, String outputDir) {
		int aspectCount = 0;
		File aopXML = new File(outputDir + File.separatorChar + "META-INF" + File.separatorChar + "aop-ajc.xml");

		if (!aopXML.exists()) {
			fail("Expected file " + aopXML.getAbsolutePath() + " to exist but it doesn't");
		}
		try {
			BufferedReader reader = new BufferedReader(new FileReader(aopXML));
			String line = reader.readLine();
			while (line != null) {
				if (aspectName.equals("") && line.indexOf("aspect name=\"") != -1) {
					aspectCount++;
				} else if (line.indexOf("aspect name=\"" + aspectName + "\"") != -1) {
					aspectCount++;
				}
				line = reader.readLine();
			}
			reader.close();
		} catch (IOException ie) {
			ie.printStackTrace();
		}
		if (aspectCount != expectedOccurrences) {
			fail("Expected aspect " + aspectName + " to appear " + expectedOccurrences + " times"
					+ " in the aop.xml file but found " + aspectCount + " occurrences");
		}
	}

	protected void assertContains(String expectedSubstring, Object object) {
		String actualString = object.toString();
		if (actualString.indexOf(expectedSubstring) == -1) {
			fail("Expected to find '" + expectedSubstring + "' in '" + actualString + "'");
		}
	}

	/** @return the number of relationship pairs */
	protected void printModel(String projectName) throws Exception {
		dumptree(getModelFor(projectName).getHierarchy().getRoot(), 0);
		PrintWriter pw = new PrintWriter(System.out);
		getModelFor(projectName).dumprels(pw);
		pw.flush();
	}

	protected File getProjectRelativePath(String p, String filename) {
		File projDir = new File(getWorkingDir(), p);
		return new File(projDir, filename);
	}

	protected void assertNoErrors(String projectName) {
		assertTrue("Should be no errors, but got " + getErrorMessages(projectName), getErrorMessages(projectName).size() == 0);
	}
}
