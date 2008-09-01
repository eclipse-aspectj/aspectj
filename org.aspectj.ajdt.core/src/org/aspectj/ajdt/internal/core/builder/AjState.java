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
 * ******************************************************************/

package org.aspectj.ajdt.internal.core.builder;

import java.io.File;
import java.io.FileFilter;
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

import org.aspectj.ajdt.internal.compiler.InterimCompilationResult;
import org.aspectj.asm.IHierarchy;
import org.aspectj.asm.IRelationshipMap;
import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.Message;
import org.aspectj.bridge.SourceLocation;
import org.aspectj.org.eclipse.jdt.core.compiler.CharOperation;
import org.aspectj.org.eclipse.jdt.internal.compiler.CompilationResult;
import org.aspectj.org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader;
import org.aspectj.org.eclipse.jdt.internal.compiler.classfmt.ClassFormatException;
import org.aspectj.org.eclipse.jdt.internal.compiler.env.IBinaryField;
import org.aspectj.org.eclipse.jdt.internal.compiler.env.IBinaryMethod;
import org.aspectj.org.eclipse.jdt.internal.compiler.env.IBinaryType;
import org.aspectj.org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.ExtraCompilerModifiers;
import org.aspectj.org.eclipse.jdt.internal.core.builder.ReferenceCollection;
import org.aspectj.org.eclipse.jdt.internal.core.builder.StringSet;
import org.aspectj.util.FileUtil;
import org.aspectj.weaver.BCException;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.bcel.BcelWeaver;
import org.aspectj.weaver.bcel.BcelWorld;
import org.aspectj.weaver.bcel.UnwovenClassFile;

/**
 * Maintains state needed for incremental compilation
 */
public class AjState implements CompilerConfigurationChangeFlags {

	// SECRETAPI static so beware of multi-threading bugs...
	public static IStateListener stateListener = null;

	public static boolean FORCE_INCREMENTAL_DURING_TESTING = false;

	private AjBuildManager buildManager;
	private boolean couldBeSubsequentIncrementalBuild = false;
	private INameEnvironment nameEnvironment;

	private IHierarchy structureModel;
	private IRelationshipMap relmap;

	/**
	 * When looking at changes on the classpath, this set accumulates files in our state instance that affected by those changes.
	 * Then if we can do an incremental build - these must be compiled.
	 */
	private Set affectedFiles = new HashSet();

	private long lastSuccessfulFullBuildTime = -1;
	private Hashtable /* File, long */structuralChangesSinceLastFullBuild = new Hashtable();

	private long lastSuccessfulBuildTime = -1;
	private long currentBuildTime = -1;

	private AjBuildConfig buildConfig;

	private boolean batchBuildRequiredThisTime = false;

	/**
	 * Keeps a list of (FQN,Filename) pairs (as ClassFile objects) for types that resulted from the compilation of the given File.
	 * Note :- the ClassFile objects contain no byte code, they are simply a Filename,typename pair.
	 * 
	 * Populated in noteResult and used in addDependentsOf(File)
	 * 
	 * Added by AMC during state refactoring, 1Q06.
	 */
	private Map/* <File, List<ClassFile> */fullyQualifiedTypeNamesResultingFromCompilationUnit = new HashMap();

	/**
	 * Source files defining aspects
	 * 
	 * Populated in noteResult and used in processDeletedFiles
	 * 
	 * Added by AMC during state refactoring, 1Q06.
	 */
	private Set/* <File> */sourceFilesDefiningAspects = new HashSet();

	/**
	 * Populated in noteResult to record the set of types that should be recompiled if the given file is modified or deleted.
	 * 
	 * Refered to during addAffectedSourceFiles when calculating incremental compilation set.
	 */
	private Map/* <File, ReferenceCollection> */references = new HashMap();

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
	private Map/* File, List<UnwovenClassFile> */binarySourceFiles = new HashMap();

	/**
	 * Initially a duplicate of the information held in binarySourceFiles, with the key difference that the values are ClassFiles
	 * (type name, File) not UnwovenClassFiles (which also have all the byte code in them). After a batch build, binarySourceFiles
	 * is cleared, leaving just this much lighter weight map to use in processing subsequent incremental builds.
	 */
	private Map/* <File,List<ClassFile> */inputClassFilesBySource = new HashMap();

	/**
	 * Holds structure information on types as they were at the end of the last build. It would be nice to get rid of this too, but
	 * can't see an easy way to do that right now.
	 */
	private Map/* FQN,CompactStructureRepresentation */resolvedTypeStructuresFromLastBuild = new HashMap();

