/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     PARC     initial implementation 
 * ******************************************************************/


package org.aspectj.weaver.ast;

import org.aspectj.weaver.Member;
import org.aspectj.weaver.ResolvedType;

public abstract class Test extends ASTNode {

    public Test() {
        super();
    }
    
    public abstract void accept(ITestVisitor v);
    
    public static Test makeAnd(Test a, Test b) {
//    	if (a == Literal.NO_TEST) return b;
//    	if (b == Literal.NO_TEST) return a;
        if (a == Literal.TRUE) {
            if (b == Literal.TRUE) {
                return a;
            } else {
                return b;
            }
        } else if (b == Literal.TRUE) {
            return a;
        } else if (a == Literal.FALSE || b == Literal.FALSE) {
            return Literal.FALSE;
        } else {
            return new And(a, b);
        }   
    }
    
    public static Test makeOr(Test a, Test b) {
//    	if (a == Literal.NO_TEST) return a;
//    	if (b == Literal.NO_TEST) return b;
        if (a == Literal.FALSE) {
        	return b;
        } else if (b == Literal.FALSE) {
            return a;
        } else if (a == Literal.TRUE || b == Literal.TRUE) {
            return Literal.TRUE;
        } else {
            return new Or(a, b);
        }   
    }
    
    public static Test makeNot(Test a) {
        if (a instanceof Not) {
            return ((Not) a).getBody();
        } else if (a == Literal.TRUE) {
            return Literal.FALSE;
        } else if (a == Literal.FALSE) {
            return Literal.TRUE;
//        } else if (a == Literal.NO_TEST) {
//        	return a;
        } else {
            return new Not(a);
        }
    }
    
    // uses our special rules that anything matches object
    public static Test makeInstanceof(Var v, ResolvedType ty) {
        if (ty.equals(ResolvedType.OBJECT)) return Literal.TRUE;
        
        Test e;
        if (ty.isAssignableFrom(v.getType())) e = Literal.TRUE;
        else if (! ty.isCoerceableFrom(v.getType())) e = Literal.FALSE;
        else e = new Instanceof(v, ty);
        return e;
    }
    
    public static Test makeHasAnnotation(Var v, ResolvedType annTy) {
        return new HasAnnotation(v,annTy);
    }
    
    public static Test makeCall(Member m, Expr[] args) {
    	return new Call(m, args);
    }
    public static Test makeFieldGetCall(Member f, Member m, Expr[] args) {
    	return new FieldGetCall(f, m, args);
    }
    
}
