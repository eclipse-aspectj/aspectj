/********************************************************************
 * Copyright (c) 2007 Contributors. All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://eclipse.org/legal/epl-v10.html
 *
 * Contributors: IBM Corporation - initial API and implementation
 * 				 Helen Hawkins   - initial version (bug 148190)
 *******************************************************************/
package org.aspectj.ajde.core.internal;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.aspectj.ajde.core.AjCompiler;
import org.aspectj.ajde.core.ICompilerConfiguration;
import org.aspectj.ajde.core.IOutputLocationManager;
import org.aspectj.ajdt.ajc.AjdtCommand;
import org.aspectj.ajdt.ajc.BuildArgParser;
import org.aspectj.ajdt.ajc.ConfigParser;
import org.aspectj.ajdt.internal.core.builder.AjBuildConfig;
import org.aspectj.ajdt.internal.core.builder.AjBuildManager;
import org.aspectj.ajdt.internal.core.builder.AjState;
import org.aspectj.ajdt.internal.core.builder.IncrementalStateManager;
import org.aspectj.asm.AsmManager;
import org.aspectj.bridge.AbortException;
import org.aspectj.bridge.CountingMessageHandler;
import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.IMessageHandler;
import org.aspectj.bridge.ISourceLocation;
import org.aspectj.bridge.Message;
import org.aspectj.bridge.SourceLocation;
import org.aspectj.bridge.context.CompilationAndWeavingContext;
import org.aspectj.org.eclipse.jdt.internal.compiler.batch.FileSystem.Classpath;
import org.aspectj.org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.aspectj.util.LangUtil;

/**
 * Build Manager which drives the build for a given AjCompiler. Tools call build on the AjCompiler which drives this.
 */
public class AjdeCoreBuildManager {

	private final AjCompiler compiler;
	private AjdeCoreBuildNotifierAdapter buildEventNotifier = null;
	private final AjBuildManager ajBuildManager;
	private final IMessageHandler msgHandlerAdapter;

	public AjdeCoreBuildManager(AjCompiler compiler) {
		this.compiler = compiler;
		this.msgHandlerAdapter = new AjdeCoreMessageHandlerAdapter(compiler.getMessageHandler());
		this.ajBuildManager = new AjBuildManager(msgHandlerAdapter);
		this.ajBuildManager.environmentSupportsIncrementalCompilation(true);

		// this static information needs to be set to ensure
		// incremental compilation works correctly
		IncrementalStateManager.recordIncrementalStates = true;
		IncrementalStateManager.debugIncrementalStates = false;
		AsmManager.attemptIncrementalModelRepairs = true;
	}

	public AjBuildManager getAjBuildManager() {
		return ajBuildManager;
	}