	/**
	 * Populated in noteResult to record the set of UnwovenClassFiles (intermediate results) that originated from compilation of the
	 * class with the given fully-qualified name.
	 * 
	 * Used in removeAllResultsOfLastBuild to remove .class files from output directory.
	 * 
	 * Passed into StatefulNameEnvironment during incremental compilation to support findType lookups.
	 */
	private Map/* <String, File> */classesFromName = new HashMap();

	/**
	 * Populated by AjBuildManager to record the aspects with the file name in which they're contained. This is later used when
	 * writing the outxml file in AjBuildManager. Need to record the file name because want to write an outxml file for each of the
	 * output directories and in order to ask the OutputLocationManager for the output location for a given aspect we need the file
	 * in which it is contained.
	 */
	private Map /* <String, char[]> */aspectsFromFileNames;

	private Set/* File */compiledSourceFiles = new HashSet();
	private List/* String */resources = new ArrayList();

	// these are references created on a particular compile run - when looping round in
	// addAffectedSourceFiles(), if some have been created then we look at which source files
	// touch upon those and get them recompiled.
	private StringSet qualifiedStrings = new StringSet(3);

	private StringSet simpleStrings = new StringSet(3);

	private Set addedFiles;
	private Set deletedFiles;
	private Set /* BinarySourceFile */addedBinaryFiles;
	private Set /* BinarySourceFile */deletedBinaryFiles;

	private BcelWeaver weaver;
	private BcelWorld world;

	public AjState(AjBuildManager buildManager) {
		this.buildManager = buildManager;
	}

	public void setCouldBeSubsequentIncrementalBuild(boolean yesThereCould) {
		this.couldBeSubsequentIncrementalBuild = yesThereCould;
	}

	void successfulCompile(AjBuildConfig config, boolean wasFullBuild) {
		buildConfig = config;
		lastSuccessfulBuildTime = currentBuildTime;
		if (stateListener != null)
			stateListener.buildSuccessful(wasFullBuild);
		if (wasFullBuild)
			lastSuccessfulFullBuildTime = currentBuildTime;
	}

	/**
	 * Returns false if a batch build is needed.
	 */
	public boolean prepareForNextBuild(AjBuildConfig newBuildConfig) {
		currentBuildTime = System.currentTimeMillis();

		if (!maybeIncremental()) {
			if (listenerDefined())
				getListener().recordDecision(
						"Preparing for build: not going to be incremental because either not in AJDT or incremental deactivated");
			return false;
		}

		if (this.batchBuildRequiredThisTime) {
			this.batchBuildRequiredThisTime = false;
			if (listenerDefined())
				getListener().recordDecision(
						"Preparing for build: not going to be incremental this time because batch build explicitly forced");
			return false;
		}

		if (lastSuccessfulBuildTime == -1 || buildConfig == null) {
			structuralChangesSinceLastFullBuild.clear();
			if (listenerDefined())
				getListener().recordDecision(
						"Preparing for build: not going to be incremental because no successful previous full build");
			return false;
		}

		// we don't support incremental with an outjar yet
		if (newBuildConfig.getOutputJar() != null) {
			structuralChangesSinceLastFullBuild.clear();
			if (listenerDefined())
				getListener().recordDecision("Preparing for build: not going to be incremental because outjar being used");
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
			if (listenerDefined())
				getListener()
						.recordDecision(
								"Preparing for build: not going to be incremental because path change detected (one of classpath/aspectpath/inpath/injars)");
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
			addedFiles = Collections.EMPTY_SET;
			deletedFiles = Collections.EMPTY_SET;
		} else {
			Set oldFiles = new HashSet(buildConfig.getFiles());
			Set newFiles = new HashSet(newBuildConfig.getFiles());

			addedFiles = new HashSet(newFiles);
			addedFiles.removeAll(oldFiles);
			deletedFiles = new HashSet(oldFiles);
			deletedFiles.removeAll(newFiles);
		}

		Set oldBinaryFiles = new HashSet(buildConfig.getBinaryFiles());
		Set newBinaryFiles = new HashSet(newBuildConfig.getBinaryFiles());

		addedBinaryFiles = new HashSet(newBinaryFiles);
		addedBinaryFiles.removeAll(oldBinaryFiles);
		deletedBinaryFiles = new HashSet(oldBinaryFiles);
		deletedBinaryFiles.removeAll(newBinaryFiles);

		boolean couldStillBeIncremental = processDeletedFiles(deletedFiles);

		if (!couldStillBeIncremental) {
			if (listenerDefined())
				getListener().recordDecision("Preparing for build: not going to be incremental because an aspect was deleted");
			return false;
		}

		if (listenerDefined())
			getListener().recordDecision("Preparing for build: planning to be an incremental build");
		return true;
	}

