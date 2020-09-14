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
 *     Mik Kersten	  port to AspectJ 1.1+ code base
 * ******************************************************************/

package org.aspectj.tools.ajdoc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.*;

import org.aspectj.asm.AsmManager;
import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.Version;
import org.aspectj.util.FileUtil;

/**
 * This is an old implementation of ajdoc that does not use an OO style. However, it does the job, and should serve to evolve a
 * lightweight ajdoc implementation until we can make a properly extended javadoc implementation.
 * 
 * @author Mik Kersten
 */
public class Main implements Config {

	private static final String FAIL_MESSAGE = "> compile failed, exiting ajdoc";

	/** Command line options. */
	static Vector<String> options;

	/** Options to pass to ajc. */
	static Vector<String> ajcOptions;

	/** All of the files to be processed by ajdoc. */
	static Vector<String> filenames;

	/** List of files to pass to javadoc. */
	static Vector<String> fileList;

	/** List of packages to pass to javadoc. */
	static Vector<String> packageList;

	/** Default to package visiblity. */
	static String docModifier = "package";

	static Vector<String> sourcepath;

	static boolean verboseMode = false;
	static boolean packageMode = false;
	static boolean authorStandardDocletSwitch = false;
	static boolean versionStandardDocletSwitch = false;
	static File rootDir = null;
	static Hashtable declIDTable = new Hashtable();
	static String docDir = ".";

	private static boolean deleteTempFilesOnExit = true;

	private static boolean aborted = false;
	private static IMessage[] errors;
	private static boolean shownAjdocUsageMessage = false;

	// creating a local variable to enable us to create the ajdocworkingdir
	// in a local sandbox during testing
	private static String outputWorkingDir = Config.WORKING_DIR;

	public static void clearState() {
		options = new Vector<>();
		ajcOptions = new Vector<>();
		filenames = new Vector<>();
		fileList = new Vector<>();
		packageList = new Vector<>();
		docModifier = "package";
		sourcepath = new Vector<>();
		verboseMode = false;
		packageMode = false;
		rootDir = null;
		declIDTable = new Hashtable();
		docDir = ".";
		aborted = false;
		deleteTempFilesOnExit = true;
	}

	public static void main(String[] args) {
		clearState();

		// STEP 1: parse the command line and do other global setup
		sourcepath.addElement("."); // add the current directory to the classapth
		parseCommandLine(args);
		rootDir = getRootDir();
		File[] inputFiles = new File[filenames.size()];
		File[] signatureFiles = new File[filenames.size()];
		try {
			// create the workingdir if it doesn't exist
			if (!(new File(outputWorkingDir).isDirectory())) {
				File dir = new File(outputWorkingDir);
				dir.mkdir();
				if (deleteTempFilesOnExit)
					dir.deleteOnExit();
			}

			for (int i = 0; i < filenames.size(); i++) {
				inputFiles[i] = new File(filenames.elementAt(i));
			}

			// PHASE 0: call ajc
			AsmManager model = callAjc(inputFiles);
			if (CompilerWrapper.hasErrors()) {
				System.out.println(FAIL_MESSAGE);
				aborted = true;
				errors = CompilerWrapper.getErrors();
				return;
			}

			for (int ii = 0; ii < filenames.size(); ii++) {
				signatureFiles[ii] = createSignatureFile(model, inputFiles[ii]);
			}

			// PHASE 1: generate Signature files (Java with DeclIDs and no bodies).
			System.out.println("> Building signature files...");
			try {
				StubFileGenerator.doFiles(model, declIDTable, inputFiles, signatureFiles);
				// Copy package.html and related files over
				packageHTML(model, inputFiles);
			} catch (DocException d) {
				System.err.println(d.getMessage());
				return;
			}

			// PHASE 2: let Javadoc generate HTML (with DeclIDs)
			callJavadoc(signatureFiles);

			// PHASE 3: add AspectDoc specific stuff to the HTML (and remove the DeclIDS).
			decorateHtmlFiles(model, inputFiles);
			System.out.println("> Finished.");
		} catch (Throwable e) {
			handleInternalError(e);
			exit(-2);
		}
	}

