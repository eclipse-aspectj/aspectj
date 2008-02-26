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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.aspectj.ajde.core.AjCompiler;
import org.aspectj.ajde.core.ICompilerConfiguration;
import org.aspectj.ajde.core.IOutputLocationManager;
import org.aspectj.ajde.core.JavaOptions;
import org.aspectj.ajdt.ajc.AjdtCommand;
import org.aspectj.ajdt.ajc.BuildArgParser;
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
import org.aspectj.org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.aspectj.util.ConfigParser;
import org.aspectj.util.LangUtil;

/**
 * Build Manager which drives the build for a given AjCompiler.
 * Tools call build on the AjCompiler which drives this.
 */
public class AjdeCoreBuildManager {

	private AjCompiler compiler;
	
    private AjdeCoreBuildNotifierAdapter currNotifier = null;
	private AjBuildManager ajBuildManager;
	private IMessageHandler msgHandlerAdapter;
	
	public AjdeCoreBuildManager(AjCompiler compiler) {
		this.compiler = compiler;
		msgHandlerAdapter = new AjdeCoreMessageHandlerAdapter(compiler.getMessageHandler());
		ajBuildManager = new AjBuildManager(msgHandlerAdapter);
		ajBuildManager.environmentSupportsIncrementalCompilation(true);
		// this static information needs to be set to ensure 
		// incremental compilation works correctly
		IncrementalStateManager.recordIncrementalStates=true;
		IncrementalStateManager.debugIncrementalStates=false;
		AsmManager.attemptIncrementalModelRepairs = true;
	}
	
	// XXX hideous, should not be Object
	public void setCustomMungerFactory(Object o) {
		ajBuildManager.setCustomMungerFactory(o);
	}
	
	public Object getCustomMungerFactory() {
		return ajBuildManager.getCustomMungerFactory();
	}
	
	/**
	 * @param buildFresh - true if want to force a full build, false otherwise
	 */
	public void doBuild(boolean buildFresh) {
       	if (!buildFresh) {
       		buildFresh = updateAsmManagerInformation();
       	}
        try {
        	startNotifiers();
        	
        	// record the options passed to the compiler
			handleMessage(new Message(getFormattedOptionsString(),IMessage.INFO,null,null));

			CompilationAndWeavingContext.reset();
			AjBuildConfig buildConfig = genAjBuildConfig();
			if (buildConfig == null) return;
			
            if (buildFresh) {
            	ajBuildManager.batchBuild(buildConfig,msgHandlerAdapter); 
            } else {
				ajBuildManager.incrementalBuild(buildConfig,msgHandlerAdapter);				
            }
			IncrementalStateManager.recordSuccessfulBuild(compiler.getId(),ajBuildManager.getState());
            
        } catch (ConfigParser.ParseException pe) {
        	handleMessage(new Message("Config file entry invalid, file: " + pe.getFile().getPath() 
                	+ ", line number: " + pe.getLine(),IMessage.WARNING,null,null));
		} catch (AbortException e) {
            final IMessage message = e.getIMessage();
            if (message == null) {
                handleMessage(new Message(LangUtil.unqualifiedClassName(e) + " thrown: " 
                		+ e.getMessage(),IMessage.ERROR,e,null));
            } else {
            	handleMessage(new Message(message.getMessage() + "\n" 
            			+ CompilationAndWeavingContext.getCurrentContext(),IMessage.ERROR,e,null));
            };
		} catch (Throwable t) {
            handleMessage(new Message("Compile error: " + LangUtil.unqualifiedClassName(t) + " thrown: " +
            		"" + t.getMessage(),IMessage.ABORT,t,null));
        } finally {
        	compiler.getBuildProgressMonitor().finish(ajBuildManager.wasFullBuild());
        }
	}
	
	/**
	 * Starts the various notifiers which are interested in the build progress
	 */
	private void startNotifiers() {
    	compiler.getBuildProgressMonitor().begin();
		currNotifier = new AjdeCoreBuildNotifierAdapter(compiler.getBuildProgressMonitor());		
		ajBuildManager.setProgressListener(currNotifier);		
	}
	
	/**
	 * Switches the relationshipMap and hierarchy used by AsmManager to be
	 * the one for the current compiler - this will not be necessary once
	 * the static nature is removed from the asm.
	 */
	private boolean updateAsmManagerInformation() {
   		AjState updatedState = IncrementalStateManager.retrieveStateFor(compiler.getId());
   		if (updatedState == null) {
   			return true;
   		} else {
       		AsmManager.getDefault().setRelationshipMap(updatedState.getRelationshipMap());
       		AsmManager.getDefault().setHierarchy(updatedState.getStructureModel());
   		}
		return false;
	}
    