	/**
	 * Checks if any of the files in the set passed in contains an aspect declaration. If one is found then we start the process of
	 * batch building, i.e. we remove all the results of the last build, call any registered listener to tell them whats happened
	 * and return false.
	 * 
	 * @return false if we discovered an aspect declaration
	 */
	private boolean processDeletedFiles(Set deletedFiles) {
		for (Iterator iter = deletedFiles.iterator(); iter.hasNext();) {
			File aDeletedFile = (File) iter.next();
			if (this.sourceFilesDefiningAspects.contains(aDeletedFile)) {
				removeAllResultsOfLastBuild();
				if (stateListener != null) {
					stateListener.detectedAspectDeleted(aDeletedFile);
				}
				return false;
			}
			List/* ClassFile */classes = (List) fullyQualifiedTypeNamesResultingFromCompilationUnit.get(aDeletedFile);
			if (classes != null) {
				for (Iterator iterator = classes.iterator(); iterator.hasNext();) {
					ClassFile element = (ClassFile) iterator.next();
					resolvedTypeStructuresFromLastBuild.remove(element.fullyQualifiedTypeName);
				}
			}
		}
		return true;
	}

	private Collection getModifiedFiles() {
		return getModifiedFiles(lastSuccessfulBuildTime);
	}

	Collection getModifiedFiles(long lastBuildTime) {
		Set ret = new HashSet();

		// Check if the build configuration knows what files have changed...
		List/* File */modifiedFiles = buildConfig.getModifiedFiles();

		if (modifiedFiles == null) {
			// do not know, so need to go looking
			// not our job to account for new and deleted files
			for (Iterator i = buildConfig.getFiles().iterator(); i.hasNext();) {
				File file = (File) i.next();
				if (!file.exists())
					continue;

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

	private Collection getModifiedBinaryFiles() {
		return getModifiedBinaryFiles(lastSuccessfulBuildTime);
	}

	Collection getModifiedBinaryFiles(long lastBuildTime) {
		List ret = new ArrayList();
		// not our job to account for new and deleted files
		for (Iterator i = buildConfig.getBinaryFiles().iterator(); i.hasNext();) {
			AjBuildConfig.BinarySourceFile bsfile = (AjBuildConfig.BinarySourceFile) i.next();
			File file = bsfile.binSrc;
			if (!file.exists())
				continue;

			long modTime = file.lastModified();
			// System.out.println("check: " + file + " mod " + modTime + " build " + lastBuildTime);
			// need to add 1000 since lastModTime is only accurate to a second on some (all?) platforms
			if (modTime + 1000 >= lastBuildTime) {
				ret.add(bsfile);
			}
		}
		return ret;
	}

	private static int CLASS_FILE_NO_CHANGES = 0;

	private static int CLASS_FILE_CHANGED_THAT_NEEDS_INCREMENTAL_BUILD = 1;

	private static int CLASS_FILE_CHANGED_THAT_NEEDS_FULL_BUILD = 2;

	private static int MAX_AFFECTED_FILES_BEFORE_FULL_BUILD = 30;

	public static final FileFilter classFileFilter = new FileFilter() {
		public boolean accept(File pathname) {
			return pathname.getName().endsWith(".class");
		}
	};

	/**
	 * Analyse .class files in the directory specified, if they have changed since the last successful build then see if we can
	 * determine which source files in our project depend on the change. If we can then we can still do an incremental build, if we
	 * can't then we have to do a full build.
	 * 
	 */
	private int classFileChangedInDirSinceLastBuildRequiringFullBuild(File dir) {
		// Are we managing that output directory?
		AjState state = IncrementalStateManager.findStateManagingOutputLocation(dir);
		if (listenerDefined()) {
			if (state != null) {
				getListener().recordDecision("Found state instance managing output location : " + dir);
			} else {
				getListener().recordDecision("Failed to find a state instance managing output location : " + dir);
			}
		}

		List classFiles = FileUtil.listClassFiles(dir);

		for (Iterator iterator = classFiles.iterator(); iterator.hasNext();) {
			File classFile = (File) iterator.next();
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
						return CLASS_FILE_CHANGED_THAT_NEEDS_FULL_BUILD;
					}
					if (state.hasStructuralChangedSince(classFile, lastSuccessfulBuildTime)) {
						if (listenerDefined()) {
							getListener().recordDecision("Structural change detected in : " + classFile);
						}

						if (isTypeWeReferTo(classFile)) {
							if (affectedFiles.size() > MAX_AFFECTED_FILES_BEFORE_FULL_BUILD)
								return CLASS_FILE_CHANGED_THAT_NEEDS_FULL_BUILD;
						}
					} else {
						if (listenerDefined())
							getListener().recordDecision("Change detected in " + classFile + " but it is not structural");
					}
				} else {
					// No state object to ask, so it only matters if we know which type depends on this file
					if (isTypeWeReferTo(classFile)) {
						if (affectedFiles.size() > MAX_AFFECTED_FILES_BEFORE_FULL_BUILD)
							return CLASS_FILE_CHANGED_THAT_NEEDS_FULL_BUILD;
						return CLASS_FILE_CHANGED_THAT_NEEDS_INCREMENTAL_BUILD;
					} else {
						return CLASS_FILE_NO_CHANGES;
					}
				}
			}
		}
		return CLASS_FILE_NO_CHANGES;
	}