	/**
	 * Method to copy over any package.html files that may be part of the documentation so that javadoc generates the
	 * package-summary properly.
	 */
	private static void packageHTML(AsmManager model, File[] inputFiles) throws IOException {
		List<String> dirList = new ArrayList<>();
		for (File inputFile : inputFiles) {
			String packageName = StructureUtil.getPackageDeclarationFromFile(model, inputFile);
			// Only copy the package.html file once.
			if (dirList.contains(packageName))
				continue;

			// Check to see if there exist a package.html file for this package.
			String dir = inputFile.getAbsolutePath().substring(0, inputFile.getAbsolutePath().lastIndexOf(File.separator));
			File input = new File(dir + Config.DIR_SEP_CHAR + "package.html");
			File inDir = new File(dir + Config.DIR_SEP_CHAR + "doc-files");
			// If it does not exist lets go to the next package.
			if (!input.exists()) {
				dirList.add(packageName);
				continue;
			}

			String filename = "";
			String docFiles = "";
			if (packageName != null) {
				String pathName = outputWorkingDir + File.separator + packageName.replace('.', File.separatorChar);
				File packageDir = new File(pathName);
				if (!packageDir.exists()) {
					dirList.add(packageName);
					continue;
				}
				packageName = packageName.replace('.', '/'); // !!!
				filename = outputWorkingDir + Config.DIR_SEP_CHAR + packageName + Config.DIR_SEP_CHAR + "package.html";
				docFiles = rootDir.getAbsolutePath() + Config.DIR_SEP_CHAR + packageName + Config.DIR_SEP_CHAR + "doc-files";
			} else {
				filename = outputWorkingDir + Config.DIR_SEP_CHAR + "package.html";
				docFiles = rootDir.getAbsolutePath() + Config.DIR_SEP_CHAR + "doc-files";
			}

			File output = new File(filename);
			FileUtil.copyFile(input, output);// Copy package.html
			// javadoc doesn't do anything with the doc-files folder so
			// we'll just copy it directly to the document location.
			if (!inDir.exists())
				continue;
			File outDir = new File(docFiles);
			System.out.println("> Copying folder " + outDir);
			FileUtil.copyFile(inDir, outDir);// Copy doc-files folder if it exist
		}
	}

	private static AsmManager callAjc(File[] inputFiles) {
		ajcOptions.addElement("-noExit");
		ajcOptions.addElement("-XjavadocsInModel"); // TODO: wrong option to force model gen
		ajcOptions.addElement("-d");
		ajcOptions.addElement(rootDir.getAbsolutePath());
		String[] argsToCompiler = new String[ajcOptions.size() + inputFiles.length];
		int i = 0;
		for (; i < ajcOptions.size(); i++) {
			argsToCompiler[i] = ajcOptions.elementAt(i);
		}
		for (File inputFile : inputFiles) {
			argsToCompiler[i] = inputFile.getAbsolutePath();
			// System.out.println(">> file to ajc: " + inputFiles[j].getAbsolutePath());
			i++;
		}
		System.out.println("> Calling ajc...");
		return CompilerWrapper.executeMain(argsToCompiler);
	}