	// AMC - updated for AspectJ 1.1 options
	private String getFormattedOptionsString() {
		ICompilerConfiguration compilerConfig = compiler.getCompilerConfiguration();
		return "Building with settings: "
			+ "\n-> output paths: " + formatCollection(compilerConfig.getOutputLocationManager()
					.getAllOutputLocations())
			+ "\n-> classpath: " + compilerConfig.getClasspath()
			+ "\n-> -inpath " + formatCollection(compilerConfig.getInpath())
			+ "\n-> -outjar " + formatOptionalString(compilerConfig.getOutJar())
			+ "\n-> -aspectpath " + formatCollection(compilerConfig.getAspectPath())
			+ "\n-> -sourcePathResources " + formatMap(compilerConfig.getSourcePathResources())
			+ "\n-> non-standard options: " + compilerConfig.getNonStandardOptions()
			+ "\n-> javaoptions:" + formatMap(compilerConfig.getJavaOptionsMap());
	}
	
	private String formatCollection( Collection options ) {
		if ( options == null ) return "<default>";
		if ( options.isEmpty() ) return "none";
		
		StringBuffer formattedOptions = new StringBuffer();
		Iterator it = options.iterator();
		while (it.hasNext()) {
			String o = it.next().toString();
			if (formattedOptions.length() > 0) formattedOptions.append(", ");
			formattedOptions.append( o );
		}
		return formattedOptions.toString();
	}
	
	private String formatMap( Map options) {
		if (options == null) return "<default>";
		if (options.isEmpty()) return "none";
		
		return options.toString();
	}
	
	private String formatOptionalString( String s ) {
		if ( s == null ) { return ""	; }
		else { return s; }
	}
	
    /**
     * Generate a new AjBuildConfig from the compiler configuration
     * associated with this AjdeCoreBuildManager
     * 
     * @return null if invalid configuration, corresponding 
     * AjBuildConfig otherwise
     */
	public AjBuildConfig genAjBuildConfig() {
	    File configFile = new File(compiler.getId());
	    String[] args = null;
	    if (configFile.exists() && configFile.isFile()) {
			args = new String[] { "@" + configFile.getAbsolutePath() };
		} else {
			List l = compiler.getCompilerConfiguration().getProjectSourceFiles();
			if (l == null) return null;
			args = new String[l.size()];
			int counter = 0;
			for (Iterator iter = l.iterator(); iter.hasNext();) {
				String element = (String) iter.next();
				args[counter] = element;
				counter++;
			}
		}
        CountingMessageHandler handler = CountingMessageHandler.makeCountingMessageHandler(
        		msgHandlerAdapter);
		BuildArgParser parser = new BuildArgParser(handler);
		
		AjBuildConfig config = new AjBuildConfig();
        parser.populateBuildConfig(config, args, false, configFile); 
        configureCompilerOptions(config);

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
		return config;
	}
	
	/**
	 * Check that the user hasn't specified Java 6 for the compliance, source and
	 * target levels. If they have then an error is thrown. 
	 */
	private void checkNotAskedForJava6Compliance() {
		// bug 164384 - Throwing an IMessage.ERRROR rather than an IMessage.ABORT 
		// means that we'll continue to try to compile the code. This means that
		// the user may see other errors (for example, if they're using annotations
		// then they'll get errors saying that they require 5.0 compliance).
		// Throwing IMessage.ABORT would prevent this, however, 'abort' is really
		// for compiler exceptions.
		Map javaOptions = compiler.getCompilerConfiguration().getJavaOptionsMap();
		if (javaOptions != null){
			String version = (String)javaOptions.get(CompilerOptions.OPTION_Compliance);
			String sourceVersion = (String)javaOptions.get(CompilerOptions.OPTION_Source);
			String targetVersion = (String)javaOptions.get(CompilerOptions.OPTION_TargetPlatform);
			if (version!=null && version.equals(JavaOptions.VERSION_16)) {
				String msg = "Java 6.0 compliance level is unsupported";
				IMessage m = new Message(msg, IMessage.ERROR, null, null);            
				compiler.getMessageHandler().handleMessage(m);
			} else if (sourceVersion!=null && sourceVersion.equals(JavaOptions.VERSION_16)) {
				String msg = "Java 6.0 source level is unsupported";
				IMessage m = new Message(msg, IMessage.ERROR, null, null);            
				compiler.getMessageHandler().handleMessage(m);				
			} else if (targetVersion!=null && targetVersion.equals(JavaOptions.VERSION_16)) {
				String msg = "Java 6.0 target level is unsupported";
				IMessage m = new Message(msg, IMessage.ERROR, null, null);            
				compiler.getMessageHandler().handleMessage(m);	
			}
		}
	}
	