	/**
	 * Execute a full or incremental build
	 *
	 * @param fullBuild true if requesting a full build, false if requesting to try an incremental build
	 */
	public void performBuild(boolean fullBuild) {

		// If an incremental build is requested, check that we can
		if (!fullBuild) {
			AjState existingState = IncrementalStateManager.retrieveStateFor(compiler.getId());
			if (existingState == null || existingState.getBuildConfig() == null
					|| ajBuildManager.getState().getBuildConfig() == null) {
				// No existing state so we must do a full build
				fullBuild = true;
			} else {
				AsmManager.setLastActiveStructureModel(existingState.getStructureModel());
				// AsmManager.getDefault().setRelationshipMap(existingState.getRelationshipMap());
				// AsmManager.getDefault().setHierarchy(existingState.getStructureModel());
			}
		}
		try {
			reportProgressBegin();

			// record the options passed to the compiler if INFO turned on
			if (!msgHandlerAdapter.isIgnoring(IMessage.INFO)) {
				handleMessage(new Message(getFormattedOptionsString(), IMessage.INFO, null, null));
			}

			CompilationAndWeavingContext.reset();

			if (fullBuild) { // FULL BUILD
				AjBuildConfig buildConfig = generateAjBuildConfig();
				if (buildConfig == null) {
					return;
				}
				ajBuildManager.batchBuild(buildConfig, msgHandlerAdapter);
			} else { // INCREMENTAL BUILD
				// Only rebuild the config object if the configuration has changed
				AjBuildConfig buildConfig = null;
				ICompilerConfiguration compilerConfig = compiler.getCompilerConfiguration();
				int changes = compilerConfig.getConfigurationChanges();
				if (changes != ICompilerConfiguration.NO_CHANGES) {

					// What configuration changes can we cope with? And besides just repairing the config object
					// what does it mean for any existing state that we have?

					buildConfig = generateAjBuildConfig();
					if (buildConfig == null) {
						return;
					}
				} else {
					buildConfig = ajBuildManager.getState().getBuildConfig();
					buildConfig.setChanged(changes); // pass it through for the state to use it when making decisions
					buildConfig.setModifiedFiles(compilerConfig.getProjectSourceFilesChanged());
					buildConfig.setClasspathElementsWithModifiedContents(compilerConfig.getClasspathElementsWithModifiedContents());
					compilerConfig.configurationRead();
				}
				ajBuildManager.incrementalBuild(buildConfig, msgHandlerAdapter);
			}
			IncrementalStateManager.recordSuccessfulBuild(compiler.getId(), ajBuildManager.getState());

		} catch (ConfigParser.ParseException pe) {
			handleMessage(new Message("Config file entry invalid, file: " + pe.getFile().getPath() + ", line number: "
					+ pe.getLine(), IMessage.WARNING, null, null));
		} catch (AbortException e) {
			final IMessage message = e.getIMessage();
			if (message == null) {
				handleMessage(new Message(LangUtil.unqualifiedClassName(e) + " thrown: " + e.getMessage(), IMessage.ERROR, e, null));
			} else {
				handleMessage(new Message(message.getMessage() + "\n" + CompilationAndWeavingContext.getCurrentContext(),
						IMessage.ERROR, e, null));
			}
		} catch (Throwable t) {
			handleMessage(new Message("Compile error: " + LangUtil.unqualifiedClassName(t) + " thrown: " + "" + t.getMessage(),
					IMessage.ABORT, t, null));
		} finally {
			compiler.getBuildProgressMonitor().finish(ajBuildManager.wasFullBuild());
		}
	}

	/**
	 * Starts the various notifiers which are interested in the build progress
	 */
	private void reportProgressBegin() {
		compiler.getBuildProgressMonitor().begin();
		buildEventNotifier = new AjdeCoreBuildNotifierAdapter(compiler.getBuildProgressMonitor());
		ajBuildManager.setProgressListener(buildEventNotifier);
	}

	private String getFormattedOptionsString() {
		ICompilerConfiguration compilerConfig = compiler.getCompilerConfiguration();
		return "Building with settings: " + "\n-> output paths: "
		+ formatCollection(compilerConfig.getOutputLocationManager().getAllOutputLocations()) + "\n-> classpath: "
		+ compilerConfig.getClasspath() + "\n-> -inpath " + formatCollection(compilerConfig.getInpath()) + "\n-> -outjar "
		+ formatOptionalString(compilerConfig.getOutJar()) + "\n-> -aspectpath "
		+ formatCollection(compilerConfig.getAspectPath()) + "\n-> -sourcePathResources "
		+ formatMap(compilerConfig.getSourcePathResources()) + "\n-> non-standard options: "
		+ compilerConfig.getNonStandardOptions() + "\n-> javaoptions:" + formatMap(compilerConfig.getJavaOptionsMap());
	}

	private String formatCollection(Collection<?> options) {
		if (options == null) {
			return "<default>";
		}
		if (options.isEmpty()) {
			return "none";
		}

		StringBuffer formattedOptions = new StringBuffer();
		for (Object option : options) {
			String o = option.toString();
			if (formattedOptions.length() > 0) {
				formattedOptions.append(", ");
			}
			formattedOptions.append(o);
		}
		return formattedOptions.toString();
	}

	private String formatMap(Map<String,? extends Object> options) {
		if (options == null) {
			return "<default>";
		}
		if (options.isEmpty()) {
			return "none";
		}

		return options.toString();
	}

	private String formatOptionalString(String s) {
		if (s == null) {
			return "";
		} else {
			return s;
		}
	}

