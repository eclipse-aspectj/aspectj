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
 *     before(): within(com.bigboxco..*) &amp;&amp; execution(public * *(..)) {
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
     * @return an abbreviated string representation of the join point.
     */
    String toShortString();

    /**
     * @return an extended string representation of the join point.
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
     *
     * @return the currently executing object (or null if not available - e.g. static context)
     */
    Object getThis();

    /**
     * Returns the target object.  This will always be
     * the same object as that matched by the <code>target</code> pointcut
     * designator.  Unless you specifically need this reflective access,
     * you should use the <code>target</code> pointcut designator to
     * get at this object for better static typing and performance.
     *
     * Returns null when there is no target object
     *
     * @return the target object (or null if there isn't one)
     */
    Object getTarget();

    /**
     * @return the arguments at this join point
     */
    Object[] getArgs();

    /**
     * <code>getStaticPart().getSignature()</code> returns the same object
     * @return the signature at the join point.
     */
    Signature getSignature();

    /**
     *
     *  <p>If there is no source location available, returns null.</p>
     *
     *  <p>Returns the SourceLocation of the defining class for default constructors.</p>
     *
     *  <p> <code>getStaticPart().getSourceLocation()</code> returns the same object. </p>
     *
     * @return the source location corresponding to the join point.
     */
    SourceLocation getSourceLocation();

    /** This string is guaranteed to be interned.
     * <code>getStaticPart().getKind()</code> returns the same object.
     *
     * @return a string representing the kind of join point.
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
	interface StaticPart {
        /** @return the signature at the join point.  */
        Signature getSignature();

        /** Returns the source location corresponding to the join point.
        *
        *  If there is no source location available, returns null.
        *
        *  @return the SourceLocation of the defining class for default constructors
        */
        SourceLocation getSourceLocation();

        /** @return a string representing the kind of join point.  This String
        *       is guaranteed to be interned
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
        * @return an abbreviated string representation of the join point
        */
        String toShortString();

        /**
        * @return an extended string representation of the join point
        */
        String toLongString();
    }

    interface EnclosingStaticPart extends StaticPart {}

    /**
     * @return an object that encapsulates the static parts of this join point.
     */
    StaticPart getStaticPart();


    /**
     * The legal return values from getKind()
     */
	String METHOD_EXECUTION = "method-execution";
    String METHOD_CALL = "method-call";
    String CONSTRUCTOR_EXECUTION = "constructor-execution";
    String CONSTRUCTOR_CALL = "constructor-call";
    String FIELD_GET = "field-get";
    String FIELD_SET = "field-set";
    String STATICINITIALIZATION = "staticinitialization";
    String PREINITIALIZATION = "preinitialization";
    String INITIALIZATION = "initialization";
    String EXCEPTION_HANDLER = "exception-handler";
    String SYNCHRONIZATION_LOCK = "lock";
    String SYNCHRONIZATION_UNLOCK = "unlock";

    String ADVICE_EXECUTION = "adviceexecution";

}
