/* *******************************************************************
 * Copyright (c) 2003 Contributors.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     Mik Kersten     initial implementation 
 * ******************************************************************/


package org.aspectj.asm;

import java.io.*;
import java.util.*;

import org.aspectj.asm.internal.*;
import org.aspectj.bridge.ISourceLocation;

/**
 * The Abstract Structure Model (ASM) represents the containment hierarchy and crossccutting
 * structure map for AspectJ programs.  It is used by IDE views such as the document outline,
 * and by other tools such as ajdoc to show both AspectJ declarations and crosscutting links,
 * such as which advice affects which join point shadows.
 * 
 * @author Mik Kersten
 */
public class AsmManager {
	
	/**
	 * @deprecated	use getDefault() method instead
	 */  
	private static AsmManager INSTANCE = new AsmManager();
	private boolean shouldSaveModel = true;
    protected IHierarchy hierarchy;
    private List structureListeners = new ArrayList();
	private IRelationshipMap mapper;

    protected AsmManager() {
    	hierarchy = new AspectJElementHierarchy();
//    	List relationships = new ArrayList();
		mapper = new RelationshipMap(hierarchy);
    }

    public IHierarchy getHierarchy() {
        return hierarchy;	
	}

	public static AsmManager getDefault() {
		return INSTANCE;
	}
	
	public IRelationshipMap getRelationshipMap() {
		return mapper;
	}

	public void fireModelUpdated() {
		notifyListeners();	
		if (hierarchy.getConfigFile() != null) {
			writeStructureModel(hierarchy.getConfigFile());
		}
	}

    /**
     * Constructs map each time it's called.
     */
    public HashMap getInlineAnnotations(
    	String sourceFile, 
    	boolean showSubMember, 
    	boolean showMemberAndType) { 

        if (!hierarchy.isValid()) return null;
		
        HashMap annotations = new HashMap();
        IProgramElement node = hierarchy.findElementForSourceFile(sourceFile);
        if (node == IHierarchy.NO_STRUCTURE) {
            return null;
        } else {
            IProgramElement fileNode = (IProgramElement)node;
            ArrayList peNodes = new ArrayList();
            getAllStructureChildren(fileNode, peNodes, showSubMember, showMemberAndType);
            for (Iterator it = peNodes.iterator(); it.hasNext(); ) {
                IProgramElement peNode = (IProgramElement)it.next();
                List entries = new ArrayList();
                entries.add(peNode);
                ISourceLocation sourceLoc = peNode.getSourceLocation();
                if (null != sourceLoc) {
                    Integer hash = new Integer(sourceLoc.getLine());
                    List existingEntry = (List)annotations.get(hash);
                    if (existingEntry != null) {
                        entries.addAll(existingEntry);
                    }
                    annotations.put(hash, entries);
                }
            }
            return annotations;
        }
    }

    private void getAllStructureChildren(IProgramElement node, List result, boolean showSubMember, boolean showMemberAndType) {
        List children = node.getChildren();
        if (node.getChildren() == null) return;
        for (Iterator it = children.iterator(); it.hasNext(); ) {
			IProgramElement next = (IProgramElement)it.next();
            List rels = AsmManager.getDefault().getRelationshipMap().get(next);
            if (next != null
            	&& ((next.getKind() == IProgramElement.Kind.CODE && showSubMember) 
            	|| (next.getKind() != IProgramElement.Kind.CODE && showMemberAndType))
            	&& rels != null 
            	&& rels.size() > 0) {
                result.add(next);
            }
            getAllStructureChildren((IProgramElement)next, result, showSubMember, showMemberAndType);
        }
    }

    public void addListener(IHierarchyListener listener) {
        structureListeners.add(listener);
    }

    public void removeStructureListener(IHierarchyListener listener) {
        structureListeners.remove(listener);
    }

	// this shouldn't be needed - but none of the people that add listeners
	// in the test suite ever remove them. AMC added this to be called in
	// setup() so that the test cases would cease leaking listeners and go 
	// back to executing at a reasonable speed.
	public void removeAllListeners() {
		structureListeners.clear();
	}

    private void notifyListeners() {
        for (Iterator it = structureListeners.iterator(); it.hasNext(); ) {
            ((IHierarchyListener)it.next()).elementsUpdated(hierarchy);
        }
    }

	/**
	 * Fails silently.
	 */
    public void writeStructureModel(String configFilePath) {
        try {
            String filePath = genExternFilePath(configFilePath);
            ObjectOutputStream s = new ObjectOutputStream(new FileOutputStream(filePath));
            s.writeObject(hierarchy);
            s.flush();
        } catch (Exception e) {
            // ignore
        }
    }
  
