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

 
package org.aspectj.ajde.ui;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.aspectj.ajde.Ajde;
import org.aspectj.ajde.ui.internal.NavigationHistoryModel;
import org.aspectj.ajde.ui.internal.TreeStructureViewBuilder;
import org.aspectj.asm.AdviceAssociation;
import org.aspectj.asm.InheritanceAssociation;
import org.aspectj.asm.IntroductionAssociation;
import org.aspectj.asm.LinkNode;
import org.aspectj.asm.ProgramElementNode;
import org.aspectj.asm.ReferenceAssociation;
import org.aspectj.asm.StructureModel;
import org.aspectj.asm.StructureModelListener;
import org.aspectj.asm.StructureModelManager;
import org.aspectj.asm.StructureNode;

/**
 * @author	Mik Kersten
 */
public class StructureViewManager {

	private TreeStructureViewBuilder treeViewBuilder;
	private String buildConfigFilePath = null;

	private NavigationHistoryModel historyModel = new NavigationHistoryModel();
	private ArrayList structureViews = new ArrayList();	
	private FileStructureView defaultFileView = null;
	
    private static final StructureViewProperties DEFAULT_VIEW_PROPERTIES; 
    private static final List AVAILABLE_RELATIONS;
	
    public final StructureModelListener VIEW_LISTENER = new StructureModelListener() {
        public void modelUpdated(StructureModel model) {        	
        	Ajde.getDefault().logEvent("updating structure views: " + structureViews);
//        	
//        	if (defaultFileView != null) {
//        		defaultFileView.setSourceFile(Ajde.getDefault().getEditorManager().getCurrFile());
//        	}
        	
        	for (Iterator it = structureViews.iterator(); it.hasNext(); ) {
        		treeViewBuilder.buildView((StructureView)it.next(), (StructureModel)model);
        	}
        }
    }; 
  
  	/**
  	 * @param nodeFactory			concrete factory for creating view nodes
  	 */
	public StructureViewManager(StructureViewNodeFactory nodeFactory) {
		treeViewBuilder = new TreeStructureViewBuilder(nodeFactory);
				
		StructureModelManager.getDefault().addListener(VIEW_LISTENER);			
	}
	
	public void fireNavigateBackAction(StructureView view) {
		ProgramElementNode backNode = historyModel.navigateBack();
		
		if (backNode == null) {
			Ajde.getDefault().getIdeUIAdapter().displayStatusInformation("No node to navigate back to in history");	
		} else {
			navigationAction(backNode, false);
		}
	}
  
	public void fireNavigateForwardAction(StructureView view) {
		ProgramElementNode forwardNode = historyModel.navigateForward();
		
		if (forwardNode == null) {
			Ajde.getDefault().getIdeUIAdapter().displayStatusInformation("No node to navigate forward to in history");	
		} else {
			navigationAction(forwardNode, false);
		}
	}

	/**
	 * Only navigations of the default view are registered with
	 * the history.
     * @param newFilePath the canonicalized path to the new file
	 */
	public void fireNavigationAction(String newFilePath, int lineNumber) {				
		StructureNode currNode = Ajde.getDefault().getStructureModelManager().getStructureModel().findNodeForSourceLine(
			newFilePath,
			lineNumber);
		
		if (currNode instanceof ProgramElementNode) {
			navigationAction((ProgramElementNode)currNode, true);	
		}
	} 
		
	/**
	 * History is recorded for {@link LinkNode} navigations.
	 */
	public void fireNavigationAction(StructureNode structureNode) {
		ProgramElementNode node = null;
		boolean recordHistory = false;
		if (structureNode instanceof LinkNode) {
			node = ((LinkNode)structureNode).getProgramElementNode();
			recordHistory = true;
		} else if (structureNode instanceof ProgramElementNode) {
			node = (ProgramElementNode)structureNode;
		}
		if (node != null) navigationAction(node, recordHistory);
	}

	/**
	 * Highlights the given node in all structure views.  If the node represents code
	 * and as such is below the granularity visible in the view the parent is highlighted,
	 * along with the corresponding sourceline.
	 */ 
	private void navigationAction(ProgramElementNode node, boolean recordHistory) { 
		if (node == null 
			|| node == StructureModel.NO_STRUCTURE) {
			Ajde.getDefault().getIdeUIAdapter().displayStatusInformation("Source not available for node: " + node.getName());
			return;    	
		}
		Ajde.getDefault().logEvent("navigating to node: " + node + ", recordHistory: " + recordHistory);
		if (recordHistory) historyModel.navigateToNode(node); 
    	if (defaultFileView != null && node.getSourceLocation() != null) {
    		String newFilePath = node.getSourceLocation().getSourceFile().getAbsolutePath();
			if (defaultFileView.getSourceFile() != null
				&& !defaultFileView.getSourceFile().equals(newFilePath)) {
				defaultFileView.setSourceFile(newFilePath);
				treeViewBuilder.buildView(defaultFileView, StructureModelManager.getDefault().getStructureModel());
			}
		}
		   
	    for (Iterator it = structureViews.iterator(); it.hasNext(); ) {
    		StructureView view = (StructureView)it.next();
    		if (!(view instanceof GlobalStructureView) || !recordHistory || defaultFileView == null) {
	    		if (node.getProgramElementKind().equals(ProgramElementNode.Kind.CODE)) {
	    			ProgramElementNode parentNode = (ProgramElementNode)node.getParent();
	    			if (parentNode != null) {
		    			StructureViewNode currNode = view.findCorrespondingViewNode(parentNode);
		    			int lineOffset = node.getSourceLocation().getLine() - parentNode.getSourceLocation().getLine();
		    			if (currNode != null) view.setActiveNode(currNode, lineOffset);
	    			}
	    		} else {
	    			StructureViewNode currNode = view.findCorrespondingViewNode(node);
	    			if (currNode != null) view.setActiveNode(currNode);	
	    		}	
    		}
    	}
	}
	
