/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     PARC     initial implementation 
 * ******************************************************************/


package org.aspectj.ajdt.internal.core.builder;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.aspectj.ajdt.internal.compiler.AjCompiler;
import org.aspectj.ajdt.internal.compiler.lookup.AjLookupEnvironment;
import org.aspectj.ajdt.internal.compiler.lookup.EclipseFactory;
import org.aspectj.ajdt.internal.compiler.parser.AjParser;
import org.aspectj.ajdt.internal.compiler.problem.AjProblemReporter;
import org.aspectj.asm.ProgramElementNode;
import org.aspectj.asm.StructureModel;
import org.aspectj.asm.StructureModelManager;
import org.aspectj.bridge.AbortException;
import org.aspectj.bridge.CountingMessageHandler;
import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.IMessageHandler;
import org.aspectj.bridge.IProgressListener;
import org.aspectj.bridge.Message;
import org.aspectj.bridge.MessageUtil;
import org.aspectj.bridge.Version;
import org.aspectj.weaver.World;
import org.aspectj.weaver.bcel.BcelWeaver;
import org.aspectj.weaver.bcel.BcelWorld;
import org.aspectj.weaver.bcel.UnwovenClassFile;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.internal.compiler.ClassFile;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.DefaultErrorHandlingPolicies;
import org.eclipse.jdt.internal.compiler.ICompilerRequestor;
import org.eclipse.jdt.internal.compiler.IProblemFactory;
import org.eclipse.jdt.internal.compiler.batch.CompilationUnit;
import org.eclipse.jdt.internal.compiler.batch.FileSystem;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblemFactory;
import org.eclipse.jdt.internal.compiler.util.HashtableOfObject;

public class AjBuildManager {
	private IProgressListener progressListener = null;
	
	private int compiledCount;
	private int sourceFileCount;
	
	private StructureModel structureModel;
	public AjBuildConfig buildConfig;
	
	AjState state = new AjState(this);
    
	BcelWeaver bcelWeaver;
	public BcelWorld bcelWorld;
	
	public CountingMessageHandler handler;

	public AjBuildManager(IMessageHandler holder) {
		super();
        this.handler = CountingMessageHandler.makeCountingMessageHandler(holder);
	}

    /** @return true if we should generate a model as a side-effect */
    public boolean doGenerateModel() {
        return buildConfig.isGenerateModelMode();
    }

    /** @throws AbortException if check for runtime fails */
	public boolean batchBuild(AjBuildConfig buildConfig, IMessageHandler baseHandler) 
        throws IOException, AbortException
    {
		this.handler = CountingMessageHandler.makeCountingMessageHandler(baseHandler);
 
		try {
			setBuildConfig(buildConfig);
			state.prepareForNextBuild(buildConfig);
			
			String check = checkRtJar(buildConfig);
			if (check != null) {
                IMessage message = new Message(check, Message.WARNING, null, null);
                // give delegate a chance to implement different message (abort)?
                handler.handleMessage(message); 
            }

			setupModel();
            initBcelWorld(handler);
            if (handler.hasErrors()) {
                return false;
            }

			if (buildConfig.isEmacsSymMode() || buildConfig.isGenerateModelMode()) {  
				bcelWorld.setModel(StructureModelManager.INSTANCE.getStructureModel());
			}
			
			performCompilation(buildConfig.getFiles());

			if (buildConfig.isEmacsSymMode()) {
				new org.aspectj.ajdt.internal.core.builder.EmacsStructureModelManager().externalizeModel();
			}

			if (handler.hasErrors()) {
                return false;
            }
            
            state.successfulCompile(buildConfig);  //!!! a waste of time when not incremental

			boolean weaved = weaveAndGenerateClassFiles();
			
			if (buildConfig.isGenerateModelMode()) {
				StructureModelManager.INSTANCE.fireModelUpdated();  
			}
			return !handler.hasErrors();
		} finally {
            handler = null;        
        }
	}
    
