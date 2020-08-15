/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Xerox/PARC     initial implementation
 * ******************************************************************/

package org.aspectj.internal.tools.ant.taskdefs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.MatchingTask;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Reference;

/**
 * Check that included .java files contain license and copyright strings for MPL 1.0 (default), Apache, or CPL. Use list="true" to
 * get a list of known license variants {license}-{copyrightHolder} todo reimplement with regexp and jdiff FileLine utilities
 */
@SuppressWarnings("deprecation")
public class Checklics extends MatchingTask {
	/*
	 * This does not enforce that copyrights are correct/current, only that they exist. E.g., the default behavior requires MPL but
	 * permits either Xerox or PARC copyright holders and any valid year.
	 */
	public static final String MPL_TAG = "mpl";
	public static final String APACHE_TAG = "apache";
	public static final String CPL_IBM_PARC_TAG = "cpl-ibm|parc";
	public static final String CPL_IBM_TAG = "cpl-ibm";
	public static final String MPL_XEROX_PARC_TAG = "mpl-parc|xerox";
	public static final String MPL_ONLY_TAG = "mpl-only";
	public static final String MPL_PARC_TAG = "mpl-parc";
	public static final String PARC_COPYRIGHT_TAG = "parc-copy";
	public static final String CPL_IBM_PARC_XEROX_TAG = "cpl-ibm|parc|xerox";
	public static final String CPL_IBM_PARC_XEROX_OTHERS_TAG = "cpl-ibm|parc|xerox|others";
	public static final String EPL_CPL_IBM_PARC_XEROX_OTHERS_TAG = "epl-cpl-ibm|parc|xerox|vmware|others";
	public static final String DEFAULT = EPL_CPL_IBM_PARC_XEROX_OTHERS_TAG;

	static final Map<String,License> LICENSES; // unmodifiable Map

	static {
		final String CONTRIBUTORS = "Contributors";
		final String XEROX = "Xerox";
		final String PARC = "Palo Alto Research Center";
		final String APACHE = "The Apache Software Foundation";
		final String IBM = "IBM";
		final String VMWARE = "VMware";
		final String IBM_LONG = "International Business Machines";
		final String LIC_APL = "Apache Software Foundation (http://www.apache.org/)";
		final String LIC_MPL = "http://aspectj.org/MPL/";
		final String LIC_CPL = "Eclipse Public License";
		final String LIC_ECPL = " Public License";
		License APL = new License(APACHE_TAG, LIC_APL, APACHE);
		License MPL = new License(MPL_TAG, LIC_MPL, XEROX);
		License MPL_XEROX_PARC = new License(DEFAULT, LIC_MPL, XEROX, PARC);
		License CPL_IBM_PARC = new License(CPL_IBM_PARC_TAG, LIC_CPL, new String[] { IBM_LONG, IBM, PARC });
		License CPL_IBM_PARC_XEROX = new License(CPL_IBM_PARC_XEROX_TAG, LIC_CPL, new String[] { IBM_LONG, IBM, PARC, XEROX });

		License CPL_IBM_PARC_XEROX_OTHERS = new License(CPL_IBM_PARC_XEROX_OTHERS_TAG, LIC_CPL, new String[] { IBM_LONG, IBM, PARC,
				XEROX, CONTRIBUTORS });
		License EPL_CPL_IBM_PARC_XEROX_OTHERS = new License(EPL_CPL_IBM_PARC_XEROX_OTHERS_TAG, LIC_ECPL, new String[] { IBM_LONG,
				IBM, PARC, XEROX, VMWARE, CONTRIBUTORS });
		License CPL_IBM = new License(CPL_IBM_TAG, LIC_CPL, IBM, IBM_LONG);
		License MPL_ONLY = new License(MPL_ONLY_TAG, LIC_MPL);
		License MPL_PARC = new License(MPL_PARC_TAG, LIC_MPL, PARC);
		License PARC_COPYRIGHT = new License(PARC_COPYRIGHT_TAG, null, PARC);
		LICENSES = new Hashtable<>();
		LICENSES.put(APL.tag, APL);
		LICENSES.put(MPL.tag, MPL);
		LICENSES.put(MPL_PARC.tag, MPL_PARC);
		LICENSES.put(MPL_XEROX_PARC.tag, MPL_XEROX_PARC);
		LICENSES.put(CPL_IBM_PARC.tag, CPL_IBM_PARC);
		LICENSES.put(MPL_ONLY.tag, MPL_ONLY);
		LICENSES.put(CPL_IBM.tag, CPL_IBM);
		LICENSES.put(PARC_COPYRIGHT.tag, PARC_COPYRIGHT);
		LICENSES.put(CPL_IBM_PARC_XEROX.tag, CPL_IBM_PARC_XEROX);
		LICENSES.put(CPL_IBM_PARC_XEROX_OTHERS.tag, CPL_IBM_PARC_XEROX_OTHERS);
		LICENSES.put(EPL_CPL_IBM_PARC_XEROX_OTHERS.tag, EPL_CPL_IBM_PARC_XEROX_OTHERS);
	}

