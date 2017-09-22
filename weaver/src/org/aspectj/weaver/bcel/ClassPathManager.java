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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.DirectoryStream;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.SimpleFileVisitor;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.aspectj.bridge.IMessageHandler;
import org.aspectj.bridge.MessageUtil;
import org.aspectj.util.LangUtil;
import org.aspectj.weaver.BCException;
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.WeaverMessages;
import org.aspectj.weaver.tools.Trace;
import org.aspectj.weaver.tools.TraceFactory;

public class ClassPathManager {

	private static Trace trace = TraceFactory.getTraceFactory().getTrace(ClassPathManager.class);

	private List<Entry> entries;

	// In order to control how many open files we have, we maintain a list.
	// The max number is configured through the property:
	// org.aspectj.weaver.openarchives
	// and it defaults to 1000
	private List<ZipFile> openArchives = new ArrayList<ZipFile>();

	private static int maxOpenArchives = -1;
	
	private static final int MAXOPEN_DEFAULT = 1000;

	static {
		String openzipsString = getSystemPropertyWithoutSecurityException("org.aspectj.weaver.openarchives",
				Integer.toString(MAXOPEN_DEFAULT));
		maxOpenArchives = Integer.parseInt(openzipsString);
		if (maxOpenArchives < 20) {
			maxOpenArchives = 1000;
		}
	}

	public ClassPathManager(List<String> classpath, IMessageHandler handler) {
		if (trace.isTraceEnabled()) {
			trace.enter("<init>", this, new Object[] { classpath==null?"null":classpath.toString(), handler });
		}
		entries = new ArrayList<Entry>();
		for (String classpathEntry: classpath) {
			addPath(classpathEntry,handler);
		}
		if (trace.isTraceEnabled()) {
			trace.exit("<init>");
		}
	}

	protected ClassPathManager() {
	}

	private static URI JRT_URI = URI.create("jrt:/"); //$NON-NLS-1$
	
	public void addPath(String name, IMessageHandler handler) {
		File f = new File(name);
		String lc = name.toLowerCase();
		if (!f.isDirectory()) {
			if (!f.isFile()) {
				if (!lc.endsWith(".jar") || lc.endsWith(".zip")) {
					// heuristic-only: ending with .jar or .zip means probably a
					// zip file
					MessageUtil.info(handler, WeaverMessages.format(WeaverMessages.ZIPFILE_ENTRY_MISSING, name));
				} else {
					MessageUtil.info(handler, WeaverMessages.format(WeaverMessages.DIRECTORY_ENTRY_MISSING, name));
				}
				return;
			}
			try {
				if (lc.endsWith(LangUtil.JRT_FS)) {
					// Java9
					entries.add(new JImageEntry());//new File(f.getParentFile()+File.separator+"lib"+File.separator+"modules")));
				} else {
					entries.add(new ZipFileEntry(f));
				}
			} catch (IOException ioe) {
				MessageUtil.warn(handler,
						WeaverMessages.format(WeaverMessages.ZIPFILE_ENTRY_INVALID, name, ioe.getMessage()));
				return;
			}
		} else {
			entries.add(new DirEntry(f));
		}
	}

	public ClassFile find(UnresolvedType type) {
		if (trace.isTraceEnabled()) {
			trace.enter("find", this, type);
		}
		String name = type.getName();
		for (Iterator<Entry> i = entries.iterator(); i.hasNext();) {
			Entry entry = i.next();
			try {
				ClassFile ret = entry.find(name);
				if (trace.isTraceEnabled()) {
					trace.event("searching for "+type+" in "+entry.toString());
				}
				if (ret != null) {
					if (trace.isTraceEnabled()) {
						trace.exit("find", ret);
					}
					return ret;
				}
			} catch (IOException ioe) {
				// this is NOT an error: it's valid to have missing classpath entries
				if (trace.isTraceEnabled()) {
					trace.error("Removing classpath entry for "+entry,ioe);
				}
				i.remove();
			}
		}
		if (trace.isTraceEnabled()) {
			trace.exit("find", null);
		}
		return null;
	}

	public String toString() {
		StringBuffer buf = new StringBuffer();
		boolean start = true;
		for (Iterator<Entry> i = entries.iterator(); i.hasNext();) {
			if (start) {
				start = false;
			} else {
				buf.append(File.pathSeparator);
			}
			buf.append(i.next());
		}
		return buf.toString();
	}

	public abstract static class ClassFile {
		public abstract InputStream getInputStream() throws IOException;
		public abstract String getPath();
		public abstract void close();
	}

	public abstract static class Entry {
		public abstract ClassFile find(String name) throws IOException;
	}
	
	private static class ByteBasedClassFile extends ClassFile {

		private byte[] bytes;
		private ByteArrayInputStream bais;
		private String path;
		