	private void setupModel() {
		String rootLabel = "<root>";
		if (buildConfig.getConfigFile() != null) {
			rootLabel = buildConfig.getConfigFile().getName();
			StructureModelManager.INSTANCE.getStructureModel().setConfigFile(
				buildConfig.getConfigFile().getAbsolutePath()
			);	
		}
		StructureModelManager.INSTANCE.getStructureModel().setRoot(
			new ProgramElementNode(
				rootLabel,
				ProgramElementNode.Kind.FILE_JAVA,
				new ArrayList()));
				
		HashMap modelFileMap = new HashMap();
		StructureModelManager.INSTANCE.getStructureModel().setFileMap(new HashMap());
		setStructureModel(StructureModelManager.INSTANCE.getStructureModel());
			
	}

	//XXX fake incremental
	public boolean incrementalBuild(AjBuildConfig buildConfig, IMessageHandler baseHandler) throws IOException {
		if (!state.prepareForNextBuild(buildConfig)) {
			return batchBuild(buildConfig, baseHandler);
		}
		
		//!!! too much cut-and-paste from batchBuild
		this.handler = CountingMessageHandler.makeCountingMessageHandler(baseHandler);
 
		try {
			setBuildConfig(buildConfig);

			//setupModel();
//			initBcelWorld(handler);
//			if (handler.hasErrors()) {
//				return false;
//			}

//			if (buildConfig.isEmacsSymMode() || buildConfig.isGenerateModelMode()) {  
//				bcelWorld.setModel(StructureModelManager.INSTANCE.getStructureModel());
//			}
			int count = 0;
			List filesToCompile;
			while ( !(filesToCompile = state.getFilesToCompile(count == 0)).isEmpty() ) {
				//if (count > 0) return batchBuild(buildConfig, baseHandler);  //??? only 1 try
				performCompilation(filesToCompile);
				
				if (handler.hasErrors()) return false;
				
				if (count++ > 5) {
					return batchBuild(buildConfig, baseHandler);
				}
			}
			
			//System.err.println("built in " + count + " cycles");

//			if (buildConfig.isEmacsSymMode()) {
//				new org.aspectj.ajdt.internal.core.builder.EmacsStructureModelManager().externalizeModel();
//			}

			if (handler.hasErrors()) {
				return false;
			}
            
			state.successfulCompile(buildConfig);

			boolean weaved = weaveAndGenerateClassFiles();
			
			if (buildConfig.isGenerateModelMode()) {
				StructureModelManager.INSTANCE.fireModelUpdated();  
			}
			return !handler.hasErrors();
		} finally {
			handler = null;        
		}		
		
	}

//        if (javaBuilder == null || javaBuilder.currentProject == null || javaBuilder.lastState == null) {
//        	return batchBuild(buildConfig, messageHandler);
//        }
//        
//        
//        final CountingMessageHandler counter = new CountingMessageHandler(messageHandler);                    
//        try {
//            currentHandler = counter;
//    		IncrementalBuilder builder = getIncrementalBuilder(messageHandler);
//    //		SimpleLookupTable deltas = 
//    		//XXX for Mik, replace this with a call to builder.build(SimpleLookupTable deltas)
//        
//    		IContainer[] sourceFolders = new IContainer[buildConfig.getSourceRoots().size()];
//    		int count = 0;
//    		for (Iterator i = buildConfig.getSourceRoots().iterator(); i.hasNext(); count++) {
//    			sourceFolders[count] = new FilesystemFolder(((File)i.next()).getAbsolutePath());
//    		}
//    		builder.setSourceFolders(sourceFolders);
//    		getJavaBuilder().binaryResources = new SimpleLookupTable();
//    		SimpleLookupTable deltas = getDeltas(buildConfig);
//    	 
//    	 	//MessageUtil.info(messageHandler, "about to do incremental build: " + deltas);
//    	 
//    		boolean succeeded = builder.build(deltas);
//    		
//       		if (counter.hasErrors()) {
//                return false;
//            }
//    		
//       		if (succeeded) {
//    			return weaveAndGenerateClassFiles(builder.getNewState());
//    		} else {
//    			return batchBuild(buildConfig, messageHandler);
//    		}
//        } finally {
//            currentHandler = null;
//        }
//    
//	}	
		