	/** @param args String[] { &lt; sourcepath &gt; {, &lt; licenseTag &gt; } } */
	public static void main(String[] args) {
		switch (args.length) {
		case 1:
			runDirect(args[0], null, false);
			break;
		case 2:
			runDirect(args[0], args[1], false);
			break;
		default:
			String options = "{replace-headers|get-years|list|{licenseTag}}";
			System.err.println("java {me} sourcepath " + options);
			break;
		}
	}

	/**
	 * Run the license check directly
	 *
	 * @param sourcepaths String[] of paths to source directories
	 * @param license the String tag for the license, if any
	 * @param failonerror boolean flag to pass to Checklics
	 * @throws IllegalArgumentException if sourcepaths is empty
	 * @return total number of failed licenses
	 */
	public static int runDirect(String sourcepath, String license, boolean failonerror) {
		if ((null == sourcepath) || (1 > sourcepath.length())) {
			throw new IllegalArgumentException("bad sourcepath: " + sourcepath);
		}
		Checklics me = new Checklics();
		Project p = new Project();
		p.setName("direct interface to Checklics");
		p.setBasedir(".");
		me.setProject(p);
		me.setFailOnError(failonerror);
		me.setSourcepath(new Path(p, sourcepath));
		if (null != license) {
			if ("replace-headers".equals(license)) {
				me.setReplaceheaders(true);
			} else if ("get-years".equals(license)) {
				me.setGetYears(true);
			} else if ("list".equals(license)) {
				me.setList(true);
			} else {
				me.setLicense(license);
			}
		}
		me.execute();
		return me.failed;
	}

	private Path sourcepath;
	private License license;
	private boolean list;
	private String streamTag;
	private boolean failOnError;
	private boolean getYears;
	private boolean replaceHeaders;
	private int failed;
	private int passed;

	private boolean printDirectories;

	/** @param list if true, don't run but list known license tags */
	public void setList(boolean list) {
		this.list = list;
	}

	public void setPrintDirectories(boolean print) {
		printDirectories = print;
	}

	/**
	 * When failOnError is true, if any file failed, throw BuildException listing number of files that file failed to pass license
	 * check
	 *
	 * @param fail if true, report errors by throwing BuildException
	 */
	public void setFailOnError(boolean fail) {
		this.failOnError = fail;
	}

	/** @param tl mpl | apache | cpl */
	public void setLicense(String tl) {
		License input = LICENSES.get(tl);
		if (null == input) {
			throw new BuildException("no license known for " + tl);
		}
		license = input;
	}

	public void setSourcepath(Path path) {
		if (sourcepath == null) {
			sourcepath = path;
		} else {
			sourcepath.append(path);
		}
	}

	public Path createSourcepath() {
		return sourcepath == null ? (sourcepath = new Path(project)) : sourcepath.createPath();
	}

	public void setSourcepathRef(Reference id) {
		createSourcepath().setRefid(id);
	}

	/** @param out "out" or "err" */
	public void setOutputStream(String out) {
		this.streamTag = out;
	}

	public void setReplaceheaders(boolean replaceHeaders) {
		this.replaceHeaders = replaceHeaders;
	}

	public void setGetYears(boolean getYears) {
		this.getYears = getYears;
	}

	/** list known licenses or check source tree */
	@Override
	public void execute() throws BuildException {
		if (list) {
			list();
		} else if (replaceHeaders) {
			replaceHeaders();
		} else if (getYears) {
			getYears();
		} else {
			checkLicenses();
		}
	}

