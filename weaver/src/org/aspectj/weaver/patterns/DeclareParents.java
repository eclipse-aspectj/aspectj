/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     Xerox/PARC     initial implementation 
 * ******************************************************************/


package org.aspectj.weaver.patterns;

import java.io.*;
import java.util.List;

import org.aspectj.weaver.*;
import org.aspectj.weaver.ResolvedTypeX;

public class DeclareParents extends Declare {
	private TypePattern child;
	private TypePatternList parents;
	

	public DeclareParents(TypePattern child, List parents) {
		this(child, new TypePatternList(parents));
	}
	
	private DeclareParents(TypePattern child, TypePatternList parents) {
		this.child = child;
		this.parents = parents;
	}
	
	public boolean match(ResolvedTypeX typeX) {
		return child.matchesStatically(typeX);
	}


	
	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("declare parents: ");
		buf.append(child);
		buf.append(" extends ");  //extends and implements are treated equivalently
		buf.append(parents);
		buf.append(";");
		return buf.toString();
	}
	
	public boolean equals(Object other) { 
		if (!(other instanceof DeclareParents)) return false;
		DeclareParents o = (DeclareParents)other;
		return o.child.equals(child) && o.parents.equals(parents);
	}
    
    //??? cache this 
    public int hashCode() {
    	int result = 23;
        result = 37*result + child.hashCode();
        result = 37*result + parents.hashCode();
    	return result;
    }


	public void write(DataOutputStream s) throws IOException {
		s.writeByte(Declare.PARENTS);
		child.write(s);
		parents.write(s);
		writeLocation(s);
	}

	public static Declare read(DataInputStream s, ISourceContext context) throws IOException {
		Declare ret = new DeclareParents(TypePattern.read(s, context), TypePatternList.read(s, context));
		ret.readLocation(context, s);
		return ret;
	}
	
    public void resolve(IScope scope) {
    	child = child.resolveBindings(scope, Bindings.NONE, false);
    	parents = parents.resolveBindings(scope, Bindings.NONE, false); 
    	for (int i=0; i < parents.size(); i++) {
    		parents.get(i).assertExactType(scope.getMessageHandler());
		}
    }

	public TypePatternList getParents() {
		return parents;
	}

	public TypePattern getChild() {
		return child;
	}
	
	public boolean isAdviceLike() {
		return false;
	}

}
