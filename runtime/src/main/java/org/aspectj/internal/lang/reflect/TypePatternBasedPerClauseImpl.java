/* *******************************************************************
 * Copyright (c) 2005 Contributors.
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *
 * Contributors:
 *   Adrian Colyer			Initial implementation
 * ******************************************************************/
package org.aspectj.internal.lang.reflect;

import org.aspectj.lang.reflect.PerClauseKind;
import org.aspectj.lang.reflect.TypePattern;
import org.aspectj.lang.reflect.TypePatternBasedPerClause;

/**
 * @author colyer
 *
 */
public class TypePatternBasedPerClauseImpl extends PerClauseImpl implements
		TypePatternBasedPerClause {

	private TypePattern typePattern;

	public TypePatternBasedPerClauseImpl(PerClauseKind kind, String pattern) {
		super(kind);
		this.typePattern = new TypePatternImpl(pattern);
	}

	public TypePattern getTypePattern() {
		return this.typePattern;
	}

	public String toString() {
		return "pertypewithin(" + typePattern.asString() + ")";
	}

}
