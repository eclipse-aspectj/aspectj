/* *******************************************************************
 * Copyright (c) 2002, 2010 Contributors
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *
 * Contributors:
 *     PARC     initial implementation
 * ******************************************************************/
package org.aspectj.weaver.patterns;

import java.io.IOException;
import java.util.Map;

import org.aspectj.util.FuzzyBoolean;
import org.aspectj.weaver.CompressingDataOutputStream;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.World;

public class EllipsisTypePattern extends TypePattern {

	/**
	 * Constructor for EllipsisTypePattern.
	 */
	public EllipsisTypePattern() {
		super(false, false, new TypePatternList());
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.aspectj.weaver.patterns.TypePattern#couldEverMatchSameTypesAs(org.aspectj.weaver.patterns.TypePattern)
	 */
	@Override
	protected boolean couldEverMatchSameTypesAs(TypePattern other) {
		return true;
	}

	/**
	 * @see org.aspectj.weaver.patterns.TypePattern#matchesExactly(ResolvedType)
	 */
	@Override
	protected boolean matchesExactly(ResolvedType type) {
		return false;
	}

	@Override
	protected boolean matchesExactly(ResolvedType type, ResolvedType annotatedType) {
		return false;
	}

	/**
	 * @see org.aspectj.weaver.patterns.TypePattern#matchesInstanceof(ResolvedType)
	 */
	@Override
	public FuzzyBoolean matchesInstanceof(ResolvedType type) {
		return FuzzyBoolean.NO;
	}

	@Override
	public void write(CompressingDataOutputStream s) throws IOException {
		s.writeByte(ELLIPSIS_KEY);
	}

	@Override
	public boolean isEllipsis() {
		return true;
	}

	@Override
	public String toString() {
		return "..";
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		return (obj instanceof EllipsisTypePattern);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return 17 * 37;
	}

	@Override
	public Object accept(PatternNodeVisitor visitor, Object data) {
		return visitor.visit(this, data);
	}

	@Override
	public TypePattern parameterizeWith(Map typeVariableMap, World w) {
		return this;
	}

}
