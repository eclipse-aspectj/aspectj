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

package org.aspectj.testing.compare.adapters;

import org.aspectj.testing.compare.CompareUtil;
import java.util.Comparator;

// for testing code
import java.util.*;
import java.text.Collator;

/** 
 * This adopts pairs of [Class, Comparator]
 * and implements compare by delegation thereto, 
 * selecting the first added where the classes of the both inputs
 * are assignable to the pair Class.
 * It first applies the null-semantics of CompareUtil.compare(..),
 * which holds that null values are less-than non-null values,
 * and that two nulls are equal.
 * Note that this class uses Object.equals(..)'s default reference
 * equality, so two SubTypeComparator with the same list 
 * of delegates will NOT be considered equal.
 * <p>todo: This class is not thread-safe.
 * <p>todo: immutable: final list copy on construction to implement equals??
 */
public class SubTypeComparator implements Comparator {
	/** order-sensitive comparators collection */
	private Vector comparators;
	/** copy of comparators for compare method */
	private Struct[] cache;

	/** default constructor */
	public SubTypeComparator () {
		comparators = new Vector();
	}

	/**
	 * Return true if the Class is in the list of comparators
	 * as of the initial execution of the method.
	 * @param c the Class to look for a comparator
	 * @return true of comparator exists for c
	 */
	private boolean contains(Class c) {
		final int size = comparators.size();
		for (int i = 0; i < size; i++) {
			if (c == ((Struct) comparators.elementAt(i)).accepts) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Get copy of comparators for compare operation.
	 */
	private Struct[] getComparators() { // todo: sync on comparators
		if ((null == cache) || (cache.length < comparators.size())) {
			cache = new Struct[comparators.size()];
			comparators.copyInto(cache);
		}
		return cache;
	}

	/**
	 * Add a Class, Comparator pair as delegate when
	 * both input are assignable to Class.
	 * Note that additions are checked in the order they are added,
	 * so add the lowest subtypes first.  (i.e., if you add
	 * a comparator for Class Object first, none of the others
	 * will ever match.)
	 * @param accepts the Class supertype of objects to accept
	 * @param comparator the Comparator to use on such objects
	 * @return false if not added, because either input is null
	 *         or because the Class is represented already.
	 */
	public final boolean addComparator(Class accepts, Comparator comparator) {
		if ((null == accepts) || (null == comparator)
			|| (contains(accepts))) {
			return false;
		}
		comparators.addElement(new Struct(accepts, comparator));
		return true;
	}

	/** 
	 * This implements compare by delegating
	 * to the first input Comparator 
	 * where the class of the both input
	 * is assignable to the pair Class.
	 * It first enforces the null-semantics of CompareUtil.compare(..).
	 * @throws ClassCastException if both input are not null and
	 *         they are not assignable to any registered Comparator.
	 */
	public final int compare(Object lhs, Object rhs) {
		int result = CompareUtil.compare(lhs, rhs);
		if (Integer.MAX_VALUE == result) {
			Class lhClass = lhs.getClass() ;
			Class rhClass = rhs.getClass() ;
			Struct[] comp = getComparators();
			Class compClass;
			for (int i = 0; i < comp.length; i++) {
				compClass = comp[i].accepts;
				if ((compClass.isAssignableFrom(lhClass))
					&& (compClass.isAssignableFrom(lhClass))) {
					return comp[i].comparator.compare(lhs, rhs);
				}
			}
			// not found - throw ClassCastException
			String mssg = "Unable to find Comparator for "
				+ "lhs Class: " + lhClass.getName()
				+ " rhs Class: " + rhClass.getName();
			throw new ClassCastException(mssg);
		}
		return result;
	}

	/** 
	 * (unnecessary) Struct to hold class-comparator pair 
	 * is preparation for using collections
	 */
	static class Struct {
		public final Class accepts;
		public final Comparator comparator;
		/**
		 * @param accepts the Class to accept input for - not null
		 * @param comparator the Comparator to compare input with - not null
		 */
		public Struct(Class accepts,Comparator comparator) {
			this.accepts = accepts;
			this.comparator = comparator;
		}
		/** delegate to accept hashcode */
		public int hashCode() { return accepts.hashCode() ; }
		/** WARNING: fast comparison based only on accept key reference identity */
		public boolean equals(Object o) {
			return ((null != o) && (o instanceof Struct)
					&& (accepts == ((Struct)o).accepts));
		}
	} // class Struct

	//----------------------------------------------- test code 
	/** test code */
	static class Test { // todo move elsewhere
		public void runTest(String[] args) {
			if ((null == args) || (1 > args.length)) {
				args = new String[] { "one", "two", "THREE", "FOUR", "fIVE", "6" };
			}
			SubTypeComparator me = new SubTypeComparator();
			String[] copy = new String[args.length];
			System.arraycopy(args, 0, copy, 0, copy.length);
			List list = Arrays.asList(args);
			Throwable result  = test(me, list);
			if ((null == result)
				 && (!ClassCastException.class.isAssignableFrom(result.getClass()))) {
				System.err.println("FAIL: expected cce: " + result);
			}
			me.addComparator(String.class, Collator.getInstance());
			me.addComparator(Object.class, new Comparator () { 
					public int compare (Object lhs, Object rhs) { 
						throw new Error("never used");
					}});
			result  = test(me, list);
			if (null != result) {
				if (ClassCastException.class.isAssignableFrom(result.getClass())) {
					System.err.println("FAIL: unexpected cce: " + result);
				} else {
					System.err.println("FAIL: unexpected Throwable: " + result);
				}
			}
			// heterogeneous - pick Object
			Object[] temp = new Object[] { "string", new Integer(1) };
			result  = test(me, Arrays.asList(temp));
			if ((null == result)
				 && (!Error.class.isAssignableFrom(result.getClass()))) {
				System.err.println("FAIL: expected Error: " + result);
			}
			
			StringBuffer toPrint = print(Arrays.asList(copy), null);
			toPrint.append("\n");
			print(list, toPrint);
			System.err.println(toPrint.toString());
		}

		StringBuffer print(List list, StringBuffer sb) {
			if (null == sb) sb = new StringBuffer();
			sb.append("[");
			ListIterator iter = list.listIterator();
			while (iter.hasNext()) {
				sb.append(iter.next().toString());
				if (iter.hasNext()) sb.append(", ");
			}
			sb.append("]");
			return sb;
		}

		/**
		 * run comparison, return true if got expected exception
		 */
		Throwable test(Comparator c, List l) {
			try {
				Collections.sort(l,c);
				return null;
			} catch (Throwable r) {
				return r;
			}
		}
	} // class Test

	/** invoke Test.runTest(args) todo remove */
	public static void main(String[] args) {
		new Test().runTest(args);
	}
} // class SubTypeComparator
