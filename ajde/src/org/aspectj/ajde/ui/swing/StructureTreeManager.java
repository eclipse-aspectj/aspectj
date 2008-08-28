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

package org.aspectj.ajde.ui.swing;

import java.awt.event.MouseListener;
import java.util.ArrayList;

import javax.swing.SwingUtilities;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.aspectj.ajde.Ajde;
import org.aspectj.ajde.ui.AbstractIcon;
import org.aspectj.ajde.ui.GlobalStructureView;
import org.aspectj.ajde.ui.IStructureViewNode;
import org.aspectj.ajde.ui.StructureView;
import org.aspectj.ajde.ui.StructureViewProperties;
import org.aspectj.asm.IHierarchy;
import org.aspectj.asm.IProgramElement;
import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.Message;

/**
 * @author Mik Kersten
 */
class StructureTreeManager {

	private StructureTree structureTree;
	private SwingTreeViewNodeRenderer cellRenderer = null;
	private TreeSelectionListener treeListener = null;

	private final StructureTreeModel NO_STRUCTURE_MODEL = new StructureTreeModel(new SwingTreeViewNode(IHierarchy.NO_STRUCTURE,
			new AbstractIcon(null), new ArrayList()));

	/**
	 * @todo should probably avoid that MouseListener cast
	 */
	public StructureTreeManager() {
		structureTree = new StructureTree();
		structureTree.setModel(NO_STRUCTURE_MODEL);
		cellRenderer = new SwingTreeViewNodeRenderer();
		structureTree.setCellRenderer(cellRenderer);
		// if (fileView) {
		treeListener = new StructureViewTreeListener(structureTree);
		// } else {
		// treeListener = new BrowserViewTreeListener(structureTree);
		// }
		structureTree.addTreeSelectionListener(treeListener);
		structureTree.addMouseListener((MouseListener) treeListener);
	}

	public void highlightNode(IProgramElement node) {
		highlightNode((SwingTreeViewNode) structureTree.getModel().getRoot(), node);
	}

	public IProgramElement getSelectedIProgramElement() {
		return (IProgramElement) ((SwingTreeViewNode) structureTree.getLastSelectedPathComponent()).getUserObject();
	}

	public void scrollToHighlightedNode() {
		structureTree.scrollPathToVisible(structureTree.getSelectionPath());
	}

	private void highlightNode(SwingTreeViewNode parent, IProgramElement node) {
		for (int i = 0; i < parent.getChildCount(); i++) {
			SwingTreeViewNode currNode = (SwingTreeViewNode) parent.getChildAt(i);
			IProgramElement sNode = currNode.getStructureNode();
			if (sNode != null && sNode.equals(node) && currNode.getKind() != IStructureViewNode.Kind.LINK) {
				TreePath path = new TreePath(currNode.getPath());
				structureTree.setSelectionPath(path);
				int currRow = structureTree.getRowForPath(path);
				structureTree.expandRow(currRow);
				structureTree.scrollRowToVisible(currRow);
			} else {
				highlightNode(currNode, node);
			}
		}
	}

	// public void updateTree(StructureView structureView) {
	// displayTree(structureView, 10);
	// }
	//
	// public void updateTree(GlobalStructureView structureView) {
	// displayTree(structureView, depth);
	// }

	public void updateTree(final StructureView structureView) {
		if (structureView == null)
			return;
		Runnable update = new Runnable() {
			public void run() {
				structureTree.removeAll();
				// SwingTreeViewNode currNode;
				if (structureView.getRootNode() == null) {
					structureTree.setModel(NO_STRUCTURE_MODEL);
				} else {
					structureTree.setModel(new StructureTreeModel((SwingTreeViewNode) structureView.getRootNode()));
				}

				if (structureView instanceof GlobalStructureView) {
					GlobalStructureView view = (GlobalStructureView) structureView;
					if (view.getGlobalViewProperties().getHierarchy() == StructureViewProperties.Hierarchy.DECLARATION) {
						expandTreeToFiles();
					} else {
						expandTree(15);
					}
				} else {
					expandTree(10);
				}

			}
		};

		if (SwingUtilities.isEventDispatchThread()) {
			update.run();
		} else {
			try {
				SwingUtilities.invokeAndWait(update);
			} catch (Exception e) {
				Message msg = new Message("Could not update tree.", IMessage.ERROR, e, null);
				Ajde.getDefault().getMessageHandler().handleMessage(msg);
			}
		}
	}

