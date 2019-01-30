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

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;

import org.aspectj.ajde.Ajde;
import org.aspectj.asm.IProgramElement;

/**
 * @author  Mik Kersten
 */
class BrowserViewTreeListener implements TreeSelectionListener, MouseListener {
    private StructureTree tree = null;

    public BrowserViewTreeListener(StructureTree tree) {
        this.tree = tree;
    }

    public void valueChanged(TreeSelectionEvent e) { }

    public void mouseEntered(MouseEvent e) { }

    public void mouseExited(MouseEvent e) { }

    public void mousePressed(MouseEvent e) { }

    public void mouseReleased(MouseEvent e) { }

    public void mouseClicked(MouseEvent e) {
        singleClickNavigation(e);
		//doubleClickNavigation(e);
        maybeShowPopup(e);
    }

    public void singleClickNavigation(MouseEvent e) {
        SwingTreeViewNode treeNode = (SwingTreeViewNode)tree.getLastSelectedPathComponent();
        if (treeNode != null && !e.isControlDown() && !e.isShiftDown() && e.getModifiers() != 4) {
            IProgramElement currNode = (IProgramElement)treeNode.getUserObject();
            if (currNode!=null && !e.isControlDown()
                && !e.isShiftDown() && e.getModifiers() != 4) {
                //AjdeUIManager.getDefault().getViewManager().showNodeInMasterView((ProgramElementNode)currNode);
                //if (AjdeUIManager.getDefault().getViewManager().isSplitViewMode()) {
                //    AjdeUIManager.getDefault().getViewManager().showNodeInSlaveView((ProgramElementNode)currNode);
                //}
            } 
//            	else if (currNode instanceof LinkNode) {
                //if (!AjdeUIManager.getDefault().getViewManager().isSplitViewMode()) {
                //    AjdeUIManager.getDefault().getViewManager().showNodeInMasterView((LinkNode)currNode);
                //} else {
                //    AjdeUIManager.getDefault().getViewManager().showNodeInSlaveView(((LinkNode)currNode).getProgramElementNode());
                //}
//           }
        }
    }

        public void doubleClickNavigation(MouseEvent e) {
//            int clickCount = e.getClickCount();
            SwingTreeViewNode treeNode = (SwingTreeViewNode)tree.getLastSelectedPathComponent();
            if (treeNode != null) {
                IProgramElement currNode = (IProgramElement)treeNode.getUserObject();
                if (currNode!=null && !e.isControlDown() && !e.isShiftDown()
                    && e.getModifiers() != 4) {
                    //AjdeUIManager.getDefault().getViewManager().showNodeInMasterView(((LinkNode)currNode).getProgramElementNode());
                    //AjdeUIManager.getDefault().getViewManager().showNodeInSlaveView(((LinkNode)currNode).getProgramElementNode());
                } 
//                else if (currNode instanceof LinkNode) {
//                    if (clickCount == 1) {
//                        //AjdeUIManager.getDefault().getViewManager().showLink((LinkNode)currNode);
//                    } else if (clickCount == 2) {
//                        //navigationAction((ProgramElementNode)((LinkNode)currNode).getProgramElementNode(), true, true);
//                    }
//                }
            }
        }

    /**
     * @todo    this should probably use <CODE>e.isPopupTrigger()</CODE> but that
     * doesn't work for some reason, so we just check if the right mouse button
     * has been clicked.
     */
    private void maybeShowPopup(MouseEvent e) {
        if (e.getModifiers() == InputEvent.BUTTON3_MASK && tree.getSelectionCount() > 0) {
//            TreePath[] selectionPaths = tree.getSelectionPaths();
            final List signatures = new ArrayList();
//            for (int i = 0; i < selectionPaths.length; i++) {
//                IProgramElement currNode = (IProgramElement)((SwingTreeViewNode)selectionPaths[i].getLastPathComponent()).getUserObject();
////                if (currNode instanceof LinkNode || currNode instanceof IProgramElement) {
////                    signatures.add(currNode);
////                }
//            }

            JPopupMenu popup = new JPopupMenu();
            JMenuItem showSourcesItem = new JMenuItem("Display sources", Ajde.getDefault().getIconRegistry().getStructureSwingIcon(IProgramElement.Kind.CODE));
            showSourcesItem.setFont(new java.awt.Font("Dialog", 0, 11));
            showSourcesItem.addActionListener(new AbstractAction() {
                
				private static final long serialVersionUID = 1L;

				public void actionPerformed(ActionEvent e) {
                    //AjdeUIManager.getDefault().getViewManager().showSourcesNodes(signatures);
                    // USED THE FOLLOWING FROM: BrowserViewManager:
//					public void showSourcesNodes(java.util.List nodes) {
//						for (Iterator it = nodes.iterator(); it.hasNext(); ) {
//							ProgramElementNode currNode = null;
//							IProgramElement IProgramElement = (IProgramElement)it.next();
//							if (IProgramElement instanceof LinkNode) {
//								currNode = ((LinkNode)IProgramElement).getProgramElementNode();
//							} else {
//								currNode = (ProgramElementNode)IProgramElement;
//							}
//							ISourceLocation sourceLoc = currNode.getSourceLocation();
//							if (null != sourceLoc) {
//								Ajde.getDefault().getEditorManager().addViewForSourceLine(
//									sourceLoc.getSourceFile().getAbsolutePath(),
//									sourceLoc.getLine());
//							}
//						}
//					}

                }
            });
            popup.add(showSourcesItem);

            popup.addSeparator();
            JMenuItem generatePCD = new JMenuItem("Pointcut Wizard (alpha)...", Ajde.getDefault().getIconRegistry().getStructureSwingIcon(IProgramElement.Kind.POINTCUT));
            generatePCD.setFont(new java.awt.Font("Dialog", 0, 11));
            generatePCD.addActionListener(new AbstractAction() {
                
				private static final long serialVersionUID = 1L;

				public void actionPerformed(ActionEvent e) {
					Ajde.getDefault().getViewManager().extractAndInsertSignatures(signatures, true);
                }
            });
            popup.add(generatePCD);

            popup.show(e.getComponent(), e.getX(), e.getY());
        }
    }
}
