/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC),
 *               2003 Contributors.
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Xerox/PARC     initial implementation
 *     Wes Isberg     2003 changes.
 * ******************************************************************/

package org.aspectj.testing.drivers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.IMessageHolder;
import org.aspectj.bridge.MessageHandler;
import org.aspectj.bridge.MessageUtil;
import org.aspectj.testing.harness.bridge.AbstractRunSpec;
import org.aspectj.testing.harness.bridge.AjcTest;
import org.aspectj.testing.harness.bridge.CompilerRun;
import org.aspectj.testing.harness.bridge.FlatSuiteReader;
import org.aspectj.testing.harness.bridge.IRunSpec;
import org.aspectj.testing.harness.bridge.IncCompilerRun;
import org.aspectj.testing.harness.bridge.JavaRun;
import org.aspectj.testing.harness.bridge.RunSpecIterator;
import org.aspectj.testing.harness.bridge.Sandbox;
import org.aspectj.testing.harness.bridge.Validator;
import org.aspectj.testing.run.IRun;
import org.aspectj.testing.run.IRunIterator;
import org.aspectj.testing.run.IRunListener;
import org.aspectj.testing.run.IRunStatus;
import org.aspectj.testing.run.IRunValidator;
import org.aspectj.testing.run.RunListener;
import org.aspectj.testing.run.RunStatus;
import org.aspectj.testing.run.RunValidator;
import org.aspectj.testing.run.Runner;
import org.aspectj.testing.util.BridgeUtil;
import org.aspectj.testing.util.RunUtils;
import org.aspectj.testing.util.StreamsHandler;
import org.aspectj.testing.util.StreamsHandler.Result;
import org.aspectj.testing.xml.AjcSpecXmlReader;
import org.aspectj.testing.xml.XMLWriter;
import org.aspectj.util.FileUtil;
import org.aspectj.util.LangUtil;

/**
 * Test harness for running AjcTest.Suite test suites.
 * This can be easily extended by subclassing.
 * <ul>
 * <li>template algorithms for reading arguments, printing syntax,
 *     reading suites, and reporting results all
 *     delegate to methods that subclasses can override to support
 *     additional arguments or different reporting.</li>
 * <li>implement arbitrary features as IRunListeners</li>
 * <li>support single-option aliases to any number of single-options </li>
 * </ul>
 * See {@link report(IRunStatus, int, int)} for an explanation of test result
 * categories.
 */
public class Harness {
	/**
	 * Spaces up to the width that an option should take in the syntax,
	 * including the two-space leader
	 */
	protected static final String SYNTAX_PAD = "                    ";
	protected static final String OPTION_DELIM = ";";
	private static final String JAVA_VERSION;
	private static final String ASPECTJ_VERSION;
	static {
		String version = "UNKNOWN";
		try {  version = System.getProperty("java.version", "UNKNOWN"); }
		catch (Throwable t) {}
		JAVA_VERSION = version;

		version = "UNKNOWN";
		try {
			Class c = Class.forName("org.aspectj.bridge.Version");
			version = (String) c.getField("text").get(null);
		} catch (Throwable t) {
			// ignore
		}
		ASPECTJ_VERSION = version;
	}

	/** factory for the subclass currently anointed as default */
	public static Harness makeHarness() {
		return new FeatureHarness();
	}

	/** @param args String[] like runMain(String[]) args */
	public static void main(String[] args) throws Exception {
		if (LangUtil.isEmpty(args)) {
			File argFile = new File("HarnessArgs.txt");
			if (argFile.canRead()) {
				args = readArgs(argFile);
			} else {
				args = new String[] { "-help" };
			}
		}
		makeHarness().runMain(args, null);
	}

	/**
	 * Get known option aliases.
	 * Subclasses may add new aliases, where the key is the alias option,
	 * and the value is a comma-delimited String of target options.
	 * @return Properties with feature aliases or null
	 */
	protected static Properties getOptionAliases() {
		if (null == optionAliases) {
			optionAliases = new Properties();
			// XXX load from **OptionAliases.properties
		}
		return optionAliases;
	}

	/**
	 * Read argFile contents into String[],
	 * delimiting at any whitespace
	 */
	private static String[] readArgs(File argFile) {
		ArrayList<String> args = new ArrayList<>();
		//        int lineNum = 0;

		try {
			BufferedReader stream =
					new BufferedReader(new FileReader(argFile));
			String line;
			while (null != (line = stream.readLine())) {
				StringTokenizer st = new StringTokenizer(line);
				while (st.hasMoreTokens()) {
					args.add(st.nextToken());
				}
			}
		} catch (IOException e) {
			e.printStackTrace(System.err);
		}
		return args.toArray(new String[0]);
	}

	/** aliases key="option" value="option{,option}" */
	private static Properties optionAliases;

	/** be extra noisy if true */
	private boolean verboseHarness;

	/** be extra quiet if true */
	private boolean quietHarness;

	/** just don't say anything! */
	protected boolean silentHarness;

	private Map<String,Feature> features;

	/** if true, do not delete temporary files. */
	private boolean keepTemp;

	/** if true, delete temporary files as each test completes. */
	private boolean killTemp;

	/** if true, then log results in report(..) when done */
	private boolean logResults;

	/** if true and there were failures, do System.exit({numFailures})*/
	private boolean exitOnFailure;

	protected Harness() {
		features = new HashMap<>();
	}