	/**
	 * Generate a new AjBuildConfig from the compiler configuration associated with this AjdeCoreBuildManager or from a
	 * configuration file.
	 *
	 * @return null if invalid configuration, corresponding AjBuildConfig otherwise
	 */
	public AjBuildConfig generateAjBuildConfig() {
		File configFile = new File(compiler.getId());
		ICompilerConfiguration compilerConfig = compiler.getCompilerConfiguration();
		CountingMessageHandler handler = CountingMessageHandler.makeCountingMessageHandler(msgHandlerAdapter);

		String[] args = null;
		// Retrieve the set of files from either an arg file (@filename) or the compiler configuration
		if (configFile.exists() && configFile.isFile()) {
			args = new String[] { "@" + configFile.getAbsolutePath() };
		} else {
			List<String> projectSourceFiles = compilerConfig.getProjectSourceFiles();
			if (projectSourceFiles == null) {
				return null;
			}
			List<String> l = new ArrayList<>(projectSourceFiles);
			// If the processor options are specified build the command line options for the JDT compiler to see
			String processor = compilerConfig.getProcessor();
			if (processor != null && processor.length() != 0) {
				l.add("-processor");
				l.add(processor);
			}
			String processorPath = compilerConfig.getProcessorPath();
			if (processorPath != null && processorPath.length() != 0) {
				l.add("-processorpath");
				l.add(processorPath);
			}
			if (compilerConfig.getOutputLocationManager() != null &&
					compilerConfig.getOutputLocationManager().getDefaultOutputLocation() != null) {
				l.add("-d");
				l.add(compilerConfig.getOutputLocationManager().getDefaultOutputLocation().toString());
			}
			List<String> xmlfiles = compilerConfig.getProjectXmlConfigFiles();
			if (xmlfiles != null && !xmlfiles.isEmpty()) {
				args = new String[l.size() + xmlfiles.size() + 1];
				// TODO speedup
				int p = 0;
				for (String s : l) {
					args[p++] = s;
				}
				for (String xmlfile : xmlfiles) {
					args[p++] = xmlfile;
				}
				args[p++] = "-xmlConfigured";
			} else {
				args = l.toArray(new String[0]);
			}
		}

		BuildArgParser parser = new BuildArgParser(handler);

		AjBuildConfig config = new AjBuildConfig(parser);

		parser.populateBuildConfig(config, args, false, configFile);

		// Process the CLASSPATH
		String propcp = compilerConfig.getClasspath();
		if (propcp != null && propcp.length() != 0) {
			StringTokenizer st = new StringTokenizer(propcp, File.pathSeparator);
			List<String> configClasspath = config.getClasspath();
			ArrayList<String> toAdd = new ArrayList<>();
			while (st.hasMoreTokens()) {
				String entry = st.nextToken();
				if (!configClasspath.contains(entry)) {
					toAdd.add(entry);
				}
			}
			if (0 < toAdd.size()) {
				List<String> both = new ArrayList<>(configClasspath.size() + toAdd.size());
				both.addAll(configClasspath);
				both.addAll(toAdd);
				config.setClasspath(both);
				Classpath[] checkedClasspaths = config.getCheckedClasspaths();
				ArrayList<Classpath> cps = parser.handleClasspath(toAdd, compilerConfig.getProjectEncoding());
				Classpath[] newCheckedClasspaths = new Classpath[checkedClasspaths.length+cps.size()];
				System.arraycopy(checkedClasspaths, 0, newCheckedClasspaths, 0, checkedClasspaths.length);
				for (int i=0;i<cps.size();i++) {
					newCheckedClasspaths[checkedClasspaths.length+i] = cps.get(i);
				}
				config.setCheckedClasspaths(newCheckedClasspaths);
			}
		}

		// Process the OUTJAR
		if (config.getOutputJar() == null) {
			String outJar = compilerConfig.getOutJar();
			if (outJar != null && outJar.length() != 0) {
				config.setOutputJar(new File(outJar));
			}
		}

		// Process the OUTPUT LOCATION MANAGER
		IOutputLocationManager outputLocationManager = compilerConfig.getOutputLocationManager();
		if (config.getCompilationResultDestinationManager() == null && outputLocationManager != null) {
			config.setCompilationResultDestinationManager(new OutputLocationAdapter(outputLocationManager));
		}

		// Process the INPATH
		config.addToInpath(compilerConfig.getInpath());
		// bug 168840 - calling 'setInPath(..)' creates BinarySourceFiles which
		// are used to see if there have been changes in classes on the inpath
		if (config.getInpath() != null) {
			config.processInPath();
		}

		// Process the SOURCE PATH RESOURCES
		config.setSourcePathResources(compilerConfig.getSourcePathResources());

		// Process the ASPECTPATH
		config.addToAspectpath(compilerConfig.getAspectPath());

		// Process the JAVA OPTIONS MAP
		Map<String,String> jom = compilerConfig.getJavaOptionsMap();
		if (jom != null) {
			String version = jom.get(CompilerOptions.OPTION_Compliance);
			if (version != null && !version.equals(CompilerOptions.VERSION_1_4)) {
				config.setBehaveInJava5Way(true);
			}
			config.getOptions().set(jom);
		}

		// Process the NON-STANDARD COMPILER OPTIONS
		configureNonStandardOptions(config);

		compilerConfig.configurationRead();

		ISourceLocation location = null;
		if (config.getConfigFile() != null) {
			location = new SourceLocation(config.getConfigFile(), 0);
		}

		String message = parser.getOtherMessages(true);
		if (null != message) {
			IMessage m = new Message(message, IMessage.ERROR, null, location);
			handler.handleMessage(m);
		}

		// always force model generation in AJDE
		config.setGenerateModelMode(true);
		// always be in incremental mode in AJDE
		config.setIncrementalMode(true);
		// always force proceedOnError in AJDE
		config.setProceedOnError(true);

		config.setProjectEncoding(compilerConfig.getProjectEncoding());
		config.setProcessor(compilerConfig.getProcessor());
		config.setProcessorPath(compilerConfig.getProcessorPath());
		return config;
	}

