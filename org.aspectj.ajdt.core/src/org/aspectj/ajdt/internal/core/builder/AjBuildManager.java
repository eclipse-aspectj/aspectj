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

import org.aspectj.ajdt.internal.compiler.AjCompiler;
import org.aspectj.ajdt.internal.compiler.lookup.AjLookupEnvironment;
import org.aspectj.ajdt.internal.compiler.lookup.EclipseWorld;
import org.aspectj.ajdt.internal.compiler.parser.AjParser;
import org.aspectj.ajdt.internal.compiler.problem.AjProblemReporter;
import org.aspectj.asm.*;
import org.aspectj.bridge.*;
import org.aspectj.util.LangUtil;
import org.aspectj.weaver.bcel.*;
import org.aspectj.workbench.resources.*;
import org.eclipse.core.internal.events.ResourceDelta;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.internal.compiler.*;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.util.Util;
import org.eclipse.jdt.internal.core.builder.*;
import org.eclipse.jdt.internal.compiler.Compiler;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.jar.*;

public class AjBuildManager {

    public static final IProject DEFAULT_PROJECT // XXX
        = new SimpleProject(new Path(""), "DefaultProject");

	private JavaBuilder javaBuilder;
	private StructureModel structureModel;
	private BuildNotifier buildNotifier = null;
	public AjBuildConfig buildConfig;
    
	private BcelWeaver bcelWeaver;
	public BcelWorld bcelWorld;
	
    /** temp handler for callbacks */
    private IMessageHandler currentHandler; // XXX wrong lifecyle, used for callbacks
    
	ClassFileCache classFileCache;

	Set addedFiles;
 	Set deletedFiles;

	public AjBuildManager(IMessageHandler holder) {
		super();
        LangUtil.throwIaxIfNull(holder, "holder");
	}

    /** @return true if we should generate a model as a side-effect */
    public boolean doGenerateModel() {
        return buildConfig.isGenerateModelMode();
    }

    /** @throws AbortException if check for runtime fails */
	public boolean batchBuild(AjBuildConfig buildConfig, IMessageHandler handler) 
        throws IOException, AbortException {
        final CountingMessageHandler counter = new CountingMessageHandler(handler);            
        handler = null; // fyi - use counter only
		try {
			setBuildConfig(buildConfig);
            currentHandler = counter;
			String check = checkRtJar(buildConfig);
			if (check != null) {
                IMessage message = new Message(check, Message.WARNING, null, null);
                // give delegate a chance to implement different message (abort)?
                counter.handleMessage(message); 
            }

			setupModel();
            initBcelWorld(counter);
            if (counter.hasErrors()) {
                return false;
            }
            initJavaBuilder(counter);
            if (counter.hasErrors()) {
                return false;
            }

			if (buildConfig.isEmacsSymMode() || buildConfig.isGenerateModelMode()) {  
				bcelWorld.setModel(StructureModelManager.INSTANCE.getStructureModel());
			}
			BatchBuilder builder = new BatchBuilder(javaBuilder, counter);
			State newState = builder.run();
			if (buildConfig.isEmacsSymMode()) {
				new org.aspectj.ajdt.internal.core.builder.EmacsStructureModelManager().externalizeModel();
			}
//			System.err.println("check error: " + counter + ", " + 
//					counter.numMessages(IMessage.ERROR) + ", " + counter.numMessages(IMessage.FAIL, false));
			if (counter.hasErrors()) {
                return false;
            }

			boolean weaved = weaveAndGenerateClassFiles(newState);
			//XXX more sturucture disabling until it's optional
			if (false) StructureModelManager.INSTANCE.fireModelUpdated();  
			return weaved;
		} catch (CoreException ce) {
            counter.handleMessage(new Message("core exception", IMessage.ABORT, ce, null));
            //messageAdapter.handleAbort("core exception", ce);
			return false;
		} finally {
            currentHandler = null;        
        }
	}