	private static void callJavadoc(File[] signatureFiles) throws IOException {
		System.out.println("> Calling javadoc...");
		String[] javadocargs = null;

		List<String> files = new ArrayList<>();
		if (packageMode) {
			int numExtraArgs = 2;
			if (authorStandardDocletSwitch)
				numExtraArgs++;
			if (versionStandardDocletSwitch)
				numExtraArgs++;
			javadocargs = new String[numExtraArgs + options.size() + packageList.size() + fileList.size()];
			javadocargs[0] = "-sourcepath";
			javadocargs[1] = outputWorkingDir;
			int argIndex = 2;
			if (authorStandardDocletSwitch) {
				javadocargs[argIndex] = "-author";
				argIndex++;
			}
			if (versionStandardDocletSwitch) {
				javadocargs[argIndex] = "-version";
			}
			// javadocargs[1] = getSourcepathAsString();
			for (int k = 0; k < options.size(); k++) {
				javadocargs[numExtraArgs + k] = options.elementAt(k);
			}
			for (int k = 0; k < packageList.size(); k++) {
				javadocargs[numExtraArgs + options.size() + k] = packageList.elementAt(k);
			}
			for (int k = 0; k < fileList.size(); k++) {
				javadocargs[numExtraArgs + options.size() + packageList.size() + k] = fileList.elementAt(k);
			}
			options = new Vector<>();
			Collections.addAll(options, javadocargs);
		} else {
			javadocargs = new String[options.size() + signatureFiles.length];
			for (int k = 0; k < options.size(); k++) {
				javadocargs[k] = options.elementAt(k);
			}
			for (int k = 0; k < signatureFiles.length; k++) {
				javadocargs[options.size() + k] = StructureUtil.translateAjPathName(signatureFiles[k].getCanonicalPath());
			}
			for (File signatureFile : signatureFiles) {
				files.add(StructureUtil.translateAjPathName(signatureFile.getCanonicalPath()));
			}
		}
		JavadocRunner.callJavadocViaToolProvider(options, files);
	}

	/**
	 * We start with the known HTML files (the ones that correspond directly to the input files.) As we go along, we may learn that
	 * Javadoc split one .java file into multiple .html files to handle inner classes or local classes. The html file decorator
	 * picks that up.
	 */
	private static void decorateHtmlFiles(AsmManager model, File[] inputFiles) throws IOException {
		System.out.println("> Decorating html files...");
		HtmlDecorator.decorateHTMLFromInputFiles(model, declIDTable, rootDir, inputFiles, docModifier);

		System.out.println("> Removing generated tags...");
		removeDeclIDsFromFile("index-all.html", true);
		removeDeclIDsFromFile("serialized-form.html", true);
		if (packageList.size() > 0) {
			for (int p = 0; p < packageList.size(); p++) {
				removeDeclIDsFromFile(packageList.elementAt(p).replace('.', '/') + Config.DIR_SEP_CHAR
						+ "package-summary.html", true);
			}
		} else {
			File[] files = rootDir.listFiles();
			if (files == null) {
				System.err.println("Destination directory is not a directory: " + rootDir.toString());
				return;
			}
			files = FileUtil.listFiles(rootDir, new FileFilter() {
				@Override
				public boolean accept(File f) {
					return f.getName().equals("package-summary.html");
				}
			});
			for (File file : files) {
				removeDeclIDsFromFile(file.getAbsolutePath(), false);
			}
		}
	}

	private static void removeDeclIDsFromFile(String filename, boolean relativePath) {
		// Remove the decl ids from "index-all.html"
		File indexFile;
		if (relativePath) {
			indexFile = new File(docDir + Config.DIR_SEP_CHAR + filename);
		} else {
			indexFile = new File(filename);
		}
		try {
			if (indexFile.exists()) {
				BufferedReader indexFileReader = new BufferedReader(new FileReader(indexFile));
				// StringBuffer greatly reduces the time it takes to remove generated tags
				StringBuffer indexFileBuffer = new StringBuffer((int) indexFile.length());
				String line = indexFileReader.readLine();
				while (line != null) {
					int indexStart = line.indexOf(Config.DECL_ID_STRING);
					int indexEnd = line.indexOf(Config.DECL_ID_TERMINATOR);
					if (indexStart != -1 && indexEnd != -1) {
						line = line.substring(0, indexStart) + line.substring(indexEnd + Config.DECL_ID_TERMINATOR.length());
					}
					indexFileBuffer.append(line);
					line = indexFileReader.readLine();
				}
				FileOutputStream fos = new FileOutputStream(indexFile);
				fos.write(indexFileBuffer.toString().getBytes());

				indexFileReader.close();
				fos.close();
			}
		} catch (IOException ioe) {
			// be siltent
		}
	}

	static Vector<String> getSourcePath() {
		Vector<String> sourcePath = new Vector<>();
		boolean found = false;
		for (int i = 0; i < options.size(); i++) {
			String currOption = options.elementAt(i);
			if (found && !currOption.startsWith("-")) {
				sourcePath.add(currOption);
			}
			if (currOption.equals("-sourcepath")) {
				found = true;
			}
		}
		return sourcePath;
	}

