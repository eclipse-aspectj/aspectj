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
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.List;

import org.aspectj.weaver.*;
import org.aspectj.weaver.ast.*;
import org.aspectj.util.FuzzyBoolean;

public class PerCflow extends PerClause {
	private boolean isBelow;
	private Pointcut entry;
	
	public PerCflow(Pointcut entry, boolean isBelow) {
		this.entry = entry;
		this.isBelow = isBelow;
	}
	
	// -----
	
    public FuzzyBoolean match(Shadow shadow) {
        return FuzzyBoolean.YES;
    }

    public void resolveBindings(IScope scope, Bindings bindings) {
    	// assert bindings == null;
    	entry.resolve(scope);  
    }

    public Test findResidue(Shadow shadow, ExposedState state) {
    	Expr myInstance =
    		Expr.makeCallExpr(AjcMemberMaker.perCflowAspectOfMethod(inAspect),
    							Expr.NONE, inAspect);
    	state.setAspectInstance(myInstance);
    	return Test.makeCall(AjcMemberMaker.perCflowHasAspectMethod(inAspect), Expr.NONE);
    }


	public PerClause concretize(ResolvedTypeX inAspect) {
		PerCflow ret = new PerCflow(entry, isBelow);
		ret.inAspect = inAspect;
		if (inAspect.isAbstract()) return ret;
		
		Member cflowStackField = new ResolvedMember(
			Member.FIELD, inAspect, Modifier.STATIC|Modifier.PUBLIC|Modifier.FINAL,
						TypeX.forName(NameMangler.CFLOW_STACK_TYPE), NameMangler.PERCFLOW_FIELD_NAME, TypeX.NONE);
						
		World world = inAspect.getWorld();
		
		CrosscuttingMembers xcut = inAspect.crosscuttingMembers;
		
		Collection previousCflowEntries = xcut.getCflowEntries();
		Pointcut concreteEntry = entry.concretize(inAspect, IntMap.EMPTY);
		List innerCflowEntries = new ArrayList(xcut.getCflowEntries());
		innerCflowEntries.removeAll(previousCflowEntries);
					
		xcut.addConcreteShadowMunger(
				Advice.makePerCflowEntry(world, concreteEntry, isBelow, cflowStackField, 
									inAspect, innerCflowEntries));
		return ret;
	}

    public void write(DataOutputStream s) throws IOException {
    	PERCFLOW.write(s);
    	entry.write(s);
    	s.writeBoolean(isBelow);
    	writeLocation(s);
    }
    
	public static PerClause readPerClause(DataInputStream s, ISourceContext context) throws IOException {
		PerCflow ret = new PerCflow(Pointcut.read(s, context), s.readBoolean());
		ret.readLocation(context, s);
		return ret;
	}
	
	public PerClause.Kind getKind() {
		return PERCFLOW;
	}
	
	public String toString() {
		return "percflow(" + inAspect + " on " + entry + ")";
	}
}
