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

package org.aspectj.ajdt.ajc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

public class ConfigParser {
	Location location;
	protected File relativeDirectory = null;
	protected List<File> files = new LinkedList<>();
	protected List<File> xmlfiles = new ArrayList<>();
	private boolean fileParsed = false;
	protected static String CONFIG_MSG = "build config error: ";

	public List<File> getFiles() {
		return files;
	}

	public List<File> getXmlFiles() {
		return xmlfiles;
	}

	public void parseCommandLine(String[] argsArray) throws ParseException {
		location = new CommandLineLocation();
		LinkedList<Arg> args = new LinkedList<>();
		for (String s : argsArray) {
			args.add(new Arg(s, location));
		}
		String aspectjOptions = null;
		try {
			aspectjOptions = System.getenv("ASPECTJ_OPTS");
			if (aspectjOptions == null) {
				aspectjOptions = System.getProperty("ASPECTJ_OPTS");
			}
		} catch (Throwable t) {
			aspectjOptions = null;
		}
		if (aspectjOptions != null) {
			StringTokenizer st = new StringTokenizer(aspectjOptions);
			while (st.hasMoreElements()) {
				args.add(new Arg(st.nextToken(),location));
			}
		}
		parseArgs(args);
	}

	public void parseConfigFile(File configFile) throws ParseException {
		if (fileParsed == true) {
			throw new ParseException(CONFIG_MSG + "The file has already been parsed.", null);
		} else {
			parseConfigFileHelper(configFile);
		}
	}

	/**
	 * @throws ParseException if the config file has already been prased.
	 */
	private void parseConfigFileHelper(File configFile) {
		if (!configFile.exists()) {
			showError("file does not exist: " + configFile.getPath());
			return;
		}

		LinkedList<Arg> args = new LinkedList<>();
		int lineNum = 0;

		try {
			BufferedReader stream = new BufferedReader(new FileReader(configFile));
			String line = null;
			while ((line = stream.readLine()) != null) {
				lineNum += 1;
				line = stripWhitespaceAndComments(line);
				if (line.length() == 0) {
					continue;
				}
				args.add(new Arg(line, new CPSourceLocation(configFile, lineNum)));
			}
			stream.close();
		} catch (IOException e) {
			location = new CPSourceLocation(configFile, lineNum);
			showError("error reading config file: " + e.toString());
		}
		File oldRelativeDirectory = relativeDirectory; // for nested arg files;
		relativeDirectory = configFile.getParentFile();
		parseArgs(args);
		relativeDirectory = oldRelativeDirectory;
		fileParsed = true;
	}

	File getCurrentDir() {
		return location.getDirectory();
	}

	String stripSingleLineComment(String s, String commentString) {
		int commentStart = s.indexOf(commentString);
		if (commentStart == -1) {
			return s;
		} else {
			return s.substring(0, commentStart);
		}
	}

	String stripWhitespaceAndComments(String s) {
		s = stripSingleLineComment(s, "//");
		s = stripSingleLineComment(s, "#");
		s = s.trim();
		if (s.startsWith("\"") && s.endsWith("\"")) {
			if (s.length() == 1) {
				return "";
			} else {
				s = s.substring(1, s.length() - 1);
			}
		}
		return s;
	}

	/**
	 * ??? We would like to call a showNonFatalError method here to show all errors in config files before aborting the compilation
	 */
	protected void addFile(File sourceFile) {
		if (!sourceFile.isFile()) {
			showError("source file does not exist: " + sourceFile.getPath());
		}
		files.add(sourceFile);
	}

	protected void addXmlFile(File xmlFile) {
		if (!xmlFile.isFile()) {
			showError("XML file does not exist: " + xmlFile.getPath());
		}
		xmlfiles.add(xmlFile);
	}

	void addFileOrPattern(File sourceFile) {
		if (sourceFile.getName().charAt(0) == '*') {
			if (sourceFile.getName().equals("*.java")) {
				addFiles(sourceFile.getParentFile(), new FileFilter() {
					@Override
					public boolean accept(File f) {
						return f != null && f.getName().endsWith(".java");
					}
				});
			} else if (sourceFile.getName().equals("*.aj")) {
				addFiles(sourceFile.getParentFile(), new FileFilter() {
					@Override
					public boolean accept(File f) {
						return f != null && f.getName().endsWith(".aj");
					}
				});
			} else {
				addFile(sourceFile);
			}
		} else {
			addFile(sourceFile);
		}
	}

	void addFiles(File dir, FileFilter filter) {
		if (dir == null) {
			dir = new File(System.getProperty("user.dir"));
		}

		if (!dir.isDirectory()) {
			showError("can't find " + dir.getPath());
		} else {

			File[] files = dir.listFiles(filter);
			if (files.length == 0) {
				showWarning("no matching files found in: " + dir);
			}

			for (File file : files) {
				addFile(file);
			}
		}
	}

