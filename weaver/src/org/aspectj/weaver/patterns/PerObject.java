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

import org.aspectj.util.FuzzyBoolean;
import org.aspectj.weaver.Advice;
import org.aspectj.weaver.AjcMemberMaker;
import org.aspectj.weaver.ISourceContext;
import org.aspectj.weaver.PerObjectInterfaceTypeMunger;
import org.aspectj.weaver.ResolvedTypeMunger;
import org.aspectj.weaver.ResolvedTypeX;
import org.aspectj.weaver.Shadow;
import org.aspectj.weaver.World;
import org.aspectj.weaver.ast.Expr;
import org.aspectj.weaver.ast.Test;
import org.aspectj.weaver.ast.Var;

public class PerObject extends PerClause {
	private boolean isThis;
	private Pointcut entry;
	
	public PerObject(Pointcut entry, boolean isThis) {
		this.entry = entry;
		this.isThis = isThis;
	}
	
	// -----
	public FuzzyBoolean fastMatch(ResolvedTypeX type) {
		return FuzzyBoolean.MAYBE;
	}
	
	
    public FuzzyBoolean match(Shadow shadow) {
    	//System.err.println("matches " + this + " ? " + shadow + ", " + shadow.hasTarget());
    	//??? could probably optimize this better by testing could match
    	if (isThis) return FuzzyBoolean.fromBoolean(shadow.hasThis());
    	else return FuzzyBoolean.fromBoolean(shadow.hasTarget());
    }

    public void resolveBindings(IScope scope, Bindings bindings) {
    	// assert bindings == null;
    	entry.resolve(scope);  
    }
    
    private Var getVar(Shadow shadow) {
    	return isThis ? shadow.getThisVar() : shadow.getTargetVar();
    }

    public Test findResidue(Shadow shadow, ExposedState state) {
    	Expr myInstance =
    		Expr.makeCallExpr(AjcMemberMaker.perObjectAspectOfMethod(inAspect),
    							new Expr[] {getVar(shadow)}, inAspect);
    	state.setAspectInstance(myInstance);
    	return Test.makeCall(AjcMemberMaker.perObjectHasAspectMethod(inAspect), 
    			new Expr[] { getVar(shadow) });
    }



	public PerClause concretize(ResolvedTypeX inAspect) {
		PerObject ret = new PerObject(entry, isThis);
		
		ret.inAspect = inAspect;
		if (inAspect.isAbstract()) return ret;
		
		
		World world = inAspect.getWorld();
		
		Pointcut concreteEntry = entry.concretize(inAspect, 0, null);
		//concreteEntry = new AndPointcut(this, concreteEntry);
		//concreteEntry.state = Pointcut.CONCRETE;
		inAspect.crosscuttingMembers.addConcreteShadowMunger(
				Advice.makePerObjectEntry(world, concreteEntry, isThis, inAspect));
		ResolvedTypeMunger munger =
			new PerObjectInterfaceTypeMunger(inAspect, concreteEntry);
		inAspect.crosscuttingMembers.addTypeMunger(world.concreteTypeMunger(munger, inAspect));
		return ret;
	}

    public void write(DataOutputStream s) throws IOException {
    	PEROBJECT.write(s);
    	entry.write(s);
    	s.writeBoolean(isThis);
    	writeLocation(s);
    }
    
	public static PerClause readPerClause(DataInputStream s, ISourceContext context) throws IOException {
		PerClause ret = new PerObject(Pointcut.read(s, context), s.readBoolean());
		ret.readLocation(context, s);
		return ret;
	}
	
	public PerClause.Kind getKind() {
		return PEROBJECT;
	}
	
	public String toString() {
		return "per" + (isThis ? "this" : "target") +
			"(" + entry + ")";
	}
}
