/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     PARC     initial implementation 
 * ******************************************************************/


package org.aspectj.weaver.patterns;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.aspectj.bridge.IMessage;
import org.aspectj.weaver.ISourceContext;
import org.aspectj.weaver.ResolvedTypeX;
import org.aspectj.weaver.TypeX;
import org.aspectj.weaver.World;

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
		if (!child.matchesStatically(typeX)) return false;
		if (typeX.getWorld().getLint().typeNotExposedToWeaver.isEnabled() &&
				!typeX.isExposedToWeaver())
		{
			typeX.getWorld().getLint().typeNotExposedToWeaver.signal(typeX.getName(), getSourceLocation());
		}
		
		return true;
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
    	child = child.resolveBindings(scope, Bindings.NONE, false, false);
    	parents = parents.resolveBindings(scope, Bindings.NONE, false, true); 
//    	for (int i=0; i < parents.size(); i++) {
//    		parents.get(i).assertExactType(scope.getMessageHandler());
//		}
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
	
	private ResolvedTypeX maybeGetNewParent(ResolvedTypeX targetType, TypePattern typePattern, World world) {
		if (typePattern == TypePattern.NO) return null;  // already had an error here
		TypeX iType = typePattern.getExactType();
		ResolvedTypeX parentType = iType.resolve(world);
			
		if (parentType.isAssignableFrom(targetType)) return null;  // already a parent
					
		if (targetType.isAssignableFrom(parentType)) {
			world.showMessage(IMessage.ERROR,
				"type can not extend itself", this.getSourceLocation(), null
			);
			return null;
		}
					
		if (parentType.isClass()) {
			if (targetType.isInterface()) {
				world.showMessage(IMessage.ERROR, 
					"interface can not extend a class", 
					this.getSourceLocation(), null
				);
				return null;
				// how to handle xcutting errors???
			}
			
			if (!targetType.getSuperclass().isAssignableFrom(parentType)) {
				world.showMessage(IMessage.ERROR,
									"can only insert a class into hierarchy, but "
									+ iType.getName() + " is not a subtype of " +
									targetType.getSuperclass().getName(), 
									this.getSourceLocation(), null
				);
				return null;
			} else {
				return parentType;
			}				
		} else {
			return parentType;
		}
	}
	

	public List/*<ResolvedTypeX>*/ findMatchingNewParents(ResolvedTypeX onType) {
		if (!match(onType)) return Collections.EMPTY_LIST;
		
		List ret = new ArrayList();
		for (int i=0; i < parents.size(); i++) {
			ResolvedTypeX t = maybeGetNewParent(onType, parents.get(i), onType.getWorld());
			if (t != null) ret.add(t);
		}
		
		return ret;
	}

}