	private PrintStream getOut() {
		return ("err".equals(streamTag) ? System.err : System.out);
	}

	interface FileVisitor {
		void visit(File file);
	}

	/** visit all .java files in all directories... */
	private void visitAll(FileVisitor visitor) {
		// List filelist = new ArrayList();
		String[] dirs = sourcepath.list();
		for (String dir2 : dirs) {
			File dir = project.resolveFile(dir2);
			String[] files = getDirectoryScanner(dir).getIncludedFiles();
			for (String file2 : files) {
				File file = new File(dir, file2);
				String path = file.getPath();
				if (path.endsWith(".java")) {
					visitor.visit(file);
				}
			}
		}
	}

	private void replaceHeaders() {
		class YearVisitor implements FileVisitor {
			@Override
			public void visit(File file) {
				HeaderInfo info = Header.checkFile(file);
				if (!Header.replaceHeader(file, info)) {
					throw new BuildException("failed to replace header for " + file + " using " + info);
				}
			}
		}
		visitAll(new YearVisitor());
	}

	private void getYears() {
		final PrintStream out = getOut();
		class YearVisitor implements FileVisitor {
			@Override
			public void visit(File file) {
				HeaderInfo info = Header.checkFile(file);
				out.println(info.toString());
			}
		}
		visitAll(new YearVisitor());
	}

	private void checkLicenses() throws BuildException {
		if (null == license) {
			setLicense(DEFAULT);
		}
		final License license = this.license; // being paranoid...
		if (null == license) {
			throw new BuildException("no license");
		}
		final PrintStream out = getOut();

		class Visitor implements FileVisitor {
			int failed = 0;
			int passed = 0;

			@Override
			public void visit(File file) {
				if (license.checkFile(file)) {
					passed++;
				} else {
					failed++;
					String path = file.getPath();
					if (!license.foundLicense()) {
						out.println(license.tag + "   LICENSE FAIL: " + path);
					}
					if (!license.foundCopyright()) {
						out.println(license.tag + " COPYRIGHT FAIL: " + path);
					}
				}
			}
		}
		Visitor visitor = new Visitor();
		visitAll(visitor);
		this.failed = visitor.failed;
		this.passed = visitor.passed;
		if (0 < visitor.failed) {
			getOut().println("Total passed: " + visitor.passed + (visitor.failed == 0 ? "" : " failed: " + visitor.failed));
			if (failOnError) {
				throw new BuildException(failed + " files failed license check");
			}
		}
	}

	private void list() {
		Iterator enu = LICENSES.keySet().iterator();
		StringBuffer sb = new StringBuffer();
		sb.append("known license keys:");
		boolean first = true;
		while (enu.hasNext()) {
			sb.append((first ? " " : ", ") + enu.next());
			if (first) {
				first = false;
			}
		}
		getOut().println(sb.toString());
	}

	/**
	 * Encapsulate license and copyright specifications to check files use hokey string matching.
	 */
	public static class License {
		/** acceptable years for copyright prefix to company - append " " */
		static final String[] YEARS = // remove older after license xfer?
				new String[] { "2002 ", "2003 ", "2004 ", "2005", "2006", "2007", "2008",
						"2009", "2010", "2011", "2012", "2013", "2014", "2015", "2016", "2017", "2018", "2019", "2020", "2001 ", "2000 ",
		"1999 " };
		public final String tag;
		public final String license;
		private final String[] copyright;
		private boolean gotLicense;
		private boolean gotCopyright;

		License(String tag, String license) {
			this(tag, license, (String[]) null);
		}

		License(String tag, String license, String copyright) {
			this(tag, license, new String[] { copyright });
		}

		License(String tag, String license, String copyright, String altCopyright) {
			this(tag, license, new String[] { copyright, altCopyright });
		}

		License(String tag, String license, String[] copyright) {
			this.tag = tag;
			if ((null == tag) || (0 == tag.length())) {
				throw new IllegalArgumentException("null tag");
			}
			this.license = license;
			this.copyright = copyright;
		}

		public final boolean gotValidFile() {
			return foundLicense() && foundCopyright();
		}

		/** @return true if no license sought or if some license found */
		public final boolean foundLicense() {
			return ((null == license) || gotLicense);
		}

