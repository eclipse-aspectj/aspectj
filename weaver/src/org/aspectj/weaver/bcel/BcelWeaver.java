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


package org.aspectj.weaver.bcel;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;
import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.IProgressListener;
import org.aspectj.util.FileUtil;
import org.aspectj.weaver.ConcreteTypeMunger;
import org.aspectj.weaver.CrosscuttingMembersSet;
import org.aspectj.weaver.IWeaver;
import org.aspectj.weaver.NewParentTypeMunger;
import org.aspectj.weaver.ResolvedTypeMunger;
import org.aspectj.weaver.ResolvedTypeX;
import org.aspectj.weaver.ShadowMunger;
import org.aspectj.weaver.TypeX;
import org.aspectj.weaver.patterns.DeclareParents;

public class BcelWeaver implements IWeaver {
    private BcelWorld world;
    private CrosscuttingMembersSet xcutSet;
    private IProgressListener progressListener = null;
    private double progressMade;
    private double progressPerClassFile;

    public BcelWeaver(BcelWorld world) {
        super();
        this.world = world;
        this.xcutSet = world.getCrosscuttingMembersSet();
    }
        
    public BcelWeaver() {
    	this(new BcelWorld());
    }

	// ---- fields	
    private Map  sourceJavaClasses = new HashMap();   /* String -> UnwovenClassFile */
    private List addedClasses      = new ArrayList(); /* List<UnovenClassFile> */
    private List deletedTypenames  = new ArrayList(); /* List<String> */
    private Map  resources         = new HashMap(); /* String -> UnwovenClassFile */ 
    private boolean needToReweaveWorld = false;

    private List shadowMungerList = null; // setup by prepareForWeave
	private List typeMungerList = null; // setup by prepareForWeave 
	private List declareParentsList = null; // setup by prepareForWeave 

    private ZipOutputStream zipOutputStream;

	// ----
    
	// only called for testing
    public void setShadowMungers(List l) {
        shadowMungerList = l;
    }


    public void addLibraryAspect(String aspectName) {
    	ResolvedTypeX type = world.resolve(aspectName);
    	//System.out.println("type: " + type + " for " + aspectName);
		if (type.isAspect()) {
			xcutSet.addOrReplaceAspect(type);
		} else {
			throw new RuntimeException("unimplemented");
		}
    }
    


	public void addLibraryJarFile(File inFile) throws IOException {
		ZipInputStream inStream = new ZipInputStream(new FileInputStream(inFile)); //??? buffered
		
		List addedAspects = new ArrayList();
		
		while (true) {
			ZipEntry entry = inStream.getNextEntry();
			if (entry == null) break;
			
			if (entry.isDirectory() || !entry.getName().endsWith(".class")) {
				continue;
			}
			
			ClassParser parser = new ClassParser(new ByteArrayInputStream(FileUtil.readAsByteArray(inStream)), entry.getName());
	        JavaClass jc = parser.parse();
			inStream.closeEntry();
			
			ResolvedTypeX type = world.addSourceObjectType(jc).getResolvedTypeX();
    		if (type.isAspect()) {
    			addedAspects.add(type);
    		}
			
		}
		
		inStream.close();
		
		for (Iterator i = addedAspects.iterator(); i.hasNext();) {
			ResolvedTypeX aspectX = (ResolvedTypeX) i.next();
			xcutSet.addOrReplaceAspect(aspectX);
		}
	}


