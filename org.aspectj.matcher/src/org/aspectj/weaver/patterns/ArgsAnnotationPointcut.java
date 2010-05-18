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
import org.aspectj.bridge.ISourceLocation;
import org.aspectj.bridge.MessageUtil;
import org.aspectj.util.FuzzyBoolean;
import org.aspectj.weaver.CompressingDataOutputStream;
import org.aspectj.weaver.ISourceContext;
import org.aspectj.weaver.IntMap;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.Shadow;
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
public class ArgsAnnotationPointcut extends NameBindingPointcut {

	private AnnotationPatternList arguments;
	private String declarationText;

	/**
	 * 
	 */
	public ArgsAnnotationPointcut(AnnotationPatternList arguments) {
		super();
		this.arguments = arguments;
		this.pointcutKind = ATARGS;
		buildDeclarationText();
	}

	public AnnotationPatternList getArguments() {
		return arguments;
	}

	public int couldMatchKinds() {
		return Shadow.ALL_SHADOW_KINDS_BITS; // empty args() matches jps with no args
	}

	public Pointcut parameterizeWith(Map typeVariableMap, World w) {
		ArgsAnnotationPointcut ret = new ArgsAnnotationPointcut(arguments.parameterizeWith(typeVariableMap, w));
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
		arguments.resolve(shadow.getIWorld());
		FuzzyBoolean ret = arguments.matches(shadow.getIWorld().resolve(shadow.getArgTypes()));
		return ret;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.aspectj.weaver.patterns.Pointcut#resolveBindings(org.aspectj.weaver.patterns.IScope,
	 * org.aspectj.weaver.patterns.Bindings)
	 */
	protected void resolveBindings(IScope scope, Bindings bindings) {
		if (!scope.getWorld().isInJava5Mode()) {
			scope.message(MessageUtil.error(WeaverMessages.format(WeaverMessages.ATARGS_ONLY_SUPPORTED_AT_JAVA5_LEVEL),
					getSourceLocation()));
			return;
		}
		arguments.resolveBindings(scope, bindings, true);
		if (arguments.ellipsisCount > 1) {
			scope.message(IMessage.ERROR, this, "uses more than one .. in args (compiler limitation)");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.aspectj.weaver.patterns.Pointcut#concretize1(org.aspectj.weaver.ResolvedType, org.aspectj.weaver.IntMap)
	 */
	protected Pointcut concretize1(ResolvedType inAspect, ResolvedType declaringType, IntMap bindings) {
		if (isDeclare(bindings.getEnclosingAdvice())) {
			// Enforce rule about which designators are supported in declare
			inAspect.getWorld().showMessage(IMessage.ERROR, WeaverMessages.format(WeaverMessages.ARGS_IN_DECLARE),
					bindings.getEnclosingAdvice().getSourceLocation(), null);
			return Pointcut.makeMatchesNothing(Pointcut.CONCRETE);
		}
		AnnotationPatternList list = arguments.resolveReferences(bindings);
		Pointcut ret = new ArgsAnnotationPointcut(list);
		ret.copyLocationFrom(this);
		return ret;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.aspectj.weaver.patterns.Pointcut#findResidue(org.aspectj.weaver.Shadow, org.aspectj.weaver.patterns.ExposedState)
	 */
	protected Test findResidueInternal(Shadow shadow, ExposedState state) {
		int len = shadow.getArgCount();

		// do some quick length tests first
		int numArgsMatchedByEllipsis = (len + arguments.ellipsisCount) - arguments.size();
		if (numArgsMatchedByEllipsis < 0) {
			return Literal.FALSE; // should never happen
		}
		if ((numArgsMatchedByEllipsis > 0) && (arguments.ellipsisCount == 0)) {
			return Literal.FALSE; // should never happen
		}
		// now work through the args and the patterns, skipping at ellipsis
		Test ret = Literal.TRUE;
		int argsIndex = 0;
		for (int i = 0; i < arguments.size(); i++) {
			if (arguments.get(i) == AnnotationTypePattern.ELLIPSIS) {
				// match ellipsisMatchCount args
				argsIndex += numArgsMatchedByEllipsis;
			} else if (arguments.get(i) == AnnotationTypePattern.ANY) {
				argsIndex++;
			} else {
				// match the argument type at argsIndex with the ExactAnnotationTypePattern
				// we know it is exact because nothing else is allowed in args
				ExactAnnotationTypePattern ap = (ExactAnnotationTypePattern) arguments.get(i);
				UnresolvedType argType = shadow.getArgType(argsIndex);
				ResolvedType rArgType = argType.resolve(shadow.getIWorld());
				if (rArgType.isMissing()) {
					shadow.getIWorld().getLint().cantFindType.signal(new String[] { WeaverMessages.format(
							WeaverMessages.CANT_FIND_TYPE_ARG_TYPE, argType.getName()) }, shadow.getSourceLocation(),
							new ISourceLocation[] { getSourceLocation() });
					// IMessage msg = new Message(
					// WeaverMessages.format(WeaverMessages.CANT_FIND_TYPE_ARG_TYPE,argType.getName()),
					// "",IMessage.ERROR,shadow.getSourceLocation(),null,new ISourceLocation[]{getSourceLocation()});
				}

				ResolvedType rAnnType = ap.getAnnotationType().resolve(shadow.getIWorld());
				if (ap instanceof BindingAnnotationTypePattern) {
					BindingAnnotationTypePattern btp = (BindingAnnotationTypePattern) ap;
					Var annvar = shadow.getArgAnnotationVar(argsIndex, rAnnType);
					state.set(btp.getFormalIndex(), annvar);
				}
				if (!ap.matches(rArgType).alwaysTrue()) {
					// we need a test...
					ret = Test.makeAnd(ret, Test.makeHasAnnotation(shadow.getArgVar(argsIndex), rAnnType));
				}
				argsIndex++;
			}
		}
		return ret;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.aspectj.weaver.patterns.NameBindingPointcut#getBindingAnnotationTypePatterns()
	 */
	public List getBindingAnnotationTypePatterns() {
		List l = new ArrayList();
		AnnotationTypePattern[] pats = arguments.getAnnotationPatterns();
		for (int i = 0; i < pats.length; i++) {
			if (pats[i] instanceof BindingAnnotationTypePattern) {
				l.add(pats[i]);
			}
		}
		return l;
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
		s.writeByte(Pointcut.ATARGS);
		arguments.write(s);
		writeLocation(s);
	}

	public static Pointcut read(VersionedDataInputStream s, ISourceContext context) throws IOException {
		AnnotationPatternList annotationPatternList = AnnotationPatternList.read(s, context);
		ArgsAnnotationPointcut ret = new ArgsAnnotationPointcut(annotationPatternList);
		ret.readLocation(context, s);
		return ret;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (!(obj instanceof ArgsAnnotationPointcut)) {
			return false;
		}
		ArgsAnnotationPointcut other = (ArgsAnnotationPointcut) obj;
		return other.arguments.equals(arguments);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return 17 + 37 * arguments.hashCode();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	private void buildDeclarationText() {
		StringBuffer buf = new StringBuffer("@args");
		buf.append(arguments.toString());
		this.declarationText = buf.toString();
	}

	public String toString() {
		return this.declarationText;
	}

	public Object accept(PatternNodeVisitor visitor, Object data) {
		return visitor.visit(this, data);
	}
}
