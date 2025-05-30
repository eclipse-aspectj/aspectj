/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *
 * Contributors:
 *     PARC     initial implementation
 * ******************************************************************/

package org.aspectj.ajdt.internal.core.builder;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

import org.aspectj.ajdt.ajc.BuildArgParser;
import org.aspectj.ajdt.internal.compiler.AjCompilerAdapter;
import org.aspectj.ajdt.internal.compiler.AjPipeliningCompilerAdapter;
import org.aspectj.ajdt.internal.compiler.CompilationResultDestinationManager;
import org.aspectj.ajdt.internal.compiler.IBinarySourceProvider;
import org.aspectj.ajdt.internal.compiler.ICompilerAdapter;
import org.aspectj.ajdt.internal.compiler.ICompilerAdapterFactory;
import org.aspectj.ajdt.internal.compiler.IIntermediateResultsRequestor;
import org.aspectj.ajdt.internal.compiler.IOutputClassFileNameProvider;
import org.aspectj.ajdt.internal.compiler.InterimCompilationResult;
import org.aspectj.ajdt.internal.compiler.lookup.AjLookupEnvironment;
import org.aspectj.ajdt.internal.compiler.lookup.AnonymousClassPublisher;
import org.aspectj.ajdt.internal.compiler.lookup.EclipseFactory;
import org.aspectj.ajdt.internal.compiler.problem.AjProblemReporter;
import org.aspectj.asm.AsmManager;
import org.aspectj.asm.IHierarchy;
import org.aspectj.asm.IProgramElement;
import org.aspectj.asm.internal.ProgramElement;
import org.aspectj.bridge.AbortException;
import org.aspectj.bridge.CountingMessageHandler;
import org.aspectj.bridge.ILifecycleAware;
import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.IMessageHandler;
import org.aspectj.bridge.IProgressListener;
import org.aspectj.bridge.ISourceLocation;
import org.aspectj.bridge.Message;
import org.aspectj.bridge.MessageUtil;
import org.aspectj.bridge.SourceLocation;
import org.aspectj.bridge.Version;
import org.aspectj.bridge.context.CompilationAndWeavingContext;
import org.aspectj.bridge.context.ContextFormatter;
import org.aspectj.bridge.context.ContextToken;
import org.aspectj.org.eclipse.jdt.core.compiler.CharOperation;
import org.aspectj.org.eclipse.jdt.core.compiler.IProblem;
import org.aspectj.org.eclipse.jdt.internal.compiler.ClassFile;
import org.aspectj.org.eclipse.jdt.internal.compiler.CompilationResult;
import org.aspectj.org.eclipse.jdt.internal.compiler.DefaultErrorHandlingPolicies;
import org.aspectj.org.eclipse.jdt.internal.compiler.ICompilerRequestor;
import org.aspectj.org.eclipse.jdt.internal.compiler.IErrorHandlingPolicy;
import org.aspectj.org.eclipse.jdt.internal.compiler.IProblemFactory;
import org.aspectj.org.eclipse.jdt.internal.compiler.batch.ClasspathLocation;
import org.aspectj.org.eclipse.jdt.internal.compiler.batch.CompilationUnit;
import org.aspectj.org.eclipse.jdt.internal.compiler.batch.FileSystem;
import org.aspectj.org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.aspectj.org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.aspectj.org.eclipse.jdt.internal.compiler.env.IModule;
import org.aspectj.org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.aspectj.org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.aspectj.org.eclipse.jdt.internal.compiler.parser.Parser;
import org.aspectj.org.eclipse.jdt.internal.compiler.problem.AbortCompilation;
import org.aspectj.org.eclipse.jdt.internal.compiler.problem.DefaultProblemFactory;
import org.aspectj.tools.ajc.Main;
import org.aspectj.util.FileUtil;
import org.aspectj.weaver.CustomMungerFactory;
import org.aspectj.weaver.Dump;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.World;
import org.aspectj.weaver.bcel.BcelWeaver;
import org.aspectj.weaver.bcel.BcelWorld;
import org.aspectj.weaver.bcel.UnwovenClassFile;
import org.eclipse.core.runtime.OperationCanceledException;

public class AjBuildManager implements IOutputClassFileNameProvider, IBinarySourceProvider, ICompilerAdapterFactory {
	private static final String CROSSREFS_FILE_NAME = "build.lst";
	private static final String CANT_WRITE_RESULT = "unable to write compilation result";
	private static final String MANIFEST_NAME = "META-INF/MANIFEST.MF";
	public static boolean COPY_INPATH_DIR_RESOURCES = false;
	// AJDT doesn't want this check, so Main enables it.
	private static boolean DO_RUNTIME_VERSION_CHECK = false;
	// If runtime version check fails, warn or fail? (unset?)
	static final boolean FAIL_IF_RUNTIME_NOT_FOUND = false;

	private static final FileFilter binarySourceFilter = new FileFilter() {
		@Override
		public boolean accept(File f) {
			return f.getName().endsWith(".class");
		}
	};

	/**
	 * This builder is static so that it can be subclassed and reset. However, note that there is only one builder present, so if
	 * two extendsion reset it, only the latter will get used.
	 */
	public static AsmHierarchyBuilder asmHierarchyBuilder = new AsmHierarchyBuilder();

	static {
		// CompilationAndWeavingContext.setMultiThreaded(false);
		CompilationAndWeavingContext.registerFormatter(CompilationAndWeavingContext.BATCH_BUILD, new AjBuildContexFormatter());
		CompilationAndWeavingContext
				.registerFormatter(CompilationAndWeavingContext.INCREMENTAL_BUILD, new AjBuildContexFormatter());
	}

	private IProgressListener progressListener = null;

	private boolean environmentSupportsIncrementalCompilation = false;
	private int compiledCount;
	private int sourceFileCount;

	private JarOutputStream zos;
	private boolean batchCompile = true;
	private INameEnvironment environment;

	private Map<String, List<UnwovenClassFile>> /* String -> List<UCF> */binarySourcesForTheNextCompile = new HashMap<>();

	// FIXME asc should this really be in here?
	// private AsmManager structureModel;
	public AjBuildConfig buildConfig;
	private boolean ignoreOutxml;
	private boolean wasFullBuild = true; // true if last build was a full build rather than an incremental build

	AjState state = new AjState(this);

	/**
	 * Enable check for runtime version, used only by Ant/command-line Main.
	 *
	 * @param caller Main unused except to limit to non-null clients.
	 */
	public static void enableRuntimeVersionCheck(Main caller) {
		DO_RUNTIME_VERSION_CHECK = null != caller;
	}

	public BcelWeaver getWeaver() {
		return state.getWeaver();
	}

	public BcelWorld getBcelWorld() {
		return state.getBcelWorld();
	}

	public CountingMessageHandler handler;
	private CustomMungerFactory customMungerFactory;

	public AjBuildManager(IMessageHandler holder) {
		super();
		this.handler = CountingMessageHandler.makeCountingMessageHandler(holder);
	}

	public void environmentSupportsIncrementalCompilation(boolean itDoes) {
		this.environmentSupportsIncrementalCompilation = itDoes;
		if (itDoes) {
			org.aspectj.weaver.loadtime.definition.DocumentParser.deactivateCaching();
		}
	}

	/** @return true if we should generate a model as a side-effect */
	public boolean doGenerateModel() {
		return buildConfig.isGenerateModelMode();
	}

	public boolean batchBuild(AjBuildConfig buildConfig, IMessageHandler baseHandler) throws IOException, AbortException {
		return performBuild(buildConfig, baseHandler, true);
	}

	public boolean incrementalBuild(AjBuildConfig buildConfig, IMessageHandler baseHandler) throws IOException, AbortException {
		return performBuild(buildConfig, baseHandler, false);
	}

