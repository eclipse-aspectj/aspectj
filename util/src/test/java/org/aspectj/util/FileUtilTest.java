/* *******************************************************************
 * Copyright (c) 1999-2001 Xerox Corporation,
 *               2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Xerox/PARC     initial implementation
 * ******************************************************************/

package org.aspectj.util;

//import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.StringBufferInputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import junit.framework.TestCase;
import junit.textui.TestRunner;

/**
 *
 */
@SuppressWarnings("deprecation")
public class FileUtilTest extends TestCase {
	public static final String[] NONE = new String[0];
	public static boolean log = false;

	public static void main(String[] args) {
		TestRunner.main(new String[] { "org.aspectj.util.FileUtilTest" });
	}

	public static void assertSame(String prefix, String[] lhs, String[] rhs) { // XXX cheap diff
		String srcPaths = LangUtil.arrayAsList(lhs).toString();
		String destPaths = LangUtil.arrayAsList(rhs).toString();
		if (!srcPaths.equals(destPaths)) {
			log("expected: " + srcPaths);
			log("  actual: " + destPaths);
			assertTrue(prefix + " expected=" + srcPaths + " != actual=" + destPaths, false);
		}
	}

	/**
	 * Verify that dir contains files with names, and return the names of other files in dir.
	 *
	 * @return the contents of dir after excluding files or NONE if none
	 * @throws AssertionFailedError if any names are not in dir
	 */
	public static String[] dirContains(File dir, final String[] filenames) {
		final List<String> sought = new ArrayList<>(LangUtil.arrayAsList(filenames));
		FilenameFilter filter = new FilenameFilter() {
			public boolean accept(File d, String name) {
				return !sought.remove(name);
			}
		};
		// remove any found from sought and return remainder
		String[] found = dir.list(filter);
		if (0 < sought.size()) {
			assertTrue("found " + LangUtil.arrayAsList(dir.list()).toString() + " expected " + sought, false);
		}
		return (found.length == 0 ? NONE : found);
	}

	/** @return sorted String[] of all paths to all files/dirs under dir */
	public static String[] dirPaths(File dir) {
		return dirPaths(dir, new String[0]);
	}

	/**
	 * Get a sorted String[] of all paths to all files/dirs under dir. Files with names starting with "." are ignored, as are
	 * directory paths containing "CVS". The directory prefix of the path is stripped. Thus, given directory:
	 *
	 * <pre>
	 * path/to
	 *   .cvsignore
	 *   CVS/
	 *     Root
	 *     Repository
	 *   Base.java
	 *   com/
	 *     parc/
	 *       messages.properties
	 *   org/
	 *     aspectj/
	 *       Version.java
	 * </pre>
	 *
	 * a call
	 *
	 * <pre>
	 * dirPaths(new File(&quot;path/to&quot;), new String[0]);
	 * </pre>
	 *
	 * returns
	 *
	 * <pre>
	 * { &quot;Base.java&quot;, &quot;com/parc/messages.properties&quot;, &quot;org/aspectj/Version.java&quot; }
	 * </pre>
	 *
	 * while the call
	 *
	 * <pre>
	 * dirPaths(new File(&quot;path/to&quot;), new String[] { &quot;.java&quot; });
	 * </pre>
	 *
	 * returns
	 *
	 * <pre>
	 * { &quot;Base.java&quot;, &quot;org/aspectj/Version.java&quot; }
	 * </pre>
	 *
	 * @param dir the File path to the directory to inspect
	 * @param suffixes if not empty, require files returned to have this suffix
	 * @return sorted String[] of all paths to all files under dir ending with one of the listed suffixes but not starting with "."
	 */
	public static String[] dirPaths(File dir, String[] suffixes) {
		ArrayList<String> result = new ArrayList<>();
		doDirPaths(dir, result);
		// if suffixes required, remove those without suffixes
		if (!LangUtil.isEmpty(suffixes)) {
			for (ListIterator<String> iter = result.listIterator(); iter.hasNext();) {
				String path = iter.next().toString();
				boolean hasSuffix = false;
				for (int i = 0; !hasSuffix && (i < suffixes.length); i++) {
					hasSuffix = path.endsWith(suffixes[i]);
				}
				if (!hasSuffix) {
					iter.remove();
				}
			}
		}
		Collections.sort(result);
		// trim prefix
		final String prefix = dir.getPath();
		final int len = prefix.length() + 1; // plus directory separator
		String[] ra = result.toArray(new String[0]);
		for (int i = 0; i < ra.length; i++) {
			// assertTrue(ra[i].startsWith(prefix));
			assertTrue(ra[i], ra[i].length() > len);
			ra[i] = ra[i].substring(len);
		}
		return ra;
	}

