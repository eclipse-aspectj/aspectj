/* *******************************************************************
 * Copyright (c) 1999-2026 Contributors
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *
 * Contributors:
 *     Xerox/PARC     initial implementation
 * ******************************************************************/
package org.aspectj.testing.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A node in a tree containing other Node or SpecElements items.
 */
public class Node<T> {
	public final String name;
	public final Object key;
	protected final List<Node<T>> nodes;
	protected final List<T> items;

	/**
	 * Visit all the SpecElements (and Node) reachable from node in depth-first
	 * order, halting if checker objects.
	 * 
	 * @param node        the Node to pass to checker
	 * @param itemChecker the ObjectChecker to pass items to
	 * @param nodeVisitor if not null, then use instead of recursing
	 * @return false on first objection, true otherwise
	 * @throws IllegalArgumentExcpetion if checker is null
	 */
	public static final <T> boolean visit(Node<T> node, ObjectChecker<? super T> itemChecker,
			ObjectChecker<? super Node<T>> nodeVisitor) {
		if (null == node) {
			return (null == itemChecker ? true : itemChecker.isValid(null));
		}
		boolean result = true;

		for (Node<T> nd: node.nodes) {
			result = (null == nodeVisitor ? visit(nd, itemChecker, null) : nodeVisitor.isValid(nd));
			if (!result) {
				break;
			}
		}
		if (result) {
			for (T item: node.items) {
				result = itemChecker.isValid(item);
				if (!result) {
					break;
				}
			}
		}
		return result;
	}

	public Node() {
		this("Node");
	}

	public Node(String name) {
		this(name, name);
	}

	/**  */
	public Node(String name, Object key) {
		if (null == name) {
			throw new IllegalArgumentException("null name");
		}
		if (null == key) {
			throw new IllegalArgumentException("null key");
		}
		this.name = name;
		this.key = key;
		nodes = new ArrayList<>();
		items = new ArrayList<>();
	}

	public void clear() { // XXX synchronize
		nodes.clear();
		items.clear();
	}

	/**
	 * Add item to list of items unless it is null, of the wrong type, or the
	 * collection fails to add
	 * 
	 * @return true if added
	 */
	public boolean add(T item) {
		if (null == item) {
			throw new IllegalArgumentException("null item");
		}
		return items.add(item);
	}

	/**
	 * Add node to list of nodes unless it is null, of the wrong type, or the
	 * collection fails to add
	 * 
	 * @return true if added
	 */
	public boolean addNode(Node<T> node) {
		if (null == node) {
			throw new IllegalArgumentException("null node");
		}
		return nodes.add(node);
	}

	/**
	 * Get the current list of nodes - never null
	 */
	public List<Node<T>> getNodes() {
		return Collections.unmodifiableList(nodes);
	}

	public List<T> getItems() {
		return Collections.unmodifiableList(items);
	}

	public String toString() {
		return name;
	}
}