	/**
	 * Perform a build.
	 *
	 * @return true if the build was successful (ie. no errors)
	 */
	private boolean performBuild(AjBuildConfig buildConfig, IMessageHandler baseHandler, boolean isFullBuild) throws IOException,
			AbortException {
		boolean ret = true;
		batchCompile = isFullBuild;
		wasFullBuild = isFullBuild;
		if (baseHandler instanceof ILifecycleAware) {
			((ILifecycleAware) baseHandler).buildStarting(!isFullBuild);
		}
		CompilationAndWeavingContext.reset();
		final int phase = isFullBuild ? CompilationAndWeavingContext.BATCH_BUILD : CompilationAndWeavingContext.INCREMENTAL_BUILD;
		final ContextToken ct = CompilationAndWeavingContext.enteringPhase(phase, buildConfig);
		try {
			if (isFullBuild) {
				this.state = new AjState(this);
			}

			this.state.setCouldBeSubsequentIncrementalBuild(this.environmentSupportsIncrementalCompilation);

			final boolean canIncremental = state.prepareForNextBuild(buildConfig);
			if (!canIncremental && !isFullBuild) { // retry as batch?
				CompilationAndWeavingContext.leavingPhase(ct);
				if (state.listenerDefined()) {
					state.getListener().recordDecision("Falling back to batch compilation");
				}
				return performBuild(buildConfig, baseHandler, true);
			}
			this.handler = CountingMessageHandler.makeCountingMessageHandler(baseHandler);

			if (buildConfig == null || buildConfig.isCheckRuntimeVersion()) {
				if (DO_RUNTIME_VERSION_CHECK) {
					final String check = checkRtJar(buildConfig);
					if (check != null) {
						if (FAIL_IF_RUNTIME_NOT_FOUND) {
							MessageUtil.error(handler, check);
							CompilationAndWeavingContext.leavingPhase(ct);
							return false;
						} else {
							MessageUtil.warn(handler, check);
						}
					}
				}
			}

			// if (batch) {
			setBuildConfig(buildConfig);
			// }
			if (isFullBuild || !AsmManager.attemptIncrementalModelRepairs) {
				// if (buildConfig.isEmacsSymMode() || buildConfig.isGenerateModelMode()) {
				setupModel(buildConfig);
				// }
			}
			if (isFullBuild) {
				initBcelWorld(handler);
			}

			if (handler.hasErrors()) {
				CompilationAndWeavingContext.leavingPhase(ct);
				return false;
			}

			if (buildConfig.getOutputJar() != null) {
				if (!openOutputStream(buildConfig.getOutputJar())) {
					CompilationAndWeavingContext.leavingPhase(ct);
					return false;
				}
			}

			if (isFullBuild) {
				// System.err.println("XXXX batch: " + buildConfig.getFiles());
				if (buildConfig.isEmacsSymMode() || buildConfig.isGenerateModelMode()) {
					AsmManager.setLastActiveStructureModel(state.getStructureModel());
					getWorld().setModel(state.getStructureModel());
					// in incremental build, only get updated model?
				}
				binarySourcesForTheNextCompile = state.getBinaryFilesToCompile(true);
				performCompilation(buildConfig.getFiles());
				state.clearBinarySourceFiles(); // we don't want these hanging around...
				if (!proceedOnError() && handler.hasErrors()) {
					CompilationAndWeavingContext.leavingPhase(ct);
					if (AsmManager.isReporting()) {
						state.getStructureModel().reportModelInfo("After a batch build");
					}
					return false;
				}

				if (AsmManager.isReporting()) {
					state.getStructureModel().reportModelInfo("After a batch build");
				}

			} else {
				// done already?
				// if (buildConfig.isEmacsSymMode() || buildConfig.isGenerateModelMode()) {
				// bcelWorld.setModel(StructureModelManager.INSTANCE.getStructureModel());
				// }
				// System.err.println("XXXX start inc ");
				AsmManager.setLastActiveStructureModel(state.getStructureModel());
				binarySourcesForTheNextCompile = state.getBinaryFilesToCompile(true);
				Set<File> files = state.getFilesToCompile(true);
				if (buildConfig.isEmacsSymMode() || buildConfig.isGenerateModelMode()) {
					if (AsmManager.attemptIncrementalModelRepairs) {
						state.getStructureModel().resetDeltaProcessing();
						state.getStructureModel().processDelta(files, state.getAddedFiles(), state.getDeletedFiles());
					}
				}
				boolean hereWeGoAgain = !(files.isEmpty() && binarySourcesForTheNextCompile.isEmpty());
				for (int i = 0; (i < 5) && hereWeGoAgain; i++) {
					if (state.listenerDefined()) {
						state.getListener()
								.recordInformation("Starting incremental compilation loop " + (i + 1) + " of possibly 5");
						// System.err.println("XXXX inc: " + files);
					}

					performCompilation(files);
					if ((!proceedOnError() && handler.hasErrors())
							|| (progressListener != null && progressListener.isCancelledRequested())) {
						CompilationAndWeavingContext.leavingPhase(ct);
						return false;
					}

					if (state.requiresFullBatchBuild()) {
						if (state.listenerDefined()) {
							state.getListener().recordInformation(" Dropping back to full build");
						}
						return batchBuild(buildConfig, baseHandler);
					}

					binarySourcesForTheNextCompile = state.getBinaryFilesToCompile(false);
					files = state.getFilesToCompile(false);
					hereWeGoAgain = !(files.isEmpty() && binarySourcesForTheNextCompile.isEmpty());
					// TODO Andy - Needs some thought here...
					// I think here we might want to pass empty addedFiles/deletedFiles as they were
					// dealt with on the first call to processDelta - we are going through this loop
					// again because in compiling something we found something else we needed to
					// rebuild. But what case causes this?
					if (hereWeGoAgain) {
						if (buildConfig.isEmacsSymMode() || buildConfig.isGenerateModelMode()) {
							if (AsmManager.attemptIncrementalModelRepairs) {
								state.getStructureModel().processDelta(files, state.getAddedFiles(), state.getDeletedFiles());
							}
						}
					}
				}
				if (!files.isEmpty()) {
					CompilationAndWeavingContext.leavingPhase(ct);
					return batchBuild(buildConfig, baseHandler);
				} else {
					if (AsmManager.isReporting()) {
						state.getStructureModel().reportModelInfo("After an incremental build");
					}
				}
			}

			// XXX not in Mik's incremental
			if (buildConfig.isEmacsSymMode()) {
				new org.aspectj.ajdt.internal.core.builder.EmacsStructureModelManager().externalizeModel(state.getStructureModel());
			}

			// for bug 113554: support ajsym file generation for command line builds
			if (buildConfig.isGenerateCrossRefsMode()) {
				final File configFileProxy = new File(buildConfig.getOutputDir(), CROSSREFS_FILE_NAME);
				state.getStructureModel().writeStructureModel(configFileProxy.getAbsolutePath());
			}

			// have to tell state we succeeded or next is not incremental
			state.successfulCompile(buildConfig, isFullBuild);

			// For a full compile, copy resources to the destination
			// - they should not get deleted on incremental and AJDT
			// will handle changes to them that require a recopying
			if (isFullBuild) {
				copyResourcesToDestination();
			}

			if (buildConfig.getOutxmlName() != null) {
				writeOutxmlFile();
			}

			/* boolean weaved = */// weaveAndGenerateClassFiles();
			// if not weaved, then no-op build, no model changes
			// but always returns true
			// XXX weaved not in Mik's incremental
			if (buildConfig.isGenerateModelMode()) {
				state.getStructureModel().fireModelUpdated();
			}
			CompilationAndWeavingContext.leavingPhase(ct);

		} finally {
			if (baseHandler instanceof ILifecycleAware) {
				((ILifecycleAware) baseHandler).buildFinished(!isFullBuild);
			}
			if (zos != null) {
				closeOutputStream(buildConfig.getOutputJar());
			}
			ret = !handler.hasErrors();
			if (getBcelWorld() != null) {
				final BcelWorld bcelWorld = getBcelWorld();
				bcelWorld.reportTimers();
				bcelWorld.tidyUp();
			}
			if (getWeaver() != null) {
				getWeaver().tidyUp();
				// bug 59895, don't release reference to handler as may be needed by a nested call
				// handler = null;
			}
		}
		return ret;
	}