	/**
	 * @param dir the File to read - ignored if null, not a directory, or has "CVS" in its path
	 * @param useSuffix if true, then use dir as suffix to path
	 */
	private static void doDirPaths(File dir, ArrayList<String> paths) {
		if ((null == dir) || !dir.canRead() || (dir.getPath().contains("CVS"))) {
			return;
		}
		File[] files = dir.listFiles();
		for (File file : files) {
			String path = file.getPath();
			if (!file.getName().startsWith(".")) {
				if (file.isFile()) {
					paths.add(path);
				} else if (file.isDirectory()) {
					doDirPaths(file, paths);
				} else {
					log("not file or dir: " + dir + "/" + path);
				}
			}
		}
	}

	/** Print s if logging is enabled */
	private static void log(String s) {
		if (log) {
			System.err.println(s);
		}
	}

	/** List of File files or directories to delete when exiting */
	final List<File> tempFiles;

	public FileUtilTest(String s) {
		super(s);
		tempFiles = new ArrayList<>();
	}

	public void tearDown() {
		for (ListIterator<File> iter = tempFiles.listIterator(); iter.hasNext();) {
			File dir = iter.next();
			log("removing " + dir);
			FileUtil.deleteContents(dir);
			dir.delete();
			iter.remove();
		}
	}

	public void testNotIsFileIsDirectory() {
		File noSuchFile = new File("foo");
		assertTrue(!noSuchFile.isFile());
		assertTrue(!noSuchFile.isDirectory());
	}

	public void testGetBestFile() {
		assertNull(FileUtil.getBestFile((String[]) null));
		assertNull(FileUtil.getBestFile(new String[0]));
		assertNull(FileUtil.getBestFile(new String[] { "!" }));
		File f = FileUtil.getBestFile(new String[] { "." });
		assertNotNull(f);
		f = FileUtil.getBestFile(new String[] { "!", "." });
		assertNotNull(f);
		assertTrue(f.canRead());
		boolean setProperty = false;
		try {
			System.setProperty("bestfile", ".");
			setProperty = true;
		} catch (Throwable t) {
			// ignore Security, etc.
		}
		if (setProperty) {
			f = FileUtil.getBestFile(new String[] { "sp:bestfile" });
			assertNotNull(f);
			assertTrue(f.canRead());
		}
	}

	public void testCopyFiles() {
		// bad input
		Class<?> iaxClass = IllegalArgumentException.class;

		checkCopyFiles(null, null, iaxClass, false);

		File noSuchFile = new File("foo");
		checkCopyFiles(noSuchFile, null, iaxClass, false);
		checkCopyFiles(noSuchFile, noSuchFile, iaxClass, false);

		File tempDir = FileUtil.getTempDir("testCopyFiles");
		tempFiles.add(tempDir);
		File fromFile = new File(tempDir, "fromFile");
		String err = FileUtil.writeAsString(fromFile, "contents of from file");
		assertTrue(err, null == err);
		checkCopyFiles(fromFile, null, iaxClass, false);
		checkCopyFiles(fromFile, fromFile, iaxClass, false);

		// file-file
		File toFile = new File(tempDir, "toFile");
		checkCopyFiles(fromFile, toFile, null, true);

		// file-dir
		File toDir = new File(tempDir, "toDir");
		assertTrue(toDir.mkdirs());
		checkCopyFiles(fromFile, toDir, null, true);

		// dir-dir
		File fromDir = new File(tempDir, "fromDir");
		assertTrue(fromDir.mkdirs());
		checkCopyFiles(fromFile, fromDir, null, false);
		File toFile2 = new File(fromDir, "toFile2");
		checkCopyFiles(fromFile, toFile2, null, false);
		checkCopyFiles(fromDir, toDir, null, true);
	}

