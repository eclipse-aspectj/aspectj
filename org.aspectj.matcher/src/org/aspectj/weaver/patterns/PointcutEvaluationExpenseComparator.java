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

import java.util.Comparator;

import org.aspectj.weaver.Shadow;

public class PointcutEvaluationExpenseComparator implements Comparator<Pointcut> {

	private static final int MATCHES_NOTHING = -1;
	private static final int WITHIN = 1;
	private static final int ATWITHIN = 2;
	private static final int STATICINIT = 3;
	private static final int ADVICEEXECUTION = 4;
	private static final int HANDLER = 5;
	private static final int GET_OR_SET = 6;
	private static final int WITHINCODE = 7;
	private static final int ATWITHINCODE = 8;
	private static final int EXE_INIT_PREINIT = 9;
	private static final int CALL_WITH_DECLARING_TYPE = 10;
	private static final int THIS_OR_TARGET = 11;
	private static final int CALL_WITHOUT_DECLARING_TYPE = 12;
	private static final int ANNOTATION = 13;
	private static final int AT_THIS_OR_TARGET = 14;
	private static final int ARGS = 15;
	private static final int AT_ARGS = 16;
	private static final int CFLOW = 17;
	private static final int IF = 18;
	private static final int OTHER = 20;

	/**
	 * Compare 2 pointcuts based on an estimate of how expensive they may be to evaluate.
	 * 
	 * within
	 * 
	 * @within staticinitialization [make sure this has a fast match method] adviceexecution handler get, set withincode
	 * @withincode execution, initialization, preinitialization call
	 * @annotation this, target
	 * @this, @target args
	 * @args cflow, cflowbelow if
	 */
	public int compare(Pointcut p1, Pointcut p2) {
		// important property for a well-defined comparator
		if (p1.equals(p2)) {
			return 0;
		}
		int result = getScore(p1) - getScore(p2);
		if (result == 0) {
			// they have the same evaluation expense, but are not 'equal'
			// sort by hashCode
			int p1code = p1.hashCode();
			int p2code = p2.hashCode();
			if (p1code == p2code) {
				return 0;
			} else if (p1code < p2code) {
				return -1;
			} else {
				return +1;
			}
		}
		return result;
	}

	// a higher score means a more expensive evaluation
	private int getScore(Pointcut p) {
		if (p.couldMatchKinds() == Shadow.NO_SHADOW_KINDS_BITS) {
			return MATCHES_NOTHING;
		}
		if (p instanceof WithinPointcut) {
			return WITHIN;
		}
		if (p instanceof WithinAnnotationPointcut) {
			return ATWITHIN;
		}
		if (p instanceof KindedPointcut) {
			KindedPointcut kp = (KindedPointcut) p;
			Shadow.Kind kind = kp.getKind();
			if (kind == Shadow.AdviceExecution) {
				return ADVICEEXECUTION;
			} else if ((kind == Shadow.ConstructorCall) || (kind == Shadow.MethodCall)) {
				TypePattern declaringTypePattern = kp.getSignature().getDeclaringType();
				if (declaringTypePattern instanceof AnyTypePattern) {
					return CALL_WITHOUT_DECLARING_TYPE;
				} else {
					return CALL_WITH_DECLARING_TYPE;					
				}
			} else if ((kind == Shadow.ConstructorExecution) || (kind == Shadow.MethodExecution) || (kind == Shadow.Initialization)
					|| (kind == Shadow.PreInitialization)) {
				return EXE_INIT_PREINIT;
			} else if (kind == Shadow.ExceptionHandler) {
				return HANDLER;
			} else if ((kind == Shadow.FieldGet) || (kind == Shadow.FieldSet)) {
				return GET_OR_SET;
			} else if (kind == Shadow.StaticInitialization) {
				return STATICINIT;
			} else {
				return OTHER;
			}
		}
		if (p instanceof AnnotationPointcut) {
			return ANNOTATION;
		}
		if (p instanceof ArgsPointcut) {
			return ARGS;
		}
		if (p instanceof ArgsAnnotationPointcut) {
			return AT_ARGS;
		}
		if (p instanceof CflowPointcut || p instanceof ConcreteCflowPointcut) {
			return CFLOW;
		}
		if (p instanceof HandlerPointcut) {
			return HANDLER;
		}
		if (p instanceof IfPointcut) {
			return IF;
		}
		if (p instanceof ThisOrTargetPointcut) {
			return THIS_OR_TARGET;
		}
		if (p instanceof ThisOrTargetAnnotationPointcut) {
			return AT_THIS_OR_TARGET;
		}
		if (p instanceof WithincodePointcut) {
			return WITHINCODE;
		}
		if (p instanceof WithinCodeAnnotationPointcut) {
			return ATWITHINCODE;
		}
		if (p instanceof NotPointcut) {
			return getScore(((NotPointcut) p).getNegatedPointcut());
		}
		if (p instanceof AndPointcut) {
			int leftScore = getScore(((AndPointcut) p).getLeft());
			int rightScore = getScore(((AndPointcut) p).getRight());
			if (leftScore < rightScore) {
				return leftScore;
			} else {
				return rightScore;
			}
		}
		if (p instanceof OrPointcut) {
			int leftScore = getScore(((OrPointcut) p).getLeft());
			int rightScore = getScore(((OrPointcut) p).getRight());
			if (leftScore > rightScore) {
				return leftScore;
			} else {
				return rightScore;
			}
		}
		return OTHER;
	}
}
