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

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.patterns.PointcutVisitor#visit(org.aspectj.weaver.patterns.AnyTypePattern, java.lang.Object)
	 */
	public Object visit(AnyTypePattern node, Object data) {
		return node;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.patterns.PointcutVisitor#visit(org.aspectj.weaver.patterns.NoTypePattern, java.lang.Object)
	 */
	public Object visit(NoTypePattern node, Object data) {
		return node;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.patterns.PointcutVisitor#visit(org.aspectj.weaver.patterns.EllipsisTypePattern, java.lang.Object)
	 */
	public Object visit(EllipsisTypePattern node, Object data) {
		return node;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.patterns.PointcutVisitor#visit(org.aspectj.weaver.patterns.AnyWithAnnotationTypePattern, java.lang.Object)
	 */
	public Object visit(AnyWithAnnotationTypePattern node, Object data) {
		return node;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.patterns.PointcutVisitor#visit(org.aspectj.weaver.patterns.AnyAnnotationTypePattern, java.lang.Object)
	 */
	public Object visit(AnyAnnotationTypePattern node, Object data) {
		return node;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.patterns.PointcutVisitor#visit(org.aspectj.weaver.patterns.EllipsisAnnotationTypePattern, java.lang.Object)
	 */
	public Object visit(EllipsisAnnotationTypePattern node, Object data) {
		return node;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.patterns.PointcutVisitor#visit(org.aspectj.weaver.patterns.AndAnnotationTypePattern, java.lang.Object)
	 */
	public Object visit(AndAnnotationTypePattern node, Object data) {
		return node;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.patterns.PointcutVisitor#visit(org.aspectj.weaver.patterns.AndPointcut, java.lang.Object)
	 */
	public Object visit(AndPointcut node, Object data) {
		return node;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.patterns.PointcutVisitor#visit(org.aspectj.weaver.patterns.AndTypePattern, java.lang.Object)
	 */
	public Object visit(AndTypePattern node, Object data) {
		return node;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.patterns.PointcutVisitor#visit(org.aspectj.weaver.patterns.AnnotationPatternList, java.lang.Object)
	 */
	public Object visit(AnnotationPatternList node, Object data) {
		return node;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.patterns.PointcutVisitor#visit(org.aspectj.weaver.patterns.AnnotationPointcut, java.lang.Object)
	 */
	public Object visit(AnnotationPointcut node, Object data) {
		return node;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.patterns.PointcutVisitor#visit(org.aspectj.weaver.patterns.ArgsAnnotationPointcut, java.lang.Object)
	 */
	public Object visit(ArgsAnnotationPointcut node, Object data) {
		return node;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.patterns.PointcutVisitor#visit(org.aspectj.weaver.patterns.ArgsPointcut, java.lang.Object)
	 */
	public Object visit(ArgsPointcut node, Object data) {
		return node;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.patterns.PointcutVisitor#visit(org.aspectj.weaver.patterns.BindingAnnotationTypePattern, java.lang.Object)
	 */
	public Object visit(BindingAnnotationTypePattern node, Object data) {
		return node;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.patterns.PointcutVisitor#visit(org.aspectj.weaver.patterns.BindingTypePattern, java.lang.Object)
	 */
	public Object visit(BindingTypePattern node, Object data) {
		return node;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.patterns.PointcutVisitor#visit(org.aspectj.weaver.patterns.CflowPointcut, java.lang.Object)
	 */
	public Object visit(CflowPointcut node, Object data) {
		return node;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.patterns.PointcutVisitor#visit(org.aspectj.weaver.patterns.ConcreteCflowPointcut, java.lang.Object)
	 */
	public Object visit(ConcreteCflowPointcut node, Object data) {
		return node;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.patterns.PointcutVisitor#visit(org.aspectj.weaver.patterns.DeclareAnnotation, java.lang.Object)
	 */
	public Object visit(DeclareAnnotation node, Object data) {
		return node;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.patterns.PointcutVisitor#visit(org.aspectj.weaver.patterns.DeclareErrorOrWarning, java.lang.Object)
	 */
	public Object visit(DeclareErrorOrWarning node, Object data) {
		return node;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.patterns.PointcutVisitor#visit(org.aspectj.weaver.patterns.DeclareParents, java.lang.Object)
	 */
	public Object visit(DeclareParents node, Object data) {
		return node;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.patterns.PointcutVisitor#visit(org.aspectj.weaver.patterns.DeclarePrecedence, java.lang.Object)
	 */
	public Object visit(DeclarePrecedence node, Object data) {
		return node;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.patterns.PointcutVisitor#visit(org.aspectj.weaver.patterns.DeclareSoft, java.lang.Object)
	 */
	public Object visit(DeclareSoft node, Object data) {
		return node;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.patterns.PointcutVisitor#visit(org.aspectj.weaver.patterns.ExactAnnotationTypePattern, java.lang.Object)
	 */
	public Object visit(ExactAnnotationTypePattern node, Object data) {
		return node;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.patterns.PointcutVisitor#visit(org.aspectj.weaver.patterns.ExactTypePattern, java.lang.Object)
	 */
	public Object visit(ExactTypePattern node, Object data) {
		return node;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.patterns.PointcutVisitor#visit(org.aspectj.weaver.patterns.HandlerPointcut, java.lang.Object)
	 */
	public Object visit(HandlerPointcut node, Object data) {
		return node;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.patterns.PointcutVisitor#visit(org.aspectj.weaver.patterns.IfPointcut, java.lang.Object)
	 */
	public Object visit(IfPointcut node, Object data) {
		return node;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.patterns.PointcutVisitor#visit(org.aspectj.weaver.patterns.KindedPointcut, java.lang.Object)
	 */
	public Object visit(KindedPointcut node, Object data) {
		return node;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.patterns.PointcutVisitor#visit(org.aspectj.weaver.patterns.ModifiersPattern, java.lang.Object)
	 */
	public Object visit(ModifiersPattern node, Object data) {
		return node;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.patterns.PointcutVisitor#visit(org.aspectj.weaver.patterns.NamePattern, java.lang.Object)
	 */
	public Object visit(NamePattern node, Object data) {
		return node;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.patterns.PointcutVisitor#visit(org.aspectj.weaver.patterns.NotAnnotationTypePattern, java.lang.Object)
	 */
	public Object visit(NotAnnotationTypePattern node, Object data) {
		return node;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.patterns.PointcutVisitor#visit(org.aspectj.weaver.patterns.NotPointcut, java.lang.Object)
	 */
	public Object visit(NotPointcut node, Object data) {
		return node;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.patterns.PointcutVisitor#visit(org.aspectj.weaver.patterns.NotTypePattern, java.lang.Object)
	 */
	public Object visit(NotTypePattern node, Object data) {
		return node;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.patterns.PointcutVisitor#visit(org.aspectj.weaver.patterns.OrAnnotationTypePattern, java.lang.Object)
	 */
	public Object visit(OrAnnotationTypePattern node, Object data) {
		return node;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.patterns.PointcutVisitor#visit(org.aspectj.weaver.patterns.OrPointcut, java.lang.Object)
	 */
	public Object visit(OrPointcut node, Object data) {
		return node;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.patterns.PointcutVisitor#visit(org.aspectj.weaver.patterns.OrTypePattern, java.lang.Object)
	 */
	public Object visit(OrTypePattern node, Object data) {
		return node;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.patterns.PointcutVisitor#visit(org.aspectj.weaver.patterns.PerCflow, java.lang.Object)
	 */
	public Object visit(PerCflow node, Object data) {
		return node;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.patterns.PointcutVisitor#visit(org.aspectj.weaver.patterns.PerFromSuper, java.lang.Object)
	 */
	public Object visit(PerFromSuper node, Object data) {
		return node;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.patterns.PointcutVisitor#visit(org.aspectj.weaver.patterns.PerObject, java.lang.Object)
	 */
	public Object visit(PerObject node, Object data) {
		return node;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.patterns.PointcutVisitor#visit(org.aspectj.weaver.patterns.PerSingleton, java.lang.Object)
	 */
	public Object visit(PerSingleton node, Object data) {
		return node;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.patterns.PointcutVisitor#visit(org.aspectj.weaver.patterns.PerTypeWithin, java.lang.Object)
	 */
	public Object visit(PerTypeWithin node, Object data) {
		return node;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.patterns.PointcutVisitor#visit(org.aspectj.weaver.patterns.PatternNode, java.lang.Object)
	 */
	public Object visit(PatternNode node, Object data) {
		return node;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.patterns.PointcutVisitor#visit(org.aspectj.weaver.patterns.ReferencePointcut, java.lang.Object)
	 */
	public Object visit(ReferencePointcut node, Object data) {
		return node;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.patterns.PointcutVisitor#visit(org.aspectj.weaver.patterns.SignaturePattern, java.lang.Object)
	 */
	public Object visit(SignaturePattern node, Object data) {
		return node;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.patterns.PointcutVisitor#visit(org.aspectj.weaver.patterns.ThisOrTargetAnnotationPointcut, java.lang.Object)
	 */
	public Object visit(ThisOrTargetAnnotationPointcut node, Object data) {
		return node;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.patterns.PointcutVisitor#visit(org.aspectj.weaver.patterns.ThisOrTargetPointcut, java.lang.Object)
	 */
	public Object visit(ThisOrTargetPointcut node, Object data) {
		return node;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.patterns.PointcutVisitor#visit(org.aspectj.weaver.patterns.ThrowsPattern, java.lang.Object)
	 */
	public Object visit(ThrowsPattern node, Object data) {
		return node;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.patterns.PointcutVisitor#visit(org.aspectj.weaver.patterns.TypePatternList, java.lang.Object)
	 */
	public Object visit(TypePatternList node, Object data) {
		return node;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.patterns.PointcutVisitor#visit(org.aspectj.weaver.patterns.WildAnnotationTypePattern, java.lang.Object)
	 */
	public Object visit(WildAnnotationTypePattern node, Object data) {
		return node;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.patterns.PointcutVisitor#visit(org.aspectj.weaver.patterns.WildTypePattern, java.lang.Object)
	 */
	public Object visit(WildTypePattern node, Object data) {
		return node;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.patterns.PointcutVisitor#visit(org.aspectj.weaver.patterns.WithinAnnotationPointcut, java.lang.Object)
	 */
	public Object visit(WithinAnnotationPointcut node, Object data) {
		return node;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.patterns.PointcutVisitor#visit(org.aspectj.weaver.patterns.WithinCodeAnnotationPointcut, java.lang.Object)
	 */
	public Object visit(WithinCodeAnnotationPointcut node, Object data) {
		return node;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.patterns.PointcutVisitor#visit(org.aspectj.weaver.patterns.WithinPointcut, java.lang.Object)
	 */
	public Object visit(WithinPointcut node, Object data) {
		return node;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.patterns.PointcutVisitor#visit(org.aspectj.weaver.patterns.WithincodePointcut, java.lang.Object)
	 */
	public Object visit(WithincodePointcut node, Object data) {
		return node;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.patterns.PointcutVisitor#visit(org.aspectj.weaver.patterns.Pointcut.MatchesNothingPointcut, java.lang.Object)
	 */
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
}
