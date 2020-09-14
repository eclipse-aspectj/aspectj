/* *******************************************************************
 * Copyright (c) 2001-2001 Xerox Corporation,
 *               2002 Palo Alto Research Center, Incorporated (PARC)
 *               2003-2004 Contributors.
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Xerox/PARC     initial implementation
 *     Wes Isberg     2003-2004 changes
 * ******************************************************************/

package org.aspectj.tools.ant.taskdefs;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.tools.ant.AntClassLoader;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Location;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Copy;
import org.apache.tools.ant.taskdefs.Delete;
import org.apache.tools.ant.taskdefs.Execute;
import org.apache.tools.ant.taskdefs.Expand;
import org.apache.tools.ant.taskdefs.Javac;
import org.apache.tools.ant.taskdefs.LogStreamHandler;
import org.apache.tools.ant.taskdefs.MatchingTask;
import org.apache.tools.ant.taskdefs.Mkdir;
import org.apache.tools.ant.taskdefs.PumpStreamHandler;
import org.apache.tools.ant.taskdefs.Zip;
import org.apache.tools.ant.types.Commandline;
import org.apache.tools.ant.types.CommandlineJava;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.PatternSet;
import org.apache.tools.ant.types.Reference;
import org.apache.tools.ant.types.ZipFileSet;
import org.apache.tools.ant.util.TaskLogger;
import org.aspectj.bridge.AbortException;
import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.IMessage.Kind;
import org.aspectj.bridge.IMessageHandler;
import org.aspectj.bridge.IMessageHolder;
import org.aspectj.bridge.MessageHandler;
import org.aspectj.bridge.MessageUtil;
import org.aspectj.tools.ajc.Main;
import org.aspectj.util.FileUtil;
import org.aspectj.util.LangUtil;

/**
 * This runs the AspectJ 1.1 compiler, supporting all the command-line options. In 1.1.1, ajc copies resources from input jars, but
 * you can copy resources from the source directories using sourceRootCopyFilter. When not forking, things will be copied as needed
 * for each iterative compile, but when forking things are only copied at the completion of a successful compile.
 * <p>
 * See the development environment guide for usage documentation.
 *
 * @since AspectJ 1.1, Ant 1.5
 */
public class AjcTask extends MatchingTask {
	/*
	 * This task mainly converts ant specification for ajc, verbosely ignoring improper input. It also has some special features for
	 * non-obvious clients: (1) Javac compiler adapter supported in <code>setupAjc(AjcTask, Javac, File)</code> and
	 * <code>readArguments(String[])</code>; (2) testing is supported by (a) permitting the same specification to be re-run with
	 * added flags (settings once made cannot be removed); and (b) permitting recycling the task with <code>reset()</code>
	 * (untested).
	 *
	 * The parts that do more than convert ant specs are (a) code for forking; (b) code for copying resources.
	 *
	 * If you maintain/upgrade this task, keep in mind: (1) changes to the semantics of ajc (new options, new values permitted,
	 * etc.) will have to be reflected here. (2) the clients: the iajc ant script, Javac compiler adapter, maven clients of iajc,
	 * and testing code.
	 */

	// XXX move static methods after static initializer
	/**
	 * This method extracts javac arguments to ajc, and add arguments to make ajc behave more like javac in copying resources.
	 * <p>
	 * Pass ajc-specific options using compilerarg sub-element:
	 *
	 * <pre>
	 * &lt;javac srcdir=&quot;src&quot;&gt;
	 *     &lt;compilerarg compiler=&quot;...&quot; line=&quot;-argfile src/args.lst&quot;/&gt;
	 * &lt;javac&gt;
	 * </pre>
	 *
	 * Some javac arguments are not supported in this component (yet):
	 *
	 * <pre>
	 * String memoryInitialSize;
	 * boolean includeAntRuntime = true;
	 * boolean includeJavaRuntime = false;
	 * </pre>
	 *
	 * Other javac arguments are not supported in ajc 1.1:
	 *
	 * <pre>
	 * boolean optimize;
	 * String forkedExecutable;
	 * FacadeTaskHelper facade;
	 * boolean depend;
	 * String debugLevel;
	 * Path compileSourcepath;
	 * </pre>
	 *
	 * @param javac the Javac command to implement (not null)
	 * @return null if no error, or String error otherwise
	 */
	public String setupAjc(Javac javac) {
		if (null == javac) {
			return "null javac";
		}
		AjcTask ajc = this;
		// no null checks b/c AjcTask handles null input gracefully
		ajc.setProject(javac.getProject());
		ajc.setLocation(javac.getLocation());
		ajc.setTaskName("javac-iajc");
		ajc.setDebug(javac.getDebug());
		ajc.setDeprecation(javac.getDeprecation());
		ajc.setFailonerror(javac.getFailonerror());
		final boolean fork = javac.isForkedJavac();
		ajc.setFork(fork);
		if (fork) {
			ajc.setMaxmem(javac.getMemoryMaximumSize());
		}
		ajc.setNowarn(javac.getNowarn());
		ajc.setListFileArgs(javac.getListfiles());
		ajc.setVerbose(javac.getVerbose());
		ajc.setTarget(javac.getTarget());
		ajc.setSource(javac.getSource());
		ajc.setEncoding(javac.getEncoding());
		File javacDestDir = javac.getDestdir();
		if (null != javacDestDir) {
			ajc.setDestdir(javacDestDir);
			// filter requires dest dir
			// mimic Javac task's behavior in copying resources,
			ajc.setSourceRootCopyFilter("**/CVS/*,**/*.java,**/*.aj");
		}
		ajc.setBootclasspath(javac.getBootclasspath());
		ajc.setExtdirs(javac.getExtdirs());
		ajc.setClasspath(javac.getClasspath());
		// ignore srcDir -- all files picked up in recalculated file list
		// ajc.setSrcDir(javac.getSrcdir());
		ajc.addFiles(javac.getFileList());
		// arguments can override the filter, add to paths, override options
		ajc.readArguments(javac.getCurrentCompilerArgs());

		return null;
	}

	/**
	 * Find aspectjtools.jar on the task or system classpath. Accept <code>aspectj{-}tools{...}.jar</code> mainly to support build
	 * systems using maven-style re-naming (e.g., <code>aspectj-tools-1.1.0.jar</code>. Note that we search the task classpath
	 * first, though an entry on the system classpath would be loaded first, because it seems more correct as the more specific one.
	 *
	 * @return readable File for aspectjtools.jar, or null if not found.
	 */
	public static File findAspectjtoolsJar() {
		File result = null;
		ClassLoader loader = AjcTask.class.getClassLoader();
		if (loader instanceof AntClassLoader) {
			AntClassLoader taskLoader = (AntClassLoader) loader;
			String cp = taskLoader.getClasspath();
			String[] cps = LangUtil.splitClasspath(cp);
			for (int i = 0; (i < cps.length) && (null == result); i++) {
				result = isAspectjtoolsjar(cps[i]);
			}
		}
		if (null == result) {
			final Path classpath = Path.systemClasspath;
			final String[] paths = classpath.list();
			for (int i = 0; (i < paths.length) && (null == result); i++) {
				result = isAspectjtoolsjar(paths[i]);
			}
		}
		return (null == result ? null : result.getAbsoluteFile());
	}

	/** @return File if readable jar with aspectj tools name, or null */
	private static File isAspectjtoolsjar(String path) {
		if (null == path) {
			return null;
		}
		final String prefix = "aspectj";
		final String infix = "tools";
		final String altInfix = "-tools";
		final String suffix = ".jar";
		final int prefixLength = 7; // prefix.length();
		final int minLength = 16;
		// prefixLength + infix.length() + suffix.length();
		if (!path.endsWith(suffix)) {
			return null;
		}
		int loc = path.lastIndexOf(prefix);
		if ((-1 != loc) && ((loc + minLength) <= path.length())) {
			String rest = path.substring(loc + prefixLength);
			if (rest.contains(File.pathSeparator)) {
				return null;
			}
			if (rest.startsWith(infix) || rest.startsWith(altInfix)) {
				File result = new File(path);
				if (result.canRead() && result.isFile()) {
					return result;
				}
			}
		}
		return null;
	}

	/**
	 * Maximum length (in chars) of command line before converting to an argfile when forking
	 */
	private static final int MAX_COMMANDLINE = 4096;

	private static final File DEFAULT_DESTDIR = new File(".") {
		@Override
		public String toString() {
			return "(no destination dir specified)";
		}
	};

	/** do not throw BuildException on fail/abort message with usage */
	private static final String USAGE_SUBSTRING = "AspectJ-specific options";