	StructureTree getStructureTree() {
		return structureTree;
	}

	private void expandTreeToFiles() {
		for (int i = 0; i < structureTree.getRowCount(); i++) {
			TreePath path = structureTree.getPathForRow(i);
			SwingTreeViewNode node = (SwingTreeViewNode) path.getLastPathComponent();
			if (node.getUserObject() instanceof IProgramElement) {
				IProgramElement pNode = (IProgramElement) node.getUserObject();
				IProgramElement.Kind kind = pNode.getKind();
				if (kind == IProgramElement.Kind.PROJECT || kind == IProgramElement.Kind.PACKAGE) {
					structureTree.expandPath(path);
				} else {
					structureTree.collapsePath(path);
				}
			} else {
				structureTree.collapsePath(path);
			}
		}
		structureTree.expandPath(structureTree.getPathForRow(0));
	}

	private void expandTree(int depth) {
		for (int i = 0; i < structureTree.getRowCount(); i++) {
			TreePath path = structureTree.getPathForRow(i);
			SwingTreeViewNode node = (SwingTreeViewNode) path.getLastPathComponent();
			if (path.getPath().length - 1 > depth || node.getKind() == IStructureViewNode.Kind.RELATIONSHIP) {
				structureTree.collapsePath(path);
			} else {
				structureTree.expandPath(path);
			}
		}
		structureTree.expandPath(structureTree.getPathForRow(0));
	}

	private static class StructureTreeModel extends DefaultTreeModel {

		private static final long serialVersionUID = 1L;

		public StructureTreeModel(TreeNode newRoot) {
			super(newRoot);
		}

		public void valueForPathChanged(TreePath path, Object newValue) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
			nodeChanged(node);
		}
	}
}

// /**
// * @param node if null assume root
// */
// public void navigationAction(ProgramElementNode node, boolean followedLink, boolean forward) {
// if (node == null) {
// structureTree.setSelectionRow(0);
// structureTree.scrollRowToVisible(0);
// } else if (node.getSourceLocation().getSourceFilePath() != null) {
// final String fileName = node.getSourceLocation().getSourceFilePath();
// final int lineNumber = node.getSourceLocation().getLineNumber();
// if (fileName != null && lineNumber > 0) {
// Runnable update = new Runnable() {
// public void run() {
// Ajde.getDefault().getEditorManager().showSourceLine(fileName, lineNumber, true);
// }
// };
//
// if (SwingUtilities.isEventDispatchThread()) {
// update.run();
// } else {
// try {
// SwingUtilities.invokeAndWait(update);
// } catch (Exception ee) {
//                        
// }
// }
// }
// if (followedLink) {
// highlightNode((SwingTreeViewNode)structureTree.getModel().getRoot(), node);
// }
// }
// }

// if (node instanceof ProgramElementNode) {
// ProgramElementNode lexicalNode = (ProgramElementNode)node;
// setIcon(getProgramElementNodeIcon(lexicalNode));
// } else if (node instanceof RelationNode) {
// RelationNode relationNode = (RelationNode)node;
//                
// setIcon(icons.getAssociationSwingIcon(relationNode.getRelation()));
// this.setFont(new Font(this.getFont().getName(), Font.ITALIC, this.getFont().getSize()));
//
// } else if (node instanceof LinkNode) {
// LinkNode link = (LinkNode)node;
// setIcon(getProgramElementNodeIcon(link.getProgramElementNode()));
// } else {
// if (node != null && ProgramElementNode.Kind.PACKAGE.equals(node.getKind())) {
// setIcon(icons.getStructureSwingIcon(ProgramElementNode.Kind.PACKAGE));
// } else if (node != null && ProgramElementNode.Kind.PROJECT.equals(node.getKind())) {
// setIcon(icons.getStructureSwingIcon(ProgramElementNode.Kind.PROJECT));
// } else if (node != null && ProgramElementNode.Kind.FILE.equals(node.getKind())) {
// setIcon(icons.getStructureSwingIcon(ProgramElementNode.Kind.CLASS));
// } else {
// setIcon(null);
// }
// }

