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
 *     PARC     initial implementation 
 * ******************************************************************/

package org.aspectj.ajdt.internal.compiler.batch;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.aspectj.bridge.ICommand;
import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.IMessageHandler;
import org.aspectj.bridge.IMessageHolder;
import org.aspectj.bridge.ISourceLocation;
import org.aspectj.bridge.Message;
import org.aspectj.bridge.MessageHandler;
import org.aspectj.bridge.ReflectionFactory;
import org.aspectj.util.FileUtil;
import org.aspectj.util.LangUtil;

/**
 * Mostly stateless incremental test case.
 * Subclass to use from junit.
 */
public class IncrementalCase { // XXX NOT bound to junit - bridge tests?
    public static final String[] RA_String = new String[0]; // XXX
	boolean verbose = true;
	boolean ignoreWarnings = false;

	public static void main(String[] args) throws IOException {
    	IncrementalCase me = new IncrementalCase();
		MessageHandler h = new MessageHandler();
//		boolean result;
		StringBuffer sb = new StringBuffer();
		for (String arg : args) {
			sb.append("\n###### results for " + arg);
			sb.append("\n" + me.run(new File(arg), h) + ": " + h);
		}
		System.err.flush();
		System.out.flush();
		System.err.println(sb.toString());
	}

	/** 
	 * Run an incremental compile case.
	 * For each i=1..9, copy files srcDir/*{i=1..9}0.java
	 * to the sandbox and compile.
	 * This only expects the changed files to be recompiled, but
	 * it also calls verifyCompile(..);
	 * @param handler all non-functional feedback here.
	 * Exceptions are logged as ABORT messages 
	 */
	public boolean run(File srcBase, IMessageHandler handler)
		throws IOException {
		final String cname = ReflectionFactory.ECLIPSE;

		File targetBase =
			makeDir(getSandboxDir(), "IncrementalCaseSandbox", handler);
		if (null == targetBase) {
			return false;
		}
		File targetSrc = makeDir(targetBase, "src", handler);
		if (null == targetSrc) {
			return false;
		}
		File targetClasses = makeDir(targetBase, "classes", handler);
		if (null == targetClasses) {
			return false;
		}
		final List<File> files = new ArrayList<>();
		final FileFilter collector = new FileFilter() {
			@Override
			public boolean accept(File file) {
				return files.add(file);
			}
		};
		final ICommand compiler =
			ReflectionFactory.makeCommand(cname, handler);

		List recompiled = null;
		boolean result = true;

		final String toSuffix = ".java";
//		final String canonicalFrom = srcBase.getCanonicalPath();
		final Definition[] defs = getDefinitions(srcBase);
		if ((null == defs) || (defs.length < 9)) {
			throw new Error("did not get definitions");
		}
		MessageHandler compilerMessages = new MessageHandler();
        StringBuffer commandLine = new StringBuffer();
		for (int i = 1; result && (i < 10); i++) { 
			String fromSuffix = "." + i + "0.java";
			// copy files, collecting as we go...
			files.clear();
			FileUtil.copyDir(
				srcBase,
				targetSrc,
				fromSuffix,
				toSuffix,
				collector);
			if (0 == files.size()) { // XXX detect incomplete?
				break;
			}
			List safeFiles = Collections.unmodifiableList(files);
			log("Compiling ", safeFiles, handler);
			if (1 == i) {
				ArrayList<String> argList = new ArrayList<>(getBaseArgs(targetSrc, targetClasses));
				File[] fra = (File[]) safeFiles.toArray(new File[0]);
				// sigh
				argList.addAll(
					Arrays.asList(FileUtil.getAbsolutePaths(fra)));
				String[] args = argList.toArray(new String[0]);
    		    commandLine.append(""+argList);
            	result = compiler.runCommand(args, compilerMessages);
			} else {
				if (null == recompiled) {
					recompiled = new ArrayList();
				} else {
					recompiled.clear();
				}
				compilerMessages.init();
	            commandLine.append("["+i+": " + recompiled + "] ");
    			result =
					compiler.repeatCommand(compilerMessages);
			}
			result =
				verifyCompile(
					i,
					result,
					srcBase,
					targetSrc,
					targetClasses,
					defs[i - 1],
					compilerMessages,
                    commandLine,
					handler);
		}
		return result;
	}

