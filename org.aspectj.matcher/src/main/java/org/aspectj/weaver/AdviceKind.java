/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     PARC     initial implementation 
 * ******************************************************************/

package org.aspectj.weaver;

import java.io.IOException;

import org.aspectj.util.TypeSafeEnum;

/**
 * The five kinds of advice in AspectJ.
 * 
 * @author Erik Hilsdale
 * @author Jim Hugunin
 */
public class AdviceKind extends TypeSafeEnum {
	private int precedence;
	private boolean isAfter;
	private boolean isCflow;

	public AdviceKind(String name, int key, int precedence, boolean isAfter, boolean isCflow) {
		super(name, key);
		this.precedence = precedence;
		this.isAfter = isAfter;
		this.isCflow = isCflow;
	}

	public static AdviceKind read(VersionedDataInputStream s) throws IOException {
		int key = s.readByte();
		switch (key) {
		case 1:
			return Before;
		case 2:
			return After;
		case 3:
			return AfterThrowing;
		case 4:
			return AfterReturning;
		case 5:
			return Around;
		case 6:
			return CflowEntry;
		case 7:
			return CflowBelowEntry;

		case 8:
			return InterInitializer;

		case 9:
			return PerCflowEntry;
		case 10:
			return PerCflowBelowEntry;
		case 11:
			return PerThisEntry;
		case 12:
			return PerTargetEntry;

		case 13:
			return Softener;

		case 14:
			return PerTypeWithinEntry;
		}
		throw new RuntimeException("unimplemented kind: " + key);
	}

	public static final AdviceKind Before = new AdviceKind("before", 1, 0, false, false);
	public static final AdviceKind After = new AdviceKind("after", 2, 0, true, false);
	public static final AdviceKind AfterThrowing = new AdviceKind("afterThrowing", 3, 0, true, false);
	public static final AdviceKind AfterReturning = new AdviceKind("afterReturning", 4, 0, true, false);
	public static final AdviceKind Around = new AdviceKind("around", 5, 0, false, false);

	// these kinds can't be declared, but are used by the weaver
	public static final AdviceKind CflowEntry = new AdviceKind("cflowEntry", 6, 1, false, true);
	public static final AdviceKind CflowBelowEntry = new AdviceKind("cflowBelowEntry", 7, -1, false, true); // XXX resolve
																											// precednece with the
																											// below
	public static final AdviceKind InterInitializer = new AdviceKind("interInitializer", 8, -2, false, false);

	public static final AdviceKind PerCflowEntry = new AdviceKind("perCflowEntry", 9, 1, false, true);
	public static final AdviceKind PerCflowBelowEntry = new AdviceKind("perCflowBelowEntry", 10, -1, false, true);

	public static final AdviceKind PerThisEntry = new AdviceKind("perThisEntry", 11, 1, false, false);
	public static final AdviceKind PerTargetEntry = new AdviceKind("perTargetEntry", 12, 1, false, false);

	public static final AdviceKind Softener = new AdviceKind("softener", 13, 1, false, false);

	// PTWIMPL Advice representing when aspect should be initialized
	public static final AdviceKind PerTypeWithinEntry = new AdviceKind("perTypeWithinEntry", 14, 1, false, false);

	public static AdviceKind stringToKind(String s) {
		if (s.equals(Before.getName()))
			return Before;
		if (s.equals(After.getName()))
			return After;
		if (s.equals(AfterThrowing.getName()))
			return AfterThrowing;
		if (s.equals(AfterReturning.getName()))
			return AfterReturning;
		if (s.equals(Around.getName()))
			return Around;
		throw new IllegalArgumentException("unknown kind: " + "\"" + s + "\"");
	}

	public boolean isAfter() {
		return this.isAfter;
	}

	public boolean isCflow() {
		return this.isCflow;
	}

	public int getPrecedence() {
		return precedence;
	}

	public boolean isPerEntry() {
		return this == PerCflowEntry || this == PerCflowBelowEntry || this == PerThisEntry || this == PerTargetEntry
				|| this == PerTypeWithinEntry; // PTWIMPL Allow for PTW case
	}

	public boolean isPerObjectEntry() {
		return this == PerThisEntry || this == PerTargetEntry;
	}

}