	/**
	 * Entry point for a test.
	 * This reads in the arguments,
	 * creates the test suite(s) from the input file(s),
	 * and for each suite does setup, run, report, and cleanup.
	 * When arguments are read, any option ending with "-" causes
	 * option variants, a set of args with and another without the
	 * option. See {@link LangUtil.optionVariants(String[])} for
	 * more details.
	 * @param args the String[] for the test suite - use -help to get options,
	 *         and use "-" suffixes for variants.
	 * @param resultList List for IRunStatus results - ignored if null
	 */
	public void runMain(String[] args, List resultList) {
		LangUtil.throwIaxIfFalse(!LangUtil.isEmpty(args), "empty args");
		// read arguments
		final ArrayList<String> globals = new ArrayList<>();
		final List<String> files = new ArrayList<>();
		final LinkedList<String> argList = new LinkedList<>(Arrays.asList(args));
		for (int i = 0; i < argList.size(); i++) {
			String arg = argList.get(i);
			List<String> aliases = aliasOptions(arg);
			if (!LangUtil.isEmpty(aliases)) {
				argList.remove(i);
				argList.addAll(i, aliases);
				i--;
				continue;
			}
			if ("-help".equals(arg)) {
				logln("java " + Harness.class.getName() + " {option|suiteFile}..");
				printSyntax(getLogStream());
				return;
			} else if (isSuiteFile(arg)) {
				files.add(arg);
			} else if (!acceptOption(arg)) {
				globals.add(arg);
			} // else our options absorbed
		}
		if (0 == files.size()) {
			logln("## Error reading arguments: at least 1 suite file required");
			logln("java " + Harness.class.getName() + " {option|suiteFile}..");
			printSyntax(getLogStream());
			return;
		}
		String[] globalOptions = globals.toArray(new String[0]);
		String[][] globalOptionVariants = optionVariants(globalOptions);
		AbstractRunSpec.RT runtime = new AbstractRunSpec.RT();
		if (verboseHarness) {
			runtime.setVerbose(true);
		}

		// run suites read from each file
		AjcTest.Suite.Spec spec;
		for (String string : files) {
			File suiteFile = new File(string);
			if (!suiteFile.canRead()) {
				logln("runMain(..) cannot read file: " + suiteFile);
				continue;
			}
			if (null == (spec = readSuite(suiteFile))) {
				logln("runMain(..) cannot read suite from file: " + suiteFile);
				continue;
			}

			MessageHandler holder = new MessageHandler();
			for (String[] globalOptionVariant : globalOptionVariants) {
				runtime.setOptions(globalOptionVariant);
				holder.init();
				boolean skip = !spec.adoptParentValues(runtime, holder);
				// awful/brittle assumption about number of skips == number of skip messages
				final List<IMessage> skipList = MessageUtil.getMessages(holder, IMessage.INFO, false, "skip");
				if ((verboseHarness || skip || (0 < skipList.size()))) {
					final List<String> curArgs = Arrays.asList(globalOptionVariant);
					logln("runMain(" + suiteFile + ", " + curArgs + ")");
					if (verboseHarness) {
						String format = "yyyy.MM.dd G 'at' hh:mm:ss a zzz";
						SimpleDateFormat formatter = new SimpleDateFormat (format);
						String date = formatter.format(new Date());
						logln("test date: " + date);
						logln("harness features: " + listFeatureNames());
						logln("Java version: " + JAVA_VERSION);
						logln("AspectJ version: " + ASPECTJ_VERSION);
					}
					if (!(quietHarness || silentHarness) && holder.hasAnyMessage(null, true)) {
						MessageUtil.print(getLogStream(), holder, "skip - ");
						MessageUtil.printMessageCounts(getLogStream(), holder, "skip - ");
					}
				}
				if (!skip) {
					doStartSuite(suiteFile);
					long elapsed = 0;
					RunResult result = null;
					try {
						final long startTime = System.currentTimeMillis();
						result = run(spec);
						if (null != resultList) {
							resultList.add(result);
						}
						elapsed = System.currentTimeMillis() - startTime;
						report(result.status, skipList.size(), result.numIncomplete, elapsed);
					} finally {
						doEndSuite(suiteFile,elapsed);
					}
					if (exitOnFailure) {
						int numFailures = RunUtils.numFailures(result.status, true);
						if (0 < numFailures) {
							System.exit(numFailures);
						}
						Object value = result.status.getResult();
						if ((value instanceof Boolean)
								&& !(Boolean) value) {
							System.exit(-1);
						}
					}
				}
			}
		}
	}


	/**
	 * Tell all IRunListeners that we are about to start a test suite
	 * @param suiteFile
	 * @param elapsed
	 */
	private void doEndSuite(File suiteFile, long elapsed) {
		Collection c = features.values();
		for (Object o : c) {
			Feature element = (Feature) o;
			if (element.listener instanceof TestCompleteListener) {
				((TestCompleteListener) element.listener).doEndSuite(suiteFile, elapsed);
			}
		}
	}
	/**
	 * Generate variants of String[] options by creating an extra set for
	 * each option that ends with "-".  If none end with "-", then an
	 * array equal to <code>new String[][] { options }</code> is returned;
	 * if one ends with "-", then two sets are returned,
	 * three causes eight sets, etc.
	 * @return String[][] with each option set.
	 * @throws IllegalArgumentException if any option is null or empty.
	 */
	public static String[][] optionVariants(String[] options) {
		if ((null == options) || (0 == options.length)) {
			return new String[][] { new String[0]};
		}
		// be nice, don't stomp input
		String[] temp = new String[options.length];
		System.arraycopy(options, 0, temp, 0, temp.length);
		options = temp;
		boolean[] dup = new boolean[options.length];
		int numDups = 0;

		for (int i = 0; i < options.length; i++) {
			String option = options[i];
			if (LangUtil.isEmpty(option)) {
				throw new IllegalArgumentException("empty option at " + i);
			}
			if (option.endsWith("-")) {
				options[i] = option.substring(0, option.length()-1);
				dup[i] = true;
				numDups++;
			}
		}
		final String[] NONE = new String[0];
		final int variants = exp(2, numDups);
		final String[][] result = new String[variants][];
		// variant is a bitmap wrt doing extra value when dup[k]=true
		for (int variant = 0; variant < variants; variant++) {
			ArrayList<String> next = new ArrayList<>();
			int nextOption = 0;
			for (int k = 0; k < options.length; k++) {
				if (!dup[k] || (0 != (variant & (1 << (nextOption++))))) {
					next.add(options[k]);
				}
			}
			result[variant] = next.toArray(NONE);
		}
		return result;
	}

	private static int exp(int base, int power) { // not in Math?
		if (0 > power) {
			throw new IllegalArgumentException("negative power: " + power);
		}
		int result = 1;
		while (0 < power--) {
			result *= base;
		}
		return result;
	}

	/**
	 * @param suiteFile
	 */
	private void doStartSuite(File suiteFile) {
		Collection<Feature> c = features.values();
		for (Feature element : c) {
			if (element.listener instanceof TestCompleteListener) {
				((TestCompleteListener)element.listener).doStartSuite(suiteFile);
			}
		}
	}