	/** valid -X[...] options other than -Xlint variants */
	private static final List VALID_XOPTIONS;

	/** valid warning (-warn:[...]) variants */
	private static final List VALID_WARNINGS;

	/** valid debugging (-g:[...]) variants */
	private static final List VALID_DEBUG;

	/**
	 * -Xlint variants (error, warning, ignore)
	 *
	 * @see org.aspectj.weaver.Lint
	 */
	private static final List VALID_XLINT;

	public static final String COMMAND_EDITOR_NAME = AjcTask.class.getName() + ".COMMAND_EDITOR";

	static final String[] TARGET_INPUTS = new String[] { "1.1", "1.2", "1.3", "1.4", "1.5", "1.6", "1.7", "1.8", "1.9", "9", "10", "11", "12", "13", "14" };
	static final String[] SOURCE_INPUTS = new String[] { "1.3", "1.4", "1.5", "1.6", "1.7", "1.8", "1.9", "9", "10", "11", "12", "13", "14" };
	static final String[] COMPLIANCE_INPUTS = new String[] { "-1.3", "-1.4", "-1.5", "-1.6", "-1.7", "-1.8", "-1.9", "-9", "-10", "-11", "-12", "-13", "-14" };

	private static final ICommandEditor COMMAND_EDITOR;

	static {
		// many now deprecated: reweavable*
		String[] xs = new String[] { "serializableAspects", "incrementalFile", "lazyTjp", "reweavable", "reweavable:compress",
				"notReweavable", "noInline", "terminateAfterCompilation", "hasMember", "ajruntimetarget:1.2",
				"ajruntimetarget:1.5", "addSerialVersionUID"

				// , "targetNearSource", "OcodeSize",
		};
		VALID_XOPTIONS = Collections.unmodifiableList(Arrays.asList(xs));

		xs = new String[] { "constructorName", "packageDefaultMethod", "deprecation", "maskedCatchBlocks", "unusedLocals",
				"unusedArguments", "unusedImports", "syntheticAccess", "assertIdentifier", "allDeprecation", "allJavadoc",
				"charConcat", "conditionAssign",

				"emptyBlock", "fieldHiding", "finally", "indirectStatic", "intfNonInherited", "javadoc", "localHiding", "nls",
				"noEffectAssign", "pkgDefaultMethod", "semicolon", "unqualifiedField", "unusedPrivate", "unusedThrown",
				"uselessTypeCheck", "specialParamHiding", "staticReceiver", "syntheticAccess", "none" };
		VALID_WARNINGS = Collections.unmodifiableList(Arrays.asList(xs));

		xs = new String[] { "none", "lines", "vars", "source" };
		VALID_DEBUG = Collections.unmodifiableList(Arrays.asList(xs));

		xs = new String[] { "error", "warning", "ignore" };
		VALID_XLINT = Collections.unmodifiableList(Arrays.asList(xs));

		ICommandEditor editor = null;
		try {
			String editorClassName = System.getProperty(COMMAND_EDITOR_NAME);
			if (null != editorClassName) {
				ClassLoader cl = AjcTask.class.getClassLoader();
				Class editorClass = cl.loadClass(editorClassName);
				editor = (ICommandEditor) editorClass.newInstance();
			}
		} catch (Throwable t) {
			System.err.println("Warning: unable to load command editor");
			t.printStackTrace(System.err);
		}
		COMMAND_EDITOR = editor;
	}
	// ---------------------------- state and Ant interface thereto
	private boolean verbose;
	private boolean timers;
	private boolean listFileArgs;
	private boolean failonerror;
	private boolean fork;
	private String maxMem;
	private TaskLogger logger;

	// ------- single entries dumped into cmd
	protected GuardedCommand cmd;

	// ------- lists resolved in addListArgs() at execute() time
	private Path srcdir;
	private Path injars;
	private Path inpath;
	private Path classpath;
	private Path bootclasspath;
	private Path forkclasspath;
	private Path extdirs;
	private Path aspectpath;
	private Path argfiles;
	private Path inxmlfiles;
	private List ignored;
	private Path sourceRoots;
	private File xweaveDir;
	private String xdoneSignal;

	private List<CompilerArg> compilerArgs;

	// ----- added by adapter - integrate better?
	private List /* File */adapterFiles;
	private String[] adapterArguments;

	private IMessageHolder messageHolder;
	private ICommandEditor commandEditor;

	// -------- resource-copying
	/** true if copying injar non-.class files to the output jar */
	private boolean copyInjars;
	private boolean copyInpath;

	/** non-null if copying all source root files but the filtered ones */
	private String sourceRootCopyFilter;

	/** non-null if copying all inpath dir files but the filtered ones */
	private String inpathDirCopyFilter;

	/** directory sink for classes */
	private File destDir;

	/** zip file sink for classes */
	private File outjar;

	/** track whether we've supplied any temp outjar */
	private boolean outjarFixedup;

	/**
	 * When possibly copying resources to the output jar, pass ajc a fake output jar to copy from, so we don't change the
	 * modification time of the output jar when copying injars/inpath into the actual outjar.
	 */
	private File tmpOutjar;

	private boolean executing;

	/** non-null only while executing in same vm */
	private Main main;

	/** true only when executing in other vm */
	private boolean executingInOtherVM;

	/** true if -incremental */
	private boolean inIncrementalMode;

	/** true if -XincrementalFile (i.e, setTagFile) */
	private boolean inIncrementalFileMode;

	/** log command in non-verbose mode */
	private boolean logCommand;

	/** used when forking */
	private CommandlineJava javaCmd = new CommandlineJava();

	// also note MatchingTask grabs source files...

	public AjcTask() {
		reset();
	}

	/** to use this same Task more than once (testing) */
	public void reset() { // XXX possible to reset MatchingTask?
		// need declare for "all fields initialized in ..."
		adapterArguments = null;
		adapterFiles = new ArrayList();
		compilerArgs = null;
		argfiles = null;
		inxmlfiles = null;
		executing = false;
		aspectpath = null;
		bootclasspath = null;
		classpath = null;
		cmd = new GuardedCommand();
		copyInjars = false;
		copyInpath = false;
		destDir = DEFAULT_DESTDIR;
		executing = false;
		executingInOtherVM = false;
		extdirs = null;
		failonerror = true; // non-standard default
		forkclasspath = null;
		inIncrementalMode = false;
		inIncrementalFileMode = false;
		ignored = new ArrayList();
		injars = null;
		inpath = null;
		listFileArgs = false;
		maxMem = null;
		messageHolder = null;
		outjar = null;
		sourceRootCopyFilter = null;
		inpathDirCopyFilter = null;
		sourceRoots = null;
		srcdir = null;
		tmpOutjar = null;
		verbose = false;
		timers = false;
		xweaveDir = null;
		xdoneSignal = null;
		logCommand = false;
		javaCmd = new CommandlineJava();
	}

	protected void ignore(String ignored) {
		this.ignored.add(ignored + " at " + getLocation());
	}

	// ---------------------- option values

	// used by entries with internal commas
	protected String validCommaList(String list, List valid, String label) {
		return validCommaList(list, valid, label, valid.size());
	}

	protected String validCommaList(String list, List valid, String label, int max) {
		StringBuffer result = new StringBuffer();
		StringTokenizer st = new StringTokenizer(list, ",");
		int num = 0;
		while (st.hasMoreTokens()) {
			String token = st.nextToken().trim();
			num++;
			if (num > max) {
				ignore("too many entries for -" + label + ": " + token);
				break;
			}
			if (!valid.contains(token)) {
				ignore("bad commaList entry for -" + label + ": " + token);
			} else {
				if (0 < result.length()) {
					result.append(",");
				}
				result.append(token);
			}
		}
		return (0 == result.length() ? null : result.toString());
	}

	/**
	 * Controls whether annotation processing and/or compilation is done.
	 * -proc:none means that compilation takes place without annotation processing.
	 * -proc:only means that only annotation processing is done, without any subsequent compilation.
	 */
	public void setProc(String proc) {
		if (proc.equals("none")) {
			cmd.addFlag("-proc:none", true);
		} else if (proc.equals("only")) {
			cmd.addFlag("-proc:only", true);
		}
	}

	/**
	 * -processor class1[,class2,class3...]
	 *  Names of the annotation processors to run. This bypasses the default discovery process.
	 */
	public void setProcessor(String processors) {
		cmd.addFlagged("-processor", processors);
	}

	/**
	 * -processorpath path
	 * Specify where to find annotation processors; if this option is not used, the class path will be searched for processors.
	 */
	public void setProcessorpath(String processorpath) {
		cmd.addFlagged("-processorpath", processorpath);
	}