	/**
	 * Helper method for configure build options. This reads all command-line options specified in the non-standard options text
	 * entry and sets any corresponding unset values in config.
	 */
	private void configureNonStandardOptions(AjBuildConfig config) {

		String nonStdOptions = compiler.getCompilerConfiguration().getNonStandardOptions();
		if (LangUtil.isEmpty(nonStdOptions)) {
			return;
		}

		// Break a string into a string array of non-standard options.
		// Allows for one option to include a ' '. i.e. assuming it has been quoted, it
		// won't accidentally get treated as a pair of options (can be needed for xlint props file option)
		List<String> tokens = new ArrayList<>();
		int ind = nonStdOptions.indexOf('\"');
		int ind2 = nonStdOptions.indexOf('\"', ind + 1);
		if ((ind > -1) && (ind2 > -1)) { // dont tokenize within double quotes
			String pre = nonStdOptions.substring(0, ind);
			String quoted = nonStdOptions.substring(ind + 1, ind2);
			String post = nonStdOptions.substring(ind2 + 1, nonStdOptions.length());
			tokens.addAll(tokenizeString(pre));
			tokens.add(quoted);
			tokens.addAll(tokenizeString(post));
		} else {
			tokens.addAll(tokenizeString(nonStdOptions));
		}
		String[] args = tokens.toArray(new String[] {});

		// set the non-standard options in an alternate build config
		// (we don't want to lose the settings we already have)
		CountingMessageHandler counter = CountingMessageHandler.makeCountingMessageHandler(msgHandlerAdapter);
		AjBuildConfig altConfig = AjdtCommand.genBuildConfig(args, counter);
		if (counter.hasErrors()) {
			return;
		}
		// copy globals where local is not set
		config.installGlobals(altConfig);
	}

	/** Local helper method for splitting option strings */
	private List<String> tokenizeString(String str) {
		List<String> tokens = new ArrayList<>();
		StringTokenizer tok = new StringTokenizer(str);
		while (tok.hasMoreTokens()) {
			tokens.add(tok.nextToken());
		}
		return tokens;
	}

	/**
	 * Helper method to ask the messagehandler to handle the given message
	 */
	private void handleMessage(Message msg) {
		compiler.getMessageHandler().handleMessage(msg);
	}

	public void setCustomMungerFactory(Object o) {
		ajBuildManager.setCustomMungerFactory(o);
	}

	public Object getCustomMungerFactory() {
		return ajBuildManager.getCustomMungerFactory();
	}

	public void cleanupEnvironment() {
		ajBuildManager.cleanupEnvironment();
	}

	public AsmManager getStructureModel() {
		return ajBuildManager.getStructureModel();
	}
}
