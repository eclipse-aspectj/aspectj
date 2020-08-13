/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     PARC     initial implementation 
 *     Andy Clement     overhauled
 * ******************************************************************/

package org.aspectj.ajdt.internal.core.builder;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aspectj.ajdt.internal.compiler.CompilationResultDestinationManager;
import org.aspectj.ajdt.internal.compiler.InterimCompilationResult;
import org.aspectj.ajdt.internal.core.builder.AjBuildConfig.BinarySourceFile;
import org.aspectj.apache.bcel.classfile.ClassParser;
import org.aspectj.asm.AsmManager;
import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.Message;
import org.aspectj.bridge.SourceLocation;
import org.aspectj.org.eclipse.jdt.core.compiler.CharOperation;
import org.aspectj.org.eclipse.jdt.internal.compiler.CompilationResult;
import org.aspectj.org.eclipse.jdt.internal.compiler.batch.FileSystem;
import org.aspectj.org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader;
import org.aspectj.org.eclipse.jdt.internal.compiler.classfmt.ClassFormatException;
import org.aspectj.org.eclipse.jdt.internal.compiler.env.IBinaryAnnotation;
import org.aspectj.org.eclipse.jdt.internal.compiler.env.IBinaryField;
import org.aspectj.org.eclipse.jdt.internal.compiler.env.IBinaryMethod;
import org.aspectj.org.eclipse.jdt.internal.compiler.env.IBinaryNestedType;
import org.aspectj.org.eclipse.jdt.internal.compiler.env.IBinaryType;
import org.aspectj.org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.ExtraCompilerModifiers;
import org.aspectj.org.eclipse.jdt.internal.core.builder.ReferenceCollection;
import org.aspectj.org.eclipse.jdt.internal.core.builder.StringSet;
import org.aspectj.util.FileUtil;
import org.aspectj.weaver.BCException;
import org.aspectj.weaver.CompressingDataOutputStream;
import org.aspectj.weaver.ReferenceType;
import org.aspectj.weaver.ReferenceTypeDelegate;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.bcel.BcelWeaver;
import org.aspectj.weaver.bcel.BcelWorld;
import org.aspectj.weaver.bcel.TypeDelegateResolver;
import org.aspectj.weaver.bcel.UnwovenClassFile;

/**
 * Maintains state needed for incremental compilation
 */
public class AjState implements CompilerConfigurationChangeFlags, TypeDelegateResolver {

	// SECRETAPI configures whether we use state instead of lastModTime - see pr245566
	public static boolean CHECK_STATE_FIRST = true;

	// SECRETAPI static so beware of multi-threading bugs...
	public static IStateListener stateListener = null;

	public static boolean FORCE_INCREMENTAL_DURING_TESTING = false;

	static int PATHID_CLASSPATH = 0;
	static int PATHID_ASPECTPATH = 1;
	static int PATHID_INPATH = 2;

	private static int CLASS_FILE_NO_CHANGES = 0;
	private static int CLASS_FILE_CHANGED_THAT_NEEDS_INCREMENTAL_BUILD = 1;
	private static int CLASS_FILE_CHANGED_THAT_NEEDS_FULL_BUILD = 2;

	private static final char[][] EMPTY_CHAR_ARRAY = new char[0][];

	// now follows non static, but transient state - no need to write out, doesn't need reinitializing
	// State recreated for each build:

	/**
	 * When looking at changes on the classpath, this set accumulates files in our state instance that affected by those changes.
	 * Then if we can do an incremental build - these must be compiled.
	 */
	private final Set<File> affectedFiles = new HashSet<>();

	// these are references created on a particular compile run - when looping round in
	// addAffectedSourceFiles(), if some have been created then we look at which source files
	// touch upon those and get them recompiled.
	private StringSet qualifiedStrings = new StringSet(3);

	private StringSet simpleStrings = new StringSet(3);

	private Set<File> addedFiles;
	private Set<File> deletedFiles;
	private Set<BinarySourceFile> addedBinaryFiles;
	private Set<BinarySourceFile> deletedBinaryFiles;
	// For a particular build run, this set records the changes to classesFromName
	public final Set<String> deltaAddedClasses = new HashSet<>();

	// now follows non static, but transient state - no need to write out, DOES need reinitializing when read AjState instance
	// reloaded

	private final AjBuildManager buildManager;
	private INameEnvironment nameEnvironment;
	private FileSystem fileSystem;

	// now follows normal state that must be written out

	private boolean couldBeSubsequentIncrementalBuild = false;
	private boolean batchBuildRequiredThisTime = false;
	private AjBuildConfig buildConfig;
	private long lastSuccessfulFullBuildTime = -1;
	private final Hashtable<String, Long> structuralChangesSinceLastFullBuild = new Hashtable<>();
	private long lastSuccessfulBuildTime = -1;
	private long currentBuildTime = -1;
	private AsmManager structureModel;

	/**
	 * For a given source file, records the ClassFiles (which contain a fully qualified name and a file name) that were created when
	 * the source file was compiled. Populated in noteResult and used in addDependentsOf(File)
	 */
	private final Map<File, List<ClassFile>> fullyQualifiedTypeNamesResultingFromCompilationUnit = new HashMap<>();

	/**
	 * Source files defining aspects Populated in noteResult and used in processDeletedFiles
	 */
	private final Set<File> sourceFilesDefiningAspects = new HashSet<>();

	/**
	 * Populated in noteResult to record the set of types that should be recompiled if the given file is modified or deleted.
	 * Referred to during addAffectedSourceFiles when calculating incremental compilation set.
	 */
	private final Map<File, ReferenceCollection> references = new HashMap<>();

	/**
	 * Holds UnwovenClassFiles (byte[]s) originating from the given file source. This could be a jar file, a directory, or an
	 * individual .class file. This is an *expensive* map. It is cleared immediately following a batch build, and the cheaper
	 * inputClassFilesBySource map is kept for processing of any subsequent incremental builds.
	 * 
	 * Populated during AjBuildManager.initBcelWorld().
	 * 
	 * Passed into AjCompiler adapter as the set of binary input files to reweave if the weaver determines a full weave is required.
	 * 
	 * Cleared during initBcelWorld prior to repopulation.
	 * 
	 * Used when a file is deleted during incremental compilation to delete all of the class files in the output directory that
	 * resulted from the weaving of File.
	 * 
	 * Used during getBinaryFilesToCompile when compiling incrementally to determine which files should be recompiled if a given
	 * input file has changed.
	 * 
	 */
	private Map<String, List<UnwovenClassFile>> binarySourceFiles = new HashMap<>();

	/**
	 * Initially a duplicate of the information held in binarySourceFiles, with the key difference that the values are ClassFiles
	 * (type name, File) not UnwovenClassFiles (which also have all the byte code in them). After a batch build, binarySourceFiles
	 * is cleared, leaving just this much lighter weight map to use in processing subsequent incremental builds.
	 */
	private final Map<String, List<ClassFile>> inputClassFilesBySource = new HashMap<>();

	/**
	 * A list of the .class files created by this state that contain aspects.
	 */
	private final List<String> aspectClassFiles = new ArrayList<>();

	/**
	 * Holds structure information on types as they were at the end of the last build. It would be nice to get rid of this too, but
	 * can't see an easy way to do that right now.
	 */
	private final Map<String, CompactTypeStructureRepresentation> resolvedTypeStructuresFromLastBuild = new HashMap<>();

	/**
	 * Populated in noteResult to record the set of UnwovenClassFiles (intermediate results) that originated from compilation of the
	 * class with the given fully-qualified name.
	 * 
	 * Used in removeAllResultsOfLastBuild to remove .class files from output directory.
	 * 
	 * Passed into StatefulNameEnvironment during incremental compilation to support findType lookups.
	 */
	private final Map<String, File> classesFromName = new HashMap<>();

	/**
	 * Populated by AjBuildManager to record the aspects with the file name in which they're contained. This is later used when
	 * writing the outxml file in AjBuildManager. Need to record the file name because want to write an outxml file for each of the
	 * output directories and in order to ask the OutputLocationManager for the output location for a given aspect we need the file
	 * in which it is contained.
	 */
	private Map<String, char[]> aspectsFromFileNames;

	private Set<File> compiledSourceFiles = new HashSet<>();
	private final Map<String, File> resources = new HashMap<>();

	SoftHashMap/* <baseDir,SoftHashMap<theFile,className>> */fileToClassNameMap = new SoftHashMap();

	private BcelWeaver weaver;
	private BcelWorld world;

	// --- below here is unsorted state

	// ---

	public AjState(AjBuildManager buildManager) {
		this.buildManager = buildManager;
	}

	public void setCouldBeSubsequentIncrementalBuild(boolean yesThereCould) {
		this.couldBeSubsequentIncrementalBuild = yesThereCould;
	}

	void successfulCompile(AjBuildConfig config, boolean wasFullBuild) {
		buildConfig = config;
		lastSuccessfulBuildTime = currentBuildTime;
		if (stateListener != null) {
			stateListener.buildSuccessful(wasFullBuild);
		}
		if (wasFullBuild) {
			lastSuccessfulFullBuildTime = currentBuildTime;
		}
	}

	/**
	 * Returns false if a batch build is needed.
	 */
	public boolean prepareForNextBuild(AjBuildConfig newBuildConfig) {
		currentBuildTime = System.currentTimeMillis();

		if (!maybeIncremental()) {
			if (listenerDefined()) {
				getListener().recordDecision(
						"Preparing for build: not going to be incremental because either not in AJDT or incremental deactivated");
			}
			return false;
		}

		if (this.batchBuildRequiredThisTime) {
			this.batchBuildRequiredThisTime = false;
			if (listenerDefined()) {
				getListener().recordDecision(
						"Preparing for build: not going to be incremental this time because batch build explicitly forced");
			}
			return false;
		}

		if (lastSuccessfulBuildTime == -1 || buildConfig == null) {
			structuralChangesSinceLastFullBuild.clear();
			if (listenerDefined()) {
				getListener().recordDecision(
						"Preparing for build: not going to be incremental because no successful previous full build");
			}
			return false;
		}

		// we don't support incremental with an outjar yet
		if (newBuildConfig.getOutputJar() != null) {
			structuralChangesSinceLastFullBuild.clear();
			if (listenerDefined()) {
				getListener().recordDecision("Preparing for build: not going to be incremental because outjar being used");
			}
			return false;
		}

		affectedFiles.clear();

		// we can't do an incremental build if one of our paths
		// has changed, or a jar on a path has been modified
		if (pathChange(buildConfig, newBuildConfig)) {
			// last time we built, .class files and resource files from jars on the
			// inpath will have been copied to the output directory.
			// these all need to be deleted in preparation for the clean build that is
			// coming - otherwise a file that has been deleted from an inpath jar
			// since the last build will not be deleted from the output directory.
			removeAllResultsOfLastBuild();
			if (stateListener != null) {
				stateListener.pathChangeDetected();
			}
			structuralChangesSinceLastFullBuild.clear();
			if (listenerDefined()) {
				getListener()
						.recordDecision(
								"Preparing for build: not going to be incremental because path change detected (one of classpath/aspectpath/inpath/injars)");
			}
			return false;
		}

		if (simpleStrings.elementSize > 20) {
			simpleStrings = new StringSet(3);
		} else {
			simpleStrings.clear();
		}
		if (qualifiedStrings.elementSize > 20) {
			qualifiedStrings = new StringSet(3);
		} else {
			qualifiedStrings.clear();
		}

		if ((newBuildConfig.getChanged() & PROJECTSOURCEFILES_CHANGED) == 0) {
			addedFiles = Collections.emptySet();
			deletedFiles = Collections.emptySet();
		} else {
			Set<File> oldFiles = new HashSet<>(buildConfig.getFiles());
			Set<File> newFiles = new HashSet<>(newBuildConfig.getFiles());

			addedFiles = new HashSet<>(newFiles);
			addedFiles.removeAll(oldFiles);
			deletedFiles = new HashSet<>(oldFiles);
			deletedFiles.removeAll(newFiles);
		}

		Set<BinarySourceFile> oldBinaryFiles = new HashSet<>(buildConfig.getBinaryFiles());
		Set<BinarySourceFile> newBinaryFiles = new HashSet<>(newBuildConfig.getBinaryFiles());

		addedBinaryFiles = new HashSet<>(newBinaryFiles);
		addedBinaryFiles.removeAll(oldBinaryFiles);
		deletedBinaryFiles = new HashSet<>(oldBinaryFiles);
		deletedBinaryFiles.removeAll(newBinaryFiles);

		boolean couldStillBeIncremental = processDeletedFiles(deletedFiles);

		if (!couldStillBeIncremental) {
			if (listenerDefined()) {
				getListener().recordDecision("Preparing for build: not going to be incremental because an aspect was deleted");
			}
			return false;
		}

		if (listenerDefined()) {
			getListener().recordDecision("Preparing for build: planning to be an incremental build");
		}
		return true;
	}

