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

import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.ISourceLocation;
import org.aspectj.bridge.Message;
import org.aspectj.bridge.MessageUtil;
import org.aspectj.lang.JoinPoint;
import org.aspectj.util.FuzzyBoolean;
import org.aspectj.weaver.ISourceContext;
import org.aspectj.weaver.IntMap;
import org.aspectj.weaver.ResolvedTypeX;
import org.aspectj.weaver.Shadow;
import org.aspectj.weaver.WeaverMessages;
import org.aspectj.weaver.World;
import org.aspectj.weaver.ast.Literal;
import org.aspectj.weaver.ast.Test;

public class WithinPointcut extends Pointcut {
	TypePattern typePattern;
	
	public WithinPointcut(TypePattern type) {
		this.typePattern = type;
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
	
	public FuzzyBoolean fastMatch(FastMatchInfo info) {
		return isWithinType(info.getType());
	}
    
	public FuzzyBoolean match(Shadow shadow) {
		ResolvedTypeX enclosingType = shadow.getIWorld().resolve(shadow.getEnclosingType(),true);
		if (enclosingType == ResolvedTypeX.MISSING) {
			IMessage msg = new Message(
			    WeaverMessages.format(WeaverMessages.CANT_FIND_TYPE_WITHINPCD,
			    		              shadow.getEnclosingType().getName()),
				shadow.getSourceLocation(),true,new ISourceLocation[]{getSourceLocation()});
			shadow.getIWorld().getMessageHandler().handleMessage(msg);
		}
		return isWithinType(enclosingType);
	}

	public FuzzyBoolean match(JoinPoint jp, JoinPoint.StaticPart encJp) {
		return isWithinType(encJp.getSignature().getDeclaringType());
	}
		
	private FuzzyBoolean isWithinType(Class type) {
		while (type != null) {
			if (typePattern.matchesStatically(type)) {
				return FuzzyBoolean.YES;
			} 
			type = type.getDeclaringClass();
		}		
		return FuzzyBoolean.NO;
	}

	
	public void write(DataOutputStream s) throws IOException {
		s.writeByte(Pointcut.WITHIN);
		typePattern.write(s);
		writeLocation(s);
	}
	public static Pointcut read(DataInputStream s, ISourceContext context) throws IOException {
		TypePattern type = TypePattern.read(s, context);
		WithinPointcut ret = new WithinPointcut(type);
		ret.readLocation(context, s);
		return ret;
	}

	public void resolveBindings(IScope scope, Bindings bindings) {
		typePattern = typePattern.resolveBindings(scope, bindings, false, false);
	}

	public void resolveBindingsFromRTTI() {
		typePattern = typePattern.resolveBindingsFromRTTI(false,false);
	}
	
	public void postRead(ResolvedTypeX enclosingType) {
		typePattern.postRead(enclosingType);
	}

	public boolean equals(Object other) {
		if (!(other instanceof WithinPointcut)) return false;
		WithinPointcut o = (WithinPointcut)other;
		return o.typePattern.equals(this.typePattern);
	}
    public int hashCode() {
        int result = 43;
        result = 37*result + typePattern.hashCode();
        return result;
    }

	public String toString() {
		return "within(" + typePattern + ")";
	}

	public Test findResidue(Shadow shadow, ExposedState state) {
		return match(shadow).alwaysTrue() ? Literal.TRUE : Literal.FALSE;
	}
	
	
	public Pointcut concretize1(ResolvedTypeX inAspect, IntMap bindings) {
		return new WithinPointcut(typePattern);
	}
}