	/**
	 * -s dir
	 * Specify the directory where to place generated source files. The directory must already exist; javac will not create it.
	 * If a class is part of a package, the compiler puts the source file in a subdirectory reflecting the package name,
	 * creating directories as needed.
	 *
	 * For example, if you specify -s C:\mysrc and the class is called com.mypackage.MyClass,
	 * then the source file will be placed in C:\mysrc\com\mypackage\MyClass.java.
	 */
	public void setS(String s) {
		cmd.addFlagged("-s", s);
	}

	public void setIncremental(boolean incremental) {
		cmd.addFlag("-incremental", incremental);
		inIncrementalMode = incremental;
	}

	public void setLogCommand(boolean logCommand) {
		this.logCommand = logCommand;
	}

	public void setHelp(boolean help) {
		cmd.addFlag("-help", help);
	}

	public void setVersion(boolean version) {
		cmd.addFlag("-version", version);
	}

	public void setXTerminateAfterCompilation(boolean b) {
		cmd.addFlag("-XterminateAfterCompilation", b);
	}

	public void setXReweavable(boolean reweavable) {
		cmd.addFlag("-Xreweavable", reweavable);
	}

	public void setXmlConfigured(boolean xmlConfigured) {
		cmd.addFlag("-xmlConfigured", xmlConfigured);
	}

	public void setXJoinpoints(String optionalJoinpoints) {
		cmd.addFlag("-Xjoinpoints:" + optionalJoinpoints, true);
	}

	public void setCheckRuntimeVersion(boolean b) {
		cmd.addFlag("-checkRuntimeVersion:" + b, true);
	}

	public void setXNoWeave(boolean b) {
		if (logger != null) {
			logger.warning("the noweave option is no longer required and is being ignored");
		}
	}

	public void setNoWeave(boolean b) {
		if (logger != null) {
			logger.warning("the noweave option is no longer required and is being ignored");
		}
	}

	public void setXNotReweavable(boolean notReweavable) {
		cmd.addFlag("-XnotReweavable", notReweavable);
	}

	public void setXaddSerialVersionUID(boolean addUID) {
		cmd.addFlag("-XaddSerialVersionUID", addUID);
	}

	public void setXNoInline(boolean noInline) {
		cmd.addFlag("-XnoInline", noInline);
	}

	public void setShowWeaveInfo(boolean showweaveinfo) {
		cmd.addFlag("-showWeaveInfo", showweaveinfo);
	}

	public void setNowarn(boolean nowarn) {
		cmd.addFlag("-nowarn", nowarn);
	}

	public void setDeprecation(boolean deprecation) {
		cmd.addFlag("-deprecation", deprecation);
	}

	public void setWarn(String warnings) {
		warnings = validCommaList(warnings, VALID_WARNINGS, "warn");
		cmd.addFlag("-warn:" + warnings, (null != warnings));
	}

	public void setDebug(boolean debug) {
		cmd.addFlag("-g", debug);
	}

	public void setDebugLevel(String level) {
		level = validCommaList(level, VALID_DEBUG, "g");
		cmd.addFlag("-g:" + level, (null != level));
	}

	public void setEmacssym(boolean emacssym) {
		cmd.addFlag("-emacssym", emacssym);
	}

	public void setCrossrefs(boolean on) {
		cmd.addFlag("-crossrefs", on);
	}

	/**
	 * -Xlint - set default level of -Xlint messages to warning (same as <code>-Xlint:warning</code>)
	 */
	public void setXlintwarnings(boolean xlintwarnings) {
		cmd.addFlag("-Xlint", xlintwarnings);
	}

	/**
	 * -Xlint:{error|warning|info} - set default level for -Xlint messages
	 *
	 * @param xlint the String with one of error, warning, ignored
	 */
	public void setXlint(String xlint) {
		xlint = validCommaList(xlint, VALID_XLINT, "Xlint", 1);
		cmd.addFlag("-Xlint:" + xlint, (null != xlint));
	}

	/**
	 * -Xlintfile {lint.properties} - enable or disable specific forms of -Xlint messages based on a lint properties file (default
	 * is <code>org/aspectj/weaver/XLintDefault.properties</code>)
	 *
	 * @param xlintFile the File with lint properties
	 */
	public void setXlintfile(File xlintFile) {
		cmd.addFlagged("-Xlintfile", xlintFile.getAbsolutePath());
	}

	public void setPreserveAllLocals(boolean preserveAllLocals) {
		cmd.addFlag("-preserveAllLocals", preserveAllLocals);
	}

	public void setNoImportError(boolean noImportError) {
		cmd.addFlag("-warn:-unusedImport", noImportError);
	}

	public void setEncoding(String encoding) {
		cmd.addFlagged("-encoding", encoding);
	}

	public void setLog(File file) {
		cmd.addFlagged("-log", file.getAbsolutePath());
	}

	public void setProceedOnError(boolean proceedOnError) {
		cmd.addFlag("-proceedOnError", proceedOnError);
	}

	public void setVerbose(boolean verbose) {
		cmd.addFlag("-verbose", verbose);
		this.verbose = verbose;
	}

	public void setTimers(boolean timers) {
		cmd.addFlag("-timers", timers);
		this.timers = timers;
	}

	public void setListFileArgs(boolean listFileArgs) {
		this.listFileArgs = listFileArgs;
	}

	public void setReferenceInfo(boolean referenceInfo) {
		cmd.addFlag("-referenceInfo", referenceInfo);
	}

	public void setTime(boolean time) {
		cmd.addFlag("-time", time);
	}

	public void setNoExit(boolean noExit) {
		cmd.addFlag("-noExit", noExit);
	}

	public void setFailonerror(boolean failonerror) {
		this.failonerror = failonerror;
	}

	/**
	 * @return true if fork was set
	 */
	public boolean isForked() {
		return fork;
	}

	public void setFork(boolean fork) {
		this.fork = fork;
	}

	public void setMaxmem(String maxMem) {
		this.maxMem = maxMem;
	}

	/** support for nested &lt;jvmarg&gt; elements */
	public Commandline.Argument createJvmarg() {
		return this.javaCmd.createVmArgument();
	}

	public static class CompilerArg {

		private String value;

		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}