	/**
	 * Checks if any of the files in the set passed in contains an aspect declaration. If one is found then we start the process of
	 * batch building, i.e. we remove all the results of the last build, call any registered listener to tell them whats happened
	 * and return false.
	 * 
	 * @return false if we discovered an aspect declaration
	 */
	private boolean processDeletedFiles(Set<File> deletedFiles) {
		for (File deletedFile : deletedFiles) {
			if (this.sourceFilesDefiningAspects.contains(deletedFile)) {
				removeAllResultsOfLastBuild();
				if (stateListener != null) {
					stateListener.detectedAspectDeleted(deletedFile);
				}
				return false;
			}
			List<ClassFile> classes = fullyQualifiedTypeNamesResultingFromCompilationUnit.get(deletedFile);
			if (classes != null) {
				for (ClassFile cf : classes) {
					resolvedTypeStructuresFromLastBuild.remove(cf.fullyQualifiedTypeName);
				}
			}
		}
		return true;
	}

	private Collection<File> getModifiedFiles() {
		return getModifiedFiles(lastSuccessfulBuildTime);
	}

	Collection<File> getModifiedFiles(long lastBuildTime) {
		Set<File> ret = new HashSet<>();

		// Check if the build configuration knows what files have changed...
		List<File> modifiedFiles = buildConfig.getModifiedFiles();

		if (modifiedFiles == null) {
			// do not know, so need to go looking
			// not our job to account for new and deleted files
			for (File file : buildConfig.getFiles()) {
				if (!file.exists()) {
					continue;
				}

				long modTime = file.lastModified();
				// System.out.println("check: " + file + " mod " + modTime + " build " + lastBuildTime);
				// need to add 1000 since lastModTime is only accurate to a second on some (all?) platforms
				if (modTime + 1000 > lastBuildTime) {
					ret.add(file);
				}
			}
		} else {
			ret.addAll(modifiedFiles);
		}
		ret.addAll(affectedFiles);
		return ret;
	}

	private Collection<BinarySourceFile> getModifiedBinaryFiles() {
		return getModifiedBinaryFiles(lastSuccessfulBuildTime);
	}

	Collection<BinarySourceFile> getModifiedBinaryFiles(long lastBuildTime) {
		List<BinarySourceFile> ret = new ArrayList<>();
		// not our job to account for new and deleted files
		for (BinarySourceFile bsfile : buildConfig.getBinaryFiles()) {
			File file = bsfile.binSrc;
			if (!file.exists()) {
				continue;
			}

			long modTime = file.lastModified();
			// System.out.println("check: " + file + " mod " + modTime + " build " + lastBuildTime);
			// need to add 1000 since lastModTime is only accurate to a second on some (all?) platforms
			if (modTime + 1000 >= lastBuildTime) {
				ret.add(bsfile);
			}
		}
		return ret;
	}

	private void recordDecision(String decision) {
		getListener().recordDecision(decision);
	}

	/**
	 * Analyse .class files in the directory specified, if they have changed since the last successful build then see if we can
	 * determine which source files in our project depend on the change. If we can then we can still do an incremental build, if we
	 * can't then we have to do a full build.
	 * 
	 */
	private int classFileChangedInDirSinceLastBuildRequiringFullBuild(File dir, int pathid) {

		if (!dir.isDirectory()) {
			if (listenerDefined()) {
				recordDecision("ClassFileChangeChecking: not a directory so forcing full build: '" + dir.getPath() + "'");
			}
			return CLASS_FILE_CHANGED_THAT_NEEDS_FULL_BUILD;
		}

		// Are we managing that output directory?
		AjState state = IncrementalStateManager.findStateManagingOutputLocation(dir);
		if (listenerDefined()) {
			if (state != null) {
				recordDecision("ClassFileChangeChecking: found state instance managing output location : " + dir);
			} else {
				recordDecision("ClassFileChangeChecking: failed to find a state instance managing output location : " + dir + " (could be getting managed by JDT)");
			}
		}

		// pr268827 - this guard will cause us to exit quickly if the state says there really is
		// nothing of interest. This will not catch the case where a user modifies the .class files outside of
		// eclipse because the state will not be aware of it. But that seems an unlikely scenario and
		// we are paying a heavy price to check it
		if (state != null && !state.hasAnyStructuralChangesSince(lastSuccessfulBuildTime)) {
			if (listenerDefined()) {
				getListener().recordDecision("ClassFileChangeChecking: no reported changes in that state");
			}
			return CLASS_FILE_NO_CHANGES;
		}

		if (state == null) {
			// This may be because the directory is the output path of a Java project upon which we depend
			// we need to call back into AJDT to ask about that projects state.
			CompilationResultDestinationManager crdm = buildConfig.getCompilationResultDestinationManager();
			if (crdm != null) {
				int i = crdm.discoverChangesSince(dir, lastSuccessfulBuildTime);
				// 0 = dontknow if it has changed
				// 1 = definetly not changed at all
				// further numbers can determine more granular changes
				if (i == 1) {
					if (listenerDefined()) {
						getListener().recordDecision(
								"ClassFileChangeChecking: queried JDT and '" + dir
										+ "' is apparently unchanged so not performing timestamp check");
					}
					return CLASS_FILE_NO_CHANGES;
				}
			}
		}

		List<File> classFiles = FileUtil.listClassFiles(dir);

		for (File classFile : classFiles) {
			if (CHECK_STATE_FIRST && state != null) {
				// Next section reworked based on bug 270033:
				// if it is an aspect we may or may not be in trouble depending on whether (a) we depend on it (b) it is on the
				// classpath or the aspectpath
				if (state.isAspect(classFile)) {
					boolean hasStructuralChanges = state.hasStructuralChangedSince(classFile, lastSuccessfulBuildTime);
					if (hasStructuralChanges || isTypeWeReferTo(classFile)) {
						if (hasStructuralChanges) {
							if (listenerDefined()) {
								getListener().recordDecision(
										"ClassFileChangeChecking: aspect found that has structurally changed : " + classFile);
							}
							return CLASS_FILE_CHANGED_THAT_NEEDS_FULL_BUILD;
						} else {
							// must be 'isTypeWeReferTo()'
							if (pathid == PATHID_CLASSPATH) {
								if (listenerDefined()) {
									getListener().recordDecision(
											"ClassFileChangeChecking: aspect found that this project refers to : " + classFile
													+ " but only referred to via classpath");
								}
							} else {
								if (listenerDefined()) {
									getListener().recordDecision(
											"ClassFileChangeChecking: aspect found that this project refers to : " + classFile
													+ " from either inpath/aspectpath, switching to full build");
								}
								return CLASS_FILE_CHANGED_THAT_NEEDS_FULL_BUILD;
							}
						}

					} else {
						// it is an aspect but we don't refer to it:
						// - for CLASSPATH I think this is OK, we can continue and try an
						// incremental build
						// - for ASPECTPATH we don't know what else might be touched in this project
						// and must rebuild
						if (pathid == PATHID_CLASSPATH) {
							if (listenerDefined()) {
								getListener()
										.recordDecision(
												"ClassFileChangeChecking: found aspect on classpath but this project doesn't reference it, continuing to try for incremental build : "
														+ classFile);
							}
						} else {
							if (listenerDefined()) {
								getListener().recordDecision(
										"ClassFileChangeChecking: found aspect on aspectpath/inpath - can't determine if this project is affected, must full build: "
												+ classFile);
							}
							return CLASS_FILE_CHANGED_THAT_NEEDS_FULL_BUILD;
						}
					}

				}
				if (state.hasStructuralChangedSince(classFile, lastSuccessfulBuildTime)) {
					if (listenerDefined()) {
						getListener().recordDecision("ClassFileChangeChecking: structural change detected in : " + classFile);
					}
					isTypeWeReferTo(classFile);
				}
			} else {
				long modTime = classFile.lastModified();
				if ((modTime + 1000) >= lastSuccessfulBuildTime) {
					// so the class on disk has changed since the last successful build for this state object

					// BUG? we stop on the first change that leads us to an incremental build, surely we need to continue and look
					// at all files incase another change means we need to incremental a bit more stuff?

					// To work out if it is a real change we should ask any state
					// object managing the output location whether the file has
					// structurally changed or not
					if (state != null) {
						if (state.isAspect(classFile)) {
							if (state.hasStructuralChangedSince(classFile, lastSuccessfulBuildTime) || isTypeWeReferTo(classFile)) {
								// further improvements possible
								if (listenerDefined()) {
									getListener().recordDecision(
											"ClassFileChangeChecking: aspect found that has structurally changed or that this project depends upon : "
													+ classFile);
								}
								return CLASS_FILE_CHANGED_THAT_NEEDS_FULL_BUILD;
							} else {
								// it is an aspect but we don't refer to it:
								// - for CLASSPATH I think this is OK, we can continue and try an
								// incremental build
								// - for ASPECTPATH we don't know what else might be touched in this project
								// and must rebuild
								if (pathid == PATHID_CLASSPATH) {
									if (listenerDefined()) {
										getListener()
												.recordDecision(
														"ClassFileChangeChecking: found aspect on classpath but this project doesn't reference it, continuing to try for incremental build : "
																+ classFile);
									}
								} else {
									if (listenerDefined()) {
										getListener().recordDecision(
												"ClassFileChangeChecking: found aspect on aspectpath/inpath - can't determine if this project is affected, must full build: "
														+ classFile);
									}
									return CLASS_FILE_CHANGED_THAT_NEEDS_FULL_BUILD;
								}
							}
						}
						if (state.hasStructuralChangedSince(classFile, lastSuccessfulBuildTime)) {
							if (listenerDefined()) {
								getListener().recordDecision(
										"ClassFileChangeChecking: structural change detected in : " + classFile);
							}
							isTypeWeReferTo(classFile);
						} else {
							if (listenerDefined()) {
								getListener().recordDecision(
										"ClassFileChangeChecking: change detected in " + classFile + " but it is not structural");
							}
						}
					} else {
						// No state object to ask, so it only matters if we know which type depends on this file
						if (isTypeWeReferTo(classFile)) {
							return CLASS_FILE_CHANGED_THAT_NEEDS_INCREMENTAL_BUILD;
						} else {
							return CLASS_FILE_NO_CHANGES;
						}
					}
				}
			}
		}
		return CLASS_FILE_NO_CHANGES;
	}

	private boolean isAspect(File file) {
		return aspectClassFiles.contains(file.getAbsolutePath());
	}

	@SuppressWarnings("rawtypes")
	public static class SoftHashMap extends AbstractMap {

		private final Map map;

		private final ReferenceQueue rq = new ReferenceQueue();

		public SoftHashMap(Map map) {
			this.map = map;
		}

		public SoftHashMap() {
			this(new HashMap());
		}

		public SoftHashMap(Map map, boolean b) {
			this(map);
		}

		class SoftReferenceKnownKey extends SoftReference {

			private final Object key;

			@SuppressWarnings("unchecked")
			SoftReferenceKnownKey(Object k, Object v) {
				super(v, rq);
				this.key = k;
			}
		}

		private void processQueue() {
			SoftReferenceKnownKey sv = null;
			while ((sv = (SoftReferenceKnownKey) rq.poll()) != null) {
				map.remove(sv.key);
			}
		}

		@Override
		public Object get(Object key) {
			SoftReferenceKnownKey value = (SoftReferenceKnownKey) map.get(key);
			if (value == null) {
				return null;
			}
			if (value.get() == null) {
				// it got GC'd
				map.remove(value.key);
				return null;
			} else {
				return value.get();
			}
		}

		@Override
		public Object put(Object k, Object v) {
			processQueue();
			return map.put(k, new SoftReferenceKnownKey(k, v));
		}

