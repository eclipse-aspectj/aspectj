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
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aspectj.ajdt.internal.compiler.InterimCompilationResult;
import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.Message;
import org.aspectj.bridge.SourceLocation;
import org.aspectj.util.FileUtil;
import org.aspectj.weaver.bcel.UnwovenClassFile;
import org.aspectj.org.eclipse.jdt.internal.compiler.CompilationResult;
import org.aspectj.org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader;
import org.aspectj.org.eclipse.jdt.internal.compiler.classfmt.ClassFormatException;
import org.aspectj.org.eclipse.jdt.internal.core.builder.ReferenceCollection;
import org.aspectj.org.eclipse.jdt.internal.core.builder.StringSet;


/**
 * Holds state needed for incremental compilation
 */
public class AjState {
	AjBuildManager buildManager;
	
	long lastSuccessfulBuildTime = -1;
	long currentBuildTime = -1;
	AjBuildConfig buildConfig;
	AjBuildConfig newBuildConfig;
	
//	Map/*<File, List<UnwovenClassFile>*/ classesFromFile = new HashMap();
	Map/*<File, CompilationResult*/ resultsFromFile = new HashMap();
	Map/*<File, ReferenceCollection>*/ references = new HashMap();
	Map/*File, List<UnwovenClassFile>*/ binarySourceFiles = new HashMap();
	Map/*<String, UnwovenClassFile>*/ classesFromName = new HashMap();
	List/*File*/ compiledSourceFiles = new ArrayList();
	List/*String*/ resources = new ArrayList();
	
	ArrayList/*<String>*/ qualifiedStrings;
	ArrayList/*<String>*/ simpleStrings;
	
	Set addedFiles;
	Set deletedFiles;
	Set /*BinarySourceFile*/addedBinaryFiles;
	Set /*BinarySourceFile*/deletedBinaryFiles;
	
	List addedClassFiles;
	
	public AjState(AjBuildManager buildManager) {
		this.buildManager = buildManager;
	}
	
	void successfulCompile(AjBuildConfig config) {
		buildConfig = config;
		lastSuccessfulBuildTime = currentBuildTime;
	}
	
	/**
	 * Returns false if a batch build is needed.
	 */
	boolean prepareForNextBuild(AjBuildConfig newBuildConfig) {
		currentBuildTime = System.currentTimeMillis();
		
		addedClassFiles = new ArrayList();
		
		if (lastSuccessfulBuildTime == -1 || buildConfig == null) {
			return false;
		}
		
		// we don't support incremental with an outjar yet
		if (newBuildConfig.getOutputJar() != null) return false;
		
		// we can't do an incremental build if one of our paths
		// has changed, or a jar on a path has been modified
		if (pathChange(buildConfig,newBuildConfig)) {
		    // last time we built, .class files and resource files from jars on the
		    // inpath will have been copied to the output directory.
		    // these all need to be deleted in preparation for the clean build that is
		    // coming - otherwise a file that has been deleted from an inpath jar 
		    // since the last build will not be deleted from the output directory.
		    removeAllResultsOfLastBuild();
		    return false;
		}
		
		simpleStrings = new ArrayList();
		qualifiedStrings = new ArrayList();
		
		Set oldFiles = new HashSet(buildConfig.getFiles());
		Set newFiles = new HashSet(newBuildConfig.getFiles());
		
		addedFiles = new HashSet(newFiles);
		addedFiles.removeAll(oldFiles);
		deletedFiles = new HashSet(oldFiles);
		deletedFiles.removeAll(newFiles);
		
		Set oldBinaryFiles = new HashSet(buildConfig.getBinaryFiles());
		Set newBinaryFiles = new HashSet(newBuildConfig.getBinaryFiles());
		
		addedBinaryFiles = new HashSet(newBinaryFiles);
		addedBinaryFiles.removeAll(oldBinaryFiles);
		deletedBinaryFiles = new HashSet(oldBinaryFiles);
		deletedBinaryFiles.removeAll(newBinaryFiles);
		
		this.newBuildConfig = newBuildConfig;
		
		return true;
	}
	
