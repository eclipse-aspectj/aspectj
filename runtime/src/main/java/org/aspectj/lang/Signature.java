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

/** <p>Represents the signature at a join point.  This interface parallels
 *  <code>java.lang.reflect.Member</code>. </p>
 * 
 * <p>This interface is typically used for tracing or logging applications
 * to obtain reflective information about the join point, i.e. using
 * the j2se 1.4 <code>java.util.logging</code> API</p>
 * <pre>
 * aspect Logging {
 *     Logger logger = Logger.getLogger("MethodEntries");
 * 
 *     before(): within(com.bigboxco..*) &amp;&amp; execution(public * *(..)) {
 *         Signature sig = thisJoinPoint.getSignature();
 *         logger.entering(sig.getDeclaringType().getName(),
 *                         sig.getName());
 *     }
 * }
 * </pre>
 * 
 * 
 * <p>More detailed information about a specific kind of signature can
 * be obtained by casting this <code>Signature</code> object into one 
 * of its more specific sub-types available in
 * <code>org.aspectj.lang.reflect</code>.
 * 
 *  @see java.lang.reflect.Member
 *  @see java.util.logging.Logger
 */
public interface Signature {
    String toString();

    /**
     * @return an abbreviated string representation of this signature.
     */
    String toShortString();

    /**
     * @return an extended string representation of this signature.
     */
    String toLongString();


    /**
     * @return the identifier part of this signature.  For methods this
     * will return the method name.
     * 
     * @see java.lang.reflect.Member#getName
     */
    String getName();

    /**
     * Returns the modifiers on this signature represented as an int.  Use
     * the constants and helper methods defined on 
     * <code>java.lang.reflect.Modifier</code> to manipulate this, i.e.
     * <pre>
     *     // check if this signature is public
     *     java.lang.reflect.Modifier.isPublic(sig.getModifiers());
     * 
     *     // print out the modifiers
     *     java.lang.reflect.Modifier.toString(sig.getModifiers());
     * </pre>
     * 
     * @return the modifiers on this signature represented as an int
     * @see java.lang.reflect.Member#getModifiers
     * @see java.lang.reflect.Modifier
     */
    int    getModifiers();                              

    /**
     * <p>Returns a <code>java.lang.Class</code> object representing the class,
     * interface, or aspect that declared this member.  For intra-member
     * declarations, this will be the type on which the member is declared,
     * not the type where the declaration is lexically written.  Use
     * <code>SourceLocation.getWithinType()</code> to get the type in 
     * which the declaration occurs lexically.</p>
     * <p>For consistency with <code>java.lang.reflect.Member</code>, this
     * method should have been named <code>getDeclaringClass()</code>.</p>
     * 
     * @return the class, interface or aspect that declared this member
     * @see java.lang.reflect.Member#getDeclaringClass
     */
    Class  getDeclaringType();
    
    /**
     * This is equivalent to calling getDeclaringType().getName(), but caches
     * the result for greater efficiency.
     * 
     * @return the fully qualified name of the declaring type
     */
    String getDeclaringTypeName();
}