	private boolean isAspect(File file) {
		if (aspectsFromFileNames == null)
			return false;
		return aspectsFromFileNames.containsKey(file);
	}

	public static class SoftHashMap extends AbstractMap {

		private Map map;

		private ReferenceQueue rq = new ReferenceQueue();

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

		public Object get(Object key) {
			SoftReferenceKnownKey value = (SoftReferenceKnownKey) map.get(key);
			if (value == null)
				return null;
			if (value.get() == null) {
				// it got GC'd
				map.remove(value.key);
				return null;
			} else {
				return value.get();
			}
		}

		public Object put(Object k, Object v) {
			processQueue();
			return map.put(k, new SoftReferenceKnownKey(k, v));
		}

		public Set entrySet() {
			return map.entrySet();
		}

		public void clear() {
			processQueue();
			map.clear();
		}

		public int size() {
			processQueue();
			return map.size();
		}

		public Object remove(Object k) {
			processQueue();
			SoftReferenceKnownKey value = (SoftReferenceKnownKey) map.remove(k);
			if (value == null)
				return null;
			if (value.get() != null) {
				return value.get();
			}
			return null;
		}
	}

	SoftHashMap/* <baseDir,SoftHashMap<theFile,className>> */fileToClassNameMap = new SoftHashMap();

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

