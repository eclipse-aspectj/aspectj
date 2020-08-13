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
import java.util.ArrayList;
import java.util.List;

import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.ISourceLocation;
import org.aspectj.util.FileUtil;

/**
 * @author Mik Kersten
 * 
 *         TODO: clean-up after merging of org.aspectj.asm.StructureNode
 */
public class BuildConfigNode {

	protected BuildConfigNode parent = null;
	protected String name = "";
	protected Kind kind;
	// children.listIterator() should support remove() operation
	protected List<BuildConfigNode> children = new ArrayList<>();
	protected IMessage message = null;
	protected ISourceLocation sourceLocation = null;

	/**
	 * Used during serialization.
	 */
	public BuildConfigNode() {
	}

	// public BuildConfigNode(String name, String kind, String resourcePath, List children) {
	// this(name, kind, children);
	// this.resourcePath = resourcePath;
	// }

	public BuildConfigNode(String name, Kind kind, String resourcePath) {
		this(name, kind);
		this.kind = kind;
		this.resourcePath = resourcePath;
	}

	// public BuildConfigNode(String name, Kind kind, List children) {
	// this.name = name;
	// this.kind = kind;
	// if (children != null) {
	// this.children = children;
	// }
	// setParents();
	// }

	public BuildConfigNode(String name, Kind kind) {
		this.name = name;
		this.kind = kind;
	}

	public String toString() {
		return name;
	}

	public List<BuildConfigNode> getChildren() {
		return children;
	}

	public void addChild(BuildConfigNode child) {
		if (children == null) {
			children = new ArrayList<>();
		}
		children.add(child);
		child.setParent(this);
	}

	public void addChild(int position, BuildConfigNode child) {
		if (children == null) {
			children = new ArrayList<>();
		}
		children.add(position, child);
		child.setParent(this);
	}

	public boolean removeChild(BuildConfigNode child) {
		child.setParent(null);
		return children.remove(child);
	}

	/**
	 * Comparison is string-name based only.
	 */
	public int compareTo(Object o) throws ClassCastException {
		if (o instanceof BuildConfigNode) {
			BuildConfigNode sn = (BuildConfigNode) o;
			return this.getName().compareTo(sn.getName());
		}
		return -1;
	}

	public String getName() {
		return name;
	}

	public ISourceLocation getSourceLocation() {
		return sourceLocation;
	}

	public void setSourceLocation(ISourceLocation sourceLocation) {
		this.sourceLocation = sourceLocation;
	}

	public IMessage getMessage() {
		return message;
	}

	public void setMessage(IMessage message) {
		this.message = message;
	}

	public BuildConfigNode getParent() {
		return parent;
	}

	public void setParent(BuildConfigNode parent) {
		this.parent = parent;
	}

	// private void setParents() {
	// if (children == null) return;
	// for (Iterator it = children.iterator(); it.hasNext(); ) {
	// ((BuildConfigNode)it.next()).setParent(this);
	// }
	// }

	public void setName(String string) {
		name = string;
	}

	private String resourcePath;
	private boolean isActive = true;

	public String getResourcePath() {
		return resourcePath;
	}

	public void setResourcePath(String resourcePath) {
		this.resourcePath = resourcePath;
	}

	public boolean isValidResource() {
		return FileUtil.hasSourceSuffix(name) || name.endsWith(".lst");
	}

	public boolean isActive() {
		return isActive;
	}

	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}

	/**
	 * Uses "typesafe enum" pattern.
	 */
	public static class Kind implements Serializable {

		private static final long serialVersionUID = 3924996793884978885L;

		public static final Kind FILE_JAVA = new Kind("Java source file");
		public static final Kind FILE_ASPECTJ = new Kind("AspectJ source file");
		public static final Kind FILE_LST = new Kind("build configuration file");
		public static final Kind ERROR = new Kind("error");
		public static final Kind DIRECTORY = new Kind("directory");

		public static final Kind[] ALL = { FILE_JAVA, FILE_ASPECTJ, FILE_LST, DIRECTORY };

		private final String name;

		private Kind(String name) {
			this.name = name;
		}

		public String toString() {
			return name;
		}

		// public boolean equals(Object o) {
		// return (o instanceof Kind? this==o : false);
		// // return o.equals(name);
		// }
		//
		// public int hashCode() {
		// return ordinal;
		// // return name.hashCode();
		// }

		public boolean isDeclareKind() {
			return name.startsWith("declare");
		}

		// The 4 declarations below are necessary for serialization
		private static int nextOrdinal = 0;
		private final int ordinal = nextOrdinal++;

		private Object readResolve() throws ObjectStreamException {
			return ALL[ordinal];
		}
	}

	public Kind getBuildConfigNodeKind() {
		return kind;
	}
}
