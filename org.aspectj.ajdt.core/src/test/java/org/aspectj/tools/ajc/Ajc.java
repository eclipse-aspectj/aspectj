/* *******************************************************************
 * Copyright (c) 2004 IBM Corporation
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *
 * Contributors:
 *     Adrian Colyer,
 * ******************************************************************/
package org.aspectj.tools.ajc;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;

import org.aspectj.asm.AsmManager;
import org.aspectj.asm.IProgramElement;
import org.aspectj.asm.IRelationship;
import org.aspectj.asm.IRelationshipMap;
import org.aspectj.asm.internal.Relationship;
import org.aspectj.bridge.AbortException;
import org.aspectj.bridge.ICommand;
import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.IMessage.Kind;
import org.aspectj.bridge.IMessageHandler;
import org.aspectj.bridge.MessageHandler;
import org.aspectj.bridge.context.CompilationAndWeavingContext;
import org.aspectj.testing.util.TestUtil;
import org.aspectj.util.FileUtil;

import static java.io.File.pathSeparator;
import static java.io.File.separator;
import static org.aspectj.tools.ajc.AjcTestCase.CLASSPATH_ASM;
import static org.aspectj.tools.ajc.AjcTestCase.CLASSPATH_JUNIT;

/**
 * The Ajc class is intended for use as part of a unit-test suite, it drives the AspectJ compiler and lets you check the compilation
 * results. Compilations run in a sandbox that is created in C:\temp\ajcSandbox or /tmp/ajcSandbox depending on your platform.
 * <p>
 * The expected usage of Ajc is through the TestCase superclass, AjcTestCase, which provides helper methods that conveniently drive
 * the base functions exposed by this class.
 * </p>
 *
 * @see org.aspectj.tools.ajc.AjcTestCase
 */
public class Ajc {

	private static final String BUILD_OUTPUT_FOLDER = "target";

	public static final String outputFolder(String module) {
		return pathSeparator + ".." + separator + module + separator + BUILD_OUTPUT_FOLDER + separator + "classes";
	}

	public static final String outputFolders(String... modules) {
		StringBuilder s = new StringBuilder();
		for (String module: modules) {
			s.append(pathSeparator + ".." + separator + module + separator + BUILD_OUTPUT_FOLDER + separator + "classes");
		}
		return s.toString();
	}

	// ALSO SEE ANTSPEC AND AJCTESTCASE
	private static final String TESTER_PATH =
		outputFolder("testing-client")
			+ outputFolder("runtime")
			+ outputFolder("bcel-builder")
			+ pathSeparator + CLASSPATH_JUNIT
			+ pathSeparator + CLASSPATH_ASM
			+ outputFolder("bridge")
			+ outputFolder("loadtime")
			+ outputFolder("weaver")
			+ outputFolder("org.aspectj.matcher")
			+ outputFolder("bridge");

	private CompilationResult result;
	private File sandbox;
	private File baseDir;
	private final Main main;
	private String[] ajcArgs;
	private int incrementalStage = 10;
	private boolean shouldEmptySandbox = true;
	private final AjcCommandController controller;
	public static boolean verbose = System.getProperty("aspectj.tests.verbose", "true").equals("true");

	/**
	 * Constructs a new Ajc instance, with a new AspectJ compiler inside.
	 */
	public Ajc() {
		main = new Main();
		controller = new AjcCommandController();
		main.setController(controller);
	}

	/**
	 * By default, each call to <code>compile</code> creates a new sandbox (C:\temp\ajcSandbox\ajtTestxxx.tmp, or
	 * /tmp/ajcSandbox/ajcTestxxx.tmp depending on your platform). To write a test that performs multiple (non-incremental)
	 * compiles, building on the results of previous compilations, set 'should empty sandbox' to false after the first compile,
	 * which will cause subsequent compiles in the test to use the same directory and contents.
	 */
	public void setShouldEmptySandbox(boolean empty) {
		this.shouldEmptySandbox = empty;
	}