		/** @return true if no copyright sought or if some copyright found */
		public final boolean foundCopyright() {
			return ((null == copyright) || gotCopyright);
		}

		public boolean checkFile(final File file) {
			clear();
			// boolean result = false;
			BufferedReader input = null;
			int lineNum = 0;
			try {
				input = new BufferedReader(new FileReader(file));
				String line;
				while (!gotValidFile() && (line = input.readLine()) != null) {
					lineNum++;
					checkLine(line);
				}
			} catch (IOException e) {
				System.err.println("reading line " + lineNum + " of " + file);
				e.printStackTrace(System.err);
			} finally {
				if (null != input) {
					try {
						input.close();
					} catch (IOException e) {
					} // ignore
				}
			}
			return gotValidFile();
		}

		@Override
		public String toString() {
			return tag;
		}

		private void checkLine(String line) {
			if ((null == line) || (0 == line.length())) {
				return;
			}
			if (!gotLicense && (null != license) && (line.contains(license))) {
				gotLicense = true;
			}
			if (!gotCopyright && (null != copyright)) {
				int loc;
				for (int j = 0; !gotCopyright && (j < YEARS.length); j++) {
					if (-1 != (loc = line.indexOf(YEARS[j]))) {
						loc += YEARS[j].length();
						String afterLoc = line.substring(loc).trim();
						for (int i = 0; !gotCopyright && (i < copyright.length); i++) {
							if (0 == afterLoc.indexOf(copyright[i])) {
								gotCopyright = true;
							}
						}
					}
				}
			}
		}

		private void clear() {
			if (gotLicense) {
				gotLicense = false;
			}
			if (gotCopyright) {
				gotCopyright = false;
			}
		}
	} // class License
}

class HeaderInfo {
	/** File for which this is the info */
	public final File file;

	/** unmodifiable List of String years */
	public final List years;

	/** last line of license */
	public final int lastLine;

	/** last line of license */
	public final boolean hasLicense;

	public HeaderInfo(File file, int lastLine, List<String> years, boolean hasLicense) {
		this.lastLine = lastLine;
		this.file = file;
		this.hasLicense = hasLicense;
		List<String> newYears = new ArrayList<>(years);
		Collections.sort(newYears);
		this.years = Collections.unmodifiableList(newYears);
		if ((null == file) || !file.canWrite()) {
			throw new IllegalArgumentException("bad file: " + this);
		}
		if (!hasLicense) {
			if ((0 > lastLine) || (65 < lastLine)) {
				throw new IllegalArgumentException("bad last line: " + this);
			}
		} else {
			if ((null == years) || (1 > years.size())) {
				throw new IllegalArgumentException("no years: " + this);
			}
			if ((20 > lastLine) || (65 < lastLine)) {
				throw new IllegalArgumentException("bad last line: " + this);
			}
		}
	}

	@Override
	public String toString() {
		return file.getPath() + ":" + lastLine + " " + years;
	}

	public void writeHeader(PrintWriter writer) {
		if (!hasLicense) {
			writer.println(TOP);
			writer.println(PARC_ONLY);
			writeRest(writer);
		} else {
			final int size = years.size();
			if (1 > size) {
				throw new Error("no years found in " + toString());
			}
			String first = (String) years.get(0);
			String last = (String) years.get(size - 1);
			boolean lastIs2002 = "2002".equals(last);
			String xlast = last;
			if (lastIs2002) { // 2002 was PARC
				xlast = (String) (size > 1 ? years.get(size - 2) : null);
				// 1999-2002 Xerox implies 1999-2001 Xerox
				if (first.equals(xlast) && !"2001".equals(xlast)) {
					xlast = "2001";
				}
			}
			String xyears = first + "-" + xlast;
			if (first.equals(last)) {
				xyears = first;
			}

			writer.println(TOP);
			if (!lastIs2002) { // Xerox only
				writer.println(XEROX_PREFIX + xyears + XEROX_SUFFIX + ". ");
			} else if (size == 1) { // PARC only
				writer.println(PARC_ONLY);
			} else { // XEROX plus PARC
				writer.println(XEROX_PREFIX + xyears + XEROX_SUFFIX + ", ");
				writer.println(PARC);
			}
			writeRest(writer);
		}
	}