    /** extruded method as testing backdoor */
    IncrementalBuilder getIncrementalBuilder(IMessageHandler handler) {
        return new IncrementalBuilder(javaBuilder, handler);
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

	public boolean incrementalBuild(AjBuildConfig buildConfig, IMessageHandler messageHandler) throws CoreException, IOException {
        if (javaBuilder == null || javaBuilder.currentProject == null) {
        	return batchBuild(buildConfig, messageHandler);
        }
        
        
        final CountingMessageHandler counter = new CountingMessageHandler(messageHandler);                    
        try {
            currentHandler = counter;
    		IncrementalBuilder builder = getIncrementalBuilder(messageHandler);
    //		SimpleLookupTable deltas = 
    		//XXX for Mik, replace this with a call to builder.build(SimpleLookupTable deltas)
        
    		IContainer[] sourceFolders = new IContainer[buildConfig.getSourceRoots().size()];
    		int count = 0;
    		for (Iterator i = buildConfig.getSourceRoots().iterator(); i.hasNext(); count++) {
    			sourceFolders[count] = new FilesystemFolder(((File)i.next()).getAbsolutePath());
    		}
    		builder.setSourceFolders(sourceFolders);
    		getJavaBuilder().binaryResources = new SimpleLookupTable();
    		SimpleLookupTable deltas = getDeltas(buildConfig);
    	 
    	 	//MessageUtil.info(messageHandler, "about to do incremental build: " + deltas);
    	 
    		boolean succeeded = builder.build(deltas);
    		
       		if (counter.hasErrors()) {
                return false;
            }
    		
       		if (succeeded) {
    			return weaveAndGenerateClassFiles(builder.getNewState());
    		} else {
    			return batchBuild(buildConfig, messageHandler);
    		}
        } finally {
            currentHandler = null;
        }
    
	}
    
	SimpleLookupTable getDeltas(AjBuildConfig newBuildConfig) {	  
		updateBuildConfig(newBuildConfig);
		//System.err.println("sourceRoots: " + newBuildConfig.getSourceRoots());
		// !!! support multiple source roots
		SimpleLookupTable deltas = new SimpleLookupTable();
		for (Iterator i = newBuildConfig.getSourceRoots().iterator(); i.hasNext(); ) {
			makeDeltas(deltas, getModifiedFiles(), deletedFiles, ((File)i.next()).getAbsolutePath());
		}
		return deltas;
	}
	
	void makeDeltas(SimpleLookupTable deltas, Collection modifiedAndAdded, Collection deletedFiles, String sourcePath) {

		IProject project = javaBuilder.currentProject; //???

		List deltaChildren = new ArrayList();
		//??? do we need to distinguish added from modified
//		for (Iterator it = addedFiles.iterator(); it.hasNext(); ) {
//			File addedFile = (File)it.next();
//			CommandLineResourceDelta addedDelta = new CommandLineResourceDelta(
//				new Path(addedFile.getAbsolutePath()), null);
//			addedDelta.setKind(IResourceDelta.ADDED);
//			deltaChildren.add(addedDelta);
//		}

		for (Iterator it = deletedFiles.iterator(); it.hasNext(); ) {
			File addedFile = (File)it.next();
			CommandLineResourceDelta addedDelta = new CommandLineResourceDelta(
				new FilesystemFile(addedFile.getAbsolutePath()));
			addedDelta.setKind(IResourceDelta.REMOVED);
			deltaChildren.add(addedDelta);
		}

		for (Iterator it = modifiedAndAdded.iterator(); it.hasNext(); ) {
			File addedFile = (File)it.next();
			CommandLineResourceDelta addedDelta = new CommandLineResourceDelta(
				new FilesystemFile(addedFile.getAbsolutePath()));
			addedDelta.setKind(IResourceDelta.CHANGED);
			deltaChildren.add(addedDelta);
		}

		CommandLineResourceDelta delta = new CommandLineResourceDelta(
			new FilesystemFile(sourcePath));
		delta.setKind(IResourceDelta.CHANGED);
		delta.setChildren((ResourceDelta[])deltaChildren.toArray(new ResourceDelta[deltaChildren.size()]));
		deltas.put(project,delta);
	}
	
	void updateBuildConfig(AjBuildConfig newBuildConfig) {
		Set oldFiles = new HashSet(buildConfig.getFiles());
		Set newFiles = new HashSet(newBuildConfig.getFiles());
		
		addedFiles = new HashSet(newFiles);
		addedFiles.removeAll(oldFiles);
		deletedFiles = new HashSet(oldFiles);
		deletedFiles.removeAll(newFiles);
		setBuildConfig(newBuildConfig);		
	}	
		
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
	
	//??? do this well
	private void initJavaBuilder(IMessageHandler handler) {
		javaBuilder = new JavaBuilder();
		javaBuilder.currentProject = DEFAULT_PROJECT;
		if (buildConfig.getOutputDir() == null) {
			//XXX must handle this case better to get javac output compatibility
			javaBuilder.outputFolder = new FilesystemFolder(new Path("."));
		} else {
		    javaBuilder.outputFolder = new FilesystemFolder(new Path(buildConfig.getOutputDir().getPath())); //outputDir.getPath()));
		}
		classFileCache = new ClassFileCache(javaBuilder.outputFolder, handler);
		javaBuilder.outputFolder = classFileCache;
		
		javaBuilder.sourceFolders = new IContainer[0];

		javaBuilder.classpath = makeClasspathLocations();
		
		//XXX override build notifier to get progress info
		if (buildNotifier == null) buildNotifier = new BuildNotifier(null, javaBuilder.currentProject);
		javaBuilder.notifier = buildNotifier;
	}
	
	public static String[] getFilenames(List files) {
		int len = files.size();
		String[] ret = new String[len];
		for (int i=0; i < len; i++) {
			File file = (File)files.get(i);
			ret[i] = file.getPath();
		}
		return ret;
	}

	public static String[] getInitialTypeNames(List files) {
		int len = files.size();
		String[] ret = new String[len];
		for (int i=0; i < len; i++) {
			File file = (File)files.get(i);
			String name = file.getName();
			int dot = name.indexOf('.'); //XXX what if there's no '.'
			ret[i] = name.substring(0, dot);
		}
		return ret;
	}
	
	void addAspectClassFilesToWeaver() throws IOException {
		//System.out.println("added or changed: " + classFileCache.getAddedOrChanged());
		
		for (Iterator i = classFileCache.getAddedOrChanged().iterator(); i.hasNext(); ) {
			UnwovenClassFile classFile = (UnwovenClassFile) i.next();
			bcelWeaver.addClassFile(classFile);
		}
		for (Iterator i = classFileCache.getDeleted().iterator(); i.hasNext(); ) {
			UnwovenClassFile classFile = (UnwovenClassFile) i.next();
			bcelWeaver.deleteClassFile(classFile.getClassName());
			classFile.deleteRealFile();
		}
	}

	public boolean weaveAndGenerateClassFiles(State newState) throws IOException, CoreException {
		currentHandler.handleMessage(MessageUtil.info("weaving"));
		javaBuilder.lastState = newState;
		addAspectClassFilesToWeaver();
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
		return true;
        //return messageAdapter.getErrorCount() == 0; //!javaBuilder.notifier.anyErrors();
	}
    
    /** TEST-ONLY access to builder */
    JavaBuilder getJavaBuilder() {
        return javaBuilder;
    }

	private void setBuildConfig(AjBuildConfig buildConfig) {
		this.buildConfig = buildConfig;
		
		// clear previous error info and incremental info when build config is set
		if (javaBuilder != null) {
			javaBuilder.notifier = new BuildNotifier(null, javaBuilder.currentProject);
		}
		if (classFileCache != null) {
			classFileCache.resetIncrementalInfo();
		}
	}
	
	private Collection getModifiedFiles() {		
		return getModifiedFiles(javaBuilder.lastState.lastStructuralBuildTime);
	}

	Collection getModifiedFiles(long lastBuildTime) {
		List ret = new ArrayList();
		//not our job to account for new and deleted files
		for (Iterator i = buildConfig.getFiles().iterator(); i.hasNext(); ) {
			File file = (File)i.next();
			
			long modTime = file.lastModified();
			//System.out.println("check: " + file + " mod " + modTime + " build " + lastBuildTime);			
			if (modTime > lastBuildTime) {
				ret.add(file);
			} 
		}
		return ret;
	}	
	
	ClasspathLocation makeClasspathLocation(String entry) {
		if (entry.endsWith(".jar") || entry.endsWith(".zip")) {
			return ClasspathLocation.forLibrary(entry);
		} else {
			return ClasspathLocation.forBinaryFolder(entry);
		}
	}
	
	ClasspathLocation makeUnwovenClassfileLocation(IContainer container) {
		return new ClasspathContainer(container);
	}
	
	/**
	 * Adds in this order<UL>
	 * skip this for now, incremental may need some version<LI>magic source path
	 * <LI>output path
	 * <LI>classpath 
	 * </UL>
	 */
	ClasspathLocation[] makeClasspathLocations() {
		List locations = new ArrayList();

		locations.add(makeUnwovenClassfileLocation(classFileCache));
		
		//System.out.println("full classpath: " + buildConfig.getFullClasspath());
		
		for (Iterator it = buildConfig.getFullClasspath().iterator(); it.hasNext(); ) {
			locations.add(makeClasspathLocation((String)it.next()));	
		}		
		return (ClasspathLocation[])locations.toArray(new ClasspathLocation[locations.size()]);
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
	

	/**
	 * Just like AbstractImageBuilder.newCompiler, except doesn't use JavaCore
	 */
	Compiler makeCompiler( // XXX restore private
        final AbstractImageBuilder builder, 
        NameEnvironment nameEnvironment,
        IMessageHandler handler) {
		// called once when the builder is initialized... can override if needed
		AjCompiler compiler = new AjCompiler(
			nameEnvironment,
			DefaultErrorHandlingPolicies.proceedWithAllProblems(),
			buildConfig.getJavaOptions(),
			builder,
			ProblemFactory.getProblemFactory(Locale.getDefault()));
			
			
		AjProblemReporter pr = new AjProblemReporter(DefaultErrorHandlingPolicies.proceedWithAllProblems(),
			compiler.options, ProblemFactory.getProblemFactory(Locale.getDefault()));
		
		compiler.problemReporter = pr;
			
		AjLookupEnvironment le =
			new AjLookupEnvironment(compiler, compiler.options,
									pr, nameEnvironment);
		EclipseWorld ew = new EclipseWorld(le, handler);
		ew.setLint(bcelWorld.getLint());
		ew.setXnoInline(buildConfig.isXnoInline());
		le.world = ew;
		pr.world = ew;
		le.world.buildManager = this;
		
		compiler.lookupEnvironment = le;
		
		compiler.parser =
			new AjParser(
				pr, 
				compiler.options.parseLiteralExpressionsAsConstants, 
				compiler.options.assertMode);
				
				
		
		//EclipseWorld world = EclipseWorld.forLookupEnvironment(compiler.lookupEnvironment);
		return compiler;
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

	public void setBuildNotifier(BuildNotifier notifier) {
		buildNotifier = notifier;
	}
    
    /** callback for builders used only during build */
    private boolean handleProblem(ICompilationUnit unit, IProblem problem) { // XXX
        IMessageHandler handler = currentHandler;
        if (null == handler) {
            throw new IllegalStateException("no current handler when handling "
                + problem + " in " + unit);
        }
        IMessage message = EclipseAdapterUtils.makeMessage(unit, problem);
        return handler.handleMessage(message);
    }

    /** initialization for testing purposes */
    boolean testInit(IMessageHandler handler) {
        try {
            initBcelWorld(handler);
            initJavaBuilder(handler);
        } catch (Throwable e) {
            return false;
        }
        return true;
    }
    /** invasive test has to set handler */
    void testSetHandler(IMessageHandler handler) {
        currentHandler = handler;
    }

    // ----------------------------------------------------
    
    /** build batch command; alive during batchBuild(...) only */
    private class BatchBuilder  extends BatchImageBuilder {
        boolean initialized;
        boolean running;
    
        public BatchBuilder(JavaBuilder builder, IMessageHandler handler) {
            super(builder);
            this.compiler = makeCompiler(this, nameEnvironment, handler);
        }
    
        /**
         * XXXwe'll want to preload aspects from projects or library .jars
         * XXXthat we depend on
         * 
         * compile all files and store in generatedClassFiles
         * then collect aspects
         * then weave into generatedClassFiles and dump to disk
         * 
         * a possible optimization is to generate all aspects first, it's unclear
         * that this is worth the effort or loss of clarity
         */
        public State run() throws IOException, CoreException {
            compile(getFilenames(buildConfig.getFiles()), 
                    getInitialTypeNames(buildConfig.getFiles()));
            return newState;
        }
    
        /**
         * Only used to get source files
         * 
         * XXX verify this rigourously
         */
        protected IResource resourceForLocation(String sourceLocation) {
            return new FilesystemFile(new Path(sourceLocation));
        }
        
        protected void createErrorFor(IResource resource, String message) {
            //XXX don't think there's anything to do here
        }
        
        protected void updateProblemsFor(
            String sourceLocation,
            CompilationResult result)
            throws CoreException {
            IProblem[] problems = result.getProblems();
            if (problems == null || problems.length == 0) return;
    
            notifier.updateProblemCounts(problems);
            for (int i=0, len=problems.length; i < len; i++) {
                handleProblem(result.getCompilationUnit(), problems[i]);
            }
        }
        
        /**
         * We can't create a compiler until after our super's constructor has run
         * We will fill in the right value then.
         */
        protected Compiler newCompiler() { // XXX unimplemented??
            return null;
        }
        
        protected SourceFile makeSourceFile(String filename, String initialTypeName) {
            SourceFile sourceFile = super.makeSourceFile(filename, initialTypeName);
            sourceFile.packageName = null;  // tells eclipse that we don't know this package
            return sourceFile;
        }
    }  // class BatchBuilder
    
    class IncrementalBuilder extends IncrementalImageBuilder {
    
        public IncrementalBuilder(JavaBuilder builder, IMessageHandler handler) {
            super(builder);
            this.compiler = makeCompiler(this, nameEnvironment, handler);
        }
    
    
        List getLocations() {
            return locations;
        }
    
        public State getNewState() {
            return newState;
        }
        
        public boolean build(SimpleLookupTable deltas) {
            return super.build(deltas);
        }
        
        /**
         * @param changedFiles includes both new files and changed files
         */
        public boolean build(Collection changedFiles, Collection deletedFiles) {
    
            // initialize builder
            // walk this project's deltas, find changed source files
            // walk prereq projects' deltas, find changed class files & add affected source files
            //   use the build state # to skip the deltas for certain prereq projects
            //   ignore changed zip/jar files since they caused a full build
            // compile the source files & acceptResult()
            // compare the produced class files against the existing ones on disk
            // recompile all dependent source files of any type with structural changes or new/removed secondary type
            // keep a loop counter to abort & perform a full build
        
            if (JavaBuilder.DEBUG)
                System.out.println("INCREMENTAL build"); //$NON-NLS-1$
        
            try {
                resetCollections();
        
                notifier.subTask(Util.bind("build.analyzingDeltas")); //$NON-NLS-1$
                if (!addFileLocations(changedFiles, deletedFiles)) return false;
                
                //XXX deal with deleted files here too
                
                
    //          IResourceDelta sourceDelta = (IResourceDelta) deltas.get(javaBuilder.currentProject);
    //          if (sourceDelta != null)
    //              if (!findSourceFiles(sourceDelta)) return false;
    //          notifier.updateProgressDelta(0.10f);
        
    //          Object[] keyTable = deltas.keyTable;
    //          Object[] valueTable = deltas.valueTable;
    //          for (int i = 0, l = keyTable.length; i < l; i++) {
    //              IResourceDelta delta = (IResourceDelta) valueTable[i];
    //              if (delta != null) {
    //                  IResource[] binaryResources = (IResource[]) javaBuilder.binaryResources.get(keyTable[i]);
    //                  if (binaryResources != null)
    //                      if (!findAffectedSourceFiles(delta, binaryResources)) return false;
    //              }
    //          }
    //          notifier.updateProgressDelta(0.10f);
        
                notifier.subTask(Util.bind("build.analyzingSources")); //$NON-NLS-1$
                addAffectedSourceFiles();
                notifier.updateProgressDelta(0.05f);
        
                int compileLoop = 0;
                float increment = 0.40f;
                while (locations.size() > 0) { // added to in acceptResult
                    if (++compileLoop > MaxCompileLoop) {
                        if (JavaBuilder.DEBUG)
                            System.out.println("ABORTING incremental build... exceeded loop count"); //$NON-NLS-1$
                        return false;
                    }
                    notifier.checkCancel();
        
                    String[] allSourceFiles = new String[locations.size()];
                    locations.toArray(allSourceFiles);
                    String[] initialTypeStrings = new String[typeNames.size()];
                    typeNames.toArray(initialTypeStrings);
                    resetCollections();
        
                    workQueue.addAll(allSourceFiles);
                    notifier.setProgressPerCompilationUnit(increment / allSourceFiles.length);
                    increment = increment / 2;
                    compile(allSourceFiles, initialTypeStrings);
                    //removeSecondaryTypes();
                    addAffectedSourceFiles();
                }
            } catch (AbortIncrementalBuildException e) {
                // abort the incremental build and let the batch builder handle the problem
                if (JavaBuilder.DEBUG)
                    System.out.println("ABORTING incremental build... cannot find " + e.qualifiedTypeName + //$NON-NLS-1$
                        ". Could have been renamed inside its existing source file."); //$NON-NLS-1$
                return false;
    //      } catch (CoreException e) {
    //          throw internalException(e);
            } finally {
                cleanUp();
            }
            return true;
        }
    
        public void setSourceFolders(IContainer[] sourceFolders) {
            super.sourceFolders = sourceFolders;    
        }
    
        public IContainer[] getSourceFolders() {
            return sourceFolders;   
        }
    
        private boolean addFileLocations(Collection changedFiles, Collection deletedFiles) {
            addDeletedFiles(deletedFiles);
            
            for (Iterator i = changedFiles.iterator(); i.hasNext(); ) {
                File file = (File)i.next();
                locations.add(getSourceLocation(file));
                typeNames.add(file.getName().substring(0, file.getName().length()-5));
            }
            return changedFiles.size() > 0 || deletedFiles.size() > 0;
        }
    
        private String getSourceLocation(File file) {
            return file.getPath().replace('\\', '/');
        }
    
        
        /**
         * @see IncrementalImageBuilder.findSourceFiles
         */
        private void addDeletedFiles(Collection deletedFiles) {
        }
    //      for (Iterator i = deletedFiles.iterator(); i.hasNext(); ) {
    //          File file = (File)i.next();
    //          String sourceLocation = getSourceLocation(file);
    //          char[][] definedTypeNames = newState.getDefinedTypeNamesFor(sourceLocation);
    //          
    //          if (definedTypeNames == null) { // defined a single type matching typePath
    //              removeClassFile(typePath);
    //          } else {
    //              if (JavaBuilder.DEBUG)
    //                  System.out.println("Add dependents of removed source file " + typePath.toString()); //$NON-NLS-1$
    //              addDependentsOf(typePath, true); // add dependents of the source file since it may be involved in a name collision
    //              if (definedTypeNames.length > 0) { // skip it if it failed to successfully define a type
    //                  IPath packagePath = typePath.removeLastSegments(1);
    //                  for (int i = 0, length = definedTypeNames.length; i < length; i++)
    //                      removeClassFile(packagePath.append(new String(definedTypeNames[i])));
    //              }
    //          }
    //          newState.remove(sourceLocation);
    //      }
        
    
    //  public void acceptResult(CompilationResult result) {
    //      System.out.println("result: " + result);
    //      buildManager.noteRecompiled(new File(new String(result.fileName)));
    //      //newState.dump();
    //      super.acceptResult(result);
    //      //newState.dump();
    //  }
    
        protected IResource resourceForLocation(String sourceLocation) {
            return new FilesystemFile(new Path(sourceLocation));
        }
    
        protected void createErrorFor(IResource resource, String message) {
            //XXX don't think there's anything to do here
        }
    
        protected void updateProblemsFor(
            String sourceLocation,
            CompilationResult result)
            throws CoreException {
            IProblem[] problems = result.getProblems();
            if (problems == null || problems.length == 0) return;
    
            notifier.updateProblemCounts(problems);
            for (int i=0, len=problems.length; i < len; i++) {
                handleProblem(result.getCompilationUnit(), problems[i]);
            }
        }
    
    
        protected String extractTypeNameFrom(String sourceLocation) {
            File file = new File(sourceLocation);
            return file.getName().substring(0, file.getName().length()-5);
        }
        
        /**
         * We can't create a compiler until after our super's constructor has run
         * We will fill in the right value then.
         */
        protected Compiler newCompiler() {
            return null;
        }
        
        protected SourceFile makeSourceFile(String filename, String initialTypeName) {
            SourceFile sourceFile = super.makeSourceFile(filename, initialTypeName);
            sourceFile.packageName = null;  // tells eclipse that we don't know this package
            return sourceFile;
        }
    
    }   // class IncrementalBuilder

}   // class AjBuildManager

