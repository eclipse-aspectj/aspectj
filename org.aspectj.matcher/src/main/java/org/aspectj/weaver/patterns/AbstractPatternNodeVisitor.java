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
package org.aspectj.weaver.patterns;

import org.aspectj.weaver.patterns.Pointcut.MatchesNothingPointcut;

/**
 * @author colyer
 * 
 */
public abstract class AbstractPatternNodeVisitor implements PatternNodeVisitor {

	public Object visit(AnyTypePattern node, Object data) {
		return node;
	}

	public Object visit(NoTypePattern node, Object data) {
		return node;
	}

	public Object visit(EllipsisTypePattern node, Object data) {
		return node;
	}

	public Object visit(AnyWithAnnotationTypePattern node, Object data) {
		return node;
	}

	public Object visit(AnyAnnotationTypePattern node, Object data) {
		return node;
	}

	public Object visit(EllipsisAnnotationTypePattern node, Object data) {
		return node;
	}

	public Object visit(AndAnnotationTypePattern node, Object data) {
		return node;
	}

	public Object visit(AndPointcut node, Object data) {
		return node;
	}

	public Object visit(AndTypePattern node, Object data) {
		return node;
	}

	public Object visit(AnnotationPatternList node, Object data) {
		return node;
	}

	public Object visit(AnnotationPointcut node, Object data) {
		return node;
	}

	public Object visit(ArgsAnnotationPointcut node, Object data) {
		return node;
	}

	public Object visit(ArgsPointcut node, Object data) {
		return node;
	}

	public Object visit(BindingAnnotationTypePattern node, Object data) {
		return node;
	}

	public Object visit(BindingTypePattern node, Object data) {
		return node;
	}

	public Object visit(CflowPointcut node, Object data) {
		return node;
	}

	public Object visit(ConcreteCflowPointcut node, Object data) {
		return node;
	}

	public Object visit(DeclareAnnotation node, Object data) {
		return node;
	}

	public Object visit(DeclareErrorOrWarning node, Object data) {
		return node;
	}

	public Object visit(DeclareParents node, Object data) {
		return node;
	}

	public Object visit(DeclarePrecedence node, Object data) {
		return node;
	}

	public Object visit(DeclareSoft node, Object data) {
		return node;
	}

	public Object visit(ExactAnnotationTypePattern node, Object data) {
		return node;
	}

	public Object visit(ExactTypePattern node, Object data) {
		return node;
	}

	public Object visit(HandlerPointcut node, Object data) {
		return node;
	}

	public Object visit(IfPointcut node, Object data) {
		return node;
	}

	public Object visit(KindedPointcut node, Object data) {
		return node;
	}

	public Object visit(ModifiersPattern node, Object data) {
		return node;
	}

	public Object visit(NamePattern node, Object data) {
		return node;
	}

	public Object visit(NotAnnotationTypePattern node, Object data) {
		return node;
	}

	public Object visit(NotPointcut node, Object data) {
		return node;
	}

	public Object visit(NotTypePattern node, Object data) {
		return node;
	}

	public Object visit(OrAnnotationTypePattern node, Object data) {
		return node;
	}

	public Object visit(OrPointcut node, Object data) {
		return node;
	}

	public Object visit(OrTypePattern node, Object data) {
		return node;
	}

	public Object visit(PerCflow node, Object data) {
		return node;
	}

	public Object visit(PerFromSuper node, Object data) {
		return node;
	}

	public Object visit(PerObject node, Object data) {
		return node;
	}

	public Object visit(PerSingleton node, Object data) {
		return node;
	}

	public Object visit(PerTypeWithin node, Object data) {
		return node;
	}

	public Object visit(PatternNode node, Object data) {
		return node;
	}

	public Object visit(ReferencePointcut node, Object data) {
		return node;
	}

	public Object visit(SignaturePattern node, Object data) {
		return node;
	}

	public Object visit(ThisOrTargetAnnotationPointcut node, Object data) {
		return node;
	}

	public Object visit(ThisOrTargetPointcut node, Object data) {
		return node;
	}

	public Object visit(ThrowsPattern node, Object data) {
		return node;
	}

	public Object visit(TypePatternList node, Object data) {
		return node;
	}

	public Object visit(WildAnnotationTypePattern node, Object data) {
		return node;
	}

	public Object visit(WildTypePattern node, Object data) {
		return node;
	}

	public Object visit(WithinAnnotationPointcut node, Object data) {
		return node;
	}

	public Object visit(WithinCodeAnnotationPointcut node, Object data) {
		return node;
	}

	public Object visit(WithinPointcut node, Object data) {
		return node;
	}

	public Object visit(WithincodePointcut node, Object data) {
		return node;
	}

	public Object visit(MatchesNothingPointcut node, Object data) {
		return node;
	}

	public Object visit(TypeVariablePattern node, Object data) {
		return node;
	}

	public Object visit(TypeVariablePatternList node, Object data) {
		return node;
	}

	public Object visit(HasMemberTypePattern node, Object data) {
		return node;
	}

	public Object visit(TypeCategoryTypePattern node, Object data) {
		return node;
	}
}