	static File getRootDir() {
		File rootDir = new File(".");
		for (int i = 0; i < options.size(); i++) {
			if (options.elementAt(i).equals("-d")) {
				rootDir = new File(options.elementAt(i + 1));
				if (!rootDir.exists()) {
					rootDir.mkdir();
					// System.out.println( "Destination directory not found: " +
					// (String)options.elementAt(i+1) );
					// System.exit( -1 );
				}
			}
		}
		return rootDir;
	}

	static File createSignatureFile(AsmManager model, File inputFile) throws IOException {
		String packageName = StructureUtil.getPackageDeclarationFromFile(model, inputFile);

		String filename = "";
		if (packageName != null) {
			String pathName = outputWorkingDir + '/' + packageName.replace('.', '/');
			File packageDir = new File(pathName);
			if (!packageDir.exists()) {
				packageDir.mkdirs();
				if (deleteTempFilesOnExit)
					packageDir.deleteOnExit();
			}
			// verifyPackageDirExists(packageName, null);
			packageName = packageName.replace('.', '/'); // !!!
			filename = outputWorkingDir + Config.DIR_SEP_CHAR + packageName + Config.DIR_SEP_CHAR + inputFile.getName();
		} else {
			filename = outputWorkingDir + Config.DIR_SEP_CHAR + inputFile.getName();
		}
		File signatureFile = new File(filename);
		if (deleteTempFilesOnExit)
			signatureFile.deleteOnExit();
		return signatureFile;
	}

	// static void verifyPackageDirExists( String packageName, String offset ) {
	// System.err.println(">>> name: " + packageName + ", offset: " + offset);
	// if ( packageName.indexOf( "." ) != -1 ) {
	// File tempFile = new File("c:/aspectj-test/d1/d2/d3");
	// tempFile.mkdirs();
	// String currPkgDir = packageName.substring( 0, packageName.indexOf( "." ) );
	// String remainingPkg = packageName.substring( packageName.indexOf( "." )+1 );
	// String filePath = null;
	// if ( offset != null ) {
	// filePath = Config.WORKING_DIR + Config.DIR_SEP_CHAR +
	// offset + Config.DIR_SEP_CHAR + currPkgDir ;
	// }
	// else {
	// filePath = Config.WORKING_DIR + Config.DIR_SEP_CHAR + currPkgDir;
	// }
	// File packageDir = new File( filePath );
	// if ( !packageDir.exists() ) {
	// packageDir.mkdir();
	// if (deleteTempFilesOnExit) packageDir.deleteOnExit();
	// }
	// if ( remainingPkg != "" ) {
	// verifyPackageDirExists( remainingPkg, currPkgDir );
	// }
	// }
	// else {
	// String filePath = null;
	// if ( offset != null ) {
	// filePath = Config.WORKING_DIR + Config.DIR_SEP_CHAR + offset + Config.DIR_SEP_CHAR + packageName;
	// }
	// else {
	// filePath = Config.WORKING_DIR + Config.DIR_SEP_CHAR + packageName;
	// }
	// File packageDir = new File( filePath );
	// if ( !packageDir.exists() ) {
	// packageDir.mkdir();
	// if (deleteTempFilesOnExit) packageDir.deleteOnExit();
	// }
	// }
	// }

	/**
	 * Can read Eclipse-generated single-line arg
	 */
	static void parseCommandLine(String[] args) {
		if (args.length == 0) {
			displayHelpAndExit(null);
		} else if (args.length == 1 && args[0].startsWith("@")) {
			String argFile = args[0].substring(1);
			System.out.println("> Using arg file: " + argFile);
			BufferedReader br;
			try {
				br = new BufferedReader(new FileReader(argFile));
				String line = "";
				line = br.readLine();
				StringTokenizer st = new StringTokenizer(line, " ");
				List<String> argList = new ArrayList<>();
				while (st.hasMoreElements()) {
					argList.add(st.nextToken());
				}
				// System.err.println(argList);
				args = new String[argList.size()];
				int counter = 0;
				for (String s : argList) {
					args[counter] = s;
					counter++;
				}
			} catch (FileNotFoundException e) {
				System.err.println("> could not read arg file: " + argFile);
				e.printStackTrace();
			} catch (IOException ioe) {
				System.err.println("> could not read arg file: " + argFile);
				ioe.printStackTrace();
			}
		}
		List<String> vargs = new LinkedList<>(Arrays.asList(args));
		vargs.add("-Xset:minimalModel=false");
		parseArgs(vargs, new File(".")); // !!!

		if (filenames.size() == 0) {
			displayHelpAndExit("ajdoc: No packages or classes specified");
		}
	}