// void updateTree(int depth, GlobalViewProperties properties) {
// this.hierarchy = properties.getHierarchy();
// displayTree(depth, null);
// }
//
// void updateTree(String filePath, int depth, GlobalViewProperties properties) {
// this.hierarchy = properties.getHierarchy();
// if (filePath == null || filePath.equals("")) {
// structureTree.setModel(NO_FILE_SELECTED_MODEL);
// } else {
// structureTree.setRootFilePath(filePath);
// displayTree(depth, filePath);
// }
// }

// int accessibility = 0;
// if (pNode.getAccessibility().contains(ProgramElementNode.Accessibility.PUBLIC)) {
// accessibility = 1;
// } else if (pNode.getAccessibility().contains(ProgramElementNode.Accessibility.PROTECTED)) {
// accessibility = 2;
// } else if (pNode.getAccessibility().contains(ProgramElementNode.Accessibility.PRIVATE)) {
// accessibility = 3;
// } else if (pNode.getAccessibility().contains(ProgramElementNode.Accessibility.PRIVILEGED)) {
// accessibility = 3;
// }
//
// if (pNode == null || pNode.getKind() == null) {
// return null;
// } else if (ProgramElementNode.Kind.PROJECT.equals(pNode.getKind())) {
// return icons.getStructureSwingIcon(ProgramElementNode.Kind.PROJECT);
// } else if (ProgramElementNode.Kind.PACKAGE.equals(pNode.getKind())) {
// return icons.getStructureSwingIcon(ProgramElementNode.Kind.PACKAGE);
// } else if (ProgramElementNode.Kind.FILE.equals(pNode.getKind())) {
// return icons.getStructureSwingIcon(ProgramElementNode.Kind.CLASS);
// } else if (ProgramElementNode.Kind.CLASS.equals(pNode.getKind())) {
// switch (accessibility) {
// case 1: return icons.getClassPublicIcon();
// case 2: return icons.getClassProtectedIcon();
// case 3: return icons.getClassPrivateIcon();
// default: return icons.getClassPackageIcon();
// }
// } else if (ProgramElementNode.Kind.INTERFACE.equals(pNode.getKind())) {
// switch (accessibility) {
// case 1: return icons.getInterfacePublicIcon();
// case 2: return icons.getInterfaceProtectedIcon();
// case 3: return icons.getInterfacePrivateIcon();
// default: return icons.getInterfacePackageIcon();
// }
// } else if (ProgramElementNode.Kind.ASPECT.equals(pNode.getKind())) {
// switch (accessibility) {
// case 1: return icons.getAspectPublicIcon();
// case 2: return icons.getAspectProtectedIcon();
// case 3: return icons.getAspectPrivateIcon();
// case 4: return icons.getAspectPrivilegedIcon();
// default: return icons.getAspectPackageIcon();
// }
// } else if (ProgramElementNode.Kind.METHOD.equals(pNode.getKind())
// || ProgramElementNode.Kind.INITIALIZER.equals(pNode.getKind())
// || ProgramElementNode.Kind.CONSTRUCTOR.equals(pNode.getKind())) {
// switch (accessibility) {
// case 1: return icons.getMethodPublicIcon();
// case 2: return icons.getMethodProtectedIcon();
// case 3: return icons.getMethodPrivateIcon();
// default: return icons.getMethodPackageIcon();
// }
// } else if (ProgramElementNode.Kind.FIELD.equals(pNode.getKind())) {
// switch (accessibility) {
// case 1: return icons.getFieldPublicIcon();
// case 2: return icons.getFieldProtectedIcon();
// case 3: return icons.getFieldPrivateIcon();
// default: return icons.getFieldPackageIcon();
// }
// } else if (ProgramElementNode.Kind.INTRODUCTION.equals(pNode.getKind())) {
// switch (accessibility) {
// case 1: return icons.getIntroductionPublicIcon();
// case 2: return icons.getIntroductionProtectedIcon();
// case 3: return icons.getIntroductionPrivateIcon();
// default: return icons.getIntroductionPackageIcon();
// }
// } else if (ProgramElementNode.Kind.POINTCUT.equals(pNode.getKind())) {
// switch (accessibility) {
// case 1: return icons.getJoinpointPublicIcon();
// case 2: return icons.getJoinpointProtectedIcon();
// case 3: return icons.getJoinpointPrivateIcon();
// default: return icons.getJoinpointPackageIcon();
// }
// } else if (ProgramElementNode.Kind.ADVICE.equals(pNode.getKind())) {
// return icons.getAdviceIcon();
// } else if (ProgramElementNode.Kind.DECLARE_PARENTS.equals(pNode.getKind())) {
// return icons.getDeclareParentsIcon();
// } else if (ProgramElementNode.Kind.DECLARE_ERROR.equals(pNode.getKind())) {
// return icons.getDeclareErrorIcon();
// } else if (ProgramElementNode.Kind.DECLARE_WARNING.equals(pNode.getKind())) {
// return icons.getDeclareWarningIcon();
// } else if (ProgramElementNode.Kind.DECLARE_SOFT.equals(pNode.getKind())) {
// return icons.getDeclareSoftIcon();
// } else if (ProgramElementNode.Kind.CODE.equals(pNode.getKind())) {
// return icons.getCodeIcon();
// } else {
// return null;
// }