	/** Run the test suite specified by the spec */
	protected RunResult run(AjcTest.Suite.Spec spec) {
		LangUtil.throwIaxIfNull(spec, "spec");
		/*
		 * For each run, initialize the runner and validator,
		 * create a new set of IRun{Iterator} tests,
		 * and run them.
		 * Delete all temp files when done.
		 */
		Runner runner = new Runner();
		if (0 != features.size()) {
			for (Entry<String, Feature> entry : features.entrySet()) {
				Feature feature = entry.getValue();
				runner.registerListener(feature.clazz, feature.listener);
			}
		}
		IMessageHolder holder = new MessageHandler();
		int numIncomplete = 0;
		RunStatus status = new RunStatus(holder, runner);
		status.setIdentifier(spec);
		// validator is used for all setup in entire tree...
		Validator validator = new Validator(status);
		if (!killTemp) {
			validator.lock(this);
		}
		Sandbox sandbox = null;
		try {
			sandbox = new Sandbox(spec.getSuiteDirFile(), validator);
			IRunIterator tests = spec.makeRunIterator(sandbox, validator);
			runner.runIterator(tests, status, null);
			if (tests instanceof RunSpecIterator) {
				numIncomplete = ((RunSpecIterator) tests).getNumIncomplete();
			}
		} finally {
			if (!keepTemp) {
				if (!killTemp) {
					validator.unlock(this);
				}
				validator.deleteTempFiles(verboseHarness);
			}
		}
		return new RunResult(status, numIncomplete);
	}

	/**
	 * Report the results of a test run after it is completed.
	 * Clients should be able to identify the number of:
	 * <ul>
	 * <li>tests run and passed</li>
	 * <li>tests failed, i.e., run and not passed (fail, error, etc.)</li>
	 * <li>tests incomplete, i.e., test definition read but test run setup failed</li>
	 * <li>tests skipped, i.e., test definition read and found incompatible with
	 *     the current configuration.</li>
	 * <ul>
	 *
	 * @param status returned from the run
	 * @param numSkipped int tests that were skipped because of
	 *         configuration incompatibilities
	 * @param numIncomplete int tests that failed during setup,
	 *         usually indicating a test definition or configuration error.
	 * @param msElapsed elapsed time in milliseconds
	 * */
	protected void report(IRunStatus status, int numSkipped, int numIncomplete,
			long msElapsed ) {
		if (logResults) {
			RunUtils.AJCSUITE_PRINTER.printRunStatus(getLogStream(), status);
		} else if (!(quietHarness || silentHarness) && (0 < status.numMessages(null, true))) {
			if (!silentHarness) {
				MessageUtil.print(getLogStream(), status, "");
			}
		}

		logln(BridgeUtil.childString(status, numSkipped, numIncomplete)
				+ " " + (msElapsed/1000) + " seconds");

	}

	// --------------- delegate methods
	protected void logln(String s) {
		if (!silentHarness) {
			getLogStream().println(s);
		}
	}

	protected PrintStream getLogStream() {
		return System.out;
	}

	protected boolean isSuiteFile(String arg) {
		return ((null != arg)
				&& (arg.endsWith(".txt") || arg.endsWith(".xml"))
				&& new File(arg).canRead());
	}

	/**
	 * Get the options that the input option is an alias for.
	 * Subclasses may add options directly to the getFeatureAliases result
	 * or override this.
	 * @return null if the input is not an alias for other options,
	 * or a non-empty List (String) of options that this option is an alias for
	 */
	protected List<String> aliasOptions(String option) {
		Properties aliases = Harness.getOptionAliases();
		if (null != aliases) {
			String args = aliases.getProperty(option);
			if (!LangUtil.isEmpty(args)) {
				return LangUtil.anySplit(args, OPTION_DELIM);
			}
		}
		return null;
	}

	/**
	 * Read and implement any of our options.
	 * Options other than this and suite files will be
	 * passed down as parent options through the test spec hierarchy.
	 * Subclasses override this to implement new options.
	 */
	protected boolean acceptOption(String option) {
		//        boolean result = false;
		if (LangUtil.isEmpty(option)) {
			return true; // skip bad input
		} else if ("-verboseHarness".equals(option)) {
			verboseHarness = true;
		} else if ("-quietHarness".equals(option)) {
			quietHarness = true;
		} else if ("-silentHarness".equals(option)) {
			silentHarness = true;
		} else if ("-keepTemp".equals(option)) {
			keepTemp = true;
		} else if ("-killTemp".equals(option)) {
			killTemp = true;
		} else if ("-logResults".equals(option)) {
			logResults = true;
		} else if ("-exitOnFailure".equals(option)) {
			exitOnFailure = true;
		} else {
			return false;
		}
		return true;
	}

	/**
	 * Read a test suite file.
	 * This implementation knows how to read .txt and .xml files
	 * and logs any errors.
	 * Subclasses override this to read new kinds of suites.
	 * @return null if unable to read (logging errors) or AjcTest.Suite.Spec otherwise
	 */
	protected AjcTest.Suite.Spec readSuite(File suiteFile) {
		if (null != suiteFile) {
			String path = suiteFile.getPath();
			try {
				if (path.endsWith(".xml")) {
					return AjcSpecXmlReader.getReader().readAjcSuite(suiteFile);
				} else if (path.endsWith(".txt")) {
					return FlatSuiteReader.ME.readSuite(suiteFile);
				} else {
					logln("unrecognized extension? " + path);
				}
			} catch (IOException e) {
				e.printStackTrace(getLogStream());
			}
		}
		return null;
	}

	/** Add feature to take effect during the next runMain(..) invocation.
	 * @param feature the Feature to add, using feature.name as key.
	 */
	protected void addFeature(Feature feature) {
		if (null != feature) {
			features.put(feature.name, feature);
		}
	}

	/** remove feature by name (same as feature.name) */
	protected void removeFeature(String name) {
		if (!LangUtil.isEmpty(name)) {
			features.remove(name);
		}
	}

	/** @return unmodifiable Set of feature names */
	protected Set listFeatureNames() {
		return Collections.unmodifiableSet(features.keySet());
	}