	static void setSourcepath(String arg) {
		sourcepath.clear();
		arg = arg + File.pathSeparator; // makes things easier for ourselves
		StringTokenizer tokenizer = new StringTokenizer(arg, File.pathSeparator);
		while (tokenizer.hasMoreElements()) {
			sourcepath.addElement(tokenizer.nextToken());
		}
	}

	static String getSourcepathAsString() {
		String cPath = "";
		for (int i = 0; i < sourcepath.size(); i++) {
			cPath += sourcepath.elementAt(i) + Config.DIR_SEP_CHAR + outputWorkingDir;
			if (i != sourcepath.size() - 1) {
				cPath += File.pathSeparator;
			}
		}
		return cPath;
	}

	static void parseArgs(List vargs, File currentWorkingDir) {
		boolean addNextAsOption = false;
		boolean addNextAsArgFile = false;
		boolean addNextToAJCOptions = false;
		boolean addNextAsDocDir = false;
		boolean addNextAsClasspath = false;
		boolean ignoreArg = false; // used for discrepancy betwen class/sourcepath in ajc/javadoc
		boolean addNextAsSourcePath = false;
		if (vargs.size() == 0) {
			displayHelpAndExit(null);
		}
		for (Object varg : vargs) {
			String arg = (String) varg;
			ignoreArg = false;
			if (addNextAsDocDir) {
				docDir = arg;
				addNextAsDocDir = false;
			}
			if (addNextAsClasspath) {
				addNextAsClasspath = false;
			}
			if (addNextAsSourcePath) {
				setSourcepath(arg);
				addNextAsSourcePath = false;
				ignoreArg = true;
			}

			if (arg.startsWith("@")) {
				expandAtSignFile(arg.substring(1), currentWorkingDir);
			} else if (arg.equals("-argfile")) {
				addNextAsArgFile = true;
			} else if (addNextAsArgFile) {
				expandAtSignFile(arg, currentWorkingDir);
				addNextAsArgFile = false;
			} else if (arg.equals("-d")) {
				addNextAsOption = true;
				options.addElement(arg);
				addNextAsDocDir = true;
			} else if (arg.equals("-bootclasspath")) {
				addNextAsOption = true;
				addNextToAJCOptions = true;
				options.addElement(arg);
				ajcOptions.addElement(arg);
			} else if (arg.equals("-source")) {
				addNextAsOption = true;
				addNextToAJCOptions = true;
				addNextAsClasspath = true;
				options.addElement(arg);
				ajcOptions.addElement(arg);
			} else if (arg.equals("-classpath")) {
				addNextAsOption = true;
				addNextToAJCOptions = true;
				addNextAsClasspath = true;
				options.addElement(arg);
				ajcOptions.addElement(arg);
			} else if (arg.equals("-encoding")) {
				addNextAsOption = true;
				addNextToAJCOptions = false;
				options.addElement(arg);
			} else if (arg.equals("-docencoding")) {
				addNextAsOption = true;
				addNextToAJCOptions = false;
				options.addElement(arg);
			} else if (arg.equals("-charset")) {
				addNextAsOption = true;
				addNextToAJCOptions = false;
				options.addElement(arg);
			} else if (arg.equals("-sourcepath")) {
				addNextAsSourcePath = true;
				// options.addElement( arg );
				// ajcOptions.addElement( arg );
			} else if (arg.equals("-link")) {
				addNextAsOption = true;
				options.addElement(arg);
			} else if (arg.equals("-bottom")) {
				addNextAsOption = true;
				options.addElement(arg);
			} else if (arg.equals("-windowtitle")) {
				addNextAsOption = true;
				options.addElement(arg);
			} else if (arg.equals("-XajdocDebug")) {
				deleteTempFilesOnExit = false;
			} else if (arg.equals("-use")) {
				System.out.println("> Ignoring unsupported option: -use");
			} else if (arg.equals("-splitindex")) {
				// passed to javadoc
			} else if (arg.startsWith("-") || addNextAsOption || addNextToAJCOptions) {
				if (arg.equals("-private")) {
					docModifier = "private";
				} else if (arg.equals("-package")) {
					docModifier = "package";
				} else if (arg.equals("-protected")) {
					docModifier = "protected";
				} else if (arg.equals("-public")) {
					docModifier = "public";
				} else if (arg.equals("-verbose")) {
					verboseMode = true;
				} else if (arg.equals("-author")) {
					authorStandardDocletSwitch = true;
				} else if (arg.equals("-version")) {
					versionStandardDocletSwitch = true;
				} else if (arg.equals("-v")) {
					System.out.println(getVersion());
					exit(0);
				} else if (arg.equals("-help")) {
					displayHelpAndExit(null);
				} else if (arg.equals("-doclet") || arg.equals("-docletpath")) {
					System.out.println("The doclet and docletpath options are not currently supported    \n"
							+ "since ajdoc makes assumptions about the behavior of the standard \n"
							+ "doclet. If you would find this option useful please email us at: \n"
							+ "                                                                 \n"
							+ "       aspectj-dev@eclipse.org                            \n"
							+ "                                                                 \n");
					exit(0);
				} else if (arg.equals("-nonavbar") || arg.equals("-noindex")) {
					// pass through
					// System.err.println("> ignoring unsupported option: " + arg);
				} else if (addNextToAJCOptions || addNextAsOption) {
					// deal with these two options together even though effectively
					// just falling through if addNextAsOption is true. Otherwise
					// will have to ensure check "addNextToAJCOptions" before
					// "addNextAsOption" so as the options are added to the
					// correct lists.
					if (addNextToAJCOptions) {
						ajcOptions.addElement(arg);
						if (!arg.startsWith("-")) {
							addNextToAJCOptions = false;
						}
						if (!addNextAsOption) {
							continue;
						}
					}
				} else if (arg.startsWith("-")) {
					ajcOptions.addElement(arg);
					addNextToAJCOptions = true;
					continue;
				} else {
					System.err.println("> unrecognized argument: " + arg);
					displayHelpAndExit(null);
				}
				options.addElement(arg);
				addNextAsOption = false;
			} else {
				// check if this is a file or a package
				// System.err.println(">>>>>>>> " + );
				// String entryName = arg.substring(arg.lastIndexOf(File.separator)+1);
				if (FileUtil.hasSourceSuffix(arg) || arg.endsWith(".lst") && arg != null) {
					File f = new File(arg);
					if (f.isAbsolute()) {
						filenames.addElement(arg);
					} else {
						filenames.addElement(currentWorkingDir + Config.DIR_SEP_CHAR + arg);
					}
					fileList.addElement(arg);
				}

				// PACKAGE MODE STUFF
				else if (!ignoreArg) {

					packageMode = true;
					packageList.addElement(arg);
					arg = arg.replace('.', '/'); // !!!

					// do this for every item in the classpath
					for (int c = 0; c < sourcepath.size(); c++) {
						String path = sourcepath.elementAt(c) + Config.DIR_SEP_CHAR + arg;
						File pkg = new File(path);
						if (pkg.isDirectory()) {
							String[] files = pkg.list(new FilenameFilter() {
								@Override
								public boolean accept(File dir, String name) {
									int index1 = name.lastIndexOf(".");
									int index2 = name.length();
									if ((index1 >= 0 && index2 >= 0)
											&& (name.substring(index1, index2).equals(".java") || name.substring(index1, index2)
											.equals(".aj"))) {
										return true;
									} else {
										return false;
									}
								}
							});
							for (String file : files) {
								filenames.addElement(sourcepath.elementAt(c) + Config.DIR_SEP_CHAR + arg
										+ Config.DIR_SEP_CHAR + file);
							}
						} else if (c == sourcepath.size()) { // last element on classpath
							System.out.println("ajdoc: No package, class, or source file " + "found named " + arg + ".");
						} else {
							// didn't find it on that element of the classpath but that's ok
						}
					}
				}
			}
		}
		// set the default visibility as an option to javadoc option
		if (!options.contains("-private") && !options.contains("-package") && !options.contains("-protected")
				&& !options.contains("-public")) {
			options.addElement("-package");
		}
	}

