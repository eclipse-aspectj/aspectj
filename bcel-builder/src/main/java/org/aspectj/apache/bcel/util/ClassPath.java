/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001, 2017 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache BCEL" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache BCEL", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */
package org.aspectj.apache.bcel.util;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URI;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Responsible for loading (class) files from the CLASSPATH. Inspired by
 * sun.tools.ClassPath.
 *
 * @author M. Dahm
 * @author Mario Ivankovits
 * @author Andy Clement
 */
public class ClassPath implements Serializable {
	private static final String JRT_FS = "jrt-fs.jar";

	private static ClassPath SYSTEM_CLASS_PATH = null;

	private PathEntry[] paths;
	private String class_path;

	public static ClassPath getSystemClassPath() {
		if (SYSTEM_CLASS_PATH == null) {
			SYSTEM_CLASS_PATH = new ClassPath();
		}
		return SYSTEM_CLASS_PATH;
	}

	/**
	 * Search for classes in given path.
	 */
	public ClassPath(String class_path) {
		this.class_path = class_path;

		List<PathEntry> vec = new ArrayList<>();

		for (StringTokenizer tok = new StringTokenizer(class_path, System.getProperty("path.separator")); tok
				.hasMoreTokens();) {
			String path = tok.nextToken();

			if (!path.equals("")) {
				File file = new File(path);

				try {
					if (file.exists()) {
						if (file.isDirectory()) {
							vec.add(new Dir(path));
						} else if (file.getName().endsWith("jrt-fs.jar")) { // TODO a bit crude...
							vec.add(new JImage());
						} else {
							vec.add(new Zip(new ZipFile(file)));
						}
					}
				} catch (IOException e) {
					System.err.println("CLASSPATH component " + file + ": " + e);
				}
			}
		}

		paths = new PathEntry[vec.size()];
		vec.toArray(paths);
	}

	/**
	 * Search for classes in CLASSPATH.
	 *
	 * @deprecated Use SYSTEM_CLASS_PATH constant
	 */
	@Deprecated
	public ClassPath() {
		this(getClassPath());
	}

	/**
	 * @return used class path string
	 */
	@Override
	public String toString() {
		return class_path;
	}

	@Override
	public int hashCode() {
		return class_path.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof ClassPath) {
			return class_path.equals(((ClassPath) o).class_path);
		}

