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
package org.aspectj.weaver.ltw;

import org.aspectj.weaver.Advice;
import org.aspectj.weaver.ConcreteTypeMunger;
import org.aspectj.weaver.Member;
import org.aspectj.weaver.ReferenceType;
import org.aspectj.weaver.ReferenceTypeDelegate;
import org.aspectj.weaver.ResolvedMember;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.ResolvedTypeMunger;
import org.aspectj.weaver.World;
import org.aspectj.weaver.AjAttribute.AdviceAttribute;
import org.aspectj.weaver.patterns.Pointcut;
import org.aspectj.weaver.patterns.PerClause.Kind;

/**
 * @author adrian
 * 
 * For use in LT weaving
 * 
 * Backed by both a BcelWorld and a ReflectionWorld
 * 
 * Needs a callback when a woven class is defined
 * This is the trigger for us to ditch the class from
 * Bcel and cache it in the reflective world instead.
 *
 * Problems with classes that are loaded by delegates
 * of our classloader
 * 
 * Create by passing in a classloader, message handler
 */
public class LTWWorld extends World {

	protected ReferenceTypeDelegate resolveDelegate(ReferenceType ty) {
		// TODO Auto-generated method stub
		return null;
		
	}

	public Advice createAdviceMunger(AdviceAttribute attribute, Pointcut pointcut, Member signature) {
		// TODO Auto-generated method stub
		return null;
	}

	public ConcreteTypeMunger makeCflowStackFieldAdder(ResolvedMember cflowField) {
		// TODO Auto-generated method stub
		return null;
	}

	public ConcreteTypeMunger makeCflowCounterFieldAdder(ResolvedMember cflowField) {
		// TODO Auto-generated method stub
		return null;
	}

	public ConcreteTypeMunger makePerClauseAspect(ResolvedType aspect, Kind kind) {
		// TODO Auto-generated method stub
		return null;
	}

	public ConcreteTypeMunger concreteTypeMunger(ResolvedTypeMunger munger, ResolvedType aspectType) {
		// TODO Auto-generated method stub
		return null;
	}

}
