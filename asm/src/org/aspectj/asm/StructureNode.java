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


package org.aspectj.asm;

import java.util.*;
import java.io.*;

import org.aspectj.bridge.*;
import org.aspectj.bridge.IMessage;

/**
 * Children are non-repeating making the parent-child structure a strict
 * tree.
 * 
 * !!! relies on a java.io.Serializable implementation of ISourceLocation
 * 
 * @author Mik Kersten
 */
public abstract class StructureNode implements Serializable, Comparable {

	protected StructureNode parent = null;
    protected String name = "";
    protected String kind = "";
    protected List children = new ArrayList();
    protected IMessage message = null;
    protected ISourceLocation sourceLocation = null;

    /**
     * Used during serialization.
     */
    public StructureNode() { }

    public StructureNode(String name, String kind, List children) {
        this.name = name;
        this.kind = kind;
        if (children != null) {
	        this.children = children;
        }
     	setParents();
    }

    public StructureNode(String name, String kind) {
        this.name = name;
        this.kind = kind;
    }

	public String toLongString() {
		final StringBuffer buffer = new StringBuffer();
		ModelWalker walker = new ModelWalker() {
			private int depth = 0;
			
			public void preProcess(StructureNode node) { 
				for (int i = 0; i < depth; i++) buffer.append(' ');
				buffer.append(node.toString());
				buffer.append('\n');
				depth += 2;
			}
			
			public void postProcess(StructureNode node) { 
				depth -= 2;
			}
		};
		walker.process(this);
		return buffer.toString();
	}

    public String toString() {
        return  name;
    }

    public String getKind() {
        return kind;
    }

    public List getChildren() {
        return children;
    }

    public void addChild(StructureNode child) {
        if (children == null) {
            children = new ArrayList();
        }
        children.add(child);
        child.setParent(this);
    }
    
    public void addChild(int position, StructureNode child) {
        if (children == null) {
            children = new ArrayList();
        }
        children.add(position, child);
        child.setParent(this);
    }
    
    public boolean removeChild(StructureNode child) {
    	child.setParent(null);
    	return children.remove(child);	
    }

	public StructureNode walk(ModelWalker walker) {
		for (Iterator it = children.iterator(); it.hasNext(); ) {
			StructureNode child = (StructureNode)it.next();
			walker.process(child);	
		} 
		return this;
	}
	
//	public boolean equals(Object o) {
//		if (!(o instanceof StructureNode)) return false;
//		StructureNode sn = (StructureNode)o;
//		return objectEqual(sn.getName(), this.getName())
//			&& objectEqual(sn.getKind(), this.getKind())
//			&& objectEqual(sn.getChildren(), this.getChildren());
//	}
//
//	protected boolean objectEqual(Object o1, Object o2) {
//		return (o1 == null && o2 == null) || (o1 != null && o1.equals(o2));		
//	}

    /**
     * Comparison is string-name based only.
     */
    public int compareTo(Object o) throws ClassCastException {
        if (this == o) {
            return 0;
        } else {
            StructureNode sn = (StructureNode)o;
            return this.getName().compareTo(sn.getName());
        }
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

	public StructureNode getParent() {
		return parent;
	}

	public void setParent(StructureNode parent) {
		this.parent = parent;
	}

	private void setParents() {
		if (children == null) return;
		for (Iterator it = children.iterator(); it.hasNext(); ) {
			((StructureNode)it.next()).setParent(this);	
		}	
	}
//
//    /**
//     * Creates and returns a copy of this object.
//     */
//	public Object clone() {
//		List cloneChildren = new ArrayList();
//		for (Iterator it = children.iterator(); it.hasNext(); ) {
//			cloneChildren.add(((StructureNode)it.next()).clone());	
//		}
//    	StructureNode cloneNode = new StructureNode(name, kind, cloneChildren);
//    	return cloneNode;
//	} 
}


//    private void writeObject(ObjectOutputStream s) throws IOException {
//        s.defaultWriteObject();
//        // customized serialization code
//    }
//
//    private void readObject(ObjectInputStream s) throws IOException  {
//        s.defaultReadObject();
//        // customized deserialization code
//        ...
//        // followed by code to update the object, if necessary
//    }

//    public void writeExternal(ObjectOutput out) throws IOException {
//        if (this instanceof ProgramElementNode) {
//            out.writeInt(1);
//            writeString(name, out);
//            writeString(kind, out);
//            ((ProgramElementNode)this).writeExternal(out);
//        } if (this instanceof RelationNode) {
//            out.writeInt(1);
//            writeString(name, out);
//            writeString(kind, out);
//            ((RelationNode)this).writeExternal(out);
//        } if (this instanceof LinkNode) {
//            out.writeInt(3);
//            writeString(name, out);
//            writeString(kind, out);
//            ((LinkNode)this).writeExternal(out);
//        } else {
//            out.writeInt(0);
//            writeString(name, out);
//            writeString(kind, out);
//        }
//    }
//
//    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
//        int kindint = in.readInt();
//        name = readString(in);
//        kind = readString(in);
//
//        switch (kindint) {
//            case 1:
//                ((StructureNode)it.next()).readExternal(in);
//                break;
//            case 2:
//                ((RelationNode)it.next()).readExternal(in);
//                break;
//            case 3:
//                ((LinkNode)it.next()).readExternal(in);
//                break;
//        }
//    }
//
//    protected void writeString(String s, ObjectOutput out) throws IOException {
//        out.writeInt(s.length());
//        out.write(s.getBytes());
//    }
//
//    protected String readString(ObjectInput in) throws IOException {
//        int length = in.readInt();
//        byte[] nameArray = new byte[length];
//        in.read(nameArray, 0, length);
//        return new String(nameArray);
//    }
//