	/**
	 * Call the compiler with the given arguments (args are exactly the same as you would pass to ajc on the command-line). The
	 * results of the compile are returned in a <code>CompilationResult</code>, which provides for easy testing of results.
	 * <p>
	 * The compilation happens in a sandbox (C:\temp\ajcSandbox\ajTestxxx.tmp or /tmp/ajcSandbox/ajcTestxxx.tmp depending on
	 * platform). Compiler arguments are adapted to the sandbox as follows.
	 * </p>
	 * <p>
	 * For every file or directory listed in an argument (source file, or component of inpath, aspectpath, sourceroots,
	 * classpath,...), if the file is specified using an absolute path then it is left unchanged, but if the file is specified using
	 * a relative path, and a base directory (see setBaseDir) has been provided, then files/directories are copied from the base
	 * directory to the sandbox, and the compiler arguments adjusted to reflect their new location.
	 * </p>
	 * <p>
	 * For example, given a baseDir of "tests/pr12345" and a compile command: "ajc src/A.java src/B.java", the files in
	 *
	 * <pre>
	 *    tests/pr12345/
	 *                  src/
	 *                      A.java
	 *                      B.java
	 * </pre>
	 *
	 * are copied to:
	 *
	 * <pre>
	 *     ajcSandbox/ajcTestxxx.tmp/
	 *                               src/
	 *                                   A.java
	 *                                   B.java
	 * </pre>
	 * <p>
	 * If no classpath is specified (no -classpath in the arguments) the classpath will be set to include the sandbox directory,
	 * testing-client/bin (for the Tester class), and runtime/bin (for the AspectJ runtime). If a classpath <i>is</i> specified,
	 * then any relative directories in it will be made relative to the sandbox, and the testing-client and runtime bin directories
	 * are also added.
	 * </p>
	 * <p>
	 * If no output directory is specified (no -d in the arguments), the output directory is set to the sandbox. If a directory is
	 * specified, and the path is relative, it will be made relative to the sandbox.
	 * </p>
	 * <ul>
	 * </ul>
	 * </p>
	 *
	 * @param args The compiler arguments.
	 * @return a CompilationResult object with all the messages produced by the compiler, a description of the ajc command that was
	 *         issued, and the standard output and error of the compile (excluding messages which are provided separately)
	 * @throws IOException
	 * @see org.aspectj.tools.ajc.CompilationResult
	 */
	public CompilationResult compile(String[] args) throws IOException {
		incrementalStage = 10;
		return compile(args, false);
	}

	private CompilationResult compile(String[] args, boolean isIncremental) throws IOException {
		result = null;
		ajcArgs = args;
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		PrintStream pout = new PrintStream(out);
		ByteArrayOutputStream err = new ByteArrayOutputStream();
		PrintStream perr = new PrintStream(err);
		PrintStream systemOut = System.out;
		PrintStream systemErr = System.err;
		System.setOut(pout);
		System.setErr(perr);

		List<IMessage> fails = new ArrayList<>();
		List<IMessage> errors = new ArrayList<>();
		List<IMessage> warnings = new ArrayList<>();
		List<IMessage> infos = new ArrayList<>();
		List<IMessage> weaves = new ArrayList<>();

		try {
			if (!isIncremental && shouldEmptySandbox) {
				sandbox = TestUtil.createEmptySandbox();
			}
			args = adjustToSandbox(args, !isIncremental);
			MessageHandler holder = new MessageHandler();
			holder.setInterceptor(new AbortInterceptor());
			main.setHolder(holder);
			if (incrementalStage == 10 && hasSpecifiedIncremental(args)) {
				// important to sleep after preparing the sandbox on first incremental stage (see notes in pr90806)
				try {
					Thread.sleep(1000);
				} catch (Exception e) {
				}
			}
			if (isIncremental) {
				controller.doIncremental(holder);
			} else {
				main.runMain(args, false);
			}
			addMessagesTo(infos, holder.getMessages(IMessage.INFO, false));
			addMessagesTo(warnings, holder.getWarnings());
			addMessagesTo(errors, holder.getErrors());
			addMessagesTo(fails, holder.getMessages(IMessage.FAIL, true));
			addMessagesTo(weaves, holder.getMessages(IMessage.WEAVEINFO, false));
			String stdOut = out.toString();
			String stdErr = err.toString();
			result = new CompilationResult(args, stdOut, stdErr, infos, errors, warnings, fails, weaves);
		} finally {
			System.setOut(systemOut);
			System.setErr(systemErr);
		}
		if (verbose) {
			System.err.println(result.getStandardError());
			System.out.println(result.getStandardOutput());
			System.out.println(result);
		}
		return result;
	}

	private boolean hasSpecifiedIncremental(String[] args) {
		if (args == null)
			return false;
		for (String arg : args) {
			if (arg.equals("-incremental"))
				return true;
		}
		return false;
	}

	/**
	 * After compiling for the first time with compile(), if the -incremental option was specified you can do as many subsequent
	 * incremental compiles as you like by calling this method.
	 * <p>
	 * Throws an IllegalStateException if you try and call this method without first doing a compile that specified the -incremental
	 * option.
	 * </p>
	 *
	 * @return A CompilationResult giving the results of the most recent increment.
	 * @throws IOException
	 */
	public CompilationResult doIncrementalCompile() throws IOException {
		if ((ajcArgs == null) || !isIncremental(ajcArgs)) {
			throw new IllegalStateException(
					"Can't do incremental compile unless -incremental specified and first compile has taken place");
		}
		incrementalStage += 10;
		return compile(ajcArgs, true);
	}

