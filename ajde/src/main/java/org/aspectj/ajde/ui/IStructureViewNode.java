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


package org.aspectj.ajde.ui;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.List;

import org.aspectj.asm.IProgramElement;

/**
 * @author Mik Kersten
 */
public interface IStructureViewNode {

	IProgramElement getStructureNode();

	AbstractIcon getIcon();

	/**
	 * Add a child node.
	 */
	void add(IStructureViewNode child);

	/**
	 * Add a child node.
	 */
	void add(IStructureViewNode child, int position);

	/**
	 * Remove a child node.
	 */
	void remove(IStructureViewNode child);

	/**
	 * @return	an empty list if there are no children
	 */
	List getChildren();

	Kind getKind();

	String getRelationshipName();

	/**
	 * Uses "typesafe enum" pattern.
	 */
	class Kind implements Serializable {

		private static final long serialVersionUID = 6730849292562214877L;

		public static final Kind DECLARATION = new Kind("declaration");
		public static final Kind RELATIONSHIP = new Kind("relationship");
		public static final Kind LINK = new Kind("link");
		public static final Kind[] ALL = { DECLARATION, RELATIONSHIP, LINK };
		private final String name;

		private Kind(String name) {
			this.name = name;
		}

		public String toString() {
			return name;
		}

		// The 4 declarations below are necessary for serialization
		private static int nextOrdinal = 0;
		private final int ordinal = nextOrdinal++;
		private Object readResolve() throws ObjectStreamException {
			return ALL[ordinal];
		}
	}
}