	private Collection getModifiedFiles() {		
		return getModifiedFiles(lastSuccessfulBuildTime);
	}

	Collection getModifiedFiles(long lastBuildTime) {
		List ret = new ArrayList();
		//not our job to account for new and deleted files
		for (Iterator i = buildConfig.getFiles().iterator(); i.hasNext(); ) {
			File file = (File)i.next();
			if (!file.exists()) continue;
			
			long modTime = file.lastModified();
			//System.out.println("check: " + file + " mod " + modTime + " build " + lastBuildTime);	
			// need to add 1000 since lastModTime is only accurate to a second on some (all?) platforms
			if (modTime + 1000 >= lastBuildTime) {
				ret.add(file);
			} 
		}
		return ret;
	}

	private Collection getModifiedBinaryFiles() {		
		return getModifiedBinaryFiles(lastSuccessfulBuildTime);
	}

	Collection getModifiedBinaryFiles(long lastBuildTime) {
		List ret = new ArrayList();
		//not our job to account for new and deleted files
		for (Iterator i = buildConfig.getBinaryFiles().iterator(); i.hasNext(); ) {
			AjBuildConfig.BinarySourceFile bsfile = (AjBuildConfig.BinarySourceFile)i.next();
			File file = bsfile.binSrc;
			if (!file.exists()) continue;
			
			long modTime = file.lastModified();
			//System.out.println("check: " + file + " mod " + modTime + " build " + lastBuildTime);			
			// need to add 1000 since lastModTime is only accurate to a second on some (all?) platforms
			if (modTime + 1000 >= lastBuildTime) {
				ret.add(bsfile);
			} 
		}
		return ret;
	}
	
	private boolean pathChange(AjBuildConfig oldConfig, AjBuildConfig newConfig) {
		boolean changed = false;
		List oldClasspath = oldConfig.getClasspath();
		List newClasspath = newConfig.getClasspath();
		if (changed(oldClasspath,newClasspath)) return true;
		List oldAspectpath = oldConfig.getAspectpath();
		List newAspectpath = newConfig.getAspectpath();
		if (changed(oldAspectpath,newAspectpath)) return true;
		List oldInJars = oldConfig.getInJars();
		List newInJars = newConfig.getInJars();
		if (changed(oldInJars,newInJars)) return true;
		List oldInPath = oldConfig.getInpath();
		List newInPath = newConfig.getInpath();
		if (changed(oldInPath, newInPath)) return true;
		return changed;
	}
	
	private boolean changed(List oldPath, List newPath) {
		if (oldPath == null) oldPath = new ArrayList();
		if (newPath == null) newPath = new ArrayList();
		if (oldPath.size() != newPath.size()) {
			return true;
		}
		for (int i = 0; i < oldPath.size(); i++) {
			if (!oldPath.get(i).equals(newPath.get(i))) {
				return true;
			}
			Object o = oldPath.get(i);  // String on classpath, File on other paths
			File f = null;
			if (o instanceof String) {
				f = new File((String)o);
			} else {
				f = (File) o;
			}
			if (f.exists() && !f.isDirectory() && (f.lastModified() >= lastSuccessfulBuildTime)) {
				return true;
			}
		}
		return false;
	}

	public List getFilesToCompile(boolean firstPass) {
		List thisTime = new ArrayList();
		if (firstPass) {
			compiledSourceFiles = new ArrayList();
			Collection modifiedFiles = getModifiedFiles();
			//System.out.println("modified: " + modifiedFiles);
			thisTime.addAll(modifiedFiles);
			//??? eclipse IncrementalImageBuilder appears to do this
	//		for (Iterator i = modifiedFiles.iterator(); i.hasNext();) {
	//			File file = (File) i.next();
	//			addDependentsOf(file);
	//		}
			
			thisTime.addAll(addedFiles);	
			
			deleteClassFiles();
			deleteResources();
			
			addAffectedSourceFiles(thisTime,thisTime);
		} else {
			
			addAffectedSourceFiles(thisTime,compiledSourceFiles);
		}
		compiledSourceFiles = thisTime;
		return thisTime;
	}

