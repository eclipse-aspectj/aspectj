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

import java.io.*;
import java.util.*;
import java.util.jar.*;

import org.aspectj.ajdt.internal.compiler.AjCompiler;
import org.aspectj.ajdt.internal.compiler.lookup.*;
import org.aspectj.ajdt.internal.compiler.problem.AjProblemReporter;
import org.aspectj.asm.*;
//import org.aspectj.asm.internal.*;
import org.aspectj.asm.internal.ProgramElement;
import org.aspectj.bridge.*;
import org.aspectj.weaver.World;
import org.aspectj.weaver.bcel.*;
import org.eclipse.jdt.core.compiler.*;
import org.eclipse.jdt.internal.compiler.*;
import org.eclipse.jdt.internal.compiler.batch.*;
import org.eclipse.jdt.internal.compiler.batch.FileSystem;
import org.eclipse.jdt.internal.compiler.env.*;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.parser.Parser;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblemFactory;
//import org.eclipse.jdt.internal.compiler.util.HashtableOfObject;

public class AjBuildManager {
    static final boolean FAIL_IF_RUNTIME_NOT_FOUND = false;
	private IProgressListener progressListener = null;
	
	private int compiledCount;
	private int sourceFileCount;
	
	private IHierarchy structureModel;
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

	public boolean batchBuild(
        AjBuildConfig buildConfig, 
        IMessageHandler baseHandler) 
        throws IOException, AbortException {
        return doBuild(buildConfig, baseHandler, true);
    }

    public boolean incrementalBuild(
        AjBuildConfig buildConfig, 
        IMessageHandler baseHandler) 
        throws IOException, AbortException {
        return doBuild(buildConfig, baseHandler, false);
    }
    

    /** @throws AbortException if check for runtime fails */
    protected boolean doBuild(
        AjBuildConfig buildConfig, 
        IMessageHandler baseHandler, 
        boolean batch) throws IOException, AbortException {
        
        try {
        	if (batch) {
        		this.state = new AjState(this);
        	}
        	
            boolean canIncremental = state.prepareForNextBuild(buildConfig);
            if (!canIncremental && !batch) { // retry as batch?
                return doBuild(buildConfig, baseHandler, true);
            }
            this.handler = 
                CountingMessageHandler.makeCountingMessageHandler(baseHandler);
            // XXX duplicate, no? remove?
            String check = checkRtJar(buildConfig);
            if (check != null) {
                if (FAIL_IF_RUNTIME_NOT_FOUND) {
                    MessageUtil.error(handler, check);
                    return false;
                } else {
                    MessageUtil.warn(handler, check);
                }
            }
            // if (batch) {
                setBuildConfig(buildConfig);
            //}
            setupModel();
            if (batch) {
                initBcelWorld(handler);
            }
            if (handler.hasErrors()) {
                return false;
            }
            
            if (batch) {
                // System.err.println("XXXX batch: " + buildConfig.getFiles());
                if (buildConfig.isEmacsSymMode() || buildConfig.isGenerateModelMode()) {  
                    bcelWorld.setModel(AsmManager.getDefault().getHierarchy());
                    // in incremental build, only get updated model?
                }
                performCompilation(buildConfig.getFiles());
                if (handler.hasErrors()) {
                    return false;
                }
            } else {
// done already?
//                if (buildConfig.isEmacsSymMode() || buildConfig.isGenerateModelMode()) {  
//                    bcelWorld.setModel(StructureModelManager.INSTANCE.getStructureModel());
//                }
                // System.err.println("XXXX start inc ");
                List files = state.getFilesToCompile(true);
                for (int i = 0; (i < 5) && !files.isEmpty(); i++) {
                    // System.err.println("XXXX inc: " + files);
                    performCompilation(files);
                    if (handler.hasErrors()) {
                        return false;
                    } 
                    files = state.getFilesToCompile(false);
                }
                if (!files.isEmpty()) {
                    return batchBuild(buildConfig, baseHandler);
                }
            }

            // XXX not in Mik's incremental
            if (buildConfig.isEmacsSymMode()) {
                new org.aspectj.ajdt.internal.core.builder.EmacsStructureModelManager().externalizeModel();
            }
            // have to tell state we succeeded or next is not incremental
            state.successfulCompile(buildConfig);

            /*boolean weaved = */weaveAndGenerateClassFiles();
            // if not weaved, then no-op build, no model changes
            // but always returns true
            // XXX weaved not in Mik's incremental
            if (buildConfig.isGenerateModelMode()) {
                AsmManager.getDefault().fireModelUpdated();  
            }
            return !handler.hasErrors();
        } finally {
            handler = null;        
        }
    }
     
