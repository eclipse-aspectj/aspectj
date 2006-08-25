/* *******************************************************************
 * Copyright (c) 2004 IBM Corporation
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
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
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import junit.framework.AssertionFailedError;


import org.aspectj.asm.AsmManager;
import org.aspectj.asm.IProgramElement;
import org.aspectj.asm.IRelationshipMap;
import org.aspectj.asm.internal.Relationship;
import org.aspectj.bridge.AbortException;
import org.aspectj.bridge.ICommand;
import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.IMessageHandler;
import org.aspectj.bridge.MessageHandler;
import org.aspectj.bridge.IMessage.Kind;
import org.aspectj.bridge.context.CompilationAndWeavingContext;
import org.aspectj.util.FileUtil;

/**
 * The Ajc class is intended for use as part of a unit-test suite, it drives
 * the AspectJ compiler and lets you check the compilation results. Compilations
 * run in a sandbox that is created in C:\temp\ajcSandbox or /tmp/ajcSandbox
 * depending on your platform.
 * <p>
 * The expected usage of Ajc is through the TestCase superclass,
 * AjcTestCase, which provides helper methods that conveniently 
 * drive the base functions exposed by this class.
 * </p>
 * @see org.aspectj.tools.ajc.AjcTestCase
 */
public class Ajc {

	private static final String SANDBOX_NAME = "ajcSandbox";

	private static final String TESTER_PATH = 
		".."+File.separator+"testing-client"+File.separator+"bin"
	    + File.pathSeparator+".."+File.separator+"runtime"   +File.separator+"bin"
		+ File.pathSeparator+".."+File.separator+"aspectj5rt"+File.separator+"bin"
        + File.pathSeparator+".."+File.separator+"lib"       +File.separator+"junit"+File.separator+"junit.jar"
        + File.pathSeparator+".."+File.separator+"bridge"    +File.separator+"bin" 
        + File.pathSeparator+".."+File.separator+"loadtime"  +File.separator+"bin"
        + File.pathSeparator+".."+File.separator+"weaver"    +File.separator+"bin"
        + File.pathSeparator+".."+File.separator+"weaver5"   +File.separator+"bin"
        
        // When the build machine executes the tests, it is using code built into jars rather than code build into
        // bin directories.  This means for the necessary types to be found we have to put these jars on the classpath:
		+ File.pathSeparator+".."+File.separator+"aj-build"+File.separator+"jars"+File.separator+"bridge.jar"
        + File.pathSeparator+".."+File.separator+"aj-build"+File.separator+"jars"+File.separator+"util.jar"
		+ File.pathSeparator+".."+File.separator+"aj-build"+File.separator+"jars"+File.separator+"loadtime.jar"
		+ File.pathSeparator+".."+File.separator+"aj-build"+File.separator+"jars"+File.separator+"weaver.jar"
		+ File.pathSeparator+".."+File.separator+"aj-build"+File.separator+"jars"+File.separator+"weaver5.jar"
		+ File.pathSeparator+".."+File.separator+"aj-build"+File.separator+"jars"+File.separator+"asm.jar"
		+ File.pathSeparator+".."+File.separator+"lib"     +File.separator+"test"+File.separator+"testing-client.jar"
		// hmmm, this next one should perhaps point to an aj-build jar...
		+ File.pathSeparator+".."+File.separator+"lib"     +File.separator+"test"+File.separator+"aspectjrt.jar"
		;



	private CompilationResult result;
	private File sandbox;
	private File baseDir;
	private Main main;
	private String[] ajcArgs;
	private int incrementalStage = 10;
	private boolean shouldEmptySandbox = true;
	private AjcCommandController controller;
	private static boolean verbose = System.getProperty("org.aspectj.tools.ajc.Ajc.verbose","true").equals("true");

	/**
	 * Constructs a new Ajc instance, with a new AspectJ compiler
	 * inside. 
	 */
	public Ajc() {
		main = new Main();
		controller = new AjcCommandController();
		main.setController(controller);
	}
	
	/**
	 * By default, each call to <code>compile</code> creates a new
	 * sandbox (C:\temp\ajcSandbox\ajtTestxxx.tmp, or /tmp/ajcSandbox/ajcTestxxx.tmp
	 * depending on your platform). To write a test that performs multiple
	 * (non-incremental) compiles, building on the results of previous
	 * compilations, set 'should empty sandbox' to false after the first
	 * compile, which will cause subsequent compiles in the test to use the
	 * same directory and contents.
	 */
	public void setShouldEmptySandbox(boolean empty) {
		this.shouldEmptySandbox = empty;
	}
	
