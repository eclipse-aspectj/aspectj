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


import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.jar.Attributes.Name;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.aspectj.apache.bcel.classfile.ClassParser;
import org.aspectj.apache.bcel.classfile.JavaClass;
import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.IProgressListener;
import org.aspectj.bridge.Message;
import org.aspectj.bridge.SourceLocation;
import org.aspectj.util.FileUtil;
import org.aspectj.util.FuzzyBoolean;
import org.aspectj.weaver.ConcreteTypeMunger;
import org.aspectj.weaver.CrosscuttingMembersSet;
import org.aspectj.weaver.IClassFileProvider;
import org.aspectj.weaver.IWeaveRequestor;
import org.aspectj.weaver.IWeaver;
import org.aspectj.weaver.NewParentTypeMunger;
import org.aspectj.weaver.ResolvedTypeMunger;
import org.aspectj.weaver.ResolvedTypeX;
import org.aspectj.weaver.ShadowMunger;
import org.aspectj.weaver.TypeX;
import org.aspectj.weaver.WeaverMessages;
import org.aspectj.weaver.WeaverMetrics;
import org.aspectj.weaver.WeaverStateInfo;
import org.aspectj.weaver.patterns.CflowPointcut;
import org.aspectj.weaver.patterns.DeclareParents;
import org.aspectj.weaver.patterns.FastMatchInfo;


public class BcelWeaver implements IWeaver {
    private BcelWorld world;
    private CrosscuttingMembersSet xcutSet;
    private IProgressListener progressListener = null;
    private double progressMade;
    private double progressPerClassFile;
    
    private boolean inReweavableMode = false;
    
    public BcelWeaver(BcelWorld world) {
        super();
        WeaverMetrics.reset();
        this.world = world;
        this.xcutSet = world.getCrosscuttingMembersSet();
    }
        
    public BcelWeaver() {
    	this(new BcelWorld());
    }

	// ---- fields	
//    private Map  sourceJavaClasses = new HashMap();   /* String -> UnwovenClassFile */
    private List addedClasses      = new ArrayList(); /* List<UnovenClassFile> */
    private List deletedTypenames  = new ArrayList(); /* List<String> */
//	private Map  resources         = new HashMap(); /* String -> UnwovenClassFile */ 
	private Manifest manifest = null;
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
    