	/**
	 * Configure the given AjBuildConfig with the options found in the
	 * ICompilerConfiguration implementation associated with the AjCompiler
	 * for this AjdeCoreBuildManager
	 * 
	 * @param config
	 */
	private void configureCompilerOptions(AjBuildConfig config) {
		
        String propcp = compiler.getCompilerConfiguration().getClasspath();
        if (!LangUtil.isEmpty(propcp)) {
            StringTokenizer st = new StringTokenizer(propcp, File.pathSeparator);
            List configClasspath = config.getClasspath();
            ArrayList toAdd = new ArrayList();
            while (st.hasMoreTokens()) {
                String entry = st.nextToken();
                if (!configClasspath.contains(entry)) {
                    toAdd.add(entry);
                }
            }
            if (0 < toAdd.size()) {
                ArrayList both = new ArrayList(configClasspath.size() + toAdd.size());
                both.addAll(configClasspath);
                both.addAll(toAdd);
                config.setClasspath(both);
            }
        }
        
        // set the outputjar
        if (config.getOutputJar() == null) {
            String outJar = compiler.getCompilerConfiguration().getOutJar();
            if (!LangUtil.isEmpty(outJar)) {
                config.setOutputJar(new File( outJar ) );  
            }
        }
        
        // set compilation result destination manager 
        IOutputLocationManager outputLocationManager = compiler.getCompilerConfiguration().getOutputLocationManager();
        if (config.getCompilationResultDestinationManager() == null && outputLocationManager != null) {
        	config.setCompilationResultDestinationManager(new OutputLocationAdapter(outputLocationManager));
        }

        join(config.getInpath(),compiler.getCompilerConfiguration().getInpath());
        // bug 168840 - calling 'setInPath(..)' creates BinarySourceFiles which
        // are used to see if there have been changes in classes on the inpath
        if (config.getInpath() != null) config.setInPath(config.getInpath());
		config.setSourcePathResources(compiler.getCompilerConfiguration().getSourcePathResources());
        join(config.getAspectpath(), compiler.getCompilerConfiguration().getAspectPath());
        
		Map jom = compiler.getCompilerConfiguration().getJavaOptionsMap();
		if (jom!=null) {
			String version = (String)jom.get(CompilerOptions.OPTION_Compliance);
			if (version!=null && ( version.equals(CompilerOptions.VERSION_1_5) || version.equals(CompilerOptions.VERSION_1_6))) {
				config.setBehaveInJava5Way(true);
			}
			config.getOptions().set(jom);
		}
		
		configureNonStandardOptions(config);	
	}
	
    private void join(Collection target, Collection source) { 
        if ((null == target) || (null == source)) {
            return;
        }
        for (Iterator iter = source.iterator(); iter.hasNext();) {
            Object next = iter.next();
            if (! target.contains(next)) {
                target.add(next);
            }
        }
    }
    
	/**
	 * Helper method for configure build options. This reads all command-line 
	 * options specified in the non-standard options text entry and sets any 
	 * corresponding unset values in config.
	 */
	private void configureNonStandardOptions(AjBuildConfig config) {

		String nonStdOptions = compiler.getCompilerConfiguration().getNonStandardOptions();
        if (LangUtil.isEmpty(nonStdOptions)) {
            return;
        }
		
		// Break a string into a string array of non-standard options.
		// Allows for one option to include a ' '.   i.e. assuming it has been quoted, it
		// won't accidentally get treated as a pair of options (can be needed for xlint props file option)
		List tokens = new ArrayList();
		int ind = nonStdOptions.indexOf('\"');
		int ind2 = nonStdOptions.indexOf('\"',ind+1);
		if ((ind > -1) && (ind2 > -1)) { // dont tokenize within double quotes
			String pre = nonStdOptions.substring(0,ind);
			String quoted = nonStdOptions.substring(ind+1,ind2);
			String post = nonStdOptions.substring(ind2+1,nonStdOptions.length());
			tokens.addAll(tokenizeString(pre));
			tokens.add(quoted);
			tokens.addAll(tokenizeString(post));
		} else {
			tokens.addAll(tokenizeString(nonStdOptions));
		}
		String[] args = (String[])tokens.toArray(new String[]{});
		
		
		// set the non-standard options in an alternate build config
		// (we don't want to lose the settings we already have)
        CountingMessageHandler counter 
            = CountingMessageHandler.makeCountingMessageHandler(msgHandlerAdapter);
		AjBuildConfig altConfig = AjdtCommand.genBuildConfig(args, counter);
		if (counter.hasErrors()) {
            return;
        }
        // copy globals where local is not set
        config.installGlobals(altConfig);
    }
	
	/** Local helper method for splitting option strings */
	private List tokenizeString(String str) {
		List tokens = new ArrayList();
		StringTokenizer tok = new StringTokenizer(str);
		while ( tok.hasMoreTokens() ) {
			tokens.add(tok.nextToken());	
		}
		return tokens;
	}
	
	/**
	 * Helper method to ask the messagehandler to handle the 
	 * given message
	 */
	private void handleMessage(Message msg) {
		compiler.getMessageHandler().handleMessage(msg);
	}
}