	public Map /* String -> List<ucf> */ getBinaryFilesToCompile(boolean firstTime) {
		if (lastSuccessfulBuildTime == -1 || buildConfig == null) {
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
				if (ucf == null) continue;
				List ucfs = new ArrayList();
				ucfs.add(ucf);
				addDependentsOf(ucf.getClassName());
				binarySourceFiles.put(bsf.binSrc.getPath(),ucfs);
				toWeave.put(bsf.binSrc.getPath(),ucfs);
			}
			deleteBinaryClassFiles();
		} else {
			// return empty set... we've already done our bit.
		}
		return toWeave;
	}
	
	/**
	 * Called when a path change is about to trigger a full build, but
	 * we haven't cleaned up from the last incremental build...
	 */
	private void removeAllResultsOfLastBuild() {
	    // remove all binarySourceFiles, and all classesFromName...
	    for (Iterator iter = binarySourceFiles.values().iterator(); iter.hasNext();) {
            List ucfs = (List) iter.next();
            for (Iterator iterator = ucfs.iterator(); iterator.hasNext();) {
                UnwovenClassFile ucf = (UnwovenClassFile) iterator.next();
                try {
                    ucf.deleteRealFile();
                } catch (IOException ex) { /* we did our best here */ } 
            }
        }
	    for (Iterator iterator = classesFromName.values().iterator(); iterator.hasNext();) {
            UnwovenClassFile ucf = (UnwovenClassFile) iterator.next();
            try {
                ucf.deleteRealFile();
            } catch (IOException ex) { /* we did our best here */ }  
        }
	    for (Iterator iter = resources.iterator(); iter.hasNext();) {
            String resource = (String) iter.next();
            new File(buildConfig.getOutputDir(),resource).delete();            
        }
	}
	
	private void deleteClassFiles() {
		for (Iterator i = deletedFiles.iterator(); i.hasNext(); ) {
			File deletedFile = (File)i.next();
			//System.out.println("deleting: " + deletedFile);
			addDependentsOf(deletedFile);
			InterimCompilationResult intRes = (InterimCompilationResult) resultsFromFile.get(deletedFile);
			resultsFromFile.remove(deletedFile);
			//System.out.println("deleting: " + unwovenClassFiles);
			if (intRes == null) continue;
			for (int j=0; j<intRes.unwovenClassFiles().length; j++ ) {
				deleteClassFile(intRes.unwovenClassFiles()[j]);
			}
		}
	}
	
	private void deleteBinaryClassFiles() {
		// range of bsf is ucfs, domain is files (.class and jars) in inpath/jars
		for (Iterator iter = deletedBinaryFiles.iterator(); iter.hasNext();) {
			AjBuildConfig.BinarySourceFile deletedFile = (AjBuildConfig.BinarySourceFile) iter.next();
			List ucfs = (List) binarySourceFiles.get(deletedFile.binSrc.getPath());
			binarySourceFiles.remove(deletedFile.binSrc.getPath());
			deleteClassFile((UnwovenClassFile)ucfs.get(0));
		}
	}
	
	private void deleteResources() {
		List oldResources = new ArrayList();
		oldResources.addAll(resources);
		
		// note - this deliberately ignores resources in jars as we don't yet handle jar changes
		// with incremental compilation
		for (Iterator i = buildConfig.getInpath().iterator(); i.hasNext(); ) {
			File inPathElement = (File)i.next();
			if (inPathElement.isDirectory() && AjBuildManager.COPY_INPATH_DIR_RESOURCES) {
				deleteResourcesFromDirectory(inPathElement,oldResources);
			}			
		}	
		
		if (buildConfig.getSourcePathResources() != null) {
			for (Iterator i = buildConfig.getSourcePathResources().keySet().iterator(); i.hasNext(); ) {
				String resource = (String)i.next();
				maybeDeleteResource(resource, oldResources);
			}
		}
		
		// oldResources need to be deleted...
		for (Iterator iter = oldResources.iterator(); iter.hasNext();) {
			String victim = (String) iter.next();
			File f = new File(buildConfig.getOutputDir(),victim);
			if (f.exists()) {
				f.delete();
			}			
			resources.remove(victim);
		}
	}
	
	private void maybeDeleteResource(String resName, List oldResources) {
		if (resources.contains(resName)) {
			oldResources.remove(resName);
			File source = new File(buildConfig.getOutputDir(),resName);
			if ((source != null) && (source.exists()) &&
			    (source.lastModified() >= lastSuccessfulBuildTime)) {
				resources.remove(resName); // will ensure it is re-copied
			}
		}		
	}
	
	private void deleteResourcesFromDirectory(File dir, List oldResources) {
		File[] files = FileUtil.listFiles(dir,new FileFilter() {
			public boolean accept(File f) {
				boolean accept = !(f.isDirectory() || f.getName().endsWith(".class")) ;
				return accept;
			}
		});
		
		// For each file, add it either as a real .class file or as a resource
		for (int i = 0; i < files.length; i++) {
			// ASSERT: files[i].getAbsolutePath().startsWith(inFile.getAbsolutePath()
			// or we are in trouble...
			String filename = files[i].getAbsolutePath().substring(
			                    dir.getAbsolutePath().length()+1);
			maybeDeleteResource(filename, oldResources);
		}				
	}

	
	private void deleteClassFile(UnwovenClassFile classFile) {		
		classesFromName.remove(classFile.getClassName());
		
		buildManager.bcelWeaver.deleteClassFile(classFile.getClassName());
		try {
			classFile.deleteRealFile();
		} catch (IOException e) {
			//!!! might be okay to ignore
		}
	}
	
	private UnwovenClassFile createUnwovenClassFile(AjBuildConfig.BinarySourceFile bsf) {
		UnwovenClassFile ucf = null;
		try {
			ucf = buildManager.bcelWeaver.addClassFile(bsf.binSrc, bsf.fromInPathDirectory, buildConfig.getOutputDir());
		} catch(IOException ex) {
			IMessage msg = new Message("can't read class file " + bsf.binSrc.getPath(),
									   new SourceLocation(bsf.binSrc,0),false);
			buildManager.handler.handleMessage(msg);
		}
		return ucf;
	}
	
	public void noteResult(InterimCompilationResult result) {
		File sourceFile = new File(result.fileName());
		CompilationResult cr = result.result();

		if (result != null) {
			references.put(sourceFile, new ReferenceCollection(cr.qualifiedReferences, cr.simpleNameReferences));
		}

		InterimCompilationResult previous = (InterimCompilationResult) resultsFromFile.get(sourceFile);
		UnwovenClassFile[] unwovenClassFiles = result.unwovenClassFiles();
		for (int i = 0; i < unwovenClassFiles.length; i++) {
			UnwovenClassFile lastTimeRound = removeFromPreviousIfPresent(unwovenClassFiles[i],previous);
			recordClassFile(unwovenClassFiles[i],lastTimeRound);
			classesFromName.put(unwovenClassFiles[i].getClassName(),unwovenClassFiles[i]);
		}

		if (previous != null) {
			for (int i = 0; i < previous.unwovenClassFiles().length; i++) {
				if (previous.unwovenClassFiles()[i] != null) {
					deleteClassFile(previous.unwovenClassFiles()[i]);
				}
			}
		}
		resultsFromFile.put(sourceFile, result);

	}
	
	private UnwovenClassFile removeFromPreviousIfPresent(UnwovenClassFile cf, InterimCompilationResult previous) {
		if (previous == null) return null;
		UnwovenClassFile[] unwovenClassFiles = previous.unwovenClassFiles();
		for (int i = 0; i < unwovenClassFiles.length; i++) {
			UnwovenClassFile candidate = unwovenClassFiles[i];
			if ((candidate != null) && candidate.getFilename().equals(cf.getFilename())) {
				unwovenClassFiles[i] = null;
				return candidate;
			}
		}
		return null;
	}
	
	private void recordClassFile(UnwovenClassFile thisTime, UnwovenClassFile lastTime) {
		if (simpleStrings == null) return; // batch build

		if (lastTime == null) {
			addDependentsOf(thisTime.getClassName());
			return;
		}

		byte[] newBytes = thisTime.getBytes();
		byte[] oldBytes = lastTime.getBytes();
		boolean bytesEqual = (newBytes.length == oldBytes.length);
		for (int i = 0; (i < oldBytes.length) && bytesEqual; i++) {
			if (newBytes[i] != oldBytes[i]) bytesEqual = false;
		}
		if (!bytesEqual) {
			try {
				ClassFileReader reader = new ClassFileReader(oldBytes, lastTime.getFilename().toCharArray());
				// ignore local types since they're only visible inside a single method
				if (!(reader.isLocal() || reader.isAnonymous()) && reader.hasStructuralChanges(newBytes)) {
					addDependentsOf(lastTime.getClassName());
				}
			} catch (ClassFormatException e) {
				addDependentsOf(lastTime.getClassName());
			}			
		}
	}
	