	/** print detail message for syntax of main(String[]) command-line */
	protected void printSyntax(PrintStream out) {
		out.println("  {??}              unrecognized options are used as test spec globals");
		out.println("  -help             print this help message");
		out.println("  -verboseHarness   harness components log verbosely");
		out.println("  -quietHarness     harness components suppress logging");
		out.println("  -keepTemp         do not delete temporary files");
		out.println("  -logResults       log results at end, verbosely if fail");
		out.println("  -exitOnFailure    do System.exit({num-failures}) if suite fails");
		out.println("  {suiteFile}.xml.. specify test suite XML file");
		out.println("  {suiteFile}.txt.. specify test suite .txt file (deprecated)");
	}

	/** print known aliases at the end of the syntax message */
	protected void printAliases(PrintStream out) {
		LangUtil.throwIaxIfNull(out, "out");
		Map props = getOptionAliases();
		if (null == props) {
			return;
		}
		int pdLength = SYNTAX_PAD.length();
		Set<Map.Entry<Object,Object>> entries = props.entrySet();
		for (Map.Entry<Object,Object> entry : entries) {
			String alias = "  " + (String) entry.getKey();
			int buf = pdLength - alias.length();
			if (0 < buf) {
				alias += SYNTAX_PAD.substring(0, buf);
			} else {
				alias += " ";
			}
			out.println(alias + entry.getValue());
		}
	}

	/** result struct for run(AjcTest.Spec) */
	public static class RunResult {
		public final IRunStatus status;
		public final int numIncomplete;
		public RunResult(IRunStatus status, int numIncomplete) {
			this.status = status;
			this.numIncomplete = numIncomplete;
		}
	}
	/** feature implemented as named IRunIterator/IRun association */
	public static class Feature {
		/** never null, always assignable to IRun */
		public final Class clazz;

		/** never null */
		public final IRunListener listener;

		/** never null or empty */
		public final String name;

		/** @throws IllegalArgumentException if any is null/empty or clazz is
		 *           not assignable to IRun
		 */
		public Feature(String name, Class clazz, IRunListener listener) {
			LangUtil.throwIaxIfNull(clazz, "class");
			if (!IRun.class.isAssignableFrom(clazz)
					&& !IRunIterator.class.isAssignableFrom(clazz)) {
				String s = clazz.getName() + "is not assignable to IRun or IRunIterator";
				LangUtil.throwIaxIfFalse(false, s);
			}
			LangUtil.throwIaxIfNull(listener, "listener");
			LangUtil.throwIaxIfNull(name, "name");
			LangUtil.throwIaxIfFalse(0 < name.length(), "empty name");
			this.clazz = clazz;
			this.listener = listener;
			this.name = name;
		}

		/** @return feature name */
		@Override
		public String toString() {
			return name;
		}
	}
}


/**
 * Harness with features for controlling output
 * (logging results and hiding streams).
 * Use -help to get a list of feature options.
 */
class FeatureHarness extends Harness {

	private static final String[] ALIASES = new String[]
			{ "-hideStreams",
					"-hideCompilerStreams"
							+ OPTION_DELIM + "-hideRunStreams",
							"-jim",
							"-logMinFail"
									+ OPTION_DELIM + "-hideStreams",
									"-loud",
									"-verboseHarness",
									"-baseline",
									"-verboseHarness"
											+ OPTION_DELIM + "-traceTestsMin"
											+ OPTION_DELIM + "-hideStreams",
											"-release",
											"-baseline"
													+ OPTION_DELIM + "-ajctestSkipKeywords=knownLimitation,purejava",
													"-junit",
													"-silentHarness" + OPTION_DELIM + "-logJUnit" + OPTION_DELIM +
													"-hideStreams",
													"-cruisecontrol",
													"-junit" + OPTION_DELIM + "-ajctestSkipKeywords=knownLimitation,purejava"
			};
	static {
		Map optionAliases = Harness.getOptionAliases();
		if (null != optionAliases) {
			for (int i = 1; i < ALIASES.length; i += 2) {
				optionAliases.put(ALIASES[i-1], ALIASES[i]);
			}
		}
	}

	/** controller for suppressing and sniffing error and output streams. */
	StreamsHandler streamsHandler;

	/** facility of hiding-streams may be applied in many features */
	IRunListener streamHider;

	/** facility of capture/log may be applied in many features */
	IRunListener captureLogger;

	/** when making tests, do not run them */
	TestMaker testMaker;

	public FeatureHarness() {
		super();
		streamsHandler = new StreamsHandler(false, true);
	}
	/** override to make tests or run as usual */
	@Override
	protected RunResult run(AjcTest.Suite.Spec spec) {
		if (null != testMaker) {
			System.out.println("generating rather than running tests...");
			return testMaker.run(spec);
		} else {
			return super.run(spec);
		}
	}

	/**
	 * Log via StreamsHandler-designated log stream.
	 * @see org.aspectj.testing.drivers.Harness#log(String)
	 */
	@Override
	protected void logln(String s) {
		if (!silentHarness)
			streamsHandler.lnlog(s);
	}

	/**
	 * @see org.aspectj.testing.drivers.Harness#getLogStream()
	 * @return StreamsHandler-designated log stream.
	 */
	@Override
	protected PrintStream getLogStream() {
		return streamsHandler.out;
	}


	/** print detail message for syntax of main(String[]) command-line */
	@Override
	protected void printSyntax(PrintStream out) {
		super.printSyntax(out);
		out.println("  -progressDots     log . or ! for each AjcTest pass or fail");
		out.println("  -logFail          log each failed AjcTest");
		out.println("  -logPass          log each passed AjcTest");
		out.println("  -logAll           log each AjcTest");
		out.println("  -logMinFail       log each AjcTest failure with minimal excess data");
		out.println("  -logMinPass       log each AjcTest success with minimal excess data");
		out.println("  -logMinAll        log all AjcTest with minimal excess data");
		out.println("  -logXMLFail       log XML definition for each failed AjcTest");
		out.println("  -logXMLPass       log XML definition for each passed AjcTest");
		out.println("  -logXMLAll        log XML definition for each AjcTest");
		out.println("  -logJUnit         log all tests in JUnit XML report style");
		out.println("  -hideRunStreams   hide err/out streams during java runs");
		out.println("  -hideCompilerStreams   hide err/out streams during compiler runs");
		out.println("  -traceTests       log pass|fail, /time/memory taken after each test");
		out.println("  -traceTestsMin    log pass|fail after each test");
		out.println("  -XmakeTests       create source files/dirs for initial compile run of each test");
		out.println("  -XlogPublicType   log test XML if \"public type\" in an error message");
		out.println("  -XlogSourceIn=Y,Z log test XML if Y or Z is in path of any sources");
		super.printAliases(out);
	}

