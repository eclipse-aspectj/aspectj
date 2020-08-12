/* *******************************************************************
 * Copyright (c) 1999-2000 Xerox Corporation. 
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Xerox/PARC     initial implementation 
 * ******************************************************************/


package org.aspectj.testing.util;


import java.io.Serializable;

/**
 * imutable class to enforce an integer range
 */
public class IntRange implements IntValidator, ObjectChecker, Serializable {
    /** no values permitted */
    public static final IntRange NONE = new IntRange(0, 0);
    /** 0 permitted */
    public static final IntRange ZERO = new IntRange(0, 1);
    /** 1 permitted */
    public static final IntRange ONE = new IntRange(1, 2);
    /** 0..1 permitted */
    public static final IntRange OPTIONAL = new IntRange(0, 2);
    /** 1..1000 permitted */
    public static final IntRange MANY = new IntRange(1, 1001);

    /** all positive numbers permitted except Integer.MAX_VALUE */
    public static final IntRange POSITIVE = new IntRange(1, Integer.MAX_VALUE);
    /** all negative numbers permitted  */
    public static final IntRange NEGATIVE = new IntRange(Integer.MIN_VALUE, 0);
    /** any int number permitted except Integer.MAX_VALUE */
    public static final IntRange ANY = new IntRange(Integer.MIN_VALUE, Integer.MAX_VALUE);

    /** 
     * Make an IntRange that accepts this value
     * (using existing if available).
     * @throws IllegalArgumentException if value is Integer.MAX_VALUE.
     */
    public static final IntRange make(int value) {
        switch (value) {
            case (1) : return ONE;   
            case (0) : return ZERO;  
            case (Integer.MAX_VALUE) : 
                throw new IllegalArgumentException("illegal " + value); 
            default : 
                return new IntRange(value, value + 1);
        }
    }
      
    public final int min;
    public final int max;
    private transient String cache;

    /** use only for serialization 
     * @deprecated IntRange(int, int)
     */
    protected IntRange() {
        min = 0;
        max = 0;
    }    
    
    /**
     * @param min minimum permitted value, inclusive
     * @param max maximum permitted value, exclusive
     */
    public IntRange(int min, int max) {
        this.min = min;
        this.max = max;
        if (min > max) {
            throw new IllegalArgumentException( min + " > " + max);
        }
        toString(); // create cache to view during debugging
    }
    
    /** @return true if integer instanceof Integer with acceptable intValue */
    public final boolean isValid(Object integer) {
        return ((integer instanceof Integer)
                && (acceptInt((Integer) integer)));
    }
    
    /** @return true if min <= value < max */
    public final boolean acceptInt(int value) {
        return ((value >= min) && (value < max));
    }
    

    /** 
     * @deprecated acceptInt(int)
     * @return true if min <= value < max 
     */
    public final boolean inRange(int value) {
        return acceptInt(value);
    }
    /** 
     * @return true if, for any int x s.t. other.inRange(x)
     * is true, this.inRange(x) is also true
     */
    public final boolean inRange(IntRange other) {
        return ((null != other)  && (other.min >= min)
            && (other.max <= max));
    }
    
    // XXX equals(Object)    
    
    public String toString() {
        if (null == cache) {
            cache = "IntRange [" + min + ".." + max + "]";
        }
        return cache;
    }
}
