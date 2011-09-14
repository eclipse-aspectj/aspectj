/*******************************************************************************
 * Copyright (c) 2005 Contributors.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://eclipse.org/legal/epl-v10.html 
 * 
 * Contributors:
 *   Alexandre Vasseur         initial implementation
 *******************************************************************************/
package org.aspectj.weaver.patterns;

import org.aspectj.weaver.BCException;
import org.aspectj.weaver.ResolvedPointcutDefinition;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.Shadow;

/**
 * A visitor that turns a pointcut into a type pattern equivalent for a perthis or pertarget matching: - pertarget(target(Foo)) =>
 * Foo+ (this one is a special case..) - pertarget(execution(* Foo.do()) => Foo - perthis(call(* Foo.do()) => * - perthis(!call(*
 * Foo.do()) => * (see how the ! has been absorbed here..)
 * 
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
public class PerThisOrTargetPointcutVisitor extends AbstractPatternNodeVisitor {

	/** A maybe marker */
	private final static TypePattern MAYBE = new TypePatternMayBe();

	private final boolean m_isTarget;
	private final ResolvedType m_fromAspectType;

	public PerThisOrTargetPointcutVisitor(boolean isTarget, ResolvedType fromAspectType) {
		m_isTarget = isTarget;
		m_fromAspectType = fromAspectType;
	}

	public TypePattern getPerTypePointcut(Pointcut perClausePointcut) {
		Object o = perClausePointcut.accept(this, perClausePointcut);
		if (o instanceof TypePattern) {
			return (TypePattern) o;
		} else {
			throw new BCException("perClausePointcut visitor did not return a typepattern, it returned " + o
					+ (o == null ? "" : " of type " + o.getClass()));
		}
	}

	// -- visitor methods, all is like Identity visitor except when it comes to transform pointcuts

	public Object visit(WithinPointcut node, Object data) {
		if (m_isTarget) {
			// pertarget(.. && within(Foo)) => true
			// pertarget(.. && !within(Foo)) => true as well !
			return MAYBE;
		} else {
			return node.getTypePattern();
		}
	}

	public Object visit(WithincodePointcut node, Object data) {
		if (m_isTarget) {
			// pertarget(.. && withincode(* Foo.do())) => true
			// pertarget(.. && !withincode(* Foo.do())) => true as well !
			return MAYBE;
		} else {
			return node.getSignature().getDeclaringType();
		}
	}

	public Object visit(WithinAnnotationPointcut node, Object data) {
		if (m_isTarget) {
			return MAYBE;
		} else {
			return new AnyWithAnnotationTypePattern(node.getAnnotationTypePattern());
		}
	}

	public Object visit(WithinCodeAnnotationPointcut node, Object data) {
		if (m_isTarget) {
			return MAYBE;
		} else {
			return MAYBE;// FIXME AV - can we optimize ? perthis(@withincode(Foo)) = hasmethod(..)
		}
	}

	public Object visit(KindedPointcut node, Object data) {
		if (node.getKind().equals(Shadow.AdviceExecution)) {
			return MAYBE;// TODO AV - can we do better ?
		} else if (node.getKind().equals(Shadow.ConstructorExecution) || node.getKind().equals(Shadow.Initialization)
				|| node.getKind().equals(Shadow.MethodExecution) || node.getKind().equals(Shadow.PreInitialization)
				|| node.getKind().equals(Shadow.StaticInitialization)) {
			SignaturePattern signaturePattern = node.getSignature();
			boolean isStarAnnotation = signaturePattern.isStarAnnotation();
			// For a method execution joinpoint, we check for an annotation pattern. If there is one we know it will be matched
			// against the 'primary' joinpoint (the one in the type) - 'super'joinpoints can't match it. If this situation occurs
			// we can re-use the HasMemberTypePattern to guard on whether the perthis/target should match. pr354470
			if (!m_isTarget && node.getKind().equals(Shadow.MethodExecution)) {
				if (!isStarAnnotation) {
					return new HasMemberTypePatternForPerThisMatching(signaturePattern);
				}
			}
			return signaturePattern.getDeclaringType();
		} else if (node.getKind().equals(Shadow.ConstructorCall) || node.getKind().equals(Shadow.FieldGet)
				|| node.getKind().equals(Shadow.FieldSet) || node.getKind().equals(Shadow.MethodCall)) {
			if (m_isTarget) {
				return node.getSignature().getDeclaringType();
			} else {
				return MAYBE;
			}
		} else if (node.getKind().equals(Shadow.ExceptionHandler)) {
			return MAYBE;
		} else {
			throw new ParserException("Undetermined - should not happen: " + node.getKind().getSimpleName(), null);
		}
	}

	public Object visit(AndPointcut node, Object data) {
		return new AndTypePattern(getPerTypePointcut(node.left), getPerTypePointcut(node.right));
	}

	public Object visit(OrPointcut node, Object data) {
		return new OrTypePattern(getPerTypePointcut(node.left), getPerTypePointcut(node.right));
	}

	public Object visit(NotPointcut node, Object data) {
		// TypePattern negated = getPerTypePointcut(node.getNegatedPointcut());
		// if (MAYBE.equals(negated)) {
		// return MAYBE;
		// }
		// return new NotTypePattern(negated);
		// AMC - the only safe thing to return here is maybe...
		// see for example pr114054
		return MAYBE;
	}

	public Object visit(ThisOrTargetAnnotationPointcut node, Object data) {
		if (m_isTarget && !node.isThis()) {
			return new AnyWithAnnotationTypePattern(node.getAnnotationTypePattern());
		} else if (!m_isTarget && node.isThis()) {
			return new AnyWithAnnotationTypePattern(node.getAnnotationTypePattern());
		} else {
			// perthis(@target(Foo))
			return MAYBE;
		}
	}

	public Object visit(ThisOrTargetPointcut node, Object data) {
		if ((m_isTarget && !node.isThis()) || (!m_isTarget && node.isThis())) {
			String pointcutString = node.getType().toString();
			// see pr115788 "<nothing>" means there was a problem resolving types - that will be reported so dont blow up
			// the parser here..
			if (pointcutString.equals("<nothing>")) {
				return new NoTypePattern();
			}
			// pertarget(target(Foo)) => Foo+ for type pattern matching
			// perthis(this(Foo)) => Foo+ for type pattern matching
			// TODO AV - we do like a deep copy by parsing it again.. quite dirty, would need a clean deep copy
			TypePattern copy = new PatternParser(pointcutString.replace('$', '.')).parseTypePattern();
			// TODO AV - see dirty replace from $ to . here as inner classes are with $ instead (#108488)
			copy.includeSubtypes = true;
			return copy;
		} else {
			// perthis(target(Foo)) => maybe
			return MAYBE;
		}
	}

	public Object visit(ReferencePointcut node, Object data) {
		// && pc_ref()
		// we know there is no support for binding in perClause: perthis(pc_ref(java.lang.String))
		// TODO AV - may need some work for generics..

		ResolvedPointcutDefinition pointcutDec;
		ResolvedType searchStart = m_fromAspectType;
		if (node.onType != null) {
			searchStart = node.onType.resolve(m_fromAspectType.getWorld());
			if (searchStart.isMissing()) {
				return MAYBE;// this should not happen since concretize will fails but just in case..
			}
		}
		pointcutDec = searchStart.findPointcut(node.name);

		return getPerTypePointcut(pointcutDec.getPointcut());
	}

	public Object visit(IfPointcut node, Object data) {
		return TypePattern.ANY;
	}

	public Object visit(HandlerPointcut node, Object data) {
		// quiet unexpected since a KindedPointcut but do as if...
		return MAYBE;
	}

	public Object visit(CflowPointcut node, Object data) {
		return MAYBE;
	}

	public Object visit(ConcreteCflowPointcut node, Object data) {
		return MAYBE;
	}

	public Object visit(ArgsPointcut node, Object data) {
		return MAYBE;
	}

	public Object visit(ArgsAnnotationPointcut node, Object data) {
		return MAYBE;
	}

	public Object visit(AnnotationPointcut node, Object data) {
		return MAYBE;
	}

	public Object visit(Pointcut.MatchesNothingPointcut node, Object data) {
		// a small hack since the usual MatchNothing has its toString = "<nothing>" which is not parseable back
		// while I use back parsing for check purpose.
		return new NoTypePattern() {
			public String toString() {
				return "false";
			}
		};
	}

	/**
	 * A MayBe type pattern that acts as ANY except that !MAYBE = MAYBE
	 * 
	 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
	 */
	private static class TypePatternMayBe extends AnyTypePattern {
	}
}