	public void addLibraryJarFile(File inFile) throws IOException  {
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


//	// The ANT copy task should be used to copy resources across.
//	private final static boolean CopyResourcesFromInpathDirectoriesToOutput=false;
	private Set alreadyConfirmedReweavableState;
	
	/**
	 * Add any .class files in the directory to the outdir.  Anything other than .class files in
	 * the directory (or its subdirectories) are considered resources and are also copied. 
	 *  
	 */
	public List addDirectoryContents(File inFile,File outDir) throws IOException {
		List addedClassFiles = new ArrayList();
		
		// Get a list of all files (i.e. everything that isnt a directory)
		File[] files = FileUtil.listFiles(inFile,new FileFilter() {
			public boolean accept(File f) {
				boolean accept = !f.isDirectory();
				return accept;
			}
		});
		
		// For each file, add it either as a real .class file or as a resource
		for (int i = 0; i < files.length; i++) {
			addedClassFiles.add(addClassFile(files[i],inFile,outDir));
		}
		
		return addedClassFiles;
	}


	/** Adds all class files in the jar
	 */
	public List addJarFile(File inFile, File outDir, boolean canBeDirectory){
//		System.err.println("? addJarFile(" + inFile + ", " + outDir + ")");
		List addedClassFiles = new ArrayList();
		needToReweaveWorld = true;
		JarFile inJar = null;
		
		try {
			// Is this a directory we are looking at?
			if (inFile.isDirectory() && canBeDirectory) {
				addedClassFiles.addAll(addDirectoryContents(inFile,outDir));
			} else {
			
				inJar = new JarFile(inFile);
				addManifest(inJar.getManifest());
				Enumeration entries = inJar.entries();
			
				while (entries.hasMoreElements()) {
					JarEntry entry = (JarEntry)entries.nextElement();
					InputStream inStream = inJar.getInputStream(entry);
					
					byte[] bytes = FileUtil.readAsByteArray(inStream);
					String filename = entry.getName();
//					System.out.println("? addJarFile() filename='" + filename + "'");
					UnwovenClassFile classFile = new UnwovenClassFile(new File(outDir, filename).getAbsolutePath(), bytes);

					if (filename.endsWith(".class")) {
						this.addClassFile(classFile);
						addedClassFiles.add(classFile);
					}
//					else if (!entry.isDirectory()) {
//
//						/* bug-44190 Copy meta-data */
//						addResource(filename,classFile);
//					}

					inStream.close();
				}
				inJar.close();
			}
		} catch (FileNotFoundException ex) {
			IMessage message = new Message(
					"Could not find input jar file " + inFile.getPath() + ", ignoring",
					new SourceLocation(inFile,0),
					false);
			world.getMessageHandler().handleMessage(message);
		} catch (IOException ex) {
			IMessage message = new Message(
					"Could not read input jar file " + inFile.getPath() + "(" + ex.getMessage() + ")",
					new SourceLocation(inFile,0),
					true);
			world.getMessageHandler().handleMessage(message);
		} finally {
			if (inJar != null) {
				try {inJar.close();}
				catch (IOException ex) {
					IMessage message = new Message(
							"Could not close input jar file " + inFile.getPath() + "(" + ex.getMessage() + ")",
							new SourceLocation(inFile,0),
							true);					
					world.getMessageHandler().handleMessage(message);					
				}
			}
		}
		
		return addedClassFiles;
	}

//	public void addResource(String name, File inPath, File outDir) throws IOException {
//
//		/* Eliminate CVS files. Relative paths use "/" */
//		if (!name.startsWith("CVS/") && (-1 == name.indexOf("/CVS/")) && !name.endsWith("/CVS")) {
////			System.err.println("? addResource('" + name + "')");
////			BufferedInputStream inStream = new BufferedInputStream(new FileInputStream(inPath));
////			byte[] bytes = new byte[(int)inPath.length()];
////			inStream.read(bytes);
////			inStream.close();
//			byte[] bytes = FileUtil.readAsByteArray(inPath);
//			UnwovenClassFile resourceFile = new UnwovenClassFile(new File(outDir, name).getAbsolutePath(), bytes);
//			addResource(name,resourceFile);
//		}
//	}

	public boolean needToReweaveWorld() {
		return needToReweaveWorld;
	}
	
    /** Should be addOrReplace
     */
    public void addClassFile(UnwovenClassFile classFile) {
    	addedClasses.add(classFile);
//    	if (null != sourceJavaClasses.put(classFile.getClassName(), classFile)) {
////    		throw new RuntimeException(classFile.getClassName());
//    	}
    	world.addSourceObjectType(classFile.getJavaClass());
    }
    
    public UnwovenClassFile addClassFile(File classFile, File inPathDir, File outDir) throws IOException {
		FileInputStream fis = new FileInputStream(classFile);
		byte[] bytes = FileUtil.readAsByteArray(fis);
		// String relativePath = files[i].getPath();
		
		// ASSERT: files[i].getAbsolutePath().startsWith(inFile.getAbsolutePath()
		// or we are in trouble...
		String filename = classFile.getAbsolutePath().substring(
		                    inPathDir.getAbsolutePath().length()+1);
		UnwovenClassFile ucf = new UnwovenClassFile(new File(outDir,filename).getAbsolutePath(),bytes);
		if (filename.endsWith(".class")) {
			// System.err.println("BCELWeaver: processing class from input directory "+classFile);
			this.addClassFile(ucf);
		}
		fis.close();
		return ucf;
    }


    public void deleteClassFile(String typename) {
    	deletedTypenames.add(typename);
//    	sourceJavaClasses.remove(typename);
    	world.deleteSourceObjectType(TypeX.forName(typename));
    }

//	public void addResource (String name, UnwovenClassFile resourceFile) {
//		/* bug-44190 Change error to warning and copy first resource */
//		if (!resources.containsKey(name)) {
//			resources.put(name, resourceFile);
//		}
//		else {
//			world.showMessage(IMessage.WARNING, "duplicate resource: '" + name + "'",
//				null, null);
//		}
//	}

	// ---- weave preparation

    public void prepareForWeave() {
    	needToReweaveWorld = false;

    	CflowPointcut.clearCaches();
    	
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
    
//    public void dumpUnwoven(File file) throws IOException {
//    	BufferedOutputStream os = FileUtil.makeOutputStream(file);
//    	this.zipOutputStream = new ZipOutputStream(os);
//    	dumpUnwoven();
//		/* BUG 40943*/
//		dumpResourcesToOutJar();
//    	zipOutputStream.close();  //this flushes and closes the acutal file
//    }
//    
//    
//    public void dumpUnwoven() throws IOException {
//    	Collection filesToDump = new HashSet(sourceJavaClasses.values());
//    	for (Iterator i = filesToDump.iterator(); i.hasNext(); ) {
//            UnwovenClassFile classFile = (UnwovenClassFile)i.next();
//            dumpUnchanged(classFile);
//       	}
//    }
    
//	public void dumpResourcesToOutPath() throws IOException {
////		System.err.println("? dumpResourcesToOutPath() resources=" + resources.keySet());
//		Iterator i = resources.keySet().iterator();
//		while (i.hasNext()) {
//			UnwovenClassFile res = (UnwovenClassFile)resources.get(i.next());
//			dumpUnchanged(res);
//		}
//		//resources = new HashMap();
//	}
//
	/* BUG #40943 */
//    public void dumpResourcesToOutJar() throws IOException {
////		System.err.println("? dumpResourcesToOutJar() resources=" + resources.keySet());
//		Iterator i = resources.keySet().iterator();
//		while (i.hasNext()) {
//			String name = (String)i.next();
//			UnwovenClassFile res = (UnwovenClassFile)resources.get(name);
//			writeZipEntry(name,res.getBytes());
//		}
//		resources = new HashMap();
//    }
//    
//    // halfway house for when the jar is managed outside of the weaver, but the resources
//    // to be copied are known in the weaver.
//    public void dumpResourcesToOutJar(ZipOutputStream zos) throws IOException {
//    	this.zipOutputStream = zos;
//    	dumpResourcesToOutJar();
//    }
    
	public void addManifest (Manifest newManifest) {
//		System.out.println("? addManifest() newManifest=" + newManifest);
		if (manifest == null) {
			manifest = newManifest;
		}
	}
	
	public static final String MANIFEST_NAME = "META-INF/MANIFEST.MF";
	
	private static final String WEAVER_MANIFEST_VERSION = "1.0";
	private static final Attributes.Name CREATED_BY = new Name("Created-By");
	private static final String WEAVER_CREATED_BY = "AspectJ Compiler";
    
    public Manifest getManifest (boolean shouldCreate) {
		
		if (manifest == null && shouldCreate) {
			manifest = new Manifest();

			Attributes attributes = manifest.getMainAttributes();
			attributes.put(Name.MANIFEST_VERSION,WEAVER_MANIFEST_VERSION);
			attributes.put(CREATED_BY,WEAVER_CREATED_BY);
		}
		
		return manifest;
    }
    
    // ---- weaving

    // Used by some test cases only...
    public Collection weave(File file) throws IOException {
    	OutputStream os = FileUtil.makeOutputStream(file);
    	this.zipOutputStream = new ZipOutputStream(os);
    	prepareForWeave();
    	Collection c = weave( new IClassFileProvider() {

			public Iterator getClassFileIterator() {
				return addedClasses.iterator();
			}

			public IWeaveRequestor getRequestor() {
				return new IWeaveRequestor() {
					public void acceptResult(UnwovenClassFile result) {
						try {
							writeZipEntry(result.filename, result.bytes);
						} catch(IOException ex) {}
					}
					public void processingReweavableState() {}
					public void addingTypeMungers() {}
					public void weavingAspects() {}
					public void weavingClasses() {}
					public void weaveCompleted() {}
				};
			}
		});
//    	/* BUG 40943*/
//    	dumpResourcesToOutJar();
    	zipOutputStream.close();  //this flushes and closes the acutal file
    	return c;
    }
    
//    public Collection weave() throws IOException {
//    	prepareForWeave();
//    	Collection filesToWeave;
//    	
//    	if (needToReweaveWorld) {
//    		filesToWeave = sourceJavaClasses.values();
//    	} else {
//    		filesToWeave = addedClasses;
//    	}
//    	
//    	Collection wovenClassNames = new ArrayList();
//    	world.showMessage(IMessage.INFO, "might need to weave " + filesToWeave + 
//    					"(world=" + needToReweaveWorld + ")", null, null);
//    	
//    	
//    	//System.err.println("typeMungers: " + typeMungerList);
//    	
//    	prepareToProcessReweavableState();
//		// clear all state from files we'll be reweaving
//        for (Iterator i = filesToWeave.iterator(); i.hasNext(); ) {
//            UnwovenClassFile classFile = (UnwovenClassFile)i.next();
//	    	String className = classFile.getClassName();
//            BcelObjectType classType = getClassType(className);			            
//			processReweavableStateIfPresent(className, classType);
//        }
//    	
//    	
//    	
//    	//XXX this isn't quite the right place for this...
//    	for (Iterator i = filesToWeave.iterator(); i.hasNext(); ) {
//            UnwovenClassFile classFile = (UnwovenClassFile)i.next();
//            String className = classFile.getClassName();
//            addTypeMungers(className);
//        }
//    	
//		// first weave into aspects
//        for (Iterator i = filesToWeave.iterator(); i.hasNext(); ) {
//            UnwovenClassFile classFile = (UnwovenClassFile)i.next();
//	    	String className = classFile.getClassName();
//            BcelObjectType classType = BcelWorld.getBcelObjectType(world.resolve(className));
//            if (classType.isAspect()) {
//	            weave(classFile, classType);
//	            wovenClassNames.add(className);
//            }
//        }
//
//		// then weave into non-aspects
//		for (Iterator i = filesToWeave.iterator(); i.hasNext(); ) {
//            UnwovenClassFile classFile = (UnwovenClassFile)i.next();
//	    	String className = classFile.getClassName();
//            BcelObjectType classType = BcelWorld.getBcelObjectType(world.resolve(className));
//            if (! classType.isAspect()) {
//	            weave(classFile, classType);
//	            wovenClassNames.add(className);
//            }
//        }
//        
//        if (zipOutputStream != null && !needToReweaveWorld) {
//        	Collection filesToDump = new HashSet(sourceJavaClasses.values());
//        	filesToDump.removeAll(filesToWeave);
//        	for (Iterator i = filesToDump.iterator(); i.hasNext(); ) {
//                UnwovenClassFile classFile = (UnwovenClassFile)i.next();
//                dumpUnchanged(classFile);
//        	}
//        }
//        
//        addedClasses = new ArrayList();
//    	deletedTypenames = new ArrayList();
//		
//        return wovenClassNames;
//    }
    
    // variation of "weave" that sources class files from an external source.
    public Collection weave(IClassFileProvider input) throws IOException {
    	Collection wovenClassNames = new ArrayList();
    	IWeaveRequestor requestor = input.getRequestor();

    	requestor.processingReweavableState();
		prepareToProcessReweavableState();
		// clear all state from files we'll be reweaving
		for (Iterator i = input.getClassFileIterator(); i.hasNext(); ) {
		    UnwovenClassFile classFile = (UnwovenClassFile)i.next();
			String className = classFile.getClassName();
		    BcelObjectType classType = getClassType(className);			            
			processReweavableStateIfPresent(className, classType);
		}
								
		requestor.addingTypeMungers();
		//XXX this isn't quite the right place for this...
		for (Iterator i = input.getClassFileIterator(); i.hasNext(); ) {
		    UnwovenClassFile classFile = (UnwovenClassFile)i.next();
		    String className = classFile.getClassName();
		    addTypeMungers(className);
		}

		requestor.weavingAspects();
		// first weave into aspects
		for (Iterator i = input.getClassFileIterator(); i.hasNext(); ) {
		    UnwovenClassFile classFile = (UnwovenClassFile)i.next();
			String className = classFile.getClassName();
		    BcelObjectType classType = BcelWorld.getBcelObjectType(world.resolve(className));
		    if (classType.isAspect()) {
		        weaveAndNotify(classFile, classType,requestor);
		        wovenClassNames.add(className);
		    }
		}

		requestor.weavingClasses();
		// then weave into non-aspects
		for (Iterator i = input.getClassFileIterator(); i.hasNext(); ) {
		    UnwovenClassFile classFile = (UnwovenClassFile)i.next();
			String className = classFile.getClassName();
		    BcelObjectType classType = BcelWorld.getBcelObjectType(world.resolve(className));
		    if (! classType.isAspect()) {
		        weaveAndNotify(classFile, classType, requestor);
		        wovenClassNames.add(className);
		    }
		}
		
		addedClasses = new ArrayList();
		deletedTypenames = new ArrayList();
		requestor.weaveCompleted();
		
    	return wovenClassNames;
    }
    
    public void prepareToProcessReweavableState() {
		if (inReweavableMode)
			world.showMessage(IMessage.INFO,
					WeaverMessages.format(WeaverMessages.REWEAVABLE_MODE),
					null, null);
    	 	    	
    	alreadyConfirmedReweavableState = new HashSet();
    }
    
    public void processReweavableStateIfPresent(String className, BcelObjectType classType) {
		// If the class is marked reweavable, check any aspects around when it was built are in this world
		WeaverStateInfo wsi = classType.getWeaverState();		
		if (wsi!=null && wsi.isReweavable()) { // Check all necessary types are around!
			world.showMessage(IMessage.INFO,
					WeaverMessages.format(WeaverMessages.PROCESSING_REWEAVABLE,className,classType.getSourceLocation().getSourceFile()),
					null,null);
			Set aspectsPreviouslyInWorld = wsi.getAspectsAffectingType();
			if (aspectsPreviouslyInWorld!=null) {
				for (Iterator iter = aspectsPreviouslyInWorld.iterator(); iter.hasNext();) {
					String requiredTypeName = (String) iter.next();
					if (!alreadyConfirmedReweavableState.contains(requiredTypeName)) {
						ResolvedTypeX rtx = world.resolve(TypeX.forName(requiredTypeName),true);
						boolean exists = rtx!=ResolvedTypeX.MISSING;
						if (!exists) {
							world.showMessage(IMessage.ERROR, 
									WeaverMessages.format(WeaverMessages.MISSING_REWEAVABLE_TYPE,requiredTypeName,className),
								    classType.getSourceLocation(), null);
						} else {
							if (!world.getMessageHandler().isIgnoring(IMessage.INFO))
							  world.showMessage(IMessage.INFO,
							  		WeaverMessages.format(WeaverMessages.VERIFIED_REWEAVABLE_TYPE,requiredTypeName,rtx.getSourceLocation().getSourceFile()),
									null,null);
							alreadyConfirmedReweavableState.add(requiredTypeName);
						}
					}		
				}
			}
			classType.setJavaClass(Utility.makeJavaClass(classType.getJavaClass().getFileName(), wsi.getUnwovenClassFileData()));
		} else {
			classType.resetState();
		}
	}

    private void weaveAndNotify(UnwovenClassFile classFile, BcelObjectType classType,
    		                    IWeaveRequestor requestor) throws IOException {
    	LazyClassGen clazz = weaveWithoutDump(classFile,classType);
    	classType.finishedWith();
		//clazz is null if the classfile was unchanged by weaving...
		if (clazz != null) {
			UnwovenClassFile[] newClasses = getClassFilesFor(clazz);
			for (int i = 0; i < newClasses.length; i++) {
				requestor.acceptResult(newClasses[i]);
			}
		} else {
			requestor.acceptResult(classFile);
		}
    }
    
	// helper method
    public BcelObjectType getClassType(String forClass) {
        return BcelWorld.getBcelObjectType(world.resolve(forClass));    	
    }
 
    
    public void addTypeMungers(String typeName) {
    	weave(world.resolve(typeName));
    }

    public UnwovenClassFile[] getClassFilesFor(LazyClassGen clazz) {
    	List childClasses = clazz.getChildClasses(world);
    	UnwovenClassFile[] ret = new UnwovenClassFile[1 + childClasses.size()];    	
    	ret[0] = new UnwovenClassFile(clazz.getFileName(),clazz.getJavaClass(world).getBytes());
    	int index = 1;
    	for (Iterator iter = childClasses.iterator(); iter.hasNext();) {
			UnwovenClassFile.ChildClass element = (UnwovenClassFile.ChildClass) iter.next();
			UnwovenClassFile childClass = new UnwovenClassFile(clazz.getFileName() + "$" + element.name, element.bytes);
			ret[index++] = childClass;
		}
    	return ret;
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
								WeaverMessages.format(WeaverMessages.DECP_BINARY_LIMITATION,onType.getName()),
								p.getSourceLocation(), null);
						continue;
					}
					
					classType.addParent(newParent);
					ResolvedTypeMunger newParentMunger = new NewParentTypeMunger(newParent);
					onType.addInterTypeMunger(new BcelTypeMunger(newParentMunger, xcutSet.findAspectDeclaringParents(p)));
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
				//XXXclazz.print(System.err);
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

		// here we do the coarsest grained fast match with no kind constraints
		// this will remove all obvious non-matches and see if we need to do any weaving
		FastMatchInfo info = new FastMatchInfo(type, null);

		List result = new ArrayList();
		Iterator iter = list.iterator();
		while (iter.hasNext()) {
			ShadowMunger munger = (ShadowMunger)iter.next();
			FuzzyBoolean fb = munger.getPointcut().fastMatch(info);
			WeaverMetrics.recordFastMatchTypeResult(fb); // Could pass: munger.getPointcut().toString(),info
			if (fb.maybeTrue()) {
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

	public void setReweavableMode(boolean mode,boolean compress) {
		inReweavableMode = mode;
		WeaverStateInfo.setReweavableModeDefaults(mode,compress);
		BcelClassWeaver.setReweavableMode(mode,compress);
	}

	public boolean isReweavable() {
		return inReweavableMode;
	}
}