    private void setupModel() {
        String rootLabel = "<root>";
		IHierarchy model = AsmManager.getDefault().getHierarchy();
        IProgramElement.Kind kind = IProgramElement.Kind.FILE_JAVA;
        if (buildConfig.getConfigFile() != null) {
            rootLabel = buildConfig.getConfigFile().getName();
            model.setConfigFile(
                buildConfig.getConfigFile().getAbsolutePath()
            );
            kind = IProgramElement.Kind.FILE_LST;  
        }
        model.setRoot(new ProgramElement(rootLabel, kind, new ArrayList()));
                
        model.setFileMap(new HashMap());
        setStructureModel(model);            
    }
    
    /** init only on initial batch compile? no file-specific options */
	private void initBcelWorld(IMessageHandler handler) throws IOException {
		bcelWorld = new BcelWorld(buildConfig.getClasspath(), handler, null);
		bcelWorld.setXnoInline(buildConfig.isXnoInline());
		bcelWorld.setXlazyTjp(buildConfig.isXlazyTjp());
		bcelWeaver = new BcelWeaver(bcelWorld);
		
		for (Iterator i = buildConfig.getAspectpath().iterator(); i.hasNext();) {
			File f = (File) i.next();
			bcelWeaver.addLibraryJarFile(f);
		}
		
//		String lintMode = buildConfig.getLintMode();
		
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
			bcelWeaver.addJarFile(inJar, buildConfig.getOutputDir(),false);
		}
		
		for (Iterator i = buildConfig.getInpath().iterator(); i.hasNext(); ) {
			File inPathElement = (File)i.next();
			bcelWeaver.addJarFile(inPathElement,buildConfig.getOutputDir(),true);
		}
		
		if (buildConfig.getSourcePathResources() != null) {
			for (Iterator i = buildConfig.getSourcePathResources().keySet().iterator(); i.hasNext(); ) {
	//			File resource = (File)i.next();
				String resource = (String)i.next();
				bcelWeaver.addResource(resource, (File)buildConfig.getSourcePathResources().get(resource), buildConfig.getOutputDir());
	//			bcelWeaver.addResource(resource, buildConfig.getOutputDir());
			}
		}
		//check for org.aspectj.runtime.JoinPoint
		bcelWorld.resolve("org.aspectj.lang.JoinPoint");
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
		bcelWeaver.setProgressListener(progressListener, 0.5, 0.5/state.addedClassFiles.size());
		//!!! doesn't provide intermediate progress during weaving
		// XXX add all aspects even during incremental builds?
        addAspectClassFilesToWeaver(state.addedClassFiles);
		if (buildConfig.isNoWeave()) {
			if (buildConfig.getOutputJar() != null) {
				bcelWeaver.dumpUnwoven(buildConfig.getOutputJar());
			} else {
				bcelWeaver.dumpUnwoven();
				bcelWeaver.dumpResourcesToOutPath();
			}
		} else {
			if (buildConfig.getOutputJar() != null) {
				bcelWeaver.weave(buildConfig.getOutputJar());
			} else {
				bcelWeaver.weave();
				bcelWeaver.dumpResourcesToOutPath();
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
		// Bug 46671: We need an array as long as the number of elements in the classpath - *even though* not every
		// element of the classpath is likely to be a directory.  If we ensure every element of the array is set to
		// only look for BINARY, then we make sure that for any classpath element that is a directory, we won't build
		// a classpathDirectory object that will attempt to look for source when it can't find binary.
		int[] classpathModes = new int[classpaths.length];
		for (int i =0 ;i<classpaths.length;i++) classpathModes[i]=ClasspathDirectory.BINARY;
		return new FileSystem(classpaths, filenames, defaultEncoding,classpathModes);
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
//		HashtableOfObject knownFileNames = new HashtableOfObject(fileCount);

		String defaultEncoding = (String) buildConfig.getJavaOptions().get(CompilerOptions.OPTION_Encoding);
		if ("".equals(defaultEncoding)) //$NON-NLS-1$
			defaultEncoding = null; //$NON-NLS-1$

		for (int i = 0; i < fileCount; i++) {
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
		pr.factory = factory;
		le.factory.buildManager = this;
		
		compiler.lookupEnvironment = le;
		
		compiler.parser =
			new Parser(
				pr, 
				compiler.options.parseLiteralExpressionsAsConstants);

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
					progressListener.setText("compiled: " + new String(compilationResult.getFileName()));
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
					filename = new File(filename).getName();
					filename = new File(extractDestinationPathFromSourceFile(unitResult), filename).getPath();
				} else {
					filename = new File(destinationPath, filename).getPath();
				}
				
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


	public void setStructureModel(IHierarchy structureModel) {
		this.structureModel = structureModel;
	}

	/**
	 * Returns null if there is no structure model
	 */
	public IHierarchy getStructureModel() {
		return structureModel;
	}
    
	public IProgressListener getProgressListener() {
		return progressListener;
	}

	public void setProgressListener(IProgressListener progressListener) {
		this.progressListener = progressListener;
	}
}   // class AjBuildManager

