/* *******************************************************************
 * Copyright (c) 1999-2001 Xerox Corporation, 
 *               2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Xerox/PARC     initial implementation 
 * ******************************************************************/


package org.aspectj.lang;

import org.aspectj.lang.reflect.SourceLocation;

/**
 * <p>Provides reflective access to both the state available at a join point and
 * static information about it.  This information is available from the body
 * of advice using the special form <code>thisJoinPoint</code>.  The primary
 * use of this reflective information is for tracing and logging applications.
 * </p>
 *
 * <pre>
 * aspect Logging {
 *     before(): within(com.bigboxco..*) && execution(public * *(..)) {
 *         System.err.println("entering: " + thisJoinPoint);
 *         System.err.println("  w/args: " + thisJoinPoint.getArgs());
 *         System.err.println("      at: " + thisJoinPoint.getSourceLocation());
 *     }
 * }
 * </pre>
 */
public interface JoinPoint {

    String toString();

    /**
     * Returns an abbreviated string representation of the join point.
     */
    String toShortString();

    /**
     * Returns an extended string representation of the join point.
     */
    String toLongString();

    /**
     * <p> Returns the currently executing object.  This will always be
     * the same object as that matched by the <code>this</code> pointcut
     * designator.  Unless you specifically need this reflective access,
     * you should use the <code>this</code> pointcut designator to
     * get at this object for better static typing and performance.</p>
     *
     * <p> Returns null when there is no currently executing object available.
     * This includes all join points that occur in a static context.</p>
     */
    Object getThis();

    /**
     * <p> Returns the target object.  This will always be
     * the same object as that matched by the <code>target</code> pointcut
     * designator.  Unless you specifically need this reflective access,
     * you should use the <code>target</code> pointcut designator to
     * get at this object for better static typing and performance.</p>
     *
     * <p> Returns null when there is no target object.</p>

     */
    Object getTarget();

    /**
     * <p>Returns the arguments at this join point.</p>
     */
    Object[] getArgs();

    /** Returns the signature at the join point.
     *
     * <code>getStaticPart().getSignature()</code> returns the same object
     */
    Signature getSignature();

    /** <p>Returns the source location corresponding to the join point.</p>
     *
     *  <p>If there is no source location available, returns null.</p>
     *
     *  <p>Returns the SourceLocation of the defining class for default constructors.</p>
     *
     *  <p> <code>getStaticPart().getSourceLocation()</code> returns the same object. </p>
     */
    SourceLocation getSourceLocation();

    /** Returns a String representing the kind of join point.  This
     *       String is guaranteed to be
     *       interned. <code>getStaticPart().getKind()</code> returns
     *       the same object.
     */
    String getKind();

    /**
     * <p>This helper object contains only the static information about a join point.
     * It is available from the <code>JoinPoint.getStaticPart()</code> method, and
     * can be accessed separately within advice using the special form
     * <code>thisJoinPointStaticPart</code>.</p>
     *
     * <p>If you are only interested in the static information about a join point,
     * you should access it through this type for the best performance.  This
     * is particularly useful for library methods that want to do serious
     * manipulations of this information, i.e.</p>
     *
     * <pre>
     * public class LoggingUtils {
     *     public static void prettyPrint(JoinPoint.StaticPart jp) {
     *         ...
     *     }
     * }
     *
     * aspect Logging {
     *     before(): ... { LoggingUtils.prettyPrint(thisJoinPointStaticPart); }
     * }
     * </pre>
     *
     * @see JoinPoint#getStaticPart()
     */
    public interface StaticPart {
        /** Returns the signature at the join point.  */
        Signature getSignature();

        /** <p>Returns the source location corresponding to the join point.</p>
        *
        *  <p>If there is no source location available, returns null.</p>
        *
        *  <p>Returns the SourceLocation of the defining class for default constructors.</p>
        */
        SourceLocation getSourceLocation();

        /** <p> Returns a String representing the kind of join point.  This String
        *       is guaranteed to be interned</p>
        */
        String getKind();
        
        /**
         * Return the id for this JoinPoint.StaticPart.  All JoinPoint.StaticPart
         * instances are assigned an id number upon creation.  For each advised type 
         * the id numbers start at 0.
         * <br>
         * The id is guaranteed to remain constant across repeated executions 
         * of a program but may change if the code is recompiled. 
         * <br>
         * The benefit of having an id is that it can be used for array index
         * purposes which can be quicker than using the JoinPoint.StaticPart
         * object itself in a map lookup.
         * <br>
         * Since two JoinPoint.StaticPart instances in different advised types may have
         * the same id, then if the id is being used to index some joinpoint specific
         * state then that state must be maintained on a pertype basis - either by
         * using pertypewithin() or an ITD.
         * 
         * @return the id of this joinpoint
         */
        int getId();

        String toString();

        /**
        * Returns an abbreviated string representation of the join point
        */
        String toShortString();

        /**
        * Returns an extended string representation of the join point
        */
        String toLongString();
    }

    public interface EnclosingStaticPart extends StaticPart {}

    /**
     * Returns an object that encapsulates the static parts of this join point.
     */
    StaticPart getStaticPart();


    /**
     * The legal return values from getKind()
     */
    static String METHOD_EXECUTION = "method-execution";
    static String METHOD_CALL = "method-call";
    static String CONSTRUCTOR_EXECUTION = "constructor-execution";
    static String CONSTRUCTOR_CALL = "constructor-call";
    static String FIELD_GET = "field-get";
    static String FIELD_SET = "field-set";
    static String STATICINITIALIZATION = "staticinitialization";
    static String PREINITIALIZATION = "preinitialization";
    static String INITIALIZATION = "initialization";
    static String EXCEPTION_HANDLER = "exception-handler";
    static String SYNCHRONIZATION_LOCK = "lock";
    static String SYNCHRONIZATION_UNLOCK = "unlock";

    static String ADVICE_EXECUTION = "adviceexecution"; 

}