	/**
	 * Open an output jar file in which to write the compiler output.
	 *
	 * @param outJar the jar file to open
	 * @return true if successful
	 */
	private boolean openOutputStream(File outJar) {
		try {
			OutputStream os = FileUtil.makeOutputStream(buildConfig.getOutputJar());
			zos = new JarOutputStream(os, getWeaver().getManifest(true));
		} catch (IOException ex) {
			IMessage message = new Message("Unable to open outjar " + outJar.getPath() + "(" + ex.getMessage() + ")",
					new SourceLocation(outJar, 0), true);
			handler.handleMessage(message);
			return false;
		}
		return true;
	}

	private void closeOutputStream(File outJar) {
		try {
			if (zos != null) {
				zos.close();
				if (buildConfig.getCompilationResultDestinationManager() != null) {
					buildConfig.getCompilationResultDestinationManager().reportFileWrite(outJar.getPath(),
							CompilationResultDestinationManager.FILETYPE_OUTJAR);
				}
			}
			zos = null;

			/* Ensure we don't write an incomplete JAR bug-71339 */
			if (handler.hasErrors()) {
				outJar.delete();
				if (buildConfig.getCompilationResultDestinationManager() != null) {
					buildConfig.getCompilationResultDestinationManager().reportFileRemove(outJar.getPath(),
							CompilationResultDestinationManager.FILETYPE_OUTJAR);
				}
			}
		} catch (IOException ex) {
			IMessage message = new Message("Unable to write outjar " + outJar.getPath() + "(" + ex.getMessage() + ")",
					new SourceLocation(outJar, 0), true);
			handler.handleMessage(message);
		}
	}

	private void copyResourcesToDestination() throws IOException {
		// resources that we need to copy are contained in the injars and inpath only
        for (File inJar : buildConfig.getInJars()) {
            copyResourcesFromJarFile(inJar);
        }

        for (File inPathElement : buildConfig.getInpath()) {
            if (inPathElement.isDirectory()) {
                copyResourcesFromDirectory(inPathElement);
            }
            else {
                copyResourcesFromJarFile(inPathElement);
            }
        }

		if (buildConfig.getSourcePathResources() != null) {
            for (String resource : buildConfig.getSourcePathResources().keySet()) {
                File from = buildConfig.getSourcePathResources().get(resource);
                copyResourcesFromFile(from, resource, from);
            }
		}

		writeManifest();
	}

	private void copyResourcesFromJarFile(File jarFile) throws IOException {
		try (JarInputStream inStream = new JarInputStream(new FileInputStream(jarFile))) {
			while (true) {
				ZipEntry entry = inStream.getNextEntry();
				if (entry == null) {
					break;
				}

				String filename = entry.getName();
				// System.out.println("? copyResourcesFromJarFile() filename='" + filename +"'");
				if (entry.isDirectory()) {
					writeDirectory(filename, jarFile);
				} else if (acceptResource(filename, false)) {
					byte[] bytes = FileUtil.readAsByteArray(inStream);
					writeResource(filename, bytes, jarFile);
				}

				inStream.closeEntry();
			}
		}
	}

	private void copyResourcesFromDirectory(File dir) throws IOException {
		if (!COPY_INPATH_DIR_RESOURCES) {
			return;
		}
		// Get a list of all files (i.e. everything that isnt a directory)
		File[] files = FileUtil.listFiles(dir, new FileFilter() {
			@Override
			public boolean accept(File f) {
				boolean accept = !(f.isDirectory() || f.getName().endsWith(".class"));
				return accept;
			}
		});

		// For each file, add it either as a real .class file or as a resource
        for (File file : files) {
            // ASSERT: files[i].getAbsolutePath().startsWith(inFile.getAbsolutePath()
            // or we are in trouble...
            String filename = file.getAbsolutePath().substring(dir.getAbsolutePath().length() + 1);
            copyResourcesFromFile(file, filename, dir);
        }
	}