//	public void noteClassesFromFile(CompilationResult result, String sourceFileName, List unwovenClassFiles) {
//		File sourceFile = new File(sourceFileName);
//		
//		if (result != null) {
//			references.put(sourceFile, new ReferenceCollection(result.qualifiedReferences, result.simpleNameReferences));
//		}
//		
//		List previous = (List)classesFromFile.get(sourceFile);
//		List newClassFiles = new ArrayList();
//		for (Iterator i = unwovenClassFiles.iterator(); i.hasNext();) {
//			UnwovenClassFile cf = (UnwovenClassFile) i.next();
//			cf = writeClassFile(cf, findAndRemoveClassFile(cf.getClassName(), previous));
//			newClassFiles.add(cf);
//			classesFromName.put(cf.getClassName(), cf);
//		}
//		
//		if (previous != null && !previous.isEmpty()) {
//			for (Iterator i = previous.iterator(); i.hasNext();) {
//				UnwovenClassFile cf = (UnwovenClassFile) i.next();
//				deleteClassFile(cf);
//			}
//		}
//
//		classesFromFile.put(sourceFile, newClassFiles);
//		resultsFromFile.put(sourceFile, result);
//	}
//
//	private UnwovenClassFile findAndRemoveClassFile(String name, List previous) {
//		if (previous == null) return null;
//		for (Iterator i = previous.iterator(); i.hasNext();) {
//			UnwovenClassFile cf = (UnwovenClassFile) i.next();
//			if (cf.getClassName().equals(name)) {
//				i.remove();
//				return cf;
//			} 
//		}
//		return null;
//	}
//
//	private UnwovenClassFile writeClassFile(UnwovenClassFile cf, UnwovenClassFile previous) {
//		if (simpleStrings == null) { // batch build
//			addedClassFiles.add(cf);
//			return cf;
//		}
//		
//		try {
//			if (previous == null) {
//				addedClassFiles.add(cf);
//				addDependentsOf(cf.getClassName());
//				return cf;
//			} 
//			
//			byte[] oldBytes = previous.getBytes();
//			byte[] newBytes = cf.getBytes();
//			//if (this.compileLoop > 1) { // only optimize files which were recompiled during the dependent pass, see 33990
//				notEqual : if (newBytes.length == oldBytes.length) {
//					for (int i = newBytes.length; --i >= 0;) {
//						if (newBytes[i] != oldBytes[i]) break notEqual;
//					}
//					//addedClassFiles.add(previous); //!!! performance wasting
//					buildManager.bcelWorld.addSourceObjectType(previous.getJavaClass());
//					return previous; // bytes are identical so skip them
//				}
//			//}
//			ClassFileReader reader = new ClassFileReader(oldBytes, previous.getFilename().toCharArray());
//			// ignore local types since they're only visible inside a single method
//			if (!(reader.isLocal() || reader.isAnonymous()) && reader.hasStructuralChanges(newBytes)) {
//				addDependentsOf(cf.getClassName());
//			}
//		} catch (ClassFormatException e) {
//			addDependentsOf(cf.getClassName());
//		}
//		addedClassFiles.add(cf);
//		return cf;
//	}
	
	private static StringSet makeStringSet(List strings) {
		StringSet ret = new StringSet(strings.size());
		for (Iterator iter = strings.iterator(); iter.hasNext();) {
			String element = (String) iter.next();
			ret.add(element);
		}
		return ret;
	}
		
	
	
	protected void addAffectedSourceFiles(List addTo, List lastTimeSources) {
		if (qualifiedStrings.isEmpty() && simpleStrings.isEmpty()) return;

		// the qualifiedStrings are of the form 'p1/p2' & the simpleStrings are just 'X'
		char[][][] qualifiedNames = ReferenceCollection.internQualifiedNames(makeStringSet(qualifiedStrings));
		// if a well known qualified name was found then we can skip over these
		if (qualifiedNames.length < qualifiedStrings.size())
			qualifiedNames = null;
		char[][] simpleNames = ReferenceCollection.internSimpleNames(makeStringSet(simpleStrings));
		// if a well known name was found then we can skip over these
		if (simpleNames.length < simpleStrings.size())
			simpleNames = null;

		//System.err.println("simple: " + simpleStrings);
		//System.err.println("qualif: " + qualifiedStrings);

		for (Iterator i = references.entrySet().iterator(); i.hasNext();) {
			Map.Entry entry = (Map.Entry) i.next();
			ReferenceCollection refs = (ReferenceCollection)entry.getValue();
			if (refs != null && refs.includes(qualifiedNames, simpleNames)) {
				File file = (File)entry.getKey();
				if (file.exists()) {
					if (!lastTimeSources.contains(file)) {  //??? O(n**2)
						addTo.add(file);
					}
				}
			}
		}
		
		qualifiedStrings.clear();
		simpleStrings.clear();
	}

	protected void addDependentsOf(String qualifiedTypeName) {
		int lastDot = qualifiedTypeName.lastIndexOf('.');
		String typeName;
		if (lastDot != -1) {
			String packageName = qualifiedTypeName.substring(0,lastDot).replace('.', '/');
			if (!qualifiedStrings.contains(packageName)) { //??? O(n**2)
				qualifiedStrings.add(packageName);
			}
			typeName = qualifiedTypeName.substring(lastDot+1);
		} else {
			qualifiedStrings.add("");
			typeName = qualifiedTypeName;
		}

			
		int memberIndex = typeName.indexOf('$');
		if (memberIndex > 0)
			typeName = typeName.substring(0, memberIndex);
		if (!simpleStrings.contains(typeName)) {  //??? O(n**2)
			simpleStrings.add(typeName);
		}		
		//System.err.println("adding: " + qualifiedTypeName);
	}

	protected void addDependentsOf(File sourceFile) {
		InterimCompilationResult intRes = (InterimCompilationResult)resultsFromFile.get(sourceFile);
		if (intRes == null) return;
		
		for (int i = 0; i < intRes.unwovenClassFiles().length; i++) {
			addDependentsOf(intRes.unwovenClassFiles()[i].getClassName());
		}
	}
}
