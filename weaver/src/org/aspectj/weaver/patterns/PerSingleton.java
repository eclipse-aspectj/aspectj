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

import java.io.*;
import java.lang.reflect.Modifier;

import org.aspectj.weaver.*;
import org.aspectj.weaver.ast.*;
import org.aspectj.util.FuzzyBoolean;

public class PerSingleton extends PerClause {
	public PerSingleton() {
	}
	
    public FuzzyBoolean match(Shadow shadow) {
        return FuzzyBoolean.YES;
    }

    public void resolveBindings(IScope scope, Bindings bindings) {
    	// this method intentionally left blank
    }

    public Test findResidue(Shadow shadow, ExposedState state) {
    	Expr myInstance =
    		Expr.makeCallExpr(AjcMemberMaker.perSingletonAspectOfMethod(inAspect),
    							Expr.NONE, inAspect);
    	
    	state.setAspectInstance(myInstance);
    	
    	// we have no test
    	// a NoAspectBoundException will be thrown if we need an instance of this
    	// aspect before we are bound
        return Literal.TRUE;
    }

	public PerClause concretize(ResolvedTypeX inAspect) {
		PerSingleton ret = new PerSingleton();
		ret.inAspect = inAspect;
		return ret;
	}

    public void write(DataOutputStream s) throws IOException {
    	SINGLETON.write(s);
    	writeLocation(s);
    }
    
	public static PerClause readPerClause(DataInputStream s, ISourceContext context) throws IOException {
		PerSingleton ret = new PerSingleton();
		ret.readLocation(context, s);
		return ret;
	}
	
	
	public PerClause.Kind getKind() {
		return SINGLETON;
	}
	
	public String toString() {
		return "persingleton(" + inAspect + ")";
	}

}