		public ByteBasedClassFile(byte[] bytes, String path) {
			this.bytes = bytes;			
			this.path = path;
		}
		
		@Override
		public InputStream getInputStream() throws IOException {
			this.bais = new ByteArrayInputStream(bytes);
			return this.bais;
		}

		@Override
		public String getPath() {
			return this.path;
		}

		@Override
		public void close() {
			if (this.bais!=null) {
				try {
					this.bais.close();
				} catch (IOException e) {
				}
				this.bais = null;
			}
		}
		
	}

	private static class FileClassFile extends ClassFile {
		private File file;
		private FileInputStream fis;

		public FileClassFile(File file) {
			this.file = file;
		}

		public InputStream getInputStream() throws IOException {
			fis = new FileInputStream(file);
			return fis;
		}

		public void close() {
			try {
				if (fis != null)
					fis.close();
			} catch (IOException ioe) {
				throw new BCException("Can't close class file : " + file.getName(), ioe);
			} finally {
				fis = null;
			}
		}

		public String getPath() {
			return file.getPath();
		}
	}

	public class DirEntry extends Entry {
		private String dirPath;

		public DirEntry(File dir) {
			this.dirPath = dir.getPath();
		}

		public DirEntry(String dirPath) {
			this.dirPath = dirPath;
		}

		public ClassFile find(String name) {
			File f = new File(dirPath + File.separator + name.replace('.', File.separatorChar) + ".class");
			if (f.isFile())
				return new FileClassFile(f);
			else
				return null;
		}

		public String toString() {
			return dirPath;
		}
	}

	private static class ZipEntryClassFile extends ClassFile {
		private ZipEntry entry;
		private ZipFileEntry zipFile;
		private InputStream is;

		public ZipEntryClassFile(ZipFileEntry zipFile, ZipEntry entry) {
			this.zipFile = zipFile;
			this.entry = entry;
		}

		public InputStream getInputStream() throws IOException {
			is = zipFile.getZipFile().getInputStream(entry);
			return is;
		}

		public void close() {
			try {
				if (is != null)
					is.close();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				is = null;
			}
		}

		public String getPath() {
			return entry.getName();
		}

	}
	
	/**
	 * Maintains a shared package cache for java runtime image. This maps packages (for example:
	 * java/lang) to a starting root position in the filesystem (for example: /modules/java.base/java/lang).
	 * When searching for a type we work out the package name, use it to find where in the filesystem
	 * to start looking then run from there. Once found we do cache what we learn to make subsequent
	 * lookups of that type even faster.
	 */
	public static class JImageEntry extends Entry {
		
		private final static FileSystem fs = FileSystems.getFileSystem(JRT_URI);
		private final static Map<String, Path> fileCache = new HashMap<>();
		private final static Map<String, Path> packageCache = new HashMap<>();
		private static boolean packageCacheInitialized = false;

		public JImageEntry() {
			buildPackageMap();
		}
		
		class PackageCacheConstructionVisitor extends SimpleFileVisitor<Path> {
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				if (file.getNameCount() > 3 && file.toString().endsWith(".class")) {
					int fnc = file.getNameCount();
					if (fnc > 3) { // There is a package name - e.g. /modules/java.base/java/lang/Object.class
						Path packagePath = file.subpath(2, fnc-1);
						String packagePathString = packagePath.toString();
	//					System.out.println("adding entry "+packagePath+" "+file.subpath(0, fnc-1));
	//					if (packageMap.get(packagePath) != null && !packageMap.get(packagePath).equals(file.subpath(0, 2))) {
	//						throw new IllegalStateException("Found "+packageMap.get(packagePath)+" for path "+file);
	//					}
						packageCache.put(packagePathString, file.subpath(0, fnc-1));
					}
				}
				return FileVisitResult.CONTINUE;
			}
		}
		
		/**
		 * Create a map from package names to the root area of the relevant filesystem (e.g.
		 * java/lang -> /modules/java.base).
		 */
		private synchronized void buildPackageMap() {
			if (!packageCacheInitialized) {
				packageCacheInitialized = true;
//				long s = System.currentTimeMillis();
				Iterable<java.nio.file.Path> roots = fs.getRootDirectories();
				PackageCacheConstructionVisitor visitor = new PackageCacheConstructionVisitor();
				try {
					for (java.nio.file.Path path : roots) {
						Files.walkFileTree(path, visitor);
		 			}
				}
				catch (IOException e) {
					throw new RuntimeException(e);
				}
//				System.out.println("Time to build package map: "+(System.currentTimeMillis()-s)+"ms");
			}
		}
		
		class TypeLocator extends SimpleFileVisitor<Path> {
			
			private String name;
			public Path found;
			public int filesSearchedCount;

