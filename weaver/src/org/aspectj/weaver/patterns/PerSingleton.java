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

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Set;

import org.aspectj.util.FuzzyBoolean;
import org.aspectj.weaver.AjcMemberMaker;
import org.aspectj.weaver.ISourceContext;
import org.aspectj.weaver.ResolvedTypeX;
import org.aspectj.weaver.Shadow;
import org.aspectj.weaver.VersionedDataInputStream;
import org.aspectj.weaver.bcel.BcelAccessForInlineMunger;
import org.aspectj.weaver.ast.Expr;
import org.aspectj.weaver.ast.Literal;
import org.aspectj.weaver.ast.Test;
import org.aspectj.weaver.ataspectj.Ajc5MemberMaker;

public class PerSingleton extends PerClause {
	public PerSingleton() {
	}

	public Set couldMatchKinds() {
		return Shadow.ALL_SHADOW_KINDS;
	}

	public FuzzyBoolean fastMatch(FastMatchInfo type) {
		return FuzzyBoolean.YES;
	}
	
    protected FuzzyBoolean matchInternal(Shadow shadow) {
        return FuzzyBoolean.YES;
    }

    public void resolveBindings(IScope scope, Bindings bindings) {
    	// this method intentionally left blank
    }

    public Test findResidueInternal(Shadow shadow, ExposedState state) {
        // TODO: the commented code is for slow Aspects.aspectOf() style - keep or remove
        //
        //    	Expr myInstance =
        //    		Expr.makeCallExpr(AjcMemberMaker.perSingletonAspectOfMethod(inAspect),
        //    							Expr.NONE, inAspect);
        //
        //    	state.setAspectInstance(myInstance);
        //
        //    	// we have no test
        //    	// a NoAspectBoundException will be thrown if we need an instance of this
        //    	// aspect before we are bound
        //        return Literal.TRUE;
//        if (!Ajc5MemberMaker.isSlowAspect(inAspect)) {
            Expr myInstance =
                Expr.makeCallExpr(AjcMemberMaker.perSingletonAspectOfMethod(inAspect),
                                    Expr.NONE, inAspect);

            state.setAspectInstance(myInstance);

            // we have no test
            // a NoAspectBoundException will be thrown if we need an instance of this
            // aspect before we are bound
            return Literal.TRUE;
//        } else {
//            CallExpr callAspectOf =Expr.makeCallExpr(
//                    Ajc5MemberMaker.perSingletonAspectOfMethod(inAspect),
//                    new Expr[]{
//                        Expr.makeStringConstantExpr(inAspect.getName(), inAspect),
//                        //FieldGet is using ResolvedType and I don't need that here
//                        new FieldGetOn(Member.ajClassField, shadow.getEnclosingType())
//                    },
//                    inAspect
//            );
//            Expr castedCallAspectOf = new CastExpr(callAspectOf, inAspect.getName());
//            state.setAspectInstance(castedCallAspectOf);
//            return Literal.TRUE;
//        }
    }

	public PerClause concretize(ResolvedTypeX inAspect) {
		PerSingleton ret = new PerSingleton();

        ret.copyLocationFrom(this);

		ret.inAspect = inAspect;

        //ATAJ: add a munger to add the aspectOf(..) to the @AJ aspects
        if (!inAspect.isAbstract() && Ajc5MemberMaker.isAnnotationStyleAspect(inAspect)) {
            //TODO will those change be ok if we add a serializable aspect ?
            // dig: "can't be Serializable/Cloneable unless -XserializableAspects"
            inAspect.crosscuttingMembers.addLateTypeMunger(
                    inAspect.getWorld().makePerClauseAspect(inAspect, getKind())
            );
        }

        //ATAJ inline around advice support
        if (Ajc5MemberMaker.isAnnotationStyleAspect(inAspect)) {
            inAspect.crosscuttingMembers.addLateTypeMunger(new BcelAccessForInlineMunger(inAspect));
        }

        return ret;
	}

    public void write(DataOutputStream s) throws IOException {
    	SINGLETON.write(s);
    	writeLocation(s);
    }
    
	public static PerClause readPerClause(VersionedDataInputStream s, ISourceContext context) throws IOException {
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
	
	public String toDeclarationString() {
		return "";
	}

}
