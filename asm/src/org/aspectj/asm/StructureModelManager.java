/* *******************************************************************
 * Copyright (c) 1999-2001 Xerox Corporation, 
 *               2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     Xerox/PARC     initial implementation 
 * ******************************************************************/


package org.aspectj.asm;

import java.util.*;
import java.io.*;
import org.aspectj.asm.*;

/**
 * @author Mik Kersten
 */
public class StructureModelManager {
	
	/**
	 * Singleton instance.
	 */
	public static StructureModelManager INSTANCE = new StructureModelManager();
	private boolean shouldSaveModel = true;
    protected StructureModel model = new StructureModel();
    private List structureListeners = new ArrayList();
    private List associations = new ArrayList();

    protected StructureModelManager() {
        associations.add(new AdviceAssociation());
        associations.add(new IntroductionAssociation());
        associations.add(new InheritanceAssociation());
        associations.add(new ReferenceAssociation());
    }

    public StructureModel getStructureModel() {
        return model;	
	}

	public void fireModelUpdated() {
		notifyListeners();	
		if (model.getConfigFile() != null) {
			writeStructureModel(model.getConfigFile());
		}
	}

    /**
     * Constructs map each time it's called.
     */
    public HashMap getInlineAnnotations(
    	String sourceFile, 
    	boolean showSubMember, 
    	boolean showMemberAndType) { 
        
        if (!model.isValid()) return null;
		
        HashMap annotations = new HashMap();
        StructureNode node = model.findRootNodeForSourceFile(sourceFile);
        if (node == StructureModel.NO_STRUCTURE) {
            return null;
        } else {
            ProgramElementNode fileNode = (ProgramElementNode)node;
            ArrayList peNodes = new ArrayList();
            getAllStructureChildren(fileNode, peNodes, showSubMember, showMemberAndType);
            for (Iterator it = peNodes.iterator(); it.hasNext(); ) {
                ProgramElementNode peNode = (ProgramElementNode)it.next();
                List entries = new ArrayList();
                entries.add(peNode);
                Integer hash = new Integer(peNode.getSourceLocation().getLine());
                List existingEntry = (List)annotations.get(hash);
                if (existingEntry != null) {
                    entries.addAll(existingEntry);
                }
                annotations.put(hash, entries);
            }
            return annotations;
        }
    }

    private void getAllStructureChildren(ProgramElementNode node, List result, boolean showSubMember, boolean showMemberAndType) {
        List children = node.getChildren();
        for (Iterator it = children.iterator(); it.hasNext(); ) {
            StructureNode next = (StructureNode)it.next();
            if (next instanceof ProgramElementNode) {
                ProgramElementNode pNode = (ProgramElementNode)next;
                if (pNode != null
                	&& ((pNode.isCode() && showSubMember) || (!pNode.isCode() && showMemberAndType))
                	&& pNode.getRelations() != null 
                	&& pNode.getRelations().size() > 0) {
                    result.add(next);
                }
                getAllStructureChildren((ProgramElementNode)next, result, showSubMember, showMemberAndType);
            }
        }
    }

    public void addListener(StructureModelListener listener) {
        structureListeners.add(listener);
    }

    public void removeStructureListener(StructureModelListener listener) {
        structureListeners.remove(listener);
    }

    private void notifyListeners() {
        for (Iterator it = structureListeners.iterator(); it.hasNext(); ) {
            ((StructureModelListener)it.next()).modelUpdated(model);
        }
    }

    public List getAssociations() {
        return associations;
    }

	/**
	 * Fails silently.
	 */
    public void writeStructureModel(String configFilePath) {
        try {
            String filePath = genExternFilePath(configFilePath);
            ObjectOutputStream s = new ObjectOutputStream(new FileOutputStream(filePath));
            s.writeObject(model);
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
            	model.setRoot(StructureModel.NO_STRUCTURE);
            } else {
	            String filePath = genExternFilePath(configFilePath);
	            FileInputStream in = new FileInputStream(filePath);
	            ObjectInputStream s = new ObjectInputStream(in);
	            model = (StructureModel)s.readObject();
            }
        } catch (Exception e) {
        	//System.err.println("AJDE Message: could not read structure model: " + e);
            model.setRoot(StructureModel.NO_STRUCTURE);
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

