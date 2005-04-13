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
 *    Andy Clement     incremental support and switch on/off state
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
	private static boolean creatingModel = false;
	

	public static boolean dumpModelPostBuild = false; // Dumping the model is expensive
	public static boolean attemptIncrementalModelRepairs = false;
//	for debugging ...	
	


    // For offline debugging, you can now ask for the AsmManager to
    // dump the model - see the method setReporting()
	private static boolean dumpModel = false;
	private static boolean dumpRelationships = false;
	private static boolean dumpDeltaProcessing = false;
	private static String  dumpFilename = "";
	private static boolean reporting = false;

//	static {
//		setReporting("c:/model.nfo",true,true,true,true);
//	}
	
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
		if (dumpModelPostBuild && hierarchy.getConfigFile() != null) {
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
            FileOutputStream fos = new FileOutputStream(filePath);
            ObjectOutputStream s = new ObjectOutputStream(fos);
            s.writeObject(hierarchy); // Store the program element tree
            s.writeObject(mapper); // Store the relationships
            s.flush();
            fos.flush();
            fos.close();
            s.close();
        } catch (Exception e) {
            // System.err.println("AsmManager: Unable to write structure model: "+configFilePath+" because of:");
            // e.printStackTrace();
        }
    }
  
  	/**
  	 * @todo	add proper handling of bad paths/suffixes/etc
  	 * @param	configFilePath		path to an ".lst" file
  	 */
    public void readStructureModel(String configFilePath) {
    	boolean hierarchyReadOK = false;
        try {
            if (configFilePath == null) {
            	hierarchy.setRoot(IHierarchy.NO_STRUCTURE);
            } else {
	            String filePath = genExternFilePath(configFilePath);
	            FileInputStream in = new FileInputStream(filePath);
	            ObjectInputStream s = new ObjectInputStream(in);
	            hierarchy = (AspectJElementHierarchy)s.readObject();
	            hierarchyReadOK = true;
	            mapper = (RelationshipMap)s.readObject();
	            ((RelationshipMap)mapper).setHierarchy(hierarchy);
            }
        } catch (FileNotFoundException fnfe) {
        	// That is OK
			hierarchy.setRoot(IHierarchy.NO_STRUCTURE);
		} catch (EOFException eofe) {
			// Might be an old format sym file that is missing its relationships
			if (!hierarchyReadOK) {
				System.err.println("AsmManager: Unable to read structure model: "+configFilePath+" because of:");
				eofe.printStackTrace();
				hierarchy.setRoot(IHierarchy.NO_STRUCTURE);
			}
        } catch (Exception e) {
            // System.err.println("AsmManager: Unable to read structure model: "+configFilePath+" because of:");
            // e.printStackTrace();
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
	}

	public static void setReporting(String filename,boolean dModel,boolean dRels,boolean dDeltaProcessing,boolean deletefile) {
		reporting         = true;
		dumpModel         = dModel;
		dumpRelationships = dRels;
		dumpDeltaProcessing = dDeltaProcessing;
		if (deletefile) new File(filename).delete();
		dumpFilename      = filename;
	}
	
	public static boolean isReporting() {
		return reporting;
	}
	


	public void reportModelInfo(String reasonForReport) {
		if (!dumpModel && !dumpRelationships) return;
		try {
			FileWriter fw = new FileWriter(dumpFilename,true);
			BufferedWriter bw = new BufferedWriter(fw);
			if (dumpModel) {
				bw.write("=== MODEL STATUS REPORT ========= "+reasonForReport+"\n");
				dumptree(bw,AsmManager.getDefault().getHierarchy().getRoot(),0);
				
				bw.write("=== END OF MODEL REPORT =========\n");
			}
			if (dumpRelationships) {
				bw.write("=== RELATIONSHIPS REPORT ========= "+reasonForReport+"\n");
				dumprels(bw);
				bw.write("=== END OF RELATIONSHIPS REPORT ==\n");
			}
			Properties p = ModelInfo.summarizeModel().getProperties();
			Enumeration pkeyenum = p.keys();
			bw.write("=== Properties of the model and relationships map =====\n");
			while (pkeyenum.hasMoreElements()) {
				String pkey = (String)pkeyenum.nextElement();
				bw.write(pkey+"="+p.getProperty(pkey)+"\n");
			}
			bw.flush();
			fw.close();
		} catch (IOException e) {
			System.err.println("InternalError: Unable to report model information:");
			e.printStackTrace();
		}
	}
	

	public static void dumptree(Writer w,IProgramElement node,int indent) throws IOException {
		for (int i =0 ;i<indent;i++) w.write(" ");
		String loc = "";
		if (node!=null) { 
			if (node.getSourceLocation()!=null) 
				loc = node.getSourceLocation().toString();
		}
		w.write(node+"  ["+(node==null?"null":node.getKind().toString())+"] "+loc+"\n");
		if (node!=null) 
		for (Iterator i = node.getChildren().iterator();i.hasNext();) {
			dumptree(w,(IProgramElement)i.next(),indent+2);
		}
	}
	
	private void dumprels(Writer w) throws IOException {
		IRelationshipMap irm = AsmManager.getDefault().getRelationshipMap();
		int ctr = 1;
		Set entries = irm.getEntries();
		for (Iterator iter = entries.iterator(); iter.hasNext();) {
			String hid = (String) iter.next();
			List rels =  irm.get(hid);
			for (Iterator iterator = rels.iterator(); iterator.hasNext();) {
				IRelationship ir = (IRelationship) iterator.next();
				List targets = ir.getTargets();
				for (Iterator iterator2 = targets.iterator();
					iterator2.hasNext();
					) {
					String thid = (String) iterator2.next();
					w.write("Hid:"+(ctr++)+":(targets="+targets.size()+") "+hid+" ("+ir.getName()+") "+thid+"\n");
				}
			}
		}
	}
	
	//===================== DELTA PROCESSING CODE ============== start ==========//
	
	// XXX shouldn't be aware of the delimiter
	private String getFilename(String hid) {
		return hid.substring(0,hid.indexOf("|"));
	}
	
	// This code is *SLOW* but it isnt worth fixing until we address the
	// bugs in binary weaving.
	public void fixupStructureModel(Writer fw,List filesToBeCompiled,Set files_added,Set files_deleted) throws IOException {
		// Three kinds of things to worry about:
		// 1. New files have been added since the last compile
		// 2. Files have been deleted since the last compile
		// 3. Files have 'changed' since the last compile (really just those in config.getFiles())
		
		//  List files = config.getFiles();
		IHierarchy model = AsmManager.getDefault().getHierarchy();
		
		boolean modelModified = false;
		// Files to delete are: those to be compiled + those that have been deleted
		
		Set filesToRemoveFromStructureModel = new HashSet(filesToBeCompiled);
		filesToRemoveFromStructureModel.addAll(files_deleted);
		Set deletedNodes = new HashSet();
		for (Iterator iter = filesToRemoveFromStructureModel.iterator(); iter.hasNext();) {
			File fileForCompilation = (File) iter.next();
			String correctedPath = AsmManager.getDefault().getCanonicalFilePath(fileForCompilation);
			IProgramElement progElem = (IProgramElement)model.findInFileMap(correctedPath);
			if (progElem!=null) {
				// Found it, let's remove it
				if (dumpDeltaProcessing) {
					fw.write("Deleting "+progElem+" node for file "+fileForCompilation+"\n");
				}
				removeNode(progElem);
				deletedNodes.add(getFilename(progElem.getHandleIdentifier()));
				if (!model.removeFromFileMap(correctedPath.toString())) 
						throw new RuntimeException("Whilst repairing model, couldn't remove entry for file: "+correctedPath.toString()+" from the filemap");
				modelModified = true;
			} 
		}
		if (modelModified) {
			model.flushTypeMap();
			model.updateHandleMap(deletedNodes);
		}
	}
	
	public void processDelta(List filesToBeCompiled,Set files_added,Set files_deleted) {

		try {
			Writer fw = null;
			
			// Are we recording this ?
			if (dumpDeltaProcessing) {
				FileWriter filew = new FileWriter(dumpFilename,true);
				fw = new BufferedWriter(filew);
				fw.write("=== Processing delta changes for the model ===\n");
				fw.write("Files for compilation:#"+filesToBeCompiled.size()+":"+filesToBeCompiled+"\n");
				fw.write("Files added          :#"+files_added.size()+":"+files_added+"\n");
				fw.write("Files deleted        :#"+files_deleted.size()+":"+files_deleted+"\n");
			}
			
			long stime = System.currentTimeMillis();
			
			fixupStructureModel(fw,filesToBeCompiled,files_added,files_deleted);
			
			long etime1 = System.currentTimeMillis(); // etime1-stime = time to fix up the model
		
			IHierarchy model = AsmManager.getDefault().getHierarchy();
			
			// Some of this code relies on the fact that relationships are always in pairs, so you know
			// if you are the target of a relationship because you are also the source of a relationship
			// This seems a valid assumption for now...
		
			//TODO Speed this code up by making this assumption:
			// the only piece of the handle that is interesting is the file name.  We are working at file granularity, if the
			// file does not exist (i.e. its not in the filemap) then any handle inside that file cannot exist.
			if (dumpDeltaProcessing) fw.write("Repairing relationships map:\n");
		
			// Now sort out the relationships map
			IRelationshipMap irm = AsmManager.getDefault().getRelationshipMap();
			Set sourcesToRemove = new HashSet(); 
			Set nonExistingHandles = new HashSet(); // Cache of handles that we *know* are invalid
			int srchandlecounter = 0;
			int tgthandlecounter = 0;
			
			// Iterate over the source handles in the relationships map
			Set keyset = irm.getEntries(); // These are source handles
			for (Iterator keyiter = keyset.iterator(); keyiter.hasNext();) {
				String hid = (String) keyiter.next();
				srchandlecounter++;
				
				// Do we already know this handle points to nowhere?
				if (nonExistingHandles.contains(hid)) {
					sourcesToRemove.add(hid);
				} else {
			  		// We better check if it actually exists
			  		IProgramElement existingElement = model.getElement(hid);
			  		if (dumpDeltaProcessing) fw.write("Looking for handle ["+hid+"] in model, found: "+existingElement+"\n");
			  
			 		// Did we find it?
			  		if (existingElement == null) {
						// No, so delete this relationship
						sourcesToRemove.add(hid);
						nonExistingHandles.add(hid); // Speed up a bit you swine
			  		} else {
			  			// Ok, so the source is valid, what about the targets?
						List relationships = irm.get(hid);
						List relationshipsToRemove = new ArrayList();
						// Iterate through the relationships against this source handle
						for (Iterator reliter = relationships.iterator();reliter.hasNext();) {
							IRelationship rel = (IRelationship) reliter.next();
							List targets = rel.getTargets();
							List targetsToRemove = new ArrayList();
					
							// Iterate through the targets for this relationship
							for (Iterator targetIter = targets.iterator();targetIter.hasNext();) {
								String targethid = (String) targetIter.next();
								tgthandlecounter++;
								// Do we already know it doesn't exist?
								if (nonExistingHandles.contains(targethid)) {
									if (dumpDeltaProcessing) fw.write("Target handle ["+targethid+"] for srchid["+hid+"]rel["+rel.getName()+"] does not exist\n");
									targetsToRemove.add(targethid);
								} else {
									// We better check
									IProgramElement existingTarget = model.getElement(targethid);
									if (existingTarget == null) {
										if (dumpDeltaProcessing) fw.write("Target handle ["+targethid+"] for srchid["+hid+"]rel["+rel.getName()+"] does not exist\n");
										targetsToRemove.add(targethid);
										nonExistingHandles.add(targethid);
									}
								}
							}
							
							// Do we have some targets that need removing?
							if (targetsToRemove.size()!=0) {
								// Are we removing *all* of the targets for this relationship (i.e. removing the relationship)
								if (targetsToRemove.size()==targets.size()) {
									if (dumpDeltaProcessing) fw.write("No targets remain for srchid["+hid+"] rel["+rel.getName()+"]: removing it\n");
									relationshipsToRemove.add(rel);							
								} else {
									// Remove all the targets that are no longer valid
						  			for (Iterator targsIter = targetsToRemove.iterator();targsIter.hasNext();) {
							  			String togo = (String) targsIter.next();
							  			targets.remove(togo);
						  			}
						  			// Should have already been caught above, but lets double check ...
						  			if (targets.size()==0) {
										if (dumpDeltaProcessing) fw.write("No targets remain for srchid["+hid+"] rel["+rel.getName()+"]: removing it\n");
						  				relationshipsToRemove.add(rel); // TODO Should only remove this relationship for the srchid?
						  			}
								}
							}
						}
						// Now, were any relationships emptied during that processing and so need removing for this source handle
						if (relationshipsToRemove.size()>0) {
							// Are we removing *all* of the relationships for this source handle?
							if (relationshipsToRemove.size() == relationships.size()) { 
								// We know they are all going to go, so just delete the source handle.
								sourcesToRemove.add(hid);
							} else {
								for (int i = 0 ;i<relationshipsToRemove.size();i++) {
									IRelationship irel = (IRelationship)relationshipsToRemove.get(i);
									verifyAssumption(irm.remove(hid,irel),"Failed to remove relationship "+irel.getName()+" for shid "+hid);
								}
								List rels = irm.get(hid);
								if (rels==null || rels.size()==0) sourcesToRemove.add(hid);
							}
						}
					}
				}
			}
			// Remove sources that have no valid relationships any more
			for (Iterator srciter = sourcesToRemove.iterator(); srciter.hasNext();) {
				String hid = (String) srciter.next();
				irm.removeAll(hid);
				IProgramElement ipe = model.getElement(hid);
				if (ipe!=null) {
					// If the relationship was hanging off a 'code' node, delete it.
					if (ipe.getKind().equals(IProgramElement.Kind.CODE)) {
						removeSingleNode(ipe);
					} 
				}
			}
			long etime2 = System.currentTimeMillis(); // etime2-stime = time to repair the relationship map
			if (dumpDeltaProcessing) {
				fw.write("===== Delta Processing timing ==========\n");
				fw.write("Hierarchy="+(etime1-stime)+"ms   Relationshipmap="+(etime2-etime1)+"ms\n");
				fw.write("===== Traversal ========================\n");
				fw.write("Source handles processed="+srchandlecounter+"\n");
				fw.write("Target handles processed="+tgthandlecounter+"\n");
				fw.write("========================================\n");
				fw.flush();fw.close();
			}
			reportModelInfo("After delta processing");
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
    
	/**
	 * Removes a specified program element from the structure model.
	 * We go to the parent of the program element, ask for all its children
	 * and remove the node we want to delete from the list of children.
	 */
	private void removeSingleNode(IProgramElement progElem) {
		verifyAssumption(progElem!=null);
		boolean deleteOK = false;
		IProgramElement parent = progElem.getParent();
		List kids = parent.getChildren();
		for (int i =0 ;i<kids.size();i++) {
		  if (kids.get(i).equals(progElem)) {
			  kids.remove(i); 
			  deleteOK=true;
			  break;
		  }
		}
		verifyAssumption(deleteOK);
	}
	
	
	/**
	 * Removes a specified program element from the structure model.
	 * Two processing stages:
	 * <p>First: We go to the parent of the program element, ask for all its children
	 *    and remove the node we want to delete from the list of children.
	 * <p>Second:We check if that parent has any other children.  If it has no other
	 *    children and it is either a CODE node or a PACKAGE node, we delete it too.
	 */
	private void removeNode(IProgramElement progElem) {
		
//		StringBuffer flightrecorder = new StringBuffer();
		try {
//			flightrecorder.append("In removeNode, about to chuck away: "+progElem+"\n");
		
			verifyAssumption(progElem!=null);
			boolean deleteOK = false;
			IProgramElement parent = progElem.getParent();
//			flightrecorder.append("Parent of it is "+parent+"\n");
			List kids = parent.getChildren();
//			flightrecorder.append("Which has "+kids.size()+" kids\n");
			for (int i =0 ;i<kids.size();i++) {
//				flightrecorder.append("Comparing with "+kids.get(i)+"\n");
		  		if (kids.get(i).equals(progElem)) {
			 		kids.remove(i); 
//			  		flightrecorder.append("Removing it\n");
			  		deleteOK=true;
			  		break;
		  		}
			}
//			verifyAssumption(deleteOK,flightrecorder.toString());
			// Are there any kids left for this node?
			if (parent.getChildren().size()==0  && parent.getParent()!=null && 
				(parent.getKind().equals(IProgramElement.Kind.CODE) ||
				parent.getKind().equals(IProgramElement.Kind.PACKAGE))) {
				// This node is on its own, we should trim it too *as long as its not a structural node* which we currently check by making sure its a code node
				// We should trim if it
				// System.err.println("Deleting parent:"+parent);
				removeNode(parent);
			}
		} catch (NullPointerException npe ){
			// Occurred when commenting out other 2 ras classes in wsif?? reproducable?
//			System.err.println(flightrecorder.toString());
			npe.printStackTrace();
		}
	}
	
	
	public static void verifyAssumption(boolean b,String info) {
		if (!b) {
			System.err.println("=========== ASSERTION IS NOT TRUE =========v");
			System.err.println(info);
			Thread.dumpStack();
			System.err.println("=========== ASSERTION IS NOT TRUE =========^");		
			throw new RuntimeException("Assertion is false");
		} 
	}
	
	public static void verifyAssumption(boolean b) {
		if (!b) {
			Thread.dumpStack();
			throw new RuntimeException("Assertion is false");
		} 
	}
	

	//===================== DELTA PROCESSING CODE ==============  end  ==========//
	
	/**
	 * A ModelInfo object captures basic information about the structure model.
	 * It is used for testing and producing debug info.
	 */
	public static class ModelInfo {
		private Hashtable nodeTypeCount = new Hashtable();
		private Properties extraProperties = new Properties();
		
		private ModelInfo(IHierarchy hierarchy,IRelationshipMap relationshipMap) {
		  IProgramElement ipe = hierarchy.getRoot();
		  walkModel(ipe);
		  recordStat("FileMapSize",
			new Integer(hierarchy.getFileMapEntrySet().size()).toString());
		  recordStat("RelationshipMapSize",
			new Integer(relationshipMap.getEntries().size()).toString());	
		}
		
		private void walkModel(IProgramElement ipe) {
			countNode(ipe);
			List kids = ipe.getChildren();
			for (Iterator iter = kids.iterator(); iter.hasNext();) {
				IProgramElement nextElement = (IProgramElement) iter.next();
				walkModel(nextElement);
			}
		}
		
		private void countNode(IProgramElement ipe) {
			String node = ipe.getKind().toString();
			Integer ctr = (Integer)nodeTypeCount.get(node);
			if (ctr==null) {
				nodeTypeCount.put(node,new Integer(1));
			} else {
				ctr = new Integer(ctr.intValue()+1);
				nodeTypeCount.put(node,ctr);
			}
		}
		
		public String toString() {
			StringBuffer sb = new StringBuffer();
			sb.append("Model node summary:\n");
			Enumeration nodeKeys = nodeTypeCount.keys();
			while (nodeKeys.hasMoreElements()) {
				String key = (String)nodeKeys.nextElement();
				Integer ct = (Integer)nodeTypeCount.get(key);
				sb.append(key+"="+ct+"\n");
			}
			sb.append("Model stats:\n");
			Enumeration ks = extraProperties.keys();
			while (ks.hasMoreElements()) {
				String k = (String)ks.nextElement();
				String v = extraProperties.getProperty(k);
				sb.append(k+"="+v+"\n");
			}
			return sb.toString();
		}
		
		public Properties getProperties() {
			Properties p = new Properties();
			Enumeration nodeKeys = nodeTypeCount.keys();
			while (nodeKeys.hasMoreElements()) {
				String key = (String)nodeKeys.nextElement();
				Integer ct = (Integer)nodeTypeCount.get(key);
				p.setProperty(key,ct.toString());
			}
			p.putAll(extraProperties);
			return p;
		}

		public void recordStat(String string, String string2) {
			extraProperties.setProperty(string,string2);
		}
		
		public static ModelInfo summarizeModel() {
			return new ModelInfo(AsmManager.getDefault().getHierarchy(),
								 AsmManager.getDefault().getRelationshipMap());
		}
	}

    /**
     * Set to indicate whether we are currently building a structure model, should
     * be set up front.
     */
	public static void setCreatingModel(boolean b) {
		creatingModel = b;
	}
	
    /**
     * returns true if we are currently generating a structure model, enables
     * guarding of expensive operations on an empty/null model.
     */
	public static boolean isCreatingModel() { return creatingModel;}
	
	
}