		for (Iterator i = references.entrySet().iterator(); i.hasNext();) {
			Map.Entry entry = (Map.Entry) i.next();
			ReferenceCollection refs = (ReferenceCollection) entry.getValue();
			if (refs != null && refs.includes(qualifiedNames, simpleNames)) {
				if (listenerDefined()) {
					getListener().recordDecision(
							toString() + ": type " + new String(className) + " is depended upon by '" + entry.getKey() + "'");
				}
				affectedFiles.add(entry.getKey());
				if (affectedFiles.size() > MAX_AFFECTED_FILES_BEFORE_FULL_BUILD)
					return true;
				// return true;
			}
		}
		if (affectedFiles.size() > 0)
			return true;
		if (listenerDefined())
			getListener().recordDecision(toString() + ": type " + new String(className) + " is not depended upon by this state");

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
	 * file is the file we are wondering about lastSBT is the last build time for the state asking the question
	 */
	private boolean hasStructuralChangedSince(File file, long lastSuccessfulBuildTime) {
		// long lastModTime = file.lastModified();
		Long l = (Long) structuralChangesSinceLastFullBuild.get(file.getAbsolutePath());
		long strucModTime = -1;
		if (l != null)
			strucModTime = l.longValue();
		else
			strucModTime = this.lastSuccessfulFullBuildTime;
		// we now have:
		// 'strucModTime'-> the last time the class was structurally changed
		return (strucModTime > lastSuccessfulBuildTime);
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
			List oldOutputLocs = getOutputLocations(previousConfig);

			List oldClasspath = previousConfig.getClasspath();
			List newClasspath = newConfig.getClasspath();
			if (stateListener != null)
				stateListener.aboutToCompareClasspaths(oldClasspath, newClasspath);
			if (changedAndNeedsFullBuild(oldClasspath, newClasspath, true, oldOutputLocs))
				return true;

			List oldAspectpath = previousConfig.getAspectpath();
			List newAspectpath = newConfig.getAspectpath();
			if (changedAndNeedsFullBuild(oldAspectpath, newAspectpath, true, oldOutputLocs))
				return true;

			List oldInPath = previousConfig.getInpath();
			List newInPath = newConfig.getInpath();
			if (changedAndNeedsFullBuild(oldInPath, newInPath, false, oldOutputLocs))
				return true;

			List oldInJars = previousConfig.getInJars();
			List newInJars = newConfig.getInJars();
			if (changedAndNeedsFullBuild(oldInJars, newInJars, false, oldOutputLocs))
				return true;
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
	private List /* File */getOutputLocations(AjBuildConfig config) {
		List outputLocs = new ArrayList();
		// Is there a default location?
		if (config.getOutputDir() != null) {
			try {
				outputLocs.add(config.getOutputDir().getCanonicalFile());
			} catch (IOException e) {
			}
		}
		if (config.getCompilationResultDestinationManager() != null) {
			List dirs = config.getCompilationResultDestinationManager().getAllOutputLocations();
			for (Iterator iterator = dirs.iterator(); iterator.hasNext();) {
				File f = (File) iterator.next();
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
	private boolean changedAndNeedsFullBuild(List oldPath, List newPath, boolean checkClassFiles, List outputLocs) {
		if (oldPath == null) {
			oldPath = new ArrayList();
		}
		if (newPath == null) {
			newPath = new ArrayList();
		}
		if (oldPath.size() != newPath.size()) {
			return true;
		}
		Set analysedPaths = new HashSet();
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
				for (Iterator iterator = outputLocs.iterator(); iterator.hasNext();) {
					File dir = (File) iterator.next();
					if (f.equals(dir)) {
						foundMatch = true;
					}
				}
				if (!foundMatch) {
					if (!analysedPaths.contains(f.getAbsolutePath())) { // Do not check paths more than once
						analysedPaths.add(f.getAbsolutePath());
						int classFileChanges = classFileChangedInDirSinceLastBuildRequiringFullBuild(f);
						if (classFileChanges == CLASS_FILE_CHANGED_THAT_NEEDS_FULL_BUILD)
							return true;
						// if (b && stateListener!=null) stateListener.detectedClassChangeInThisDir(f);
					}
				}
			}
		}
		return false;
	}

	public Set getFilesToCompile(boolean firstPass) {
		Set thisTime = new HashSet();
		if (firstPass) {
			compiledSourceFiles = new HashSet();
			Collection modifiedFiles = getModifiedFiles();
			// System.out.println("modified: " + modifiedFiles);
			thisTime.addAll(modifiedFiles);
			// ??? eclipse IncrementalImageBuilder appears to do this
			// for (Iterator i = modifiedFiles.iterator(); i.hasNext();) {
			// File file = (File) i.next();
			// addDependentsOf(file);
			// }

			if (addedFiles != null) {
				for (Iterator fIter = addedFiles.iterator(); fIter.hasNext();) {
					Object o = fIter.next();
					if (!thisTime.contains(o))
						thisTime.add(o);
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

	public Map /* String -> List<ucf> */getBinaryFilesToCompile(boolean firstTime) {
		if (lastSuccessfulBuildTime == -1 || buildConfig == null || !maybeIncremental()) {
			return binarySourceFiles;
		}
		// else incremental...
		Map toWeave = new HashMap();
		if (firstTime) {
			List addedOrModified = new ArrayList();
			addedOrModified.addAll(addedBinaryFiles);
			addedOrModified.addAll(getModifiedBinaryFiles());
			for (Iterator iter = addedOrModified.iterator(); iter.hasNext();) {
				AjBuildConfig.BinarySourceFile bsf = (AjBuildConfig.BinarySourceFile) iter.next();
				UnwovenClassFile ucf = createUnwovenClassFile(bsf);
				if (ucf == null)
					continue;
				List ucfs = new ArrayList();
				ucfs.add(ucf);
				recordTypeChanged(ucf.getClassName());
				binarySourceFiles.put(bsf.binSrc.getPath(), ucfs);
				List cfs = new ArrayList(1);
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
		for (Iterator iter = this.inputClassFilesBySource.values().iterator(); iter.hasNext();) {
			List cfs = (List) iter.next();
			for (Iterator iterator = cfs.iterator(); iterator.hasNext();) {
				ClassFile cf = (ClassFile) iterator.next();
				cf.deleteFromFileSystem();
			}
		}
		for (Iterator iterator = classesFromName.values().iterator(); iterator.hasNext();) {
			File f = (File) iterator.next();
			new ClassFile("", f).deleteFromFileSystem();
		}
		for (Iterator iter = resources.iterator(); iter.hasNext();) {
			String resource = (String) iter.next();
			List outputDirs = getOutputLocations(buildConfig);
			for (Iterator iterator = outputDirs.iterator(); iterator.hasNext();) {
				File dir = (File) iterator.next();
				File f = new File(dir, resource);
				if (f.exists()) {
					f.delete();
				}
			}
		}
	}

	private void deleteClassFiles() {
		if (deletedFiles == null)
			return;
		for (Iterator i = deletedFiles.iterator(); i.hasNext();) {
			File deletedFile = (File) i.next();
			addDependentsOf(deletedFile);

			List cfs = (List) this.fullyQualifiedTypeNamesResultingFromCompilationUnit.get(deletedFile);
			this.fullyQualifiedTypeNamesResultingFromCompilationUnit.remove(deletedFile);

			if (cfs != null) {
				for (Iterator iter = cfs.iterator(); iter.hasNext();) {
					ClassFile cf = (ClassFile) iter.next();
					deleteClassFile(cf);
				}
			}

		}
	}

	private void deleteBinaryClassFiles() {
		// range of bsf is ucfs, domain is files (.class and jars) in inpath/jars
		for (Iterator iter = deletedBinaryFiles.iterator(); iter.hasNext();) {
			AjBuildConfig.BinarySourceFile deletedFile = (AjBuildConfig.BinarySourceFile) iter.next();
			List cfs = (List) this.inputClassFilesBySource.get(deletedFile.binSrc.getPath());
			for (Iterator iterator = cfs.iterator(); iterator.hasNext();) {
				deleteClassFile((ClassFile) iterator.next());
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
		cf.deleteFromFileSystem();
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

		references.put(sourceFile, new ReferenceCollection(cr.qualifiedReferences, cr.simpleNameReferences));

		UnwovenClassFile[] unwovenClassFiles = result.unwovenClassFiles();
		for (int i = 0; i < unwovenClassFiles.length; i++) {
			File lastTimeRound = (File) classesFromName.get(unwovenClassFiles[i].getClassName());
			recordClassFile(unwovenClassFiles[i], lastTimeRound);
			classesFromName.put(unwovenClassFiles[i].getClassName(), new File(unwovenClassFiles[i].getFilename()));
		}

		// need to do this before types are deleted from the World...
		recordWhetherCompilationUnitDefinedAspect(sourceFile, cr);
		deleteTypesThatWereInThisCompilationUnitLastTimeRoundButHaveBeenDeletedInThisIncrement(sourceFile, unwovenClassFiles);

		recordFQNsResultingFromCompilationUnit(sourceFile, result);
	}

	/**
	 * @param sourceFile
	 * @param unwovenClassFiles
	 */
	private void deleteTypesThatWereInThisCompilationUnitLastTimeRoundButHaveBeenDeletedInThisIncrement(File sourceFile,
			UnwovenClassFile[] unwovenClassFiles) {
		List classFiles = (List) this.fullyQualifiedTypeNamesResultingFromCompilationUnit.get(sourceFile);
		if (classFiles != null) {
			for (int i = 0; i < unwovenClassFiles.length; i++) {
				// deleting also deletes types from the weaver... don't do this if they are
				// still present this time around...
				removeFromClassFilesIfPresent(unwovenClassFiles[i].getClassName(), classFiles);
			}
			for (Iterator iter = classFiles.iterator(); iter.hasNext();) {
				ClassFile cf = (ClassFile) iter.next();
				deleteClassFile(cf);
			}
		}
	}

	private void removeFromClassFilesIfPresent(String className, List classFiles) {
		ClassFile victim = null;
		for (Iterator iter = classFiles.iterator(); iter.hasNext();) {
			ClassFile cf = (ClassFile) iter.next();
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
		List classFiles = new ArrayList();
		UnwovenClassFile[] types = icr.unwovenClassFiles();
		for (int i = 0; i < types.length; i++) {
			classFiles.add(new ClassFile(types[i].getClassName(), new File(types[i].getFilename())));
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
				for (Iterator iterator = compiledTypes.keySet().iterator(); iterator.hasNext();) {
					char[] className = (char[]) iterator.next();
					String typeName = new String(className).replace('/', '.');
					if (typeName.indexOf(BcelWeaver.SYNTHETIC_CLASS_POSTFIX) == -1) {
						ResolvedType rt = world.resolve(typeName);
						if (rt.isMissing()) {
							throw new IllegalStateException("Type '" + rt.getSignature() + "' not found in world!");
						}
						if (rt.isAspect()) {
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
			// record resolved type for structural comparisions in future increments
			// this records a second reference to a structure already held in memory
			// by the world.
			ResolvedType rType = world.resolve(thisTime.getClassName());
			if (!rType.isMissing()) {
				try {
					ClassFileReader reader = new ClassFileReader(thisTime.getBytes(), null);
					this.resolvedTypeStructuresFromLastBuild.put(thisTime.getClassName(), new CompactTypeStructureRepresentation(
							reader));
				} catch (ClassFormatException cfe) {
					throw new BCException("Unexpected problem processing class", cfe);
				}
			}
			return;
		}

		CompactTypeStructureRepresentation existingStructure = (CompactTypeStructureRepresentation) this.resolvedTypeStructuresFromLastBuild
				.get(thisTime.getClassName());
		ResolvedType newResolvedType = world.resolve(thisTime.getClassName());
		if (!newResolvedType.isMissing()) {
			try {
				ClassFileReader reader = new ClassFileReader(thisTime.getBytes(), null);
				this.resolvedTypeStructuresFromLastBuild.put(thisTime.getClassName(),
						new CompactTypeStructureRepresentation(reader));
			} catch (ClassFormatException cfe) {
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
					if (world.forDEBUG_structuralChangesCode)
						System.err.println("Detected a structural change in " + thisTime.getFilename());
					structuralChangesSinceLastFullBuild.put(thisTime.getFilename(), new Long(currentBuildTime));
					recordTypeChanged(new String(reader.getName()).replace('/', '.'));
				}
			}
		} catch (ClassFormatException e) {
			recordTypeChanged(thisTime.getClassName());
		}
	}

	private static final char[][] EMPTY_CHAR_ARRAY = new char[0][];

	/**
	 * Compare the class structure of the new intermediate (unwoven) class with the existingResolvedType of the same class that we
	 * have in the world, looking for any structural differences (and ignoring aj members resulting from weaving....)
	 * 
	 * Some notes from Andy... lot of problems here, which I've eventually resolved by building the compactstructure based on a
	 * classfilereader, rather than on a ResolvedType. There are accessors for inner types and funky fields that the compiler
	 * creates to support the language - for non-static inner types it also mangles ctors to be prefixed with an instance of the
	 * surrounding type.
	 * 
	 * Warning : long but boring method implementation...
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

		// interfaces
		char[][] existingIfs = existingType.interfaces;
		char[][] newIfsAsChars = reader.getInterfaceNames();
		if (newIfsAsChars == null) {
			newIfsAsChars = EMPTY_CHAR_ARRAY;
		} // damn I'm lazy...
		if (existingIfs == null) {
			existingIfs = EMPTY_CHAR_ARRAY;
		}
		if (existingIfs.length != newIfsAsChars.length)
			return true;
		new_interface_loop: for (int i = 0; i < newIfsAsChars.length; i++) {
			for (int j = 0; j < existingIfs.length; j++) {
				if (CharOperation.equals(existingIfs[j], newIfsAsChars[i])) {
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
		if (newFields.length != existingFs.length)
			return true;
		new_field_loop: for (int i = 0; i < newFields.length; i++) {
			IBinaryField field = newFields[i];
			char[] fieldName = field.getName();
			for (int j = 0; j < existingFs.length; j++) {
				if (CharOperation.equals(existingFs[j].getName(), fieldName)) {
					if (!modifiersEqual(field.getModifiers(), existingFs[j].getModifiers())) {
						return true;
					}
					if (!CharOperation.equals(existingFs[j].getTypeName(), field.getTypeName())) {
						return true;
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
		if (newMethods.length != existingMs.length)
			return true;
		new_method_loop: for (int i = 0; i < newMethods.length; i++) {
			IBinaryMethod method = newMethods[i];
			char[] methodName = method.getSelector();
			for (int j = 0; j < existingMs.length; j++) {
				if (CharOperation.equals(existingMs[j].getSelector(), methodName)) {
					// candidate match
					if (!CharOperation.equals(method.getMethodDescriptor(), existingMs[j].getMethodDescriptor())) {
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
						if (!modifiersEqual(method.getModifiers(), existingMs[j].getModifiers())) {
							return true;
						}
						continue new_method_loop;
					}
				}
			}
			return true; // (no match found)
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

	private String stringifyList(Set l) {
		StringBuffer sb = new StringBuffer();
		sb.append("{");
		for (Iterator iter = l.iterator(); iter.hasNext();) {
			Object el = iter.next();
			sb.append(el);
			if (iter.hasNext())
				sb.append(",");
		}
		sb.append("}");
		return sb.toString();
	}

	protected void addAffectedSourceFiles(Set addTo, Set lastTimeSources) {
		if (qualifiedStrings.elementSize == 0 && simpleStrings.elementSize == 0)
			return;
		if (listenerDefined())
			getListener().recordDecision(
					"Examining whether any other files now need compilation based on just compiling: '"
							+ stringifyList(lastTimeSources) + "'");
		// the qualifiedStrings are of the form 'p1/p2' & the simpleStrings are just 'X'
		char[][][] qualifiedNames = ReferenceCollection.internQualifiedNames(qualifiedStrings);
		// if a well known qualified name was found then we can skip over these
		if (qualifiedNames.length < qualifiedStrings.elementSize)
			qualifiedNames = null;
		char[][] simpleNames = ReferenceCollection.internSimpleNames(simpleStrings);
		// if a well known name was found then we can skip over these
		if (simpleNames.length < simpleStrings.elementSize)
			simpleNames = null;

		// System.err.println("simple: " + simpleStrings);
		// System.err.println("qualif: " + qualifiedStrings);

		for (Iterator i = references.entrySet().iterator(); i.hasNext();) {
			Map.Entry entry = (Map.Entry) i.next();
			ReferenceCollection refs = (ReferenceCollection) entry.getValue();
			if (refs != null && refs.includes(qualifiedNames, simpleNames)) {
				File file = (File) entry.getKey();
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
		if (addTo.size() > 0)
			addTo.addAll(lastTimeSources);
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
		if (memberIndex > 0)
			typeName = typeName.substring(0, memberIndex);
		simpleStrings.add(typeName);
	}

	protected void addDependentsOf(File sourceFile) {
		List cfs = (List) this.fullyQualifiedTypeNamesResultingFromCompilationUnit.get(sourceFile);

		if (cfs != null) {
			for (Iterator iter = cfs.iterator(); iter.hasNext();) {
				ClassFile cf = (ClassFile) iter.next();
				recordTypeChanged(cf.fullyQualifiedTypeName);
			}
		}

	}

	public void setStructureModel(IHierarchy model) {
		structureModel = model;
	}

	public IHierarchy getStructureModel() {
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
	}

	public BcelWorld getBcelWorld() {
		return world;
	}

	public void setRelationshipMap(IRelationshipMap irm) {
		relmap = irm;
	}

	public IRelationshipMap getRelationshipMap() {
		return relmap;
	}

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
		this.binarySourceFiles = new HashMap();
	}

	public void recordBinarySource(String fromPathName, List unwovenClassFiles) {
		this.binarySourceFiles.put(fromPathName, unwovenClassFiles);
		if (this.maybeIncremental()) {
			List simpleClassFiles = new LinkedList();
			for (Iterator iter = unwovenClassFiles.iterator(); iter.hasNext();) {
				UnwovenClassFile ucf = (UnwovenClassFile) iter.next();
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

	public Map getBinarySourceMap() {
		return this.binarySourceFiles;
	}

	public Map getClassNameToFileMap() {
		return this.classesFromName;
	}

	public boolean hasResource(String resourceName) {
		return this.resources.contains(resourceName);
	}

	public void recordResource(String resourceName) {
		this.resources.add(resourceName);
	}

	/**
	 * @return Returns the addedFiles.
	 */
	public Set getAddedFiles() {
		return this.addedFiles;
	}

	/**
	 * @return Returns the deletedFiles.
	 */
	public Set getDeletedFiles() {
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

		public void deleteFromFileSystem() {
			String namePrefix = locationOnDisk.getName();
			namePrefix = namePrefix.substring(0, namePrefix.lastIndexOf('.'));
			final String targetPrefix = namePrefix + BcelWeaver.CLOSURE_CLASS_PREFIX;
			File dir = locationOnDisk.getParentFile();
			if (dir != null) {
				File[] weaverGenerated = dir.listFiles(new FilenameFilter() {
					public boolean accept(File dir, String name) {
						return name.startsWith(targetPrefix);
					}
				});
				if (weaverGenerated != null) {
					for (int i = 0; i < weaverGenerated.length; i++) {
						weaverGenerated[i].delete();
					}
				}
			}
			locationOnDisk.delete();
		}
	}

	public void wipeAllKnowledge() {
		buildManager.state = null;
		buildManager.setStructureModel(null);
	}

	public Map getAspectNamesToFileNameMap() {
		return aspectsFromFileNames;
	}

	public void initializeAspectNamesToFileNameMap() {
		this.aspectsFromFileNames = new HashMap();
	}

	// Will allow us to record decisions made during incremental processing, hopefully aid in debugging
	public boolean listenerDefined() {
		return stateListener != null;
	}

	public IStateListener getListener() {
		return stateListener;
	}

	public IBinaryType checkPreviousBuild(String name) {
		return (IBinaryType) resolvedTypeStructuresFromLastBuild.get(name);
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
}
