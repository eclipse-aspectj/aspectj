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
import org.aspectj.testing.compare.adapters.StructureGenericTreeNodeFactory;
import org.aspectj.testing.compare.adapters.JTreeNodeGenericTreeNodeFactory;
import org.aspectj.testing.compare.adapters.GenericTreeNodeFactoryI;
// target
// XXX compiler import org.aspectj.asm.StructureNode;
// testing
import javax.swing.tree.TreeNode;
import javax.swing.tree.DefaultMutableTreeNode;
// utils
import java.io.*;

/**
 * Compare two generic trees for tree-equality.
 * Currently does not indicate where or how they failed.
 * Input trees are serialized to disk in a format that
 * is (or can be wrapped using) <code>GenericTreeNode</code>.
 * requires files expected.ser and actual.ser
 * to deserialize to type StructureNode (untested - use Structure)
 * or Swing TreeNode.
 */
public class TreeCompare {
    public static boolean DODIFF;
	/**
	 * @param args ignored - reading expected.ser and actual.ser
	 */
    public static void main(String[] args) {
        TreeCompare me = new TreeCompare();
        File expected = new File("expected.ser");
        File actual = new File("actual.ser");
        if ((args.length > 0)
            || (!expected.exists() || (!actual.exists()))) {
            DODIFF = (args.length > 1);
            takeSnapshot(expected);
            takeSnapshot(actual);
        }
        me.compareSnapshots(expected, actual);
    }
    private static void takeSnapshot(File file) {
        DefaultMutableTreeNode snapshot = getRoot(file) ;
        ObjectOutputStream p = null;
        FileOutputStream ostream = null;
        try {
            ostream = new FileOutputStream(file);
            p = new ObjectOutputStream(ostream);
            p.writeObject(snapshot);
        } catch (IOException e) {
            e.printStackTrace(System.err);
        } finally {
            try {
                if (null != p) p.flush();
                if (null != ostream) ostream.close();
            } catch (IOException o) {} // ignored
        }
    }

    private static DefaultMutableTreeNode getRoot(File file) {
        boolean isActual = (!DODIFF ? false : (-1 != (file.getPath().indexOf("actual"))));
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Edna");
        DefaultMutableTreeNode child = new DefaultMutableTreeNode("Evalyn");
        root.add(child);
        child.add(new DefaultMutableTreeNode("Marsha"));
        child.add(new DefaultMutableTreeNode("Ray"));
        if (DODIFF && isActual) { // Bill added to actual
            child.add(new DefaultMutableTreeNode("Bill"));
        }
        child = new DefaultMutableTreeNode("Clifford");
        root.add(child);
        child.add(new DefaultMutableTreeNode("Terry"));
        if (DODIFF && isActual) { // Peter mispelled in actual
            child.add(new DefaultMutableTreeNode("peter"));
        } else {
            child.add(new DefaultMutableTreeNode("Peter"));
        }
        child.add(new DefaultMutableTreeNode("Mary"));
        child = new DefaultMutableTreeNode("Anastasia");
        root.add(child);
        child.add(new DefaultMutableTreeNode("Victor"));
        child.add(new DefaultMutableTreeNode("Valerie"));
        child.add(new DefaultMutableTreeNode("Valentine"));
        if (DODIFF && isActual) { // Vim added in actual, with a child
            DefaultMutableTreeNode par = new DefaultMutableTreeNode("VimAdded");
            par.add(new DefaultMutableTreeNode("Vim kid"));
            child.add(par);
        }
        return root;
    }

