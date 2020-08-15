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

package org.aspectj.testing.harness.bridge;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.aspectj.bridge.AbortException;
import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.IMessage.Kind;
import org.aspectj.bridge.ISourceLocation;
import org.aspectj.bridge.Message;
import org.aspectj.bridge.MessageUtil;
import org.aspectj.bridge.SourceLocation;
import org.aspectj.testing.util.BridgeUtil;
import org.aspectj.testing.util.ObjectChecker;
import org.aspectj.testing.util.SFileReader;
import org.aspectj.testing.util.StandardObjectChecker;
import org.aspectj.testing.util.UtilLineReader;
import org.aspectj.util.FileUtil;
import org.aspectj.util.LangUtil;

/** 
 * SFileReader.Maker implementation to read tests 
 * XXX supports iterative but not yet incremental compiles
 */
public class FlatSuiteReader implements SFileReader.Maker {
	public static final String[] RA_String = new String[0];
	public static final FlatSuiteReader ME = new FlatSuiteReader();
	private static final SFileReader READER = new SFileReader(ME);

	static boolean isNumber(String s) { // XXX costly
		if ((null == s) || (0 == s.length())) {
			return false;
		}
		try {
			Integer.valueOf(s);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

    /** if true, clean up records before returning from make */
    public boolean clean;
    
	private FlatSuiteReader() {
	}

	/**
	 * @see org.aspectj.testing.harness.bridge.SFileReader.Maker#getType()
	 */
	public Class getType() {
		return AjcTest.Spec.class;
	}

	/**
	 * This constructs an AjcTest.Spec assuming we are at the start of a
	 * test definition in reader and taking the parent directory of
	 * the reader as the base directory for the test suite root. 
	 * @return the next AjcTest in reader, or null
	 * @see org.aspectj.testing.harness.bridge.SFileReader.Maker#make(UtilLineReader)
	 */
	public Object make(final UtilLineReader reader)
		throws AbortException, IOException {
		final AjcTest.Spec result = new AjcTest.Spec();
		boolean usingEclipse = false; // XXX
		/** handle read errors by throwing AbortException with context info */
		class R {
			public String read(String context) throws IOException {
				return read(context, true);
			}
			public String read(String context, boolean required)
				throws IOException {
				final boolean skipEmpties = false;
				String result = reader.nextLine(skipEmpties);
				if ((null != result) && (0 == result.length())) {
					result = null;
				}
				if ((null == result) && required) {
					String s = "expecting " + context + " at " + reader;
					throw new AbortException(s);
				}
				return result;
			}
		}

		final R r = new R();
		//final String baseDir = reader.getFile().getParent();
		String line;
		String[] words;
//		boolean isRequired = true;

		final int startLine = reader.getLineNumber() - 1;

		// description first - get from last line read
		// XXX permits exactly one blank line between test records?
		result.description = reader.lastLine();
		if (null == result.description) {
			throw new AbortException("expecting description at " + reader);
		}

		// next line is baseDir {option..}
		line = r.read("baseDir {option..}");
		words = LangUtil.split(line);
		if ((null == words) || (0 == words.length)) {
			throw new AbortException(
				"expecting dir {option..} at " + reader);
		}
		// XXX per-test (shared) root
		//final File sourceRoot =  new File(baseDir, words[0]);
		result.setTestDirOffset(words[0]);

		String[] compileOptions = new String[words.length - 1];
		System.arraycopy(words, 1, compileOptions, 0, words.length - 1);

		// next are 1..n source lines: source...
		CompilerRun.Spec lastCompileSpec = null;
		// save last source file as default for error/warning line
		File lastFile = null; // XXX per-compiler-run errors
		while (null != (line = r.read("source.."))) {
			words = LangUtil.split(line);
			if (0 == FileUtil.sourceSuffixLength(words[0])) { // XXX
				break;
			} else {
				lastCompileSpec = new CompilerRun.Spec();
				lastCompileSpec.testSrcDirOffset = null;
				// srcs are in test base for old
				lastCompileSpec.addOptions(compileOptions);
				lastCompileSpec.addPaths(words);
				lastFile = new File(words[words.length - 1]);
				result.addChild(lastCompileSpec);
			}
		}
		if (null == lastCompileSpec) {
			throw new AbortException("expected sources at " + reader);
		}

		List<Message> exp = new ArrayList<>();
		// !compile || noerrors || className {runOption..}
		String first = words[0];
		if ("!compile".equals(first)) {
			//result.className = words[0];
			//result.runOptions = new String[words.length-1];
			//System.arraycopy(words, 0, result.runOptions, 0, words.length-1);
		} else if ("noerrors".equals(first)) {
			// className is null, but no errors expected
			// so compile succeeds but run not attempted
			//result.errors = Main.RA_ErrorLine;
			// result.runOptions = Main.RA_String;
		} else if (isNumber(first) || (first.contains(":"))) {
			exp.addAll(makeMessages(IMessage.ERROR, words, 0, lastFile));
		} else {
			String[] args = new String[words.length - 1];
			System.arraycopy(words, 0, args, 0, args.length);
			JavaRun.Spec spec = new JavaRun.Spec();
			spec.className = first;
			spec.addOptions(args);
			//XXXrun.runDir = sourceRoot;
			result.addChild(spec);
		}

		// optional: warnings, eclipse.warnings, eclipse.errors
		// XXX unable to specify error in eclipse but not ajc
		boolean gotErrors = false;
		while (null
			!= (line =
				r.read(
					" errors, warnings, eclipse.warnings, eclipse.error",
					false))) {
			words = LangUtil.split(line);
			first = words[0];
			if ("eclipse.warnings:".equals(first)) {
				if (usingEclipse) {
					exp.addAll(
						makeMessages(
							IMessage.WARNING,
							words,
							0,
							lastFile));
				}
			} else if ("eclipse.errors:".equals(first)) {
				if (usingEclipse) {
					exp.addAll(
						makeMessages(IMessage.ERROR, words, 0, lastFile));
				}
			} else if ("warnings:".equals(first)) {
				exp.addAll(
					makeMessages(IMessage.WARNING, words, 0, lastFile));
			} else if (gotErrors) {
				exp.addAll(
					makeMessages(IMessage.WARNING, words, 0, lastFile));
			} else {
				exp.addAll(
					makeMessages(IMessage.ERROR, words, 0, lastFile));
				gotErrors = true;
			}
		}
		lastCompileSpec.addMessages(exp);

		int endLine = reader.getLineNumber();
		File sourceFile = reader.getFile();
		ISourceLocation sl =
			new SourceLocation(sourceFile, startLine, endLine, 0);
		result.setSourceLocation(sl);

        if (clean) {
            cleanup(result, reader);
        }
        return result;
	}
    
    /** post-process result 
     * - use file name as keyword
     * - clip / for dir offsets
     * - extract purejava keyword variants
     * - extract bugID
     * - convert test options to force-options
     * - detect illegal xml characters
     */
    private void cleanup(AjcTest.Spec result, UtilLineReader lineReader) {
        LangUtil.throwIaxIfNull(result, "result");
        LangUtil.throwIaxIfNull(lineReader, "lineReader");

        File suiteFile = lineReader.getFile();
        String name = suiteFile.getName();
        if (!name.endsWith(".txt")) {
            throw new Error("unexpected name: " + name);
        }
        result.addKeyword("from-" + name.substring(0,name.length()-4));

        final String dir = result.testDirOffset;
        if (dir.endsWith("/")) {
            result.testDirOffset = dir.substring(0,dir.length()-1);
        }

        StringBuffer description = new StringBuffer(result.description);
        if (strip(description, "PUREJAVA")) {
            result.addKeyword("purejava");
        }
        if (strip(description, "PUREJAVE")) {
            result.addKeyword("purejava");
        }
        if (strip(description, "[purejava]")) {
            result.addKeyword("purejava");
        }
        String input = description.toString();
        int loc = input.indexOf("PR#");
        if (-1 != loc) {
            String prefix = input.substring(0, loc).trim();
            String pr = input.substring(loc+3, loc+6).trim();
            String suffix = input.substring(loc+6).trim();
            description.setLength(0);
            description.append((prefix + " " + suffix).trim());
            try {
                result.setBugId(Integer.valueOf(pr));
            } catch (NumberFormatException e) {
                throw new Error("unable to convert " + pr + " for " + result
                    + " at " + lineReader);
            }                      
        }
        input = description.toString();
        String error = null;
        if (input.contains("&")) {
            error = "char &";
        } else if (input.contains("<")) {
            error = "char <";
        } else if (input.contains(">")) {
            error = "char >";
        } else if (input.contains("\"")) {
            error = "char \"";
        }
        if (null != error) {
            throw new Error(error + " in " + input + " at " + lineReader);
        }
        result.description = input;
    
        ArrayList<String> newOptions = new ArrayList<>();
        Iterable<String> optionsCopy = result.getOptionsList();
        for (String option: optionsCopy) {
			if (option.startsWith("-")) {
                newOptions.add("!" + option.substring(1));
            } else {
                throw new Error("non-flag option? " + option);
            }
		}
        result.setOptionsArray((String[]) newOptions.toArray(new String[0]));
    }
    
    private boolean strip(StringBuffer sb, String infix) {
        String input = sb.toString();
        int loc = input.indexOf(infix);
        if (-1 != loc) {
            String prefix = input.substring(0, loc);
            String suffix = input.substring(loc+infix.length());
            input = (prefix.trim() + " " + suffix.trim()).trim();
            sb.setLength(0);
            sb.append(input);
            return true;
        }
        return false;
    }

	/**
	 * Generate list of expected messages of this kind.
	 * @param kind any non-null kind, but s.b. IMessage.WARNING or ERROR
	 * @param words
	 * @param start index in words where to start
	 * @param lastFile default file for source location if the input does not specify
	 * @return List
	 */
	private List<Message> makeMessages(// XXX weak - also support expected exceptions, etc.
	Kind kind, String[] words, int start, File lastFile) {
		List<Message> result = new ArrayList<>();
		for (int i = start; i < words.length; i++) {
			ISourceLocation sl =
				BridgeUtil.makeSourceLocation(words[i], lastFile);
			if (null == sl) { // XXX signalling during make
				// System.err.println(...);
				//MessageUtil.debug(handler, "not a source location: " + words[i]);
			} else {
				String text =
					(("" + sl.getLine()).equals(words[i]) ? "" : words[i]);
				result.add(new Message(text, kind, null, sl));
			}
		}
		return (0 == result.size() ? Collections.<Message>emptyList() : result);
	}

	/** 
	 * Read suite spec from a flat .txt file.
	 * @throws AbortException on failure
	 * @return AjcTest.Suite.Spec with any AjcTest.Spec as children
	 */ 
	public AjcTest.Suite.Spec readSuite(File suiteFile) {
		LangUtil.throwIaxIfNull(suiteFile, "suiteFile");
		if (!suiteFile.isAbsolute()) {
			suiteFile = suiteFile.getAbsoluteFile();
		}
        final AjcTest.Suite.Spec result = new AjcTest.Suite.Spec();
        result.setSuiteDirFile(suiteFile.getParentFile());
		ObjectChecker collector = new StandardObjectChecker(IRunSpec.class) {
			public boolean doIsValid(Object o) {
                result.addChild((IRunSpec) o);
				return true;
			}
		};
		boolean abortOnError = true;
		try {
			READER.readNodes(
				suiteFile,
				collector,
				abortOnError,
				System.err);
		} catch (IOException e) {
			IMessage m = MessageUtil.fail("reading " + suiteFile, e);
			throw new AbortException(m);
		}
        
		return result;
	}
}
