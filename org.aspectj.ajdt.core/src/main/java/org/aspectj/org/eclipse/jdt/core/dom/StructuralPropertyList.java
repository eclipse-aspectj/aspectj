/*******************************************************************************
 * Copyright (c) 2026 Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.aspectj.org.eclipse.jdt.core.dom;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Collects the structural property descriptors of one node type. This is what {@code ASTNode.createPropertyList},
 * {@code addProperty} and {@code reapPropertyList} do, but typed: they work on a raw list that has the class of the node as its
 * first element, followed by the descriptors.
 */
final class StructuralPropertyList {

	private final Class<? extends ASTNode> nodeClass;
	private final List<StructuralPropertyDescriptor> descriptors;

	/**
	 * @param nodeClass the class of the node type the descriptors are for
	 * @param capacity the number of descriptors that is expected
	 */
	StructuralPropertyList(Class<? extends ASTNode> nodeClass, int capacity) {
		this.nodeClass = nodeClass;
		this.descriptors = new ArrayList<>(capacity);
	}

	/** @throws RuntimeException if the descriptor is for another node type (a cut and paste mistake, easily made) */
	void add(StructuralPropertyDescriptor property) {
		if (property.getNodeClass() != nodeClass) {
			throw new RuntimeException("Structural property descriptor has wrong node class!"); //$NON-NLS-1$
		}
		descriptors.add(property);
	}

	/** Adds descriptors that a super type has. They are for the node class of the super type, so they are not checked. */
	void addAll(List<?> inherited) {
		for (Object property : inherited) {
			descriptors.add((StructuralPropertyDescriptor) property);
		}
	}

	/** @return an unmodifiable list of the descriptors that were added */
	List<StructuralPropertyDescriptor> reap() {
		return Collections.unmodifiableList(new ArrayList<>(descriptors));
	}
}