	static void expandAtSignFile(String filename, File currentWorkingDir) {
		List<String> result = new LinkedList<>();

		File atFile = qualifiedFile(filename, currentWorkingDir);
		String atFileParent = atFile.getParent();
		File myWorkingDir = null;
		if (atFileParent != null)
			myWorkingDir = new File(atFileParent);

		try {
			BufferedReader stream = new BufferedReader(new FileReader(atFile));
			String line = null;
			while ((line = stream.readLine()) != null) {
				// strip out any comments of the form # to end of line
				int commentStart = line.indexOf("//");
				if (commentStart != -1) {
					line = line.substring(0, commentStart);
				}

				// remove extra whitespace that might have crept in
				line = line.trim();
				// ignore blank lines
				if (line.length() == 0)
					continue;
				result.add(line);
			}
			stream.close();
		} catch (IOException e) {
			System.err.println("Error while reading the @ file " + atFile.getPath() + ".\n" + e);
			System.exit(-1);
		}

		parseArgs(result, myWorkingDir);
	}

	static File qualifiedFile(String name, File currentWorkingDir) {
		name = name.replace('/', File.separatorChar);
		File file = new File(name);
		if (!file.isAbsolute() && currentWorkingDir != null) {
			file = new File(currentWorkingDir, name);
		}
		return file;
	}