	void checkCopyFiles(File from, File to, Class exceptionClass, boolean clean) {
		try {
			FileUtil.copyFile(from, to);
			assertTrue(null == exceptionClass);
			if (to.isFile()) {
				assertTrue(from.length() == to.length()); // XXX cheap test
			} else if (!from.isDirectory()) {
				File toFile = new File(to, from.getName());
				assertTrue(from.length() == toFile.length());
			} else {
				// from is a dir and to is a dir, toDir should be created, and have the
				// same contents as fromDir.
				assertTrue(to.exists());
				assertTrue(from.listFiles().length == to.listFiles().length);
			}
		} catch (Throwable t) {
			assertTrue(null != exceptionClass);
			assertTrue(exceptionClass.isAssignableFrom(t.getClass()));
		} finally {
			if (clean && (null != to) && (to.exists())) {
				if (to.isDirectory()) {
					FileUtil.deleteContents(to);
				}
				to.delete();
			}
		}
	}

	public void testDirCopySubdirs() throws IOException {
		File srcDir = new File("src");
		File destDir = FileUtil.getTempDir("testDirCopySubdirs");
		tempFiles.add(destDir);
		FileUtil.copyDir(srcDir, destDir);
		assertSame("testDirCopySubdirs", dirPaths(srcDir), dirPaths(destDir));
	}

	public void testDirCopySubdirsSuffix() throws IOException {
		File srcDir = new File("src");
		File destDir = FileUtil.getTempDir("testDirCopySubdirsSuffix");
		tempFiles.add(destDir);
		FileUtil.copyDir(srcDir, destDir, ".java", ".aj");

		String[] sources = dirPaths(srcDir, new String[] { ".java" });
		for (int i = 0; i < sources.length; i++) {
			sources[i] = sources[i].substring(0, sources[i].length() - 4);
		}
		String[] sinks = dirPaths(destDir, new String[] { ".aj" });
		for (int i = 0; i < sinks.length; i++) {
			sinks[i] = sinks[i].substring(0, sinks[i].length() - 2);
		}
		assertSame("testDirCopySubdirs", sources, sinks);
	}

	public void testGetURL() {
		String[] args = new String[] { ".", "../util/testdata", "../lib/test/aspectjrt.jar" };
		for (String arg : args) {
			checkGetURL(arg);
		}
	}

	/**
	 * Method checkGetURL.
	 *
	 * @param string
	 * @param uRL
	 */
	private void checkGetURL(String arg) {
		assertTrue(null != arg);
		File f = new File(arg);
		URL url = FileUtil.getFileURL(f);
		assertTrue(null != url);
		log("url       " + url);
		if (!f.exists()) {
			log("not exist        " + f);
		} else if (f.isDirectory()) {
			log("directory        " + f);
		} else {
			log("     file        " + f);
			InputStream in = null;
			try {
				in = url.openStream();
			} catch (IOException e) {
				assertTrue("IOException: " + e, false);
			} finally {
				if (null != in) {
					try {
						in.close();
					} catch (IOException e) {
					}
				}
			}
		}
	}

	public void testGetTempDir() {
		boolean pass = true;
		boolean delete = true;
		checkGetTempDir("parent", null, pass, delete);
		checkGetTempDir(null, "child", pass, delete);
		tempFiles.add(checkGetTempDir("parent", "child", pass, !delete).getParentFile());
		tempFiles.add(checkGetTempDir("parent", "child", pass, !delete).getParentFile());
		tempFiles.add(checkGetTempDir("parent", "child", pass, !delete).getParentFile());
	}