	/** Accept a number of logging and output options */
	@Override
	protected boolean acceptOption(String option) {
		if (null == option) {
			return false;
		}

		final StreamsHandler streams = streamsHandler;
		final IRunValidator validator = RunValidator.NORMAL;
		final RunUtils.IRunStatusPrinter verbose
		= RunUtils.VERBOSE_PRINTER;
		final RunUtils.IRunStatusPrinter terse
		= RunUtils.TERSE_PRINTER;
		//        final boolean LOGPASS = true;
		//        final boolean LOGFAIL = true;
		//        final boolean SKIPPASS = false;
		//        final boolean SKIPFAIL = false;
		//        final boolean LOGSTREAMS = true;
		final boolean SKIPSTREAMS = false;

		Feature feature = null;
		if (super.acceptOption(option)) {
			// ok, result returned below

		} else if ("-XmakeTests".equals(option)) {
			testMaker = TestMaker.ME;
		} else if (option.startsWith("-traceTestsMin")) {
			feature = new Feature(option, AjcTest.class,new TestTraceLogger(streams, false));
		} else if (option.startsWith("-traceTests")) {
			feature = new Feature(option, AjcTest.class,new TestTraceLogger(streams, true));
		} else if (option.startsWith("-logMin")) {
			feature = new Feature(option, AjcTest.class,
					new RunLogger(option, SKIPSTREAMS, streams, validator, terse));
		} else if (option.startsWith("-logXML")) {
			feature = new Feature(option, AjcTest.class,
					new XmlLogger(option, streams, validator));
		} else if (option.startsWith("-logJUnit")) {
			feature = new Feature(option, AjcTest.class,
					new JUnitXMLLogger(option,streams,validator));
		} else if (option.startsWith("-log")) {
			feature = new Feature(option, AjcTest.class,
					new RunLogger(option, SKIPSTREAMS, streams, validator, verbose));
		} else if ("-hideRunStreams".equals(option)) {
			feature = new Feature(option, JavaRun.class, getStreamHider());
		} else if ("-hideCompilerStreams".equals(option)) {
			addFeature(new Feature(option, IncCompilerRun.class, getStreamHider()));   // hmmm
			feature = new Feature(option, CompilerRun.class, getStreamHider());
		} else if ("-progressDots".equals(option)) {
			IRunListener listener = new RunListener() {
				@Override
				public void runCompleted(IRunStatus run) {
					streamsHandler.log((validator.runPassed(run) ? "." : "!"));
				}
			};
			feature = new Feature(option, AjcTest.class, listener);
		} else if (option.startsWith("-XlogPublicType")) {
			String label = option + TestCompleteListener.PASS; // print when validator true
			feature = new Feature(option, AjcTest.class,
					new XmlLogger(label, streams, MessageRunValidator.PUBLIC_TYPE_ERROR));
		} else if (option.startsWith("-XlogSourceIn")) {
			String input = option.substring("-XlogSourceIn=".length());
			LangUtil.throwIaxIfFalse(0 < input.length(), option);
			String label = "-XlogSourceIn="  + TestCompleteListener.PASS; // print when validator true
			StringRunner sr = new SubstringRunner(input, false);
			feature = new Feature(option, AjcTest.class,
					new XmlLogger(label, streams, new SourcePathValidator(sr)));
		} else {
			return false;
		}
		addFeature(feature);
		return true;
	}

	/** lazy construction for shared hider */
	protected IRunListener getStreamHider() {
		if (null == streamHider) {
			streamHider =  new RunListener() {
				@Override
				public void runStarting(IRunStatus run) {
					streamsHandler.hide();
				}
				@Override
				public void runCompleted(IRunStatus run) {
					streamsHandler.show();
				}
				@Override
				public String toString() { return "Harness StreamHider"; }
			};
		}
		return streamHider;
	}
}

/** Generate any needed test case files for any test. */
class TestMaker  {

	static TestMaker ME = new TestMaker();

	/** @throws Error if unable to make dir */
	static void mkdirs(File dir) {
		if (null != dir && !dir.exists()) {
			if (!dir.mkdirs()) {
				throw new Error("unable to make dir: " + dir);
			}
		}
	}
	static String getFileContents(File baseDir, File file, String label) {
		String fileName = file.getName();
		if (fileName.endsWith(".java")) {
			fileName = fileName.substring(0, fileName.length() - 5);
		}
		StringBuffer sb = new StringBuffer();
		String filePath = file.getParentFile().getAbsolutePath();
		String dirPath = baseDir.getAbsolutePath();
		String pack = null;
		if (filePath.startsWith(dirPath)) {
			pack = filePath.substring(dirPath.length()).replace('/', '.');
		}
		if (!LangUtil.isEmpty(pack)) {
			sb.append("package " + pack + ";");
		}
		final String EOL = "\n"; // XXX find discovered EOL
		sb.append( EOL
				+ EOL + "import org.aspectj.testing.Tester;"
				+ EOL + ""
				+ EOL + "/** @testcase " + label + " */"
				+ EOL + "public class " + fileName + " {"
				+ EOL + "\tpublic static void main(String[] args) { "
				+ EOL + "\t\tTester.check(null != args, \"null args\"); "
				+ EOL + "\t}"
				+ EOL + "}"
				+ EOL
				);

		return sb.toString();
	}

	/** create a minimal source file for a test */
	static void createSrcFile(File baseDir, File file, String testName) {
		if (file.exists()) {
			return;
		}
		String contents = getFileContents(baseDir, file, testName);
		String error = FileUtil.writeAsString(file, contents);
		if (null != error) {
			throw new Error(error);
		}
	}

