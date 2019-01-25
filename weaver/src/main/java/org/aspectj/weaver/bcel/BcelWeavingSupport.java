/* *******************************************************************
 * Copyright (c) 2008 Contributors
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Andy Clement     initial implementation 
 * ******************************************************************/
package org.aspectj.weaver.bcel;

import org.aspectj.weaver.Advice;
import org.aspectj.weaver.AjAttribute;
import org.aspectj.weaver.ConcreteTypeMunger;
import org.aspectj.weaver.IWeavingSupport;
import org.aspectj.weaver.Member;
import org.aspectj.weaver.ResolvedMember;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.ResolvedTypeMunger;
import org.aspectj.weaver.ast.Var;
import org.aspectj.weaver.patterns.PerClause;
import org.aspectj.weaver.patterns.Pointcut;

/**
 * Bcel implementation of the weaving support required in a BcelWorld which will actually modify bytecode.
 * 
 * @author Andy Clement
 */
public class BcelWeavingSupport implements IWeavingSupport {

	public Advice createAdviceMunger(AjAttribute.AdviceAttribute attribute, Pointcut pointcut, Member signature,
			ResolvedType concreteAspect) {
		// System.err.println("concrete advice: " + signature + " context " +
		// sourceContext);
		return new BcelAdvice(attribute, pointcut, signature, concreteAspect);
	}

	public ConcreteTypeMunger makeCflowStackFieldAdder(ResolvedMember cflowField) {
		return new BcelCflowStackFieldAdder(cflowField);
	}

	public ConcreteTypeMunger makeCflowCounterFieldAdder(ResolvedMember cflowField) {
		return new BcelCflowCounterFieldAdder(cflowField);
	}

	/**
	 * Register a munger for perclause @AJ aspect so that we add aspectOf(..) to them as needed
	 * 
	 * @param aspect
	 * @param kind
	 * @return munger
	 */
	public ConcreteTypeMunger makePerClauseAspect(ResolvedType aspect, PerClause.Kind kind) {
		return new BcelPerClauseAspectAdder(aspect, kind);
	}

	public Var makeCflowAccessVar(ResolvedType formalType, Member cflowField, int arrayIndex) {
		return new BcelCflowAccessVar(formalType, cflowField, arrayIndex);
	}

	public ConcreteTypeMunger concreteTypeMunger(ResolvedTypeMunger munger, ResolvedType aspectType) {
		return new BcelTypeMunger(munger, aspectType);
	}

	public ConcreteTypeMunger createAccessForInlineMunger(ResolvedType aspect) {
		return new BcelAccessForInlineMunger(aspect);
	}

}
