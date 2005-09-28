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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.MessageUtil;
import org.aspectj.util.FuzzyBoolean;
import org.aspectj.weaver.ISourceContext;
import org.aspectj.weaver.IntMap;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.Shadow;
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.VersionedDataInputStream;
import org.aspectj.weaver.WeaverMessages;
import org.aspectj.weaver.ast.Literal;
import org.aspectj.weaver.ast.Test;
import org.aspectj.weaver.ast.Var;

// 

/**
 * Corresponds to target or this pcd.
 * 
 * <p>type is initially a WildTypePattern.  If it stays that way, it's a this(Foo) 
 * type deal.
 * however, the resolveBindings method may convert it to a BindingTypePattern, 
 * in which
 * case, it's a this(foo) type deal.
 * 
 * @author Erik Hilsdale
 * @author Jim Hugunin
 */
public class ThisOrTargetPointcut extends NameBindingPointcut {
	private boolean isThis;
	private TypePattern type;

	private static final Set thisKindSet = new HashSet(Shadow.ALL_SHADOW_KINDS);
	private static final Set targetKindSet = new HashSet(Shadow.ALL_SHADOW_KINDS);
	static {
		for (Iterator iter = Shadow.ALL_SHADOW_KINDS.iterator(); iter.hasNext();) {
			Shadow.Kind kind = (Shadow.Kind) iter.next();
			if (kind.neverHasThis()) thisKindSet.remove(kind);
			if (kind.neverHasTarget()) targetKindSet.remove(kind);
		}
	}

	public ThisOrTargetPointcut(boolean isThis, TypePattern type) {
		this.isThis = isThis;
		this.type = type;
		this.pointcutKind = THIS_OR_TARGET;
	}

    public TypePattern getType() {
        return type;
    }

	public boolean isThis() { return isThis; }

	public Set couldMatchKinds() {
		return isThis ? thisKindSet : targetKindSet;
	}

	public FuzzyBoolean fastMatch(FastMatchInfo type) {
		return FuzzyBoolean.MAYBE;
	}

	private boolean couldMatch(Shadow shadow) {
		return isThis ? shadow.hasThis() : shadow.hasTarget();
	}
    
	protected FuzzyBoolean matchInternal(Shadow shadow) {
		if (!couldMatch(shadow)) return FuzzyBoolean.NO;
		UnresolvedType typeToMatch = isThis ? shadow.getThisType() : shadow.getTargetType(); 
		//if (typeToMatch == ResolvedType.MISSING) return FuzzyBoolean.NO;
		
		return type.matches(typeToMatch.resolve(shadow.getIWorld()), TypePattern.DYNAMIC);
	}

	public void write(DataOutputStream s) throws IOException {
		s.writeByte(Pointcut.THIS_OR_TARGET);
		s.writeBoolean(isThis);
		type.write(s);
		writeLocation(s);
	}
	public static Pointcut read(VersionedDataInputStream s, ISourceContext context) throws IOException {
		boolean isThis = s.readBoolean();
		TypePattern type = TypePattern.read(s, context);
		ThisOrTargetPointcut ret = new ThisOrTargetPointcut(isThis, type);
		ret.readLocation(context, s);
		return ret;
	}

	public void resolveBindings(IScope scope, Bindings bindings) {
		type = type.resolveBindings(scope, bindings, true, true);
		
		// look for parameterized type patterns which are not supported...
		HasThisTypePatternTriedToSneakInSomeGenericOrParameterizedTypePatternMatchingStuffAnywhereVisitor 
			visitor = new HasThisTypePatternTriedToSneakInSomeGenericOrParameterizedTypePatternMatchingStuffAnywhereVisitor();
		type.traverse(visitor, null);
		if (visitor.wellHasItThen/*?*/()) {
			scope.message(MessageUtil.error(WeaverMessages.format(WeaverMessages.THIS_AND_TARGET_DONT_SUPPORT_PARAMETERS),
				getSourceLocation()));
		}		
		// ??? handle non-formal
	}
	
	public void postRead(ResolvedType enclosingType) {
		type.postRead(enclosingType);
	}
	
	/* (non-Javadoc)
	 * @see org.aspectj.weaver.patterns.NameBindingPointcut#getBindingAnnotationTypePatterns()
	 */
	public List getBindingAnnotationTypePatterns() {
		return Collections.EMPTY_LIST;
	}
	
	/* (non-Javadoc)
	 * @see org.aspectj.weaver.patterns.NameBindingPointcut#getBindingTypePatterns()
	 */
	public List getBindingTypePatterns() {
		if (type instanceof BindingTypePattern) {
			List l = new ArrayList();
			l.add(type);
			return l;
		} else return Collections.EMPTY_LIST;
	}

	
	public boolean equals(Object other) {
		if (!(other instanceof ThisOrTargetPointcut)) return false;
		ThisOrTargetPointcut o = (ThisOrTargetPointcut)other;
		return o.isThis == this.isThis && o.type.equals(this.type);
	}
    public int hashCode() {
        int result = 17;
        result = 37*result + (isThis ? 0 : 1);
        result = 37*result + type.hashCode();
        return result;
    }
	public String toString() {
		return (isThis ? "this(" : "target(") + type + ")";
	}

	protected Test findResidueInternal(Shadow shadow, ExposedState state) {
		if (!couldMatch(shadow)) return Literal.FALSE;
		
		if (type == TypePattern.ANY) return Literal.TRUE;
		
		Var var = isThis ? shadow.getThisVar() : shadow.getTargetVar();	

		if (type instanceof BindingTypePattern) {
		  BindingTypePattern btp = (BindingTypePattern)type;
		  // Check if we have already bound something to this formal
		  if ((state.get(btp.getFormalIndex())!=null) && (lastMatchedShadowId != shadow.shadowId)){
//		  	ISourceLocation pcdSloc = getSourceLocation(); 
//		  	ISourceLocation shadowSloc = shadow.getSourceLocation();
//			Message errorMessage = new Message(
//				"Cannot use "+(isThis?"this()":"target()")+" to match at this location and bind a formal to type '"+var.getType()+
//				"' - the formal is already bound to type '"+state.get(btp.getFormalIndex()).getType()+"'"+
//				".  The secondary source location points to the problematic "+(isThis?"this()":"target()")+".",
//				shadowSloc,true,new ISourceLocation[]{pcdSloc}); 
//			shadow.getIWorld().getMessageHandler().handleMessage(errorMessage);
			state.setErroneousVar(btp.getFormalIndex());
			//return null;
		  }
		}
		return exposeStateForVar(var, type, state, shadow.getIWorld());
	}

	public Pointcut concretize1(ResolvedType inAspect, ResolvedType declaringType, IntMap bindings) {
		if (isDeclare(bindings.getEnclosingAdvice())) {
		  // Enforce rule about which designators are supported in declare
		  inAspect.getWorld().showMessage(IMessage.ERROR,
		  		WeaverMessages.format(WeaverMessages.THIS_OR_TARGET_IN_DECLARE,isThis?"this":"target"),
				bindings.getEnclosingAdvice().getSourceLocation(), null);
		  return Pointcut.makeMatchesNothing(Pointcut.CONCRETE);
		}
		
		TypePattern newType = type.remapAdviceFormals(bindings);
		if (inAspect.crosscuttingMembers != null) {
			inAspect.crosscuttingMembers.exposeType(newType.getExactType());
		}
		
		Pointcut ret = new ThisOrTargetPointcut(isThis, newType);
		ret.copyLocationFrom(this);
		return ret;
	}

    public Object accept(PatternNodeVisitor visitor, Object data) {
        return visitor.visit(this, data);
    }
}