	/** create an empty arg file for a test */
	static void createArgFile(File baseDir, File file, String testName) {
		if (file.exists()) {
			return;
		}
		String contents = "// argfile " + file;
		String error = FileUtil.writeAsString(file, contents);
		if (null != error) {
			throw new Error(error);
		}
	}

	public Harness.RunResult run(AjcTest.Suite.Spec spec) {
		Iterable<IRunSpec> kids = spec.getChildren();
		for (IRunSpec iRunSpec : kids) {
			makeTest( (AjcTest.Spec) iRunSpec);
		}
		IRunStatus status = new RunStatus(new MessageHandler(), new Runner());
		status.start();
		status.finish(IRunStatus.PASS);
		return new Harness.RunResult(status, 0);
	}

	private void makeTest(AjcTest.Spec spec) {
		CompilerRun.Spec compileSpec = AjcTest.unwrapCompilerRunSpec(spec);
		if (null == spec) {
			throw new Error("null spec");
		}
		System.out.println("  generating test files for test: " + spec.getDescription());
		File dir = spec.getSuiteDir();
		if (null != dir) {
			TestMaker.mkdirs(dir);
		}
		String offset = spec.getTestDirOffset();
		if (!LangUtil.isEmpty(offset)) {
			if (null == dir) {
				dir = new File(offset);
			} else {
				dir = new File(dir.getAbsolutePath() + "/" + offset);
			}
		} else if (null == dir) {
			dir = new File(".");
		}
		StringBuffer testName = new StringBuffer();
		int pr = spec.getBugId();
		if (0 < pr) {
			testName.append("PR#" + pr + " ");
		}

		testName.append(spec.getDescription());
		final String label = testName.toString();
		final File[] srcFiles = FileUtil.getBaseDirFiles(dir, compileSpec.getPathsArray());
		if (!LangUtil.isEmpty(srcFiles)) {
			for (File srcFile : srcFiles) {
				TestMaker.createSrcFile(dir, srcFile, label);
			}
		}
		final File[] argFiles = FileUtil.getBaseDirFiles(dir, compileSpec.getArgfilesArray());
		if (!LangUtil.isEmpty(argFiles)) {
			for (File argFile : argFiles) {
				TestMaker.createArgFile(dir, argFile, label);
			}
		}

	}

	/** @return "Testmaker()" */
	@Override
	public String toString() {
		return "TestMaker()";
	}
}

interface StringRunner {
	boolean accept(String s);
}

/**
 * StringRunner than accepts input matching 0+ substrings,
 * optionally case-insensitive.
 */
class SubstringRunner implements StringRunner {
	private static String[] extractSubstrings(
			String substrings,
			boolean caseSensitive) {
		if (null == substrings) {
			return null;
		}
		StringTokenizer st = new StringTokenizer(substrings, ",");
		String[] result = new String[st.countTokens()];
		for (int i = 0; i < result.length; i++) {
			result[i] = st.nextToken().trim();
			LangUtil.throwIaxIfFalse(0 < result[i].length(), "empty entry");
			if (!caseSensitive) {
				result[i] = result[i].toLowerCase();
			}
		}
		return result;
	}

	private final String[] substrings;
	private final boolean caseSensitive;

	/**
	 * @param substrings the String containing comma-separated substrings
	 *                    to find in input - if null, any input accepted
	 * @param caseSensitive if true, do case-sensitive comparison
	 * @throws IllegalArgumentException if any substrings contains empty entry ", ,"
	 */
	SubstringRunner(String substrings, boolean caseSensitive) {
		this.caseSensitive = caseSensitive;
		this.substrings = extractSubstrings(substrings, caseSensitive);
	}

	@Override
	public boolean accept(String input) {
		if (null == substrings) {
			return true;
		}
		if (null == input) {
			return false;
		}

		if (!caseSensitive) {
			input = input.toLowerCase();
		}
		for (String substring : substrings) {
			if (input.contains(substring)) {
				return true;
			}
		}
		return false;
	}
}

/**
 * Signal whether run "passed" based on validating absolute source paths.
 * (Static evaluation - no run necessary)
 */
class SourcePathValidator implements IRunValidator { // static - no run needed
	private final StringRunner validator;
	// XXX hoist common
	SourcePathValidator(StringRunner validator) {
		LangUtil.throwIaxIfNull(validator, "validator");
		this.validator = validator;
	}
	/**
	 * @return true if any source files in compile spec are
	 *          accepted by the validator.
	 * @see org.aspectj.testing.run.IRunValidator#runPassed(IRunStatus)
	 */
	@Override
	public boolean runPassed(IRunStatus run) {
		AjcTest.Spec testSpec = AjcTest.unwrapSpec(run);
		if (null != testSpec) {
			CompilerRun.Spec compileSpec = AjcTest.unwrapCompilerRunSpec(testSpec);
			File basedir = new File(testSpec.getSuiteDir(), testSpec.getTestDirOffset());
			String[] paths = compileSpec.getPathsArray();
			File[] files = FileUtil.getBaseDirFiles(basedir, paths);
			for (File file : files) {
				if (validator.accept(file.getAbsolutePath())) {
					return true;
				}
			}
		}
		return false;
	}

}

/** Signal whether run "passed" based on message kind and content */
class MessageRunValidator implements IRunValidator {

	/** signals "passed" if any error contains "public type" */
	static final IRunValidator PUBLIC_TYPE_ERROR
	= new MessageRunValidator("public type", IMessage.ERROR, false);

	private final IMessage.Kind kind;
	private final String sought;
	private final boolean orGreater;

	/**
	 * @param sought the String to seek anywhere in any message of the right kind
	 *         if null, accept any message of the right kind.
	 * @param kind the IMessage.Kind of messages to search - all if null
	 */
	MessageRunValidator(String sought, IMessage.Kind kind, boolean orGreater) {
		this.sought = sought;
		this.kind = kind;
		this.orGreater = orGreater;
	}

	/** @return true if this run has messages of the right kind and text */
	@Override
	public boolean runPassed(IRunStatus run) {
		return gotMessage(new IRunStatus[] {run});
	}