	File checkGetTempDir(String parent, String child, boolean ok, boolean delete) {
		File parentDir = FileUtil.getTempDir(parent);
		assertTrue("unable to create " + parent, null != parentDir);
		File dir = FileUtil.makeNewChildDir(parentDir, child);
		log("parent=" + parent + " child=" + child + " -> " + dir);
		assertTrue("dir: " + dir, ok == (dir.canWrite() && dir.isDirectory()));
		if (delete) {
			dir.delete();
			parentDir.delete();
		}
		return dir;
	}

	public void testRandomFileString() {
		List<String> results = new ArrayList<>();
		for (int i = 0; i < 1000; i++) {
			String s = FileUtil.randomFileString();
			if (results.contains(s)) {
				log("warning: got duplicate at iteration " + i);
			}
			results.add(s);
			// System.err.print(" " + s);
			// if (0 == (i % 5)) {
			// System.err.println("");
			// }
		}
	}

	public void testNormalizedPath() {
		File tempFile = null;
		try {
			tempFile = File.createTempFile("FileUtilTest_testNormalizedPath", "tmp");
			tempFiles.add(tempFile);
		} catch (IOException e) {
			log("aborting test - unable to create temp file");
			return;
		}
		File parentDir = tempFile.getParentFile();
		String tempFilePath = FileUtil.normalizedPath(tempFile, parentDir);
		assertEquals(tempFile.getName(), tempFilePath);
	}

	public void testFileToClassName() {

		File basedir = new File("/base/dir"); // never created
		File classFile = new File(basedir, "foo/Bar.class");
		assertEquals("foo.Bar", FileUtil.fileToClassName(basedir, classFile));

		classFile = new File(basedir, "foo\\Bar.class");
		assertEquals("foo.Bar", FileUtil.fileToClassName(basedir, classFile));

		assertEquals("Bar", FileUtil.fileToClassName(null, classFile));

		classFile = new File("/home/classes/org/aspectj/lang/JoinPoint.class");
		assertEquals("org.aspectj.lang.JoinPoint", FileUtil.fileToClassName(null, classFile));

		classFile = new File("/home/classes/com/sun/tools/Javac.class");
		assertEquals("com.sun.tools.Javac", FileUtil.fileToClassName(null, classFile));
	}

	public void testDeleteContents() {
		File tempDir = FileUtil.getTempDir("testDeleteContents");
		tempFiles.add(tempDir);
		File f = new File(tempDir, "foo");
		f.mkdirs();
		File g = new File(f, "bar");
		g.mkdirs();
		File h = new File(g, "bash");
		h.mkdirs();
		int d = FileUtil.deleteContents(f);
		assertTrue(0 == d);
		assertTrue(0 == f.list().length);
		f.delete();
		assertTrue(!f.exists());
	}

	public void testLineSeek() {
		File tempDir = FileUtil.getTempDir("testLineSeek");
		tempFiles.add(tempDir);
		File file = new File(tempDir, "testLineSeek");
		String path = file.getPath();
		String contents = "0123456789" + LangUtil.EOL;
		contents += contents;
		FileUtil.writeAsString(file, contents);
		tempFiles.add(file);
		List<String> sourceList = new ArrayList<>();
		sourceList.add(file.getPath());

		final List<String> errors = new ArrayList<>();
		final PrintStream errorSink = new PrintStream(System.err, true) {
			public void println(String error) {
				errors.add(error);
			}
		};
		for (int i = 0; i < 10; i++) {
			List<String> result = FileUtil.lineSeek("" + i, sourceList, true, errorSink);
			assertEquals(2, result.size());
			assertEquals(path + ":1:" + i, result.get(0));
			assertEquals(path + ":2:" + i, result.get(1));
			if (!LangUtil.isEmpty(errors)) { // XXX prefer fast-fail?
				assertTrue("errors: " + errors, false);
			}
		}

	}

