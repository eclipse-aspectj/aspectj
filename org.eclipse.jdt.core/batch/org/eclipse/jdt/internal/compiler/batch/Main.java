/*******************************************************************************
 * Copyright (c) 2000, 2001, 2002 International Business Machines Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.jdt.internal.compiler.batch;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.StringTokenizer;

import org.eclipse.jdt.core.compiler.InvalidInputException;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.internal.compiler.ClassFile;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.Compiler;
import org.eclipse.jdt.internal.compiler.ICompilerRequestor;
import org.eclipse.jdt.internal.compiler.IErrorHandlingPolicy;
import org.eclipse.jdt.internal.compiler.IProblemFactory;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblem;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblemFactory;
import org.eclipse.jdt.internal.compiler.problem.ProblemSeverities;
import org.eclipse.jdt.internal.compiler.util.CharOperation;
import org.eclipse.jdt.internal.compiler.util.HashtableOfObject;

public class Main implements ProblemSeverities {

	public boolean noWarn = false;

	public PrintWriter out;
	public boolean systemExitWhenFinished = true;
	public boolean proceedOnError = false;

	public boolean verbose = false;
	public boolean produceRefInfo = false;
	public boolean timer = false;
	public boolean showProgress = false;
	public long time = 0;
	public long lineCount;
	public boolean generatePackagesStructure;

	public Hashtable options;
	public String[] filenames;
	public String[] encodings;
	public String[] classpaths;
	public String destinationPath;
	public String log;
	public int repetitions;
	public int globalProblemsCount;
	public int globalErrorsCount;
	public int globalWarningsCount;
	public int exportedClassFilesCounter;

	public static final char[] CLASS_FILE_EXTENSION = ".class".toCharArray(); //$NON-NLS-1$
	public final static char[] DOUBLE_QUOTES = "''".toCharArray(); //$NON-NLS-1$
	public final static char[] SINGLE_QUOTE = "'".toCharArray(); //$NON-NLS-1$

	/* Bundle containing messages */
	public static ResourceBundle bundle;
	public final static String bundleName =
		"org.eclipse.jdt.internal.compiler.batch.messages"; 	//$NON-NLS-1$

	static {
		relocalize();
	}

	public boolean proceed = true;

	public Main(PrintWriter writer, boolean systemExitWhenFinished) {

		this.out = writer;
		this.systemExitWhenFinished = systemExitWhenFinished;
		exportedClassFilesCounter = 0;
		options = new Hashtable();
		options.put(
			CompilerOptions.OPTION_LocalVariableAttribute,
			CompilerOptions.DO_NOT_GENERATE);
		options.put(
			CompilerOptions.OPTION_LineNumberAttribute,
			CompilerOptions.DO_NOT_GENERATE);
		options.put(
			CompilerOptions.OPTION_SourceFileAttribute,
			CompilerOptions.DO_NOT_GENERATE);
		options.put(
			CompilerOptions.OPTION_PreserveUnusedLocal,
			CompilerOptions.OPTIMIZE_OUT);
		options.put(
			CompilerOptions.OPTION_ReportUnreachableCode,
			CompilerOptions.ERROR);
		options.put(CompilerOptions.OPTION_ReportInvalidImport, CompilerOptions.ERROR);
		options.put(
			CompilerOptions.OPTION_ReportOverridingPackageDefaultMethod,
			CompilerOptions.WARNING);
		options.put(
			CompilerOptions.OPTION_ReportMethodWithConstructorName,
			CompilerOptions.WARNING);
		options.put(CompilerOptions.OPTION_ReportDeprecation, CompilerOptions.WARNING);
		options.put(
			CompilerOptions.OPTION_ReportHiddenCatchBlock,
			CompilerOptions.WARNING);
		options.put(CompilerOptions.OPTION_ReportUnusedLocal, CompilerOptions.IGNORE);
		options.put(
			CompilerOptions.OPTION_ReportUnusedParameter,
			CompilerOptions.IGNORE);
		options.put(
			CompilerOptions.OPTION_ReportSyntheticAccessEmulation,
			CompilerOptions.IGNORE);
		options.put(
			CompilerOptions.OPTION_ReportNonExternalizedStringLiteral,
			CompilerOptions.IGNORE);
		options.put(
			CompilerOptions.OPTION_ReportAssertIdentifier,
			CompilerOptions.IGNORE);
		options.put(
			CompilerOptions.OPTION_Compliance,
			CompilerOptions.VERSION_1_3);
		options.put(
			CompilerOptions.OPTION_Source,
			CompilerOptions.VERSION_1_3);
		options.put(
			CompilerOptions.OPTION_TargetPlatform,
			CompilerOptions.VERSION_1_1);
	}

	/*
	 *  Low-level API performing the actual compilation
	 */
	public boolean compile(String[] argv) {

		// decode command line arguments
		try {
			configure(argv);
			if (proceed) {
				if (showProgress)
					out.print(Main.bind("progress.compiling")); //$NON-NLS-1$
				for (int i = 0; i < repetitions; i++) {
					globalProblemsCount = 0;
					globalErrorsCount = 0;
					globalWarningsCount = 0;
					lineCount = 0;

					if (repetitions > 1) {
						out.flush();
						out.println(
							Main.bind(
								"compile.repetition", //$NON-NLS-1$
								String.valueOf(i + 1),
								String.valueOf(repetitions)));
					}
					long startTime = System.currentTimeMillis();
					// request compilation
					performCompilation();
					if (timer) {

						time = System.currentTimeMillis() - startTime;
						if (lineCount != 0) {
							out.println(
								Main.bind(
									"compile.instantTime", 	//$NON-NLS-1$
									new String[] {
										String.valueOf(lineCount),
										String.valueOf(time),
										String.valueOf((((int) ((lineCount * 10000.0) / time)) / 10.0))}));
						} else {
							out.println(Main.bind("compile.totalTime", String.valueOf(time))); //$NON-NLS-1$
						}
					}
					if (globalProblemsCount > 0) {
						if (globalProblemsCount == 1) {
							out.print(Main.bind("compile.oneProblem")); //$NON-NLS-1$
						} else {
							out.print(
								Main.bind("compile.severalProblems", String.valueOf(globalProblemsCount))); 	//$NON-NLS-1$
						}
						out.print(" ("); //$NON-NLS-1$
						if (globalErrorsCount > 0) {
							if (globalErrorsCount == 1) {
								out.print(Main.bind("compile.oneError")); //$NON-NLS-1$
							} else {
								out.print(
									Main.bind("compile.severalErrors", String.valueOf(globalErrorsCount))); 	//$NON-NLS-1$
							}
						}
						if (globalWarningsCount > 0) {
							if (globalErrorsCount > 0) {
								out.print(", "); //$NON-NLS-1$
							}
							if (globalWarningsCount == 1) {
								out.print(Main.bind("compile.oneWarning")); //$NON-NLS-1$
							} else {
								out.print(
									Main.bind("compile.severalWarnings", String.valueOf(globalWarningsCount))); 	//$NON-NLS-1$
							}
						}
						out.println(")"); //$NON-NLS-1$
					}
					if (exportedClassFilesCounter != 0
						&& (this.showProgress || this.timer || this.verbose)) {
						if (exportedClassFilesCounter == 1) {
							out.print(Main.bind("compile.oneClassFileGenerated")); //$NON-NLS-1$
						} else {
							out.print(
								Main.bind(
									"compile.severalClassFilesGenerated", //$NON-NLS-1$
									String.valueOf(exportedClassFilesCounter)));
						}
					}
				}
				if (showProgress)
					System.out.println();
			}
			if (systemExitWhenFinished) {
				out.flush();
				System.exit(globalErrorsCount > 0 ? -1 : 0);
			}
		} catch (InvalidInputException e) {
			out.println(e.getMessage());
			out.println("------------------------"); //$NON-NLS-1$
			printUsage();
			if (systemExitWhenFinished) {
				System.exit(-1);
			}
		} catch (ThreadDeath e) { // do not stop this one
			throw e;
		} catch (Throwable e) { // internal compiler error
			if (systemExitWhenFinished) {
				out.flush();
				if (this.log != null) {
					out.close();
				}
				System.exit(-1);
			}
			//e.printStackTrace();
		} finally {
			out.flush();
			if (this.log != null) {
				out.close();
			}
		}
		if (globalErrorsCount == 0){
			return true;
		} else {
			return false;
		}
	}

	/*
	 * Internal IDE API
	 */
	public static boolean compile(String commandLine) {

		return compile(commandLine, new PrintWriter(System.out));
	}

	/*
	 * Internal IDE API for test harness purpose
	 */
	public static boolean compile(String commandLine, PrintWriter writer) {

		return new Main(writer, false).compile(tokenize(commandLine));
	}

	public static String[] tokenize(String commandLine) {

		int count = 0;
		String[] arguments = new String[10];
		StringTokenizer tokenizer = new StringTokenizer(commandLine, " \"", true); //$NON-NLS-1$
		String token = "", lastToken; //$NON-NLS-1$
		boolean insideQuotes = false;
		boolean startNewToken = true;

		// take care to quotes on the command line
		// 'xxx "aaa bbb";ccc yyy' --->  {"xxx", "aaa bbb;ccc", "yyy" }
		// 'xxx "aaa bbb;ccc" yyy' --->  {"xxx", "aaa bbb;ccc", "yyy" }
		// 'xxx "aaa bbb";"ccc" yyy' --->  {"xxx", "aaa bbb;ccc", "yyy" }
		// 'xxx/"aaa bbb";"ccc" yyy' --->  {"xxx/aaa bbb;ccc", "yyy" }
		while (tokenizer.hasMoreTokens()) {
			lastToken = token;
			token = tokenizer.nextToken();

			if (token.equals(" ")) { //$NON-NLS-1$
				if (insideQuotes) {
					arguments[count - 1] += token;
					startNewToken = false;
				} else {
					startNewToken = true;
				}
			} else if (token.equals("\"")) { //$NON-NLS-1$
				if (!insideQuotes && startNewToken) { //$NON-NLS-1$
					if (count == arguments.length)
						System.arraycopy(arguments, 0, (arguments = new String[count * 2]), 0, count);
					arguments[count++] = ""; //$NON-NLS-1$
				}
				insideQuotes = !insideQuotes;
				startNewToken = false;
			} else {
				if (insideQuotes) {
					arguments[count - 1] += token;
				} else {
					if (token.length() > 0 && !startNewToken) {
						arguments[count - 1] += token;
					} else {
						if (count == arguments.length)
							System.arraycopy(arguments, 0, (arguments = new String[count * 2]), 0, count);
						arguments[count++] = token;
					}
				}
				startNewToken = false;
			}
		}
		System.arraycopy(arguments, 0, arguments = new String[count], 0, count);
		return arguments;
	}

	/*
	Decode the command line arguments 
	 */
	public void configure(String[] argv) throws InvalidInputException {
		
		if ((argv == null) || (argv.length == 0))
			throw new InvalidInputException(Main.bind("configure.noSourceFile")); //$NON-NLS-1$
		final int InsideClasspath = 1;
		final int InsideDestinationPath = 2;
		final int TargetSetting = 4;
		final int InsideLog = 8;
		final int InsideRepetition = 16;
		final int InsideSource = 32;
		final int InsideDefaultEncoding = 64;
		final int Default = 0;
		int DEFAULT_SIZE_CLASSPATH = 4;
		boolean warnOptionInUse = false;
		boolean noWarnOptionInUse = false;
		int pathCount = 0;
		int index = -1, filesCount = 0, argCount = argv.length;
		int mode = Default;
		repetitions = 0;
		boolean versionIDRequired = false;
		boolean printUsageRequired = false;

		boolean didSpecifyCompliance = false;
		boolean didSpecifySourceLevel = false;
		boolean didSpecifyDefaultEncoding = false;
		boolean didSpecifyTarget = false;

		String customEncoding = null;
		String currentArg = ""; //$NON-NLS-1$

		while (++index < argCount) {

			if (customEncoding != null) {
				throw new InvalidInputException(
					Main.bind("configure.unexpectedCustomEncoding", currentArg, customEncoding)); //$NON-NLS-1$
			}

			currentArg = argv[index].trim();

			customEncoding = null;
			if (currentArg.endsWith("]")) { //$NON-NLS-1$ 
				// look for encoding specification
				int encodingStart = currentArg.indexOf('[') + 1;
				int encodingEnd = currentArg.length() - 1;
				if (encodingStart >= 1) {
					if (encodingStart < encodingEnd) {
						customEncoding = currentArg.substring(encodingStart, encodingEnd);
						try { // ensure encoding is supported
							new InputStreamReader(new ByteArrayInputStream(new byte[0]), customEncoding);
						} catch (UnsupportedEncodingException e) {
							throw new InvalidInputException(
								Main.bind("configure.unsupportedEncoding", customEncoding)); //$NON-NLS-1$
						}
					}
					currentArg = currentArg.substring(0, encodingStart - 1);
				}
			}

			if (currentArg.endsWith(".java")) { //$NON-NLS-1$
				if (filenames == null) {
					filenames = new String[argCount - index];
					encodings = new String[argCount - index];
				} else if (filesCount == filenames.length) {
					int length = filenames.length;
					System.arraycopy(
						filenames,
						0,
						(filenames = new String[length + argCount - index]),
						0,
						length);
					System.arraycopy(
						encodings,
						0,
						(encodings = new String[length + argCount - index]),
						0,
						length);
				}
				filenames[filesCount] = currentArg;
				encodings[filesCount++] = customEncoding;
				customEncoding = null;
				mode = Default;
				continue;
			}
			if (currentArg.equals("-log")) { //$NON-NLS-1$
				if (log != null)
					throw new InvalidInputException(
						Main.bind("configure.duplicateLog", currentArg)); //$NON-NLS-1$
				mode = InsideLog;
				continue;
			}
			if (currentArg.equals("-repeat")) { //$NON-NLS-1$
				if (repetitions > 0)
					throw new InvalidInputException(
						Main.bind("configure.duplicateRepeat", currentArg)); //$NON-NLS-1$
				mode = InsideRepetition;
				continue;
			}
			if (currentArg.equals("-source")) { //$NON-NLS-1$
				mode = InsideSource;
				didSpecifySourceLevel = true;
				continue;
			}
			if (currentArg.equals("-encoding")) { //$NON-NLS-1$
				mode = InsideDefaultEncoding;
				continue;
			}
			if (currentArg.equals("-1.3")) { //$NON-NLS-1$
				if (didSpecifyCompliance) {
					throw new InvalidInputException(
						Main.bind("configure.duplicateCompliance", currentArg));//$NON-NLS-1$
				}
				didSpecifyCompliance = true;
				options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_1_3);
				mode = Default;
				continue;
			}
			if (currentArg.equals("-1.4")) { //$NON-NLS-1$
				if (didSpecifyCompliance) {
					throw new InvalidInputException(
						Main.bind("configure.duplicateCompliance", currentArg)); //$NON-NLS-1$
				}
				didSpecifyCompliance = true;
				options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_1_4);
				mode = Default;
				continue;
			}
			if (currentArg.equals("-d")) { //$NON-NLS-1$
				if (destinationPath != null)
					throw new InvalidInputException(
						Main.bind("configure.duplicateOutputPath", currentArg)); //$NON-NLS-1$
				mode = InsideDestinationPath;
				generatePackagesStructure = true;
				continue;
			}
			if (currentArg.equals("-classpath") //$NON-NLS-1$
				|| currentArg.equals("-cp")) { //$NON-NLS-1$ //$NON-NLS-2$
				if (pathCount > 0)
					throw new InvalidInputException(
						Main.bind("configure.duplicateClasspath", currentArg)); //$NON-NLS-1$
				classpaths = new String[DEFAULT_SIZE_CLASSPATH];
				mode = InsideClasspath;
				continue;
			}
			if (currentArg.equals("-progress")) { //$NON-NLS-1$
				mode = Default;
				showProgress = true;
				continue;
			}
			if (currentArg.equals("-proceedOnError")) { //$NON-NLS-1$
				mode = Default;
				proceedOnError = true;
				continue;
			}
			if (currentArg.equals("-time")) { //$NON-NLS-1$
				mode = Default;
				timer = true;
				continue;
			}
			if (currentArg.equals("-version") //$NON-NLS-1$
				|| currentArg.equals("-v")) { //$NON-NLS-1$ //$NON-NLS-2$
				versionIDRequired = true;
				continue;
			}
			if ("-deprecation".equals(currentArg)) { //$NON-NLS-1$
				warnOptionInUse = true;
				if (noWarnOptionInUse)
					throw new InvalidInputException(
						Main.bind("configure.duplicateWarningConfiguration")); //$NON-NLS-1$				
				options.put(CompilerOptions.OPTION_ReportDeprecation, CompilerOptions.WARNING);
				continue;
			}
			if (currentArg.equals("-help")) { //$NON-NLS-1$
				printUsageRequired = true;
				continue;
			}
			if (currentArg.equals("-noImportError")) { //$NON-NLS-1$
				mode = Default;
				options.put(
					CompilerOptions.OPTION_ReportInvalidImport,
					CompilerOptions.WARNING);
				continue;
			}
			if (currentArg.equals("-noExit")) { //$NON-NLS-1$
				mode = Default;
				systemExitWhenFinished = false;
				continue;
			}
			if (currentArg.equals("-verbose")) { //$NON-NLS-1$
				mode = Default;
				verbose = true;
				continue;
			}
			if (currentArg.equals("-referenceInfo")) { //$NON-NLS-1$
				mode = Default;
				produceRefInfo = true;
				continue;
			}
			if (currentArg.startsWith("-g")) { //$NON-NLS-1$
				mode = Default;
				String debugOption = currentArg;
				int length = currentArg.length();
				if (length == 2) {
					options.put(
						CompilerOptions.OPTION_LocalVariableAttribute,
						CompilerOptions.GENERATE);
					options.put(
						CompilerOptions.OPTION_LineNumberAttribute,
						CompilerOptions.GENERATE);
					options.put(
						CompilerOptions.OPTION_SourceFileAttribute,
						CompilerOptions.GENERATE);
					continue;
				}
				if (length > 3) {
					options.put(
						CompilerOptions.OPTION_LocalVariableAttribute,
						CompilerOptions.DO_NOT_GENERATE);
					options.put(
						CompilerOptions.OPTION_LineNumberAttribute,
						CompilerOptions.DO_NOT_GENERATE);
					options.put(
						CompilerOptions.OPTION_SourceFileAttribute,
						CompilerOptions.DO_NOT_GENERATE);
					if (length == 7 && debugOption.equals("-g:none")) //$NON-NLS-1$
						continue;
					StringTokenizer tokenizer =
						new StringTokenizer(debugOption.substring(3, debugOption.length()), ","); //$NON-NLS-1$
					while (tokenizer.hasMoreTokens()) {
						String token = tokenizer.nextToken();
						if (token.equals("vars")) { //$NON-NLS-1$
							options.put(
								CompilerOptions.OPTION_LocalVariableAttribute,
								CompilerOptions.GENERATE);
						} else if (token.equals("lines")) { //$NON-NLS-1$
							options.put(
								CompilerOptions.OPTION_LineNumberAttribute,
								CompilerOptions.GENERATE);
						} else if (token.equals("source")) { //$NON-NLS-1$
							options.put(
								CompilerOptions.OPTION_SourceFileAttribute,
								CompilerOptions.GENERATE);
						} else {
							throw new InvalidInputException(
								Main.bind("configure.invalidDebugOption", debugOption)); //$NON-NLS-1$
							//$NON-NLS-1$
						}
					}
					continue;
				}
				throw new InvalidInputException(
					Main.bind("configure.invalidDebugOption", debugOption)); //$NON-NLS-1$
			}
			if (currentArg.startsWith("-nowarn")) { //$NON-NLS-1$
				noWarnOptionInUse = true;
				noWarn = true;
				if (warnOptionInUse)
					throw new InvalidInputException(
						Main.bind("configure.duplicateWarningConfiguration")); //$NON-NLS-1$
				mode = Default;
				continue;
			}
			if (currentArg.startsWith("-warn")) { //$NON-NLS-1$
				warnOptionInUse = true;
				if (noWarnOptionInUse)
					throw new InvalidInputException(
						Main.bind("configure.duplicateWarningConfiguration")); //$NON-NLS-1$
				mode = Default;
				String warningOption = currentArg;
				int length = currentArg.length();
				if (length == 10 && warningOption.equals("-warn:none")) { //$NON-NLS-1$
					noWarn = true;
					continue;
				}
				if (length < 6)
					throw new InvalidInputException(
						Main.bind("configure.invalidWarningConfiguration", warningOption)); //$NON-NLS-1$
				StringTokenizer tokenizer =
					new StringTokenizer(warningOption.substring(6, warningOption.length()), ","); //$NON-NLS-1$
				int tokenCounter = 0;

				options.put(
					CompilerOptions.OPTION_ReportOverridingPackageDefaultMethod,
					CompilerOptions.IGNORE);
				options.put(
					CompilerOptions.OPTION_ReportMethodWithConstructorName,
					CompilerOptions.IGNORE);
				options.put(
					CompilerOptions.OPTION_ReportDeprecation, 
					CompilerOptions.IGNORE);
				options.put(
					CompilerOptions.OPTION_ReportHiddenCatchBlock,
					CompilerOptions.IGNORE);
				options.put(
					CompilerOptions.OPTION_ReportUnusedLocal, 
					CompilerOptions.IGNORE);
				options.put(
					CompilerOptions.OPTION_ReportUnusedParameter,
					CompilerOptions.IGNORE);
				options.put(
					CompilerOptions.OPTION_ReportSyntheticAccessEmulation,
					CompilerOptions.IGNORE);
				options.put(
					CompilerOptions.OPTION_ReportNonExternalizedStringLiteral,
					CompilerOptions.IGNORE);
				options.put(
					CompilerOptions.OPTION_ReportAssertIdentifier,
					CompilerOptions.IGNORE);
				options.put(
					CompilerOptions.OPTION_ReportUnusedImport,
					CompilerOptions.IGNORE);

				while (tokenizer.hasMoreTokens()) {
					String token = tokenizer.nextToken();
					tokenCounter++;
					if (token.equals("constructorName")) { //$NON-NLS-1$
						options.put(
							CompilerOptions.OPTION_ReportMethodWithConstructorName,
							CompilerOptions.WARNING);
					} else if (token.equals("packageDefaultMethod")) { //$NON-NLS-1$
						options.put(
							CompilerOptions.OPTION_ReportOverridingPackageDefaultMethod,
							CompilerOptions.WARNING);
					} else if (token.equals("maskedCatchBlocks")) { //$NON-NLS-1$
						options.put(
							CompilerOptions.OPTION_ReportHiddenCatchBlock,
							CompilerOptions.WARNING);
					} else if (token.equals("deprecation")) { //$NON-NLS-1$
						options.put(
							CompilerOptions.OPTION_ReportDeprecation, 
							CompilerOptions.WARNING);
					} else if (token.equals("unusedLocals")) { //$NON-NLS-1$
						options.put(
							CompilerOptions.OPTION_ReportUnusedLocal, 
							CompilerOptions.WARNING);
					} else if (token.equals("unusedArguments")) { //$NON-NLS-1$
						options.put(
							CompilerOptions.OPTION_ReportUnusedParameter,
							CompilerOptions.WARNING);
					} else if (token.equals("unusedImports")) { //$NON-NLS-1$
						options.put(
							CompilerOptions.OPTION_ReportUnusedImport,
							CompilerOptions.WARNING);
					} else if (token.equals("syntheticAccess")) { //$NON-NLS-1$
						options.put(
							CompilerOptions.OPTION_ReportSyntheticAccessEmulation,
							CompilerOptions.WARNING);
					} else if (token.equals("nls")) { //$NON-NLS-1$
						options.put(
							CompilerOptions.OPTION_ReportNonExternalizedStringLiteral,
							CompilerOptions.WARNING);
					} else if (token.equals("assertIdentifier")) { //$NON-NLS-1$
						options.put(
							CompilerOptions.OPTION_ReportAssertIdentifier,
							CompilerOptions.WARNING);
					} else {
						throw new InvalidInputException(Main.bind("configure.invalidWarning", token)); //$NON-NLS-1$
					}
				}
				if (tokenCounter == 0)
					throw new InvalidInputException(
						Main.bind("configure.invalidWarningOption", currentArg)); //$NON-NLS-1$
				continue;
			}
			if (currentArg.equals("-target")) { //$NON-NLS-1$
				didSpecifyTarget = true;
				mode = TargetSetting;
				continue;
			}
			if (currentArg.equals("-preserveAllLocals")) { //$NON-NLS-1$
				options.put(
					CompilerOptions.OPTION_PreserveUnusedLocal,
					CompilerOptions.PRESERVE);
				continue;
			}
			if (mode == TargetSetting) {
				if (currentArg.equals("1.1")) { //$NON-NLS-1$
					options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_1_1);
				} else if (currentArg.equals("1.2")) { //$NON-NLS-1$
					options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_1_2);
				} else if (currentArg.equals("1.3")) { //$NON-NLS-1$
					options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_1_3);
				} else if (currentArg.equals("1.4")) { //$NON-NLS-1$
					options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_1_4);
				} else {
					throw new InvalidInputException(Main.bind("configure.targetJDK", currentArg)); //$NON-NLS-1$
				}
				mode = Default;
				continue;
			}
			if (mode == InsideLog) {
				log = currentArg;
				mode = Default;
				continue;
			}
			if (mode == InsideRepetition) {
				try {
					repetitions = Integer.parseInt(currentArg);
					if (repetitions <= 0) {
						throw new InvalidInputException(Main.bind("configure.repetition", currentArg)); //$NON-NLS-1$
					}
				} catch (NumberFormatException e) {
					throw new InvalidInputException(Main.bind("configure.repetition", currentArg)); //$NON-NLS-1$
				}
				mode = Default;
				continue;
			}
			if (mode == InsideSource) {
				if (currentArg.equals("1.3")) { //$NON-NLS-1$
					options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_1_3);
				} else if (currentArg.equals("1.4")) { //$NON-NLS-1$
					options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_1_4);
				} else {
					throw new InvalidInputException(Main.bind("configure.source", currentArg)); //$NON-NLS-1$
				}
				mode = Default;
				continue;
			}
			if (mode == InsideDefaultEncoding) {
				if (didSpecifyDefaultEncoding) {
					throw new InvalidInputException(
						Main.bind("configure.duplicateDefaultEncoding", currentArg)); //$NON-NLS-1$
				}
				try { // ensure encoding is supported
					new InputStreamReader(new ByteArrayInputStream(new byte[0]), currentArg);
				} catch (UnsupportedEncodingException e) {
					throw new InvalidInputException(
						Main.bind("configure.unsupportedEncoding", currentArg)); //$NON-NLS-1$
				}
				options.put(CompilerOptions.OPTION_Encoding, currentArg);
				didSpecifyDefaultEncoding = true;
				mode = Default;
				continue;
			}
			if (mode == InsideDestinationPath) {
				destinationPath = currentArg;
				mode = Default;
				continue;
			}
			if (mode == InsideClasspath) {
				StringTokenizer tokenizer = new StringTokenizer(currentArg, File.pathSeparator);
				while (tokenizer.hasMoreTokens()) {
					int length;
					if ((length = classpaths.length) <= pathCount) {
						System.arraycopy(
							classpaths,
							0,
							(classpaths = new String[length * 2]),
							0,
							length);
					}
					classpaths[pathCount++] = tokenizer.nextToken();
				}
				mode = Default;
				continue;
			}
			//default is input directory
			currentArg = currentArg.replace('/', File.separatorChar);
			if (currentArg.endsWith(File.separator))
				currentArg =
					currentArg.substring(0, currentArg.length() - File.separator.length());
			File dir = new File(currentArg);
			if (!dir.isDirectory())
				throw new InvalidInputException(
					Main.bind("configure.directoryNotExist", currentArg)); //$NON-NLS-1$
			FileFinder finder = new FileFinder();
			try {
				finder.find(dir, ".JAVA", verbose); //$NON-NLS-1$
			} catch (Exception e) {
				throw new InvalidInputException(Main.bind("configure.IOError", currentArg)); //$NON-NLS-1$
			}
			if (filenames != null) {
				// some source files were specified explicitly
				String results[] = finder.resultFiles;
				int length = results.length;
				System.arraycopy(
					filenames,
					0,
					(filenames = new String[length + filesCount]),
					0,
					filesCount);
				System.arraycopy(
					encodings,
					0,
					(encodings = new String[length + filesCount]),
					0,
					filesCount);
				System.arraycopy(results, 0, filenames, filesCount, length);
				for (int i = 0; i < length; i++) {
					encodings[filesCount + i] = customEncoding;
				}
				filesCount += length;
				customEncoding = null;
			} else {
				filenames = finder.resultFiles;
				filesCount = filenames.length;
				encodings = new String[filesCount];
				for (int i = 0; i < filesCount; i++) {
					encodings[i] = customEncoding;
				}
				customEncoding = null;
			}
			mode = Default;
			continue;
		}

		if (noWarn) {
			// filter options which are related to the assist component
			Object[] entries = options.entrySet().toArray();
			for (int i = 0, max = entries.length; i < max; i++) {
				Map.Entry entry = (Map.Entry) entries[i];
				if (!(entry.getKey() instanceof String))
					continue;
				if (!(entry.getValue() instanceof String))
					continue;
				if (((String) entry.getValue()).equals(CompilerOptions.WARNING)) {
					options.put((String) entry.getKey(), CompilerOptions.IGNORE);
				}
			}
		}
		/*
		 * Standalone options
		 */
		if (versionIDRequired) {
			out.println(Main.bind("configure.version", Main.bind("compiler.version"))); //$NON-NLS-1$ //$NON-NLS-2$
			out.println();
			proceed = false;
			return;
		}

		if (printUsageRequired) {
			printUsage();
			proceed = false;
			return;
		}

		if (filesCount != 0)
			System.arraycopy(
				filenames,
				0,
				(filenames = new String[filesCount]),
				0,
				filesCount);
		if (pathCount == 0) {
			String classProp = System.getProperty("DEFAULT_CLASSPATH"); //$NON-NLS-1$
			if ((classProp == null) || (classProp.length() == 0)) {
				out.println(Main.bind("configure.noClasspath")); //$NON-NLS-1$
				classProp = "."; //$NON-NLS-1$
			}
			StringTokenizer tokenizer = new StringTokenizer(classProp, File.pathSeparator);
			classpaths = new String[tokenizer.countTokens()];
			while (tokenizer.hasMoreTokens()) {
				classpaths[pathCount++] = tokenizer.nextToken();
			}
		}

		if (classpaths == null)
			classpaths = new String[0];
		System.arraycopy(
			classpaths,
			0,
			(classpaths = new String[pathCount]),
			0,
			pathCount);
		for (int i = 0, max = classpaths.length; i < max; i++) {
			File file = new File(classpaths[i]);
			if (!file.exists()) // signal missing classpath entry file
				out.println(Main.bind("configure.incorrectClasspath", classpaths[i])); //$NON-NLS-1$
		}
		if (destinationPath == null) {
			generatePackagesStructure = false;
		} else if ("none".equals(destinationPath)) { //$NON-NLS-1$
			destinationPath = null;
		}

		if (filenames == null)
			throw new InvalidInputException(Main.bind("configure.noSource")); //$NON-NLS-1$

		// check and set compliance/source/target compatibilities
		if (!didSpecifyCompliance){
				if (options.get(CompilerOptions.OPTION_Source).equals(CompilerOptions.VERSION_1_4)){
					options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_1_4);
				} else {
					options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_1_3);
				}
		}
		String compliance = (String)options.get(CompilerOptions.OPTION_Compliance);
		if (CompilerOptions.VERSION_1_4.equals(compliance)){
			
			// default 1.4 settings
			if (!didSpecifySourceLevel){
				options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_1_4);
			}
			if (!didSpecifyTarget){
				options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_1_4);
			}
		} else if (CompilerOptions.VERSION_1_3.equals(compliance)){

			// default 1.4 settings
			if (!didSpecifySourceLevel){
				options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_1_3);
			}
			if (!didSpecifyTarget){
				options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_1_1);
			}
		}
		// compliance must be 1.4 if source is 1.4
		if (options.get(CompilerOptions.OPTION_Source).equals(CompilerOptions.VERSION_1_4)
				&& !options.get(CompilerOptions.OPTION_Compliance).equals(CompilerOptions.VERSION_1_4)){ 
				throw new InvalidInputException(Main.bind("configure.incompatibleComplianceForSource14", (String)options.get(CompilerOptions.OPTION_Compliance))); //$NON-NLS-1$
		}
		
		// target must be 1.4 if source is 1.4
		if (options.get(CompilerOptions.OPTION_Source).equals(CompilerOptions.VERSION_1_4)
				&& !options.get(CompilerOptions.OPTION_TargetPlatform).equals(CompilerOptions.VERSION_1_4)){ 
				throw new InvalidInputException(Main.bind("configure.incompatibleTargetForSource14", (String)options.get(CompilerOptions.OPTION_TargetPlatform))); //$NON-NLS-1$
		}

		// target cannot be 1.4 if compliance is 1.3
		if (options.get(CompilerOptions.OPTION_TargetPlatform).equals(CompilerOptions.VERSION_1_4)
				&& !options.get(CompilerOptions.OPTION_Compliance).equals(CompilerOptions.VERSION_1_4)){ 
				throw new InvalidInputException(Main.bind("configure.incompatibleComplianceForTarget14", (String)options.get(CompilerOptions.OPTION_Compliance))); //$NON-NLS-1$
		}
		
		if (log != null) {
			try {
				out = new PrintWriter(new FileOutputStream(log, false));
			} catch (IOException e) {
				throw new InvalidInputException(Main.bind("configure.cannotOpenLog")); //$NON-NLS-1$
			}
		} else {
			showProgress = false;
		}

		if (repetitions == 0) {
			repetitions = 1;
		}
	}
	public Map getOptions() {
		return this.options;
	}
	/*
	 * Answer the component to which will be handed back compilation results from the compiler
	 */
	public ICompilerRequestor getBatchRequestor() {
		return new ICompilerRequestor() {
			int lineDelta = 0;
			public void acceptResult(CompilationResult compilationResult) {
				if (compilationResult.lineSeparatorPositions != null) {
					int unitLineCount = compilationResult.lineSeparatorPositions.length;
					lineCount += unitLineCount;
					lineDelta += unitLineCount;
					if (showProgress
						&& lineDelta > 2000) { // in -log mode, dump a dot every 2000 lines compiled
						System.out.print('.');
						lineDelta = 0;
					}
				}
				if (compilationResult.hasProblems()) {
					IProblem[] problems = compilationResult.getProblems();
					int count = problems.length;
					int localErrorCount = 0;
					for (int i = 0; i < count; i++) {
						if (problems[i] != null) {
							globalProblemsCount++;
							if (localErrorCount == 0)
								out.println("----------"); //$NON-NLS-1$
							out.print(
								globalProblemsCount
									+ ". "  //$NON-NLS-1$
									+ (problems[i].isError()
										? Main.bind("requestor.error")  //$NON-NLS-1$
										: Main.bind("requestor.warning")));  //$NON-NLS-1$
							if (problems[i].isError()) {
								globalErrorsCount++;
							} else {
								globalWarningsCount++;
							}
							out.print(" "); //$NON-NLS-1$
							out.print(
								Main.bind("requestor.in", new String(problems[i].getOriginatingFileName()))); //$NON-NLS-1$
							try {
								out.println(
									((DefaultProblem) problems[i]).errorReportSource(
										compilationResult.compilationUnit));
								out.println(problems[i].getMessage());
							} catch (Exception e) {
								out.println(
									Main.bind("requestor.notRetrieveErrorMessage", problems[i].toString())); //$NON-NLS-1$
							}
							out.println("----------"); //$NON-NLS-1$
							if (problems[i].isError())
								localErrorCount++;
						}
					};
					// exit?
					if (systemExitWhenFinished && !proceedOnError && (localErrorCount > 0)) {
						out.flush();
						System.exit(-1);
					}
				}
				outputClassFiles(compilationResult);
			}
		};
	}
	/*
	 *  Build the set of compilation source units
	 */
	public CompilationUnit[] getCompilationUnits()
		throws InvalidInputException {
		int fileCount = filenames.length;
		CompilationUnit[] units = new CompilationUnit[fileCount];
		HashtableOfObject knownFileNames = new HashtableOfObject(fileCount);

		String defaultEncoding = (String) options.get(CompilerOptions.OPTION_Encoding);
		if ("".equals(defaultEncoding)) //$NON-NLS-1$
			defaultEncoding = null; //$NON-NLS-1$

		for (int i = 0; i < fileCount; i++) {
			char[] charName = filenames[i].toCharArray();
			if (knownFileNames.get(charName) != null) {
				throw new InvalidInputException(Main.bind("unit.more", filenames[i])); //$NON-NLS-1$
			} else {
				knownFileNames.put(charName, charName);
			}
			File file = new File(filenames[i]);
			if (!file.exists())
				throw new InvalidInputException(Main.bind("unit.missing", filenames[i])); //$NON-NLS-1$
			String encoding = encodings[i];
			if (encoding == null)
				encoding = defaultEncoding;
			units[i] = new CompilationUnit(null, filenames[i], encoding);
		}
		return units;
	}
	/*
	 *  Low-level API performing the actual compilation
	 */
	public IErrorHandlingPolicy getHandlingPolicy() {

		// passes the initial set of files to the batch oracle (to avoid finding more than once the same units when case insensitive match)	
		return new IErrorHandlingPolicy() {
			public boolean stopOnFirstError() {
				return false;
			}
			public boolean proceedOnErrors() {
				return proceedOnError; // stop if there are some errors 
			}
		};
	}
	/*
	 *  Low-level API performing the actual compilation
	 */
	public FileSystem getLibraryAccess() {

		String defaultEncoding = (String) options.get(CompilerOptions.OPTION_Encoding);
		if ("".equals(defaultEncoding)) //$NON-NLS-1$
			defaultEncoding = null; //$NON-NLS-1$	
		return new FileSystem(classpaths, filenames, defaultEncoding);
	}
	/*
	 *  Low-level API performing the actual compilation
	 */
	public IProblemFactory getProblemFactory() {
		return new DefaultProblemFactory(Locale.getDefault());
	}
	/*
	 * External API
	 */

	public static void main(String[] argv) {
		new Main(new PrintWriter(System.out), true).compile(argv);
	}
	// Dump classfiles onto disk for all compilation units that where successfull.

	public void outputClassFiles(CompilationResult unitResult) {

		if (!((unitResult == null) || (unitResult.hasErrors() && !proceedOnError))) {
			Enumeration classFiles = unitResult.compiledTypes.elements();
			if (!this.generatePackagesStructure) {
				while (classFiles.hasMoreElements()) {
					this.destinationPath = extractDestinationPathFromSourceFile(unitResult);
					// retrieve the key and the corresponding classfile
					ClassFile classFile = (ClassFile) classFiles.nextElement();
					char[] filename = classFile.fileName();
					int length = filename.length;
					char[] relativeName = new char[length + 6];
					System.arraycopy(filename, 0, relativeName, 0, length);
					System.arraycopy(CLASS_FILE_EXTENSION, 0, relativeName, length, 6);
					CharOperation.replace(relativeName, '/', File.separatorChar);
					try {
						ClassFile.writeToDisk(
							generatePackagesStructure,
							destinationPath,
							new String(relativeName),
							classFile.getBytes());
					} catch (IOException e) {
						String fileName = destinationPath + new String(relativeName);
						e.printStackTrace();
						System.out.println(Main.bind("output.noClassFileCreated", fileName));  //$NON-NLS-1$
					}
					exportedClassFilesCounter++;
				}
			} else if (destinationPath != null) {
				while (classFiles.hasMoreElements()) {
					// retrieve the key and the corresponding classfile
					ClassFile classFile = (ClassFile) classFiles.nextElement();
					char[] filename = classFile.fileName();
					int length = filename.length;
					char[] relativeName = new char[length + 6];
					System.arraycopy(filename, 0, relativeName, 0, length);
					System.arraycopy(CLASS_FILE_EXTENSION, 0, relativeName, length, 6);
					CharOperation.replace(relativeName, '/', File.separatorChar);
					try {
						ClassFile.writeToDisk(
							generatePackagesStructure,
							destinationPath,
							new String(relativeName),
							classFile.getBytes());
					} catch (IOException e) {
						String fileName = destinationPath + new String(relativeName);
						e.printStackTrace();
						System.out.println(Main.bind("output.noClassFileCreated", fileName)); //$NON-NLS-1$
					}
					exportedClassFilesCounter++;
				}
			}
		}
	}
	/*
	 *  Low-level API performing the actual compilation
	 */
	public void performCompilation() throws InvalidInputException {

		INameEnvironment environment = getLibraryAccess();
		Compiler batchCompiler =
			new Compiler(
				environment,
				getHandlingPolicy(),
				getOptions(),
				getBatchRequestor(),
				getProblemFactory());
		CompilerOptions options = batchCompiler.options;

		// set the non-externally configurable options.
		options.setVerboseMode(verbose);
		options.produceReferenceInfo(produceRefInfo);
		batchCompiler.compile(getCompilationUnits());

		// cleanup
		environment.cleanup();
	}
	public void printUsage() {
		out.println(Main.bind("misc.usage", Main.bind("compiler.version"))); //$NON-NLS-1$ //$NON-NLS-2$
		out.flush();
	}

	/**
	 * Creates a NLS catalog for the given locale.
	 */
	public static void relocalize() {
		bundle = ResourceBundle.getBundle(bundleName, Locale.getDefault());
	}

	/**
	 * Lookup the message with the given ID in this catalog 
	 */
	public static String bind(String id) {
		return bind(id, (String[]) null);
	}

	/**
	 * Lookup the message with the given ID in this catalog and bind its
	 * substitution locations with the given string values.
	 */
	public static String bind(String id, String[] bindings) {
		if (id == null)
			return "No message available"; //$NON-NLS-1$
		String message = null;
		try {
			message = bundle.getString(id);
		} catch (MissingResourceException e) {
			// If we got an exception looking for the message, fail gracefully by just returning
			// the id we were looking for.  In most cases this is semi-informative so is not too bad.
			return "Missing message: " + id + " in: " + bundleName; //$NON-NLS-2$ //$NON-NLS-1$
		}
		// for compatibility with MessageFormat which eliminates double quotes in original message
		char[] messageWithNoDoubleQuotes =
			CharOperation.replace(message.toCharArray(), DOUBLE_QUOTES, SINGLE_QUOTE);
		message = new String(messageWithNoDoubleQuotes);

		if (bindings == null)
			return message;

		int length = message.length();
		int start = -1;
		int end = length;
		StringBuffer output = new StringBuffer(80);
		while (true) {
			if ((end = message.indexOf('{', start)) > -1) {
				output.append(message.substring(start + 1, end));
				if ((start = message.indexOf('}', end)) > -1) {
					int index = -1;
					try {
						index = Integer.parseInt(message.substring(end + 1, start));
						output.append(bindings[index]);
					} catch (NumberFormatException nfe) {
						output.append(message.substring(end + 1, start + 1));
					} catch (ArrayIndexOutOfBoundsException e) {
						output.append("{missing " + Integer.toString(index) + "}"); //$NON-NLS-2$ //$NON-NLS-1$
					}
				} else {
					output.append(message.substring(end, length));
					break;
				}
			} else {
				output.append(message.substring(start + 1, length));
				break;
			}
		}
		return output.toString();
	}

	/**
	 * Lookup the message with the given ID in this catalog and bind its
	 * substitution locations with the given string.
	 */
	public static String bind(String id, String binding) {
		return bind(id, new String[] { binding });
	}

	/**
	 * Lookup the message with the given ID in this catalog and bind its
	 * substitution locations with the given strings.
	 */
	public static String bind(String id, String binding1, String binding2) {
		return bind(id, new String[] { binding1, binding2 });
	}

	public String extractDestinationPathFromSourceFile(CompilationResult result) {
		ICompilationUnit compilationUnit = result.compilationUnit;
		if (compilationUnit != null) {
			char[] fileName = compilationUnit.getFileName();
			int lastIndex = CharOperation.lastIndexOf(java.io.File.separatorChar, fileName);
			if (lastIndex == -1) {
				return System.getProperty("user.dir"); //$NON-NLS-1$
			}
			return new String(CharOperation.subarray(fileName, 0, lastIndex));
		}
		return System.getProperty("user.dir"); //$NON-NLS-1$
	}

}