		@Override
		public Set entrySet() {
			return map.entrySet();
		}

		@Override
		public void clear() {
			processQueue();
			map.clear();
		}

		@Override
		public int size() {
			processQueue();
			return map.size();
		}

		@Override
		public Object remove(Object k) {
			processQueue();
			SoftReferenceKnownKey value = (SoftReferenceKnownKey) map.remove(k);
			if (value == null) {
				return null;
			}
			if (value.get() != null) {
				return value.get();
			}
			return null;
		}
	}

	/**
	 * If a class file has changed in a path on our classpath, it may not be for a type that any of our source files care about.
	 * This method checks if any of our source files have a dependency on the class in question and if not, we don't consider it an
	 * interesting change.
	 */
	private boolean isTypeWeReferTo(File file) {
		String fpath = file.getAbsolutePath();
		int finalSeparator = fpath.lastIndexOf(File.separator);
		String baseDir = fpath.substring(0, finalSeparator);
		String theFile = fpath.substring(finalSeparator + 1);
		SoftHashMap classNames = (SoftHashMap) fileToClassNameMap.get(baseDir);
		if (classNames == null) {
			classNames = new SoftHashMap();
			fileToClassNameMap.put(baseDir, classNames);
		}
		char[] className = (char[]) classNames.get(theFile);
		if (className == null) {
			// if (listenerDefined())
			// getListener().recordDecision("Cache miss, looking up classname for : " + fpath);

			ClassFileReader cfr;
			try {
				cfr = ClassFileReader.read(file);
			} catch (ClassFormatException e) {
				return true;
			} catch (IOException e) {
				return true;
			}
			className = cfr.getName();
			classNames.put(theFile, className);
			// } else {
			// if (listenerDefined())
			// getListener().recordDecision("Cache hit, looking up classname for : " + fpath);
		}

		char[][][] qualifiedNames = null;
		char[][] simpleNames = null;
		if (CharOperation.indexOf('/', className) != -1) {
			qualifiedNames = new char[1][][];
			qualifiedNames[0] = CharOperation.splitOn('/', className);
			qualifiedNames = ReferenceCollection.internQualifiedNames(qualifiedNames);
		} else {
			simpleNames = new char[1][];
			simpleNames[0] = className;
			simpleNames = ReferenceCollection.internSimpleNames(simpleNames, true);
		}
		int newlyAffectedFiles = 0;
		for (Map.Entry<File, ReferenceCollection> entry : references.entrySet()) {
			ReferenceCollection refs = entry.getValue();
			if (refs != null && refs.includes(qualifiedNames, simpleNames)) {
				if (listenerDefined()) {
					getListener().recordDecision(
							toString() + ": type " + new String(className) + " is depended upon by '" + entry.getKey() + "'");
				}
				newlyAffectedFiles++;
				// possibly the beginnings of addressing the second point in 270033 comment 3
				// List/*ClassFile*/ cfs = (List)this.fullyQualifiedTypeNamesResultingFromCompilationUnit.get(entry.getKey());
				affectedFiles.add(entry.getKey());
			}
		}
		if (newlyAffectedFiles > 0) {
			return true;
		}
		if (listenerDefined()) {
			getListener().recordDecision(toString() + ": type " + new String(className) + " is not depended upon by this state");
		}
		return false;
	}