	/**
	 * Call the compiler with the given arguments (args are exactly the same
	 * as you would pass to ajc on the command-line). The results of the 
	 * compile are returned in a <code>CompilationResult</code>, which
	 * provides for easy testing of results.
	 * <p>The compilation happens in a sandbox (C:\temp\ajcSandbox\ajTestxxx.tmp or
	 * /tmp/ajcSandbox/ajcTestxxx.tmp depending on platform). Compiler arguments are
	 * adapted to the sandbox as follows.</p>
	 * <p>For every file or directory listed in an argument (source file, or component
	 * of inpath, aspectpath, sourceroots, classpath,...), if the file is specified
	 * using an absolute path then it is left unchanged, but if the file is specified
	 * using a relative path, and a base directory (see setBaseDir) has been provided,
	 * then files/directories are copied from the base directory to the sandbox, and the
	 * compiler arguments adjusted to reflect their new location. 
	 * </p>
	 * <p>For example, given a baseDir of "tests/pr12345" and a compile command: 
	 * "ajc src/A.java src/B.java", the files in 
	 * <pre>
	 *    tests/pr12345/
	 *                  src/
	 *                      A.java
	 *                      B.java
	 * </pre>
	 * are copied to:
	 * <pre>
	 *     ajcSandbox/ajcTestxxx.tmp/
	 *                               src/
	 *                                   A.java
	 *                                   B.java
	 * </pre>
	 * <p>
	 * If no classpath is specified (no -classpath in the arguments) the classpath will
	 * be set to include the sandbox directory, testing-client/bin (for the Tester class), 
	 * and runtime/bin (for the AspectJ runtime). If a classpath <i>is</i> specified,
	 * then any relative directories in it will be made relative to the sandbox, and
	 * the testing-client and runtime bin directories are also added.
	 * </p>
	 * <p>
	 * If no output directory is specified (no -d in the arguments), the output directory
	 * is set to the sandbox. If a directory is specified, and the path is relative, it
	 * will be made relative to the sandbox.
	 * </p>
	 * <ul>
	 * </ul>
	 * </p>
	 * @param args The compiler arguments. 
	 * @return a CompilationResult object with all the messages produced by
	 * the compiler, a description of the ajc command that was issued,
	 * and the standard output and error of the compile (excluding messages
	 * which are provided separately)
	 * @throws IOException
	 * @see org.aspectj.tools.ajc.CompilationResult
	 */
	public CompilationResult compile(String[] args) throws IOException {
		incrementalStage = 10;
		return compile(args,false);
	}
	
