/* *******************************************************************
 * Copyright (c) 2004 IBM Corporation.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * ******************************************************************/
package org.aspectj.weaver.internal.tools;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;

import org.aspectj.lang.JoinPoint;
import org.aspectj.weaver.patterns.Pointcut;
import org.aspectj.weaver.tools.FuzzyBoolean;
import org.aspectj.weaver.tools.PointcutExpression;

/**
 * Map from weaver.tools interface to internal Pointcut implementation...
 */
public class PointcutExpressionImpl implements PointcutExpression {
	
	private Pointcut pointcut;
	private String expression;
	
	public PointcutExpressionImpl(Pointcut pointcut, String expression) {
		this.pointcut = pointcut;
		this.expression = expression;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.tools.PointcutExpression#matchesMethodCall(java.lang.reflect.Method, java.lang.Class, java.lang.Class, java.lang.reflect.Member)
	 */
	public FuzzyBoolean matchesMethodCall(Method aMethod, Class thisClass,
			Class targetClass, Member withinCode) {
		return fuzzyMatch(pointcut.matchesStatically(
				JoinPoint.METHOD_CALL,
				aMethod,
				thisClass,
				targetClass,
				withinCode));
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.tools.PointcutExpression#matchesMethodExecution(java.lang.reflect.Method, java.lang.Class)
	 */
	public FuzzyBoolean matchesMethodExecution(Method aMethod, Class thisClass) {
		return fuzzyMatch(pointcut.matchesStatically(
				JoinPoint.METHOD_EXECUTION,
				aMethod,
				thisClass,
				thisClass,
				null));
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.tools.PointcutExpression#matchesConstructorCall(java.lang.reflect.Constructor, java.lang.Class, java.lang.reflect.Member)
	 */
	public FuzzyBoolean matchesConstructorCall(Constructor aConstructor,
			Class thisClass, Member withinCode) {
		return fuzzyMatch(pointcut.matchesStatically(
				JoinPoint.CONSTRUCTOR_CALL,
				aConstructor,
				thisClass,
				aConstructor.getDeclaringClass(),
				withinCode));
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.tools.PointcutExpression#matchesConstructorExecution(java.lang.reflect.Constructor)
	 */
	public FuzzyBoolean matchesConstructorExecution(Constructor aConstructor, Class thisClass) {
		return fuzzyMatch(pointcut.matchesStatically(
				JoinPoint.CONSTRUCTOR_EXECUTION,
				aConstructor,
				thisClass,
				thisClass,
				null));
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.tools.PointcutExpression#matchesAdviceExecution(java.lang.reflect.Method, java.lang.Class)
	 */
	public FuzzyBoolean matchesAdviceExecution(Method anAdviceMethod,
			Class thisClass) {
		return fuzzyMatch(pointcut.matchesStatically(
				JoinPoint.ADVICE_EXECUTION,
				anAdviceMethod,
				thisClass,
				thisClass,
				null));
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.tools.PointcutExpression#matchesHandler(java.lang.Class, java.lang.Class, java.lang.reflect.Member)
	 */
	public FuzzyBoolean matchesHandler(Class exceptionType, Class inClass,
			Member withinCode) {
		return fuzzyMatch(pointcut.matchesStatically(
				JoinPoint.EXCEPTION_HANDLER,
				new Handler(inClass,exceptionType),
				inClass,
				inClass,
				withinCode));
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.tools.PointcutExpression#matchesInitialization(java.lang.reflect.Constructor)
	 */
	public FuzzyBoolean matchesInitialization(Constructor aConstructor) {
		return fuzzyMatch(pointcut.matchesStatically(
				JoinPoint.INITIALIZATION,
				aConstructor,
				aConstructor.getDeclaringClass(),
				aConstructor.getDeclaringClass(),
				null));
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.tools.PointcutExpression#matchesPreInitialization(java.lang.reflect.Constructor)
	 */
	public FuzzyBoolean matchesPreInitialization(Constructor aConstructor) {
		return fuzzyMatch(pointcut.matchesStatically(
				JoinPoint.PREINTIALIZATION,
				aConstructor,
				aConstructor.getDeclaringClass(),
				aConstructor.getDeclaringClass(),
				null));
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.tools.PointcutExpression#matchesStaticInitialization(java.lang.Class)
	 */
	public FuzzyBoolean matchesStaticInitialization(Class aClass) {
		return fuzzyMatch(pointcut.matchesStatically(
				JoinPoint.STATICINITIALIZATION,
				null,
				aClass,
				aClass,
				null
				));
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.tools.PointcutExpression#matchesFieldSet(java.lang.reflect.Field, java.lang.Class, java.lang.Class, java.lang.reflect.Member)
	 */
	public FuzzyBoolean matchesFieldSet(Field aField, Class thisClass,
			Class targetClass, Member withinCode) {
		return fuzzyMatch(pointcut.matchesStatically(
				JoinPoint.FIELD_SET,
				aField,
				thisClass,
				targetClass,
				withinCode));
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.tools.PointcutExpression#matchesFieldGet(java.lang.reflect.Field, java.lang.Class, java.lang.Class, java.lang.reflect.Member)
	 */
	public FuzzyBoolean matchesFieldGet(Field aField, Class thisClass,
			Class targetClass, Member withinCode) {
		return fuzzyMatch(pointcut.matchesStatically(
				JoinPoint.FIELD_GET,
				aField,
				thisClass,
				targetClass,
				withinCode));
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.tools.PointcutExpression#matchesDynamically(java.lang.Object, java.lang.Object, java.lang.Object[])
	 */
	public boolean matchesDynamically(Object thisObject, Object targetObject,
			Object[] args) {
		return pointcut.matchesDynamically(thisObject,targetObject,args);
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.tools.PointcutExpression#getPointcutExpression()
	 */
	public String getPointcutExpression() {
		return expression;
	}

	private FuzzyBoolean fuzzyMatch(org.aspectj.util.FuzzyBoolean fb) {
		if (fb == org.aspectj.util.FuzzyBoolean.YES) return FuzzyBoolean.YES;
		if (fb == org.aspectj.util.FuzzyBoolean.NO) return FuzzyBoolean.NO;
		if (fb == org.aspectj.util.FuzzyBoolean.MAYBE) return FuzzyBoolean.MAYBE;
		throw new IllegalArgumentException("Cant match FuzzyBoolean " + fb);
	}
	
	public static class Handler implements Member {

		private Class decClass;
		private Class exType;
		
		public Handler(Class decClass, Class exType) {
			this.decClass = decClass;
			this.exType = exType;
		}
		
		public int getModifiers() {
			return 0;
		}

		public Class getDeclaringClass() {
			return decClass;
		}

		public String getName() {
			return null;
		}

		public Class getHandledExceptionType() {
			return exType;
		}

		public boolean isSynthetic() {
			return false;
		}
	}
}
