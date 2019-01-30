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
 * ******************************************************************/


/*
 * Node.java created on May 14, 2002
 *
 */
package org.aspectj.testing.util;


import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

/** 
  * A node in a tree containing other Node or SpecElements items.
  */
public class Node { // XXX render
    public static final Node[] EMPTY_NODES = new Node[0];
    public static final Object[] EMPTY_ITEMS = new Object[0];

    /** 
     * Visit all the SpecElements (and Node) reachable from node
     * in depth-first order, halting if checker objects.
     * @param node the Node to pass to checker
     * @param itemChecker the ObjectChecker to pass items to
     * @param nodeVisitor if not null, then use instead of recursing
     * @return false on first objection, true otherwise
     * @throws IllegalArgumentExcpetion if checker is null
     */
    public static final boolean visit(Node node, ObjectChecker itemChecker,
                                         ObjectChecker nodeVisitor) {
        if (null == node) {
            return (null == itemChecker ? true : itemChecker.isValid(null));
        }
        boolean result = true;

        Node[] nodes = node.getNodes();
        for (int i = 0; result && (i < nodes.length); i++) {
            result = (null == nodeVisitor 
                    ? visit(nodes[i], itemChecker, null)
                    : nodeVisitor.isValid(nodes[i]));
        }
        if (result) {
            Object[] elements = node.getItems();
            for (int i = 0; result && (i < elements.length); i++) {
                result = itemChecker.isValid(elements[i]);
            }
        }

        return result;
    }

    public final String name;
    public final Class type;
    public final Object key;
    protected final Object[] typeArray;
    protected final List nodes;
    protected final List items;

    public Node() {
        this("Node");
    }

    public Node(String name) {
        this(name, null);
    }

    /** use the name as the key */
    public Node(String name, Class type) {
        this(name, type, name);
    }
    /**  */
    public Node(String name, Class type, Object key) {    
        if (null == name) {
            throw new IllegalArgumentException("null name");
        }
        if (null == key) {
            throw new IllegalArgumentException("null key");
        }
        this.name = name;
        this.type = type;
        this.key = key;
        nodes = new ArrayList();
        items = new ArrayList();
        if (type == null) {
            type = Object.class;
        }
        typeArray = (Object[]) Array.newInstance(type, 0);
    }

    /** 
     * clear all items and nodes.
     */
    public void clear() { // XXX synchronize
        nodes.clear();
        items.clear();
    }
    
    /**
     * Add item to list of items
     * unless it is null, of the wrong type, or the collection fails to add
     * @return true if added
     */
    public boolean add(Object item) {
        if (null == item)
            throw new IllegalArgumentException("null item");
        if ((null != type) && (!type.isAssignableFrom(item.getClass()))) {
            return false;
        }
        return items.add(item);
    }

    /**
     * Add node to list of nodes
     * unless it is null, of the wrong type, or the collection fails to add
     * @return true if added
     */
    public boolean addNode(Node node) {
        if (null == node) {
            throw new IllegalArgumentException("null node");
        }
        return nodes.add(node);
    }

    /**
     * Get the current list of nodes - never null
     */
    public Node[] getNodes() {
        if ((null == nodes) || (1 > nodes.size())) {
            return EMPTY_NODES;
        }
        return (Node[]) nodes.toArray(EMPTY_NODES);
    }

    /**
     * Get the current list of items - never null
     * @return items in current list, cast to type[] if type was not null
     */
    public Object[] getItems() {
        if ((null == items) || (1 > items.size())) {
            return EMPTY_ITEMS;
        }
        return items.toArray(typeArray);
    }

    /** @return name */
    public String toString() {
        return name;
    }
}