		return false;
	}

	private static final void getPathComponents(String path, List<String> list) {
		if (path != null) {
			StringTokenizer tok = new StringTokenizer(path, File.pathSeparator);

			while (tok.hasMoreTokens()) {
				String name = tok.nextToken();
				File file = new File(name);

				if (file.exists())
					list.add(name);
			}
		}
	}

	/**
	 * Checks for class path components in the following properties:
	 * "java.class.path", "sun.boot.class.path", "java.ext.dirs"
	 *
	 * @return class path as used by default by BCEL
	 */
	public static final String getClassPath() {
		String class_path = System.getProperty("java.class.path");
		String boot_path = System.getProperty("sun.boot.class.path");
		String ext_path = System.getProperty("java.ext.dirs");
		String vm_version = System.getProperty("java.version");

		ArrayList<String> list = new ArrayList<>();

		getPathComponents(class_path, list);
		getPathComponents(boot_path, list);

		ArrayList<String> dirs = new ArrayList<>();
		getPathComponents(ext_path, dirs);

		for (String string : dirs) {
			File ext_dir = new File(string);
			String[] extensions = ext_dir.list(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					name = name.toLowerCase();
					return name.endsWith(".zip") || name.endsWith(".jar");
				}
			});

			if (extensions != null)
				for (String extension : extensions)
					list.add(ext_dir.toString() + File.separatorChar + extension);
		}

		StringBuffer buf = new StringBuffer();

		for (Iterator<String> e = list.iterator(); e.hasNext();) {
			buf.append(e.next());

			if (e.hasNext())
				buf.append(File.pathSeparatorChar);
		}

		// On Java9 the sun.boot.class.path won't be set. System classes accessible through JRT filesystem
		if (vm_version.startsWith("9") || vm_version.startsWith("10")
				|| vm_version.startsWith("11")
				|| vm_version.startsWith("12")
				|| vm_version.startsWith("13")
				|| vm_version.startsWith("14")) {
			buf.insert(0, File.pathSeparatorChar);
			buf.insert(0, System.getProperty("java.home") + File.separator + "lib" + File.separator + JRT_FS);
		}

		return buf.toString().intern();
	}

	/**
	 * @param name
	 *            fully qualified class name, e.g. java.lang.String
	 * @return input stream for class
	 */
	public InputStream getInputStream(String name) throws IOException {
		return getInputStream(name, ".class");
	}

	/**
	 * Return stream for class or resource on CLASSPATH.
	 *
	 * @param name
	 *            fully qualified file name, e.g. java/lang/String
	 * @param suffix
	 *            file name ends with suff, e.g. .java
	 * @return input stream for file on class path
	 */
	public InputStream getInputStream(String name, String suffix) throws IOException {
		InputStream is = null;

		try {
			is = getClass().getClassLoader().getResourceAsStream(name + suffix);
		} catch (Exception e) {
		}

		if (is != null)
			return is;

		return getClassFile(name, suffix).getInputStream();
	}

	/**
	 * @param name
	 *            fully qualified file name, e.g. java/lang/String
	 * @param suffix
	 *            file name ends with suff, e.g. .java
	 * @return class file for the java class
	 */
	public ClassFile getClassFile(String name, String suffix) throws IOException {
		for (PathEntry path : paths) {
			ClassFile cf;

			if ((cf = path.getClassFile(name, suffix)) != null)
				return cf;
		}

		throw new IOException("Couldn't find: " + name + suffix);
	}

	/**
	 * @param name
	 *            fully qualified class name, e.g. java.lang.String
	 * @return input stream for class
	 */
	public ClassFile getClassFile(String name) throws IOException {
		return getClassFile(name, ".class");
	}

	/**
	 * @param name
	 *            fully qualified file name, e.g. java/lang/String
	 * @param suffix
	 *            file name ends with suffix, e.g. .java
	 * @return byte array for file on class path
	 */
	public byte[] getBytes(String name, String suffix) throws IOException {
		InputStream is = getInputStream(name, suffix);

		if (is == null)
			throw new IOException("Couldn't find: " + name + suffix);

		DataInputStream dis = new DataInputStream(is);
		byte[] bytes = new byte[is.available()];
		dis.readFully(bytes);
		dis.close();
		is.close();

		return bytes;
	}

	/**
	 * @return byte array for class
	 */
	public byte[] getBytes(String name) throws IOException {
		return getBytes(name, ".class");
	}

	/**
	 * @param name
	 *            name of file to search for, e.g. java/lang/String.java
	 * @return full (canonical) path for file
	 */
	public String getPath(String name) throws IOException {
		int index = name.lastIndexOf('.');
		String suffix = "";

		if (index > 0) {
			suffix = name.substring(index);
			name = name.substring(0, index);
		}

		return getPath(name, suffix);
	}

	/**
	 * @param name
	 *            name of file to search for, e.g. java/lang/String
	 * @param suffix
	 *            file name suffix, e.g. .java
	 * @return full (canonical) path for file, if it exists
	 */
	public String getPath(String name, String suffix) throws IOException {
		return getClassFile(name, suffix).getPath();
	}

	private static abstract class PathEntry implements Serializable {
		abstract ClassFile getClassFile(String name, String suffix) throws IOException;
	}

	/**
	 * Contains information about file/ZIP entry of the Java class.
	 */
	public interface ClassFile {
		/**
		 * @return input stream for class file.
		 */
		InputStream getInputStream() throws IOException;

		/**
		 * @return canonical path to class file.
		 */
		String getPath();

		/**
		 * @return base path of found class, i.e. class is contained relative to
		 *         that path, which may either denote a directory, or zip file
		 */
		String getBase();

		/**
		 * @return modification time of class file.
		 */
		long getTime();

		/**
		 * @return size of class file.
		 */
		long getSize();
	}

	private static class Dir extends PathEntry {
		private String dir;

		Dir(String d) {
			dir = d;
		}

		@Override
		ClassFile getClassFile(String name, String suffix) throws IOException {
			final File file = new File(dir + File.separatorChar + name.replace('.', File.separatorChar) + suffix);

			return file.exists() ? new ClassFile() {
				@Override
				public InputStream getInputStream() throws IOException {
					return new FileInputStream(file);
				}

				@Override
				public String getPath() {
					try {
						return file.getCanonicalPath();
					} catch (IOException e) {
						return null;
					}

				}

				@Override
				public long getTime() {
					return file.lastModified();
				}

				@Override
				public long getSize() {
					return file.length();
				}

				@Override
				public String getBase() {
					return dir;
				}

			} : null;
		}

		@Override
		public String toString() {
			return dir;
		}
	}

	private static class JImage extends PathEntry {

		private static URI JRT_URI = URI.create("jrt:/"); //$NON-NLS-1$
		private static String MODULES_PATH = "modules"; //$NON-NLS-1$
		private static String JAVA_BASE_PATH = "java.base"; //$NON-NLS-1$

		private java.nio.file.FileSystem fs;
		private final Map<String, Path> fileMap;

		JImage() {
			fs = FileSystems.getFileSystem(JRT_URI);
			fileMap = buildFileMap();
		}

		private Map<String, Path> buildFileMap() {
			final Map<String, Path> fileMap = new HashMap<>();
			final java.nio.file.PathMatcher matcher = fs.getPathMatcher("glob:*.class");
			Iterable<java.nio.file.Path> roots = fs.getRootDirectories();
			for (java.nio.file.Path path : roots) {
				try {
					Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
						@Override
						public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
							if (file.getNameCount() > 2
									&& matcher.matches(file.getFileName())) {
								Path classPath = file.subpath(2, file.getNameCount());
								fileMap.put(classPath.toString(), file);
							}

							return FileVisitResult.CONTINUE;
						}
					});
				}
				catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
			return fileMap;
		}

		private static class ByteBasedClassFile implements ClassFile {

			private byte[] bytes;
			private ByteArrayInputStream bais;
			private String path;
			private String base;
			private long time;
			private long size;

			public ByteBasedClassFile(byte[] bytes, String path, String base, long time, long size) {
				this.bytes = bytes;
				this.path = path;
				this.base = base;
				this.time = time;
				this.size = size;
			}

			@Override
			public InputStream getInputStream() throws IOException {
				// TODO too costly to keep these in inflated form in memory?
				this.bais = new ByteArrayInputStream(bytes);
				return this.bais;
			}

			@Override
			public String getPath() {
				return this.path;
			}

			@Override
			public String getBase() {
				return this.base;
			}

			@Override
			public long getTime() {
				return this.time;
			}

			@Override
			public long getSize() {
				return this.size;
			}

		}

		@Override
		ClassFile getClassFile(String name, String suffix) throws IOException {
			// Class files are in here under names like this:
			//   /modules/java.base/java/lang/Object.class (jdk9 b74)
			// so within a modules top level qualifier and then the java.base module
			String fileName = name.replace('.', '/') + suffix;
			Path p = fileMap.get(fileName);
			if (p == null) {
				return null;
			}
			byte[] bs = Files.readAllBytes(p);
			BasicFileAttributeView bfav = Files.getFileAttributeView(p, BasicFileAttributeView.class);
			BasicFileAttributes bfas = bfav.readAttributes();
			long time = bfas.lastModifiedTime().toMillis();
			long size = bfas.size();
			ClassFile cf = new ByteBasedClassFile(bs, "jimage",fileName,time,size);
			return cf;
		}
	}

	private static class Zip extends PathEntry {
		private ZipFile zip;

		Zip(ZipFile z) {
			zip = z;
		}

		@Override
		ClassFile getClassFile(String name, String suffix) throws IOException {
			final ZipEntry entry = zip.getEntry(name.replace('.', '/') + suffix);

			return (entry != null) ? new ClassFile() {
				@Override
				public InputStream getInputStream() throws IOException {
					return zip.getInputStream(entry);
				}

				@Override
				public String getPath() {
					return entry.toString();
				}

				@Override
				public long getTime() {
					return entry.getTime();
				}

				@Override
				public long getSize() {
					return entry.getSize();
				}

				@Override
				public String getBase() {
					return zip.getName();
				}
			} : null;
		}
	}
}