	// -------------------------------------- test case verification
	/**
	 * Verify that this incremental compile step worked.
	 * @param recompiled the List of Files the compiler recompiled - null the first pass
	 * @param files the (unmodifiable) List of File passed as sources to the compiler
     * @param recompiled the List sink for the Files actually recompiled
     */
    // XXX argh no parent/child relationship in this world...
	protected boolean verifyCompile(
		int iteration,
		boolean result,
		File srcDir,
		File sandboxSrcDir,
		File sandboxClassesDir,
		Definition def,
		IMessageHolder compilerMessages,
        StringBuffer commandLine,
		IMessageHandler handler) {
        log("verifyCompile -  iteration ", iteration, handler);
		log("verifyCompile -        def ", def, handler);
        log("verifyCompile -    command ", commandLine.toString(), handler);
		log("verifyCompile -   messages ", compilerMessages, handler);
        StringBuffer failures = new StringBuffer();
        if (def.expectFail == result) {
             failures.append("iteration " + iteration + 
                " expected to " + (def.expectFail ? "fail\n" : "pass"));
        }
        if (0 < failures.length()) {
            fail(handler, 
                "\nFailures in iteration " + iteration
                + "\n Command: " + commandLine 
                + "\nMessages: " + compilerMessages 
                + "\n     Def: " + def
                + "\nFailures: " + failures);
            return false; 
        }
		IMessage[] messages = compilerMessages.getMessages(IMessage.ERROR, IMessageHolder.EQUAL);
		String[] expected =
			(null != def.errors ? def.errors : def.eclipseErrors);
		if (haveAll("errors", expected, messages, handler)) {
			if (!ignoreWarnings) {
				messages = compilerMessages.getMessages(IMessage.WARNING, IMessageHolder.EQUAL);
				expected =
					(null != def.warnings
						? def.warnings
						: def.eclipseWarnings);
				if (!haveAll("warnings", expected, messages, handler)) {
                    return false;
                }
			}
		}
        return true;
	}

	// -------------------------------------- test case setup
	/** 
	 * Get the sandbox (parent) directory.
	 * This implementation uses the temporary directory
	 */
	protected File getSandboxDir() throws IOException { // XXX util
		File tempFile = File.createTempFile("IncrementalCase", ".txt");
		File tempDir = tempFile.getParentFile();
		tempFile.delete();
		return tempDir;
	}
	
	//XXX hack
	public File outputDir;

	/** @param srcDir ignored for now */
	protected List<String> getBaseArgs(File srcDir, File classesDir) {
		outputDir = classesDir;
		String[] input =
			new String[] {
				"-verbose",
//				"-classpath",
//				System.getProperty("sun.boot.class.path"),
				"-d",
				classesDir.getAbsolutePath()};
		return Collections.unmodifiableList(
				new ArrayList<>(Arrays.asList(input)));
	}

	protected File makeDir(
		File parent,
		String name,
		IMessageHandler handler) { // XXX util
		File result = new File(parent, name);
		if (!result.exists()) {
			result.mkdirs();
			if (!result.exists()) {
				fail(handler, "unable to create " + result);
				return null;
			}
		}
		return result;
	}
        
    // -------------------------------------- test case verification


    List<String> normalizeFilenames(String[] ra) { // XXX util
        List<String> result = new ArrayList<>();
        if (null != ra) {
			for (String s : ra) {
				result.add(normalizeFilename(s));
			}
            if (1 < ra.length) {
                Collections.sort(result);
            }
        }
        return result;
    }

    /** @param list the List of File */
    List<String> normalizeFilenames(List<File> list) { // XXX util
        List<String> result = new ArrayList<>();
        for (File file: list) {
//        for (Iterator<?> iter = list.iterator(); iter.hasNext();) {
			result.add(normalizeFilename(file.getPath()));			
		}
        Collections.sort(result);
        return result;
    }
    
    String normalizeFilename(String s) {        // XXX error-prone
        final String suffix = ".java";
        int loc = s.lastIndexOf(suffix);
        if (-1 == loc) {
            return s; // punt
        }
        s = s.substring(0, loc + suffix.length()).replace('\\', '/');
        loc = s.lastIndexOf("/");
        return (-1 == loc ? s : s.substring(loc+1));
    }
    