	/**
	 * Search these children and their children recursively
	 * for messages of the right kind and content.
	 * @return true at first match of message of the right kind and content
	 */
	private boolean gotMessage(IRunStatus[] children) {
		if (LangUtil.isEmpty(children)) {
			return false;
		}
		for (IRunStatus run : children) {
			if (null == run) {
				continue; // hmm
			}
			IMessage[] messages = run.getMessages(kind, orGreater);
			if (!LangUtil.isEmpty(messages)) {
				if (LangUtil.isEmpty(sought)) {
					return true;
				} else {
					for (IMessage message : messages) {
						if (null == message) {
							continue; // hmm
						}
						String text = message.getMessage();
						if ((null != text) && (text.contains(sought))) {
							return true;
						}
					}
				}
			}
			if (gotMessage(run.getChildren())) {
				return true;
			}
		}
		return false;
	}
}

/**
 * Base class for listeners that run depending on pass/fail status of input.
 * Template method runCompleted handled whether to run.
 * Subclasses implement doRunCompleted(..).
 */
abstract class TestCompleteListener extends RunListener {
	/** label suffix indicating both pass and fail */
	public static final String ALL = "All";

	/** label suffix indicating fail */
	public static final String FAIL = "Fail";

	/** label suffix indicating pass */
	public static final String PASS = "Pass";


	/** runValidator determines if a given run passed */
	protected final IRunValidator runValidator;

	/** label for this listener */
	final String label;

	/** if trun and run passed, then run doRunCompleted(..) */
	final boolean logOnPass;

	/** if true and run did not pass, then run doRunCompleted(..) */
	final boolean logOnNotPass;

	/** may be null */
	protected final StreamsHandler streamsHandler;

	/** true if the last run evaluation was ok */
	boolean lastRunOk;

	/** last run evaluated */
	IRunStatus lastRun; // XXX small memory leak - cache hashcode instead?

	/** @param label endsWith PASS || FAIL || ALL */
	protected TestCompleteListener(
			String label,
			IRunValidator runValidator,
			StreamsHandler streamsHandler) {
		if (null == runValidator) {
			runValidator = RunValidator.NORMAL;
		}
		this.label = (null == label? "" : label);
		this.logOnPass = label.endsWith(PASS) || label.endsWith(ALL);
		this.logOnNotPass = label.endsWith(FAIL) || label.endsWith(ALL);
		this.runValidator = runValidator;
		this.streamsHandler = streamsHandler;
	}

	public void runStarted(IRunStatus run) {
		if (null != streamsHandler) {
			streamsHandler.startListening();
		}
	}

	/** subclasses implement this to do some per-test initialization */
	protected void doRunStarted(IRunStatus run) {
	}


	/** subclasses implement this to do some per-suite initialization */
	protected void doStartSuite(File suite) {
	}

	/** subclasses implement this to do end-of-suite processing */
	protected void doEndSuite(File suite, long duration) {
	}

	@Override
	public final void runCompleted(IRunStatus run) {
		boolean doit = lastRunOk(run);
		StreamsHandler.Result result = null;
		if (null != streamsHandler) {
			streamsHandler.endListening(doit);
		}
		if (doit) {
			doRunCompleted(run, result);
		}
	}

	/**
	 * @return true if run is ok per constructor specifications
	 */
	protected boolean lastRunOk(IRunStatus run) {
		if (lastRun != run) {
			boolean passed = runValidator.runPassed(run);
			lastRunOk = (passed ? logOnPass : logOnNotPass);
		}
		return lastRunOk;
	}

	/** @return "{classname}({pass}{,fail})" indicating when this runs */
	@Override
	public String toString() { // XXX add label?
		return LangUtil.unqualifiedClassName(this)
				+ "(" + (logOnPass ? (logOnNotPass ? "pass, fail)" : "pass)")
						: (logOnNotPass ? "fail)" : ")"));
	}
	/**
	 * Subclasses implement this to do some completion action
	 * @param run the IRunStatus for this completed run
	 * @param result the StreamsHandler.Result (if any - may be null)
	 */
	public abstract void doRunCompleted(IRunStatus run, StreamsHandler.Result result);
}

/**
 * Write XML for any test passed and/or failed.
 * Must register with Runner for RunSpecIterator.class,
 * most sensibly AjcTest.class.
 */
class XmlLogger extends TestCompleteListener {
	/**
	 * @param printer the component that prints any status - not null
	 * @param runValidator if null, use RunValidator.NORMAL
	 */
	public XmlLogger(
			String label,
			StreamsHandler streamsHandler,
			IRunValidator runValidator) {
		super(label, runValidator, streamsHandler);
	}

	@Override
	public void doRunCompleted(IRunStatus run, StreamsHandler.Result result) {
		PrintStream out = streamsHandler.getLogStream();
		out.println("");
		XMLWriter writer = new XMLWriter(new PrintWriter(out, true));
		Object id = run.getIdentifier();
		if (!(id instanceof Runner.IteratorWrapper)) {
			out.println(this + " not IteratorWrapper: "
					+ id.getClass().getName() + ": " + id);
			return;
		}
		IRunIterator iter = ((Runner.IteratorWrapper) id).iterator;
		if (!(iter instanceof RunSpecIterator)) {
			out.println(this + " not RunSpecIterator: " + iter.getClass().getName()
					+ ": " + iter);
			return;
		}
		((RunSpecIterator) iter).spec.writeXml(writer);
		out.flush();
	}

}

/**
 * Write junit style XML output (for incorporation into html test results and
 * cruise control reports
 * format is...
 * <?xml version="1.0" encoding="UTF-8" ?>
 * <testsuite errors="x" failures="x" name="suite-name" tests="xx" time="ss.ssss">
 * <properties/>
 * <testcase name="passingTest" time="s.hh"></testcase>
 * <testcase name="failingTest" time="s.hh">
 *   <failure message="failureMessage" type="ExceptionType">free text</failure>
 * </testcase>
 * </testsuite>
 */
class JUnitXMLLogger extends TestCompleteListener {

	//  private File suite;
	private StringBuffer junitOutput;
	private long startTimeMillis;
	private int numTests = 0;
	private int numFails = 0;
	private DecimalFormat timeFormatter = new DecimalFormat("#.##");

	public JUnitXMLLogger(
			String label,
			StreamsHandler streamsHandler,
			IRunValidator runValidator) {
		super(label + ALL, runValidator, streamsHandler);
		junitOutput = new StringBuffer();
	}