	/**
	 * Return the result of the last compile or incremental compile. This is the same as the return value from the compile() or
	 * doIncrementalCompile() methods.
	 */
	public CompilationResult getLastCompilationResult() {
		return result;
	}

	/**
	 * Get the sandbox directory used for the compilation.
	 */
	public File getSandboxDirectory() {
		if (sandbox == null) {
			sandbox = TestUtil.createEmptySandbox();
		}
		return sandbox;
	}

	/**
	 * Set the base directory relative to which all relative paths specified in the arguments to a compile will be interpreted.
	 */
	public void setBaseDir(File dir) {
		if ((dir != null) && !dir.isDirectory())
			throw new IllegalArgumentException(dir.getPath() + " is not a directory: "+dir.getAbsolutePath());
		baseDir = dir;
	}

	private void addMessagesTo(List<IMessage> aList, IMessage[] messages) {
		Collections.addAll(aList, messages);
	}

	private boolean isIncremental(String[] args) {
		for (String arg : args) {
			if (arg.trim().equals("-incremental"))
				return true;
		}
		return false;
	}

	/**
	 * Make every relative file name and dir be absolute under sandbox Add TESTER_PATH to classpath
	 */
	private String[] adjustToSandbox(String[] args, boolean doCopy) throws IOException {
		String[] newArgs = new String[args.length];
		boolean hasClasspath = false;
		boolean hasOutdir = false;
		for (int i = 0; i < args.length; i++) {
			newArgs[i] = args[i];
			if (FileUtil.hasSourceSuffix(args[i])) {
				File f = new File(args[i]);
				// newArgs[i] = new File(baseDir,args[i]).getAbsolutePath(); // might be quicker?
				newArgs[i] = adjustFileOrDir(f, doCopy, false).getAbsolutePath();
			} else if (args[i].endsWith(".xml") && !args[i].startsWith("-")) {
				if (i > 0 && args[i - 1].equals("-outxmlfile")) {
					// dont adjust it
				} else {
					File f = new File(args[i]);
					// newArgs[i] = new File(baseDir,args[i]).getAbsolutePath(); // might be quicker?
					newArgs[i] = adjustFileOrDir(f, doCopy, false).getAbsolutePath();
				}
			} else {
				if ((args[i].equals("-aspectpath") || args[i].equals("-inpath") || args[i].equals("-injars")
						|| args[i].equals("-outjar") || args[i].equals("-classpath") || args[i].equals("-sourceroots")
						|| args[i].equals("-Xlintfile") || args[i].equals("-extdirs") || args[i].equals("-d"))
						&& args.length > (i + 1))
				{
					newArgs[i] = args[i];
					StringBuilder buff = new StringBuilder();
					boolean copyThisTime = doCopy;
					if (args[i].equals("-d")) {
						copyThisTime = false;
						hasOutdir = true;
					}
					boolean isOutjar = args[i].equals("-outjar");
					StringTokenizer strTok = new StringTokenizer(args[++i], pathSeparator);
					while (strTok.hasMoreTokens()) {
						File f = new File(strTok.nextToken());
						buff.append(adjustFileOrDir(f, copyThisTime, isOutjar).getAbsolutePath());
						if (strTok.hasMoreTokens())
							buff.append(pathSeparator);
					}
					newArgs[i] = buff.toString();
					if (args[i - 1].equals("-classpath")) {
						hasClasspath = true;
						newArgs[i] = newArgs[i] + pathSeparator + TESTER_PATH + pathSeparator
								+ getSandboxDirectory().getAbsolutePath();
					}
				} else {
					// could be resource file
					File f = new File(args[i]);
					if (f.exists()) {
						newArgs[i] = adjustFileOrDir(f, doCopy, false).getAbsolutePath();
					}
				}
			}
		}
		if (!hasClasspath) {
			String[] oldArgs = newArgs;
			newArgs = new String[oldArgs.length + 2];
			System.arraycopy(oldArgs, 0, newArgs, 0, oldArgs.length);
			newArgs[oldArgs.length] = "-classpath";
			newArgs[oldArgs.length + 1] = TESTER_PATH + pathSeparator + getSandboxDirectory().getAbsolutePath();
		}
		if (!hasOutdir) {
			String[] oldArgs = newArgs;
			newArgs = new String[oldArgs.length + 2];
			System.arraycopy(oldArgs, 0, newArgs, 0, oldArgs.length);
			newArgs[oldArgs.length] = "-d";
			newArgs[oldArgs.length + 1] = getSandboxDirectory().getPath();
		}
		return newArgs;
	}