    /**
     * Compare two File by reading in as serialized
     * and selecting the appropriate wrappers for the resulting
     * Object.
     */
    public void compareSnapshots(File expected, File actual) {
        try {
            // construct the respective trees
            FileInputStream efStream = new FileInputStream(expected);
            ObjectInputStream eStream  = new ObjectInputStream(efStream);
            FileInputStream afStream = new FileInputStream(actual);
            ObjectInputStream aStream  = new ObjectInputStream(afStream);
            Object expectedObject = eStream.readObject();
            Object actualObject = aStream.readObject();
            Class expectedObjectClass = (null == expectedObject ? null : expectedObject.getClass());
            Class actualObjectClass = (null == actualObject ? null : actualObject.getClass());
            // todo yuck: switch by type using known factories
// XXX compiler
//            if (StructureNode.class.isAssignableFrom(expectedObjectClass)) {
//				if (StructureNode.class.isAssignableFrom(actualObjectClass)) {
//                    compareSnapshots((StructureNode) expectedObject,(StructureNode) actualObject);
//                    System.err.println("ok");
//                } else {
//                    signalDifferentTypes(expectedObject, actualObject);
//                }
//            } else if (DefaultMutableTreeNode.class.isAssignableFrom(expectedObjectClass)) {
//				if (DefaultMutableTreeNode.class.isAssignableFrom(actualObjectClass)) {
//                    compareSnapshots((DefaultMutableTreeNode) expectedObject,
//									 (DefaultMutableTreeNode) actualObject);
//                } else {
//                    signalDifferentTypes(expectedObject, actualObject);
//                }
//            } else {
                System.err.println("Unrecognized objects - expected: "
                                   + expectedObject + " actual: " + actualObject);
//            }
        } catch (Throwable t) {
            System.err.println("TEST FAILED: " + t.getMessage());
            t.printStackTrace(System.err);
            return;
        }
    } // compareSnapshots(File, File)

    public void signalDifferentTypes(Object lhs, Object rhs) {
        Class lhc = lhs.getClass();
        Class rhc = rhs.getClass();
        String err = "Different Types? lhs: " + lhc + "=" + lhs
            + " rhs: " + rhc + "=" + rhs;
        throw new Error(err);
    }

    /**
     * Compare two StructureNode by wrapping in GenericTreeNode
     */
// XXX compiler
//    public void compareSnapshots(StructureNode expected, StructureNode actual) {
//        try {
//            GenericTreeNodeFactoryI factory =
//                StructureGenericTreeNodeFactory.SINGLETON;
//            // this is the custom part: adapter generating generic model
//            GenericTreeNode expectRoot
//                = factory.createGenericTreeNode(expected, null);
//            GenericTreeNode actualRoot
//                = factory.createGenericTreeNode(actual, null);
//            if (null == actualRoot) System.err.println("null actualRoot");
//            if (null == expectRoot) System.err.println("null expectRoot");
//            compareSnapshots(expectRoot, actualRoot);
//        } catch (Throwable t) {
//            System.err.println("TEST FAILED: " + t.getMessage());
//            t.printStackTrace(System.err);
//            return;
//        }
//    } // compareSnapshots(TreeModel, TreeModel)

    /**
     * Compare two Swing TreeModel by wrapping in GenericTreeNode
     */
    public void compareSnapshots(TreeNode expected, TreeNode actual) {
        try {
            GenericTreeNodeFactoryI factory =
                JTreeNodeGenericTreeNodeFactory.SINGLETON;
            // this is the custom part: adapter generating generic model
            GenericTreeNode expectRoot
                = factory.createGenericTreeNode(expected, null);
            GenericTreeNode actualRoot
                = factory.createGenericTreeNode(actual, null);
            if (null == actualRoot) System.err.println("null actualRoot");
            if (null == expectRoot) System.err.println("null expectRoot");
            compareSnapshots(expectRoot, actualRoot);
        } catch (Throwable t) {
            System.err.println("TEST FAILED: " + t.getMessage());
            t.printStackTrace(System.err);
            return;
        }
    } // compareSnapshots(TreeModel, TreeModel)

    /** Compare GenericTreeNode trees exactly, printing errors */
    public void compareSnapshots(GenericTreeNode expected, GenericTreeNode actual) {
        try {
            //GenericTreeNodesVisitorI visitor = GenericTreeNode.PRINTALL;
            GenericTreeNodesVisitorI visitor = GenericTreeNode.PRINTERR;
            //GenericTreeNodesVisitorI visitor = GenericTreeNode.EXACT;

            if (GenericTreeNode.traverse(expected, actual, null, visitor)) {
                System.err.println("TEST PASSED");
            } else {
                System.err.println("TEST FAILED");
            }
        } catch (Throwable t) {
            System.err.println("TEST FAILED: " + t.getMessage());
            t.printStackTrace(System.err);
            return;
        }
    } // compareSnapshots(GenericTreeNode, GenericTreeNode)
} // TreeCompare

