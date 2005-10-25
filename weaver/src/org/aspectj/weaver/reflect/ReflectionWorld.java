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
package org.aspectj.weaver.reflect;

import org.aspectj.bridge.AbortException;
import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.IMessageHandler;
import org.aspectj.util.LangUtil;
import org.aspectj.weaver.Advice;
import org.aspectj.weaver.ConcreteTypeMunger;
import org.aspectj.weaver.Member;
import org.aspectj.weaver.ReferenceType;
import org.aspectj.weaver.ReferenceTypeDelegate;
import org.aspectj.weaver.ResolvedMember;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.ResolvedTypeMunger;
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.World;
import org.aspectj.weaver.AjAttribute.AdviceAttribute;
import org.aspectj.weaver.patterns.Pointcut;
import org.aspectj.weaver.patterns.PerClause.Kind;

/**
 * A ReflectionWorld is used solely for purposes of type resolution based on 
 * the runtime classpath (java.lang.reflect). It does not support weaving operations
 * (creation of mungers etc..).
 *
 */
public class ReflectionWorld extends World {

	public ReflectionWorld() {
		super();
		this.setMessageHandler(new ExceptionBasedMessageHandler());
		setBehaveInJava5Way(LangUtil.is15VMOrGreater());
	}
	
	public ResolvedType resolve(Class aClass) {
		// classes that represent arrays return a class name that is the signature of the array type, ho-hum...
		String className = aClass.getName();
		if (aClass.isArray()) {
			return resolve(UnresolvedType.forSignature(className));
		}
		else{
			return resolve(className);
		} 
	}
	
	/* (non-Javadoc)
	 * @see org.aspectj.weaver.World#resolveDelegate(org.aspectj.weaver.ReferenceType)
	 */
	protected ReferenceTypeDelegate resolveDelegate(ReferenceType ty) {
		return ReflectionBasedReferenceTypeDelegateFactory.createDelegate(ty, this);
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.World#createAdviceMunger(org.aspectj.weaver.AjAttribute.AdviceAttribute, org.aspectj.weaver.patterns.Pointcut, org.aspectj.weaver.Member)
	 */
	public Advice createAdviceMunger(AdviceAttribute attribute,
			Pointcut pointcut, Member signature) {
		throw new UnsupportedOperationException("Cannot create advice munger in ReflectionWorld");
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.World#makeCflowStackFieldAdder(org.aspectj.weaver.ResolvedMember)
	 */
	public ConcreteTypeMunger makeCflowStackFieldAdder(ResolvedMember cflowField) {
		throw new UnsupportedOperationException("Cannot create cflow stack in ReflectionWorld");
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.World#makeCflowCounterFieldAdder(org.aspectj.weaver.ResolvedMember)
	 */
	public ConcreteTypeMunger makeCflowCounterFieldAdder(
			ResolvedMember cflowField) {
		throw new UnsupportedOperationException("Cannot create cflow counter in ReflectionWorld");
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.World#makePerClauseAspect(org.aspectj.weaver.ResolvedType, org.aspectj.weaver.patterns.PerClause.Kind)
	 */
	public ConcreteTypeMunger makePerClauseAspect(ResolvedType aspect, Kind kind) {
		throw new UnsupportedOperationException("Cannot create per clause in ReflectionWorld");
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.World#concreteTypeMunger(org.aspectj.weaver.ResolvedTypeMunger, org.aspectj.weaver.ResolvedType)
	 */
	public ConcreteTypeMunger concreteTypeMunger(ResolvedTypeMunger munger,
			ResolvedType aspectType) {
		throw new UnsupportedOperationException("Cannot create type munger in ReflectionWorld");
	}
	
	public static class ReflectionWorldException extends RuntimeException {

		private static final long serialVersionUID = -3432261918302793005L;

		public ReflectionWorldException(String message) {
			super(message);
		}
	}
	
	private static class ExceptionBasedMessageHandler implements IMessageHandler {

		public boolean handleMessage(IMessage message) throws AbortException {
			throw new ReflectionWorldException(message.toString());
		}

		public boolean isIgnoring(org.aspectj.bridge.IMessage.Kind kind) {
			if (kind == IMessage.INFO) {
				return true;
			} else {
				return false;
			}
		}

		public void dontIgnore(org.aspectj.bridge.IMessage.Kind kind) {
			// empty
		}
		
	}

}