	public void testLineSeekMore() {
		final int MAX = 3; // 1..10
		File tempDir = FileUtil.getTempDir("testLineSeekMore");
		tempFiles.add(tempDir);
		final String prefix = new File(tempDir, "testLineSeek").getPath();
		// setup files 0..MAX with 2*MAX lines
		String[] sources = new String[MAX];
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < sources.length; i++) {
			sources[i] = new File(prefix + i).getPath();
			sb.append("not matched");
			sb.append(LangUtil.EOL);
			sb.append("0123456789");
			sb.append(LangUtil.EOL);
		}
		final String contents = sb.toString();
		for (String source : sources) {
			File file = new File(source);
			FileUtil.writeAsString(file, contents);
			tempFiles.add(file);
		}
		// now test
		final List<String> errors = new ArrayList<>();
		final PrintStream errorSink = new PrintStream(System.err, true) {
			public void println(String error) {
				errors.add(error);
			}
		};
		List<String> sourceList = new ArrayList<>(Arrays.asList(sources));
		sourceList = Collections.unmodifiableList(sourceList);
		for (int k = 0; k < sources.length; k++) {
			List<String> result = FileUtil.lineSeek("" + k, sourceList, true, errorSink);
			// number k found in every other line of every file at index k
			Iterator<String> iter = result.iterator();
			for (int i = 0; i < MAX; i++) { // for each file
				for (int j = 1; j < (MAX + 1); j++) { // for every other line
					assertTrue(iter.hasNext());
					assertEquals(prefix + i + ":" + 2 * j + ":" + k, iter.next());
				}
			}
			if (!LangUtil.isEmpty(errors)) { // XXX prefer fast-fail?
				assertTrue("errors: " + errors, false);
			}
		}
	}

	public void testDirCopyNoSubdirs() throws IOException {
		String[] srcFiles = new String[] { "one.java", "two.java", "three.java" };
		String[] destFiles = new String[] { "three.java", "four.java", "five.java" };
		String[] allFiles = new String[] { "one.java", "two.java", "three.java", "four.java", "five.java" };
		File srcDir = makeTempDir("FileUtilUT_srcDir", srcFiles);
		File destDir = makeTempDir("FileUtilUT_destDir", destFiles);
		assertTrue(null != srcDir);
		assertTrue(null != destDir);
		assertTrue(NONE == dirContains(srcDir, srcFiles));
		assertTrue(NONE == dirContains(destDir, destFiles));

		FileUtil.copyDir(srcDir, destDir);
		String[] resultOne = dirContains(destDir, allFiles);
		FileUtil.copyDir(srcDir, destDir);
		String[] resultTwo = dirContains(destDir, allFiles);

		assertTrue(NONE == resultOne);
		assertTrue(NONE == resultTwo);
	}

	public void testDirCopyNoSubdirsWithSuffixes() throws IOException {
		String[] srcFiles = new String[] { "one.java", "two.java", "three.java" };
		String[] destFiles = new String[] { "three.java", "four.java", "five.java" };
		String[] allFiles = new String[] { "one.aj", "two.aj", "three.aj", "three.java", "four.java", "five.java" };
		File srcDir = makeTempDir("FileUtilUT_srcDir", srcFiles);
		File destDir = makeTempDir("FileUtilUT_destDir", destFiles);
		assertTrue(null != srcDir);
		assertTrue(null != destDir);
		assertTrue(NONE == dirContains(srcDir, srcFiles));
		assertTrue(NONE == dirContains(destDir, destFiles));

		FileUtil.copyDir(srcDir, destDir, ".java", ".aj");
		FileUtil.copyDir(srcDir, destDir, ".java", ".aj");

		assertTrue(NONE == dirContains(destDir, allFiles));
		assertTrue(NONE == dirContains(destDir, allFiles));
	}

	public void testDirCopySubdirsSuffixRoundTrip() throws IOException {
		final File srcDir = new File("src");
		final File one = FileUtil.getTempDir("testDirCopySubdirsSuffixRoundTrip_1");
		final File two = FileUtil.getTempDir("testDirCopySubdirsSuffixRoundTrip_2");
		FileUtil.copyDir(srcDir, one); // no selection
		FileUtil.copyDir(two, one, ".java", ".aj"); // only .java files
		FileUtil.copyDir(one, two, ".aj", ".java");

		FileUtil.deleteContents(one);
		one.delete();
		FileUtil.deleteContents(two);
		two.delete();
	}

	/**
	 * Create temp dir at loc containing temp files files. Result is registered for deletion on cleanup.
	 */
	File makeTempDir(String loc, String[] filenames) throws IOException {
		File d = new File(loc);
		d.mkdirs();
		assertTrue(d.exists());
		tempFiles.add(d);
		assertTrue(d.canWrite());
		for (String filename : filenames) {
			File f = new File(d, filename);
			assertTrue(filename, f.createNewFile());
		}
		return d;
	}

	public void testPipeEmpty() {
		checkPipe("");
	}

	public void testPipeMin() {
		checkPipe("0");
	}

	public void testPipe() {
		String str = "The quick brown fox jumped over the lazy dog";
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < 4096; i++) {
			sb.append(str);
		}
		checkPipe(sb.toString());
	}

	void checkPipe(String data) {
		StringBufferInputStream in = new StringBufferInputStream(data);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		FileUtil.Pipe pipe = new FileUtil.Pipe(in, out, 100l, true, true);
		pipe.run();
		assertTrue(data.equals(out.toString()));
		assertTrue(null == pipe.getThrown());
		assertEquals("totalWritten", data.length(), pipe.totalWritten());
	}

	public void testPipeThrown() {
		final String data = "The quick brown fox jumped over the lazy dog";
		final IOException thrown = new IOException("test");
		StringBufferInputStream in = new StringBufferInputStream(data);
		OutputStream out = new OutputStream() {
			public void write(int b) throws IOException {
				throw thrown;
			}
		};

		FileUtil.Pipe pipe = new FileUtil.Pipe(in, out, 100l, true, true);
		pipe.run();
		assertEquals("totalWritten", 0, pipe.totalWritten());
		assertTrue(thrown == pipe.getThrown());
	}

	public void xtestPipeHalt() { // this test periodically fails on the build machine -
									// disabling till we have time to figure out why
		final long MAX = 1000000;
		InputStream in = new InputStream() {
			long max = 0;

			public int read() throws IOException {
				if (max++ > MAX) {
					throw new IOException("test failed");
				}
				return 1;
			}

		};
		final int minWritten = 20;
		class Flag {
			boolean hit;
		}
		final Flag flag = new Flag();
		OutputStream out = new OutputStream() {
			long max = 0;

			public void write(int b) throws IOException {
				if (max++ > MAX) {
					throw new IOException("test failed");
				} else if (max > minWritten) {
					if (!flag.hit) {
						flag.hit = true;
					}
				}
			}
		};
		class Result {
			long totalWritten;
			Throwable thrown;
			boolean set;
		}
		final Result result = new Result();
		FileUtil.Pipe pipe = new FileUtil.Pipe(in, out, 100l, true, true) {
			protected void completing(long totalWritten, Throwable thrown) {
				result.totalWritten = totalWritten;
				result.thrown = thrown;
				result.set = true;
			}
		};
		// start it up
		new Thread(pipe).start();
		// wait for minWritten input
		while (!flag.hit) {
			try {
				Thread.sleep(5l);
			} catch (InterruptedException e) {
				// ignore
			}
		}
		// halt
		assertTrue(pipe.halt(true, true));
		assertTrue(result.set);
		assertTrue("Expected null but result.thrown = " + result.thrown, null == result.thrown);
		assertTrue(null == pipe.getThrown());
		assertEquals("total written", result.totalWritten, pipe.totalWritten());
		if (minWritten > pipe.totalWritten()) {
			assertTrue("written: " + pipe.totalWritten(), false);
		}
	}

}
