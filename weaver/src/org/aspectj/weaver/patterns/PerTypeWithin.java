/* *******************************************************************
 * Copyright (c) 2005 IBM
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     Andy Clement     initial implementation 
 * ******************************************************************/

package org.aspectj.weaver.patterns;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.ISourceLocation;
import org.aspectj.bridge.Message;
import org.aspectj.util.FuzzyBoolean;
import org.aspectj.weaver.Advice;
import org.aspectj.weaver.AjcMemberMaker;
import org.aspectj.weaver.ISourceContext;
import org.aspectj.weaver.Member;
//import org.aspectj.weaver.PerTypeWithinTargetTypeMunger;
import org.aspectj.weaver.PerTypeWithinTargetTypeMunger;
import org.aspectj.weaver.ResolvedTypeMunger;
import org.aspectj.weaver.ResolvedTypeX;
import org.aspectj.weaver.Shadow;
import org.aspectj.weaver.VersionedDataInputStream;
import org.aspectj.weaver.World;
import org.aspectj.weaver.ast.Expr;
import org.aspectj.weaver.ast.Literal;
import org.aspectj.weaver.ast.Test;


// PTWIMPL Represents a parsed pertypewithin()
public class PerTypeWithin extends PerClause {

	private TypePattern typePattern;
	
	// Any shadow could be considered within a pertypewithin() type pattern
	private static final Set kindSet = new HashSet(Shadow.ALL_SHADOW_KINDS);
	
	public TypePattern getTypePattern() {
		return typePattern;
	}
	
	public PerTypeWithin(TypePattern p) {
		this.typePattern = p;
	}

	public Set couldMatchKinds() {
		return kindSet;
	}
	
	// -----
	public FuzzyBoolean fastMatch(FastMatchInfo info) {
		if (typePattern.annotationPattern instanceof AnyAnnotationTypePattern) {
			return isWithinType(info.getType());
		}
		return FuzzyBoolean.MAYBE;
	}
	
	
    protected FuzzyBoolean matchInternal(Shadow shadow) {
    	ResolvedTypeX enclosingType = shadow.getIWorld().resolve(shadow.getEnclosingType(),true);
    	if (enclosingType == ResolvedTypeX.MISSING) {
    		//PTWIMPL ?? Add a proper message
    		IMessage msg = new Message(
    				"Cant find type pertypewithin matching...",
					shadow.getSourceLocation(),true,new ISourceLocation[]{getSourceLocation()});
    		shadow.getIWorld().getMessageHandler().handleMessage(msg);
    	}
    	typePattern.resolve(shadow.getIWorld());
    	return isWithinType(enclosingType);
    }

    public void resolveBindings(IScope scope, Bindings bindings) {
    	typePattern = typePattern.resolveBindings(scope, bindings, false, false);
    }
    
    protected Test findResidueInternal(Shadow shadow, ExposedState state) {
    	Member ptwField = AjcMemberMaker.perTypeWithinField(shadow.getEnclosingType(),inAspect);
    	
    	Expr myInstance =
    		Expr.makeCallExpr(AjcMemberMaker.perTypeWithinLocalAspectOf(shadow.getEnclosingType(),inAspect/*shadow.getEnclosingType()*/),
    				Expr.NONE,inAspect);
    	state.setAspectInstance(myInstance);
    	
    	// this worked at one point
    	//Expr myInstance = Expr.makeFieldGet(ptwField,shadow.getEnclosingType().resolve(shadow.getIWorld()));//inAspect);
    	//state.setAspectInstance(myInstance);
    	
    	
//   	return Test.makeFieldGetCall(ptwField,null,Expr.NONE);
    	// cflowField, cflowCounterIsValidMethod, Expr.NONE
    	
    	// This is what is in the perObject variant of this ...
//    	Expr myInstance =
//    		Expr.makeCallExpr(AjcMemberMaker.perTypeWithinAspectOfMethod(inAspect),
//    							new Expr[] {getVar(shadow)}, inAspect);
//    	state.setAspectInstance(myInstance);
//    	return Test.makeCall(AjcMemberMaker.perTypeWithinHasAspectMethod(inAspect), 
//    			new Expr[] { getVar(shadow) });
//    	

    	
    	 return match(shadow).alwaysTrue()?Literal.TRUE:Literal.FALSE;
    }
    

	public PerClause concretize(ResolvedTypeX inAspect) {
		PerTypeWithin ret = new PerTypeWithin(typePattern);
		ret.copyLocationFrom(this);
		ret.inAspect = inAspect;
		if (inAspect.isAbstract()) return ret;
		
		
		World world = inAspect.getWorld();
		
		SignaturePattern sigpat = new SignaturePattern(
				Member.STATIC_INITIALIZATION,
				ModifiersPattern.ANY,
				TypePattern.ANY,
				typePattern,
				NamePattern.ANY,
				TypePatternList.ANY,
				ThrowsPattern.ANY,
				AnnotationTypePattern.ANY
				);
		Pointcut testPc = new KindedPointcut(Shadow.StaticInitialization,sigpat);
		Pointcut testPc2= new WithinPointcut(typePattern);
		// This munger will initialize the aspect instance field in the matched type
		inAspect.crosscuttingMembers.addConcreteShadowMunger(Advice.makePerTypeWithinEntry(world, testPc, inAspect));
		
		ResolvedTypeMunger munger = new PerTypeWithinTargetTypeMunger(inAspect, ret);
		inAspect.crosscuttingMembers.addTypeMunger(world.concreteTypeMunger(munger, inAspect));
		return ret;
		
	}

    public void write(DataOutputStream s) throws IOException {
    	PERTYPEWITHIN.write(s);
    	typePattern.write(s);
    	writeLocation(s);
    }
    
	public static PerClause readPerClause(VersionedDataInputStream s, ISourceContext context) throws IOException {
		PerClause ret = new PerTypeWithin(TypePattern.read(s, context));
		ret.readLocation(context, s);
		return ret;
	}
	
	public PerClause.Kind getKind() {
		return PERTYPEWITHIN;
	}
	
	public String toString() {
		return "pertypewithin("+typePattern+")";
	}
	
	private FuzzyBoolean isWithinType(ResolvedTypeX type) {
		while (type != null) {
			if (typePattern.matchesStatically(type)) {
				return FuzzyBoolean.YES;
			}
			type = type.getDeclaringType();
		}
		return FuzzyBoolean.NO;
	}
}