		@Override
		public String toString() {
			return value;
		}
	}

	public CompilerArg createCompilerarg() {
		CompilerArg compilerArg = new CompilerArg();
		if (compilerArgs == null) {
			compilerArgs = new ArrayList<>();
		}
		compilerArgs.add(compilerArg);
		return compilerArg;
	}

	// ----------------
	public void setTagFile(File file) {
		inIncrementalMode = true;
		cmd.addFlagged(Main.CommandController.TAG_FILE_OPTION, file.getAbsolutePath());
		inIncrementalFileMode = true;
	}

	public void setOutjar(File file) {
		if (DEFAULT_DESTDIR != destDir) {
			String e = "specifying both output jar (" + file + ") and destination dir (" + destDir + ")";
			throw new BuildException(e);
		}
		outjar = file;
		outjarFixedup = false;
		tmpOutjar = null;
	}

	public void setOutxml(boolean outxml) {
		cmd.addFlag("-outxml", outxml);
	}

	public void setOutxmlfile(String name) {
		cmd.addFlagged("-outxmlfile", name);
	}

	public void setDestdir(File dir) {
		if (null != outjar) {
			String e = "specifying both output jar (" + outjar + ") and destination dir (" + dir + ")";
			throw new BuildException(e);
		}
		cmd.addFlagged("-d", dir.getAbsolutePath());
		destDir = dir;
	}

	/**
	 * @param input a String in TARGET_INPUTS
	 */
	public void setTarget(String input) {
		String ignore = cmd.addOption("-target", TARGET_INPUTS, input);
		if (null != ignore) {
			ignore(ignore);
		}
	}

	/**
	 * Language compliance level. If not set explicitly, eclipse default holds.
	 *
	 * @param input a String in COMPLIANCE_INPUTS
	 */
	public void setCompliance(String input) {
		String ignore = cmd.addOption(null, COMPLIANCE_INPUTS, input);
		if (null != ignore) {
			ignore(ignore);
		}
	}

	/**
	 * Source compliance level. If not set explicitly, eclipse default holds.
	 *
	 * @param input a String in SOURCE_INPUTS
	 */
	public void setSource(String input) {
		String ignore = cmd.addOption("-source", SOURCE_INPUTS, input);
		if (null != ignore) {
			ignore(ignore);
		}
	}

	public void setParameters(boolean b) {
		cmd.addFlag("-parameters",b);
	}

	/**
	 * Flag to copy all non-.class contents of injars to outjar after compile completes. Requires both injars and outjar.
	 *
	 * @param doCopy
	 */
	public void setCopyInjars(boolean doCopy) {
		ignore("copyInJars");
		log("copyInjars not required since 1.1.1.\n", Project.MSG_WARN);
		// this.copyInjars = doCopy;
	}

	/**
	 * Option to copy all files from all source root directories except those specified here. If this is specified and sourceroots
	 * are specified, then this will copy all files except those specified in the filter pattern. Requires sourceroots.
	 *
	 * @param filter a String acceptable as an excludes filter for an Ant Zip fileset.
	 */
	public void setSourceRootCopyFilter(String filter) {
		this.sourceRootCopyFilter = filter;
	}

	/**
	 * Option to copy all files from all inpath directories except the files specified here. If this is specified and inpath
	 * directories are specified, then this will copy all files except those specified in the filter pattern. Requires inpath. If
	 * the input does not contain "**\/*.class", then this prepends it, to avoid overwriting woven classes with unwoven input.
	 *
	 * @param filter a String acceptable as an excludes filter for an Ant Zip fileset.
	 */
	public void setInpathDirCopyFilter(String filter) {
		if (null != filter) {
			if (!filter.contains("**/*.class")) {
				filter = "**/*.class," + filter;
			}
		}
		this.inpathDirCopyFilter = filter;
	}

	public void setX(String input) { // ajc-only eajc-also docDone
		StringTokenizer tokens = new StringTokenizer(input, ",", false);
		while (tokens.hasMoreTokens()) {
			String token = tokens.nextToken().trim();
			if (1 < token.length()) {
				// new special case: allow -Xset:anything
				if (VALID_XOPTIONS.contains(token) || token.indexOf("set:") == 0 || token.indexOf("joinpoints:") == 0) {
					cmd.addFlag("-X" + token, true);
				} else {
					ignore("-X" + token);
				}
			}
		}
	}

	public void setXDoneSignal(String doneSignal) {
		this.xdoneSignal = doneSignal;
	}

	/** direct API for testing */
	public void setMessageHolder(IMessageHolder holder) {
		this.messageHolder = holder;
	}

	/**
	 * Setup custom message handling.
	 *
	 * @param className the String fully-qualified-name of a class reachable from this object's class loader, implementing
	 *        IMessageHolder, and having a public no-argument constructor.
	 * @throws BuildException if unable to create instance of className
	 */
	public void setMessageHolderClass(String className) {
		try {
			Class mclass = Class.forName(className);
			IMessageHolder holder = (IMessageHolder) mclass.newInstance();
			setMessageHolder(holder);
		} catch (Throwable t) {
			String m = "unable to instantiate message holder: " + className;
			throw new BuildException(m, t);
		}
	}

	/** direct API for testing */
	public void setCommandEditor(ICommandEditor editor) {
		this.commandEditor = editor;
	}

	/**
	 * Setup command-line filter. To do this staticly, define the environment variable
	 * <code>org.aspectj.tools.ant.taskdefs.AjcTask.COMMAND_EDITOR</code> with the <code>className</code> parameter.
	 *
	 * @param className the String fully-qualified-name of a class reachable from this object's class loader, implementing
	 *        ICommandEditor, and having a public no-argument constructor.
	 * @throws BuildException if unable to create instance of className
	 */
	public void setCommandEditorClass(String className) { // skip Ant interface?
		try {
			Class mclass = Class.forName(className);
			setCommandEditor((ICommandEditor) mclass.newInstance());
		} catch (Throwable t) {
			String m = "unable to instantiate command editor: " + className;
			throw new BuildException(m, t);
		}
	}

	// ---------------------- Path lists

	/**
	 * Add path elements to source path and return result. Elements are added even if they do not exist.
	 *
	 * @param source the Path to add to - may be null
	 * @param toAdd the Path to add - may be null
	 * @return the (never-null) Path that results
	 */
	protected Path incPath(Path source, Path toAdd) {
		if (null == source) {
			source = new Path(project);
		}
		if (null != toAdd) {
			source.append(toAdd);
		}
		return source;
	}

	public void setSourcerootsref(Reference ref) {
		createSourceRoots().setRefid(ref);
	}

	public void setSourceRoots(Path roots) {
		sourceRoots = incPath(sourceRoots, roots);
	}

	public Path createSourceRoots() {
		if (sourceRoots == null) {
			sourceRoots = new Path(project);
		}
		return sourceRoots.createPath();
	}

	public void setXWeaveDir(File file) {
		if ((null != file) && file.isDirectory() && file.canRead()) {
			xweaveDir = file;
		}
	}

	public void setInjarsref(Reference ref) {
		createInjars().setRefid(ref);
	}

	public void setInpathref(Reference ref) {
		createInpath().setRefid(ref);
	}

	public void setInjars(Path path) {
		injars = incPath(injars, path);
	}

	public void setInpath(Path path) {
		inpath = incPath(inpath, path);
	}

	public Path createInjars() {
		if (injars == null) {
			injars = new Path(project);
		}
		return injars.createPath();
	}

	public Path createInpath() {
		if (inpath == null) {
			inpath = new Path(project);
		}
		return inpath.createPath();
	}

	public void setClasspath(Path path) {
		classpath = incPath(classpath, path);
	}

	public void setClasspathref(Reference classpathref) {
		createClasspath().setRefid(classpathref);
	}

	public Path createClasspath() {
		if (classpath == null) {
			classpath = new Path(project);
		}
		return classpath.createPath();
	}

	public void setBootclasspath(Path path) {
		bootclasspath = incPath(bootclasspath, path);
	}

	public void setBootclasspathref(Reference bootclasspathref) {
		createBootclasspath().setRefid(bootclasspathref);
	}

	public Path createBootclasspath() {
		if (bootclasspath == null) {
			bootclasspath = new Path(project);
		}
		return bootclasspath.createPath();
	}

	public void setForkclasspath(Path path) {
		forkclasspath = incPath(forkclasspath, path);
	}

	public void setForkclasspathref(Reference forkclasspathref) {
		createForkclasspath().setRefid(forkclasspathref);
	}

	public Path createForkclasspath() {
		if (forkclasspath == null) {
			forkclasspath = new Path(project);
		}
		return forkclasspath.createPath();
	}

	public void setExtdirs(Path path) {
		extdirs = incPath(extdirs, path);
	}

	public void setExtdirsref(Reference ref) {
		createExtdirs().setRefid(ref);
	}

	public Path createExtdirs() {
		if (extdirs == null) {
			extdirs = new Path(project);
		}
		return extdirs.createPath();
	}

	public void setAspectpathref(Reference ref) {
		createAspectpath().setRefid(ref);
	}

	public void setAspectpath(Path path) {
		aspectpath = incPath(aspectpath, path);
	}

	public Path createAspectpath() {
		if (aspectpath == null) {
			aspectpath = new Path(project);
		}
		return aspectpath.createPath();
	}

	public void setSrcDir(Path path) {
		srcdir = incPath(srcdir, path);
	}

	public Path createSrc() {
		return createSrcdir();
	}

	public Path createSrcdir() {
		if (srcdir == null) {
			srcdir = new Path(project);
		}
		return srcdir.createPath();
	}

	/** @return true if in incremental mode (command-line or file) */
	public boolean isInIncrementalMode() {
		return inIncrementalMode;
	}

	/** @return true if in incremental file mode */
	public boolean isInIncrementalFileMode() {
		return inIncrementalFileMode;
	}

	public void setArgfilesref(Reference ref) {
		createArgfiles().setRefid(ref);
	}

	public void setArgfiles(Path path) { // ajc-only eajc-also docDone
		argfiles = incPath(argfiles, path);
	}

	public Path createArgfiles() {
		if (argfiles == null) {
			argfiles = new Path(project);
		}
		return argfiles.createPath();
	}

	public void setInxmlref(Reference ref) {
		createArgfiles().setRefid(ref);
	}

	public void setInxml(Path path) { // ajc-only eajc-also docDone
		inxmlfiles = incPath(inxmlfiles, path);
	}

	public Path createInxml() {
		if (inxmlfiles == null) {
			inxmlfiles = new Path(project);
		}
		return inxmlfiles.createPath();
	}

	// ------------------------------ run

	/**
	 * Compile using ajc per settings.
	 *
	 * @exception BuildException if the compilation has problems or if there were compiler errors and failonerror is true.
	 */
	@Override
	public void execute() throws BuildException {
		this.logger = new TaskLogger(this);
		if (executing) {
			throw new IllegalStateException("already executing");
		} else {
			executing = true;
		}
		setupOptions();
		verifyOptions();
		try {
			String[] args = makeCommand();
			if (logCommand) {
				log("ajc " + Arrays.asList(args));
			} else {
				logVerbose("ajc " + Arrays.asList(args));
			}
			if (!fork) {
				executeInSameVM(args);
			} else { // when forking, Adapter handles failonerror
				executeInOtherVM(args);
			}
		} catch (BuildException e) {
			throw e;
		} catch (Throwable x) {
			this.logger.error(Main.renderExceptionForUser(x));
			throw new BuildException("IGNORE -- See " + LangUtil.unqualifiedClassName(x) + " rendered to ant logger");
		} finally {
			executing = false;
			if (null != tmpOutjar) {
				tmpOutjar.delete();
			}
		}
	}

	/**
	 * Halt processing. This tells main in the same vm to quit. It fails when running in forked mode.
	 *
	 * @return true if not in forked mode and main has quit or been told to quit
	 */
	public boolean quit() {
		if (executingInOtherVM) {
			return false;
		}
		Main me = main;
		if (null != me) {
			me.quit();
		}
		return true;
	}

	// package-private for testing
	String[] makeCommand() {
		if (0 < ignored.size()) {
            for (Object o : ignored) {
                logVerbose("ignored: " + o);
            }
		}
		// when copying resources, use temp jar for class output
		// then copy temp jar contents and resources to output jar
		if ((null != outjar) && !outjarFixedup) {
			if (copyInjars || copyInpath || (null != sourceRootCopyFilter) || (null != inpathDirCopyFilter)) {
				String path = outjar.getAbsolutePath();
				int len = FileUtil.zipSuffixLength(path);
				path = path.substring(0, path.length() - len) + ".tmp.jar";
				tmpOutjar = new File(path);
			}
			if (null == tmpOutjar) {
				cmd.addFlagged("-outjar", outjar.getAbsolutePath());
			} else {
				cmd.addFlagged("-outjar", tmpOutjar.getAbsolutePath());
			}
			outjarFixedup = true;
		}

		ArrayList result = new ArrayList(cmd.extractArguments());
		addListArgs(result);

		String[] command = (String[]) result.toArray(new String[0]);
		if (null != commandEditor) {
			command = commandEditor.editCommand(command);
		} else if (null != COMMAND_EDITOR) {
			command = COMMAND_EDITOR.editCommand(command);
		}
		return command;
	}

	/**
	 * Create any pseudo-options required to implement some of the macro options
	 *
	 * @throws BuildException if options conflict
	 */
	protected void setupOptions() {
		if (null != xweaveDir) {
			if (DEFAULT_DESTDIR != destDir) {
				throw new BuildException("weaveDir forces destdir");
			}
			if (null != outjar) {
				throw new BuildException("weaveDir forces outjar");
			}
			if (null != injars) {
				throw new BuildException("weaveDir incompatible with injars now");
			}
			if (null != inpath) {
				throw new BuildException("weaveDir incompatible with inpath now");
			}

			File injar = zipDirectory(xweaveDir);
			setInjars(new Path(getProject(), injar.getAbsolutePath()));
			setDestdir(xweaveDir);
		}
	}

	protected File zipDirectory(File dir) {
		File tempDir = new File(".");
		try {
			tempDir = File.createTempFile("AjcTest", ".tmp");
			tempDir.mkdirs();
			tempDir.deleteOnExit(); // XXX remove zip explicitly..
		} catch (IOException e) {
			// ignore
		}
		// File result = new File(tempDir,
		String filename = "AjcTask-" + System.currentTimeMillis() + ".zip";
		File result = new File(filename);
		Zip zip = new Zip();
		zip.setProject(getProject());
		zip.setDestFile(result);
		zip.setTaskName(getTaskName() + " - zip");
		FileSet fileset = new FileSet();
		fileset.setDir(dir);
		zip.addFileset(fileset);
		zip.execute();
		Delete delete = new Delete();
		delete.setProject(getProject());
		delete.setTaskName(getTaskName() + " - delete");
		delete.setDir(dir);
		delete.execute();
		Mkdir mkdir = new Mkdir();
		mkdir.setProject(getProject());
		mkdir.setTaskName(getTaskName() + " - mkdir");
		mkdir.setDir(dir);
		mkdir.execute();
		return result;
	}

	/**
	 * @throws BuildException if options conflict
	 */
	protected void verifyOptions() {
		StringBuffer sb = new StringBuffer();
		if (fork && isInIncrementalMode() && !isInIncrementalFileMode()) {
			sb.append("can fork incremental only using tag file.\n");
		}
		if (((null != inpathDirCopyFilter) || (null != sourceRootCopyFilter)) && (null == outjar) && (DEFAULT_DESTDIR == destDir)) {
			final String REQ = " requires dest dir or output jar.\n";
			if (null == inpathDirCopyFilter) {
				sb.append("sourceRootCopyFilter");
			} else if (null == sourceRootCopyFilter) {
				sb.append("inpathDirCopyFilter");
			} else {
				sb.append("sourceRootCopyFilter and inpathDirCopyFilter");
			}
			sb.append(REQ);
		}
		if (0 < sb.length()) {
			throw new BuildException(sb.toString());
		}
	}

	/**
	 * Run the compile in the same VM by loading the compiler (Main), setting up any message holders, doing the compile, and
	 * converting abort/failure and error messages to BuildException, as appropriate.
	 *
	 * @throws BuildException if abort or failure messages or if errors and failonerror.
	 *
	 */
	protected void executeInSameVM(String[] args) {
		if (null != maxMem) {
			log("maxMem ignored unless forked: " + maxMem, Project.MSG_WARN);
		}
		IMessageHolder holder = messageHolder;
		int numPreviousErrors;
		if (null == holder) {
			MessageHandler mhandler = new MessageHandler(true);
			final IMessageHandler delegate;
			delegate = new AntMessageHandler(this.logger, this.verbose, false);
			mhandler.setInterceptor(delegate);
			holder = mhandler;
			numPreviousErrors = 0;
		} else {
			numPreviousErrors = holder.numMessages(IMessage.ERROR, true);
		}
		{
			Main newmain = new Main();
			newmain.setHolder(holder);
			newmain.setCompletionRunner(new Runnable() {
				@Override
				public void run() {
					doCompletionTasks();
				}
			});
			if (null != main) {
				MessageUtil.fail(holder, "still running prior main");
				return;
			}
			main = newmain;
		}
		main.runMain(args, false);
		if (failonerror) {
			int errs = holder.numMessages(IMessage.ERROR, false);
			errs -= numPreviousErrors;
			if (0 < errs) {
				String m = errs + " errors";
				MessageUtil.print(System.err, holder, "", MessageUtil.MESSAGE_ALL, MessageUtil.PICK_ERROR, true);
				throw new BuildException(m);
			}
		}
		// Throw BuildException if there are any fail or abort
		// messages.
		// The BuildException message text has a list of class names
		// for the exceptions found in the messages, or the
		// number of fail/abort messages found if there were
		// no exceptions for any of the fail/abort messages.
		// The interceptor message handler should have already
		// printed the messages, including any stack traces.
		// HACK: this ignores the Usage message
		{
			IMessage[] fails = holder.getMessages(IMessage.FAIL, true);
			if (!LangUtil.isEmpty(fails)) {
				StringBuffer sb = new StringBuffer();
				String prefix = "fail due to ";
				int numThrown = 0;
				for (IMessage fail : fails) {
					String message = fail.getMessage();
					if (LangUtil.isEmpty(message)) {
						message = "<no message>";
					} else if (message.contains(USAGE_SUBSTRING)) {
						continue;
					}
					Throwable t = fail.getThrown();
					if (null != t) {
						numThrown++;
						sb.append(prefix);
						sb.append(LangUtil.unqualifiedClassName(t.getClass()));
						String thrownMessage = t.getMessage();
						if (!LangUtil.isEmpty(thrownMessage)) {
							sb.append(" \"" + thrownMessage + "\"");
						}
					}
					sb.append("\"" + message + "\"");
					prefix = ", ";
				}
				if (0 < sb.length()) {
					sb.append(" (" + numThrown + " exceptions)");
					throw new BuildException(sb.toString());
				}
			}
		}
	}

	/**
	 * Execute in a separate VM. Differences from normal same-VM execution:
	 * <ul>
	 * <li>ignores any message holder {class} set</li>
	 * <li>No resource-copying between interative runs</li>
	 * <li>failonerror fails when process interface fails to return negative values</li>
	 * </ul>
	 *
	 * @param args String[] of the complete compiler command to execute
	 *
	 * @throws BuildException if ajc aborts (negative value) or if failonerror and there were compile errors.
	 */
	protected void executeInOtherVM(String[] args) {
		javaCmd.setClassname(org.aspectj.tools.ajc.Main.class.getName());

		final Path vmClasspath = javaCmd.createClasspath(getProject());
		{
			File aspectjtools = null;
			int vmClasspathSize = vmClasspath.size();
			if ((null != forkclasspath) && (0 != forkclasspath.size())) {
				vmClasspath.addExisting(forkclasspath);
			} else {
				aspectjtools = findAspectjtoolsJar();
				if (null != aspectjtools) {
					vmClasspath.createPathElement().setLocation(aspectjtools);
				}
			}
			int newVmClasspathSize = vmClasspath.size();
			if (vmClasspathSize == newVmClasspathSize) {
				String m = "unable to find aspectjtools to fork - ";
				if (null != aspectjtools) {
					m += "tried " + aspectjtools.toString();
				} else if (null != forkclasspath) {
					m += "tried " + forkclasspath.toString();
				} else {
					m += "define forkclasspath or put aspectjtools on classpath";
				}
				throw new BuildException(m);
			}
		}
		if (null != maxMem) {
			javaCmd.setMaxmemory(maxMem);
		}
		File tempFile = null;
		int numArgs = args.length;
		args = GuardedCommand.limitTo(args, MAX_COMMANDLINE, getLocation());

		if (args.length != numArgs) {
			tempFile = new File(args[1]);
		}
		try {
			boolean setMessageHolderOnForking = (this.messageHolder != null);
			String[] javaArgs = javaCmd.getCommandline();
			String[] both = new String[javaArgs.length + args.length + (setMessageHolderOnForking ? 2 : 0)];
			System.arraycopy(javaArgs, 0, both, 0, javaArgs.length);
			System.arraycopy(args, 0, both, javaArgs.length, args.length);
			if (setMessageHolderOnForking) {
				both[both.length - 2] = "-messageHolder";
				both[both.length - 1] = this.messageHolder.getClass().getName();
			}
			// try to use javaw instead on windows
			if (both[0].endsWith("java.exe")) {
				String path = both[0];
				path = path.substring(0, path.length() - 4);
				path = path + "w.exe";
				File javaw = new File(path);
				if (javaw.canRead() && javaw.isFile()) {
					both[0] = path;
				}
			}
			logVerbose("forking " + Arrays.asList(both));
			int result = execInOtherVM(both);
			if (0 > result) {
				throw new BuildException("failure[" + result + "] running ajc");
			} else if (failonerror && (0 < result)) {
				throw new BuildException("compile errors: " + result);
			}
			// when forking, do completion only at end and when successful
			doCompletionTasks();
		} finally {
			if (null != tempFile) {
				tempFile.delete();
			}
		}
	}

	/**
	 * Execute in another process using the same JDK and the base directory of the project. XXX correct?
	 */
	protected int execInOtherVM(String[] args) {
		try {

			Project project = getProject();
			PumpStreamHandler handler = new LogStreamHandler(this, verbose ? Project.MSG_VERBOSE : Project.MSG_INFO,
					Project.MSG_WARN);

			// replace above two lines with what follows as an aid to debugging when running the unit tests....
			//			LogStreamHandler handler = new LogStreamHandler(this, Project.MSG_INFO, Project.MSG_WARN) {
			//
			//				ByteArrayOutputStream baos = new ByteArrayOutputStream();
			//
			//				/*
			//				 * @see
			//				 * org.apache.tools.ant.taskdefs.PumpStreamHandler#createProcessOutputPump(java.
			//				 * io.InputStream, java.io.OutputStream)
			//				 */
			//				protected void createProcessErrorPump(InputStream is, OutputStream os) {
			//					super.createProcessErrorPump(is, baos);
			//				}
			//
			//				/*
			//				 * @see org.apache.tools.ant.taskdefs.LogStreamHandler#stop()
			//				 */
			//				public void stop() {
			//					byte[] written = baos.toByteArray();
			//					System.err.print(new String(written));
			//					super.stop();
			//				}
			//			};

			Execute exe = new Execute(handler);
			exe.setAntRun(project);
			exe.setWorkingDirectory(project.getBaseDir());
			exe.setCommandline(args);
			try {
				if (executingInOtherVM) {
					String s = "already running in other vm?";
					throw new BuildException(s, location);
				}
				executingInOtherVM = true;
				exe.execute();
			} finally {
				executingInOtherVM = false;
			}
			return exe.getExitValue();
		} catch (IOException e) {
			String m = "Error executing command " + Arrays.asList(args);
			throw new BuildException(m, e, location);
		}
	}

	// ------------------------------ setup and reporting
	protected static void addFlaggedPath(String flag, Path path, List<String> list) {
		if (!LangUtil.isEmpty(flag) && ((null != path) && (0 < path.size()))) {
			list.add(flag);
			list.add(path.toString());
		}
	}

	/**
	 * Add to list any path or plural arguments.
	 */
	protected void addListArgs(List<String> list) throws BuildException {
		addFlaggedPath("-classpath", classpath, list);
		addFlaggedPath("-bootclasspath", bootclasspath, list);
		addFlaggedPath("-extdirs", extdirs, list);
		addFlaggedPath("-aspectpath", aspectpath, list);
		addFlaggedPath("-injars", injars, list);
		addFlaggedPath("-inpath", inpath, list);
		addFlaggedPath("-sourceroots", sourceRoots, list);

		if (this.compilerArgs != null) {
			for (CompilerArg compilerArg:compilerArgs) {
				list.add(compilerArg.toString());
			}
		}

		if (argfiles != null) {
			String[] files = argfiles.list();
			for (String file : files) {
				File argfile = project.resolveFile(file);
				if (check(argfile, file, false, location)) {
					list.add("-argfile");
					list.add(argfile.getAbsolutePath());
				}
			}
		}
		if (inxmlfiles != null) {
			String[] files = inxmlfiles.list();
			for (String file : files) {
				File inxmlfile = project.resolveFile(file);
				if (check(inxmlfile, file, false, location)) {
					list.add("-xmlConfigured");
					list.add(inxmlfile.getAbsolutePath());
				}
			}
		}
		if (srcdir != null) {
			// todo: ignore any srcdir if any argfiles and no explicit includes
			String[] dirs = srcdir.list();
			for (String dir2 : dirs) {
				File dir = project.resolveFile(dir2);
				check(dir, dir2, true, location);
				// relies on compiler to prune non-source files
				String[] files = getDirectoryScanner(dir).getIncludedFiles();
				for (String file2 : files) {
					File file = new File(dir, file2);
					if (FileUtil.hasSourceSuffix(file)) {
						if (!list.contains(file.getAbsolutePath())) {
							list.add(file.getAbsolutePath());
						}
					}
				}
			}
		}
		if (0 < adapterFiles.size()) {
            for (Object adapterFile : adapterFiles) {
                File file = (File) adapterFile;
                if (file.canRead() && FileUtil.hasSourceSuffix(file)) {
                    list.add(file.getAbsolutePath());
                }
                else {
                    this.logger.warning("skipping file: " + file);
                }
            }
		}
	}

	/**
	 * Throw BuildException unless file is valid.
	 *
	 * @param file the File to check
	 * @param name the symbolic name to print on error
	 * @param isDir if true, verify file is a directory
	 * @param loc the Location used to create sensible BuildException
	 * @return
	 * @throws BuildException unless file valid
	 */
	protected final boolean check(File file, String name, boolean isDir, Location loc) {
		loc = loc != null ? loc : location;
		if (file == null) {
			throw new BuildException(name + " is null!", loc);
		}
		if (!file.exists()) {
			throw new BuildException(file + " doesn't exist!", loc);
		}
		if (isDir ^ file.isDirectory()) {
			String e = file + " should" + (isDir ? "" : "n't") + " be a directory!";
			throw new BuildException(e, loc);
		}
		return true;
	}

	/**
	 * Called when compile or incremental compile is completing, this completes the output jar or directory by copying resources if
	 * requested. Note: this is a callback run synchronously by the compiler. That means exceptions thrown here are caught by
	 * Main.run(..) and passed to the message handler.
	 */
	protected void doCompletionTasks() {
		if (!executing) {
			throw new IllegalStateException("should be executing");
		}
		if (null != outjar) {
			completeOutjar();
		} else {
			completeDestdir();
		}
		if (null != xdoneSignal) {
			MessageUtil.info(messageHolder, xdoneSignal);
		}
	}

	/**
	 * Complete the destination directory by copying resources from the source root directories (if the filter is specified) and
	 * non-.class files from the input jars (if XCopyInjars is enabled).
	 */
	private void completeDestdir() {
		if (!copyInjars && (null == sourceRootCopyFilter) && (null == inpathDirCopyFilter)) {
			return;
		} else if ((destDir == DEFAULT_DESTDIR) || !destDir.canWrite()) {
			String s = "unable to copy resources to destDir: " + destDir;
			throw new BuildException(s);
		}
		final Project project = getProject();
		if (copyInjars) { // XXXX remove as unused since 1.1.1
			if (null != inpath) {
				log("copyInjars does not support inpath.\n", Project.MSG_WARN);
			}
			String taskName = getTaskName() + " - unzip";
			String[] paths = injars.list();
			if (!LangUtil.isEmpty(paths)) {
				PatternSet patternSet = new PatternSet();
				patternSet.setProject(project);
				patternSet.setIncludes("**/*");
				patternSet.setExcludes("**/*.class");
				for (String path : paths) {
					Expand unzip = new Expand();
					unzip.setProject(project);
					unzip.setTaskName(taskName);
					unzip.setDest(destDir);
					unzip.setSrc(new File(path));
					unzip.addPatternset(patternSet);
					unzip.execute();
				}
			}
		}
		if ((null != sourceRootCopyFilter) && (null != sourceRoots)) {
			String[] paths = sourceRoots.list();
			if (!LangUtil.isEmpty(paths)) {
				Copy copy = new Copy();
				copy.setProject(project);
				copy.setTodir(destDir);
				for (String path : paths) {
					FileSet fileSet = new FileSet();
					fileSet.setDir(new File(path));
					fileSet.setIncludes("**/*");
					fileSet.setExcludes(sourceRootCopyFilter);
					copy.addFileset(fileSet);
				}
				copy.execute();
			}
		}
		if ((null != inpathDirCopyFilter) && (null != inpath)) {
			String[] paths = inpath.list();
			if (!LangUtil.isEmpty(paths)) {
				Copy copy = new Copy();
				copy.setProject(project);
				copy.setTodir(destDir);
				boolean gotDir = false;
				for (String path : paths) {
					File inpathDir = new File(path);
					if (inpathDir.isDirectory() && inpathDir.canRead()) {
						if (!gotDir) {
							gotDir = true;
						}
						FileSet fileSet = new FileSet();
						fileSet.setDir(inpathDir);
						fileSet.setIncludes("**/*");
						fileSet.setExcludes(inpathDirCopyFilter);
						copy.addFileset(fileSet);
					}
				}
				if (gotDir) {
					copy.execute();
				}
			}
		}
	}

	/**
	 * Complete the output jar by copying resources from the source root directories if the filter is specified. and non-.class
	 * files from the input jars if enabled.
	 */
	private void completeOutjar() {
		if (((null == tmpOutjar) || !tmpOutjar.canRead())
				|| (!copyInjars && (null == sourceRootCopyFilter) && (null == inpathDirCopyFilter))) {
			return;
		}
		Zip zip = new Zip();
		Project project = getProject();
		zip.setProject(project);
		zip.setTaskName(getTaskName() + " - zip");
		zip.setDestFile(outjar);
		ZipFileSet zipfileset = new ZipFileSet();
		zipfileset.setProject(project);
		zipfileset.setSrc(tmpOutjar);
		zipfileset.setIncludes("**/*.class");
		zip.addZipfileset(zipfileset);
		if (copyInjars) {
			String[] paths = injars.list();
			if (!LangUtil.isEmpty(paths)) {
				for (String path : paths) {
					File jarFile = new File(path);
					zipfileset = new ZipFileSet();
					zipfileset.setProject(project);
					zipfileset.setSrc(jarFile);
					zipfileset.setIncludes("**/*");
					zipfileset.setExcludes("**/*.class");
					zip.addZipfileset(zipfileset);
				}
			}
		}
		if ((null != sourceRootCopyFilter) && (null != sourceRoots)) {
			String[] paths = sourceRoots.list();
			if (!LangUtil.isEmpty(paths)) {
				for (String path : paths) {
					File srcRoot = new File(path);
					FileSet fileset = new FileSet();
					fileset.setProject(project);
					fileset.setDir(srcRoot);
					fileset.setIncludes("**/*");
					fileset.setExcludes(sourceRootCopyFilter);
					zip.addFileset(fileset);
				}
			}
		}
		if ((null != inpathDirCopyFilter) && (null != inpath)) {
			String[] paths = inpath.list();
			if (!LangUtil.isEmpty(paths)) {
				for (String path : paths) {
					File inpathDir = new File(path);
					if (inpathDir.isDirectory() && inpathDir.canRead()) {
						FileSet fileset = new FileSet();
						fileset.setProject(project);
						fileset.setDir(inpathDir);
						fileset.setIncludes("**/*");
						fileset.setExcludes(inpathDirCopyFilter);
						zip.addFileset(fileset);
					}
				}
			}
		}
		zip.execute();
	}

	// -------------------------- compiler adapter interface extras

	/**
	 * Add specified source files.
	 */
	void addFiles(File[] paths) {
		for (File path : paths) {
			addFile(path);
		}
	}

	/**
	 * Add specified source file.
	 */
	void addFile(File path) {
		if (null != path) {
			adapterFiles.add(path);
		}
	}

	/**
	 * Read arguments in as if from a command line, mainly to support compiler adapter compilerarg subelement.
	 *
	 * @param args the String[] of arguments to read
	 */
	public void readArguments(String[] args) { // XXX slow, stupid, unmaintainable
		if ((null == args) || (0 == args.length)) {
			return;
		}
		/** String[] wrapper with increment, error reporting */
		class Args {
			final String[] args;
			int index = 0;

			Args(String[] args) {
				this.args = args; // not null or empty
			}

			boolean hasNext() {
				return index < args.length;
			}

			String next() {
				String err = null;
				if (!hasNext()) {
					err = "need arg for flag " + args[args.length - 1];
				} else {
					String s = args[index++];
					if (null == s) {
						err = "null value";
					} else {
						s = s.trim();
						if (0 == s.trim().length()) {
							err = "no value";
						} else {
							return s;
						}
					}
				}
				err += " at [" + index + "] of " + Arrays.asList(args);
				throw new BuildException(err);
			}
		} // class Args

		Args in = new Args(args);
		String flag;
		while (in.hasNext()) {
			flag = in.next();
			if ("-1.3".equals(flag)) {
				setCompliance(flag);
			} else if ("-1.4".equals(flag)) {
				setCompliance(flag);
			} else if ("-1.5".equals(flag)) {
				setCompliance("1.5");
			} else if ("-argfile".equals(flag)) {
				setArgfiles(new Path(project, in.next()));
			} else if ("-aspectpath".equals(flag)) {
				setAspectpath(new Path(project, in.next()));
			} else if ("-classpath".equals(flag)) {
				setClasspath(new Path(project, in.next()));
			} else if ("-extdirs".equals(flag)) {
				setExtdirs(new Path(project, in.next()));
			} else if ("-Xcopyinjars".equals(flag)) {
				setCopyInjars(true); // ignored - will be flagged by setter
			} else if ("-g".equals(flag)) {
				setDebug(true);
			} else if (flag.startsWith("-g:")) {
				setDebugLevel(flag.substring(2));
			} else if ("-deprecation".equals(flag)) {
				setDeprecation(true);
			} else if ("-d".equals(flag)) {
				setDestdir(new File(in.next()));
			} else if ("-crossrefs".equals(flag)) {
				setCrossrefs(true);
			} else if ("-emacssym".equals(flag)) {
				setEmacssym(true);
			} else if ("-encoding".equals(flag)) {
				setEncoding(in.next());
			} else if ("-Xfailonerror".equals(flag)) {
				setFailonerror(true);
			} else if ("-fork".equals(flag)) {
				setFork(true);
			} else if ("-forkclasspath".equals(flag)) {
				setForkclasspath(new Path(project, in.next()));
			} else if ("-help".equals(flag)) {
				setHelp(true);
			} else if ("-incremental".equals(flag)) {
				setIncremental(true);
			} else if ("-injars".equals(flag)) {
				setInjars(new Path(project, in.next()));
			} else if ("-inpath".equals(flag)) {
				setInpath(new Path(project, in.next()));
			} else if ("-Xlistfileargs".equals(flag)) {
				setListFileArgs(true);
			} else if ("-Xmaxmem".equals(flag)) {
				setMaxmem(in.next());
			} else if ("-Xmessageholderclass".equals(flag)) {
				setMessageHolderClass(in.next());
			} else if ("-noexit".equals(flag)) {
				setNoExit(true);
			} else if ("-noimport".equals(flag)) {
				setNoExit(true);
			} else if ("-noExit".equals(flag)) {
				setNoExit(true);
			} else if ("-noImportError".equals(flag)) {
				setNoImportError(true);
			} else if ("-noWarn".equals(flag)) {
				setNowarn(true);
			} else if ("-noexit".equals(flag)) {
				setNoExit(true);
			} else if ("-outjar".equals(flag)) {
				setOutjar(new File(in.next()));
			} else if ("-outxml".equals(flag)) {
				setOutxml(true);
			} else if ("-outxmlfile".equals(flag)) {
				setOutxmlfile(in.next());
			} else if ("-preserveAllLocals".equals(flag)) {
				setPreserveAllLocals(true);
			} else if ("-proceedOnError".equals(flag)) {
				setProceedOnError(true);
			} else if ("-referenceInfo".equals(flag)) {
				setReferenceInfo(true);
			} else if ("-source".equals(flag)) {
				setSource(in.next());
			} else if ("-Xsourcerootcopyfilter".equals(flag)) {
				setSourceRootCopyFilter(in.next());
			} else if ("-sourceroots".equals(flag)) {
				setSourceRoots(new Path(project, in.next()));
			} else if ("-Xsrcdir".equals(flag)) {
				setSrcDir(new Path(project, in.next()));
			} else if ("-Xtagfile".equals(flag)) {
				setTagFile(new File(in.next()));
			} else if ("-target".equals(flag)) {
				setTarget(in.next());
			} else if ("-time".equals(flag)) {
				setTime(true);
			} else if ("-time".equals(flag)) {
				setTime(true);
			} else if ("-verbose".equals(flag)) {
				setVerbose(true);
			} else if ("-showWeaveInfo".equals(flag)) {
				setShowWeaveInfo(true);
			} else if ("-version".equals(flag)) {
				setVersion(true);
			} else if ("-warn".equals(flag)) {
				setWarn(in.next());
			} else if (flag.startsWith("-warn:")) {
				setWarn(flag.substring(6));
			} else if ("-Xlint".equals(flag)) {
				setXlintwarnings(true);
			} else if (flag.startsWith("-Xlint:")) {
				setXlint(flag.substring(7));
			} else if ("-Xlintfile".equals(flag)) {
				setXlintfile(new File(in.next()));
			} else if ("-XterminateAfterCompilation".equals(flag)) {
				setXTerminateAfterCompilation(true);
			} else if ("-Xreweavable".equals(flag)) {
				setXReweavable(true);
			} else if ("-XnotReweavable".equals(flag)) {
				setXNotReweavable(true);
			} else if (flag.startsWith("@")) {
				File file = new File(flag.substring(1));
				if (file.canRead()) {
					setArgfiles(new Path(project, file.getPath()));
				} else {
					ignore(flag);
				}
			} else {
				File file = new File(flag);
				if (file.isFile() && file.canRead() && FileUtil.hasSourceSuffix(file)) {
					addFile(file);
				} else {
					ignore(flag);
				}
			}
		}

	}

	protected void logVerbose(String text) {
		if (this.verbose) {
			this.logger.info(text);
		} else {
			this.logger.verbose(text);
		}
	}

	/**
	 * Commandline wrapper that only permits addition of non-empty values and converts to argfile form if necessary.
	 */
	public static class GuardedCommand {
		Commandline command;

		// int size;

		static boolean isEmpty(String s) {
			return ((null == s) || (0 == s.trim().length()));
		}

		GuardedCommand() {
			command = new Commandline();
		}

		void addFlag(String flag, boolean doAdd) {
			if (doAdd && !isEmpty(flag)) {
				command.createArgument().setValue(flag);
				// size += 1 + flag.length();
			}
		}

		/** @return null if added or ignoreString otherwise */
		String addOption(String prefix, String[] validOptions, String input) {
			if (isEmpty(input)) {
				return null;
			}
			for (String validOption : validOptions) {
				if (input.equals(validOption)) {
					if (isEmpty(prefix)) {
						addFlag(input, true);
					} else {
						addFlagged(prefix, input);
					}
					return null;
				}
			}
			return (null == prefix ? input : prefix + " " + input);
		}

		void addFlagged(String flag, String argument) {
			if (!isEmpty(flag) && !isEmpty(argument)) {
				command.addArguments(new String[] { flag, argument });
				// size += 1 + flag.length() + argument.length();
			}
		}

		// private void addFile(File file) {
		// if (null != file) {
		// String path = file.getAbsolutePath();
		// addFlag(path, true);
		// }
		// }

		List extractArguments() {
			List result = new ArrayList();
			String[] cmds = command.getArguments();
			if (!LangUtil.isEmpty(cmds)) {
				result.addAll(Arrays.asList(cmds));
			}
			return result;
		}

		/**
		 * Adjust args for size if necessary by creating an argument file, which should be deleted by the client after the compiler
		 * run has completed.
		 *
		 * @param max the int maximum length of the command line (in char)
		 * @return the temp File for the arguments (if generated), for deletion when done.
		 * @throws IllegalArgumentException if max is negative
		 */
		static String[] limitTo(String[] args, int max, Location location) {
			if (max < 0) {
				throw new IllegalArgumentException("negative max: " + max);
			}
			// sigh - have to count anyway for now
			int size = 0;
			for (int i = 0; (i < args.length) && (size < max); i++) {
				size += 1 + (null == args[i] ? 0 : args[i].length());
			}
			if (size <= max) {
				return args;
			}
			File tmpFile = null;
			PrintWriter out = null;
			// adapted from DefaultCompilerAdapter.executeExternalCompile
			try {
				String userDirName = System.getProperty("user.dir");
				File userDir = new File(userDirName);
				tmpFile = File.createTempFile("argfile", "", userDir);
				out = new PrintWriter(new FileWriter(tmpFile));
				for (String arg : args) {
					out.println(arg);
				}
				out.flush();
				return new String[] { "-argfile", tmpFile.getAbsolutePath() };
			} catch (IOException e) {
				throw new BuildException("Error creating temporary file", e, location);
			} finally {
				if (out != null) {
					try {
						out.close();
					} catch (Throwable t) {
					}
				}
			}
		}
	}

	private static class AntMessageHandler implements IMessageHandler {

		private TaskLogger logger;
		private final boolean taskLevelVerbose;
		private final boolean handledMessage;

		public AntMessageHandler(TaskLogger logger, boolean taskVerbose, boolean handledMessage) {
			this.logger = logger;
			this.taskLevelVerbose = taskVerbose;
			this.handledMessage = handledMessage;
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see org.aspectj.bridge.IMessageHandler#handleMessage(org.aspectj.bridge.IMessage)
		 */
		@Override
		public boolean handleMessage(IMessage message) throws AbortException {
			Kind messageKind = message.getKind();
			String messageText = message.toString();
			if (messageKind == IMessage.ABORT) {
				this.logger.error(messageText);
			} else if (messageKind == IMessage.DEBUG) {
				this.logger.debug(messageText);
			} else if (messageKind == IMessage.ERROR) {
				this.logger.error(messageText);
			} else if (messageKind == IMessage.FAIL) {
				this.logger.error(messageText);
			} else if (messageKind == IMessage.INFO) {
				if (this.taskLevelVerbose) {
					this.logger.info(messageText);
				} else {
					this.logger.verbose(messageText);
				}
			} else if (messageKind == IMessage.WARNING) {
				this.logger.warning(messageText);
			} else if (messageKind == IMessage.WEAVEINFO) {
				this.logger.info(messageText);
			} else if (messageKind == IMessage.TASKTAG) {
				// ignore
			} else {
				throw new BuildException("Unknown message kind from AspectJ compiler: " + messageKind.toString());
			}
			return handledMessage;
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see org.aspectj.bridge.IMessageHandler#isIgnoring(org.aspectj.bridge.IMessage.Kind)
		 */
		@Override
		public boolean isIgnoring(Kind kind) {
			return false;
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see org.aspectj.bridge.IMessageHandler#dontIgnore(org.aspectj.bridge.IMessage.Kind)
		 */
		@Override
		public void dontIgnore(Kind kind) {
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see org.aspectj.bridge.IMessageHandler#ignore(org.aspectj.bridge.IMessage.Kind)
		 */
		@Override
		public void ignore(Kind kind) {
		}

	}
}