	/**
	 * Add any .class files in the directory to the outdir.  Anything other than .class files in
	 * the directory (or its subdirectories) are considered resources and are also copied. 
	 *  
	 */
	public void addDirectoryContents(File inFile,File outDir) throws IOException {
		
		// Get a list of all files (i.e. everything that isnt a directory)
		File[] files = FileUtil.listFiles(inFile,new FileFilter() {
			public boolean accept(File f) {
				boolean accept = !f.isDirectory();
				return accept;
			}
		});
		
		// For each file, add it either as a real .class file or as a resource
		for (int i = 0; i < files.length; i++) {
			
			FileInputStream fis = new FileInputStream(files[i]);
			byte[] bytes = FileUtil.readAsByteArray(fis);
			// String relativePath = files[i].getPath();
			
			// ASSERT: files[i].getAbsolutePath().startsWith(inFile.getAbsolutePath()
			// or we are in trouble...
			String filename = files[i].getAbsolutePath().substring(
			                    inFile.getAbsolutePath().length()+1);
			UnwovenClassFile classFile = new UnwovenClassFile(new File(outDir,filename).getAbsolutePath(),bytes);
			if (filename.endsWith(".class")) {
				// System.err.println("BCELWeaver: processing class from input directory "+classFile);
				this.addClassFile(classFile);
			} else {
				// System.err.println("BCELWeaver: processing resource from input directory "+filename);
				addResource(filename,classFile);
			}
			fis.close();
		}
		
	}


	/** Adds all class files in the jar
	 */
	public void addJarFile(File inFile, File outDir, boolean canBeDirectory) throws IOException {
//		System.err.println("? addJarFile(" + inFile + ", " + outDir + ")");
		needToReweaveWorld = true;
		
		// Is this a directory we are looking at?
		if (inFile.isDirectory() && canBeDirectory) {
			addDirectoryContents(inFile,outDir);
		} else {
		
			ZipInputStream inStream = new ZipInputStream(new FileInputStream(inFile)); //??? buffered
		
			while (true) {
				ZipEntry entry = inStream.getNextEntry();
				if (entry == null) break;
			
				byte[] bytes = FileUtil.readAsByteArray(inStream);
				String filename = entry.getName();
				UnwovenClassFile classFile = new UnwovenClassFile(new File(outDir, filename).getAbsolutePath(), bytes);

				if (filename.endsWith(".class")) {
					this.addClassFile(classFile);
				}
				else if (!entry.isDirectory()) {

					/* bug-44190 Copy meta-data */
					addResource(filename,classFile);
				}

				inStream.closeEntry();
			}
			inStream.close();
		}
	}

	public void addResource(String name, File inPath, File outDir) throws IOException {

		/* Eliminate CVS files. Relative paths use "/" */
		if (!name.startsWith("CVS/") && (-1 == name.indexOf("/CVS/")) && !name.endsWith("/CVS")) {
//			System.err.println("? addResource('" + name + "')");
//			BufferedInputStream inStream = new BufferedInputStream(new FileInputStream(inPath));
//			byte[] bytes = new byte[(int)inPath.length()];
//			inStream.read(bytes);
//			inStream.close();
			byte[] bytes = FileUtil.readAsByteArray(inPath);
			UnwovenClassFile resourceFile = new UnwovenClassFile(new File(outDir, name).getAbsolutePath(), bytes);
			addResource(name,resourceFile);
		}
	}
    
    /** Should be addOrReplace
     */
    public void addClassFile(UnwovenClassFile classFile) {
    	addedClasses.add(classFile);
    	if (null != sourceJavaClasses.put(classFile.getClassName(), classFile)) {
//    		throw new RuntimeException(classFile.getClassName());
    	}
    	world.addSourceObjectType(classFile.getJavaClass());
    }


    public void deleteClassFile(String typename) {
    	deletedTypenames.add(typename);
    	sourceJavaClasses.remove(typename);
    	world.deleteSourceObjectType(TypeX.forName(typename));
    }

	public void addResource (String name, UnwovenClassFile resourceFile) {
		/* bug-44190 Change error to warning and copy first resource */
		if (!resources.containsKey(name)) {
			resources.put(name, resourceFile);
		}
		else {
			world.showMessage(IMessage.WARNING, "duplicate resource: '" + name + "'",
				null, null);
		}
	}

	// ---- weave preparation