	private void initBcelWorld(IMessageHandler handler) throws IOException {
		bcelWorld = new BcelWorld(buildConfig.getClasspath(), handler);
		bcelWorld.setXnoInline(buildConfig.isXnoInline());
		bcelWeaver = new BcelWeaver(bcelWorld);
		
		for (Iterator i = buildConfig.getAspectpath().iterator(); i.hasNext();) {
			File f = (File) i.next();
			bcelWeaver.addLibraryJarFile(f);
		}
		
		String lintMode = buildConfig.getLintMode();
		
		if (buildConfig.getLintMode().equals(AjBuildConfig.AJLINT_DEFAULT)) {
			bcelWorld.getLint().loadDefaultProperties();
		} else {
			bcelWorld.getLint().setAll(buildConfig.getLintMode());
		}
		
		if (buildConfig.getLintSpecFile() != null) {
			bcelWorld.getLint().setFromProperties(buildConfig.getLintSpecFile());
		}
		
		//??? incremental issues
		for (Iterator i = buildConfig.getInJars().iterator(); i.hasNext(); ) {
			File inJar = (File)i.next();
			bcelWeaver.addJarFile(inJar, buildConfig.getOutputDir());
		}
	}
	
	public World getWorld() {
		return bcelWorld;
	}
	
	void addAspectClassFilesToWeaver(List addedClassFiles) throws IOException {
		for (Iterator i = addedClassFiles.iterator(); i.hasNext(); ) {
			UnwovenClassFile classFile = (UnwovenClassFile) i.next();
			bcelWeaver.addClassFile(classFile);
		}
	}

	public boolean weaveAndGenerateClassFiles() throws IOException {
		handler.handleMessage(MessageUtil.info("weaving"));
		if (progressListener != null) progressListener.setText("weaving aspects");
		//!!! doesn't provide intermediate progress during weaving
		addAspectClassFilesToWeaver(state.addedClassFiles);
		if (buildConfig.isNoWeave()) {
			if (buildConfig.getOutputJar() != null) {
				bcelWeaver.dumpUnwoven(buildConfig.getOutputJar());
			} else {
				bcelWeaver.dumpUnwoven();
			}
		} else {
			if (buildConfig.getOutputJar() != null) {
				bcelWeaver.weave(buildConfig.getOutputJar());
			} else {
				bcelWeaver.weave();
			}
		}
		if (progressListener != null) progressListener.setProgress(1.0);
		return true;
        //return messageAdapter.getErrorCount() == 0; //!javaBuilder.notifier.anyErrors();
	}
	
	public FileSystem getLibraryAccess(String[] classpaths, String[] filenames) {
		String defaultEncoding = (String) buildConfig.getJavaOptions().get(CompilerOptions.OPTION_Encoding);
		if ("".equals(defaultEncoding)) //$NON-NLS-1$
			defaultEncoding = null; //$NON-NLS-1$	
		return new FileSystem(classpaths, filenames, defaultEncoding);
	}
	
	public IProblemFactory getProblemFactory() {
		return new DefaultProblemFactory(Locale.getDefault());
	}
    
