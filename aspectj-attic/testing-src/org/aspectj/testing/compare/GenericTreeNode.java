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


package org.aspectj.testing.compare;

import java.util.*;


/** 
 * Generic tree and pairwise traversal over it
 * for comparison purposes.
 * Implement a generic tree by delegation and
 * a traversal method for comparing two trees.
 * Guarantees after initialization:
 * <li>nodeComparator is not null</li>
 * <li>node is not null</li>
 * <li>children, if any, are assignable to childClass 
 *     (only as of initialization, since list is adopted)</li>
 * <li>Obeys GenericTreeNode contract, especially regarding equals</li>
 * <p>It will throw Error if used before initialization completes.
 */
public class GenericTreeNode implements Comparable {
    /** underlying node - never null */
    Object node; 
    /** node children - type-safe if present */
    List children;
    /** node parent - may be null if this is tree root */
    GenericTreeNode parent;
    /** used to compare underlying node */
    Comparator nodeComparator;
    /** Class used to verify children  - may be null */
    Class childClass;

    /** Track whether we are initialized */
    boolean ready;
    /** cache for heavy toString() */
    String toStringCache;

    // --------------------------------------- static
    /** Singleton visitor to match trees exactly, stopping at first failure */
    public static final GenericTreeNodesVisitorI EXACT = new MatchExact();

    /** Singleton visitor to print non-matching nodes to System.err */
    public static final GenericTreeNodesVisitorI PRINTERR = new PrintNonMatches();

    /** Singleton visitor to print all nodes to System.err */
    public static final GenericTreeNodesVisitorI PRINTALL = new PrintAllPairs();


    /** Singleton comparator for all of us */
    public static final Comparator COMPARATOR = new gtnComparator();

    /**
     * Visit two generic trees in order.
     * Visit the parents, and recursively visit the children.
     * The children are visited in an order determined by 
     * (any) ordering factory invoked to produce an ordered pair
     *  of children lists for visiting.
     * All children are visited, even if not paired.
     * When one list of children is smaller, the 
     * remainder are visited with null complements.
     * This is true for parents as well; this will visit
     * all the children of a parent even when the 
     * other parent is null.
     * This will return false without further visits 
     * as soon as the visitor returns false.  That means
     * the visitor can decide to stop visiting when null
     * nodes (parent or children) are encountered).
     * This will return true only after the tree was
     * visited without objection from the visitor.
     * <p>A simple example of using this to determine if
     * two trees are strictly equal is this:
     * @return false if the visitor returned false for any visit;
     *         true otherwise.
     * @throws Error if any children are not GenericTreeNode
     * @throws IllegalArgumentException if the visitor or both parents are null
     */

    public static boolean traverse(GenericTreeNode lhsParent
                                   , GenericTreeNode rhsParent
                                   , GenericTreeNodeListOrdererFactoryI childrenPrepFactory
                                   , GenericTreeNodesVisitorI visitor) {
        if (null == visitor) {
            throw new IllegalArgumentException("null visitor");
        }
        if ((null == lhsParent) && (null == rhsParent)) {
            throw new IllegalArgumentException("null parents");
        }
        if (visitor.visit(lhsParent, rhsParent)) {
            List lhsList = (null == lhsParent ? null : lhsParent.getChildren());
            List rhsList = (null == rhsParent ? null : rhsParent.getChildren());
            if (null != childrenPrepFactory) {
                GenericTreeNodeListOrdererI factory = 
                    childrenPrepFactory.produce(lhsParent, rhsParent, visitor);
                if (null !=  factory) {
                    List[] prepKids = factory.produceLists(lhsList, rhsList);
                    if (null != prepKids) {
                        lhsList = prepKids[0];
                        rhsList = prepKids[1];
                    }
                }
                
            }
            ListIterator lhsIterator = (null == lhsList ? null : lhsList.listIterator());
            ListIterator rhsIterator = (null == rhsList ? null : rhsList.listIterator());
            Object lhs = null;
            Object rhs = null;
            while (true) {
                lhs = null;
                rhs = null;
                // get the child pair
                if ((null != lhsIterator) && (lhsIterator.hasNext())) {
                    lhs = lhsIterator.next();
                }
                if ((null != rhsIterator) && (rhsIterator.hasNext())) {
                    rhs = rhsIterator.next();
                }
                if ((null == rhs) && (null == lhs)) {
                    break;
                }
                if ((null != lhs) && (!(lhs instanceof GenericTreeNode))) {
                    throw new Error("GenericTreeNode expected, got lhs " + lhs);
                } 
                if ((null != rhs) && (!(rhs instanceof GenericTreeNode))) {
                    throw new Error("GenericTreeNode expected, got rhs " + rhs);
                }
                // traverse the child pair
                if (!traverse((GenericTreeNode) lhs, (GenericTreeNode) rhs, 
                              childrenPrepFactory, visitor)) {
                    return false;
                }
            }
        }
        return true; // see also other return statements above
    }     // traverse

