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
//
//import org.aspectj.asm.SourceLocation;
import org.aspectj.testing.compare.GenericTreeNode;
//import org.aspectj.testing.compare.*;
//import org.aspectj.asm.StructureNode;
//import org.aspectj.asm.LinkNode;
//import org.aspectj.asm.RelationNode;
//import org.aspectj.asm.Relation;
//import org.aspectj.asm.ProgramElementNode;
//import java.util.*;
//
///**
// * Factory for adapting StructureNode trees to GenericTreeNode
// */
// XXX compiler
public class StructureGenericTreeNodeFactory implements GenericTreeNodeFactoryI {
    public GenericTreeNode createGenericTreeNode(Object root,
                                                 GenericTreeNode parent) {
                                                    return null;
    }
    /**
	 * @see org.aspectj.testing.compare.adapters.GenericTreeNodeFactoryI#getRootClass()
	 */
	public Class getRootClass() {
		return null;
	}

}
//    protected static final String[] MODIFIERS = new String[]
//    { "strictfp", "abstract", "synchronized", "native",
//      "final", "transient", "static", "volatile" };
//    protected static final List MODIFIERS_LIST = Arrays.asList(MODIFIERS);
//    protected static final String[] ACCESS = new String[]
//    { "private", "protected", "package", "public", "privileged" };
//    protected static final List ACCESS_LIST = Arrays.asList(ACCESS);
//
//    /** represent empty list of children (todo: use immutable instead) */
//    public static final List EMPTY_LIST = new ArrayList();
//    public static final GenericTreeNodeFactoryI SINGLETON
//        = new StructureGenericTreeNodeFactory();
//    /** delegate of the factory */
//    private static final Comparator NODE_COMPARATOR;
//
//    static {
//        SubTypeComparator init = new SubTypeComparator();
//        init.addComparator(LinkNode.class, LinkNodeComparator.SINGLETON);
//        init.addComparator(RelationNode.class, RelationNodeComparator.SINGLETON);
//        init.addComparator(ProgramElementNode.class, ProgramElementNodeComparator.SINGLETON);
//        init.addComparator(StructureNode.class, StructureNodeComparator.SINGLETON);
//        GenericTreeNodeComparator gtnc = new GenericTreeNodeComparator(init);
//        NODE_COMPARATOR = gtnc;
//    }
//
//    private StructureGenericTreeNodeFactory() {}
//    public Class getRootClass() {
//        return StructureNode.class;
//    }
//
//    /**
//     * Adapt Structure model to tree rooted at GenericTreeNode.
//     * Only takes the current state of a model which does not
//     * change during the construction of the adapter. If the
//     * model changes, you can ask the adapter for a newly
//     * wrapped tree.
//     * @param root the TreeNode taken as root of a tree to wrap
//     * @param parent the parent of the resulting GenericTreeNode
//     * @throws IllegalArgumentException if root is null
//     *         or if children are not instanceof TreeNode.
//     */
//    public GenericTreeNode createGenericTreeNode(Object root,
//                                                 GenericTreeNode parent) {
//        if (null == root) {
//            throw new IllegalArgumentException("null root");
//        }
//        if (! (root instanceof StructureNode)) {
//            throw new IllegalArgumentException("not StructureNode: " + root);
//        }
//        GenericTreeNode result = new GenericTreeNode();
//        StructureNode rootNode = (StructureNode) root;
//        List kidList = rootNode.getChildren();
//        List kids = EMPTY_LIST;
//        // get kids of result
//        if (null != kidList) {
//            final int numKids = kidList.size();
//            ArrayList newKids = new ArrayList(numKids);
//            ListIterator kidIter = kidList.listIterator();
//            Object child;
//            for (int i = 0; i < numKids; i++) {
//                if (! kidIter.hasNext()) { // items removed from list while iterating
//                    throw new Error("(! hasNext())");
//                }
//                child = kidIter.next();
//                if (! (child instanceof StructureNode)) {
//                    throw new Error("! (child instanceof StructureNode)): " + child );
//                }
//                newKids.add(createGenericTreeNode((StructureNode) child, result));
//            }
//            kids = newKids;
//        }
//        // todo: select comparator here - avoids type checking at run time
//        //result.init(parent, StructureComparator.SINGLETON, rootNode, kids, null);
//        result.init(parent, NODE_COMPARATOR, rootNode, kids, null);
//        return result;
//    }
//
//    /** Comparator for GenericTreeNode delegates to handler for nodes... */
//    static final class GenericTreeNodeComparator implements Comparator {
//        private final Comparator delegate;
//        private GenericTreeNodeComparator (Comparator delegate) {
//            this.delegate = delegate;
//        }
//        public final int compare(Object lhs, Object rhs) {
//            return delegate.compare(((GenericTreeNode)lhs).getNode()
//                                    , ((GenericTreeNode)lhs).getNode());
//        }
//    }
//
//    /**
//     * Comparator for RelationNode delegates to String & boolean comparison of public attributes.
//     */
//    static class RelationNodeComparator implements Comparator {
//        public static Comparator SINGLETON = new RelationNodeComparator();
//        private RelationNodeComparator () {}
//        /**
//         * Comparator for RelationNode uses String & boolean comparison of public attributes
//         * forwardNavigationName, backNavigationName, associationName, symmetrical, associative.
//         */
//        public int compare(Object lhs, Object rhs) {
//            int result = CompareUtil.compare(lhs, rhs);
//            if (Integer.MAX_VALUE == result) {
//                RelationNode lh = (RelationNode) lhs ;
//                RelationNode rh = (RelationNode) rhs ;
//                Relation leftRelation = lh.getRelation();
//                Relation rightRelation = rh.getRelation();
//                String left = null;
//                String right = null;
//                result = CompareUtil.compare(leftRelation, rightRelation);
//                if (0 == result) {
//                    left = leftRelation.getForwardNavigationName();
//                    right = rightRelation.getForwardNavigationName();
//                    result = CompareUtil.compare(left, right);
//                }
//                if (0 == result) {
//                    left = leftRelation.getBackNavigationName();
//                    right = rightRelation.getBackNavigationName();
//                    result = CompareUtil.compare(left, right);
//                }
//                if (0 == result) {
//                    left = leftRelation.getAssociationName();
//                    right = rightRelation.getAssociationName();
//                    result = CompareUtil.compare(left, right);
//                }
//                boolean l = false;
//                boolean r = false;
//                if (0 == result) {
//                    l = leftRelation.isSymmetrical();
//                    r = rightRelation.isSymmetrical();
//                    result = CompareUtil.compare(l, r);
//                }
//                if (0 == result) {
//                    l = leftRelation.isTransitive();
//                    r = rightRelation.isTransitive();
//                    result = CompareUtil.compare(l, r);
//                }
//            }
//            return result;
//        }
//    }
//
//    /** Comparator for ProgramElementNode. */
//    static class ProgramElementNodeComparator implements Comparator {
//        public static Comparator SINGLETON = new ProgramElementNodeComparator();
//        private ProgramElementNodeComparator () {}
//        public int compare(Object lhs, Object rhs) {
//            int result = CompareUtil.compare(lhs, rhs);
//            if (Integer.MAX_VALUE == result) {
//                ProgramElementNode lh = (ProgramElementNode) lhs ;
//                ProgramElementNode rh = (ProgramElementNode) rhs ;
//
//                boolean rhStmntKind = rh.isCode();
//                boolean lhStmntKind = lh.isCode();
//                if (lhStmntKind != rhStmntKind) {
//                    return (lhStmntKind ? 1 : -1);
//                }
//                String left= lh.getKind();
//                String right= rh.getKind();
//                // boilerplate
//                result = CompareUtil.compare(left, right);
//                if (Integer.MAX_VALUE == result) {
//                    result = left.compareTo(right);
//                    if (0 != result) return result;
//                }
//                right = rh.getSignature();
//                left = lh.getSignature();
//                result = CompareUtil.compare(left, right);
//                if (Integer.MAX_VALUE == result) {
//                    result = left.compareTo(right);
//                    if (0 != result) return result;
//                }
//                List rightList = rh.getModifiers();
//                List leftList = lh.getModifiers();
//                result = CompareUtil.compare(leftList, rightList, MODIFIERS_LIST);
//                if (0 != result) return result;
//
//                result = compare(rh.getAccessibility(), lh.getAccessibility());
//                if (0 != result) return result;
//
//                right = rh.getDeclaringType();
//                left = lh.getDeclaringType();
//                result = CompareUtil.compare(left, right);
//                if (Integer.MAX_VALUE == result) {
//                    result = left.compareTo(right);
//                    if (0 != result) return result;
//                }
//
//                SourceLocation leftSourceLocation = rh.getSourceLocation();
//                SourceLocation rightSourceLocation = rh.getSourceLocation();
//                int iright= rightSourceLocation.getLineNumber();
//                int ileft= leftSourceLocation.getLineNumber();
//                if (iright != ileft) return (ileft-iright);
//                iright= rightSourceLocation.getColumnNumber();
//                ileft= leftSourceLocation.getColumnNumber();
//                if (iright != ileft) return (ileft-iright);
//
//                right= rh.getFormalComment();
//                left= lh.getFormalComment();
//                if (Integer.MAX_VALUE == result) {
//                    result = left.compareTo(right);
//                    if (0 != result) return result;
//                }
//
//                right = rh.toString();  // ignored? super
//                left = lh.toString();  // ignored? super
//                if (Integer.MAX_VALUE == result) {
//                    result = left.compareTo(right);
//                    if (0 != result) return result;
//                }
//                // ignore source file path - may change?
//                // lhSourceFilePath = lh.getSourceFilePath(); // ignored
//                // lh.sourceFilePath = lh.getSourceFilePath(); // ignored
//                // List rhRelations= rh.getRelations() ; // ignored
//                // List lhRelations= lh.getRelations();  // ignored
//                return 0;
//            }
//            return result;
//        }
//    }
//    
//    /** Comparator for LinkNode. */
//    static class LinkNodeComparator implements Comparator {
//        public static Comparator SINGLETON = new LinkNodeComparator();
//        private LinkNodeComparator () {}
//        public int compare(Object lhs, Object rhs) {
//            int result = CompareUtil.compare(lhs, rhs);
//            if (Integer.MAX_VALUE == result) {
//                LinkNode lh = (LinkNode) lhs ;
//                LinkNode rh = (LinkNode) rhs ;
//                // LinkNode only has child and lexical name in toString
//                result = lh.toString().compareTo(rh.toString());
//            }
//            return result;
//        }
//    }   // class LinkNodeComparator
//
//    /**
//     * Comparator for StructureNode.
//     * <li>todo: implement comparators for each StructureNode subtype</li>
//     */
//    static class StructureNodeComparator implements Comparator {
//        public static Comparator SINGLETON = new StructureNodeComparator();
//        private StructureNodeComparator () {}
//        public int compare(Object lhs, Object rhs) {
//            int result = CompareUtil.compare(lhs, rhs);
//            if (Integer.MAX_VALUE == result) {
//                Class lhClass = lhs.getClass() ;
//                Class rhClass = rhs.getClass() ;
//                if ((StructureNode.class.isAssignableFrom(lhClass))
//                    && (StructureNode.class.isAssignableFrom(rhClass))) {
//                    StructureNode lh = (StructureNode) lhs ;
//                    StructureNode rh = (StructureNode) rhs ;
//                    Object lhObject = lh.getName(); // todo: weak name-based comparison
//                    Object rhObject = rh.getName();
//                    result = CompareUtil.compare(lhs, rhs);
//                    if (Integer.MAX_VALUE == result) {
//                        result = lhObject.toString().compareTo(rhObject.toString());
//                    }
//                } else { // urk - broken unless wrapper
//                    result = lhs.toString().compareTo(rhs.toString());
//                }
//            }
//            return result;
//        }
//    }
//}
