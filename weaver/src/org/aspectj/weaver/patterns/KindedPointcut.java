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

import org.aspectj.bridge.ISourceLocation;
import org.aspectj.lang.JoinPoint;
import org.aspectj.util.FuzzyBoolean;
import org.aspectj.weaver.Checker;
import org.aspectj.weaver.ISourceContext;
import org.aspectj.weaver.IntMap;
import org.aspectj.weaver.Member;
import org.aspectj.weaver.ResolvedTypeX;
import org.aspectj.weaver.Shadow;
import org.aspectj.weaver.ShadowMunger;
import org.aspectj.weaver.TypeX;
import org.aspectj.weaver.World;
import org.aspectj.weaver.ast.Literal;
import org.aspectj.weaver.ast.Test;

public class KindedPointcut extends Pointcut {
	Shadow.Kind kind;
	SignaturePattern signature;
    
    private ShadowMunger munger = null; // only set after concretization

    public KindedPointcut(
        Shadow.Kind kind,
        SignaturePattern signature) {
        this.kind = kind;
        this.signature = signature;
    }
    public KindedPointcut(
        Shadow.Kind kind,
        SignaturePattern signature,
        ShadowMunger munger)
    {
        this(kind, signature);
        this.munger = munger;
    }
	
    public FuzzyBoolean fastMatch(FastMatchInfo info) {
    	if (info.getKind() != null) {
			if (info.getKind() != kind) return FuzzyBoolean.NO;
    	}

		return FuzzyBoolean.MAYBE;
	}	
	
	public FuzzyBoolean match(Shadow shadow) {
		if (shadow.getKind() != kind) return FuzzyBoolean.NO;
		
		if (!signature.matches(shadow.getSignature(), shadow.getIWorld())){
            if(kind == Shadow.MethodCall) {
                warnOnConfusingSig(shadow);
            }
            return FuzzyBoolean.NO; 
        }

		return FuzzyBoolean.YES;
	}
	
	public FuzzyBoolean match(JoinPoint.StaticPart jpsp) {
		if (jpsp.getKind().equals(kind.getName())) {
			if (signature.matches(jpsp)) {
				return FuzzyBoolean.YES;
			}
		}
		return FuzzyBoolean.NO;
	}
	
	private void warnOnConfusingSig(Shadow shadow) {
        // no warnings for declare error/warning
        if (munger instanceof Checker) return;
        
        World world = shadow.getIWorld();
        
		// warning never needed if the declaring type is any
		TypeX exactDeclaringType = signature.getDeclaringType().getExactType();
        
		ResolvedTypeX shadowDeclaringType =
			shadow.getSignature().getDeclaringType().resolve(world);
        
		if (signature.getDeclaringType().isStar()
			|| exactDeclaringType== ResolvedTypeX.MISSING)
			return;

        // warning not needed if match type couldn't ever be the declaring type
		if (!shadowDeclaringType.isAssignableFrom(exactDeclaringType)) {
            return;
		}

		// if the method in the declaring type is *not* visible to the
		// exact declaring type then warning not needed.
		int shadowModifiers = shadow.getSignature().getModifiers(world);
		if (!ResolvedTypeX
			.isVisible(
				shadowModifiers,
				shadowDeclaringType,
				exactDeclaringType.resolve(world))) {
			return;
		}

		SignaturePattern nonConfusingPattern =
			new SignaturePattern(
				signature.getKind(),
				signature.getModifiers(),
				signature.getReturnType(),
				TypePattern.ANY,
				signature.getName(), 
				signature.getParameterTypes(),
				signature.getThrowsPattern());

		if (nonConfusingPattern
			.matches(shadow.getSignature(), shadow.getIWorld())) {
                shadow.getIWorld().getLint().unmatchedSuperTypeInCall.signal(
                    new String[] {
                        shadow.getSignature().getDeclaringType().toString(),
                        signature.getDeclaringType().toString()
                    },
                    this.getSourceLocation(),
                    new ISourceLocation[] {shadow.getSourceLocation()} );               
		}
	}

	public boolean equals(Object other) {
		if (!(other instanceof KindedPointcut)) return false;
		KindedPointcut o = (KindedPointcut)other;
		return o.kind == this.kind && o.signature.equals(this.signature);
	}
    
    public int hashCode() {
        int result = 17;
        result = 37*result + kind.hashCode();
        result = 37*result + signature.hashCode();
        return result;
    }
	
	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append(kind.getSimpleName());
		buf.append("(");
		buf.append(signature.toString());
		buf.append(")");
		return buf.toString();
	}
	
	
	public void postRead(ResolvedTypeX enclosingType) {
		signature.postRead(enclosingType);
	}

	public void write(DataOutputStream s) throws IOException {
		s.writeByte(Pointcut.KINDED);
		kind.write(s);
		signature.write(s);
		writeLocation(s);
	}
	
	public static Pointcut read(DataInputStream s, ISourceContext context) throws IOException {
		Shadow.Kind kind = Shadow.Kind.read(s);
		SignaturePattern sig = SignaturePattern.read(s, context);
		KindedPointcut ret = new KindedPointcut(kind, sig);
		ret.readLocation(context, s);
		return ret;
	}

	// XXX note: there is no namebinding in any kinded pointcut.
	// still might want to do something for better error messages
	// We want to do something here to make sure we don't sidestep the parameter
	// list in capturing type identifiers.
	public void resolveBindings(IScope scope, Bindings bindings) {
		if (kind == Shadow.Initialization) {
//			scope.getMessageHandler().handleMessage(
//				MessageUtil.error(
//					"initialization unimplemented in 1.1beta1",
//					this.getSourceLocation()));
		}
		signature = signature.resolveBindings(scope, bindings);
		
		
		if (kind == Shadow.ConstructorExecution) { 		// Bug fix 60936
		  if (signature.getDeclaringType() != null) {
			World world = scope.getWorld();
			TypeX exactType = signature.getDeclaringType().getExactType();
			if (signature.getKind() == Member.CONSTRUCTOR &&
				!exactType.equals(ResolvedTypeX.MISSING) &&
				exactType.isInterface(world) &&
				!signature.getDeclaringType().isIncludeSubtypes()) {
					world.getLint().noInterfaceCtorJoinpoint.signal(exactType.toString(), getSourceLocation());
				}
		  }
		}
	}
	
	public void resolveBindingsFromRTTI() {
		signature = signature.resolveBindingsFromRTTI();
	}
	
	public Test findResidue(Shadow shadow, ExposedState state) {
		return match(shadow).alwaysTrue() ? Literal.TRUE : Literal.FALSE;
	}
	
	public Pointcut concretize1(ResolvedTypeX inAspect, IntMap bindings) {
		Pointcut ret = new KindedPointcut(kind, signature, bindings.getEnclosingAdvice());
        ret.copyLocationFrom(this);
        return ret;
	}

	public Shadow.Kind getKind() {
		return kind;
	}

}
