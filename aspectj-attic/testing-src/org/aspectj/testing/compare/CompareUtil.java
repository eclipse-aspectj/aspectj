/* *******************************************************************
 * Copyright (c) 1999-2001 Xerox Corporation, 
 *               2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     Xerox/PARC     initial implementation 
 * ******************************************************************/

package org.aspectj.testing.compare;

import java.util.Iterator;
import java.util.Collection;
import java.util.List;

/** Minor methods for short-circuiting comparisons */
public class CompareUtil {    
    /**
     * Callers may abort equality checks with false
     * if this passes its misc short-circuit semantics.
     * A false result does not mean the arguments
     * are equal, but a true result does mean they are not equal. 
     * <pre>if (notSame(foo,bar,true)) then return false;</pre>
     * @param lhs the Object on the left-hand-side to compare
     * @param rhs the Object on the right-hand-side to compare
     * @param considerType if true, then also return true if the 
     *           right-hand-side cannot be assigned to the left-hand-side
     * @return true if lhs and rhs are not the same per considerType.
     */
    public static boolean notSame(Object lhs, Object rhs, boolean considerType) {
        if (null == lhs) {
            return (!(null == rhs));
        } else if (null == rhs) {
            return true;
        } else if (lhs== rhs) {
            return false;  // known to be same
        } else if (considerType) {
            Class lhClass = lhs.getClass();
            Class rhClass = rhs.getClass();
            if (!lhClass.isAssignableFrom(rhClass)) {
                return true;
            }
        }
        return false; // unknown whether equal or not
    }    

    /**
     * Return null/equal comparison:
     * <li>null considered to be lesser</li>
     * <li>reference or Object.equals() considered to be 0</li>
     * <li>return Integer.MAX_VALUE for all other cases</li>
     * <table>
     * <tr><td>result</td><td>input</td></tr>
     * <tr><td>-1</td><td>null &lt; rhs</td></tr>
     * <tr><td>1</td><td>lhs &gt; null</td></tr>
     * <tr><td>0</td><td>null == null</td></tr>
     * <tr><td>0</td><td>lhs == rhs</td></tr>
     * <tr><td>0</td><td>lhs.equals(rhs)</td></tr>
     * <tr><td>Integer.MAX_VALUE</td><td>{all other cases}</td></tr>
     * </table>
     * @see Comparator
     * @return Integer.MAX_VALUE if uncertain, value otherwise
     */
    public static int compare(Object lhs, Object rhs) {
        if (null == lhs) {
            return (null == rhs ? 0 : -1);
        } else if (null == rhs) {
            return 1;
        } else if (lhs == rhs) {
            return 0;  // known to be same
        } else {
            return Integer.MAX_VALUE;
        }
    }

    /**
     * Return boolean comparison where true > false.
     * (Comparable not defined for Boolean)
     * @see Comparator
     */
    public static int compare(boolean lhs, boolean rhs) {
        return (lhs == rhs ? 0 : (lhs ? 1 : -1));
    }

    /**
     * Return String comparison based on {@link compare(Object,Object)}
     * and {@link String.compareTo(String)}.
     * @see Comparator
     */
    public static int compare(String lhs, String rhs) {
        int result = compare((Object) lhs, (Object) rhs);
        if (Integer.MAX_VALUE == result) {
            result = lhs.compareTo(rhs);
        }
        return result;
    }

    /**
     * Compare two Collections by reference to a standard List.
     * The first Collection to not contain a standard element 
     * when the other does loses. Order is ignored.
     * The left-hand-side acts as the standard if the standard is null.
     * @param lhs the List from the left-hand-side
     * @param rhs the Collection from the right-hand-side
     * @param standard the List to act as the standard (if null, use lhs)
     * @param return -1 if lhs is null and rhs is not, 1 if reverse;
     *        0 if both have all elements in standard,
     *        1 if lhs has a standard element rhs does not, -1 if reverse
     *        (testing in standard order)
     */
    public static int compare(List lhs, Collection rhs, List standard) {
        int result = compare(lhs, rhs);
        if (result == Integer.MAX_VALUE) {
            if (null == standard) {
                result = compare(lhs, rhs, lhs); // use lhs as standard
            } else {
                boolean leftHasThem = lhs.containsAll(standard);
                boolean rightHasThem = rhs.containsAll(standard);
                if (leftHasThem != rightHasThem) {
                    result = (leftHasThem ? 1 : -1);
                } else if (leftHasThem) {
                    result = 0; // they both have them
                } else {        // first to not have an element loses
                    Iterator standardIterator = standard.iterator();
                    while (standardIterator.hasNext()) {
                        Object standardObject = standardIterator.next();
                        boolean leftHasIt = lhs.contains(standardObject);
                        boolean rightHasIt = rhs.contains(standardObject);
                        if (leftHasIt != rightHasIt) {
                            result = (leftHasIt ? 1 : -1);
                            break;
                        }
                    }
                }
            }
        }
        return result;
    }
} // class Util
