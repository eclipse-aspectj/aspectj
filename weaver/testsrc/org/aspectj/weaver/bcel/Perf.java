/* *******************************************************************
 * Copyright (c) 2006 Contributors
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Andy Clement IBM     initial implementation 
 * ******************************************************************/
package org.aspectj.weaver.bcel;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.aspectj.weaver.ReferenceType;
import org.aspectj.weaver.ResolvedMemberImpl;

/**
 * Compares loading times for bcel and asm - NOT currently pointed at from the suite.
 */
public class Perf {

	public static void main(String[] n) {

		// Two warm up runs, then 3 real runs then print average
		//if (n[0].equals("BCEL")) {
			long t = timeBcel();
			 t = timeBcel();
			 
			 t = timeBcel();
			 t+= timeBcel();
			 t+= timeBcel();
			 t+= timeBcel();
			 t+= timeBcel();
			 t+= timeBcel();
			 t+= timeBcel();
			 t+= timeBcel(); 
			 t+= timeBcel();
			 t+= timeBcel();
			 t+= timeBcel();
			 t+= timeBcel(); 
			 t+= timeBcel();
			 t+= timeBcel();
			 t+= timeBcel();
			 t+= timeBcel();
			 System.err.println("Average for BCEL (across 16 runs):"+(t/16)+"ms");

		//} else {
			 t = timeASM();
			 t = timeASM();
			 
			 t = timeASM();
			 t+= timeASM();
			 t+= timeASM();
			 t+= timeASM();
			 t+= timeASM();
			 t+= timeASM();
			 t+= timeASM();
			 t+= timeASM();
			 t+= timeASM();
			 t+= timeASM();
			 t+= timeASM();
			 t+= timeASM();
			 t+= timeASM();
			 t+= timeASM();
			 t+= timeASM();
			 t+= timeASM();
			 System.err.println("Average for ASM (across 16 runs):"+(t/16)+"ms");
		//}
	}

	private static long timeASM() {
		BcelWorld fastWorld = new BcelWorld(
				"../lib/aspectj/lib/aspectjtools.jar");
		fastWorld.setBehaveInJava5Way(true);

		try {
			File f = new File("../lib/aspectj/lib/aspectjtools.jar");
			ZipFile zf = new ZipFile(f);
			int i = 0;
			long stime = System.currentTimeMillis();
			Enumeration entries = zf.entries();
			while (entries.hasMoreElements()) {
				ZipEntry zfe = (ZipEntry) entries.nextElement();
				String classfileName = zfe.getName();
				if (classfileName.endsWith(".class")) {
					String clazzname = classfileName.substring(0,
							classfileName.length() - 6).replace('/', '.');
					ReferenceType b = (ReferenceType) fastWorld
							.resolve(clazzname);
					i++;
				}
			}
			long etime = System.currentTimeMillis();
			System.err.println("asm (" + (etime - stime) + ")");
			// System.err.println();("Successfully compared "+i+"
			// entries!!");
			fastWorld.flush();
			return (etime-stime);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return -1;
	}

	private static long timeBcel() {
		BcelWorld slowWorld = new BcelWorld(
				"../lib/aspectj/lib/aspectjtools.jar");
		slowWorld.setFastDelegateSupport(false);
		slowWorld.setBehaveInJava5Way(true);

		ResolvedMemberImpl.showParameterNames = false;
		try {
			File f = new File("../lib/aspectj/lib/aspectjtools.jar");
			// assertTrue("Couldnt find aspectjtools to test. Tried:
			// "+f.getAbsolutePath(),f.exists());
			ZipFile zf = new ZipFile(f);
			int i = 0;
			long stime = System.currentTimeMillis();
			Enumeration entries = zf.entries();
			while (entries.hasMoreElements()) {
				ZipEntry zfe = (ZipEntry) entries.nextElement();
				String classfileName = zfe.getName();
				if (classfileName.endsWith(".class")) {
					String clazzname = classfileName.substring(0,
							classfileName.length() - 6).replace('/', '.');
					ReferenceType b = (ReferenceType) slowWorld
							.resolve(clazzname);
					i++;
				}
			}
			long etime = System.currentTimeMillis();
			System.err.println("bcel (" + (etime - stime) + ")");
			slowWorld.flush();
			return (etime-stime);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return -1;
	}
}