	static void displayHelpAndExit(String message) {
		shownAjdocUsageMessage = true;
		if (message != null) {
			System.err.println(message);
			System.err.println();
			System.err.println(Config.USAGE);
		} else {
			System.out.println(Config.USAGE);
			exit(0);
		}
	}

	static protected void exit(int value) {
		System.out.flush();
		System.err.flush();
		System.exit(value);
	}

	/* This section of code handles errors that occur during compilation */
	static final String internalErrorMessage = "                                                                  \n"
			+ "If this has not already been logged as a bug raised please raise  \n"
			+ "a new AspectJ bug at https://bugs.eclipse.org/bugs including the  \n"
			+ "text below. To make the bug a priority, please also include a test\n"
			+ "program that can reproduce this problem.\n ";

	static public void handleInternalError(Throwable uncaughtThrowable) {
		System.err.println("An internal error occured in ajdoc");
		System.err.println(internalErrorMessage);
		System.err.println(uncaughtThrowable.toString());
		uncaughtThrowable.printStackTrace();
		System.err.println();
	}

	static String getVersion() {
		return "ajdoc version " + Version.getText();
	}

	public static boolean hasAborted() {
		return aborted;
	}

	public static IMessage[] getErrors() {
		return errors;
	}

	public static boolean hasShownAjdocUsageMessage() {
		return shownAjdocUsageMessage;
	}

	/**
	 * Sets the output working dir to be &lt;fullyQualifiedOutputDir&gt;\ajdocworkingdir. Useful in testing to redirect the ajdocworkingdir
	 * to the sandbox
	 */
	public static void setOutputWorkingDir(String fullyQulifiedOutputDir) {
		if (fullyQulifiedOutputDir == null) {
			resetOutputWorkingDir();
		} else {
			outputWorkingDir = fullyQulifiedOutputDir + File.separatorChar + Config.WORKING_DIR;
		}
	}

	/**
	 * Resets the output working dir to be the default which is &lt;the current directory&gt;\ajdocworkingdir
	 */
	public static void resetOutputWorkingDir() {
		outputWorkingDir = Config.WORKING_DIR;
	}
}
