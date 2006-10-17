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

import java.util.*;
import java.io.*;

public class ConfigParser {
    Location location;
    protected File relativeDirectory = null;
    protected List files = new LinkedList();
    private boolean fileParsed = false;
    protected static String CONFIG_MSG = "build config error: ";  
    
    public List getFiles() { return files; }

    public void parseCommandLine(String[] argsArray) throws ParseException {
        location = new CommandLineLocation();
        LinkedList args = new LinkedList();
        for (int i = 0; i < argsArray.length; i++) {
            args.add(new Arg(argsArray[i], location));
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
     * @throws ParseException   if the config file has already been prased.
     */
    private void parseConfigFileHelper(File configFile) {
        if (!configFile.exists()) {
            showError("file does not exist: " + configFile.getPath());
            return;
        }

        LinkedList args = new LinkedList();
        int lineNum = 0;

        try {
            BufferedReader stream =
                new BufferedReader(new FileReader(configFile));
            String line = null;
            while ( (line = stream.readLine()) != null) {
                lineNum += 1;
                line = stripWhitespaceAndComments(line);
                if (line.length() == 0) continue;
                args.add(new Arg(line, new SourceLocation(configFile, lineNum)));
            }
            stream.close();
        } catch (IOException e) {
            location = new SourceLocation(configFile, lineNum);
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
        if (commentStart == -1) return s;
        else return s.substring(0, commentStart);
    }

    String stripWhitespaceAndComments(String s) {
        s = stripSingleLineComment(s, "//");
        s = stripSingleLineComment(s, "#");
        s = s.trim();
        if (s.startsWith("\"") && s.endsWith("\"")) {
            s = s.substring(1, s.length()-1);
        }
        return s;
    }


    /** ??? We would like to call a showNonFatalError method here
     *  to show all errors in config files before aborting the compilation
     */
    protected void addFile(File sourceFile) {
        if (!sourceFile.isFile()) {
            showError("source file does not exist: " + sourceFile.getPath());
        }
        
        files.add(sourceFile);
    }

    void addFileOrPattern(File sourceFile) {
        if (sourceFile.getName().equals("*.java")) {
            addFiles(sourceFile.getParentFile(), new FileFilter() {
                    public boolean accept(File f) {
                        return f != null && f.getName().endsWith(".java");
                    }});
        } else if (sourceFile.getName().equals("*.aj")) {
            addFiles(sourceFile.getParentFile(), new FileFilter() {
                    public boolean accept(File f) {
                        return f != null && f.getName().endsWith(".aj");
                    }});
        } else {
            addFile(sourceFile);
        }
    }

    void addFiles(File dir, FileFilter filter) {
        if (dir == null) dir = new File(System.getProperty("user.dir"));
        
        if (!dir.isDirectory()) {
            showError("can't find " + dir.getPath());
        } else {

          File[] files = dir.listFiles(filter);
          if (files.length == 0) {
            showWarning("no matching files found in: " + dir);
          }

          for (int i = 0; i < files.length; i++) {
            addFile(files[i]);
          }
        }
    }

    protected void parseOption(String arg, LinkedList args) {
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

    void parseArgs(LinkedList args) {
        while (args.size() > 0) parseOneArg(args);
    }

    protected Arg removeArg(LinkedList args) {
        if (args.size() == 0) {
            showError("value missing");
            return null;
        } else {
            return (Arg)args.removeFirst();
        }
    }

    protected String removeStringArg(LinkedList args) {
        Arg arg = removeArg(args);
        if (arg == null) return null;
        return arg.getValue();
    }

    boolean isSourceFileName(String s) {
        if (s.endsWith(".java")) return true;
        if (s.endsWith(".aj")) return true;
        if (s.endsWith(".ajava")) {
            showWarning(".ajava is deprecated, replace with .aj or .java: " + s);
            return true;
        }
        return false;
    }

    void parseOneArg(LinkedList args) {
        Arg arg = removeArg(args);
        String v = arg.getValue();
        location = arg.getLocation();
        if (v.startsWith("@")) {
            parseImportedConfigFile(v.substring(1));
        } else if (v.equals("-argfile")) {
            parseConfigFileHelper(makeFile(removeArg(args).getValue()));
        } else if (isSourceFileName(v)) {
            addFileOrPattern(makeFile(v));
        } else {
            parseOption(arg.getValue(), args);
        }
    }

	protected void parseImportedConfigFile(String relativeFilePath) {
		parseConfigFileHelper(makeFile(relativeFilePath));		
	}

    public File makeFile(String name) {
        if (relativeDirectory != null) {
            return makeFile(relativeDirectory,name);
        } else {
            return makeFile(getCurrentDir(), name);
        }
    }

    private File makeFile(File dir, String name) {
        name = name.replace('/', File.separatorChar);
        File ret = new File(name);
        boolean isAbsolute = ret.isAbsolute()
            || (ret.exists() && ret.getPath().startsWith(File.separator));
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
		
        public String getValue() { return value; }
        public Location getLocation() { return location; }
    }
    
    static abstract class Location {
        public abstract File getFile();
        public abstract File getDirectory();
        public abstract int getLine();
        public abstract String toString();
    }

    static class SourceLocation extends Location {
        private int line;
        private File file;
        public SourceLocation(File file, int line) {
            this.line = line;
            this.file = file;
        }

        public File getFile() { return file; }
        public File getDirectory() { return file.getParentFile(); }
        public int getLine() { return line; }

        public String toString() {
            return file.getPath()+":"+line;
        }
    }
    
    static class CommandLineLocation extends Location {
        public File getFile() {
            return new File(System.getProperty("user.dir"));
        }
        
        public File getDirectory() {
            return new File(System.getProperty("user.dir"));
        }
        public int getLine() { return -1; }
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
            if (location == null) return -1;
            return location.getLine();
        }
        public File getFile() {
            if (location == null) return null;
            return location.getFile();
        }
    }
}
