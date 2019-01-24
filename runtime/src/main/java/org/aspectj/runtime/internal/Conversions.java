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


package org.aspectj.runtime.internal;

public final class Conversions {
	// Can't make instances of me
	private Conversions() {}

    // we might want to keep a cache of small integers around
	public static Object intObject(int i) {
        return new Integer(i);
    }
	public static Object shortObject(short i) {
        return new Short(i);
    }
	public static Object byteObject(byte i) {
        return new Byte(i);
    }
	public static Object charObject(char i) {
        return new Character(i);
    }
	public static Object longObject(long i) {
        return new Long(i);
    }
	public static Object floatObject(float i) {
        return new Float(i);
    }
	public static Object doubleObject(double i) {
        return new Double(i);
    }
	public static Object booleanObject(boolean i) {
        return new Boolean(i);
    }
	public static Object voidObject() {
        return null;
    }


	public static int intValue(Object o) {
		if (o == null) {
			return 0;
		} else if (o instanceof Number) {
			return ((Number)o).intValue();
		} else {
			throw new ClassCastException(o.getClass().getName() +
										 " can not be converted to int");
		}
	}
	public static long longValue(Object o) {
		if (o == null) {
			return 0;
		} else if (o instanceof Number) {
			return ((Number)o).longValue();
		} else {
			throw new ClassCastException(o.getClass().getName() +
										 " can not be converted to long");
		}
	}
	public static float floatValue(Object o) {
		if (o == null) {
			return 0;
		} else if (o instanceof Number) {
			return ((Number)o).floatValue();
		} else {
			throw new ClassCastException(o.getClass().getName() +
										 " can not be converted to float");
		}
	}
	public static double doubleValue(Object o) {
		if (o == null) {
			return 0;
		} else if (o instanceof Number) {
			return ((Number)o).doubleValue();
		} else {
			throw new ClassCastException(o.getClass().getName() +
										 " can not be converted to double");
		}
	}
	public static byte byteValue(Object o) {
		if (o == null) {
			return 0;
		} else if (o instanceof Number) {
			return ((Number)o).byteValue();
		} else {
			throw new ClassCastException(o.getClass().getName() +
										 " can not be converted to byte");
		}
	}
	public static short shortValue(Object o) {
		if (o == null) {
			return 0;
		} else if (o instanceof Number) {
			return ((Number)o).shortValue();
		} else {
			throw new ClassCastException(o.getClass().getName() +
										 " can not be converted to short");
		}
	}
	public static char charValue(Object o) {
		if (o == null) {
			return 0;
		} else if (o instanceof Character) {
			return ((Character)o).charValue();
		} else {
			throw new ClassCastException(o.getClass().getName() +
										 " can not be converted to char");
		}
	}
	public static boolean booleanValue(Object o) {
		if (o == null) {
			return false;
		} else if (o instanceof Boolean) {
			return ((Boolean)o).booleanValue();
		} else {
			throw new ClassCastException(o.getClass().getName() +
										 " can not be converted to boolean");
		}
	}
	
	/** 
	 * identity function for now.  This is not typed to "void" because we happen
	 * to know that in Java, any void context (i.e., {@link ExprStmt})
	 *  can also handle a return value.
	 */
	public static Object voidValue(Object o) {
		if (o == null) {
			return o;
		} else {
			// !!! this may be an error in the future
			return o;
		}
	}
}