	/*
	 *  Build the set of compilation source units
	 */
	public CompilationUnit[] getCompilationUnits(String[] filenames, String[] encodings) {
		int fileCount = filenames.length;
		CompilationUnit[] units = new CompilationUnit[fileCount];
		HashtableOfObject knownFileNames = new HashtableOfObject(fileCount);

		String defaultEncoding = (String) buildConfig.getJavaOptions().get(CompilerOptions.OPTION_Encoding);
		if ("".equals(defaultEncoding)) //$NON-NLS-1$
			defaultEncoding = null; //$NON-NLS-1$

		for (int i = 0; i < fileCount; i++) {
//			these tests are performed for AjBuildConfig
//			char[] charName = filenames[i].toCharArray();
//			if (knownFileNames.get(charName) != null) {
//				MessageUtil.error(handler, "duplicate file " + filenames[i]);
//			} else {
//				knownFileNames.put(charName, charName);
//			}
//			File file = new File(filenames[i]);
//			if (!file.exists()) {
//				MessageUtil.error(handler, "missing file " + filenames[i]);
//			}
			String encoding = encodings[i];
			if (encoding == null)
				encoding = defaultEncoding;
			units[i] = new CompilationUnit(null, filenames[i], encoding);
		}
		return units;
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
    
    
	public void performCompilation(List files) {
		if (progressListener != null) {
			compiledCount = 0;
			sourceFileCount = files.size();
			progressListener.setText("compiling source files");
		}
		//System.err.println("got files: " + files);
		String[] filenames = new String[files.size()];
		String[] encodings = new String[files.size()];
		//System.err.println("filename: " + this.filenames);
		for (int i=0; i < files.size(); i++) {
			filenames[i] = ((File)files.get(i)).getPath();
		}
		
		List cps = buildConfig.getFullClasspath();
		String[] classpaths = new String[cps.size()];
		for (int i=0; i < cps.size(); i++) {
			classpaths[i] = (String)cps.get(i);
		}
		
		//System.out.println("compiling");
		INameEnvironment environment = getLibraryAccess(classpaths, filenames);
		
		if (!state.classesFromName.isEmpty()) {
			environment = new StatefulNameEnvironment(environment, state.classesFromName);
		}
		
//		Compiler batchCompiler =
//			new Compiler(
//				environment,
//				getHandlingPolicy(),
//				getOptions(),
//				getBatchRequestor(),
//				getProblemFactory());
		AjCompiler compiler = new AjCompiler(
			environment,
			DefaultErrorHandlingPolicies.proceedWithAllProblems(),
		    buildConfig.getJavaOptions(),
			getBatchRequestor(),
			getProblemFactory());
			
			
		AjProblemReporter pr =
			new AjProblemReporter(DefaultErrorHandlingPolicies.proceedWithAllProblems(),
			compiler.options, getProblemFactory());
		
		compiler.problemReporter = pr;
			
		AjLookupEnvironment le =
			new AjLookupEnvironment(compiler, compiler.options, pr, environment);
		EclipseFactory factory = new EclipseFactory(le);
//		ew.setLint(bcelWorld.getLint());
//		ew.setXnoInline(buildConfig.isXnoInline());
		le.factory = factory;
		pr.world = factory;
		le.factory.buildManager = this;
		
		compiler.lookupEnvironment = le;
		
		compiler.parser =
			new AjParser(
				pr, 
				compiler.options.parseLiteralExpressionsAsConstants, 
				compiler.options.sourceLevel >= CompilerOptions.JDK1_4);

		CompilerOptions options = compiler.options;

		options.produceReferenceInfo(true); //TODO turn off when not needed
		
		compiler.compile(getCompilationUnits(filenames, encodings));
		
		// cleanup
		environment.cleanup();
	}

	/*
	 * Answer the component to which will be handed back compilation results from the compiler
	 */
	public ICompilerRequestor getBatchRequestor() {
		return new ICompilerRequestor() {
			int lineDelta = 0;
			public void acceptResult(CompilationResult compilationResult) {
				if (progressListener != null) {
					compiledCount++;
					progressListener.setProgress((compiledCount/2.0)/sourceFileCount);
				}
				
				if (compilationResult.hasProblems() || compilationResult.hasTasks()) {
					IProblem[] problems = compilationResult.getAllProblems();
					for (int i=0; i < problems.length; i++) {
						IMessage message =
							EclipseAdapterUtils.makeMessage(compilationResult.compilationUnit, problems[i]);
						handler.handleMessage(message);
					}
				}
				outputClassFiles(compilationResult);
			}
		};
	}
	
	private boolean proceedOnError() {
		return true;  //???
	}

	public void outputClassFiles(CompilationResult unitResult) {
		if (unitResult == null) return;
		
		String sourceFileName = new String(unitResult.fileName);
		if (!(unitResult.hasErrors() && !proceedOnError())) {
			List unwovenClassFiles = new ArrayList();
			Enumeration classFiles = unitResult.compiledTypes.elements();
			while (classFiles.hasMoreElements()) {
				ClassFile classFile = (ClassFile) classFiles.nextElement();
				String filename = new String(classFile.fileName());
				filename = filename.replace('/', File.separatorChar) + ".class";
				
				File destinationPath = buildConfig.getOutputDir();
				if (destinationPath == null) {
					destinationPath = new File(extractDestinationPathFromSourceFile(unitResult));
				}
				filename = new File(destinationPath, filename).getPath();
				//System.out.println("classfile: " + filename);
				unwovenClassFiles.add(new UnwovenClassFile(filename, classFile.getBytes()));
			}
			state.noteClassesFromFile(unitResult, sourceFileName, unwovenClassFiles);
//			System.out.println("file: " + sourceFileName);
//			for (int i=0; i < unitResult.simpleNameReferences.length; i++) {
//				System.out.println("simple: " + new String(unitResult.simpleNameReferences[i]));
//			}
//			for (int i=0; i < unitResult.qualifiedReferences.length; i++) {
//				System.out.println("qualified: " +
//					new String(CharOperation.concatWith(unitResult.qualifiedReferences[i], '/')));
//			}
		} else {
			state.noteClassesFromFile(null, sourceFileName, Collections.EMPTY_LIST);
		}
	}
    

	private void setBuildConfig(AjBuildConfig buildConfig) {
		this.buildConfig = buildConfig;
		handler.reset();
	}
	
	String makeClasspathString() {
		if (buildConfig == null || buildConfig.getClasspath() == null) return "";
		StringBuffer buf = new StringBuffer();
		boolean first = true;
		for (Iterator it = buildConfig.getClasspath().iterator(); it.hasNext(); ) {
			if (first) { first = false; }
			else { buf.append(File.pathSeparator); }
			buf.append(it.next().toString());
		}
		return buf.toString();
	}
	
	
	/**
	 * This will return null if aspectjrt.jar is present and has the correct version.
	 * Otherwise it will return a string message indicating the problem.
	 */
	public String checkRtJar(AjBuildConfig buildConfig) {
        // omitting dev info
		if (Version.text.equals(Version.DEVELOPMENT)) {
			// in the development version we can't do this test usefully
//			MessageUtil.info(holder, "running development version of aspectj compiler");
			return null;
		}
		
		if (buildConfig == null || buildConfig.getClasspath() == null) return "no classpath specified";
		for (Iterator it = buildConfig.getClasspath().iterator(); it.hasNext(); ) {
			File p = new File( (String)it.next() );
			if (p.isFile() && p.getName().equals("aspectjrt.jar")) {

				try {
                    String version = null;
                    Manifest manifest = new JarFile(p).getManifest();
                    if (manifest == null) {
                    	return "no manifest found in " + p.getAbsolutePath() + 
								", expected " + Version.text;
                    }
                    Attributes attr = manifest.getAttributes("org/aspectj/lang/");
                    if (null != attr) {
                        version = attr.getValue(Attributes.Name.IMPLEMENTATION_VERSION);
                        if (null != version) {
                            version = version.trim();
                        }
                    }
					// assume that users of development aspectjrt.jar know what they're doing
					if (Version.DEVELOPMENT.equals(version)) {
//						MessageUtil.info(holder,
//							"running with development version of aspectjrt.jar in " + 
//							p.getAbsolutePath());
                        return null;
					} else if (!Version.text.equals(version)) {
						return "bad version number found in " + p.getAbsolutePath() + 
								" expected " + Version.text + " found " + version;
					}
				} catch (IOException ioe) {
					return "bad jar file found in " + p.getAbsolutePath() + " error: " + ioe;
				}
				return null;
			} else {
				// might want to catch other classpath errors
			}
		}
		
		return "couldn't find aspectjrt.jar on classpath, checked: " + makeClasspathString();
	}
	

	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("AjBuildManager(");
		buf.append(")");
		return buf.toString();
	}


	public void setStructureModel(StructureModel structureModel) {
		this.structureModel = structureModel;
	}

	/**
	 * Returns null if there is no structure model
	 */
	public StructureModel getStructureModel() {
		return structureModel;
	}
    
	public IProgressListener getProgressListener() {
		return progressListener;
	}

	public void setProgressListener(IProgressListener progressListener) {
		this.progressListener = progressListener;
	}
}   // class AjBuildManager

