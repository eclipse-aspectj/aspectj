/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     PARC     initial implementation 
 * ******************************************************************/


package org.aspectj.weaver.bcel;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.aspectj.bridge.IMessageHandler;
import org.aspectj.bridge.MessageUtil;
import org.aspectj.weaver.TypeX;


public class ClassPathManager {
	
	private List entries;
	
	public ClassPathManager(List classpath, IMessageHandler handler) {
		entries = new ArrayList();
		for (Iterator i = classpath.iterator(); i.hasNext();) {
			String name = (String) i.next();
			addPath(name, handler);
		}
	}

	public void addPath (String name, IMessageHandler handler) {
		File f = new File(name);
		String lc = name.toLowerCase();
		if (lc.endsWith(".jar") || lc.endsWith(".zip")) {
			if (!f.isFile()) {
			MessageUtil.info(handler, "zipfile classpath entry does not exist: " + name);
			return;
			}
			try {
				entries.add(new ZipFileEntry(f));
			} catch (IOException ioe) {
			MessageUtil.warn(handler, "zipfile classpath entry is invalid: " + name + "<" + ioe.getMessage() + ">");
			return;
			}
		} else {
			if (!f.isDirectory()) {
			MessageUtil.info(handler, "directory classpath entry does not exist: " + name);
			return;
			}
			entries.add(new DirEntry(f));
		}
	}


	public ClassFile find(TypeX type) {
		String name = type.getName();
		for (Iterator i = entries.iterator(); i.hasNext(); ) {
			Entry entry = (Entry)i.next();
			ClassFile ret = entry.find(name);
			if (ret != null) return ret;
		}
		return null;
	}
	
	public String toString() {
		StringBuffer buf = new StringBuffer();
		boolean start = true;
		for (Iterator i = entries.iterator(); i.hasNext(); ) {
			if (start) { start = false; }
			else {buf.append(File.pathSeparator); }
			buf.append(i.next());
		}
		return buf.toString();
	}
	
	/**
	 * This method is extremely expensive and should only be called rarely
	 */
	public List getAllClassFiles() {
		List ret = new ArrayList();
		for (Iterator i = entries.iterator(); i.hasNext(); ) {
			Entry entry = (Entry)i.next();
			ret.addAll(entry.getAllClassFiles());
		}
		return ret;
	}
	
	
	
	public abstract static class ClassFile {
		public abstract InputStream getInputStream() throws IOException;
		public abstract String getPath();
	}


	public abstract static class Entry {
		public abstract ClassFile find(String name);
		public abstract List getAllClassFiles();
	}
	
	
	private static class FileClassFile extends ClassFile {
		private File file;
		public FileClassFile(File file) {
			this.file = file;
		}
	
		public InputStream getInputStream() throws IOException {
			return new FileInputStream(file);
		}
		
		public String getPath() { return file.getPath(); }	
	}
	
	public class DirEntry extends Entry {
		private String dirPath;
		
		public DirEntry(File dir) { this.dirPath = dir.getPath(); }
		public DirEntry(String dirPath) { this.dirPath = dirPath; }
		
		public ClassFile find(String name) {
			File f = new File(dirPath + File.separator + name.replace('.', File.separatorChar) + ".class");
			if (f.isFile()) return new FileClassFile(f);
			else return null;
		}
		
		public List getAllClassFiles() {
			throw new RuntimeException("unimplemented");
		}
		
		public String toString() { return dirPath; }
	}
	
	
	private static class ZipEntryClassFile extends ClassFile {
		private ZipEntry entry;
		private ZipFile zipFile;
		public ZipEntryClassFile(ZipFile zipFile, ZipEntry entry) {
			this.zipFile = zipFile;
			this.entry = entry;
		}
	
		public InputStream getInputStream() throws IOException {
			return zipFile.getInputStream(entry);
		}
		
		public String getPath() { return entry.getName(); }
	
	}
	
	
	public class ZipFileEntry extends Entry {
		private ZipFile zipFile;
		
		public ZipFileEntry(File file) throws IOException {
			this(new ZipFile(file));
		}
		
		public ZipFileEntry(ZipFile zipFile) {
			this.zipFile = zipFile;
		}
		
		public ClassFile find(String name) {
			String key = name.replace('.', '/') + ".class";
			ZipEntry entry = zipFile.getEntry(key);
			if (entry != null) return new ZipEntryClassFile(zipFile, entry);
			else return null;
		}
		
		public List getAllClassFiles() {
			List ret = new ArrayList();
			for (Enumeration e = zipFile.entries(); e.hasMoreElements(); ) {
				ZipEntry entry = (ZipEntry)e.nextElement();
				String name = entry.getName();
				if (hasClassExtension(name)) ret.add(new ZipEntryClassFile(zipFile, entry));
			}
			return ret;
		}

		
		public String toString() { return zipFile.getName(); }
	}


	private static boolean hasClassExtension(String name) {
		return name.toLowerCase().endsWith((".class"));
	}
}
