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

package org.aspectj.testing.compare.adapters;

import org.aspectj.testing.compare.GenericTreeNode;
import org.aspectj.testing.compare.*;

import javax.swing.tree.*; // sample uses TreeModel...
import java.util.*; 

/**
 * Factory for adapting Swing TreeNode to GenericTreeNode
 */
public class JTreeNodeGenericTreeNodeFactory implements GenericTreeNodeFactoryI {
    public static final GenericTreeNodeFactoryI SINGLETON
        = new JTreeNodeGenericTreeNodeFactory();
    private JTreeNodeGenericTreeNodeFactory() {}
    public Class getRootClass() {
        return TreeNode.class;
    }

    /** 
     * Adapt swing TreeModel to tree rooted at GenericTreeNode 
     * Only takes the current state of a TreeModel which does not
     * change during the construction of the adapter. If the
     * TreeModel changes, you can ask the adapter for a newly
     * wrapped tree.
     * Recursively convert entire tree from root to a wrapped tree
     * Note this takes a snapshot of the tree such that changes to
     * the TreeModel after the constructor returns are ignored.
     * Changes during the constructor produce undetermined results.
     * @param root the TreeNode taken as root of a tree to wrap
     * @param parent the parent of the resulting GenericTreeNode
     * @throws IllegalArgumentException if root is null 
     *         or if children are not instanceof TreeNode.
     */
    public GenericTreeNode createGenericTreeNode(Object root, GenericTreeNode parent) {
        if (null == root) {
            throw new IllegalArgumentException("null root");
        }
        if (! (root instanceof TreeNode)) {
            throw new IllegalArgumentException("not TreeNode: " + root);
        }
        TreeNode rootNode = (TreeNode) root;
        final int numKids = rootNode.getChildCount();
        ArrayList kids = new ArrayList(numKids);
        Enumeration children = rootNode.children();
        Object child;
        GenericTreeNode result = new GenericTreeNode();
        for (int i = 0; i < numKids; i++) {
            if (! children.hasMoreElements()) {
                throw new Error("(! children.hasNext())");
            } 
            child = children.nextElement();
            if (! (child instanceof TreeNode)) {
                throw new Error("! (child instanceof TreeNode)): " + child );
            }
            kids.add(createGenericTreeNode((TreeNode) child, result));
        }
        //result.init(parent, GenericTreeNode.COMPARATOR, rootNode, kids, null);
        result.init(parent, JTreeNodeComparator.SINGLETON, rootNode, kids, null);
        return result;
    } 

    /** Comparator for swing TreeNode todo convert from TreeNode to DefaultMutableTreeNode */
    static class JTreeNodeComparator implements Comparator {
        public static Comparator SINGLETON = new JTreeNodeComparator();
        private JTreeNodeComparator () {}
        public int compare(Object lhs, Object rhs) {
            int result = CompareUtil.compare(lhs, rhs);
            if (Integer.MAX_VALUE == result) {
                Class lhClass = lhs.getClass() ;
                Class rhClass = rhs.getClass() ;
                if ((DefaultMutableTreeNode.class.isAssignableFrom(lhClass)) 
                    && (DefaultMutableTreeNode.class.isAssignableFrom(rhClass))) {
                    DefaultMutableTreeNode lh = (DefaultMutableTreeNode) lhs ;
                    DefaultMutableTreeNode rh = (DefaultMutableTreeNode) rhs ;
                    Object lhObject = lh.getUserObject();
                    Object rhObject = rh.getUserObject();
                    result = CompareUtil.compare(lhs, rhs);
                    if (Integer.MAX_VALUE == result) {
                        result = lhObject.toString().compareTo(rhObject.toString());
                    }
                } else { // urk - broken unless wrapper
                    result = lhs.toString().compareTo(rhs.toString());
                }
            }
            return result;
        }
    }
}
