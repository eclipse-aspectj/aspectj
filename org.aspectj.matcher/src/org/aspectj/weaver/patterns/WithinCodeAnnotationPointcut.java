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

import org.aspectj.bridge.MessageUtil;
import org.aspectj.util.FuzzyBoolean;
import org.aspectj.weaver.BCException;
import org.aspectj.weaver.CompressingDataOutputStream;
import org.aspectj.weaver.ISourceContext;
import org.aspectj.weaver.IntMap;
import org.aspectj.weaver.Member;
import org.aspectj.weaver.NameMangler;
import org.aspectj.weaver.ResolvedMember;
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
public class WithinCodeAnnotationPointcut extends NameBindingPointcut {

	private ExactAnnotationTypePattern annotationTypePattern;
	private String declarationText;

	private static final int matchedShadowKinds;
	static {
		int flags = Shadow.ALL_SHADOW_KINDS_BITS;
		for (int i = 0; i < Shadow.SHADOW_KINDS.length; i++) {
			if (Shadow.SHADOW_KINDS[i].isEnclosingKind()) {
				flags -= Shadow.SHADOW_KINDS[i].bit;
			}
		}
		matchedShadowKinds = flags;
	}

	public WithinCodeAnnotationPointcut(ExactAnnotationTypePattern type) {
		super();
		this.annotationTypePattern = type;
		this.pointcutKind = Pointcut.ATWITHINCODE;
		buildDeclarationText();
	}

	public WithinCodeAnnotationPointcut(ExactAnnotationTypePattern type, ShadowMunger munger) {
		this(type);
		this.pointcutKind = Pointcut.ATWITHINCODE;
	}

	public ExactAnnotationTypePattern getAnnotationTypePattern() {
		return annotationTypePattern;
	}

	public int couldMatchKinds() {
		return matchedShadowKinds;
	}

	public Pointcut parameterizeWith(Map typeVariableMap, World w) {
		WithinCodeAnnotationPointcut ret = new WithinCodeAnnotationPointcut((ExactAnnotationTypePattern) this.annotationTypePattern
				.parameterizeWith(typeVariableMap, w));
		ret.copyLocationFrom(this);
		return ret;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.aspectj.weaver.patterns.Pointcut#fastMatch(org.aspectj.weaver.patterns.FastMatchInfo)
	 */
	public FuzzyBoolean fastMatch(FastMatchInfo info) {
		return FuzzyBoolean.MAYBE;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.aspectj.weaver.patterns.Pointcut#match(org.aspectj.weaver.Shadow)
	 */
	protected FuzzyBoolean matchInternal(Shadow shadow) {
		Member member = shadow.getEnclosingCodeSignature();
		ResolvedMember rMember = member.resolve(shadow.getIWorld());

		if (rMember == null) {
			if (member.getName().startsWith(NameMangler.PREFIX)) {
				return FuzzyBoolean.NO;
			}
			shadow.getIWorld().getLint().unresolvableMember.signal(member.toString(), getSourceLocation());
			return FuzzyBoolean.NO;
		}

		annotationTypePattern.resolve(shadow.getIWorld());
		return annotationTypePattern.matches(rMember);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.aspectj.weaver.patterns.Pointcut#resolveBindings(org.aspectj.weaver.patterns.IScope,
	 * org.aspectj.weaver.patterns.Bindings)
	 */
	protected void resolveBindings(IScope scope, Bindings bindings) {
		if (!scope.getWorld().isInJava5Mode()) {
			scope.message(MessageUtil.error(WeaverMessages.format(WeaverMessages.ATWITHINCODE_ONLY_SUPPORTED_AT_JAVA5_LEVEL),
					getSourceLocation()));
			return;
		}
		annotationTypePattern = (ExactAnnotationTypePattern) annotationTypePattern.resolveBindings(scope, bindings, true);
		// must be either a Var, or an annotation type pattern
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.aspectj.weaver.patterns.Pointcut#concretize1(org.aspectj.weaver.ResolvedType, org.aspectj.weaver.IntMap)
	 */
	protected Pointcut concretize1(ResolvedType inAspect, ResolvedType declaringType, IntMap bindings) {
		ExactAnnotationTypePattern newType = (ExactAnnotationTypePattern) annotationTypePattern.remapAdviceFormals(bindings);
		Pointcut ret = new WithinCodeAnnotationPointcut(newType, bindings.getEnclosingAdvice());
		ret.copyLocationFrom(this);
		return ret;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.aspectj.weaver.patterns.Pointcut#findResidue(org.aspectj.weaver.Shadow, org.aspectj.weaver.patterns.ExposedState)
	 */
	protected Test findResidueInternal(Shadow shadow, ExposedState state) {

		if (annotationTypePattern instanceof BindingAnnotationTypePattern) {
			BindingAnnotationTypePattern btp = (BindingAnnotationTypePattern) annotationTypePattern;
			UnresolvedType annotationType = btp.annotationType;
			Var var = shadow.getWithinCodeAnnotationVar(annotationType);

			// This should not happen, we shouldn't have gotten this far
			// if we weren't going to find the annotation
			if (var == null) {
				throw new BCException("Impossible! annotation=[" + annotationType + "]  shadow=[" + shadow + " at "
						+ shadow.getSourceLocation() + "]    pointcut is at [" + getSourceLocation() + "]");
			}

			state.set(btp.getFormalIndex(), var);
		}
		if (matchInternal(shadow).alwaysTrue()) {
			return Literal.TRUE;
		} else {
			return Literal.FALSE;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.aspectj.weaver.patterns.NameBindingPointcut#getBindingAnnotationTypePatterns()
	 */
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
	public List getBindingTypePatterns() {
		return Collections.EMPTY_LIST;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.aspectj.weaver.patterns.PatternNode#write(java.io.DataOutputStream)
	 */
	public void write(CompressingDataOutputStream s) throws IOException {
		s.writeByte(Pointcut.ATWITHINCODE);
		annotationTypePattern.write(s);
		writeLocation(s);
	}

	public static Pointcut read(VersionedDataInputStream s, ISourceContext context) throws IOException {
		AnnotationTypePattern type = AnnotationTypePattern.read(s, context);
		WithinCodeAnnotationPointcut ret = new WithinCodeAnnotationPointcut((ExactAnnotationTypePattern) type);
		ret.readLocation(context, s);
		return ret;
	}

	public boolean equals(Object other) {
		if (!(other instanceof WithinCodeAnnotationPointcut)) {
			return false;
		}
		WithinCodeAnnotationPointcut o = (WithinCodeAnnotationPointcut) other;
		return o.annotationTypePattern.equals(this.annotationTypePattern);
	}

	public int hashCode() {
		int result = 17;
		result = 23 * result + annotationTypePattern.hashCode();
		return result;
	}

	private void buildDeclarationText() {
		StringBuffer buf = new StringBuffer();
		buf.append("@withincode(");
		String annPatt = annotationTypePattern.toString();
		buf.append(annPatt.startsWith("@") ? annPatt.substring(1) : annPatt);
		buf.append(")");
		this.declarationText = buf.toString();
	}

	public String toString() {
		return this.declarationText;
	}

	public Object accept(PatternNodeVisitor visitor, Object data) {
		return visitor.visit(this, data);
	}
}
