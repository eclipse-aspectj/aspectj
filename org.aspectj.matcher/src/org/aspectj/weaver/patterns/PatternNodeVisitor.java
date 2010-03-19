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
 *   Adrian Colyer                refactoring for traversal and grouping by kind
 *******************************************************************************/
package org.aspectj.weaver.patterns;


/**
 * A Pointcut or TypePattern visitor
 *
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
public interface PatternNodeVisitor {

	// Annotation type patterns
	Object visit(AndAnnotationTypePattern node, Object data);
    Object visit(AnyAnnotationTypePattern node, Object data);
    Object visit(EllipsisAnnotationTypePattern node, Object data);
    Object visit(ExactAnnotationTypePattern node, Object data);
    Object visit(BindingAnnotationTypePattern node, Object data);
    Object visit(NotAnnotationTypePattern node, Object data);
    Object visit(OrAnnotationTypePattern node, Object data);
    Object visit(WildAnnotationTypePattern node, Object data);
    Object visit(AnnotationPatternList node, Object data);

    // Regular type patterns	
    Object visit(AndTypePattern node, Object data);	
    Object visit(AnyTypePattern node, Object data);
    Object visit(AnyWithAnnotationTypePattern node, Object data);
    Object visit(EllipsisTypePattern node, Object data);
    Object visit(ExactTypePattern node, Object data);
    Object visit(BindingTypePattern node, Object data);
    Object visit(NotTypePattern node, Object data);
    Object visit(NoTypePattern node, Object data);
    Object visit(OrTypePattern node, Object data);
    Object visit(WildTypePattern node, Object data);
    Object visit(TypePatternList node, Object data);
    Object visit(HasMemberTypePattern node, Object data);
    Object visit(TypeCategoryTypePattern node, Object data); 

    // Pointcuts
	Object visit(AndPointcut node, Object data);
    Object visit(CflowPointcut node, Object data);
    Object visit(ConcreteCflowPointcut node, Object data);
    Object visit(HandlerPointcut node, Object data);
    Object visit(IfPointcut node, Object data);
    Object visit(KindedPointcut node, Object data);
    Object visit(Pointcut.MatchesNothingPointcut node, Object data);
    Object visit(AnnotationPointcut node, Object data);
    Object visit(ArgsAnnotationPointcut node, Object data);
    Object visit(ArgsPointcut node, Object data);
    Object visit(ThisOrTargetAnnotationPointcut node, Object data);
    Object visit(ThisOrTargetPointcut node, Object data);
    Object visit(WithinAnnotationPointcut node, Object data);
    Object visit(WithinCodeAnnotationPointcut node, Object data);
    Object visit(NotPointcut node, Object data);
    Object visit(OrPointcut node, Object data);
    Object visit(ReferencePointcut node, Object data);
    Object visit(WithinPointcut node, Object data);
    Object visit(WithincodePointcut node, Object data);

	// Per-clauses
    Object visit(PerCflow node, Object data);
    Object visit(PerFromSuper node, Object data);
    Object visit(PerObject node, Object data);
    Object visit(PerSingleton node, Object data);
    Object visit(PerTypeWithin node, Object data);

	
	// Declares
    Object visit(DeclareAnnotation node, Object data);
    Object visit(DeclareErrorOrWarning node, Object data);
    Object visit(DeclareParents node, Object data);
    Object visit(DeclarePrecedence node, Object data);
    Object visit(DeclareSoft node, Object data);

	// Miscellaneous patterns
    Object visit(ModifiersPattern node, Object data);
    Object visit(NamePattern node, Object data);
    Object visit(SignaturePattern node, Object data);
    Object visit(ThrowsPattern node, Object data);
	Object visit(TypeVariablePattern node, Object data);
	Object visit(TypeVariablePatternList node,Object data);

	// Catch-all
    Object visit(PatternNode node, Object data);

}