	private ProgramElementNode getProgramElementNode(StructureViewNode node) {
		if (node.getStructureNode() instanceof ProgramElementNode) {
			return (ProgramElementNode)node.getStructureNode();	
		} else if (node.getStructureNode() instanceof LinkNode) {
			return ((LinkNode)node.getStructureNode()).getProgramElementNode();	
		} else {
			return null;
		}	
	}
	
	public void refreshView(StructureView view) {
		StructureViewNode activeNode = view.getActiveNode();
		treeViewBuilder.buildView(view, Ajde.getDefault().getStructureModelManager().getStructureModel());
		view.setActiveNode(activeNode);
	}		

	public StructureViewProperties getDefaultViewProperties() {
		return DEFAULT_VIEW_PROPERTIES;
	}

	/**
	 * Returns the list of all available relations.
	 */
	public List getAvailableRelations() {
		return AVAILABLE_RELATIONS;
	}
	
	/**
	 * @param	properties		can not be null
	 */
	public GlobalStructureView createGlobalView(GlobalViewProperties properties) {
		GlobalStructureView view = new GlobalStructureView(properties);
		structureViews.add(view);
		return view;
	}
	
	/**
	 * @param	sourceFilePath	full path to corresponding source file
	 * @param	properties		if null default properties will be used
	 * @return					always returns a view intance
	 */ 
	public FileStructureView createViewForSourceFile(String sourceFilePath, StructureViewProperties properties) {
		Ajde.getDefault().logEvent("creating view for file: " + sourceFilePath);
		if (properties == null) properties = DEFAULT_VIEW_PROPERTIES;
		FileStructureView view = new FileStructureView(properties);
		view.setSourceFile(sourceFilePath);
		treeViewBuilder.buildView(view, StructureModelManager.getDefault().getStructureModel()); 
		structureViews.add(view);
		return view; 
	}

	/**
	 * @return	true if the view was found and removed, false otherwise
	 */
	public boolean deleteView(StructureView view) {
		return structureViews.remove(view);
	}

	public void setDefaultFileView(FileStructureView defaultFileView) {
		this.defaultFileView = defaultFileView;
	}

	public FileStructureView getDefaultFileView() {
		return defaultFileView;
	}

	static {
		AVAILABLE_RELATIONS = new ArrayList();
        AVAILABLE_RELATIONS.add(AdviceAssociation.METHOD_CALL_SITE_RELATION);
        AVAILABLE_RELATIONS.add(AdviceAssociation.METHOD_RELATION);
        AVAILABLE_RELATIONS.add(AdviceAssociation.CONSTRUCTOR_CALL_SITE_RELATION);
        AVAILABLE_RELATIONS.add(AdviceAssociation.CONSTRUCTOR_RELATION);
        AVAILABLE_RELATIONS.add(AdviceAssociation.FIELD_ACCESS_RELATION);
        AVAILABLE_RELATIONS.add(AdviceAssociation.INITIALIZER_RELATION);
        AVAILABLE_RELATIONS.add(AdviceAssociation.HANDLER_RELATION);
        AVAILABLE_RELATIONS.add(AdviceAssociation.INTRODUCTION_RELATION);
        AVAILABLE_RELATIONS.add(IntroductionAssociation.INTRODUCES_RELATION);
        AVAILABLE_RELATIONS.add(InheritanceAssociation.IMPLEMENTS_RELATION);
        AVAILABLE_RELATIONS.add(InheritanceAssociation.INHERITS_RELATION);
        AVAILABLE_RELATIONS.add(InheritanceAssociation.INHERITS_MEMBERS_RELATION);
        AVAILABLE_RELATIONS.add(ReferenceAssociation.USES_POINTCUT_RELATION);
        AVAILABLE_RELATIONS.add(ReferenceAssociation.IMPORTS_RELATION);
        
        DEFAULT_VIEW_PROPERTIES = new StructureViewProperties();
        DEFAULT_VIEW_PROPERTIES.setRelations(AVAILABLE_RELATIONS);
	}   

}

//		this.multiFileViewMode = multiFileViewMode;
//		if (!multiFileViewMode) {
//			structureViews.add(DEFAULT_FILE_VIEW);
//			structureViews.add(DECLARATION_VIEW);
//			structureViews.add(CROSSCUTTING_VIEW);
//			structureViews.add(INHERITANCE_VIEW);
//		}

//	public GlobalStructureView getGlobalStructureView(StructureViewProperties.Hierarchy hierarchy) {
//		if (hierarchy == StructureViewProperties.Hierarchy.CROSSCUTTING) {
//			return CROSSCUTTING_VIEW;
//		} else if (hierarchy == StructureViewProperties.Hierarchy.INHERITANCE) {
//			return INHERITANCE_VIEW;
//		} else {
//			return DECLARATION_VIEW;
//		} 		
//	}

//	public FileStructureView getDefaultFileStructureView() {
//		return DEFAULT_FILE_VIEW;
//	}