	/** XXX duplicate message checking */
	boolean haveAll(
		String label,
		String[] expected,
		IMessage[] messages,
		IMessageHandler handler) {
		if (null == expected) {
			expected = new String[0];
		}
		boolean result = true;
		final int[] exp = new int[expected.length];
		StringBuffer sb = new StringBuffer();
		sb.append("[");
		for (int i = 0; i < exp.length; i++) {
			String s = expected[i];
			int loc = s.lastIndexOf(":");
			if (-1 != loc)
				s = s.substring(loc + 1);
			try {
				exp[i] = Integer.valueOf(s);
				sb.append(exp[i] + ((i < (exp.length - 1)) ? ", " : ""));
			} catch (NumberFormatException e) {
				info(handler, "bad " + label + ":" + expected[i]);
				// XXX worse than info...
				sb.append("bad" + ((i < (exp.length - 1)) ? ", " : "]"));
			}
		}
        sb.append("]");
		final String context =
			label
				+ "\n in context haveAll expected="
				+ Arrays.asList(expected)
				+ " exp="
				+ sb
				+ " actual="
				+ Arrays.asList(messages);
		info(handler, context);

		BitSet foundSet = new BitSet(10);
		for (final int expLine : exp) {
			boolean found = false;
			for (int j = 0; !found && (j < messages.length); j++) {
				ISourceLocation sl = messages[j].getSourceLocation();
				found = ((null != sl) && (expLine == sl.getLine()));
				if (found) {
					info(handler, "found " + label + " for: " + expLine);
					if (foundSet.get(j)) {
						info(
								handler,
								"duplicate " + label + " expected: " + expLine);
					}
					foundSet.set(j);
				}
			}
			if (!found) {
				String s =
						"expected "
								+ label
								+ " not found: "
								+ expLine
								+ context;
				fail(handler, s); // bad short-circuit
				if (!result) {
					result = false;
				}
			}

		}
		sb.setLength(0);
		for (int i = 0; i < messages.length; i++) {
			if (!foundSet.get(i)) {
				sb.append(
					"\n unexpected " + label + " found: " + messages[i]);
			}
		}
		if (0 == sb.length()) {
			return true;
		} else {
			fail(handler, sb.toString() + context);
			return false;
		}
	}
	// -------------------------------------- messages
	protected void log(String label, Object o, IMessageHandler handler) {
		if (verbose) {
            if (null != handler) {
    			message(IMessage.INFO, label + ": " + o, handler);
            } else {
                System.err.println("\nlog: " + label + ": " + o);
		    }
        }
	}
	protected void info(IMessageHandler handler, String mssg) {
		message(IMessage.INFO, mssg, handler);
	}
	protected void fail(IMessageHandler handler, String mssg) {
		message(IMessage.FAIL, mssg, handler);
	}

	/** this is the only client of the message handler - remplement to do other notification*/
	protected void message(
		IMessage.Kind kind,
		String mssg,
		IMessageHandler handler) {
		if (null != handler) {
			handler.handleMessage(
				new Message("\n### " + mssg, kind, null, null));
		}
	}

	/** @return Definition[9] read from srceBase/Definition.PATH */
	Definition[] getDefinitions(File srcBase) {
		File file = new File(srcBase, Definition.PATH);
		Properties props = new Properties();
		FileInputStream in = null;
		try {
			in = new FileInputStream(file);
			props.load(in);
		} catch (IOException e) {
			e.printStackTrace(System.err);
		} finally {
			if (null != in)
				try {
					in.close();
				} catch (IOException e) {
				}
		}
		Definition[] result = new Definition[9];
		for (int i = 0; i < 9; i++) { // XXX matches run
			result[i] = new Definition((1+i) + "0", props);
		}
		return result;
	}

	static class Definition {
		static final String PATH = "expected.txt";
		boolean expectFail;
		String prefix;
		String[] files;
		String[] recompiled;
		String[] errors;
		String[] warnings;
		String[] eclipseErrors;
		String[] eclipseWarnings;
		Definition(String prefix, Properties props) {
//			Enumeration keys = props.keys();
			this.prefix = prefix;
			files = get(props, prefix + ".files");
			recompiled = get(props, prefix + ".recompiled");
			errors = get(props, prefix + ".errors");
			warnings = get(props, prefix + ".warnings");
			eclipseErrors = get(props, prefix + ".eclipse.errors");
			eclipseWarnings = get(props, prefix + ".eclipse.warnings");
			expectFail =
				(((null != errors) && (0 < errors.length))
					|| ((null != eclipseErrors)
						&& (0 < eclipseErrors.length)));
		}
		String[] get(Properties props, String key) {
			String s = props.getProperty(key);
			if (null != s) {
				return LangUtil.split(s);
			}
			return null;
		}
		@Override
		public String toString() {
			return "Definition "
                + " expectFail="
                + expectFail
                + " prefix="
				+ prefix
				+ " files="
				+ safe(files)
                + " recompiled="
                + safe(recompiled)
				+ " errors="
				+ safe(errors)
				+ " warnings="
				+ safe(warnings)
				+ " eclipseErrors="
				+ safe(eclipseErrors)
				+ " eclipseWarnings="
				+ safe(eclipseWarnings);
		}
		String safe(String[] in) {
			return (null == in ? "" : "" + Arrays.asList(in));
		}
	}
}