  	/**
  	 * @todo	add proper handling of bad paths/suffixes/etc
  	 * @param	configFilePath		path to an ".lst" file
  	 */
    public void readStructureModel(String configFilePath) {
        try {
            if (configFilePath == null) {
            	hierarchy.setRoot(IHierarchy.NO_STRUCTURE);
            } else {
	            String filePath = genExternFilePath(configFilePath);
	            FileInputStream in = new FileInputStream(filePath);
	            ObjectInputStream s = new ObjectInputStream(in);
	            hierarchy = (AspectJElementHierarchy)s.readObject();
            }
        } catch (Exception e) {
        	//System.err.println("AJDE Message: could not read structure model: " + e);
            hierarchy.setRoot(IHierarchy.NO_STRUCTURE);
        } finally {
        	notifyListeners();	
        }
    }

    private String genExternFilePath(String configFilePath) {
        return configFilePath.substring(0, configFilePath.lastIndexOf(".lst")) + ".ajsym";
    }
    
	public void setShouldSaveModel(boolean shouldSaveModel) {
		this.shouldSaveModel = shouldSaveModel;
	}
	
	// ==== implementation of canonical file path map and accessors ==============

	// a more sophisticated optimisation is left here commented out as the 
	// performance gains don't justify the disturbance this close to a release...	
	// can't call prepareForWeave until preparedForCompilation has completed...
//	public synchronized void prepareForCompilation(List files) {
//		canonicalFilePathMap.prepopulate(files);
//	}
//	
//	public synchronized void prepareForWeave() {
//		canonicalFilePathMap.handover();
//	}
	
	public String getCanonicalFilePath(File f) {
		return canonicalFilePathMap.get(f);
	}
	
	private CanonicalFilePathMap canonicalFilePathMap = new CanonicalFilePathMap();
	
	private static class CanonicalFilePathMap {
		private static final int MAX_SIZE = 4000;
		
		private Map pathMap = new HashMap(MAX_SIZE);

//		// guards to ensure correctness and liveness
//		private boolean cacheInUse = false;
//		private boolean stopRequested = false;
//		
//		private synchronized boolean isCacheInUse() {
//			return cacheInUse;
//		}
//		
//		private synchronized void setCacheInUse(boolean val) {
//			cacheInUse = val;
//			if (val) {
//				notifyAll();
//			} 
//		}
//		
//		private synchronized boolean isStopRequested() {
//			return stopRequested;
//		}
//		
//		private synchronized void requestStop() {
//			stopRequested = true;
//		}
//		
//		/**
//		 * Begin prepopulating the map by adding an entry from
//		 * file.getPath -> file.getCanonicalPath for each file in
//		 * the list. Do this on a background thread.
//		 * @param files
//		 */
//		public void prepopulate(final List files) {
//			    stopRequested = false;
//				setCacheInUse(false);
//			    if (pathMap.size() > MAX_SIZE) {
//			    	pathMap.clear();
//			    }
//				new Thread() {
//					public void run() {
//						System.out.println("Starting cache population: " + System.currentTimeMillis());
//						Iterator it = files.iterator();
//						while (!isStopRequested() && it.hasNext()) {
//							File f = (File)it.next();
//							if (pathMap.get(f.getPath()) == null) {
//								// may reuse cache across compiles from ides... 
//								try {								
//									pathMap.put(f.getPath(),f.getCanonicalPath());
//								} catch (IOException ex) {
//									pathMap.put(f.getPath(),f.getPath());
//								}
//							}
//						}
//						System.out.println("Cached " + files.size());
//						setCacheInUse(true);
//						System.out.println("Cache populated: " + System.currentTimeMillis());
//					}
//				}.start();
//		}
//		
//		/**
//		 * Stop pre-populating the cache - our customers are ready to use it.
//		 * If there are any cache misses from this point on, we'll populate the
//		 * cache as we go.
//		 * The handover is done this way to ensure that only one thread is ever
//		 * accessing the cache, and that we minimize synchronization.
//		 */
//		public synchronized void handover() {
//			if (!isCacheInUse()) {
//				requestStop();
//				try {
//					while (!isCacheInUse())	wait();
//				} catch (InterruptedException intEx) { } // just continue
//			}
//		}
		
		public String get(File f) {
//			if (!cacheInUse) {  // unsynchronized test - should never be parallel 
//				                // threads at this point
//				throw new IllegalStateException(
//					"Must take ownership of cache before using by calling " +
//					"handover()");
//			}
			String ret = (String) pathMap.get(f.getPath());
			if (ret == null) {
				try {
					ret = f.getCanonicalPath();
				} catch (IOException ioEx) {
					ret = f.getPath();
				}
				pathMap.put(f.getPath(),ret);
				if (pathMap.size() > MAX_SIZE) pathMap.clear();
			}
			return ret;
		}
	};
	
}