    public void prepareForWeave() {
    	needToReweaveWorld = false;

    	
    	// update mungers
    	for (Iterator i = addedClasses.iterator(); i.hasNext(); ) { 
    		UnwovenClassFile jc = (UnwovenClassFile)i.next();
    		String name = jc.getClassName();
    		ResolvedTypeX type = world.resolve(name);
    		//System.err.println("added: " + type + " aspect? " + type.isAspect());
    		if (type.isAspect()) {
    			needToReweaveWorld |= xcutSet.addOrReplaceAspect(type);
    		}
    	}

    	for (Iterator i = deletedTypenames.iterator(); i.hasNext(); ) { 
    		String name = (String)i.next();
    		if (xcutSet.deleteAspect(TypeX.forName(name))) needToReweaveWorld = true;
    	}

		shadowMungerList = xcutSet.getShadowMungers();
		typeMungerList = xcutSet.getTypeMungers();
		declareParentsList = xcutSet.getDeclareParents();
    	
		//XXX this gets us a stable (but completely meaningless) order
		Collections.sort(
			shadowMungerList, 
			new Comparator() {
				public int compare(Object o1, Object o2) {
					return o1.toString().compareTo(o2.toString());
				}
			});
    }
    
    public void dumpUnwoven(File file) throws IOException {
    	BufferedOutputStream os = FileUtil.makeOutputStream(file);
    	this.zipOutputStream = new ZipOutputStream(os);
    	dumpUnwoven();
		/* BUG 40943*/
		dumpResourcesToOutJar();
    	zipOutputStream.close();  //this flushes and closes the acutal file
    }
    
    
    public void dumpUnwoven() throws IOException {
    	Collection filesToDump = new HashSet(sourceJavaClasses.values());
    	for (Iterator i = filesToDump.iterator(); i.hasNext(); ) {
            UnwovenClassFile classFile = (UnwovenClassFile)i.next();
            dumpUnchanged(classFile);
       	}
    }
    
	public void dumpResourcesToOutPath() throws IOException {
//		System.err.println("? dumpResourcesToOutPath() resources=" + resources.keySet());
		Iterator i = resources.keySet().iterator();
		while (i.hasNext()) {
			UnwovenClassFile res = (UnwovenClassFile)resources.get(i.next());
			dumpUnchanged(res);
		}
	}

	/* BUG #40943 */
    public void dumpResourcesToOutJar() throws IOException {
//		System.err.println("? dumpResourcesToOutJar() resources=" + resources.keySet());
		Iterator i = resources.keySet().iterator();
		while (i.hasNext()) {
			String name = (String)i.next();
			UnwovenClassFile res = (UnwovenClassFile)resources.get(name);
			writeZipEntry(name,res.getBytes());
		}
    }
    
    // ---- weaving

    public Collection weave(File file) throws IOException {
    	OutputStream os = FileUtil.makeOutputStream(file);
    	this.zipOutputStream = new ZipOutputStream(os);
    	Collection c = weave();
    	/* BUG 40943*/
    	dumpResourcesToOutJar();
    	zipOutputStream.close();  //this flushes and closes the acutal file
    	return c;
    }
    