	private void copyResourcesFromFile(File f, String filename, File src) throws IOException {
		if (!acceptResource(filename, true)) {
			return;
		}
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(f);
			byte[] bytes = FileUtil.readAsByteArray(fis);
			// String relativePath = files[i].getPath();

			writeResource(filename, bytes, src);
		} catch (FileNotFoundException fnfe) {
			// pr359332: looks like the file moved (refactoring?) just as this copy was starting
			// that is OK
		} finally {
			if (fis != null) {
				fis.close();
			}
		}
	}

	/**
	 * Add a directory entry to the output zip file. Don't do anything if not writing out to a zip file. A directory entry is one
	 * whose filename ends with '/'
	 *
	 * @param directory the directory path
	 * @param srcloc the src of the directory entry, for use when creating a warning message
	 * @throws IOException if something goes wrong creating the new zip entry
	 */
	private void writeDirectory(String directory, File srcloc) throws IOException {
		if (state.hasResource(directory)) {
			IMessage msg = new Message("duplicate resource: '" + directory + "'", IMessage.WARNING, null, new SourceLocation(
					srcloc, 0));
			handler.handleMessage(msg);
			return;
		}
		if (zos != null) {
			ZipEntry newEntry = new ZipEntry(directory);
			zos.putNextEntry(newEntry);
			zos.closeEntry();
			state.recordResource(directory, srcloc);
		}
		// Nothing to do if not writing to a zip file
	}

	private void writeResource(String filename, byte[] content, File srcLocation) throws IOException {
		if (state.hasResource(filename)) {
			IMessage msg = new Message("duplicate resource: '" + filename + "'", IMessage.WARNING, null, new SourceLocation(
					srcLocation, 0));
			handler.handleMessage(msg);
			return;
		}
		if (filename.equals(buildConfig.getOutxmlName())) {
			ignoreOutxml = true;
			IMessage msg = new Message("-outxml/-outxmlfile option ignored because resource already exists: '" + filename + "'",
					IMessage.WARNING, null, new SourceLocation(srcLocation, 0));
			handler.handleMessage(msg);
		}
		if (zos != null) {
			ZipEntry newEntry = new ZipEntry(filename); // ??? get compression scheme right
			zos.putNextEntry(newEntry);
			zos.write(content);
			zos.closeEntry();
		} else {
			File destDir = buildConfig.getOutputDir();
			if (buildConfig.getCompilationResultDestinationManager() != null) {
				destDir = buildConfig.getCompilationResultDestinationManager().getOutputLocationForResource(srcLocation);
			}
			try {
				File outputLocation = new File(destDir, filename);
				OutputStream fos = FileUtil.makeOutputStream(outputLocation);
				fos.write(content);
				fos.close();
				if (buildConfig.getCompilationResultDestinationManager() != null) {
					buildConfig.getCompilationResultDestinationManager().reportFileWrite(outputLocation.getPath(),
							CompilationResultDestinationManager.FILETYPE_RESOURCE);
				}
			} catch (FileNotFoundException fnfe) {
				IMessage msg = new Message("unable to copy resource to output folder: '" + filename + "' - reason: "
						+ fnfe.getMessage(), IMessage.ERROR, null, new SourceLocation(srcLocation, 0));
				handler.handleMessage(msg);
			}
		}
		state.recordResource(filename, srcLocation);
	}

	/*
	 * If we are writing to an output directory copy the manifest but only if we already have one
	 */
	private void writeManifest() throws IOException {
		Manifest manifest = getWeaver().getManifest(false);
		if (manifest != null && zos == null) {
			File outputDir = buildConfig.getOutputDir();
			if (buildConfig.getCompilationResultDestinationManager() != null) {
				// Manifests are only written if we have a jar on the inpath. Therefore,
				// we write the manifest to the defaultOutputLocation because this is
				// where we sent the classes that were on the inpath
				outputDir = buildConfig.getCompilationResultDestinationManager().getDefaultOutputLocation();
			}
			if (outputDir == null) {
				return;
			}
			File outputLocation = new File(outputDir, MANIFEST_NAME);
			OutputStream fos = FileUtil.makeOutputStream(outputLocation);
			manifest.write(fos);
			fos.close();
			if (buildConfig.getCompilationResultDestinationManager() != null) {
				buildConfig.getCompilationResultDestinationManager().reportFileWrite(outputLocation.getPath(),
						CompilationResultDestinationManager.FILETYPE_RESOURCE);
			}
		}
	}

	private boolean acceptResource(String resourceName, boolean fromFile) {
		if ((resourceName.startsWith("CVS/")) || (resourceName.contains("/CVS/")) || (resourceName.endsWith("/CVS"))
				|| (resourceName.endsWith(".class")) || (resourceName.startsWith(".svn/"))
				|| (resourceName.contains("/.svn/")) || (resourceName.endsWith("/.svn")) ||
				// Do not copy manifests if either they are coming from a jar or we are writing to a jar
				(resourceName.toUpperCase().equals(MANIFEST_NAME) && (!fromFile || zos != null))) {
			return false;
		} else {
			return true;
		}
	}

	private void writeOutxmlFile() throws IOException {
		if (ignoreOutxml) {
			return;
		}

		String filename = buildConfig.getOutxmlName();
		// System.err.println("? AjBuildManager.writeOutxmlFile() outxml=" + filename);

		Map<File, List<String>> outputDirsAndAspects = findOutputDirsForAspects();
		Set<Map.Entry<File, List<String>>> outputDirs = outputDirsAndAspects.entrySet();
        for (Map.Entry<File, List<String>> entry : outputDirs) {
            File outputDir = entry.getKey();
            List<String> aspects = entry.getValue();
            ByteArrayOutputStream baos = getOutxmlContents(aspects);
            if (zos != null) {
                ZipEntry newEntry = new ZipEntry(filename);

                zos.putNextEntry(newEntry);
                zos.write(baos.toByteArray());
                zos.closeEntry();
            }
            else {
                File outputFile = new File(outputDir, filename);
                OutputStream fos = FileUtil.makeOutputStream(outputFile);
                fos.write(baos.toByteArray());
                fos.close();
                if (buildConfig.getCompilationResultDestinationManager() != null) {
                    buildConfig.getCompilationResultDestinationManager().reportFileWrite(outputFile.getPath(),
                            CompilationResultDestinationManager.FILETYPE_RESOURCE);
                }
            }
        }
	}

	private ByteArrayOutputStream getOutxmlContents(List<String> aspectNames) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(baos);
		ps.println("<aspectj>");
		ps.println("<aspects>");
		if (aspectNames != null) {
            for (String name : aspectNames) {
                ps.println("<aspect name=\"" + name + "\"/>");
            }
		}
		ps.println("</aspects>");
		ps.println("</aspectj>");
		ps.println();
		ps.close();
		return baos;
	}

	/**
	 * Returns a map where the keys are File objects corresponding to all the output directories and the values are a list of
	 * aspects which are sent to that ouptut directory
	 */
	private Map<File, List<String>> findOutputDirsForAspects() {
		Map<File, List<String>> outputDirsToAspects = new HashMap<>();
		Map<String, char[]> aspectNamesToFileNames = state.getAspectNamesToFileNameMap();
		if (buildConfig.getCompilationResultDestinationManager() == null
				|| buildConfig.getCompilationResultDestinationManager().getAllOutputLocations().size() == 1) {
			// we only have one output directory...which simplifies things
			File outputDir = buildConfig.getOutputDir();
			if (buildConfig.getCompilationResultDestinationManager() != null) {
				outputDir = buildConfig.getCompilationResultDestinationManager().getDefaultOutputLocation();
			}
			List<String> aspectNames = new ArrayList<>();
			if (aspectNamesToFileNames != null) {
				Set<String> keys = aspectNamesToFileNames.keySet();
				for (String name : keys) {
					aspectNames.add(name);
				}
			}
			outputDirsToAspects.put(outputDir, aspectNames);
		} else {
			List<File> outputDirs = buildConfig.getCompilationResultDestinationManager().getAllOutputLocations();
            for (File outputDir : outputDirs) {
                outputDirsToAspects.put(outputDir, new ArrayList<>());
            }
			if (aspectNamesToFileNames != null) {
				Set<Map.Entry<String, char[]>> entrySet = aspectNamesToFileNames.entrySet();
                for (Map.Entry<String, char[]> entry : entrySet) {
                    String aspectName = entry.getKey();
                    char[] fileName = entry.getValue();
                    File outputDir = buildConfig.getCompilationResultDestinationManager().getOutputLocationForClass(
                            new File(new String(fileName)));
                    if (!outputDirsToAspects.containsKey(outputDir)) {
                        outputDirsToAspects.put(outputDir, new ArrayList<>());
                    }
                    outputDirsToAspects.get(outputDir).add(aspectName);
                }
			}
		}
		return outputDirsToAspects;
	}

	// public static void dumprels() {
	// IRelationshipMap irm = AsmManager.getDefault().getRelationshipMap();
	// int ctr = 1;
	// Set entries = irm.getEntries();
	// for (Iterator iter = entries.iterator(); iter.hasNext();) {
	// String hid = (String) iter.next();
	// List rels = irm.get(hid);
	// for (Iterator iterator = rels.iterator(); iterator.hasNext();) {
	// IRelationship ir = (IRelationship) iterator.next();
	// List targets = ir.getTargets();
	// for (Iterator iterator2 = targets.iterator();
	// iterator2.hasNext();
	// ) {
	// String thid = (String) iterator2.next();
	// System.err.println("Hid:"+(ctr++)+":(targets="+targets.size()+") "+hid+" ("+ir.getName()+") "+thid);
	// }
	// }
	// }
	// }

	/**
	 * Responsible for managing the ASM model between builds. Contains the policy for maintaining the persistance of elements in the
	 * model.
	 *
	 * This code is driven before each 'fresh' (batch) build to create a new model.
	 */
	private void setupModel(AjBuildConfig config) {
		if (!(config.isEmacsSymMode() || config.isGenerateModelMode())) {
			return;
		}
		// AsmManager.setCreatingModel(config.isEmacsSymMode() || config.isGenerateModelMode());
		// if (!AsmManager.isCreatingModel())
		// return;

		CompilationResultDestinationManager crdm = config.getCompilationResultDestinationManager();
		AsmManager structureModel = AsmManager.createNewStructureModel(crdm == null ? Collections.EMPTY_MAP : crdm.getInpathMap());
		// AsmManager.getDefault().getRelationshipMap().clear();
		IHierarchy model = structureModel.getHierarchy();
		String rootLabel = "<root>";

		IProgramElement.Kind kind = IProgramElement.Kind.FILE_JAVA;
		if (buildConfig.getConfigFile() != null) {
			rootLabel = buildConfig.getConfigFile().getName();
			model.setConfigFile(buildConfig.getConfigFile().getAbsolutePath());
			kind = IProgramElement.Kind.FILE_LST;
		}
		model.setRoot(new ProgramElement(structureModel, rootLabel, kind, new ArrayList<>()));

		model.setFileMap(new HashMap<>());
		// setStructureModel(model);
		state.setStructureModel(structureModel);
		// state.setRelationshipMap(AsmManager.getDefault().getRelationshipMap());
	}

	//
	// private void dumplist(List l) {
	// System.err.println("---- "+l.size());
	// for (int i =0 ;i<l.size();i++) System.err.println(i+"\t "+l.get(i));
	// }
	// private void accumulateFileNodes(IProgramElement ipe,List store) {
	// if (ipe.getKind()==IProgramElement.Kind.FILE_JAVA ||
	// ipe.getKind()==IProgramElement.Kind.FILE_ASPECTJ) {
	// if (!ipe.getName().equals("<root>")) {
	// store.add(ipe);
	// return;
	// }
	// }
	// for (Iterator i = ipe.getChildren().iterator();i.hasNext();) {
	// accumulateFileNodes((IProgramElement)i.next(),store);
	// }
	// }

	// LTODO delegate to BcelWeaver?
	// XXX hideous, should not be Object
	public void setCustomMungerFactory(Object o) {
		customMungerFactory = (CustomMungerFactory) o;
	}

	public Object getCustomMungerFactory() {
		return customMungerFactory;
	}

	/** init only on initial batch compile? no file-specific options */
	private void initBcelWorld(IMessageHandler handler) throws IOException {
		List<String> cp = buildConfig.getFullClasspath(); // pr145693
		// buildConfig.getBootclasspath();
		// cp.addAll(buildConfig.getClasspath());
		BcelWorld bcelWorld = new BcelWorld(cp, handler, null);
		bcelWorld.setBehaveInJava5Way(buildConfig.getBehaveInJava5Way());
		bcelWorld.setTiming(buildConfig.isTiming(), false);
		bcelWorld.setAddSerialVerUID(buildConfig.isAddSerialVerUID());
		bcelWorld.setXmlConfigured(buildConfig.isXmlConfigured());
		bcelWorld.setXmlFiles(buildConfig.getXmlFiles());
		bcelWorld.performExtraConfiguration(buildConfig.getXconfigurationInfo());
		bcelWorld.setTargetAspectjRuntimeLevel(buildConfig.getTargetAspectjRuntimeLevel());
		bcelWorld.setOptionalJoinpoints(buildConfig.getXJoinpoints());
		bcelWorld.setXnoInline(buildConfig.isXnoInline());
		bcelWorld.setXlazyTjp(buildConfig.isXlazyTjp());
		bcelWorld.setXHasMemberSupportEnabled(buildConfig.isXHasMemberEnabled());
		bcelWorld.setPinpointMode(buildConfig.isXdevPinpoint());
		bcelWorld.setErrorAndWarningThreshold(buildConfig.getOptions().errorThreshold.isSet(24), buildConfig.getOptions().warningThreshold.isSet(24));
		BcelWeaver bcelWeaver = new BcelWeaver(bcelWorld);
		bcelWeaver.setCustomMungerFactory(customMungerFactory);
		state.setWorld(bcelWorld);
		state.setWeaver(bcelWeaver);
		state.clearBinarySourceFiles();

		if (buildConfig.getLintMode().equals(AjBuildConfig.AJLINT_DEFAULT)) {
			bcelWorld.getLint().loadDefaultProperties();
		} else {
			bcelWorld.getLint().setAll(buildConfig.getLintMode());
		}
		if (buildConfig.getLintOptionsMap() != null) {
			bcelWorld.getLint().setFromMap(buildConfig.getLintOptionsMap());
		}
		if (buildConfig.getLintSpecFile() != null) {
			bcelWorld.getLint().setFromProperties(buildConfig.getLintSpecFile());
		}

		for (File f: buildConfig.getAspectpath()) {
			if (!f.exists()) {
				IMessage message = new Message("invalid aspectpath entry: " + f.getName(), null, true);
				handler.handleMessage(message);
			} else {
				bcelWeaver.addLibraryJarFile(f);
			}
		}

		// String lintMode = buildConfig.getLintMode();

		File outputDir = buildConfig.getOutputDir();
		if (outputDir == null && buildConfig.getCompilationResultDestinationManager() != null) {
			// send all output from injars and inpath to the default output location
			// (will also later send the manifest there too)
			outputDir = buildConfig.getCompilationResultDestinationManager().getDefaultOutputLocation();
		}
		// ??? incremental issues
		for (File inJar : buildConfig.getInJars()) {
			List<UnwovenClassFile> unwovenClasses = bcelWeaver.addJarFile(inJar, outputDir, false);
			state.recordBinarySource(inJar.getPath(), unwovenClasses);
		}

		for (File inPathElement : buildConfig.getInpath()) {
			if (!inPathElement.isDirectory()) {
				// its a jar file on the inpath
				// the weaver method can actually handle dirs, but we don't call it, see next block
				List<UnwovenClassFile> unwovenClasses = bcelWeaver.addJarFile(inPathElement, outputDir, true);
				state.recordBinarySource(inPathElement.getPath(), unwovenClasses);
			} else {
				// add each class file in an in-dir individually, this gives us the best error reporting
				// (they are like 'source' files then), and enables a cleaner incremental treatment of
				// class file changes in indirs.
				File[] binSrcs = FileUtil.listFiles(inPathElement, binarySourceFilter);
                for (File binSrc : binSrcs) {
                    UnwovenClassFile ucf = bcelWeaver.addClassFile(binSrc, inPathElement, outputDir);
                    List<UnwovenClassFile> ucfl = new ArrayList<>();
                    ucfl.add(ucf);
                    state.recordBinarySource(binSrc.getPath(), ucfl);
                }
			}
		}

		bcelWeaver.setReweavableMode(buildConfig.isXNotReweavable());

		// check for org.aspectj.runtime.JoinPoint
		ResolvedType joinPoint = bcelWorld.resolve("org.aspectj.lang.JoinPoint");
		if (joinPoint.isMissing()) {
			IMessage message = new Message(
					"classpath error: unable to find org.aspectj.lang.JoinPoint (check that aspectjrt.jar is in your classpath)",
					null, true);
			handler.handleMessage(message);
		}
	}

	public World getWorld() {
		return getBcelWorld();
	}

	// void addAspectClassFilesToWeaver(List addedClassFiles) throws IOException {
	// for (Iterator i = addedClassFiles.iterator(); i.hasNext();) {
	// UnwovenClassFile classFile = (UnwovenClassFile) i.next();
	// getWeaver().addClassFile(classFile);
	// }
	// }

	public FileSystem getLibraryAccess(String[] classpaths, String[] filenames) {
		String defaultEncoding = buildConfig.getOptions().defaultEncoding;
		if ("".equals(defaultEncoding)) {//$NON-NLS-1$
			defaultEncoding = null;
		}
		// Bug 46671: We need an array as long as the number of elements in the classpath - *even though* not every
		// element of the classpath is likely to be a directory. If we ensure every element of the array is set to
		// only look for BINARY, then we make sure that for any classpath element that is a directory, we won't build
		// a classpathDirectory object that will attempt to look for source when it can't find binary.
		// int[] classpathModes = new int[classpaths.length];
		// for (int i =0 ;i<classpaths.length;i++) classpathModes[i]=ClasspathDirectory.BINARY;

		FileSystem nameEnvironment = null;
		// TODO J9 The compiler likes to work in terms of checked classpath objects - these will be different
		// depending on where the code came from (classpath, modulepath). If working with just the raw
		// 'classpaths' object it isn't recording where the code came from. This will be an issue later for
		// weaving, the distinction will need to be maintained for proper 'module aware/respecting' weaving.
		if (buildConfig.getCheckedClasspaths() == null) {
			nameEnvironment = new FileSystem(classpaths, filenames, defaultEncoding, ClasspathLocation.BINARY, null);
		} else {
			nameEnvironment = new FileSystem(buildConfig.getCheckedClasspaths(), filenames, false, null);
		}
		nameEnvironment.module = buildConfig.getModuleDesc();
		return nameEnvironment;
	}

	public IProblemFactory getProblemFactory() {
		return new DefaultProblemFactory(Locale.getDefault());
	}

	/*
	 * Build the set of compilation source units
	 */
	public CompilationUnit[] getCompilationUnits(String[] filenames) {
		int fileCount = filenames.length;
		CompilationUnit[] units = new CompilationUnit[fileCount];
		// HashtableOfObject knownFileNames = new HashtableOfObject(fileCount);

		String defaultEncoding = buildConfig.getOptions().defaultEncoding;
		if ("".equals(defaultEncoding)) {//$NON-NLS-1$
			defaultEncoding = null;
		}
		CompilationUnit moduleCU = null;

		// TODO building with multiple module-infos?
		int moduleIndex = -1;
		IModule moduleDesc = buildConfig.getModuleDesc();
		String moduleName = moduleDesc == null? null: new String(moduleDesc.name());
		for (int i=0;i<fileCount;i++) {
			if (filenames[i].endsWith("module-info.java")) {
				moduleIndex = i;
				moduleCU = new CompilationUnit(null, filenames[i], defaultEncoding, null, false, moduleName);
			}
		}

		for (int i = 0; i < fileCount; i++) {
//			units[i] = new CompilationUnit(null, filenames[i], defaultEncoding);
			if (i == moduleIndex) {
				units[i] = moduleCU;
			} else {
				units[i] = new CompilationUnit(null, filenames[i], defaultEncoding, null, false, moduleName);
				// With Java 10 changes the modulebinding is fetched from the rootenvironment
				// this.moduleBinding = rootEnvironment.getModule(this.module);
				// rather than using the moduleCU:
				// if (this.modCU != null)
				// 	return this.moduleBinding = this.modCU.module(rootEnvironment);
//				units[i].setModule(moduleCU);
			}
//			new CompilationUnit(null, fileName, encoding, this.destinationPaths[i],
//					shouldIgnoreOptionalProblems(this.ignoreOptionalProblemsFromFolders, fileName.toCharArray()),
//					this.modNames[i]);
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

	public void performCompilation(Collection<File> files) {
		if (progressListener != null) {
			compiledCount = 0;
			sourceFileCount = files.size();
			progressListener.setText("compiling source files");
		}

		// Translate from strings to File objects
		String[] filenames = new String[files.size()];
		int idx = 0;
        for (File f : files) {
            filenames[idx++] = f.getPath();
        }

		environment = state.getNameEnvironment();

		boolean environmentNeedsRebuilding = false;

		// Might be a bit too cautious, but let us see how it goes
		if (buildConfig.getChanged() != AjBuildConfig.NO_CHANGES) {
			environmentNeedsRebuilding = true;
		}

		if (environment == null || environmentNeedsRebuilding) {
			List<String> cps = buildConfig.getFullClasspath();
			Dump.saveFullClasspath(cps);
			String[] classpaths = new String[cps.size()];
			for (int i = 0; i < cps.size(); i++) {
				classpaths[i] = cps.get(i);
			}
			FileSystem fileSystem = getLibraryAccess(classpaths, filenames);

			// Use upstream method to generate '--add-reads', '--add-exports' info and copy it into our FileSystem instance.
			// See https://github.com/eclipse-aspectj/aspectj/issues/145.
			FileSystem fileSystemTemp = buildConfig.getBuildArgParser().getLibraryAccess();
			fileSystem.moduleUpdates = fileSystemTemp.moduleUpdates;
			fileSystemTemp.cleanup();

			environment = new StatefulNameEnvironment(fileSystem, state.getClassNameToFileMap(), state);
			state.setFileSystem(fileSystem);
			state.setNameEnvironment(environment);
		} else {
			((StatefulNameEnvironment) environment).update(state.getClassNameToFileMap(), state.deltaAddedClasses);
			state.deltaAddedClasses.clear();
		}

		org.aspectj.ajdt.internal.compiler.CompilerAdapter.setCompilerAdapterFactory(this);
		final Map<String, String> settings = buildConfig.getOptions().getMap();
		final BuildArgParser bMain = buildConfig.getBuildArgParser();

		final org.aspectj.org.eclipse.jdt.internal.compiler.Compiler compiler = new org.aspectj.org.eclipse.jdt.internal.compiler.Compiler(
			environment, DefaultErrorHandlingPolicies.proceedWithAllProblems(), new CompilerOptions(settings),
			getBatchRequestor(), getProblemFactory());
		bMain.compilerOptions = compiler.options;
		bMain.batchCompiler = compiler;
		bMain.initializeAnnotationProcessorManager();
		compiler.options.produceReferenceInfo = true; // TODO turn off when not needed


		if (bMain.compilerOptions.complianceLevel >= ClassFileConstants.JDK1_6
				&& bMain.compilerOptions.processAnnotations) {
			// need this too?
//			if (bMain.checkVMVersion(ClassFileConstants.JDK1_6)) {
//				initializeAnnotationProcessorManager();
//				if (this.classNames != null) {
//					this.batchCompiler.setBinaryTypes(processClassNames(this.batchCompiler.lookupEnvironment));
//				}
//			} else {
//				// report a warning
//				this.logger.logIncorrectVMVersionForAnnotationProcessing();
//			}
			if (bMain.checkVMVersion(ClassFileConstants.JDK9)) {
				try {
					bMain.initRootModules(bMain.batchCompiler.lookupEnvironment, state.getFileSystem());
				} catch (IllegalArgumentException iae) {
					ISourceLocation location = null;
					if (buildConfig.getConfigFile() != null) {
						location = new SourceLocation(buildConfig.getConfigFile(), 0);
					}
					IMessage m = new Message(iae.getMessage(), IMessage.ERROR, null, location);
					handler.handleMessage(m);
				}
			}
		}

		try {
			compiler.compile(getCompilationUnits(filenames));
		} catch (OperationCanceledException oce) {
			handler.handleMessage(new Message("build cancelled:" + oce.getMessage(), IMessage.WARNING, null, null));
		}
		// cleanup
		org.aspectj.ajdt.internal.compiler.CompilerAdapter.setCompilerAdapterFactory(null);
		AnonymousClassPublisher.aspectOf().setAnonymousClassCreationListener(null);
		environment.cleanup();
		// environment = null;
	}

	public void cleanupEnvironment() {
		if (environment != null) {
			environment.cleanup();
			environment = null;
			// le = null;
		}
	}

	/*
	 * Answer the component to which will be handed back compilation results from the compiler
	 */
	public IIntermediateResultsRequestor getInterimResultRequestor() {
		return new IIntermediateResultsRequestor() {
			@Override
			public void acceptResult(InterimCompilationResult result) {
				if (progressListener != null) {
					compiledCount++;
					progressListener.setProgress((compiledCount / 2.0) / sourceFileCount);
					progressListener.setText("compiled: " + result.fileName());
				}
				state.noteResult(result);

				if (progressListener != null && progressListener.isCancelledRequested()) {
					throw new AbortCompilation(true, new OperationCanceledException("Compilation cancelled as requested"));
				}
			}
		};
	}

	public ICompilerRequestor getBatchRequestor() {
		return new ICompilerRequestor() {
			@Override
			public void acceptResult(CompilationResult unitResult) {
				// end of compile, must now write the results to the output destination
				// this is either a jar file or a file in a directory
				boolean hasErrors = unitResult.hasErrors();
				if (!hasErrors || proceedOnError()) {
					Collection<ClassFile> classFiles = unitResult.compiledTypes.values();
					boolean shouldAddAspectName = (buildConfig.getOutxmlName() != null);
                    for (ClassFile classFile : classFiles) {
                        String filename = new String(classFile.fileName());
                        String classname = filename.replace('/', '.');
                        filename = filename.replace('/', File.separatorChar) + ".class";

                        try {
                            if (buildConfig.getOutputJar() == null) {
                                String outfile = writeDirectoryEntry(unitResult, classFile, filename);
                                getWorld().classWriteEvent(classFile.getCompoundName());
                                if (environmentSupportsIncrementalCompilation) {
                                    if (!classname.endsWith("$ajcMightHaveAspect")) {
                                        ResolvedType type = getBcelWorld().resolve(classname);
                                        if (type.isAspect()) {
                                            state.recordAspectClassFile(outfile);
                                        }
                                    }
                                }
                            }
                            else {
                                writeZipEntry(classFile, filename);
                            }
                            if (shouldAddAspectName && !classname.endsWith("$ajcMightHaveAspect")) {
                                addAspectName(classname, unitResult.getFileName());
                            }
                        } catch (IOException ex) {
                            IMessage message = EclipseAdapterUtils.makeErrorMessage(new String(unitResult.fileName),
                                    CANT_WRITE_RESULT, ex);
                            handler.handleMessage(message);
                        }

                    }
					state.noteNewResult(unitResult);
					unitResult.compiledTypes.clear(); // free up references to AjClassFile instances
				}

				if (unitResult.hasProblems() || unitResult.hasTasks()) {
					IProblem[] problems = unitResult.getAllProblems();
                    for (IProblem problem : problems) {
                        IMessage message = EclipseAdapterUtils.makeMessage(unitResult.compilationUnit, problem, getBcelWorld(),
                                progressListener);
                        handler.handleMessage(message);
                    }
				}

			}

			private String writeDirectoryEntry(CompilationResult unitResult, ClassFile classFile, String filename)
					throws IOException {
				File destinationPath = buildConfig.getOutputDir();
				if (buildConfig.getCompilationResultDestinationManager() != null) {
					destinationPath = buildConfig.getCompilationResultDestinationManager().getOutputLocationForClass(
							new File(new String(unitResult.fileName)));
				}
				String outFile;
				if (destinationPath == null) {
					outFile = new File(filename).getName();
					outFile = new File(extractDestinationPathFromSourceFile(unitResult), outFile).getPath();
				} else {
					outFile = new File(destinationPath, filename).getPath();
				}

				try {
					BufferedOutputStream os = FileUtil.makeOutputStream(new File(outFile));
					os.write(classFile.getBytes());
					os.close();
				} catch (FileNotFoundException fnfe) {
					IMessage msg = new Message("unable to write out class file: '" + filename + "' - reason: " + fnfe.getMessage(),
							IMessage.ERROR, null, new SourceLocation(new File(outFile), 0));
					handler.handleMessage(msg);
				}

				if (buildConfig.getCompilationResultDestinationManager() != null) {
					buildConfig.getCompilationResultDestinationManager().reportFileWrite(outFile,
							CompilationResultDestinationManager.FILETYPE_CLASS);
				}
				return outFile;
			}

			private void writeZipEntry(ClassFile classFile, String name) throws IOException {
				name = name.replace(File.separatorChar, '/');
				ZipEntry newEntry = new ZipEntry(name); // ??? get compression scheme right

				zos.putNextEntry(newEntry);
				zos.write(classFile.getBytes());
				zos.closeEntry();
			}

			private void addAspectName(String name, char[] fileContainingAspect) {
				BcelWorld world = getBcelWorld();
				ResolvedType type = world.resolve(name);
				// System.err.println("? writeAspectName() type=" + type);
				if (type.isAspect()) {
					if (state.getAspectNamesToFileNameMap() == null) {
						state.initializeAspectNamesToFileNameMap();
					}
					if (!state.getAspectNamesToFileNameMap().containsKey(name)) {
						state.getAspectNamesToFileNameMap().put(name, fileContainingAspect);
					}
				}
			}
		};
	}

	protected boolean proceedOnError() {
		return buildConfig.getProceedOnError();
	}

	// public void noteClassFiles(AjCompiler.InterimResult result) {
	// if (result == null) return;
	// CompilationResult unitResult = result.result;
	// String sourceFileName = result.fileName();
	// if (!(unitResult.hasErrors() && !proceedOnError())) {
	// List unwovenClassFiles = new ArrayList();
	// Enumeration classFiles = unitResult.compiledTypes.elements();
	// while (classFiles.hasMoreElements()) {
	// ClassFile classFile = (ClassFile) classFiles.nextElement();
	// String filename = new String(classFile.fileName());
	// filename = filename.replace('/', File.separatorChar) + ".class";
	//
	// File destinationPath = buildConfig.getOutputDir();
	// if (destinationPath == null) {
	// filename = new File(filename).getName();
	// filename = new File(extractDestinationPathFromSourceFile(unitResult), filename).getPath();
	// } else {
	// filename = new File(destinationPath, filename).getPath();
	// }
	//
	// //System.out.println("classfile: " + filename);
	// unwovenClassFiles.add(new UnwovenClassFile(filename, classFile.getBytes()));
	// }
	// state.noteClassesFromFile(unitResult, sourceFileName, unwovenClassFiles);
	// // System.out.println("file: " + sourceFileName);
	// // for (int i=0; i < unitResult.simpleNameReferences.length; i++) {
	// // System.out.println("simple: " + new String(unitResult.simpleNameReferences[i]));
	// // }
	// // for (int i=0; i < unitResult.qualifiedReferences.length; i++) {
	// // System.out.println("qualified: " +
	// // new String(CharOperation.concatWith(unitResult.qualifiedReferences[i], '/')));
	// // }
	// } else {
	// state.noteClassesFromFile(null, sourceFileName, Collections.EMPTY_LIST);
	// }
	// }
	//

	private void setBuildConfig(AjBuildConfig buildConfig) {
		this.buildConfig = buildConfig;
		if (!this.environmentSupportsIncrementalCompilation) {
			this.environmentSupportsIncrementalCompilation = (buildConfig.isIncrementalMode() || buildConfig
					.isIncrementalFileMode());
		}
		handler.reset();
	}

	String makeClasspathString(AjBuildConfig buildConfig) {
		if (buildConfig == null || buildConfig.getFullClasspath() == null) {
			return "";
		}
		StringBuilder buf = new StringBuilder();
		boolean first = true;
        for (String s : buildConfig.getFullClasspath()) {
            if (first) {
                first = false;
            }
            else {
                buf.append(File.pathSeparator);
            }
            buf.append(s.toString());
        }
		return buf.toString();
	}

	/**
	 * This will return null if aspectjrt.jar is present and has the correct version. Otherwise it will return a string message
	 * indicating the problem.
	 */
	private String checkRtJar(AjBuildConfig buildConfig) {
		// omitting dev info
		if (Version.getText().equals(Version.DEVELOPMENT) || Version.getText().endsWith("-SNAPSHOT")) {
			// in the development version we can't do this test usefully
			// MessageUtil.info(holder, "running development version of aspectj compiler");
			return null;
		}

		if (buildConfig == null || buildConfig.getFullClasspath() == null) {
			return "no classpath specified";
		}

		for (String s: buildConfig.getFullClasspath()) {
			if (s.endsWith("runtime/target/classes") || s.endsWith("runtime\\target\\classes")) {
				// doing an AspectJ build
				return null;
			}
		}

		String ret = null;
        for (String s : buildConfig.getFullClasspath()) {
            File p = new File(s);
            // pr112830, allow variations on aspectjrt.jar of the form aspectjrtXXXXXX.jar
            if (p.isFile() && p.getName().startsWith("aspectjrt") && p.getName().endsWith(".jar")) {

                try {
                    String version = null;
                    Manifest manifest = new JarFile(p).getManifest();
                    if (manifest == null) {
                        ret = "no manifest found in " + p.getAbsolutePath() + ", expected " + Version.getText();
                        continue;
                    }
                    Attributes attr = manifest.getAttributes("org/aspectj/lang/");
                    if (null != attr) {
                        version = attr.getValue(Attributes.Name.IMPLEMENTATION_VERSION);
                        if (null != version) {
                            version = version.trim();
                        }
                    }
                    // assume that users of development aspectjrt.jar know what they're doing
                    if (version != null && (Version.DEVELOPMENT.equals(version) || version.endsWith("-SNAPSHOT"))) {
                        // MessageUtil.info(holder,
                        // "running with development version of aspectjrt.jar in " +
                        // p.getAbsolutePath());
                        return null;
                    }
                    else if (!Version.getText().equals(version)) {
                        ret = "bad version number found in " + p.getAbsolutePath() + " expected " + Version.getText() + " found "
                                + version;
                        continue;
                    }
                } catch (IOException ioe) {
                    ret = "bad jar file found in " + p.getAbsolutePath() + " error: " + ioe;
                }
                return null; // this is the "OK" return value!
            }
            else if (p.isFile() && p.getName().contains("org.aspectj.runtime")) {
                // likely to be a variant from the springsource bundle repo b272591
                return null;
            }
            else {
                // might want to catch other classpath errors
            }
        }

		if (ret != null) {
			return ret; // last error found in potentially matching jars...
		}

		return "couldn't find aspectjrt.jar on classpath, checked: " + makeClasspathString(buildConfig);
	}

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		buf.append("AjBuildManager(");
		buf.append(")");
		return buf.toString();
	}

	//
	// public void setStructureModel(IHierarchy structureModel) {
	// this.structureModel = structureModel;
	// }

	/**
	 * Returns null if there is no structure model
	 */
	public AsmManager getStructureModel() {
		return (state == null ? null : state.getStructureModel());
	}

	public IProgressListener getProgressListener() {
		return progressListener;
	}

	public void setProgressListener(IProgressListener progressListener) {
		this.progressListener = progressListener;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.aspectj.ajdt.internal.compiler.AjCompiler.IOutputClassFileNameProvider#getOutputClassFileName(char[])
	 */
	@Override
	public String getOutputClassFileName(char[] eclipseClassFileName, CompilationResult result) {
		String filename = new String(eclipseClassFileName);
		filename = filename.replace('/', File.separatorChar) + ".class";
		File destinationPath = buildConfig.getOutputDir();
		if (buildConfig.getCompilationResultDestinationManager() != null) {
			File f = new File(new String(result.getFileName()));
			destinationPath = buildConfig.getCompilationResultDestinationManager().getOutputLocationForClass(f);
		}
		String outFile;
		if (destinationPath == null) {
			outFile = new File(filename).getName();
			outFile = new File(extractDestinationPathFromSourceFile(result), outFile).getPath();
		} else {
			outFile = new File(destinationPath, filename).getPath();
		}
		return outFile;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jdt.internal.compiler.ICompilerAdapterFactory#getAdapter(org.eclipse.jdt.internal.compiler.Compiler)
	 */
	@Override
	public ICompilerAdapter getAdapter(org.aspectj.org.eclipse.jdt.internal.compiler.Compiler forCompiler) {
		// complete compiler config and return a suitable adapter...
		populateCompilerOptionsFromLintSettings(forCompiler);
		AjProblemReporter pr = new AjProblemReporter(DefaultErrorHandlingPolicies.proceedWithAllProblems(), forCompiler.options,
				getProblemFactory());

		forCompiler.problemReporter = pr;

		AjLookupEnvironment le = new AjLookupEnvironment(forCompiler, forCompiler.options, pr, environment);
		EclipseFactory factory = new EclipseFactory(le, this);
		le.factory = factory;
		pr.factory = factory;

		forCompiler.lookupEnvironment = le;

		forCompiler.parser = new Parser(pr, forCompiler.options.parseLiteralExpressionsAsConstants);
		if (getBcelWorld().shouldPipelineCompilation()) {
			IMessage message = MessageUtil.info("Pipelining compilation");
			handler.handleMessage(message);
			return new AjPipeliningCompilerAdapter(forCompiler, batchCompile, getBcelWorld(), getWeaver(), factory,
					getInterimResultRequestor(), progressListener,
					this, // IOutputFilenameProvider
					this, // IBinarySourceProvider
					state.getBinarySourceMap(), buildConfig.isTerminateAfterCompilation(), buildConfig.getProceedOnError(),
					buildConfig.isNoAtAspectJAnnotationProcessing(), buildConfig.isMakeReflectable(), state);
		} else {
			return new AjCompilerAdapter(forCompiler, batchCompile, getBcelWorld(), getWeaver(), factory,
					getInterimResultRequestor(), progressListener,
					this, // IOutputFilenameProvider
					this, // IBinarySourceProvider
					state.getBinarySourceMap(), buildConfig.isTerminateAfterCompilation(), buildConfig.getProceedOnError(),
					buildConfig.isNoAtAspectJAnnotationProcessing(), buildConfig.isMakeReflectable(), state);
		}
	}

	/**
	 * Some AspectJ lint options need to be known about in the compiler. This is how we pass them over...
	 *
	 * @param forCompiler
	 */
	private void populateCompilerOptionsFromLintSettings(org.aspectj.org.eclipse.jdt.internal.compiler.Compiler forCompiler) {
		BcelWorld world = this.state.getBcelWorld();
		IMessage.Kind swallowedExceptionKind = world.getLint().swallowedExceptionInCatchBlock.getKind();
		Map<String, String> optionsMap = new HashMap<>();
		optionsMap.put(CompilerOptions.OPTION_ReportSwallowedExceptionInCatchBlock, swallowedExceptionKind == null ? "ignore"
				: swallowedExceptionKind.toString());
		forCompiler.options.set(optionsMap);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.aspectj.ajdt.internal.compiler.IBinarySourceProvider#getBinarySourcesForThisWeave()
	 */
	@Override
	public Map<String, List<UnwovenClassFile>> getBinarySourcesForThisWeave() {
		return binarySourcesForTheNextCompile;
	}

	public static AsmHierarchyBuilder getAsmHierarchyBuilder() {
		return asmHierarchyBuilder;
	}

	/**
	 * Override the the default hierarchy builder.
	 */
	public static void setAsmHierarchyBuilder(AsmHierarchyBuilder newBuilder) {
		asmHierarchyBuilder = newBuilder;
	}

	public AjState getState() {
		return state;
	}

	public void setState(AjState buildState) {
		state = buildState;
	}

	private static class AjBuildContexFormatter implements ContextFormatter {

		@Override
		public String formatEntry(int phaseId, Object data) {
			StringBuilder sb = new StringBuilder();
			if (phaseId == CompilationAndWeavingContext.BATCH_BUILD) {
				sb.append("batch building ");
			} else {
				sb.append("incrementally building ");
			}
			AjBuildConfig config = (AjBuildConfig) data;
			List<String> classpath = config.getClasspath();
			sb.append("with classpath: ");
            for (Object o : classpath) {
                sb.append(o.toString());
                sb.append(File.pathSeparator);
            }
			return sb.toString();
		}

	}

	public boolean wasFullBuild() {
		return wasFullBuild;
	}
}