    // --------------------------------------- constructors
    public GenericTreeNode() {
        //ready = false;
    }

    public void init(GenericTreeNode parent, Comparator nodeComparator, 
                     Object node, List children, Class childClass) {
        if (ready) ready = false; // weak sync
        if (null == node) {
            throw new IllegalArgumentException("null node");
        }
        if (null == nodeComparator) {
            throw new IllegalArgumentException("null nodeComparator");
        }
        this.nodeComparator = nodeComparator;
        this.node = node;
        this.children = children;
        this.parent = parent;
        if (null != childClass) {
            ListIterator iter = children.listIterator();
            while (iter.hasNext()) {
                Object kid = iter.next();
                if (!(childClass.isAssignableFrom(kid.getClass()))) {
                    String err = "child " + kid + " is not " + childClass;
                    throw new IllegalArgumentException(err);
                }
            }
            this.childClass = childClass;
        }
        ready = true;
    }

    //-------------------- Object interface
    /** 
     * ambiguous: equal if 
     * <li>this is the input, or </li>
     * <li>the input is a GenericTreeNode, and </li>
     * <li>the underlying nodes are equal(), or</li
     * <li>the underlying nodes have equal comparators
     *     which return 0 from compare(...)</li>
     * @param input the Object to compare with
     * @return true if this equals input
     */
    public boolean equals(Object input) {
        if (input == this) {
            return true;
        } else if (!(input instanceof GenericTreeNode)) {
            return false;
        } else {
            GenericTreeNode in = (GenericTreeNode) input;
            if (node.equals(in.getNode())) { // assumes nodes are unique, not flyweights?
                return true;
            } else {
                Comparator inComp = in.getNodeComparator();
                if ((this == nodeComparator) 
                    || (this == inComp)
                    || (nodeComparator == inComp) 
                    || (nodeComparator.equals(inComp))) {
                    return (0 == nodeComparator.compare(this, in));
                } else {
                    return false;
                }
            }
        }
    }

    /** 
     * Delegate to the underlying node object 
     * @return the hashcode of the underlying node object  
     */
    public int hashCode() { // todo: probably not correct to delegate to node
        return node.hashCode();
    }

    /** toString delegates to longString() */ 
    public String toString() {
        if (null == toStringCache) {
            toStringCache = longString();
        }
        return toStringCache;
    }

    /** 
     * short rendition delegates to thisString,
     * prefixing by one tab per parent
     */
    public String shortString() {
        StringBuffer sb = new StringBuffer();
        // add a tab for each parent
        GenericTreeNode par = parent;
        while (null != par) {
            sb.append("\t");
            par = par.getParent();
        } 
        sb.append(thisString());
        return sb.toString();
    }

    /** 
     * long rendition delegates to parentString, thisString,
     * and childString as follows:
     * <pre>GenericTreeNode [parent={parentString()] [children={size}] [Node={thisString()]</pre>
     */
    public String longString() {
        StringBuffer sb = new StringBuffer();
        sb.append("GenericTreeNode ");
        sb.append("[parent=");
        sb.append(parentString());
        sb.append("] [children=");
        if (null == children) {
            sb.append("0");
        } else {
            sb.append("" + children.size());
        }
        sb.append("] [Node=");
        sb.append(thisString());
        sb.append("]");
        return sb.toString();
    }

    /** render this as "[root] [next] ... [parent]" */
    protected String thisString() {
        return node.toString();
    }

    /** render parent hierarchy as "[root] [next] ... [parent]" */
    protected String parentString() {
        if (null == parent) {
            return "[null]";
        } else  {
            Vector parents = new Vector();
            GenericTreeNode par = parent;
            do {
                parents.addElement(par);
                par = par.getParent();
            } while (null != par); 
            int size = parents.size();
            StringBuffer sb = new StringBuffer(32*size);
            par = (GenericTreeNode) parents.remove(--size);
            sb.append("[");
            do {
                sb.append(par.toString());
                par = (size<1?null:(GenericTreeNode) parents.remove(--size));
                if (null != par) {
                    sb.append("] [");
                }
            } while (null != par);
            sb.append("]");
            return sb.toString();
        }
    } // parentString()

    // -------------------------- Comparable interface
    /**
     * Comparable just delegates to Comparator.
     * i.e., <code>return compare(this, rhs)</code>
     * @param rhs the Object to compare from the right 
     * @throws ClassCastException if either is not instanceof GenericTreeNode
     * @return 0 if equal, &gt;0 if this is greater than rhs, &lt;0 otherwise.
     */
    public int compareTo(Object rhs) { 
        return COMPARATOR.compare(this, rhs);
    }