			public TypeLocator(String name) {
				this.name = name;
			}

			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				filesSearchedCount++;
				if (file.getNameCount() > 2 && file.toString().endsWith(".class")) {
					int fnc = file.getNameCount();
					Path filePath = file.subpath(2, fnc);
					String filePathString = filePath.toString();
					if (filePathString.equals(name)) {
						fileCache.put(filePathString, file);
						found = file;
						return FileVisitResult.TERMINATE;
					}
				}
				return FileVisitResult.CONTINUE;
			}
		}
		
		private Path searchForFileAndCache(final Path startPath, final String name) {
			TypeLocator locator = new TypeLocator(name);
			try {
				Files.walkFileTree(startPath, locator);
			}
			catch (IOException e) {
				throw new RuntimeException(e);
			}
			return locator.found;
 		}

		public ClassFile find(String name) throws IOException {
			String fileName = name.replace('.', '/') + ".class";
			Path p = fileCache.get(fileName);
			if (p == null) {
				// Check the packages map to see if we know about this package
				int idx = fileName.lastIndexOf('/');
				if (idx == -1) {
					return null;
				}
				Path packageStart = null;
				String packageName = null;
				if (idx !=-1 ) {
					packageName = fileName.substring(0, idx);
					packageStart = packageCache.get(packageName);
					if (packageStart != null) {
						p = searchForFileAndCache(packageStart, fileName);
					}
				}
 			}
			if (p == null) {
				return null;
			}
			byte[] bs = Files.readAllBytes(p);
			ClassFile cf = new ByteBasedClassFile(bs, fileName);
			return cf;
		}

	}

	public class ZipFileEntry extends Entry {
		private File file;
		private ZipFile zipFile;

		public ZipFileEntry(File file) throws IOException {
			this.file = file;
		}

		public ZipFileEntry(ZipFile zipFile) {
			this.zipFile = zipFile;
		}

		public ZipFile getZipFile() {
			return zipFile;
		}

		public ClassFile find(String name) throws IOException {
			ensureOpen();
			String key = name.replace('.', '/') + ".class";
			ZipEntry entry = zipFile.getEntry(key);
			if (entry != null)
				return new ZipEntryClassFile(this, entry);
			else
				return null; // This zip will be closed when necessary...
		}

		public List<ZipEntryClassFile> getAllClassFiles() throws IOException {
			ensureOpen();
			List<ZipEntryClassFile> ret = new ArrayList<ZipEntryClassFile>();
			for (Enumeration<? extends ZipEntry> e = zipFile.entries(); e.hasMoreElements();) {
				ZipEntry entry = e.nextElement();
				String name = entry.getName();
				if (hasClassExtension(name))
					ret.add(new ZipEntryClassFile(this, entry));
			}
			// if (ret.isEmpty()) close();
			return ret;
		}

		private void ensureOpen() throws IOException {
			if (zipFile != null && openArchives.contains(zipFile)) {
				if (isReallyOpen())
					return;
			}
			if (openArchives.size() >= maxOpenArchives) {
				closeSomeArchives(openArchives.size() / 10); // Close 10% of
																// those open
			}
			zipFile = new ZipFile(file);
			if (!isReallyOpen()) {
				throw new FileNotFoundException("Can't open archive: " + file.getName() + " (size() check failed)");
			}
			openArchives.add(zipFile);
		}

		private boolean isReallyOpen() {
			try {
				zipFile.size(); // this will fail if the file has been closed
								// for
				// some reason;
				return true;
			} catch (IllegalStateException ex) {
				// this means the zip file is closed...
				return false;
			}

		}

		public void closeSomeArchives(int n) {
			for (int i = n - 1; i >= 0; i--) {
				ZipFile zf = (ZipFile) openArchives.get(i);
				try {
					zf.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				openArchives.remove(i);
			}
		}

		public void close() {
			if (zipFile == null)
				return;
			try {
				openArchives.remove(zipFile);
				zipFile.close();
			} catch (IOException ioe) {
				throw new BCException("Can't close archive: " + file.getName(), ioe);
			} finally {
				zipFile = null;
			}
		}

		public String toString() {
			return file.getName();
		}
	}

	/* private */static boolean hasClassExtension(String name) {
		return name.toLowerCase().endsWith((".class"));
	}

	public void closeArchives() {
		for (Entry entry : entries) {
			if (entry instanceof ZipFileEntry) {
				((ZipFileEntry) entry).close();
			}
			openArchives.clear();
		}
	}

	// Copes with the security manager
	private static String getSystemPropertyWithoutSecurityException(String aPropertyName, String aDefaultValue) {
		try {
			return System.getProperty(aPropertyName, aDefaultValue);
		} catch (SecurityException ex) {
			return aDefaultValue;
		}
	}
}
