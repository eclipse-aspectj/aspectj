/* *******************************************************************
 * Copyright (c) 2004 IBM Corporation.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * ******************************************************************/
package org.aspectj.weaver.patterns;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.MessageUtil;
import org.aspectj.util.FuzzyBoolean;
import org.aspectj.weaver.CompressingDataOutputStream;
import org.aspectj.weaver.ISourceContext;
import org.aspectj.weaver.IntMap;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.Shadow;
import org.aspectj.weaver.ShadowMunger;
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.VersionedDataInputStream;
import org.aspectj.weaver.WeaverMessages;
import org.aspectj.weaver.World;
import org.aspectj.weaver.ast.Literal;
import org.aspectj.weaver.ast.Test;
import org.aspectj.weaver.ast.Var;

/**
 * @author colyer
 * 
 *         TODO To change the template for this generated type comment go to Window - Preferences - Java - Code Style - Code
 *         Templates
 */
public class ThisOrTargetAnnotationPointcut extends NameBindingPointcut {

	private boolean isThis;
	private boolean alreadyWarnedAboutDEoW = false;
	private ExactAnnotationTypePattern annotationTypePattern;
	private String declarationText;

	private static final int thisKindSet;
	private static final int targetKindSet;

	static {
		int thisFlags = Shadow.ALL_SHADOW_KINDS_BITS;
		int targFlags = Shadow.ALL_SHADOW_KINDS_BITS;
		for (int i = 0; i < Shadow.SHADOW_KINDS.length; i++) {
			Shadow.Kind kind = Shadow.SHADOW_KINDS[i];
			if (kind.neverHasThis()) {
				thisFlags -= kind.bit;
			}
			if (kind.neverHasTarget()) {
				targFlags -= kind.bit;
			}
		}
		thisKindSet = thisFlags;
		targetKindSet = targFlags;
	}

	/**
	 * 
	 */
	public ThisOrTargetAnnotationPointcut(boolean isThis, ExactAnnotationTypePattern type) {
		super();
		this.isThis = isThis;
		this.annotationTypePattern = type;
		this.pointcutKind = ATTHIS_OR_TARGET;
		buildDeclarationText();
	}

	public ThisOrTargetAnnotationPointcut(boolean isThis, ExactAnnotationTypePattern type, ShadowMunger munger) {
		this(isThis, type);
	}

	public ExactAnnotationTypePattern getAnnotationTypePattern() {
		return annotationTypePattern;
	}

	@Override
	public int couldMatchKinds() {
		return isThis ? thisKindSet : targetKindSet;
	}