	private CompilationResult compile(String[] args,boolean isIncremental) throws IOException {
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

		List fails = new ArrayList();
		List errors = new ArrayList();
		List warnings = new ArrayList();
		List infos = new ArrayList();
		List weaves = new ArrayList();
		
		try {
			if (!isIncremental && shouldEmptySandbox) {
				sandbox = createEmptySandbox();
			}
			args = adjustToSandbox(args,!isIncremental);
			MessageHandler holder = new MessageHandler();
			holder.setInterceptor(new AbortInterceptor());
			main.setHolder(holder);
			if (incrementalStage==10 && hasSpecifiedIncremental(args)) {
			  // important to sleep after preparing the sandbox on first incremental stage (see notes in pr90806)
			  try { Thread.sleep(1000); } catch (Exception e) {}
			}
			if (isIncremental) {
				controller.doIncremental(holder);
			} else {
				main.runMain(args,false);
			}
			addMessagesTo(infos,holder.getMessages(IMessage.INFO,false));
			addMessagesTo(warnings,holder.getWarnings());
			addMessagesTo(errors,holder.getErrors());
			addMessagesTo(fails,holder.getMessages(IMessage.FAIL,true));
			addMessagesTo(weaves,holder.getMessages(IMessage.WEAVEINFO,false));
			String stdOut = new String(out.toByteArray());
			String stdErr = new String(err.toByteArray());
			result = new CompilationResult(args,stdOut,stdErr,infos,errors,warnings,fails,weaves);
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
		if (args==null) return false;
		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("-incremental")) return true;
		}
		return false;
	}

	/**
	 * After compiling for the first time with compile(), if the -incremental option was specified
	 * you can do as many subsequent incremental compiles as you like by calling this method.
	 * <p>
	 * Throws an IllegalStateException if you try and call this method without first doing a compile
	 * that specified the -incremental option.
	 * </p>
	 * @return A CompilationResult giving the results of the most recent increment.
	 * @throws IOException
	 */
	public CompilationResult doIncrementalCompile() throws IOException {
		if ((ajcArgs == null) || !isIncremental(ajcArgs)) {
			throw new IllegalStateException("Can't do incremental compile unless -incremental specified and first compile has taken place");
		}
		incrementalStage += 10;
		return compile(ajcArgs,true);
	}
	
	/**
	 * Return the result of the last compile or incremental compile. This is the same as the 
	 * return value from the compile() or doIncrementalCompile() methods.
	 */
	public CompilationResult getLastCompilationResult() { 
		return result;
	}
	
	/**
	 * Get the sandbox directory used for the compilation.
	 */
	public File getSandboxDirectory() {
		if (sandbox == null) {sandbox = createEmptySandbox();}
		return sandbox;
	}
	
	/**
	 * Set the base directory relative to which all relative paths specified in the arguments to a compile will be
	 * interpreted.
	 */
	public void setBaseDir(File dir) {
		if ((dir != null) && !dir.isDirectory()) throw new IllegalArgumentException(dir.getPath() + " is not a directory");
		baseDir = dir;
	}
	
	private void addMessagesTo(List aList, IMessage[] messages) {
		for (int i = 0; i < messages.length; i++) {
			aList.add(messages[i]);
		}
	}
	
	private boolean isIncremental(String[] args) {
		for (int i = 0; i < args.length; i++) {
			if (args[i].trim().equals("-incremental")) return true;
		}
		return false;
	}

	public static File createEmptySandbox() {
		File sandbox;
		
		String os = System.getProperty("os.name");
		File tempDir = null;
		// AMC - I did this rather than use the JDK default as I hate having to go look
		// in c:\documents and settings\......... for the results of a failed test.
		if (os.startsWith("Windows")) {
			tempDir = new File("C:\\temp");
			if (!tempDir.exists()) {tempDir.mkdir();}
		} else {
		 	tempDir = new File("/tmp");
		}
		File sandboxRoot = new File(tempDir,SANDBOX_NAME);
		if (!sandboxRoot.exists()) {
			sandboxRoot.mkdir();
		}
	
	
		try {
			File workspace = new File(".." + File.separator);
			String workspaceName = workspace.getCanonicalPath();
			int index = workspaceName.lastIndexOf(File.separator);
			workspaceName = workspaceName.substring(index+1);
	
			File workspaceRoot = new File(sandboxRoot,workspaceName);
			if (!workspaceRoot.exists()) {
				workspaceRoot.mkdir();
			}
			
			FileUtil.deleteContents(workspaceRoot);
	
			sandbox = File.createTempFile("ajcTest",".tmp",workspaceRoot);
			sandbox.delete();
			sandbox.mkdir();
	
		} catch (IOException ioEx) {
			throw new AssertionFailedError("Unable to create sandbox directory for test");
		}
		
		return sandbox;
	}

	/**
	 * Make every relative file name and dir be absolute under sandbox
	 * Add TESTER_PATH to classpath
	 */
	private String[] adjustToSandbox(String[] args,boolean doCopy) throws IOException {
		String[] newArgs = new String[args.length];
		boolean hasClasspath = false;
		boolean hasOutdir = false;
		for (int i = 0; i < args.length; i++) {
			newArgs[i] = args[i];
			if (FileUtil.hasSourceSuffix(args[i])) {
				File f = new File(args[i]);
				//newArgs[i] = new File(baseDir,args[i]).getAbsolutePath(); // might be quicker?
				newArgs[i] = adjustFileOrDir(f,doCopy).getAbsolutePath();
			} else {
				if ((args[i].equals("-aspectpath") ||
				     args[i].equals("-inpath") ||
					 args[i].equals("-injars") ||
					 args[i].equals("-outjar") ||
					 args[i].equals("-classpath") ||
					 args[i].equals("-sourceroots") ||
					 args[i].equals("-Xlintfile") ||
					 args[i].equals("-extdirs") ||
					 args[i].equals("-d")) &&
					args.length > (i+1)) {
					newArgs[i] = args[i];
					StringBuffer buff = new StringBuffer();
					boolean copyThisTime = doCopy;
					if (args[i].equals("-d")) { 
						copyThisTime = false;
						hasOutdir = true;
					}
				    StringTokenizer strTok = new StringTokenizer(args[++i],File.pathSeparator);
				    while (strTok.hasMoreTokens()) {
				    	File f = new File(strTok.nextToken());
				    	buff.append(adjustFileOrDir(f,copyThisTime).getAbsolutePath());
				    	if (strTok.hasMoreTokens()) buff.append(File.pathSeparator);
				    }
				    newArgs[i] = buff.toString();
				    if (args[i-1].equals("-classpath")) {
				    	hasClasspath = true;
				    	newArgs[i] = newArgs[i] + File.pathSeparator + TESTER_PATH + File.pathSeparator + getSandboxDirectory().getAbsolutePath();
				    }
				} else {
					// could be resource file
					File f = new File(args[i]);
					if (f.exists()) {
						newArgs[i] = adjustFileOrDir(f,doCopy).getAbsolutePath();
					}
				}
			} 
		}
		if (!hasClasspath) {
			String[] oldArgs = newArgs;
			newArgs = new String[oldArgs.length + 2];
			System.arraycopy(oldArgs,0,newArgs,0,oldArgs.length);
			newArgs[oldArgs.length] = "-classpath";
			newArgs[oldArgs.length + 1] = TESTER_PATH + File.pathSeparator + getSandboxDirectory().getAbsolutePath();
		}
		if (!hasOutdir) {
			String[] oldArgs = newArgs;
			newArgs = new String[oldArgs.length + 2];
			System.arraycopy(oldArgs,0,newArgs,0,oldArgs.length);
			newArgs[oldArgs.length] = "-d";
			newArgs[oldArgs.length + 1] = getSandboxDirectory().getPath();			
		}
		return newArgs;
	}
	
	private File adjustFileOrDir(File from,boolean doCopy) throws IOException {
		File to = from;
		File ret = from;
		if (!from.isAbsolute()) {
			ret = new File(sandbox,from.getPath());
			File fromParent = from.getParentFile();
			String relativeToPath = (fromParent != null) ? (fromParent.getPath() + File.separator) : "";
			if (baseDir != null) {
				from = new File(baseDir,from.getPath());
			}
			if (!from.exists()) return ret;
			if (doCopy) {
				// harness requires that any files with the same name, and a different extension,
				// get copied too (e.g. .out, .err, .event files)
				if (from.isFile()) {
					final String prefix = from.getName().substring(0,from.getName().lastIndexOf('.'));
					String[] toCopy = from.getParentFile().list(new FilenameFilter() {
						public boolean accept(File dir, String name) {
							if (name.indexOf('.') == -1) return false;
							String toMatch = name.substring(0,name.lastIndexOf('.'));
							return (toMatch.equals(prefix));
						}
						});
					for (int i = 0; i < toCopy.length; i++) {
						String toPath = relativeToPath + toCopy[i];
						to = new File(sandbox,toPath);
						FileUtil.copyFile(new File(from.getParentFile(),toCopy[i]),
								          to);					
					}
				} else {
					FileUtil.copyFile(from,ret);
				}
			}
		}
		return ret;
	}
	
	public static void dumpAJDEStructureModel(String prefix) {
		dumpAJDEStructureModel(prefix, false);
	}
	
	public static void dumpAJDEStructureModel(String prefix, boolean useHandles) {
		System.out.println("======================================");//$NON-NLS-1$
		System.out.println("start of AJDE structure model:"+prefix); //$NON-NLS-1$

		IRelationshipMap asmRelMap = AsmManager.getDefault().getRelationshipMap();
		for (Iterator iter = asmRelMap.getEntries().iterator(); iter.hasNext();) {
			String sourceOfRelationship = (String) iter.next();
			System.err.println("Examining source relationship handle: "+sourceOfRelationship);
			List relationships = null;
			if (useHandles) {
				relationships = asmRelMap.get(sourceOfRelationship);
			} else {
				IProgramElement ipe = AsmManager.getDefault().getHierarchy()
										.findElementForHandle(sourceOfRelationship);
				relationships = asmRelMap.get(ipe);
			}
			if (relationships != null) {
				for (Iterator iterator = relationships.iterator(); iterator.hasNext();) {
					Relationship rel = (Relationship) iterator.next();
					List targets = rel.getTargets();
					for (Iterator iterator2 = targets.iterator(); iterator2.hasNext();) {
						String t = (String) iterator2.next();
						IProgramElement link = AsmManager.getDefault().getHierarchy().findElementForHandle(t);
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
 * So that we can drive incremental compilation easily from a unit
 * test.
 */
class AjcCommandController extends Main.CommandController {
	
	private ICommand command;
	
	/* (non-Javadoc)
	 * @see org.aspectj.tools.ajc.Main.CommandController#doRepeatCommand()
	 */
	boolean doRepeatCommand(ICommand command) {
		this.command = command;
		return false;  // ensure that control returns to caller
	}
	
	/* (non-Javadoc)
	 * @see org.aspectj.tools.ajc.Main.CommandController#running()
	 */
	public boolean running() {
		return false;  // so that we can come back for more...
	}
	
	public void doIncremental(IMessageHandler handler) {
		if (command == null) throw new IllegalArgumentException("Can't repeat command until it has executed at least once!");
		command.repeatCommand(handler);
	}
}

class AbortInterceptor implements IMessageHandler {

	public boolean handleMessage(IMessage message) throws AbortException {
		if (message.getKind() == IMessage.ABORT) {
			System.err.println("***** Abort Message Received ******");
			System.err.println(CompilationAndWeavingContext.getCurrentContext());
			System.err.println(message.getMessage());
			if (message.getThrown() != null) {
				System.err.println("caused by " + message.getThrown().toString());
			}
			
		}  // allow message to accumulate... 
		return false;
	}

	public boolean isIgnoring(Kind kind) {
		if (kind != IMessage.ABORT) return true;
		return false;
	}

	public void dontIgnore(Kind kind) {
	}

	public void ignore(Kind kind) {
	}
}
