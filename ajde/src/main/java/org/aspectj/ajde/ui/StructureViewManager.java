/* *******************************************************************
 * Copyright (c) 1999-2001 Xerox Corporation, 
 *               2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Xerox/PARC     initial implementation
 *     Helen Hawkins  Converted to new interface (bug 148190) 
 * ******************************************************************/

package org.aspectj.ajde.ui;

import java.util.ArrayList;
import java.util.List;

import org.aspectj.ajde.Ajde;
import org.aspectj.ajde.ui.internal.NavigationHistoryModel;
import org.aspectj.ajde.ui.internal.TreeStructureViewBuilder;
import org.aspectj.asm.IHierarchy;
import org.aspectj.asm.IHierarchyListener;
import org.aspectj.asm.IProgramElement;
import org.aspectj.asm.IRelationship;
import org.aspectj.asm.internal.AspectJElementHierarchy;

/**
 * @author Mik Kersten
 */
public class StructureViewManager {

	private final TreeStructureViewBuilder treeViewBuilder;
	// private String buildConfigFilePath = null;

	private final NavigationHistoryModel historyModel = new NavigationHistoryModel();
	private final List structureViews = new ArrayList();
	private FileStructureView defaultFileView = null;

	private static final StructureViewProperties DEFAULT_VIEW_PROPERTIES;
	private static final List AVAILABLE_RELATIONS;

	public final IHierarchyListener VIEW_LISTENER = new IHierarchyListener() {
		public void elementsUpdated(IHierarchy model) {
			// updating structure views:

			for (Object structureView : structureViews) {
				treeViewBuilder.buildView((StructureView) structureView, (AspectJElementHierarchy) model);
			}
		}
	};

	/**
	 * @param nodeFactory concrete factory for creating view nodes
	 */
	public StructureViewManager(StructureViewNodeFactory nodeFactory) {
		treeViewBuilder = new TreeStructureViewBuilder(nodeFactory);

		Ajde.getDefault().getModel().addListener(VIEW_LISTENER);
	}

	public void fireNavigateBackAction(StructureView view) {
		IProgramElement backNode = historyModel.navigateBack();

		if (backNode == null) {
			Ajde.getDefault().getIdeUIAdapter().displayStatusInformation("No node to navigate back to in history");
		} else {
			navigationAction(backNode, false);
		}
	}

	public void fireNavigateForwardAction(StructureView view) {
		IProgramElement forwardNode = historyModel.navigateForward();

		if (forwardNode == null) {
			Ajde.getDefault().getIdeUIAdapter().displayStatusInformation("No node to navigate forward to in history");
		} else {
			navigationAction(forwardNode, false);
		}
	}

	/**
	 * Only navigations of the default view are registered with the history.
	 * 
	 * @param newFilePath the canonicalized path to the new file
	 */
	public void fireNavigationAction(String newFilePath, int lineNumber) {
		IProgramElement currNode = Ajde.getDefault().getModel().getHierarchy().findElementForSourceLine(newFilePath, lineNumber);

		if (currNode != null) {
			navigationAction(currNode, true);
		}
	}

	public void fireNavigationAction(IProgramElement pe, boolean isLink) {
		navigationAction(pe, isLink);
	}

	/**
	 * Highlights the given node in all structure views. If the node represents code and as such is below the granularity visible in
	 * the view the parent is highlighted, along with the corresponding sourceline.
	 */
	private void navigationAction(IProgramElement node, boolean recordHistory) {
		if (node == null)
			return;
		// navigating to node: " + node + ", recordHistory: " + recordHistory
		if (recordHistory)
			historyModel.navigateToNode(node);
		if (defaultFileView != null && node.getSourceLocation() != null) {
			String newFilePath = node.getSourceLocation().getSourceFile().getAbsolutePath();
			if (defaultFileView.getSourceFile() != null && !defaultFileView.getSourceFile().equals(newFilePath)) {
				defaultFileView.setSourceFile(newFilePath);
				treeViewBuilder.buildView(defaultFileView, Ajde.getDefault().getModel().getHierarchy());
			}
		}

		for (Object structureView : structureViews) {
			StructureView view = (StructureView) structureView;
			if (!(view instanceof GlobalStructureView) || !recordHistory || defaultFileView == null) {
				if (node.getKind().equals(IProgramElement.Kind.CODE)) {
					IProgramElement parentNode = node.getParent();
					if (parentNode != null) {
						IStructureViewNode currNode = view.findCorrespondingViewNode(parentNode);
						int lineOffset = node.getSourceLocation().getLine() - parentNode.getSourceLocation().getLine();
						if (currNode != null)
							view.setActiveNode(currNode, lineOffset);
					}
				} else {
					IStructureViewNode currNode = view.findCorrespondingViewNode(node);
					if (currNode != null)
						view.setActiveNode(currNode);
				}
			}
		}
	}

	public void refreshView(StructureView view) {
		IStructureViewNode activeNode = view.getActiveNode();
		treeViewBuilder.buildView(view, Ajde.getDefault().getModel().getHierarchy());
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
	 * @param properties can not be null
	 */
	public GlobalStructureView createGlobalView(GlobalViewProperties properties) {
		GlobalStructureView view = new GlobalStructureView(properties);
		structureViews.add(view);
		return view;
	}

	/**
	 * @param sourceFilePath full path to corresponding source file
	 * @param properties if null default properties will be used
	 * @return always returns a view intance
	 */
	public FileStructureView createViewForSourceFile(String sourceFilePath, StructureViewProperties properties) {
		// creating view for file:
		if (properties == null)
			properties = DEFAULT_VIEW_PROPERTIES;
		FileStructureView view = new FileStructureView(properties);
		view.setSourceFile(sourceFilePath);
		treeViewBuilder.buildView(view, Ajde.getDefault().getModel().getHierarchy());
		structureViews.add(view);
		return view;
	}

	/**
	 * @return true if the view was found and removed, false otherwise
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
		AVAILABLE_RELATIONS.add(IRelationship.Kind.ADVICE);
		AVAILABLE_RELATIONS.add(IRelationship.Kind.DECLARE);

		DEFAULT_VIEW_PROPERTIES = new StructureViewProperties();
		DEFAULT_VIEW_PROPERTIES.setRelations(AVAILABLE_RELATIONS);
	}

}

// this.multiFileViewMode = multiFileViewMode;
// if (!multiFileViewMode) {
// structureViews.add(DEFAULT_FILE_VIEW);
// structureViews.add(DECLARATION_VIEW);
// structureViews.add(CROSSCUTTING_VIEW);
// structureViews.add(INHERITANCE_VIEW);
// }

// public GlobalStructureView getGlobalStructureView(StructureViewProperties.Hierarchy hierarchy) {
// if (hierarchy == StructureViewProperties.Hierarchy.CROSSCUTTING) {
// return CROSSCUTTING_VIEW;
// } else if (hierarchy == StructureViewProperties.Hierarchy.INHERITANCE) {
// return INHERITANCE_VIEW;
// } else {
// return DECLARATION_VIEW;
// }
// }

// public FileStructureView getDefaultFileStructureView() {
// return DEFAULT_FILE_VIEW;
// }