    //-------------------- GenericTreeNode interface
    /** @return the actual underlying node */
    public Object getNode() { assertReady(); return node ; }
    /** @return the actual underlying node */
    public GenericTreeNode getParent() { assertReady(); return parent ; }
    /** @return List of children - null if none */
    public List getChildren() { assertReady(); return children ; }
    /** @return Comparator used for comparing node (not children) */
    public Comparator getNodeComparator() { assertReady(); return nodeComparator; }
    /** @return Class which any children can be assigned to */
    public Class getChildClass() { assertReady(); return childClass; }

    //-------------------- misc
    protected void assertReady() {
        if (!ready) throw new Error("not ready");
    }    

    //-------------------- inner classes
    /**
     * This relies entirely on the GenericTreeNode implementation of equals()
     * which delegates to the underlying node equals() or the comparator
     */
    static class MatchExact implements GenericTreeNodesVisitorI {
        public boolean visit(GenericTreeNode lhs,GenericTreeNode rhs) {
            return (null == lhs ? (null == rhs) : lhs.equals(rhs));
        }
    }

    /**
     * This prints non-matching pairs to System.err,
     * returning true always.
     */
    static class PrintNonMatches implements GenericTreeNodesVisitorI {
        public boolean visit(GenericTreeNode lhs,GenericTreeNode rhs) {
            if (! (null == lhs ? (null == rhs) : lhs.equals(rhs))) {
                System.err.println("[lhs=" + lhs + "] [rhs=" + rhs + "]");
            }
            return true;
        }
    }    

    /**
     * This prints all pairs to System.err, returning true always.
     */
    static class PrintAllPairs implements GenericTreeNodesVisitorI {
        public boolean visit(GenericTreeNode lhs,GenericTreeNode rhs) {
            System.err.println("[lhs=" + lhs + "] [rhs=" + rhs + "]");
            return true;
        }
    }    

    /**
     * have to separate to distinguish
     * gtn.equals() from gtnComparator.equals
     */
    static class gtnComparator implements Comparator {
        public boolean equals(Object o) {
            if (null == o) return false;
            return ( o instanceof gtnComparator );
        }
        public int hashCode() {
            return gtnComparator.class.hashCode();
        }
        // -------------------------- Comparator interface
        /**
         * Shallow compare of two GenericTreeNodes.
         * <li>any null component translates to "less than" another null component<li>
         * <li>comparators must be equal according to
         *   <code>Comparator.equals(Comparator)</code><li>
         * <li>underlying nodes equal according to
         *    <code>Comparator.compare(Object lhs, Object rhs)</code><li>
         * <li>if the two comparators are not equal, then
         *     do arbitrary ordering of the node by toString().compareTo()
         *     of the underlying objects</li>
         * <li>children are ignored.  Two nodes may be equal even if
         *     they do not have the same children.</li>
         * @param lhs the Object to compare from the left 
         * @param rhs the Object to compare from the right 
         * @throws ClassCastException if either is not instanceof GenericTreeNode
         */ 
        public int compare(Object lhs, Object rhs) { 
            int result = CompareUtil.compare(lhs, rhs);
            if (Integer.MAX_VALUE == result) {
                GenericTreeNode lhsNode = (GenericTreeNode) lhs;
                GenericTreeNode rhsNode = (GenericTreeNode) rhs;
                Object lhObject = lhsNode.getNode();
                Object rhObject = rhsNode.getNode();
                result = CompareUtil.compare(lhObject, rhObject); //
                if (Integer.MAX_VALUE == result) {
                    Comparator lhComp = lhsNode.getNodeComparator();
                    Comparator rhComp = rhsNode.getNodeComparator();
                    result = CompareUtil.compare(lhComp, rhComp);
                    if ((Integer.MAX_VALUE == result)
                        || ((0 == result) && (null == lhComp))) {
                        // tricky: comparators are not equal or null, - todo
                        // so unable to determine standard of ordering.
                        // impose a consistent but arbitrary ordering
                        // on the underlying objects
                        System.err.println(" -- result: " + result + " lhComp " + lhComp);
                        result = lhObject.toString().compareTo(rhObject.toString());
                    } else if (0 == result) { // ok to use comparator
                        result = lhComp.compare(lhsNode.getNode(), rhsNode.getNode());
                    } else {
                        String err = "Program error - result: " + result
                            + " lhComp: " + lhComp + " rhComp: " + rhComp;
                        throw new Error(err);
                    }
                }
            }
            return result;
        } // compare 
    }

} // class GenericTreeNode 