    public Collection weave() throws IOException {
    	prepareForWeave();
    	Collection filesToWeave;
    	
    	if (needToReweaveWorld) {
    		filesToWeave = sourceJavaClasses.values();
    	} else {
    		filesToWeave = addedClasses;
    	}
    	
    	Collection wovenClassNames = new ArrayList();
    	world.showMessage(IMessage.INFO, "might need to weave " + filesToWeave + 
    					"(world=" + needToReweaveWorld + ")", null, null);
    	
    	
    	//System.err.println("typeMungers: " + typeMungerList);
    	
		// clear all state from files we'll be reweaving
        for (Iterator i = filesToWeave.iterator(); i.hasNext(); ) {
            UnwovenClassFile classFile = (UnwovenClassFile)i.next();
	    	String className = classFile.getClassName();
            BcelObjectType classType = BcelWorld.getBcelObjectType(world.resolve(className));
            classType.resetState();
        }
    	
    	
    	
    	//XXX this isn't quite the right place for this...
    	for (Iterator i = filesToWeave.iterator(); i.hasNext(); ) {
            UnwovenClassFile classFile = (UnwovenClassFile)i.next();
            String className = classFile.getClassName();
            ResolvedTypeX onType = world.resolve(className);
            weave(onType);
        }
    	
		// first weave into aspects
        for (Iterator i = filesToWeave.iterator(); i.hasNext(); ) {
            UnwovenClassFile classFile = (UnwovenClassFile)i.next();
	    	String className = classFile.getClassName();
            BcelObjectType classType = BcelWorld.getBcelObjectType(world.resolve(className));
            if (classType.isAspect()) {
	            weave(classFile, classType);
	            wovenClassNames.add(className);
            }
        }

		// then weave into non-aspects
		for (Iterator i = filesToWeave.iterator(); i.hasNext(); ) {
            UnwovenClassFile classFile = (UnwovenClassFile)i.next();
	    	String className = classFile.getClassName();
            BcelObjectType classType = BcelWorld.getBcelObjectType(world.resolve(className));
            if (! classType.isAspect()) {
	            weave(classFile, classType);
	            wovenClassNames.add(className);
            }
        }
        
        if (zipOutputStream != null && !needToReweaveWorld) {
        	Collection filesToDump = new HashSet(sourceJavaClasses.values());
        	filesToDump.removeAll(filesToWeave);
        	for (Iterator i = filesToDump.iterator(); i.hasNext(); ) {
                UnwovenClassFile classFile = (UnwovenClassFile)i.next();
                dumpUnchanged(classFile);
        	}
        }
        
        addedClasses = new ArrayList();
    	deletedTypenames = new ArrayList();
		
        return wovenClassNames;
    }

	public void weave(ResolvedTypeX onType) {
		onType.clearInterTypeMungers();
		
		// need to do any declare parents before the matching below
		for (Iterator i = declareParentsList.iterator(); i.hasNext(); ) {
			DeclareParents p = (DeclareParents)i.next();
			List newParents = p.findMatchingNewParents(onType);
			if (!newParents.isEmpty()) {
				BcelObjectType classType = BcelWorld.getBcelObjectType(onType);
				//System.err.println("need to do declare parents for: " + onType);
				for (Iterator j = newParents.iterator(); j.hasNext(); ) {
					ResolvedTypeX newParent = (ResolvedTypeX)j.next();
					if (newParent.isClass()) {
						world.showMessage(IMessage.ERROR,
							"can't use declare parents to change superclass of binary form \'" +
							onType.getName() + "\' (implementation limitation)",
							p.getSourceLocation(), null);
						continue;
					}
					
					classType.addParent(newParent);
					ResolvedTypeMunger newParentMunger = new NewParentTypeMunger(newParent);
					onType.addInterTypeMunger(new BcelTypeMunger(newParentMunger, null));
				}
			}
		}
		
		for (Iterator i = typeMungerList.iterator(); i.hasNext(); ) {
			ConcreteTypeMunger m = (ConcreteTypeMunger)i.next();
			if (m.matches(onType)) {
				onType.addInterTypeMunger(m);
			}
		}
	}


	// exposed for ClassLoader dynamic weaving
	public LazyClassGen weaveWithoutDump(UnwovenClassFile classFile, BcelObjectType classType) throws IOException {
		return weave(classFile, classType, false);
	}

	// non-private for testing
	LazyClassGen weave(UnwovenClassFile classFile, BcelObjectType classType) throws IOException {
		LazyClassGen ret = weave(classFile, classType, true);
		
		if (progressListener != null) {
			progressMade += progressPerClassFile;
			progressListener.setProgress(progressMade);
			progressListener.setText("woven: " + classFile.getFilename());
		}
		
		return ret;
	}
	
