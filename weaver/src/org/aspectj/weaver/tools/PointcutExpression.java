/* *******************************************************************
 * Copyright (c) 2004 IBM Corporation.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * ******************************************************************/

package org.aspectj.weaver.tools;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;

/**
 * Represents an AspectJ pointcut expression and provides convenience methods to determine
 * whether or not the pointcut matches join points specified in terms of the
 * java.lang.reflect interfaces.
 */
public interface PointcutExpression {

	/**
	 * Determine whether or not this pointcut matches a method call to the given method.
	 * @param aMethod the method being called
	 * @param thisClass the type making the method call
	 * @param targetClass the static type of the target of the call 
	 * (may be a subtype of aMethod.getDeclaringClass() )
	 * @param withinCode the Method or Constructor from within which the call is made
	 * @return a FuzzyBoolean indicating whether the pointcut always matches such a join point (YES),
	 * never matches such a join point (NO), or may match such a join point (MAYBE) depending on the runtime
	 * types of the arguments, caller, and called object.
	 */
	FuzzyBoolean matchesMethodCall(Method aMethod, Class thisClass, Class targetClass, Member withinCode);
	
	/**
	 * Determine whether or not this pointcut matches the execution of a given method.
	 * @param aMethod the method being executed
	 * @param thisClass the static type of the object in which the method is executing
	 * (may be a subtype of aMethod.getDeclaringClass())
	 * @return a FuzzyBoolean indicating whether the pointcut always matches such a join point (YES),
	 * never matches such a join point (NO), or may match such a join point (MAYBE) depending on the
	 * runtime types of the arguments and executing object.
	 */
	FuzzyBoolean matchesMethodExecution(Method aMethod, Class thisClass);
	
	/**
	 * Determine whether or not this pointcut matches a call to the given constructor.
	 * @param aConstructor the constructor being called
	 * @param thisClass the type making the constructor call
	 * @param withinCode the Method or Constructor from within which the call is made
	 * @return a FuzzyBoolean indicating whether the pointcut always matches such a join point (YES),
	 * never matches such a join point (NO), or may match such a join point (MAYBE) depending on the runtime
	 * types of the arguments and caller.
	 */	
	FuzzyBoolean matchesConstructorCall(Constructor aConstructor, Class thisClass, Member withinCode);
	
	/**
	 * Determine whether or not this pointcut matches the execution of a given constructor.
	 * @param aConstructor the constructor being executed
	 * @param thisClass the static type of the object in which the constructor is executing
	 * (may be a subtype of aConstructor.getDeclaringClass())
	 * @return a FuzzyBoolean indicating whether the pointcut always matches such a join point (YES),
	 * never matches such a join point (NO), or may match such a join point (MAYBE) depending on the
	 * runtime types of the arguments and executing object.
	 */	
	FuzzyBoolean matchesConstructorExecution(Constructor aConstructor, Class thisClass);
	
	/**
	 * Determine whether or not this pointcut matches the execution of a given piece of advice.
	 * @param anAdviceMethod a method representing the advice being executed
	 * @param thisClass the static type of the aspect in which the advice is executing
	 * (may be a subtype of anAdviceMethod.getDeclaringClass())
	 * @return a FuzzyBoolean indicating whether the pointcut always matches such a join point (YES),
	 * never matches such a join point (NO), or may match such a join point (MAYBE) depending on the
	 * runtime type of the executing aspect.
	 */		
	FuzzyBoolean matchesAdviceExecution(Method anAdviceMethod, Class thisClass);
	
	/**
	 * Determine whether or not this pointcut matches the execution of a given exception
	 * handler
	 * @param exceptionType the static type of the exception being handled
	 * @param inClass the class in which the catch block is declared
	 * @param withinCode the method or constructor in which the catch block is declared
	 * @return a FuzzyBoolean indicating whether the pointcut always matches such a join point (YES),
	 * never matches such a join point (NO), or may match such a join point (MAYBE) depending on the
	 * runtime types of the exception and exception-handling object.
	 */	
	FuzzyBoolean matchesHandler(Class exceptionType, Class inClass, Member withinCode);
	
	/**
	 * Determine whether or not this pointcut matches the initialization of an
	 * object initiated by a call to the given constructor.
	 * @param aConstructor the constructor initiating the initialization
	 * @return a FuzzyBoolean indicating whether the pointcut always matches such a join point (YES),
	 * never matches such a join point (NO), or may match such a join point (MAYBE) depending on the
	 * runtime types of the arguments.
	 */	
	FuzzyBoolean matchesInitialization(Constructor aConstructor);
	
	/**
	 * Determine whether or not this pointcut matches the preinitialization of an
	 * object initiated by a call to the given constructor.
	 * @param aConstructor the constructor initiating the initialization
	 * @return a FuzzyBoolean indicating whether the pointcut always matches such a join point (YES),
	 * never matches such a join point (NO), or may match such a join point (MAYBE) depending on the
	 * runtime types of the arguments.
	 */	
	FuzzyBoolean matchesPreInitialization(Constructor aConstructor);
	
	/**
	 * Determine whether or not this pointcut matches the static initialization
	 * of the given class.
	 * @param aClass the class being statically initialized
	 * @return FuzzyBoolean.YES is the pointcut always matches, FuzzyBoolean.NO if the
	 * pointcut never matches.
	 */
	FuzzyBoolean matchesStaticInitialization(Class aClass);
	
	/**
	 * Determine whether or not this pointcut matches a set of the given field.
	 * @param aField the field being updated
	 * @param thisClass the type sending the update message
	 * @param targetClass the static type of the target of the field update message 
	 * (may be a subtype of aField.getDeclaringClass() )
	 * @param withinCode the Method or Constructor from within which the update message is sent
	 * @return a FuzzyBoolean indicating whether the pointcut always matches such a join point (YES),
	 * never matches such a join point (NO), or may match such a join point (MAYBE) depending on the runtime
	 * types of the caller and called object.
	 */
	FuzzyBoolean matchesFieldSet(Field aField, Class thisClass, Class targetClass, Member withinCode);
	
	/**
	 * Determine whether or not this pointcut matches a get of the given field.
	 * @param aField the field being accessed
	 * @param thisClass the type accessing the field
	 * @param targetClass the static type of the target of the field access message 
	 * (may be a subtype of aField.getDeclaringClass() )
	 * @param withinCode the Method or Constructor from within which the field is accessed
	 * @return a FuzzyBoolean indicating whether the pointcut always matches such a join point (YES),
	 * never matches such a join point (NO), or may match such a join point (MAYBE) depending on the runtime
	 * types of the caller and called object.
	 */
	FuzzyBoolean matchesFieldGet(Field aField, Class thisClass, Class targetClass, Member withinCode);
	
	/**
	 * Returns true iff the dynamic portions of the pointcut expression (this, target, and 
	 * args) match the given this, target, and args objects. This method only needs to be 
	 * called if a previous call to a FuzzyBoolean-returning matching method returned
	 * FuzzyBoolean.MAYBE. Even if this method returns true, the pointcut can only be 
	 * considered to match the join point if the appropriate matches method for the join 
	 * point kind has also returned FuzzyBoolean.YES or FuzzyBoolean.MAYBE.
	 * @param thisObject
	 * @param targetObject
	 * @param args
	 * @return
	 */
	boolean matchesDynamically(Object thisObject, Object targetObject, Object[] args);
	
	/**
	 * Return a string representation of this pointcut expression.
	 */
	String getPointcutExpression();
}
