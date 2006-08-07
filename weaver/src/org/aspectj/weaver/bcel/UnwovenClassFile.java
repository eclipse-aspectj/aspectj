/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     PARC     initial implementation 
 * ******************************************************************/


package org.aspectj.weaver.bcel;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.aspectj.apache.bcel.classfile.JavaClass;
import org.aspectj.util.FileUtil;

public class UnwovenClassFile {
	protected String filename;
	protected byte[] bytes;
//	protected JavaClass javaClass = null;
	//protected byte[] writtenBytes = null;
	protected List /* ChildClass */ writtenChildClasses = new ArrayList(0);
	protected String className = null;
	
	public UnwovenClassFile(String filename, byte[] bytes) {
		this.filename = filename;
		this.bytes = bytes;
	}

    /** Use if the classname is known, saves a bytecode parse */
	public UnwovenClassFile(String filename, String classname,byte[] bytes) {
		this.filename = filename;
		this.className = classname;
		this.bytes = bytes;
	}
	
	public String getFilename() {
		return filename;
	}
	
	public String makeInnerFileName(String innerName) {
		String prefix = filename.substring(0, filename.length()-6); // strip the .class
		return prefix + "$" + innerName + ".class";
	}
	
	public byte[] getBytes() {
//		if (bytes == null) bytes = javaClass.getBytes();
		return bytes;
	}
	
	public JavaClass getJavaClass() {
		//XXX need to know when to make a new class and when not to
		//XXX this is an important optimization
		if (getBytes() == null) {
			System.out.println("no bytes for: " + getFilename());
			//Thread.currentThread().dumpStack();
			Thread.dumpStack();			
		}
		return Utility.makeJavaClass(filename, getBytes());
//		if (javaClass == null) javaClass = Utility.makeJavaClass(filename, getBytes());
//		return javaClass;
	}
	
	public boolean exists() {
		return getBytes() != null;
	}

	
	public void writeUnchangedBytes() throws IOException {
		writeWovenBytes(getBytes(), Collections.EMPTY_LIST);
	}
	
	public void writeWovenBytes(byte[] bytes, List childClasses) throws IOException {	
		writeChildClasses(childClasses);
		
		//System.err.println("should write: " + getClassName());
		
		//System.err.println("about to write: " + this + ", " + writtenBytes + ", ");
//					+ writtenBytes != null + " && " + unchanged(bytes, writtenBytes) );
			
		//if (writtenBytes != null && unchanged(bytes, writtenBytes)) return;
		
		//System.err.println("    actually wrote it");
		
		BufferedOutputStream os = FileUtil.makeOutputStream(new File(filename));
		os.write(bytes);
		os.close();
		
		//writtenBytes = bytes;
	}

	private void writeChildClasses(List childClasses) throws IOException {
		//??? we only really need to delete writtenChildClasses whose
		//??? names aren't in childClasses; however, it's unclear
		//??? how much that will affect performance
		deleteAllChildClasses();

		childClasses.removeAll(writtenChildClasses); //XXX is this right
		
		for (Iterator iter = childClasses.iterator(); iter.hasNext();) {
			ChildClass childClass = (ChildClass) iter.next();
			writeChildClassFile(childClass.name, childClass.bytes);
			
		}
		
		writtenChildClasses = childClasses;
		
	}

	private void writeChildClassFile(String innerName, byte[] bytes) throws IOException {
		BufferedOutputStream os =
			FileUtil.makeOutputStream(new File(makeInnerFileName(innerName)));
		os.write(bytes);
		os.close();
	}


	protected void deleteAllChildClasses() {
		for (Iterator iter = writtenChildClasses.iterator(); iter.hasNext();) {
			ChildClass childClass = (ChildClass) iter.next();
			deleteChildClassFile(childClass.name);
		}
	}

	protected void deleteChildClassFile(String innerName) {
		File childClassFile = new File(makeInnerFileName(innerName));
		childClassFile.delete();
	}



	/* private */ static boolean unchanged(byte[] b1, byte[] b2) {
		int len = b1.length;
		if (b2.length != len) return false;
		for (int i=0; i < len; i++) {
			if (b1[i] != b2[i]) return false;
		}
		return true;
	}

	
	public String getClassName() {
		if (className == null) className = getJavaClass().getClassName();
		return className;
	}
	
	public String toString() {
		return "UnwovenClassFile(" + filename + ", " + getClassName() + ")";
	}
	
	/**
	 * delete not just this file, but any files in the same directory that
	 * were generated as a result of weaving it (e.g. for an around closure). 
	 */
	public void deleteRealFile() throws IOException {
		File victim = new File(filename);
		String namePrefix = victim.getName();
		namePrefix = namePrefix.substring(0,namePrefix.lastIndexOf('.'));
		final String targetPrefix = namePrefix + "$Ajc";
		File dir = victim.getParentFile();
		if (dir != null) {
			File[] weaverGenerated = dir.listFiles(new FilenameFilter() {
				public boolean accept(File dir, String name) {
					return name.startsWith(targetPrefix);
				}});
			if (weaverGenerated!=null) {
				for (int i = 0; i < weaverGenerated.length; i++) {
					weaverGenerated[i].delete();
				}
			}
		}
		victim.delete();
	}

	// record
	public static class ChildClass {
		public final String name;
		public final byte[] bytes;
		
		ChildClass(String name, byte[] bytes) {
			this.name = name;
			this.bytes = bytes;
		}
		
		public boolean equals(Object other) {
			if (! (other instanceof ChildClass)) return false;
			ChildClass o = (ChildClass) other;
			return o.name.equals(name) && unchanged(o.bytes, bytes);
		}
		public int hashCode() {
			return name.hashCode();
		}
		
		public String toString() {
			return "(ChildClass " + name + ")";
		}
	}
}