	private LazyClassGen weave(UnwovenClassFile classFile, BcelObjectType classType, boolean dump) throws IOException {		
		if (classType.isSynthetic()) {
			if (dump) dumpUnchanged(classFile);
			return null;
		}
		
//		JavaClass javaClass = classType.getJavaClass();
		List shadowMungers = fastMatch(shadowMungerList, classType.getResolvedTypeX());
		List typeMungers = classType.getResolvedTypeX().getInterTypeMungers();
        
        classType.getResolvedTypeX().checkInterTypeMungers();

		LazyClassGen clazz = null;
		
		if (shadowMungers.size() > 0 || typeMungers.size() > 0 || classType.isAspect()) {
			clazz = classType.getLazyClassGen();
			//System.err.println("got lazy gen: " + clazz + ", " + clazz.getWeaverState());
			try {
				boolean isChanged = BcelClassWeaver.weave(world, clazz, shadowMungers, typeMungers);
				if (isChanged) {
					if (dump) dump(classFile, clazz);
					return clazz;
				}
			} catch (RuntimeException re) {
				System.err.println("trouble in: ");
				clazz.print(System.err);
				throw re;
			} catch (Error re) {
				System.err.println("trouble in: ");
				clazz.print(System.err);
				throw re;
			}
		}
		
		// this is very odd return behavior trying to keep everyone happy
		if (dump) {
			dumpUnchanged(classFile);
			return clazz;
		} else {
			return null;
		}
	}



	// ---- writing

	private void dumpUnchanged(UnwovenClassFile classFile) throws IOException {
		if (zipOutputStream != null) {
			writeZipEntry(getEntryName(classFile.getJavaClass().getClassName()), classFile.getBytes());
		} else {
			classFile.writeUnchangedBytes();
		}
	}

	private String getEntryName(String className) {
		//XXX what does bcel's getClassName do for inner names
		return className.replace('.', '/') + ".class";
	}

	private void dump(UnwovenClassFile classFile, LazyClassGen clazz) throws IOException {
		if (zipOutputStream != null) {
			String mainClassName = classFile.getJavaClass().getClassName();
			writeZipEntry(getEntryName(mainClassName),
							clazz.getJavaClass(world).getBytes());
			if (!clazz.getChildClasses(world).isEmpty()) {
				for (Iterator i = clazz.getChildClasses(world).iterator(); i.hasNext();) {
					UnwovenClassFile.ChildClass c = (UnwovenClassFile.ChildClass) i.next();
					writeZipEntry(getEntryName(mainClassName + "$" + c.name), c.bytes);
				}
			}
		} else {
			classFile.writeWovenBytes(
				clazz.getJavaClass(world).getBytes(), 
				clazz.getChildClasses(world)
			);
		}
	}
	
	private void writeZipEntry(String name, byte[] bytes) throws IOException {
		ZipEntry newEntry = new ZipEntry(name);  //??? get compression scheme right
		
		zipOutputStream.putNextEntry(newEntry);
		zipOutputStream.write(bytes);
		zipOutputStream.closeEntry();
	}

	private List fastMatch(List list, ResolvedTypeX type) {
		if (list == null) return Collections.EMPTY_LIST;

		List result = new ArrayList();
		Iterator iter = list.iterator();
		while (iter.hasNext()) {
			ShadowMunger munger = (ShadowMunger)iter.next();
			if (munger.getPointcut().fastMatch(type).maybeTrue()) {
				result.add(munger);
			}
		}
		return result;
	}

	public void setProgressListener(IProgressListener listener, double previousProgress, double progressPerClassFile) {
		progressListener = listener;
		this.progressMade = previousProgress;
		this.progressPerClassFile = progressPerClassFile;
	}

}
