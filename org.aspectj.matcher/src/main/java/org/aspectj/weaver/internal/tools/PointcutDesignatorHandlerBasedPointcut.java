/* *******************************************************************
 * Copyright (c) 2005 Contributors.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *   Adrian Colyer			Initial implementation
 * ******************************************************************/
package org.aspectj.weaver.internal.tools;

import java.io.IOException;
import java.util.Map;

import org.aspectj.util.FuzzyBoolean;
import org.aspectj.weaver.CompressingDataOutputStream;
import org.aspectj.weaver.IntMap;
import org.aspectj.weaver.ReferenceType;
import org.aspectj.weaver.ReferenceTypeDelegate;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.Shadow;
import org.aspectj.weaver.World;
import org.aspectj.weaver.ast.Literal;
import org.aspectj.weaver.ast.Test;
import org.aspectj.weaver.patterns.Bindings;
import org.aspectj.weaver.patterns.ExposedState;
import org.aspectj.weaver.patterns.FastMatchInfo;
import org.aspectj.weaver.patterns.IScope;
import org.aspectj.weaver.patterns.PatternNodeVisitor;
import org.aspectj.weaver.patterns.Pointcut;
import org.aspectj.weaver.reflect.ReflectionBasedReferenceTypeDelegate;
import org.aspectj.weaver.reflect.ReflectionFastMatchInfo;
import org.aspectj.weaver.reflect.ReflectionShadow;
import org.aspectj.weaver.reflect.ReflectionWorld;
import org.aspectj.weaver.tools.ContextBasedMatcher;
import org.aspectj.weaver.tools.MatchingContext;

/**
 * Implementation of Pointcut that is backed by a user-extension pointcut designator handler.
 * 
 */
public class PointcutDesignatorHandlerBasedPointcut extends Pointcut {

	private final ContextBasedMatcher matcher;
	private final World world;

	public PointcutDesignatorHandlerBasedPointcut(ContextBasedMatcher expr, World world) {
		this.matcher = expr;
		this.world = world;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.aspectj.weaver.patterns.Pointcut#getPointcutKind()
	 */
	public byte getPointcutKind() {
		return Pointcut.USER_EXTENSION;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.aspectj.weaver.patterns.Pointcut#fastMatch(org.aspectj.weaver.patterns.FastMatchInfo)
	 */
	public FuzzyBoolean fastMatch(FastMatchInfo info) {
		if (info instanceof ReflectionFastMatchInfo) {
			// Really need a reflectionworld here...
			if (!(world instanceof ReflectionWorld)) {
				throw new IllegalStateException("Can only match user-extension pcds with a ReflectionWorld");
			}
			Class<?> clazz = null;
			try {
				clazz = Class.forName(info.getType().getName(), false, ((ReflectionWorld) world).getClassLoader());
			} catch (ClassNotFoundException cnfe) {
				if (info.getType() instanceof ReferenceType) {
					ReferenceTypeDelegate rtd = ((ReferenceType)info.getType()).getDelegate();
					if (rtd instanceof ReflectionBasedReferenceTypeDelegate) {
						clazz = ((ReflectionBasedReferenceTypeDelegate)rtd).getClazz();
					}
				}					
			}
			if (clazz == null) {
				return FuzzyBoolean.MAYBE;
			}
			return FuzzyBoolean.fromBoolean(this.matcher.couldMatchJoinPointsInType(clazz, ((ReflectionFastMatchInfo) info).getMatchingContext()));
		}
		throw new IllegalStateException("Can only match user-extension pcds against Reflection FastMatchInfo objects");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.aspectj.weaver.patterns.Pointcut#couldMatchKinds()
	 */
	public int couldMatchKinds() {
		return Shadow.ALL_SHADOW_KINDS_BITS;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.aspectj.weaver.patterns.Pointcut#matchInternal(org.aspectj.weaver.Shadow)
	 */
	protected FuzzyBoolean matchInternal(Shadow shadow) {
		if (shadow instanceof ReflectionShadow) {
			MatchingContext context = ((ReflectionShadow) shadow).getMatchingContext();
			org.aspectj.weaver.tools.FuzzyBoolean match = this.matcher.matchesStatically(context);
			if (match == org.aspectj.weaver.tools.FuzzyBoolean.MAYBE) {
				return FuzzyBoolean.MAYBE;
			} else if (match == org.aspectj.weaver.tools.FuzzyBoolean.YES) {
				return FuzzyBoolean.YES;
			} else if (match == org.aspectj.weaver.tools.FuzzyBoolean.NO) {
				return FuzzyBoolean.NO;
			}
		}
		throw new IllegalStateException("Can only match user-extension pcds against Reflection shadows (not BCEL)");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.aspectj.weaver.patterns.Pointcut#resolveBindings(org.aspectj.weaver.patterns.IScope,
	 * org.aspectj.weaver.patterns.Bindings)
	 */
	protected void resolveBindings(IScope scope, Bindings bindings) {
		// no-op
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.aspectj.weaver.patterns.Pointcut#concretize1(org.aspectj.weaver.ResolvedType, org.aspectj.weaver.ResolvedType,
	 * org.aspectj.weaver.IntMap)
	 */
	protected Pointcut concretize1(ResolvedType inAspect, ResolvedType declaringType, IntMap bindings) {
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.aspectj.weaver.patterns.Pointcut#findResidueInternal(org.aspectj.weaver.Shadow,
	 * org.aspectj.weaver.patterns.ExposedState)
	 */
	protected Test findResidueInternal(Shadow shadow, ExposedState state) {
		if (!this.matcher.mayNeedDynamicTest()) {
			return Literal.TRUE;
		} else {
			// could be more efficient here!
			matchInternal(shadow);
			return new MatchingContextBasedTest(this.matcher);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.aspectj.weaver.patterns.Pointcut#parameterizeWith(java.util.Map)
	 */
	public Pointcut parameterizeWith(Map typeVariableMap, World w) {
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.aspectj.weaver.patterns.PatternNode#write(java.io.DataOutputStream)
	 */
	public void write(CompressingDataOutputStream s) throws IOException {
		throw new UnsupportedOperationException("can't write custom pointcut designator expressions to stream");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.aspectj.weaver.patterns.PatternNode#accept(org.aspectj.weaver.patterns.PatternNodeVisitor, java.lang.Object)
	 */
	public Object accept(PatternNodeVisitor visitor, Object data) {
		// visitor.visit(this);
		// no-op?
		return data;
	}

}