	// /**
	// * For a given class file, determine which source file it came from. This will only succeed if the class file is from a source
	// * file within this project.
	// */
	// private File getSourceFileForClassFile(File classfile) {
	// Set sourceFiles = fullyQualifiedTypeNamesResultingFromCompilationUnit.keySet();
	// for (Iterator sourceFileIterator = sourceFiles.iterator(); sourceFileIterator.hasNext();) {
	// File sourceFile = (File) sourceFileIterator.next();
	// List/* ClassFile */classesFromSourceFile = (List/* ClassFile */) fullyQualifiedTypeNamesResultingFromCompilationUnit
	// .get(sourceFile);
	// for (int i = 0; i < classesFromSourceFile.size(); i++) {
	// if (((ClassFile) classesFromSourceFile.get(i)).locationOnDisk.equals(classfile))
	// return sourceFile;
	// }
	// }
	// return null;
	// }

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		// null config means failed build i think as it is only set on successful full build?
		sb.append("AjState(").append((buildConfig == null ? "NULLCONFIG" : buildConfig.getConfigFile().toString())).append(")");
		return sb.toString();
	}

	/**
	 * Determine if a file has changed since a given time, using the local information recorded in the structural changes data
	 * structure.
	 * 
	 * @param file the file we are wondering about
	 * @param lastSuccessfulBuildTime the last build time for the state asking the question
	 */
	private boolean hasStructuralChangedSince(File file, long lastSuccessfulBuildTime) {
		// long lastModTime = file.lastModified();
		Long l = structuralChangesSinceLastFullBuild.get(file.getAbsolutePath());
		long strucModTime = -1;
		if (l != null) {
			strucModTime = l;
		} else {
			strucModTime = this.lastSuccessfulFullBuildTime;
		}
		// we now have:
		// 'strucModTime'-> the last time the class was structurally changed
		return (strucModTime > lastSuccessfulBuildTime);
	}

	/**
	 * Determine if anything has changed since a given time.
	 */
	private boolean hasAnyStructuralChangesSince(long lastSuccessfulBuildTime) {
		Set<Map.Entry<String, Long>> entries = structuralChangesSinceLastFullBuild.entrySet();
		for (Map.Entry<String, Long> entry : entries) {
			Long l = entry.getValue();
			if (l != null) {
				long lvalue = l;
				if (lvalue > lastSuccessfulBuildTime) {
					if (listenerDefined()) {
						getListener().recordDecision(
								"Seems this has changed " + entry.getKey() + "modtime=" + lvalue + " lsbt="
										+ this.lastSuccessfulFullBuildTime + "   incoming check value=" + lastSuccessfulBuildTime);
					}
					return true;
				}
			}
		}
		return (this.lastSuccessfulFullBuildTime > lastSuccessfulBuildTime);
	}

	/**
	 * Determine if something has changed on the classpath/inpath/aspectpath and a full build is required rather than an incremental
	 * one.
	 * 
	 * @param previousConfig the previous configuration used
	 * @param newConfig the new configuration being used
	 * @return true if full build required
	 */
	private boolean pathChange(AjBuildConfig previousConfig, AjBuildConfig newConfig) {
		int changes = newConfig.getChanged();

		if ((changes & (CLASSPATH_CHANGED | ASPECTPATH_CHANGED | INPATH_CHANGED | OUTPUTDESTINATIONS_CHANGED | INJARS_CHANGED)) != 0) {
			List<File> oldOutputLocs = getOutputLocations(previousConfig);

			Set<String> alreadyAnalysedPaths = new HashSet<>();

			List<String> oldClasspath = previousConfig.getClasspath();
			List<String> newClasspath = newConfig.getClasspath();
			if (stateListener != null) {
				stateListener.aboutToCompareClasspaths(oldClasspath, newClasspath);
			}
			if (classpathChangedAndNeedsFullBuild(oldClasspath, newClasspath, true, oldOutputLocs, alreadyAnalysedPaths)) {
				return true;
			}

			List<File> oldAspectpath = previousConfig.getAspectpath();
			List<File> newAspectpath = newConfig.getAspectpath();
			if (changedAndNeedsFullBuild(oldAspectpath, newAspectpath, true, oldOutputLocs, alreadyAnalysedPaths, PATHID_ASPECTPATH)) {
				return true;
			}

			List<File> oldInPath = previousConfig.getInpath();
			List<File> newInPath = newConfig.getInpath();
			if (changedAndNeedsFullBuild(oldInPath, newInPath, false, oldOutputLocs, alreadyAnalysedPaths, PATHID_INPATH)) {
				return true;
			}

			List<File> oldInJars = previousConfig.getInJars();
			List<File> newInJars = newConfig.getInJars();
			if (changedAndNeedsFullBuild(oldInJars, newInJars, false, oldOutputLocs, alreadyAnalysedPaths, PATHID_INPATH)) {
				return true;
			}
		} else if (newConfig.getClasspathElementsWithModifiedContents() != null) {
			// Although the classpath entries themselves are the same as before, the contents of one of the
			// directories on the classpath has changed - rather than go digging around to find it, let's ask
			// the compiler configuration. This will allow for projects with long classpaths where classpaths
			// are also capturing project dependencies - when a project we depend on is rebuilt, we can just check
			// it as a standalone element on our classpath rather than going through them all
			List<String> modifiedCpElements = newConfig.getClasspathElementsWithModifiedContents();
			for (String modifiedCpElement : modifiedCpElements) {
				File cpElement = new File(modifiedCpElement);
				if (cpElement.exists() && !cpElement.isDirectory()) {
					if (cpElement.lastModified() > lastSuccessfulBuildTime) {
						return true;
					}
				} else {
					int classFileChanges = classFileChangedInDirSinceLastBuildRequiringFullBuild(cpElement, PATHID_CLASSPATH);
					if (classFileChanges == CLASS_FILE_CHANGED_THAT_NEEDS_FULL_BUILD) {
						return true;
					}
				}
			}
		}

		return false;
	}

	/**
	 * Return a list of the output locations - this includes any 'default' output location and then any known by a registered
	 * CompilationResultDestinationManager.
	 * 
	 * @param config the build configuration for which the output locations should be determined
	 * @return a list of file objects
	 */
	private List<File> getOutputLocations(AjBuildConfig config) {
		List<File> outputLocs = new ArrayList<>();
		// Is there a default location?
		if (config.getOutputDir() != null) {
			try {
				outputLocs.add(config.getOutputDir().getCanonicalFile());
			} catch (IOException e) {
			}
		}
		if (config.getCompilationResultDestinationManager() != null) {
			List<File> dirs = config.getCompilationResultDestinationManager().getAllOutputLocations();
			for (File f : dirs) {
				try {
					File cf = f.getCanonicalFile();
					if (!outputLocs.contains(cf)) {
						outputLocs.add(cf);
					}
				} catch (IOException e) {
				}
			}
		}
		return outputLocs;
	}

	private File getOutputLocationFor(AjBuildConfig config, File aResourceFile) {
		if (config.getCompilationResultDestinationManager() != null) {
			File outputLoc = config.getCompilationResultDestinationManager().getOutputLocationForResource(aResourceFile);
			if (outputLoc != null) {
				return outputLoc;
			}
		}
		// Is there a default location?
		if (config.getOutputDir() != null) {
			return config.getOutputDir();
		}
		return null;
	}

	/**
	 * Check the old and new paths, if they vary by length or individual elements then that is considered a change. Or if the last
	 * modified time of a path entry has changed (or last modified time of a classfile in that path entry has changed) then return
	 * true. The outputlocations are supplied so they can be 'ignored' in the comparison.
	 * 
	 * @param oldPath
	 * @param newPath
	 * @param checkClassFiles whether to examine individual class files within directories
	 * @param outputLocs the output locations that should be ignored if they occur on the paths being compared
	 * @return true if a change is detected that requires a full build
	 */
	private boolean changedAndNeedsFullBuild(List oldPath, List newPath, boolean checkClassFiles, List<File> outputLocs,
			Set<String> alreadyAnalysedPaths, int pathid) {
		if (oldPath.size() != newPath.size()) {
			return true;
		}
		for (int i = 0; i < oldPath.size(); i++) {
			if (!oldPath.get(i).equals(newPath.get(i))) {
				return true;
			}
			Object o = oldPath.get(i); // String on classpath, File on other paths
			File f = null;
			if (o instanceof String) {
				f = new File((String) o);
			} else {
				f = (File) o;
			}
			if (f.exists() && !f.isDirectory() && (f.lastModified() >= lastSuccessfulBuildTime)) {
				return true;
			}
			if (checkClassFiles && f.exists() && f.isDirectory()) {

				// We should use here a list/set of directories we know have or have not changed - some kind of
				// List<File> buildConfig.getClasspathEntriesWithChangedContents()
				// and then only proceed to look inside directories if it is one of these, ignoring others -
				// that should save a massive amount of processing for incremental builds in a multi project scenario

				boolean foundMatch = false;
				for (Iterator<File> iterator = outputLocs.iterator(); !foundMatch && iterator.hasNext();) {
					File dir = iterator.next();
					if (f.equals(dir)) {
						foundMatch = true;
					}
				}
				if (!foundMatch) {
					if (!alreadyAnalysedPaths.contains(f.getAbsolutePath())) { // Do not check paths more than once
						alreadyAnalysedPaths.add(f.getAbsolutePath());
						int classFileChanges = classFileChangedInDirSinceLastBuildRequiringFullBuild(f, pathid);
						if (classFileChanges == CLASS_FILE_CHANGED_THAT_NEEDS_FULL_BUILD) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	/**
	 * Check the old and new paths, if they vary by length or individual elements then that is considered a change. Or if the last
	 * modified time of a path entry has changed (or last modified time of a classfile in that path entry has changed) then return
	 * true. The outputlocations are supplied so they can be 'ignored' in the comparison.
	 * 
	 * @param oldPath
	 * @param newPath
	 * @param checkClassFiles whether to examine individual class files within directories
	 * @param outputLocs the output locations that should be ignored if they occur on the paths being compared
	 * @return true if a change is detected that requires a full build
	 */
	private boolean classpathChangedAndNeedsFullBuild(List<String> oldPath, List<String> newPath, boolean checkClassFiles,
			List<File> outputLocs, Set<String> alreadyAnalysedPaths) {
		if (oldPath.size() != newPath.size()) {
			return true;
		}
		for (int i = 0; i < oldPath.size(); i++) {
			if (!oldPath.get(i).equals(newPath.get(i))) {
				return true;
			}
			File f = new File(oldPath.get(i));
			if (f.exists() && !f.isDirectory() && (f.lastModified() >= lastSuccessfulBuildTime)) {
				return true;
			}
			if (checkClassFiles && f.exists() && f.isDirectory()) {

				// We should use here a list/set of directories we know have or have not changed - some kind of
				// List<File> buildConfig.getClasspathEntriesWithChangedContents()
				// and then only proceed to look inside directories if it is one of these, ignoring others -
				// that should save a massive amount of processing for incremental builds in a multi project scenario

				boolean foundMatch = false;
				for (Iterator<File> iterator = outputLocs.iterator(); !foundMatch && iterator.hasNext();) {
					File dir = iterator.next();
					if (f.equals(dir)) {
						foundMatch = true;
					}
				}
				if (!foundMatch) {
					if (!alreadyAnalysedPaths.contains(f.getAbsolutePath())) { // Do not check paths more than once
						alreadyAnalysedPaths.add(f.getAbsolutePath());
						int classFileChanges = classFileChangedInDirSinceLastBuildRequiringFullBuild(f, PATHID_CLASSPATH);
						if (classFileChanges == CLASS_FILE_CHANGED_THAT_NEEDS_FULL_BUILD) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	public Set<File> getFilesToCompile(boolean firstPass) {
		Set<File> thisTime = new HashSet<>();
		if (firstPass) {
			compiledSourceFiles = new HashSet<>();
			Collection<File> modifiedFiles = getModifiedFiles();
			// System.out.println("modified: " + modifiedFiles);
			thisTime.addAll(modifiedFiles);
			// ??? eclipse IncrementalImageBuilder appears to do this
			// for (Iterator i = modifiedFiles.iterator(); i.hasNext();) {
			// File file = (File) i.next();
			// addDependentsOf(file);
			// }

			if (addedFiles != null) {
				for (File o : addedFiles) {
					// TODO isn't it a set?? why do this
					if (!thisTime.contains(o)) {
						thisTime.add(o);
					}
				}
				// thisTime.addAll(addedFiles);
			}

			deleteClassFiles();
			// Do not delete resources on incremental build, AJDT will handle
			// copying updates to the output folder. AspectJ only does a copy
			// of them on full build (see copyResourcesToDestination() call
			// in AjBuildManager)
			// deleteResources();

			addAffectedSourceFiles(thisTime, thisTime);
		} else {
			addAffectedSourceFiles(thisTime, compiledSourceFiles);
		}
		compiledSourceFiles = thisTime;
		return thisTime;
	}

	private boolean maybeIncremental() {
		return (FORCE_INCREMENTAL_DURING_TESTING || this.couldBeSubsequentIncrementalBuild);
	}

	public Map<String, List<UnwovenClassFile>> getBinaryFilesToCompile(boolean firstTime) {
		if (lastSuccessfulBuildTime == -1 || buildConfig == null || !maybeIncremental()) {
			return binarySourceFiles;
		}
		// else incremental...
		Map<String, List<UnwovenClassFile>> toWeave = new HashMap<>();
		if (firstTime) {
			List<BinarySourceFile> addedOrModified = new ArrayList<>();
			addedOrModified.addAll(addedBinaryFiles);
			addedOrModified.addAll(getModifiedBinaryFiles());
			for (BinarySourceFile bsf : addedOrModified) {
				UnwovenClassFile ucf = createUnwovenClassFile(bsf);
				if (ucf == null) {
					continue;
				}
				List<UnwovenClassFile> ucfs = new ArrayList<>();
				ucfs.add(ucf);
				recordTypeChanged(ucf.getClassName());
				binarySourceFiles.put(bsf.binSrc.getPath(), ucfs);
				List<ClassFile> cfs = new ArrayList<>(1);
				cfs.add(getClassFileFor(ucf));
				this.inputClassFilesBySource.put(bsf.binSrc.getPath(), cfs);
				toWeave.put(bsf.binSrc.getPath(), ucfs);
			}
			deleteBinaryClassFiles();
		} else {
			// return empty set... we've already done our bit.
		}
		return toWeave;
	}

	/**
	 * Called when a path change is about to trigger a full build, but we haven't cleaned up from the last incremental build...
	 */
	private void removeAllResultsOfLastBuild() {
		// remove all binarySourceFiles, and all classesFromName...
		for (List<ClassFile> cfs : this.inputClassFilesBySource.values()) {
			for (ClassFile cf : cfs) {
				cf.deleteFromFileSystem(buildConfig);
			}
		}
		for (File f : classesFromName.values()) {
			new ClassFile("", f).deleteFromFileSystem(buildConfig);
		}
		Set<Map.Entry<String, File>> resourceEntries = resources.entrySet();
		for (Map.Entry<String, File> resourcePair : resourceEntries) {
			File sourcePath = resourcePair.getValue();
			File outputLoc = getOutputLocationFor(buildConfig, sourcePath);
			if (outputLoc != null) {
				outputLoc = new File(outputLoc, resourcePair.getKey());
				if (!outputLoc.getPath().equals(sourcePath.getPath()) && outputLoc.exists()) {
					outputLoc.delete();
					if (buildConfig.getCompilationResultDestinationManager() != null) {
						buildConfig.getCompilationResultDestinationManager().reportFileRemove(outputLoc.getPath(),
								CompilationResultDestinationManager.FILETYPE_RESOURCE);
					}
				}
			}
		}
	}

	private void deleteClassFiles() {
		if (deletedFiles == null) {
			return;
		}
		for (File deletedFile : deletedFiles) {
			addDependentsOf(deletedFile);

			List<ClassFile> cfs = this.fullyQualifiedTypeNamesResultingFromCompilationUnit.get(deletedFile);
			this.fullyQualifiedTypeNamesResultingFromCompilationUnit.remove(deletedFile);

			if (cfs != null) {
				for (ClassFile cf : cfs) {
					deleteClassFile(cf);
				}
			}

		}
	}

	private void deleteBinaryClassFiles() {
		// range of bsf is ucfs, domain is files (.class and jars) in inpath/jars
		for (BinarySourceFile deletedFile : deletedBinaryFiles) {
			List<ClassFile> cfs = this.inputClassFilesBySource.get(deletedFile.binSrc.getPath());
			for (ClassFile cf : cfs) {
				deleteClassFile(cf);
			}
			this.inputClassFilesBySource.remove(deletedFile.binSrc.getPath());
		}
	}

	// private void deleteResources() {
	// List oldResources = new ArrayList();
	// oldResources.addAll(resources);
	//
	// // note - this deliberately ignores resources in jars as we don't yet handle jar changes
	// // with incremental compilation
	// for (Iterator i = buildConfig.getInpath().iterator(); i.hasNext();) {
	// File inPathElement = (File) i.next();
	// if (inPathElement.isDirectory() && AjBuildManager.COPY_INPATH_DIR_RESOURCES) {
	// deleteResourcesFromDirectory(inPathElement, oldResources);
	// }
	// }
	//
	// if (buildConfig.getSourcePathResources() != null) {
	// for (Iterator i = buildConfig.getSourcePathResources().keySet().iterator(); i.hasNext();) {
	// String resource = (String) i.next();
	// maybeDeleteResource(resource, oldResources);
	// }
	// }
	//
	// // oldResources need to be deleted...
	// for (Iterator iter = oldResources.iterator(); iter.hasNext();) {
	// String victim = (String) iter.next();
	// List outputDirs = getOutputLocations(buildConfig);
	// for (Iterator iterator = outputDirs.iterator(); iterator.hasNext();) {
	// File dir = (File) iterator.next();
	// File f = new File(dir, victim);
	// if (f.exists()) {
	// f.delete();
	// }
	// resources.remove(victim);
	// }
	// }
	// }

	// private void maybeDeleteResource(String resName, List oldResources) {
	// if (resources.contains(resName)) {
	// oldResources.remove(resName);
	// List outputDirs = getOutputLocations(buildConfig);
	// for (Iterator iterator = outputDirs.iterator(); iterator.hasNext();) {
	// File dir = (File) iterator.next();
	// File source = new File(dir, resName);
	// if (source.exists() && (source.lastModified() >= lastSuccessfulBuildTime)) {
	// resources.remove(resName); // will ensure it is re-copied
	// }
	// }
	// }
	// }

	// private void deleteResourcesFromDirectory(File dir, List oldResources) {
	// File[] files = FileUtil.listFiles(dir, new FileFilter() {
	// public boolean accept(File f) {
	// boolean accept = !(f.isDirectory() || f.getName().endsWith(".class"));
	// return accept;
	// }
	// });
	//
	// // For each file, add it either as a real .class file or as a resource
	// for (int i = 0; i < files.length; i++) {
	// // ASSERT: files[i].getAbsolutePath().startsWith(inFile.getAbsolutePath()
	// // or we are in trouble...
	// String filename = null;
	// try {
	// filename = files[i].getCanonicalPath().substring(dir.getCanonicalPath().length() + 1);
	// } catch (IOException e) {
	// // we are in trouble if this happens...
	// IMessage msg = new Message("call to getCanonicalPath() failed for file " + files[i] + " with: " + e.getMessage(),
	// new SourceLocation(files[i], 0), false);
	// buildManager.handler.handleMessage(msg);
	// filename = files[i].getAbsolutePath().substring(dir.getAbsolutePath().length() + 1);
	// }
	//
	// maybeDeleteResource(filename, oldResources);
	// }
	// }

	private void deleteClassFile(ClassFile cf) {
		classesFromName.remove(cf.fullyQualifiedTypeName);
		weaver.deleteClassFile(cf.fullyQualifiedTypeName);
		cf.deleteFromFileSystem(buildConfig);
	}

	private UnwovenClassFile createUnwovenClassFile(AjBuildConfig.BinarySourceFile bsf) {
		UnwovenClassFile ucf = null;
		try {
			File outputDir = buildConfig.getOutputDir();
			if (buildConfig.getCompilationResultDestinationManager() != null) {
				// createUnwovenClassFile is called only for classes that are on the inpath,
				// all inpath classes are put in the defaultOutputLocation, therefore,
				// this is the output dir
				outputDir = buildConfig.getCompilationResultDestinationManager().getDefaultOutputLocation();
			}
			ucf = weaver.addClassFile(bsf.binSrc, bsf.fromInPathDirectory, outputDir);
		} catch (IOException ex) {
			IMessage msg = new Message("can't read class file " + bsf.binSrc.getPath(), new SourceLocation(bsf.binSrc, 0), false);
			buildManager.handler.handleMessage(msg);
		}
		return ucf;
	}

	public void noteResult(InterimCompilationResult result) {
		if (!maybeIncremental()) {
			return;
		}

		File sourceFile = new File(result.fileName());
		CompilationResult cr = result.result();

		references.put(sourceFile, new ReferenceCollection(cr.qualifiedReferences, cr.simpleNameReferences,cr.rootReferences));

		UnwovenClassFile[] unwovenClassFiles = result.unwovenClassFiles();
		for (UnwovenClassFile unwovenClassFile : unwovenClassFiles) {
			File lastTimeRound = classesFromName.get(unwovenClassFile.getClassName());
			recordClassFile(unwovenClassFile, lastTimeRound);
			String name = unwovenClassFile.getClassName();
			if (lastTimeRound == null) {
				deltaAddedClasses.add(name);
			}
			classesFromName.put(name, new File(unwovenClassFile.getFilename()));
		}

		// need to do this before types are deleted from the World...
		recordWhetherCompilationUnitDefinedAspect(sourceFile, cr);
		deleteTypesThatWereInThisCompilationUnitLastTimeRoundButHaveBeenDeletedInThisIncrement(sourceFile, unwovenClassFiles);

		recordFQNsResultingFromCompilationUnit(sourceFile, result);
	}

	public void noteNewResult(CompilationResult cr) {
		// if (!maybeIncremental()) {
		// return;
		// }
		//
		// // File sourceFile = new File(result.fileName());
		// // CompilationResult cr = result.result();
		// if (new String(cr.getFileName()).indexOf("C") != -1) {
		// cr.references.put(new String(cr.getFileName()),
		// new ReferenceCollection(cr.qualifiedReferences, cr.simpleNameReferences));
		// int stop = 1;
		// }

		// references.put(sourceFile, new ReferenceCollection(cr.qualifiedReferences, cr.simpleNameReferences));
		//
		// UnwovenClassFile[] unwovenClassFiles = cr.unwovenClassFiles();
		// for (int i = 0; i < unwovenClassFiles.length; i++) {
		// File lastTimeRound = (File) classesFromName.get(unwovenClassFiles[i].getClassName());
		// recordClassFile(unwovenClassFiles[i], lastTimeRound);
		// classesFromName.put(unwovenClassFiles[i].getClassName(), new File(unwovenClassFiles[i].getFilename()));
		// }

		// need to do this before types are deleted from the World...
		// recordWhetherCompilationUnitDefinedAspect(sourceFile, cr);
		// deleteTypesThatWereInThisCompilationUnitLastTimeRoundButHaveBeenDeletedInThisIncrement(sourceFile, unwovenClassFiles);
		//
		// recordFQNsResultingFromCompilationUnit(sourceFile, result);
	}

	/**
	 * @param sourceFile
	 * @param unwovenClassFiles
	 */
	private void deleteTypesThatWereInThisCompilationUnitLastTimeRoundButHaveBeenDeletedInThisIncrement(File sourceFile,
			UnwovenClassFile[] unwovenClassFiles) {
		List<ClassFile> classFiles = this.fullyQualifiedTypeNamesResultingFromCompilationUnit.get(sourceFile);
		if (classFiles != null) {

			for (UnwovenClassFile unwovenClassFile : unwovenClassFiles) {
				// deleting also deletes types from the weaver... don't do this if they are
				// still present this time around...
				removeFromClassFilesIfPresent(unwovenClassFile.getClassName(), classFiles);
			}
			for (ClassFile cf : classFiles) {
				recordTypeChanged(cf.fullyQualifiedTypeName);
				resolvedTypeStructuresFromLastBuild.remove(cf.fullyQualifiedTypeName);
				// }
				// for (ClassFile cf : classFiles) {
				deleteClassFile(cf);
			}
		}
	}

	private void removeFromClassFilesIfPresent(String className, List<ClassFile> classFiles) {
		ClassFile victim = null;
		for (ClassFile cf : classFiles) {
			if (cf.fullyQualifiedTypeName.equals(className)) {
				victim = cf;
				break;
			}
		}
		if (victim != null) {
			classFiles.remove(victim);
		}
	}

	/**
	 * Record the fully-qualified names of the types that were declared in the given source file.
	 * 
	 * @param sourceFile, the compilation unit
	 * @param icr, the CompilationResult from compiling it
	 */
	private void recordFQNsResultingFromCompilationUnit(File sourceFile, InterimCompilationResult icr) {
		List<ClassFile> classFiles = new ArrayList<>();
		UnwovenClassFile[] types = icr.unwovenClassFiles();
		for (UnwovenClassFile type : types) {
			classFiles.add(new ClassFile(type.getClassName(), new File(type.getFilename())));
		}
		this.fullyQualifiedTypeNamesResultingFromCompilationUnit.put(sourceFile, classFiles);
	}

	/**
	 * If this compilation unit defined an aspect, we need to know in case it is modified in a future increment.
	 * 
	 * @param sourceFile
	 * @param cr
	 */
	private void recordWhetherCompilationUnitDefinedAspect(File sourceFile, CompilationResult cr) {
		this.sourceFilesDefiningAspects.remove(sourceFile);

		if (cr != null) {
			Map compiledTypes = cr.compiledTypes;
			if (compiledTypes != null) {
				for (char[] className : (Iterable<char[]>) compiledTypes.keySet()) {
					String typeName = new String(className).replace('/', '.');
					if (!typeName.contains(BcelWeaver.SYNTHETIC_CLASS_POSTFIX)) {
						ResolvedType rt = world.resolve(typeName);
						if (rt.isMissing()) {
							// This can happen in a case where another problem has occurred that prevented it being
							// correctly added to the world. Eg. pr148285. Duplicate types
							// throw new IllegalStateException("Type '" + rt.getSignature() + "' not found in world!");
						} else if (rt.isAspect()) {
							this.sourceFilesDefiningAspects.add(sourceFile);
							break;
						}
					}
				}
			}
		}

	}

	// private UnwovenClassFile removeFromPreviousIfPresent(UnwovenClassFile cf, InterimCompilationResult previous) {
	// if (previous == null)
	// return null;
	// UnwovenClassFile[] unwovenClassFiles = previous.unwovenClassFiles();
	// for (int i = 0; i < unwovenClassFiles.length; i++) {
	// UnwovenClassFile candidate = unwovenClassFiles[i];
	// if ((candidate != null) && candidate.getFilename().equals(cf.getFilename())) {
	// unwovenClassFiles[i] = null;
	// return candidate;
	// }
	// }
	// return null;
	// }

	private void recordClassFile(UnwovenClassFile thisTime, File lastTime) {
		if (simpleStrings == null) {
			// batch build
			// record resolved type for structural comparisons in future increments
			// this records a second reference to a structure already held in memory
			// by the world.
			ResolvedType rType = world.resolve(thisTime.getClassName());
			if (!rType.isMissing()) {
				try {
					ClassFileReader reader = new ClassFileReader(thisTime.getBytes(), null);
					boolean isAspect = false;
					if (rType instanceof ReferenceType && ((ReferenceType) rType).getDelegate() != null) {
						isAspect = ((ReferenceType) rType).isAspect();
					}
					this.resolvedTypeStructuresFromLastBuild.put(thisTime.getClassName(), new CompactTypeStructureRepresentation(
							reader, isAspect));
				} catch (ClassFormatException cfe) {
					throw new BCException("Unexpected problem processing class", cfe);
				}
			}
			return;
		}

		CompactTypeStructureRepresentation existingStructure = this.resolvedTypeStructuresFromLastBuild
				.get(thisTime.getClassName());
		ResolvedType newResolvedType = world.resolve(thisTime.getClassName());
		if (!newResolvedType.isMissing()) {
			try {
				ClassFileReader reader = new ClassFileReader(thisTime.getBytes(), null);
				boolean isAspect = false;
				if (newResolvedType instanceof ReferenceType && ((ReferenceType) newResolvedType).getDelegate() != null) {
					isAspect = ((ReferenceType) newResolvedType).isAspect();
				}
				this.resolvedTypeStructuresFromLastBuild.put(thisTime.getClassName(), new CompactTypeStructureRepresentation(
						reader, isAspect));
			} catch (ClassFormatException cfe) {
				try {
					String s = System.getProperty("aspectj.debug377096","false");
					if (s.equalsIgnoreCase("true")) {
						String location = System.getProperty("java.io.tmpdir","/tmp");
						String name = thisTime.getClassName();
						File f = File.createTempFile(location+File.separator+name, ".class");
						StringBuilder debug = new StringBuilder();
						debug.append("Debug377096: Dumping class called "+name+" to "+f.getName()+" size:"+thisTime.getBytes().length);
						DataOutputStream dos = new DataOutputStream(new FileOutputStream(f));
						dos.write(thisTime.getBytes());
						dos.close();
						throw new BCException(debug.toString(), cfe);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				throw new BCException("Unexpected problem processing class", cfe);
			}
		}

		if (lastTime == null) {
			recordTypeChanged(thisTime.getClassName());
			return;
		}

		if (newResolvedType.isMissing()) {
			return;
		}
		world.ensureAdvancedConfigurationProcessed();
		byte[] newBytes = thisTime.getBytes();
		try {
			ClassFileReader reader = new ClassFileReader(newBytes, lastTime.getAbsolutePath().toCharArray());
			// ignore local types since they're only visible inside a single method
			if (!(reader.isLocal() || reader.isAnonymous())) {
				if (hasStructuralChanges(reader, existingStructure)) {
					if (listenerDefined()) {
//					if (world.forDEBUG_structuralChangesCode) {
//						System.err.println("Detected a structural change in " + thisTime.getFilename());
						printStructuralChanges(thisTime.getFilename(),reader, existingStructure);
					}
					structuralChangesSinceLastFullBuild.put(thisTime.getFilename(), currentBuildTime);
					recordTypeChanged(new String(reader.getName()).replace('/', '.'));
				}
			}
		} catch (ClassFormatException e) {
			recordTypeChanged(thisTime.getClassName());
		}
	}

	/**
	 * Compare the class structure of the new intermediate (unwoven) class with the existingResolvedType of the same class that we
	 * have in the world, looking for any structural differences (and ignoring aj members resulting from weaving....)
	 * 
	 * Some notes from Andy... lot of problems here, which I've eventually resolved by building the compactstructure based on a
	 * classfilereader, rather than on a ResolvedType. There are accessors for inner types and funky fields that the compiler
	 * creates to support the language - for non-static inner types it also mangles ctors to be prefixed with an instance of the
	 * surrounding type.
	 * 
	 * @param reader
	 * @param existingType
	 * @return
	 */
	private boolean hasStructuralChanges(ClassFileReader reader, CompactTypeStructureRepresentation existingType) {
		if (existingType == null) {
			return true;
		}

		// modifiers
		if (!modifiersEqual(reader.getModifiers(), existingType.modifiers)) {
			return true;
		}

		// generic signature
		if (!CharOperation.equals(reader.getGenericSignature(), existingType.genericSignature)) {
			return true;
		}

		// superclass name
		if (!CharOperation.equals(reader.getSuperclassName(), existingType.superclassName)) {
			return true;
		}

		// have annotations changed on the type?
		IBinaryAnnotation[] newAnnos = reader.getAnnotations();
		if (newAnnos == null || newAnnos.length == 0) {
			if (existingType.annotations != null && existingType.annotations.length != 0) {
				return true;
			}
		} else {
			IBinaryAnnotation[] existingAnnos = existingType.annotations;
			if (existingAnnos == null || existingAnnos.length != newAnnos.length) {
				return true;
			}
			// Does not allow for an order switch
			// Does not cope with a change in values set on the annotation (hard to create a testcase where this is a problem tho)
			for (int i = 0; i < newAnnos.length; i++) {
				if (!CharOperation.equals(newAnnos[i].getTypeName(), existingAnnos[i].getTypeName())) {
					return true;
				}
			}

		}

		// interfaces
		char[][] existingIfs = existingType.interfaces;
		char[][] newIfsAsChars = reader.getInterfaceNames();
		if (newIfsAsChars == null) {
			newIfsAsChars = EMPTY_CHAR_ARRAY;
		} // damn I'm lazy...
		if (existingIfs == null) {
			existingIfs = EMPTY_CHAR_ARRAY;
		}
		if (existingIfs.length != newIfsAsChars.length) {
			return true;
		}
		new_interface_loop:
		for (char[] newIfsAsChar : newIfsAsChars) {
			for (char[] existingIf : existingIfs) {
				if (CharOperation.equals(existingIf, newIfsAsChar)) {
					continue new_interface_loop;
				}
			}
			return true;
		}

		// fields
		// CompactMemberStructureRepresentation[] existingFields = existingType.fields;
		IBinaryField[] newFields = reader.getFields();
		if (newFields == null) {
			newFields = CompactTypeStructureRepresentation.NoField;
		}

		// all redundant for now ... could be an optimization at some point...
		// remove any ajc$XXX fields from those we compare with
		// the existing fields - bug 129163
		// List nonGenFields = new ArrayList();
		// for (int i = 0; i < newFields.length; i++) {
		// IBinaryField field = newFields[i];
		// //if (!CharOperation.prefixEquals(NameMangler.AJC_DOLLAR_PREFIX,field.getName())) { // this would skip ajc$ fields
		// //if ((field.getModifiers()&0x1000)==0) // 0x1000 => synthetic - this will skip synthetic fields (eg. this$0)
		// nonGenFields.add(field);
		// //}
		// }
		IBinaryField[] existingFs = existingType.binFields;
		if (newFields.length != existingFs.length) {
			return true;
		}
		new_field_loop:
		for (IBinaryField field : newFields) {
			char[] fieldName = field.getName();
			for (IBinaryField existingF : existingFs) {
				if (CharOperation.equals(existingF.getName(), fieldName)) {
					IBinaryField existing = existingF;
					if (!modifiersEqual(field.getModifiers(), existing.getModifiers())) {
						return true;
					}
					if (!CharOperation.equals(existing.getTypeName(), field.getTypeName())) {
						return true;
					}

					char[] existingGSig = existing.getGenericSignature();
					char[] fieldGSig = field.getGenericSignature();
					if ((existingGSig == null && fieldGSig != null) || (existingGSig != null && fieldGSig == null)) {
						return true;
					}
					if (existingGSig != null) {
						if (!CharOperation.equals(existingGSig, fieldGSig)) {
							return true;
						}
					}

					continue new_field_loop;
				}
			}
			return true;
		}

		// methods
		// CompactMemberStructureRepresentation[] existingMethods = existingType.methods;
		IBinaryMethod[] newMethods = reader.getMethods();
		if (newMethods == null) {
			newMethods = CompactTypeStructureRepresentation.NoMethod;
		}

		// all redundant for now ... could be an optimization at some point...

		// Ctors in a non-static inner type have an 'extra parameter' of the enclosing type.
		// If skippableDescriptorPrefix gets set here then it is set to the descriptor portion
		// for this 'extra parameter'. For an inner class of pkg.Foo the skippable descriptor
		// prefix will be '(Lpkg/Foo;' - so later when comparing <init> methods we know what to
		// compare.
		// IF THIS CODE NEEDS TO GET MORE COMPLICATED, I THINK ITS WORTH RIPPING IT ALL OUT AND
		// CREATING THE STRUCTURAL CHANGES OBJECT BASED ON CLASSREADER OUTPUT RATHER THAN
		// THE RESOLVEDTYPE - THEN THERE WOULD BE NO NEED TO TREAT SOME METHODS IN A PECULIAR
		// WAY.
		// char[] skippableDescriptorPrefix = null;
		// char[] enclosingTypeName = reader.getEnclosingTypeName();
		// boolean isStaticType = Modifier.isStatic(reader.getModifiers());
		// if (!isStaticType && enclosingTypeName!=null) {
		// StringBuffer sb = new StringBuffer();
		// sb.append("(L").append(new String(enclosingTypeName)).append(";");
		// skippableDescriptorPrefix = sb.toString().toCharArray();
		// }
		//
		//
		// // remove the aspectOf, hasAspect, clinit and ajc$XXX methods
		// // from those we compare with the existing methods - bug 129163
		// List nonGenMethods = new ArrayList();
		// for (int i = 0; i < newMethods.length; i++) {
		// IBinaryMethod method = newMethods[i];
		// // if ((method.getModifiers() & 0x1000)!=0) continue; // 0x1000 => synthetic - will cause us to skip access$0 - is this
		// always safe?
		// char[] methodName = method.getSelector();
		// // if (!CharOperation.equals(methodName,NameMangler.METHOD_ASPECTOF) &&
		// // !CharOperation.equals(methodName,NameMangler.METHOD_HASASPECT) &&
		// // !CharOperation.equals(methodName,NameMangler.STATIC_INITIALIZER) &&
		// // !CharOperation.prefixEquals(NameMangler.AJC_DOLLAR_PREFIX,methodName) &&
		// // !CharOperation.prefixEquals(NameMangler.CLINIT,methodName)) {
		// nonGenMethods.add(method);
		// // }
		// }
		IBinaryMethod[] existingMs = existingType.binMethods;
		if (newMethods.length != existingMs.length) {
			return true;
		}
		new_method_loop:
		for (IBinaryMethod method : newMethods) {
			char[] methodName = method.getSelector();
			for (IBinaryMethod existingM : existingMs) {
				if (CharOperation.equals(existingM.getSelector(), methodName)) {
					// candidate match
					if (!CharOperation.equals(method.getMethodDescriptor(), existingM.getMethodDescriptor())) {
						// ok, the descriptors don't match, but is this a funky ctor on a non-static inner
						// type?
						// boolean mightBeOK =
						// skippableDescriptorPrefix!=null && // set for inner types
						// CharOperation.equals(methodName,NameMangler.INIT) && // ctor
						// CharOperation.prefixEquals(skippableDescriptorPrefix,method.getMethodDescriptor()); // checking for
						// prefix on the descriptor
						// if (mightBeOK) {
						// // OK, so the descriptor starts something like '(Lpkg/Foo;' - we now may need to look at the rest of the
						// // descriptor if it takes >1 parameter.
						// // eg. could be (Lpkg/C;Ljava/lang/String;) where the skippablePrefix is (Lpkg/C;
						// char [] md = method.getMethodDescriptor();
						// char[] remainder = CharOperation.subarray(md, skippableDescriptorPrefix.length, md.length);
						// if (CharOperation.equals(remainder,BRACKET_V)) continue new_method_loop; // no other parameters to worry
						// about
						// char[] comparableSig = CharOperation.subarray(existingMethods[j].signature, 1,
						// existingMethods[j].signature.length);
						// boolean match = CharOperation.equals(comparableSig, remainder);
						// if (match) continue new_method_loop;
						// }
						continue; // might be overloading
					} else {
						// matching sigs
						IBinaryMethod existing = existingM;
						if (!modifiersEqual(method.getModifiers(), existing.getModifiers())) {
							return true;
						}

						if (exceptionClausesDiffer(existing, method)) {
							return true;
						}

						char[] existingGSig = existing.getGenericSignature();
						char[] methodGSig = method.getGenericSignature();
						if ((existingGSig == null && methodGSig != null) || (existingGSig != null && methodGSig == null)) {
							return true;
						}
						if (existingGSig != null) {
							if (!CharOperation.equals(existingGSig, methodGSig)) {
								return true;
							}
						}

						continue new_method_loop;
					}
				}
			}
			return true; // (no match found)
		}

		// check for differences in inner types
		// TODO could make order insensitive
		IBinaryNestedType[] binaryNestedTypes = reader.getMemberTypes();
		IBinaryNestedType[] existingBinaryNestedTypes = existingType.getMemberTypes();
		if ((binaryNestedTypes == null && existingBinaryNestedTypes != null)
				|| (binaryNestedTypes != null && existingBinaryNestedTypes == null)) {
			return true;
		}
		if (binaryNestedTypes != null) {
			int bnLength = binaryNestedTypes.length;
			if (existingBinaryNestedTypes.length != bnLength) {
				return true;
			}
			for (int m = 0; m < bnLength; m++) {
				IBinaryNestedType bnt = binaryNestedTypes[m];
				IBinaryNestedType existingBnt = existingBinaryNestedTypes[m];
				if (!CharOperation.equals(bnt.getName(), existingBnt.getName())) {
					return true;
				}
			}
		}
		return false;
	}

	private void logAnalysis(String filename, String info) {
		if (listenerDefined()) {
			getListener().recordDecision("StructuralAnalysis["+filename+"]: "+info);
		}
	}
	
	private boolean printStructuralChanges(String filename, ClassFileReader reader, CompactTypeStructureRepresentation existingType) {
		logAnalysis(filename,"appears to have structurally changed, printing changes:");
		if (existingType == null) {
			logAnalysis(filename,"have not seen this type before");
			return true;
		}

		// modifiers
		if (!modifiersEqual(reader.getModifiers(), existingType.modifiers)) {
			logAnalysis(filename,"modifiers changed.  old=0x"+Integer.toHexString(existingType.getModifiers())+" new=0x"+Integer.toHexString(reader.getModifiers()));
			return true;
		}

		// generic signature
		if (!CharOperation.equals(reader.getGenericSignature(), existingType.genericSignature)) {
			logAnalysis(filename,"generic signature changed. old="+stringify(existingType.genericSignature)+" new="+stringify(reader.getGenericSignature()));
			return true;
		}

		// superclass name
		if (!CharOperation.equals(reader.getSuperclassName(), existingType.superclassName)) {
			logAnalysis(filename,"superclass name changed. old="+stringify(existingType.superclassName)+" new="+stringify(reader.getSuperclassName()));
			return true;
		}

		// have annotations changed on the type?
		IBinaryAnnotation[] newAnnos = reader.getAnnotations();
		if (newAnnos == null || newAnnos.length == 0) {
			if (existingType.annotations != null && existingType.annotations.length != 0) {
				logAnalysis(filename,"type used to have annotations and now does not: "+stringify(existingType.annotations));
				return true;
			}
		} else {
			IBinaryAnnotation[] existingAnnos = existingType.annotations;
			if (existingAnnos == null || existingAnnos.length != newAnnos.length) {
				logAnalysis(filename,"type now has annotations which it did not used to have: "+stringify(newAnnos));
				return true;
			}
			// Does not allow for an order switch
			// Does not cope with a change in values set on the annotation (hard to create a testcase where this is a problem tho)
			for (int i = 0; i < newAnnos.length; i++) {
				if (!CharOperation.equals(newAnnos[i].getTypeName(), existingAnnos[i].getTypeName())) {
					logAnalysis(filename,"type annotation change at position "+i+" old="+new String(existingAnnos[i].getTypeName())+" new="+new String(newAnnos[i].getTypeName()));
					return true;
				}
			}

		}

		// interfaces
		char[][] existingIfs = existingType.interfaces;
		char[][] newIfsAsChars = reader.getInterfaceNames();
		if (newIfsAsChars == null) {
			newIfsAsChars = EMPTY_CHAR_ARRAY;
		} // damn I'm lazy...
		if (existingIfs == null) {
			existingIfs = EMPTY_CHAR_ARRAY;
		}
		if (existingIfs.length != newIfsAsChars.length) {
			return true;
		}
		new_interface_loop:
		for (char[] newIfsAsChar : newIfsAsChars) {
			for (char[] existingIf : existingIfs) {
				if (CharOperation.equals(existingIf, newIfsAsChar)) {
					continue new_interface_loop;
				}
			}
			logAnalysis(filename, "set of interfaces changed. old=" + stringify(existingIfs) + " new=" + stringify(newIfsAsChars));
			return true;
		}

		// fields
		// CompactMemberStructureRepresentation[] existingFields = existingType.fields;
		IBinaryField[] newFields = reader.getFields();
		if (newFields == null) {
			newFields = CompactTypeStructureRepresentation.NoField;
		}

		// all redundant for now ... could be an optimization at some point...
		// remove any ajc$XXX fields from those we compare with
		// the existing fields - bug 129163
		// List nonGenFields = new ArrayList();
		// for (int i = 0; i < newFields.length; i++) {
		// IBinaryField field = newFields[i];
		// //if (!CharOperation.prefixEquals(NameMangler.AJC_DOLLAR_PREFIX,field.getName())) { // this would skip ajc$ fields
		// //if ((field.getModifiers()&0x1000)==0) // 0x1000 => synthetic - this will skip synthetic fields (eg. this$0)
		// nonGenFields.add(field);
		// //}
		// }
		IBinaryField[] existingFs = existingType.binFields;
		if (newFields.length != existingFs.length) {
			logAnalysis(filename,"number of fields changed. old="+stringify(existingFs)+" new="+stringify(newFields));
			return true;
		}
		new_field_loop:
		for (IBinaryField field : newFields) {
			char[] fieldName = field.getName();
			for (IBinaryField existingF : existingFs) {
				if (CharOperation.equals(existingF.getName(), fieldName)) {
					IBinaryField existing = existingF;
					if (!modifiersEqual(field.getModifiers(), existing.getModifiers())) {
						logAnalysis(filename, "field modifiers changed '" + existing + "' old=0x" + Integer.toHexString(existing.getModifiers()) + " new=0x" + Integer.toHexString(field.getModifiers()));
						return true;
					}
					if (!CharOperation.equals(existing.getTypeName(), field.getTypeName())) {
						logAnalysis(filename, "field type changed '" + existing + "' old=" + new String(existing.getTypeName()) + " new=" + new String(field.getTypeName()));
						return true;
					}

					char[] existingGSig = existing.getGenericSignature();
					char[] fieldGSig = field.getGenericSignature();
					if ((existingGSig == null && fieldGSig != null) || (existingGSig != null && fieldGSig == null)) {
						logAnalysis(filename, "field generic sig changed '" + existing + "' old=" +
								(existingGSig == null ? "null" : new String(existingGSig)) + " new=" + (fieldGSig == null ? "null" : new String(fieldGSig)));
						return true;
					}
					if (existingGSig != null) {
						if (!CharOperation.equals(existingGSig, fieldGSig)) {
							logAnalysis(filename, "field generic sig changed '" + existing + "' old=" +
									(existingGSig == null ? "null" : new String(existingGSig)) + " new=" + (fieldGSig == null ? "null" : new String(fieldGSig)));
							return true;
						}
					}

					continue new_field_loop;
				}
			}
			logAnalysis(filename, "field changed. New field detected '" + field + "'");
			return true;
		}

		// methods
		// CompactMemberStructureRepresentation[] existingMethods = existingType.methods;
		IBinaryMethod[] newMethods = reader.getMethods();
		if (newMethods == null) {
			newMethods = CompactTypeStructureRepresentation.NoMethod;
		}

		// all redundant for now ... could be an optimization at some point...

		// Ctors in a non-static inner type have an 'extra parameter' of the enclosing type.
		// If skippableDescriptorPrefix gets set here then it is set to the descriptor portion
		// for this 'extra parameter'. For an inner class of pkg.Foo the skippable descriptor
		// prefix will be '(Lpkg/Foo;' - so later when comparing <init> methods we know what to
		// compare.
		// IF THIS CODE NEEDS TO GET MORE COMPLICATED, I THINK ITS WORTH RIPPING IT ALL OUT AND
		// CREATING THE STRUCTURAL CHANGES OBJECT BASED ON CLASSREADER OUTPUT RATHER THAN
		// THE RESOLVEDTYPE - THEN THERE WOULD BE NO NEED TO TREAT SOME METHODS IN A PECULIAR
		// WAY.
		// char[] skippableDescriptorPrefix = null;
		// char[] enclosingTypeName = reader.getEnclosingTypeName();
		// boolean isStaticType = Modifier.isStatic(reader.getModifiers());
		// if (!isStaticType && enclosingTypeName!=null) {
		// StringBuffer sb = new StringBuffer();
		// sb.append("(L").append(new String(enclosingTypeName)).append(";");
		// skippableDescriptorPrefix = sb.toString().toCharArray();
		// }
		//
		//
		// // remove the aspectOf, hasAspect, clinit and ajc$XXX methods
		// // from those we compare with the existing methods - bug 129163
		// List nonGenMethods = new ArrayList();
		// for (int i = 0; i < newMethods.length; i++) {
		// IBinaryMethod method = newMethods[i];
		// // if ((method.getModifiers() & 0x1000)!=0) continue; // 0x1000 => synthetic - will cause us to skip access$0 - is this
		// always safe?
		// char[] methodName = method.getSelector();
		// // if (!CharOperation.equals(methodName,NameMangler.METHOD_ASPECTOF) &&
		// // !CharOperation.equals(methodName,NameMangler.METHOD_HASASPECT) &&
		// // !CharOperation.equals(methodName,NameMangler.STATIC_INITIALIZER) &&
		// // !CharOperation.prefixEquals(NameMangler.AJC_DOLLAR_PREFIX,methodName) &&
		// // !CharOperation.prefixEquals(NameMangler.CLINIT,methodName)) {
		// nonGenMethods.add(method);
		// // }
		// }
		IBinaryMethod[] existingMs = existingType.binMethods;
		if (newMethods.length != existingMs.length) {
			logAnalysis(filename,"number of methods changed. old="+stringify(existingMs)+" new="+stringify(newMethods));
			return true;
		}
		new_method_loop:
		for (IBinaryMethod method : newMethods) {
			char[] methodName = method.getSelector();
			for (IBinaryMethod existingM : existingMs) {
				if (CharOperation.equals(existingM.getSelector(), methodName)) {
					// candidate match
					if (!CharOperation.equals(method.getMethodDescriptor(), existingM.getMethodDescriptor())) {
						// ok, the descriptors don't match, but is this a funky ctor on a non-static inner
						// type?
						// boolean mightBeOK =
						// skippableDescriptorPrefix!=null && // set for inner types
						// CharOperation.equals(methodName,NameMangler.INIT) && // ctor
						// CharOperation.prefixEquals(skippableDescriptorPrefix,method.getMethodDescriptor()); // checking for
						// prefix on the descriptor
						// if (mightBeOK) {
						// // OK, so the descriptor starts something like '(Lpkg/Foo;' - we now may need to look at the rest of the
						// // descriptor if it takes >1 parameter.
						// // eg. could be (Lpkg/C;Ljava/lang/String;) where the skippablePrefix is (Lpkg/C;
						// char [] md = method.getMethodDescriptor();
						// char[] remainder = CharOperation.subarray(md, skippableDescriptorPrefix.length, md.length);
						// if (CharOperation.equals(remainder,BRACKET_V)) continue new_method_loop; // no other parameters to worry
						// about
						// char[] comparableSig = CharOperation.subarray(existingMethods[j].signature, 1,
						// existingMethods[j].signature.length);
						// boolean match = CharOperation.equals(comparableSig, remainder);
						// if (match) continue new_method_loop;
						// }
						continue; // might be overloading
					} else {
						// matching sigs
						IBinaryMethod existing = existingM;
						if (!modifiersEqual(method.getModifiers(), existing.getModifiers())) {
							logAnalysis(filename, "method modifiers changed '" + existing + "' old=0x" + Integer.toHexString(existing.getModifiers()) + " new=0x" + Integer.toHexString(method.getModifiers()));
							return true;
						}

						if (exceptionClausesDiffer(existing, method)) {
							logAnalysis(filename, "method exception clauses changed '" + existing + "' old=" + existing + " new=" + method);
							return true;
						}

						char[] existingGSig = existing.getGenericSignature();
						char[] methodGSig = method.getGenericSignature();
						if ((existingGSig == null && methodGSig != null) || (existingGSig != null && methodGSig == null)) {
							logAnalysis(filename, "method generic sig changed '" + existing + "' old=" +
									(existingGSig == null ? "null" : new String(existingGSig)) + " new=" + (methodGSig == null ? "null" : new String(methodGSig)));
							return true;
						}
						if (existingGSig != null) {
							if (!CharOperation.equals(existingGSig, methodGSig)) {
								logAnalysis(filename, "method generic sig changed '" + existing + "' old=" +
										(existingGSig == null ? "null" : new String(existingGSig)) + " new=" + (methodGSig == null ? "null" : new String(methodGSig)));
								return true;
							}
						}

						continue new_method_loop;
					}
				}
				// TODO missing a return true here? Meaning we have a field in the new that we can't find in the old!
			}

			logAnalysis(filename, "method changed. New method detected '" + stringify(method) + "' (might be a rename)");
			return true; // (no match found)
		}

		// check for differences in inner types
		// TODO could make order insensitive
		IBinaryNestedType[] binaryNestedTypes = reader.getMemberTypes();
		IBinaryNestedType[] existingBinaryNestedTypes = existingType.getMemberTypes();
		if ((binaryNestedTypes == null && existingBinaryNestedTypes != null)
				|| (binaryNestedTypes != null && existingBinaryNestedTypes == null)) {
			logAnalysis(filename,"nested types changed");
			return true;
		}
		if (binaryNestedTypes != null) {
			int bnLength = binaryNestedTypes.length;
			if (existingBinaryNestedTypes.length != bnLength) {
				logAnalysis(filename,"nested types changed. old="+stringify(existingBinaryNestedTypes)+" new="+stringify(binaryNestedTypes));
				return true;
			}
			for (int m = 0; m < bnLength; m++) {
				IBinaryNestedType bnt = binaryNestedTypes[m];
				IBinaryNestedType existingBnt = existingBinaryNestedTypes[m];
				if (!CharOperation.equals(bnt.getName(), existingBnt.getName())) {
					logAnalysis(filename,"nested type changed name at position "+m+" old="+stringify(existingBinaryNestedTypes)+" new="+stringify(binaryNestedTypes));
					return true;
				}
			}
		}
		return false;
	}

	private String stringify(char[] chars) {
		if (chars == null) {
			return "null";
		}
		return new String(chars);
	}

	private String stringify(IBinaryNestedType[] binaryNestedTypes) {
		StringBuilder buf = new StringBuilder();
		for (IBinaryNestedType binaryNestedType: binaryNestedTypes) {
			buf.append(binaryNestedType).append(" ");
		}
		return buf.toString().trim();
	}

	private String stringify(IBinaryMethod[] methods) {
		StringBuilder buf = new StringBuilder();
		for (IBinaryMethod method: methods) {
			buf.append(stringify(method)).append(" ");
		}
		return "["+buf.toString().trim()+"]";
	}

	private String stringify(IBinaryMethod m) {
		StringBuilder buf = new StringBuilder();
		buf.append("0x").append(Integer.toHexString(m.getModifiers())).append(" ");
		buf.append(m.getSelector()).append(m.getMethodDescriptor());
		// IBinaryAnnotation[] annos = m.getAnnotations();
		// TODO include annotations, generic sig, etc
		return buf.toString().trim();
	}

	private String stringify(IBinaryField[] fields) {
		StringBuilder buf = new StringBuilder();
		for (IBinaryField field: fields) {
			buf.append(stringify(field)).append(" ");
		}
		return "["+buf.toString().trim()+"]";
	}

	private Object stringify(IBinaryField f) {
		StringBuilder buf = new StringBuilder();
		buf.append("0x").append(Integer.toHexString(f.getModifiers())).append(" ");
		buf.append(f.getTypeName()).append(f.getName());
		return buf.toString().trim();
	}

	private String stringify(char[][] arrayOfCharArrays) {
		StringBuilder buf = new StringBuilder();
		for (char[] charArray: arrayOfCharArrays) {
			buf.append(charArray).append(" ");
		}
		return buf.toString().trim();
	}

	private String stringify(IBinaryAnnotation[] annotations) {
		StringBuilder buf = new StringBuilder();
		for (IBinaryAnnotation anno: annotations) {
			buf.append(anno).append(" ");
		}
		return buf.toString().trim();
	}

	/**
	 * For two methods, discover if there has been a change in the exception types specified.
	 * 
	 * @return true if the exception types have changed
	 */
	private boolean exceptionClausesDiffer(IBinaryMethod lastMethod, IBinaryMethod newMethod) {
		char[][] previousExceptionTypeNames = lastMethod.getExceptionTypeNames();
		char[][] newExceptionTypeNames = newMethod.getExceptionTypeNames();
		int pLength = previousExceptionTypeNames.length;
		int nLength = newExceptionTypeNames.length;
		if (pLength != nLength) {
			return true;
		}
		if (pLength == 0) {
			return false;
		}
		// TODO could be insensitive to an order change
		for (int i = 0; i < pLength; i++) {
			if (!CharOperation.equals(previousExceptionTypeNames[i], newExceptionTypeNames[i])) {
				return true;
			}
		}
		return false;
	}

	private boolean modifiersEqual(int eclipseModifiers, int resolvedTypeModifiers) {
		resolvedTypeModifiers = resolvedTypeModifiers & ExtraCompilerModifiers.AccJustFlag;
		eclipseModifiers = eclipseModifiers & ExtraCompilerModifiers.AccJustFlag;
		// if ((eclipseModifiers & CompilerModifiers.AccSuper) != 0) {
		// eclipseModifiers -= CompilerModifiers.AccSuper;
		// }
		return (eclipseModifiers == resolvedTypeModifiers);
	}

	// private static StringSet makeStringSet(List strings) {
	// StringSet ret = new StringSet(strings.size());
	// for (Iterator iter = strings.iterator(); iter.hasNext();) {
	// String element = (String) iter.next();
	// ret.add(element);
	// }
	// return ret;
	// }

	private String stringifySet(Set<?> l) {
		StringBuffer sb = new StringBuffer();
		sb.append("{");
		for (Iterator<?> iter = l.iterator(); iter.hasNext();) {
			Object el = iter.next();
			sb.append(el);
			if (iter.hasNext()) {
				sb.append(",");
			}
		}
		sb.append("}");
		return sb.toString();
	}

	protected void addAffectedSourceFiles(Set<File> addTo, Set<File> lastTimeSources) {
		if (qualifiedStrings.elementSize == 0 && simpleStrings.elementSize == 0) {
			return;
		}
		if (listenerDefined()) {
			getListener().recordDecision(
					"Examining whether any other files now need compilation based on just compiling: '"
							+ stringifySet(lastTimeSources) + "'");
		}
		// the qualifiedStrings are of the form 'p1/p2' & the simpleStrings are just 'X'
		char[][][] qualifiedNames = ReferenceCollection.internQualifiedNames(qualifiedStrings);
		// if a well known qualified name was found then we can skip over these
		if (qualifiedNames.length < qualifiedStrings.elementSize) {
			qualifiedNames = null;
		}
		char[][] simpleNames = ReferenceCollection.internSimpleNames(simpleStrings, true);
		// if a well known name was found then we can skip over these
		if (simpleNames.length < simpleStrings.elementSize) {
			simpleNames = null;
		}

		// System.err.println("simple: " + simpleStrings);
		// System.err.println("qualif: " + qualifiedStrings);

		for (Map.Entry<File, ReferenceCollection> entry : references.entrySet()) {
			ReferenceCollection refs = entry.getValue();
			if (refs != null && refs.includes(qualifiedNames, simpleNames)) {
				File file = entry.getKey();
				if (file.exists()) {
					if (!lastTimeSources.contains(file)) { // ??? O(n**2)
						if (listenerDefined()) {
							getListener().recordDecision("Need to recompile '" + file.getName().toString() + "'");
						}
						addTo.add(file);
					}
				}
			}
		}
		// add in the things we compiled previously - I know that seems crap but otherwise we may pull woven
		// stuff off disk (since we no longer have UnwovenClassFile objects) in order to satisfy references
		// in the new files we are about to compile (see pr133532)
		if (addTo.size() > 0) {
			addTo.addAll(lastTimeSources);
		}
		// // XXX Promote addTo to a Set - then we don't need this rubbish? but does it need to be ordered?
		// if (addTo.size()>0) {
		// for (Iterator iter = lastTimeSources.iterator(); iter.hasNext();) {
		// Object element = (Object) iter.next();
		// if (!addTo.contains(element)) addTo.add(element);
		// }
		// }

		qualifiedStrings.clear();
		simpleStrings.clear();
	}

	/**
	 * Record that a particular type has been touched during a compilation run. Information is used to ensure any types depending
	 * upon this one are also recompiled.
	 * 
	 * @param typename (possibly qualified) type name
	 */
	protected void recordTypeChanged(String typename) {
		int lastDot = typename.lastIndexOf('.');
		String typeName;
		if (lastDot != -1) {
			String packageName = typename.substring(0, lastDot).replace('.', '/');
			qualifiedStrings.add(packageName);
			typeName = typename.substring(lastDot + 1);
		} else {
			qualifiedStrings.add("");
			typeName = typename;
		}

		int memberIndex = typeName.indexOf('$');
		if (memberIndex > 0) {
			typeName = typeName.substring(0, memberIndex);
		}
		simpleStrings.add(typeName);
	}

	/**
	 * Record some additional dependencies between types. When any of the types specified in fullyQualifiedTypeNames changes, we
	 * need to recompile the file named in the CompilationResult. This method patches that information into the existing data
	 * structures.
	 */
	public boolean recordDependencies(File file, String[] typeNameDependencies) {
		try {
			File sourceFile = new File(new String(file.getCanonicalPath()));
			ReferenceCollection existingCollection = references.get(sourceFile);
			if (existingCollection != null) {
				existingCollection.addDependencies(typeNameDependencies);
				return true;
			} else {
				ReferenceCollection rc = new ReferenceCollection(null, null, null);
				rc.addDependencies(typeNameDependencies);
				references.put(sourceFile, rc);
				return true;
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		return false;
	}

	protected void addDependentsOf(File sourceFile) {
		List<ClassFile> cfs = this.fullyQualifiedTypeNamesResultingFromCompilationUnit.get(sourceFile);
		if (cfs != null) {
			for (ClassFile cf : cfs) {
				recordTypeChanged(cf.fullyQualifiedTypeName);
			}
		}
	}

	public void setStructureModel(AsmManager structureModel) {
		this.structureModel = structureModel;
	}

	public AsmManager getStructureModel() {
		return structureModel;
	}

	public void setWeaver(BcelWeaver bw) {
		weaver = bw;
	}

	public BcelWeaver getWeaver() {
		return weaver;
	}

	public void setWorld(BcelWorld bw) {
		world = bw;
		world.addTypeDelegateResolver(this);
	}

	public BcelWorld getBcelWorld() {
		return world;
	}

	//
	// public void setRelationshipMap(IRelationshipMap irm) {
	// relmap = irm;
	// }
	//
	// public IRelationshipMap getRelationshipMap() {
	// return relmap;
	// }

	public int getNumberOfStructuralChangesSinceLastFullBuild() {
		return structuralChangesSinceLastFullBuild.size();
	}

	/** Returns last time we did a full or incremental build. */
	public long getLastBuildTime() {
		return lastSuccessfulBuildTime;
	}

	/** Returns last time we did a full build */
	public long getLastFullBuildTime() {
		return lastSuccessfulFullBuildTime;
	}

	/**
	 * @return Returns the buildConfig.
	 */
	public AjBuildConfig getBuildConfig() {
		return this.buildConfig;
	}

	public void clearBinarySourceFiles() {
		this.binarySourceFiles = new HashMap<>();
	}

	public void recordBinarySource(String fromPathName, List<UnwovenClassFile> unwovenClassFiles) {
		this.binarySourceFiles.put(fromPathName, unwovenClassFiles);
		if (this.maybeIncremental()) {
			List<ClassFile> simpleClassFiles = new LinkedList<>();
			for (UnwovenClassFile ucf : unwovenClassFiles) {
				ClassFile cf = getClassFileFor(ucf);
				simpleClassFiles.add(cf);
			}
			this.inputClassFilesBySource.put(fromPathName, simpleClassFiles);
		}
	}

	/**
	 * @param ucf
	 * @return
	 */
	private ClassFile getClassFileFor(UnwovenClassFile ucf) {
		return new ClassFile(ucf.getClassName(), new File(ucf.getFilename()));
	}

	public Map<String, List<UnwovenClassFile>> getBinarySourceMap() {
		return this.binarySourceFiles;
	}

	public Map<String, File> getClassNameToFileMap() {
		return this.classesFromName;
	}

	public boolean hasResource(String resourceName) {
		return this.resources.keySet().contains(resourceName);
	}

	public void recordResource(String resourceName, File resourceSourceLocation) {
		this.resources.put(resourceName, resourceSourceLocation);
	}

	/**
	 * @return Returns the addedFiles.
	 */
	public Set<File> getAddedFiles() {
		return this.addedFiles;
	}

	/**
	 * @return Returns the deletedFiles.
	 */
	public Set<File> getDeletedFiles() {
		return this.deletedFiles;
	}

	public void forceBatchBuildNextTimeAround() {
		this.batchBuildRequiredThisTime = true;
	}

	public boolean requiresFullBatchBuild() {
		return this.batchBuildRequiredThisTime;
	}

	private static class ClassFile {
		public String fullyQualifiedTypeName;
		public File locationOnDisk;

		public ClassFile(String fqn, File location) {
			this.fullyQualifiedTypeName = fqn;
			this.locationOnDisk = location;
		}

		@Override
		public String toString() {
			StringBuilder s = new StringBuilder();
			s.append("ClassFile(type=").append(fullyQualifiedTypeName).append(",location=").append(locationOnDisk).append(")");
			return s.toString();
		}

		public void deleteFromFileSystem(AjBuildConfig buildConfig) {
			String namePrefix = locationOnDisk.getName();
			namePrefix = namePrefix.substring(0, namePrefix.lastIndexOf('.'));
			final String targetPrefix = namePrefix + BcelWeaver.CLOSURE_CLASS_PREFIX;
			File dir = locationOnDisk.getParentFile();
			if (dir != null) {
				File[] weaverGenerated = dir.listFiles(new FilenameFilter() {
					@Override
					public boolean accept(File dir, String name) {
						return name.startsWith(targetPrefix);
					}
				});
				if (weaverGenerated != null) {
					for (File file : weaverGenerated) {
						file.delete();
						if (buildConfig != null && buildConfig.getCompilationResultDestinationManager() != null) {
							buildConfig.getCompilationResultDestinationManager().reportFileRemove(file.getPath(),
									CompilationResultDestinationManager.FILETYPE_CLASS);
						}
					}
				}
			}
			locationOnDisk.delete();
			if (buildConfig != null && buildConfig.getCompilationResultDestinationManager() != null) {
				buildConfig.getCompilationResultDestinationManager().reportFileRemove(locationOnDisk.getPath(),
						CompilationResultDestinationManager.FILETYPE_CLASS);
			}
		}
	}

	public void wipeAllKnowledge() {
		buildManager.state = null;
		// buildManager.setStructureModel(null);
	}

	public Map<String, char[]> getAspectNamesToFileNameMap() {
		return aspectsFromFileNames;
	}

	public void initializeAspectNamesToFileNameMap() {
		this.aspectsFromFileNames = new HashMap<>();
	}

	// Will allow us to record decisions made during incremental processing, hopefully aid in debugging
	public boolean listenerDefined() {
		return stateListener != null;
	}

	public IStateListener getListener() {
		return stateListener;
	}

	public IBinaryType checkPreviousBuild(String name) {
		return resolvedTypeStructuresFromLastBuild.get(name);
	}

	public AjBuildManager getAjBuildManager() {
		return buildManager;
	}

	public INameEnvironment getNameEnvironment() {
		return this.nameEnvironment;
	}

	public void setNameEnvironment(INameEnvironment nameEnvironment) {
		this.nameEnvironment = nameEnvironment;
	}

	public FileSystem getFileSystem() {
		return this.fileSystem;
	}

	public void setFileSystem(FileSystem fileSystem) {
		this.fileSystem = fileSystem;
	}
	
	/**
	 * Record an aspect that came in on the aspect path. When a .class file changes on the aspect path we can then recognize it as
	 * an aspect and know to do more than just a tiny incremental build. <br>
	 * TODO but this doesn't allow for a new aspect created on the aspectpath?
	 * 
	 * @param aspectFile path to the file, eg. c:/temp/foo/Fred.class
	 */
	public void recordAspectClassFile(String aspectFile) {
		aspectClassFiles.add(aspectFile);
	}

	public void write(CompressingDataOutputStream dos) throws IOException {
		// weaver
		weaver.write(dos);
		// world
		// model
		// local state
	}

	/**
	 * See if we can create a delegate from a CompactTypeStructure - TODO better comment
	 */
	@Override
	public ReferenceTypeDelegate getDelegate(ReferenceType referenceType) {
		File f = classesFromName.get(referenceType.getName());
		if (f == null) {
			return null; // not heard of it
		}
		try {
			ClassParser parser = new ClassParser(f.toString());
			return world.buildBcelDelegate(referenceType, parser.parse(), true, false);
		} catch (IOException e) {
			IMessage msg = new Message("Failed to recover " + referenceType, referenceType.getDelegate()!=null?referenceType.getSourceLocation():null, false);
			buildManager.handler.handleMessage(msg);
		}
		return null;
	}
}