	/* (non-Javadoc)
	 * @see org.aspectj.testing.drivers.TestCompleteListener#doRunCompleted(org.aspectj.testing.run.IRunStatus, org.aspectj.testing.util.StreamsHandler.Result)
	 */
	@Override
	public void doRunCompleted(IRunStatus run, Result result) {
		long duration = System.currentTimeMillis()  - startTimeMillis;
		numTests++;
		junitOutput.append("<testcase name=\"" + run.getIdentifier() + "\" ");
		junitOutput.append("time=\"" + timeFormatter.format((duration)/1000.0f) + "\"");
		junitOutput.append(">");
		if (!run.runResult())  {
			numFails++;
			junitOutput.append("\n");
			junitOutput.append("<failure message=\"test failed\" type=\"unknown\">\n");
			//			junitOutput.println(result.junitOutput);
			//			junitOutput.println(result.err);
			junitOutput.append("</failure>\n");
		}
		junitOutput.append("</testcase>\n");
	}

	/* (non-Javadoc)
	 * @see org.aspectj.testing.drivers.TestCompleteListener#runStarted(org.aspectj.testing.run.IRunStatus)
	 */
	@Override
	public void runStarting(IRunStatus run) {
		super.runStarting(run);
		startTimeMillis = System.currentTimeMillis();
	}

	/* (non-Javadoc)
	 * @see org.aspectj.testing.drivers.TestCompleteListener#doEndSuite(java.io.File, long)
	 */
	@Override
	protected void doEndSuite(File suite, long duration) {
		super.doEndSuite(suite, duration);
		String suiteName = suite.getName();
		// junit reporter doesn't like ".xml" on the end
		suiteName = suiteName.substring(0,suiteName.indexOf('.'));
		PrintStream out = streamsHandler.getLogStream();
		out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		String timeStr = new DecimalFormat("#.##").format(duration/1000.0);
		out.print("<testsuite errors=\"" + numFails + "\" failures=\"0\" ");
		out.print("name=\"" + suite.getName() + "\" " );
		out.println("tests=\"" + numTests + "\" time=\"" + timeStr + "\">");
		out.print(junitOutput.toString());
		out.println("</testsuite>");
	}

	/* (non-Javadoc)
	 * @see org.aspectj.testing.drivers.TestCompleteListener#doStartSuite(java.io.File)
	 */
	@Override
	protected void doStartSuite(File suite) {
		super.doStartSuite(suite);
		//		this.suite = suite;
		numTests = 0;
		numFails = 0;
		junitOutput = new StringBuffer();
	}

}

/** log pass and/or failed runs */
class RunLogger extends TestCompleteListener {
	final boolean logStreams;
	final RunUtils.IRunStatusPrinter printer;

	/**
	 * @param printer the component that prints any status - not null
	 * @param runValidator if null, use RunValidator.NORMAL
	 */
	public RunLogger(
			String label,
			boolean logStreams,
			StreamsHandler streamsHandler,
			IRunValidator runValidator,
			RunUtils.IRunStatusPrinter printer) {
		super(label, runValidator, streamsHandler);
		LangUtil.throwIaxIfNull(streamsHandler, "streamsHandler");
		LangUtil.throwIaxIfNull(printer, "printer");
		this.logStreams = logStreams;
		this.printer = printer;
	}

	@Override
	public void doRunCompleted(IRunStatus run, StreamsHandler.Result result) {
		PrintStream out = streamsHandler.getLogStream();
		printer.printRunStatus(out, run);
		if (logStreams) {
			if (!LangUtil.isEmpty(result.err)) {
				out.println("--- error");
				out.println(result.err);
			}
			if (!LangUtil.isEmpty(result.out)) {
				out.println("--- ouput");
				out.println(result.out);
			}
		}
		out.println("");
	}
}

/** trace time and memory between runStaring and runCompleted */
class TestTraceLogger extends TestCompleteListener {
	private static final Runtime runtime = Runtime.getRuntime();
	private long startTime;
	private long startMemoryFree;
	private final boolean verbose;

	public TestTraceLogger(StreamsHandler handler) {
		this(handler, true);
	}
	public TestTraceLogger(StreamsHandler handler, boolean verbose) {
		super("-traceTestsAll", null, handler);
		this.verbose = verbose;
	}
	@Override
	public void runStarting(IRunStatus run) {
		super.runStarting(run);
		startTime = System.currentTimeMillis();
		startMemoryFree = runtime.freeMemory();
	}

	@Override
	public void doRunCompleted(IRunStatus run, StreamsHandler.Result result) {
		long elapsed = System.currentTimeMillis() - startTime;
		long free = runtime.freeMemory();
		long used = startMemoryFree - free;
		String label = run.runResult() ? "PASS " : "FAIL ";
		PrintStream out = streamsHandler.getLogStream();
		if (verbose) {
			label = label
					+ "elapsed: " + LangUtil.toSizedString(elapsed, 7)
					+ " free: " + LangUtil.toSizedString(free, 10)
					+ " used: " + LangUtil.toSizedString(used, 10)
					+ " id: ";
		}
		out.println(label + renderId(run));
	}

	/** @return true - always trace tests */
	protected boolean isFailLabel(String label) {
		return true;
	}

	/** @return true - always trace tests */
	protected boolean isPassLabel(String label) {
		return true;
	}

	/**
	 * This implementation returns run identifier toString().
	 * Subclasses override this to render id as message suffix.
	 */
	protected String renderId(IRunStatus run) {
		return "" + run.getIdentifier();
	}
}
// printing files
//        AjcTest.Spec testSpec = AjcTest.unwrapSpec(run);
//        if (null != testSpec) {
//            CompilerRun.Spec compileSpec = AjcTest.unwrapCompilerRunSpec(testSpec);
//            File dir = new File(testSpec.getSuiteDir(), testSpec.getTestDirOffset());
//            List files = compileSpec.getPathsAsFile(dir);
//            StringBuffer sb = new StringBuffer();
//            for (Iterator iter = files.iterator(); iter.hasNext();) {
//                File file = (File) iter.next();
//                sb.append(" " + file.getPath().replace('\\','/').substring(2));
//            }
//            out.println("files: " + sb);
//        }