//                
// if (relationNode.getKind().equals(org.aspectj.asm.associations.Advice.NAME) ||
// relationNode.getKind().equals(org.aspectj.asm.associations.Introduction.NAME)) {
// if (relationNode.getRelation().getBackNavigationName().equals(relationNode.getName()) ){
// setIcon(icons.getRelationAdviceBackIcon());
// } else {
// setIcon(icons.getAssociationSwingIcon(relationNode.getRelation()));
// setIcon(icons.getRelationAdviceForwardIcon());
// }
// } else if (relationNode.getKind().equals(org.aspectj.asm.associations.Inheritance.NAME)) {
// if (relationNode.getRelation().getBackNavigationName().equals(relationNode.getName()) ){
// setIcon(icons.getRelationInheritanceBackIcon());
// } else {
// setIcon(icons.getRelationInheritanceForwardIcon());
// }
// } else {
// if (relationNode.getRelation().getBackNavigationName().equals(relationNode.getName()) ){
// setIcon(icons.getRelationReferenceBackIcon());
// } else {
// setIcon(icons.getRelationReferenceForwardIcon());
// }
// }

// public ProgramElementNode getRootProgramElementNode() {
// IProgramElement node = (IProgramElement)((SwingTreeViewNode)structureTree.getModel().getRoot()).getUserObject();
// if (node instanceof ProgramElementNode) {
// return (ProgramElementNode)node;
// } else {
// return null;
// }
// }

// /**
// * @todo HACK: this is a workaround and can break
// */
// private static ProgramElementNode mapResult = null;
// private ProgramElementNode getNodeForLink(LinkNode node, IProgramElement rootNode) {
// ProgramElementNode result = null;
// if (rootNode instanceof ProgramElementNode &&
// ((ProgramElementNode)rootNode).getName().equals(node.getProgramElementNode().getName())) {
// mapResult = (ProgramElementNode)rootNode;
// } else {
// ProgramElementNode linkedNode = node.getProgramElementNode();
// for (Iterator it = rootNode.getChildren().iterator(); it.hasNext(); ) {
// IProgramElement child = (IProgramElement)it.next();
// getNodeForLink(node, child);
// }
// }
// return mapResult;
// }

// private void sortNodes(List nodes) {
// if (sortNodes) {
// Collections.sort(nodes, IProgramElementComparator);
// }
// }

// private class IProgramElementComparator implements Comparator {
// public int compare(Object o1, Object o2) {
// IProgramElement t1 = (IProgramElement) ((SwingTreeViewNode) o1).getUserObject();
// IProgramElement t2 = (IProgramElement) ((SwingTreeViewNode) o2).getUserObject();
// if (t1 instanceof ProgramElementNode && t2 instanceof ProgramElementNode) {
// ProgramElementNode p1 = (ProgramElementNode) t1;
// ProgramElementNode p2 = (ProgramElementNode) t2;
// return p1.getName().compareTo(p2.getName());
// } else {
// return 0;
// }
// }
// }

// private class StructureViewNodeAdapter extends DefaultMutableTreeNode {
// 
// private StructureViewNode nodeInfo = null;
//
// public StructureViewNodeAdapter(StructureViewNode nodeInfo) {
// super(nodeInfo, true);
// this.nodeInfo = nodeInfo;
// }
//
// public String toString() {
// if (nodeInfo != null) {
// return nodeInfo.toString();
// } else {
// return "";
// }
// }
// }