	@Override
	public Pointcut parameterizeWith(Map typeVariableMap, World w) {
		ExactAnnotationTypePattern newPattern = (ExactAnnotationTypePattern) this.annotationTypePattern.parameterizeWith(
				typeVariableMap, w);
		if (newPattern.getAnnotationType() instanceof ResolvedType) {
			verifyRuntimeRetention(newPattern.getResolvedAnnotationType());
		}
		ThisOrTargetAnnotationPointcut ret = new ThisOrTargetAnnotationPointcut(isThis,
				(ExactAnnotationTypePattern) annotationTypePattern.parameterizeWith(typeVariableMap, w));
		ret.copyLocationFrom(this);
		return ret;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.aspectj.weaver.patterns.Pointcut#fastMatch(org.aspectj.weaver.patterns.FastMatchInfo)
	 */
	@Override
	public FuzzyBoolean fastMatch(FastMatchInfo info) {
		return FuzzyBoolean.MAYBE;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.aspectj.weaver.patterns.Pointcut#match(org.aspectj.weaver.Shadow)
	 */
	@Override
	protected FuzzyBoolean matchInternal(Shadow shadow) {
		if (!couldMatch(shadow)) {
			return FuzzyBoolean.NO;
		}
		ResolvedType toMatchAgainst = (isThis ? shadow.getThisType() : shadow.getTargetType()).resolve(shadow.getIWorld());
		annotationTypePattern.resolve(shadow.getIWorld());
		if (annotationTypePattern.matchesRuntimeType(toMatchAgainst).alwaysTrue()) {
			return FuzzyBoolean.YES;
		} else {
			// a subtype may match at runtime
			return FuzzyBoolean.MAYBE;
		}
	}

	public boolean isThis() {
		return isThis;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.aspectj.weaver.patterns.Pointcut#resolveBindings(org.aspectj.weaver.patterns.IScope,
	 * org.aspectj.weaver.patterns.Bindings)
	 */
	@Override
	protected void resolveBindings(IScope scope, Bindings bindings) {
		if (!scope.getWorld().isInJava5Mode()) {
			scope.message(MessageUtil.error(WeaverMessages.format(isThis ? WeaverMessages.ATTHIS_ONLY_SUPPORTED_AT_JAVA5_LEVEL
					: WeaverMessages.ATTARGET_ONLY_SUPPORTED_AT_JAVA5_LEVEL), getSourceLocation()));
			return;
		}
		annotationTypePattern = (ExactAnnotationTypePattern) annotationTypePattern.resolveBindings(scope, bindings, true);
		// must be either a Var, or an annotation type pattern
		// if annotationType does not have runtime retention, this is an error
		if (annotationTypePattern.annotationType == null) {
			// it's a formal with a binding error
			return;
		}
		ResolvedType rAnnotationType = (ResolvedType) annotationTypePattern.annotationType;
		if (rAnnotationType.isTypeVariableReference()) {
			return; // we'll deal with this next check when the type var is actually bound...
		}
		verifyRuntimeRetention(rAnnotationType);

	}

	private void verifyRuntimeRetention(ResolvedType rAnnotationType) {
		if (!(rAnnotationType.isAnnotationWithRuntimeRetention())) {
			IMessage m = MessageUtil.error(WeaverMessages.format(WeaverMessages.BINDING_NON_RUNTIME_RETENTION_ANNOTATION,
					rAnnotationType.getName()), getSourceLocation());
			rAnnotationType.getWorld().getMessageHandler().handleMessage(m);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.aspectj.weaver.patterns.Pointcut#concretize1(org.aspectj.weaver.ResolvedType, org.aspectj.weaver.IntMap)
	 */
	@Override
	protected Pointcut concretize1(ResolvedType inAspect, ResolvedType declaringType, IntMap bindings) {
		if (isDeclare(bindings.getEnclosingAdvice())) {
			// Enforce rule about which designators are supported in declare
			if (!alreadyWarnedAboutDEoW) {
				inAspect.getWorld().showMessage(IMessage.ERROR,
						WeaverMessages.format(WeaverMessages.THIS_OR_TARGET_IN_DECLARE, isThis ? "this" : "target"),
						bindings.getEnclosingAdvice().getSourceLocation(), null);
				alreadyWarnedAboutDEoW = true;
			}
			return Pointcut.makeMatchesNothing(Pointcut.CONCRETE);
		}

		ExactAnnotationTypePattern newType = (ExactAnnotationTypePattern) annotationTypePattern.remapAdviceFormals(bindings);
		ThisOrTargetAnnotationPointcut ret = new ThisOrTargetAnnotationPointcut(isThis, newType, bindings.getEnclosingAdvice());
		ret.alreadyWarnedAboutDEoW = alreadyWarnedAboutDEoW;
		ret.copyLocationFrom(this);
		return ret;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.aspectj.weaver.patterns.Pointcut#findResidue(org.aspectj.weaver.Shadow, org.aspectj.weaver.patterns.ExposedState)
	 */
	/**
	 * The guard here is going to be the hasAnnotation() test - if it gets through (which we cannot determine until runtime) then we
	 * must have a TypeAnnotationAccessVar in place - this means we must *always* have one in place.
	 */
	@Override
	protected Test findResidueInternal(Shadow shadow, ExposedState state) {
		if (!couldMatch(shadow)) {
			return Literal.FALSE;
		}
		boolean alwaysMatches = match(shadow).alwaysTrue();
		Var var = isThis ? shadow.getThisVar() : shadow.getTargetVar();
		Var annVar = null;

		// Are annotations being bound?
		UnresolvedType annotationType = annotationTypePattern.annotationType;
		if (annotationTypePattern instanceof BindingAnnotationTypePattern) {
			BindingAnnotationTypePattern btp = (BindingAnnotationTypePattern) annotationTypePattern;
			annotationType = btp.annotationType;

			annVar = isThis ? shadow.getThisAnnotationVar(annotationType) : shadow.getTargetAnnotationVar(annotationType);
			if (annVar == null) {
				throw new RuntimeException("Impossible!");
			}

			state.set(btp.getFormalIndex(), annVar);
		}

		if (alwaysMatches && (annVar == null)) {// change check to verify if its the 'generic' annVar that is being used
			return Literal.TRUE;
		} else {
			ResolvedType rType = annotationType.resolve(shadow.getIWorld());
			return Test.makeHasAnnotation(var, rType);
		}
	}

	private boolean couldMatch(Shadow shadow) {
		return isThis ? shadow.hasThis() : shadow.hasTarget();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.aspectj.weaver.patterns.NameBindingPointcut#getBindingAnnotationTypePatterns()
	 */
	@Override
	public List getBindingAnnotationTypePatterns() {
		if (annotationTypePattern instanceof BindingAnnotationTypePattern) {
			List l = new ArrayList();
			l.add(annotationTypePattern);
			return l;
		} else {
			return Collections.EMPTY_LIST;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.aspectj.weaver.patterns.NameBindingPointcut#getBindingTypePatterns()
	 */
	@Override
	public List getBindingTypePatterns() {
		return Collections.EMPTY_LIST;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.aspectj.weaver.patterns.PatternNode#write(java.io.DataOutputStream)
	 */
	@Override
	public void write(CompressingDataOutputStream s) throws IOException {
		s.writeByte(Pointcut.ATTHIS_OR_TARGET);
		s.writeBoolean(isThis);
		annotationTypePattern.write(s);
		writeLocation(s);
	}

	public static Pointcut read(VersionedDataInputStream s, ISourceContext context) throws IOException {
		boolean isThis = s.readBoolean();
		AnnotationTypePattern type = AnnotationTypePattern.read(s, context);
		ThisOrTargetAnnotationPointcut ret = new ThisOrTargetAnnotationPointcut(isThis, (ExactAnnotationTypePattern) type);
		ret.readLocation(context, s);
		return ret;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof ThisOrTargetAnnotationPointcut)) {
			return false;
		}
		ThisOrTargetAnnotationPointcut other = (ThisOrTargetAnnotationPointcut) obj;
		return (other.annotationTypePattern.equals(this.annotationTypePattern) && (other.isThis == this.isThis));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return 17 + 37 * annotationTypePattern.hashCode() + (isThis ? 49 : 13);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	private void buildDeclarationText() {
		StringBuffer buf = new StringBuffer();
		buf.append(isThis ? "@this(" : "@target(");
		String annPatt = annotationTypePattern.toString();
		buf.append(annPatt.startsWith("@") ? annPatt.substring(1) : annPatt);
		buf.append(")");
		this.declarationText = buf.toString();
	}

	@Override
	public String toString() {
		return this.declarationText;
	}

	@Override
	public Object accept(PatternNodeVisitor visitor, Object data) {
		return visitor.visit(this, data);
	}
}