	private File adjustFileOrDir(File from, boolean doCopy, boolean ensureDirsExist) throws IOException {
		File to = from;
		File ret = from;
		if (!from.isAbsolute()) {
			ret = new File(sandbox, from.getPath());
			File fromParent = from.getParentFile();
			String relativeToPath = (fromParent != null) ? (fromParent.getPath() + separator) : "";
			if (baseDir != null) {
				from = new File(baseDir, from.getPath());
				//				if (ensureDirsExist) {
				//					File toMkdir = (ret.getPath().endsWith(".jar") || ret.getPath().endsWith(".zip"))?ret.getParentFile():ret;
				//					toMkdir.mkdirs();
				//				}
			}
			if (!from.exists())
				return ret;
			if (doCopy) {
				// harness requires that any files with the same name, and a different extension,
				// get copied too (e.g. .out, .err, .event files)
				if (from.isFile()) {
					final String prefix = from.getName().substring(0, from.getName().lastIndexOf('.'));
					String[] toCopy = from.getParentFile().list(new FilenameFilter() {
						@Override
						public boolean accept(File dir, String name) {
							if (name.indexOf('.') == -1)
								return false;
							String toMatch = name.substring(0, name.lastIndexOf('.'));
							return (toMatch.equals(prefix));
						}
					});
					for (String s : toCopy) {
						String toPath = relativeToPath + s;
						to = new File(sandbox, toPath);
						FileUtil.copyFile(new File(from.getParentFile(), s), to);
					}
				} else {
					FileUtil.copyFile(from, ret);
				}
			}
		}
		return ret;
	}

	public static void dumpAJDEStructureModel(AsmManager model, String prefix) {
		dumpAJDEStructureModel(model, prefix, false);
	}

	public static void dumpAJDEStructureModel(AsmManager model, String prefix, boolean useHandles) {
		System.out.println("======================================");//$NON-NLS-1$
		System.out.println("start of AJDE structure model:" + prefix); //$NON-NLS-1$

		IRelationshipMap asmRelMap = model.getRelationshipMap();
		for (String sourceOfRelationship : asmRelMap.getEntries()) {
			System.err.println("Examining source relationship handle: " + sourceOfRelationship);
			List<IRelationship> relationships = null;
			if (useHandles) {
				relationships = asmRelMap.get(sourceOfRelationship);
			} else {
				IProgramElement ipe = model.getHierarchy().findElementForHandle(sourceOfRelationship);
				relationships = asmRelMap.get(ipe);
			}
			if (relationships != null) {
				for (IRelationship relationship : relationships) {
					Relationship rel = (Relationship) relationship;
					List<String> targets = rel.getTargets();
					for (String t : targets) {
						IProgramElement link = model.getHierarchy().findElementForHandle(t);
						System.out.println(""); //$NON-NLS-1$
						System.out.println("      sourceOfRelationship " + sourceOfRelationship); //$NON-NLS-1$
						System.out.println("          relationship " + rel.getName()); //$NON-NLS-1$
						System.out.println("              target " + link.getName()); //$NON-NLS-1$
					}
				}

			}
		}
		System.out.println("End of AJDE structure model"); //$NON-NLS-1$
		System.out.println("======================================");//$NON-NLS-1$
	}
}

/*
 * So that we can drive incremental compilation easily from a unit test.
 */
class AjcCommandController extends Main.CommandController {

	private ICommand command;

	/*
	 * (non-Javadoc)
	 *
	 * @see org.aspectj.tools.ajc.Main.CommandController#doRepeatCommand()
	 */
	@Override
	boolean doRepeatCommand(ICommand command) {
		this.command = command;
		return false; // ensure that control returns to caller
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.aspectj.tools.ajc.Main.CommandController#running()
	 */
	@Override
	public boolean running() {
		return false; // so that we can come back for more...
	}

	public void doIncremental(IMessageHandler handler) {
		if (command == null)
			throw new IllegalArgumentException("Can't repeat command until it has executed at least once!");
		command.repeatCommand(handler);
	}
}

class AbortInterceptor implements IMessageHandler {

	@Override
	public boolean handleMessage(IMessage message) throws AbortException {
		if (message.getKind() == IMessage.ABORT) {
			System.err.println("***** Abort Message Received ******");
			System.err.println(CompilationAndWeavingContext.getCurrentContext());
			System.err.println(message.getMessage());
			if (message.getThrown() != null) {
				System.err.println("caused by " + message.getThrown().toString());
			}

		} // allow message to accumulate...
		return false;
	}

	@Override
	public boolean isIgnoring(Kind kind) {
		if (kind != IMessage.ABORT)
			return true;
		return false;
	}

	@Override
	public void dontIgnore(Kind kind) {
	}

	@Override
	public void ignore(Kind kind) {
	}
}
