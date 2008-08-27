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
package org.aspectj.util;

/** 
 * This class implements boolean that include a "maybe"
 */
public abstract class FuzzyBoolean {
    public abstract boolean alwaysTrue();
    public abstract boolean alwaysFalse();
    public abstract boolean maybeTrue();
    public abstract boolean maybeFalse();
    
    public abstract FuzzyBoolean and(FuzzyBoolean other);
    public abstract FuzzyBoolean or(FuzzyBoolean other);
    public abstract FuzzyBoolean not();
    
    private static class YesFuzzyBoolean extends FuzzyBoolean {
        public boolean alwaysFalse() {
            return false;
        }

        public boolean alwaysTrue() {
            return true;
        }


        public boolean maybeFalse() {
            return false;
        }

        public boolean maybeTrue() {
            return true;
        }
        
        public FuzzyBoolean and(FuzzyBoolean other) {
            return other;
        }

        public FuzzyBoolean not() {
            return FuzzyBoolean.NO;
        }

        public FuzzyBoolean or(FuzzyBoolean other) {
            return this;
        }

        public String toString() {
            return "YES";
        }
    }    
    private static class NoFuzzyBoolean extends FuzzyBoolean {
        public boolean alwaysFalse() {
            return true;
        }

        public boolean alwaysTrue() {
            return false;
        }


        public boolean maybeFalse() {
            return true;
        }

        public boolean maybeTrue() {
            return false;
        }
        
        public FuzzyBoolean and(FuzzyBoolean other) {
            return this;
        }

        public FuzzyBoolean not() {
            return FuzzyBoolean.YES;
        }

        public FuzzyBoolean or(FuzzyBoolean other) {
            return other;
        }

        public String toString() {
            return "NO";
        }
    }
    private static class NeverFuzzyBoolean extends FuzzyBoolean {
        public boolean alwaysFalse() {
            return true;
        }

        public boolean alwaysTrue() {
            return false;
        }


        public boolean maybeFalse() {
            return true;
        }

        public boolean maybeTrue() {
            return false;
        }
        
        public FuzzyBoolean and(FuzzyBoolean other) {
            return this;
        }

        public FuzzyBoolean not() {
            return this;
        }

        public FuzzyBoolean or(FuzzyBoolean other) {
            return this;
        }

        public String toString() {
            return "NEVER";
        }
    }
    
    private static class MaybeFuzzyBoolean extends FuzzyBoolean {
        public boolean alwaysFalse() {
            return false;
        }

        public boolean alwaysTrue() {
            return false;
        }


        public boolean maybeFalse() {
            return true;
        }

        public boolean maybeTrue() {
            return true;
        }
        
        public FuzzyBoolean and(FuzzyBoolean other) {
            return other.alwaysFalse() ? other : this;
        }

        public FuzzyBoolean not() {
            return this;
        }

        public FuzzyBoolean or(FuzzyBoolean other) {
            return other.alwaysTrue() ? other : this;
        }

        public String toString() {
            return "MAYBE";
        }
    }
    
    public static final FuzzyBoolean YES   = new YesFuzzyBoolean();
    public static final FuzzyBoolean NO    = new NoFuzzyBoolean();
    public static final FuzzyBoolean MAYBE = new MaybeFuzzyBoolean();
    public static final FuzzyBoolean NEVER = new NeverFuzzyBoolean();

	public static final FuzzyBoolean fromBoolean(boolean b) {
		return b ? YES : NO;
	}

}