	protected void parseOption(String arg, LinkedList<Arg> args) {
		showWarning("unrecognized option: " + arg);
	}

	protected void showWarning(String message) {
		if (location != null) {
			message += " at " + location.toString();
		}
		System.err.println(CONFIG_MSG + message);
	}

	protected void showError(String message) {
		throw new ParseException(CONFIG_MSG + message, location);
	}

	void parseArgs(LinkedList<Arg> args) {
		while (args.size() > 0) {
			parseOneArg(args);
		}
	}

	protected Arg removeArg(LinkedList<Arg> args) {
		if (args.size() == 0) {
			showError("value missing");
			return null;
		} else {
			return args.removeFirst();
		}
	}

	protected String removeStringArg(LinkedList<Arg> args) {
		Arg arg = removeArg(args);
		if (arg == null) {
			return null;
		}
		return arg.getValue();
	}

	/**
	 * aop.xml configuration files can be passed on the command line.
	 */
	boolean isXml(String s) {
		return s.endsWith(".xml");
	}

	boolean isSourceFileName(String s) {
		if (s.endsWith(".java")) {
			return true;
		}
		if (s.endsWith(".aj")) {
			return true;
		}
		// if (s.endsWith(".ajava")) {
		// showWarning(".ajava is deprecated, replace with .aj or .java: " + s);
		// return true;
		// }
		return false;
	}

	void parseOneArg(LinkedList<Arg> args) {
		Arg arg = removeArg(args);
		String v = arg.getValue();
		location = arg.getLocation();
		if (v.startsWith("@")) {
			parseImportedConfigFile(v.substring(1));
		} else if (v.equals("-argfile")) {
			parseConfigFileHelper(makeFile(removeArg(args).getValue()));
		} else if (isSourceFileName(v)) {
			addFileOrPattern(makeFile(v));
			if (v.endsWith("module-info.java")) {
				parseOption(arg.getValue(), args);				
			}
		} else if (isXml(v)) {
			addXmlFile(makeFile(v));
		} else {
			parseOption(arg.getValue(), args);
		}
	}

	protected void parseImportedConfigFile(String relativeFilePath) {
		parseConfigFileHelper(makeFile(relativeFilePath));
	}

	public File makeFile(String name) {
		if (relativeDirectory != null) {
			return makeFile(relativeDirectory, name);
		} else {
			return makeFile(getCurrentDir(), name);
		}
	}

	private File makeFile(File dir, String name) {
		name = name.replace('/', File.separatorChar);
		File ret = new File(name);
		boolean isAbsolute = ret.isAbsolute() || (ret.exists() && ret.getPath().startsWith(File.separator));
		if (!isAbsolute && (dir != null)) {
			ret = new File(dir, name);
		}
		try {
			ret = ret.getCanonicalFile();
		} catch (IOException ioEx) {
			// proceed without canonicalization
			// so nothing to do here
		}
		return ret;
	}

	protected static class Arg {
		private Location location;
		private String value;

		@Override
		public String toString() {
			return "Arg[location="+location+" value="+value+"]";
		}
		
		public Arg(String value, Location location) {
			this.value = value;
			this.location = location;
		}

		public void setValue(String value) {
			this.value = value;
		}

		public void setLocation(Location location) {
			this.location = location;
		}

		public String getValue() {
			return value;
		}

		public Location getLocation() {
			return location;
		}
	}

	static abstract class Location {
		public abstract File getFile();

		public abstract File getDirectory();

		public abstract int getLine();

		@Override
		public abstract String toString();
	}

	static class CPSourceLocation extends Location {
		private int line;
		private File file;

		public CPSourceLocation(File file, int line) {
			this.line = line;
			this.file = file;
		}

		@Override
		public File getFile() {
			return file;
		}

		@Override
		public File getDirectory() {
			return file.getParentFile();
		}

		@Override
		public int getLine() {
			return line;
		}

		@Override
		public String toString() {
			return file.getPath() + ":" + line;
		}
	}

	static class CommandLineLocation extends Location {
		@Override
		public File getFile() {
			return new File(System.getProperty("user.dir"));
		}

		@Override
		public File getDirectory() {
			return new File(System.getProperty("user.dir"));
		}

		@Override
		public int getLine() {
			return -1;
		}

		@Override
		public String toString() {
			return "command-line";
		}
	}

	public static class ParseException extends RuntimeException {
		private Location location;

		public ParseException(String message, Location location) {
			super(message);
			this.location = location;
		}

		public int getLine() {
			if (location == null) {
				return -1;
			}
			return location.getLine();
		}

		public File getFile() {
			if (location == null) {
				return null;
			}
			return location.getFile();
		}
	}
}
