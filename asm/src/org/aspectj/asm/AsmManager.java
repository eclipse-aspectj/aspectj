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
    	List relationships = new ArrayList();
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
}