	void writeRest(PrintWriter writer) {
		writer.println(" * All rights reserved. ");
		writer.println(" * This program and the accompanying materials are made available ");
		writer.println(" * under the terms of the Eclipse Public License v1.0 ");
		writer.println(" * which accompanies this distribution and is available at ");
		writer.println(" * http://www.eclipse.org/legal/epl-v10.html ");
		writer.println(" *  ");
		writer.println(" * Contributors: ");
		writer.println(" *     Xerox/PARC     initial implementation ");
		writer.println(" * ******************************************************************/");
		writer.println("");
	}

	public static final String TOP = "/* *******************************************************************";
	public static final String PARC = " *               2002 Palo Alto Research Center, Incorporated (PARC).";
	public static final String PARC_ONLY = " * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).";
	public static final String XEROX_PREFIX = " * Copyright (c) ";
	public static final String XEROX_SUFFIX = " Xerox Corporation";
	/*
	 * /* ******************************************************************* Copyright (c) 1998-2001 Xerox Corporation, 2002 Palo
	 * Alto Research Center, Incorporated (PARC). All rights reserved. This program and the accompanying materials are made
	 * available under the terms of the Eclipse Public License v1.0 which accompanies this distribution and is available at
	 * http://www.eclipse.org/legal/epl-v10.html
	 *
	 * Contributors: Xerox/PARC initial implementation ******************************************************************
	 */
}

/**
 * header search/replace using hokey string matching
 */
class Header {

	/** replace the header in file */
	public static boolean replaceHeader(File file, HeaderInfo info) {
		// ArrayList years = new ArrayList();
		// int endLine = 0;
		BufferedReader input = null;
		PrintWriter output = null;
		FileWriter outWriter = null;
		int lineNum = 0;
		boolean result = false;
		final File inFile = new File(file.getPath() + ".tmp");
		try {
			File outFile = new File(file.getPath());
			if (!file.renameTo(inFile) || !inFile.canRead()) {
				throw new Error("unable to rename " + file + " to " + inFile);
			}
			outWriter = new FileWriter(outFile);
			input = new BufferedReader(new FileReader(inFile));
			output = new PrintWriter(outWriter, true);
			info.writeHeader(output);
			String line;
			while (null != (line = input.readLine())) {
				lineNum++;
				if (lineNum > info.lastLine) {
					output.println(line);
				}
			}
		} catch (IOException e) {
			System.err.println("writing line " + lineNum + " of " + file);
			e.printStackTrace(System.err);
			result = false;
		} finally {
			if (null != input) {
				try {
					input.close();
				} catch (IOException e) {
					result = false;
				}
			}
			if (null != outWriter) {
				try {
					outWriter.close();
				} catch (IOException e) {
					result = false;
				}
			}
			result = inFile.delete();
		}
		return result;
	}

	public static HeaderInfo checkFile(final File file) {
		ArrayList<String> years = new ArrayList<>();
		int endLine = 0;
		BufferedReader input = null;
		int lineNum = 0;
		try {
			input = new BufferedReader(new FileReader(file));
			String line;
			while (null != (line = input.readLine())) {
				lineNum++;
				String ll = line.trim();
				if (ll.startsWith("package ") || ll.startsWith("import ")) {
					break; // ignore default package w/o imports
				}
				if (checkLine(line, years)) {
					endLine = lineNum;
					break;
				}
			}
		} catch (IOException e) {
			System.err.println("reading line " + lineNum + " of " + file);
			e.printStackTrace(System.err);
		} finally {
			if (null != input) {
				try {
					input.close();
				} catch (IOException e) {
				} // ignore
			}
		}
		return new HeaderInfo(file, endLine, years, endLine > 0);
	}

	/**
	 * Add any years found (as String) to years, and return true at the first end-of-comment
	 *
	 * @return true if this line has end-of-comment
	 */
	private static boolean checkLine(String line, List<String> years) {
		if ((null == line) || (0 == line.length())) {
			return false;
		}
		int loc;
		int start = 0;

		while ((-1 != (loc = line.indexOf("199", start)) || (-1 != (loc = line.indexOf("200", start))))) {
			char c = line.charAt(loc + 3);
			if ((c <= '9') && (c >= '0')) {
				years.add(line.substring(loc, loc + 4));
			}
			start = loc + 4;
		}

		return (line.contains("*/"));
	}

} // class Header

