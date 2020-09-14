/* *******************************************************************
 * Copyright (c) 2002, 2010 Contributors
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
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
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.World;

public class NoTypePattern extends TypePattern {

	public NoTypePattern() {
		super(false, false, new TypePatternList());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.aspectj.weaver.patterns.TypePattern#couldEverMatchSameTypesAs(org.aspectj.weaver.patterns.TypePattern)
	 */
	@Override
	protected boolean couldEverMatchSameTypesAs(TypePattern other) {
		return false;
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
		s.writeByte(NO_KEY);
	}

	/**
	 * @see org.aspectj.weaver.patterns.TypePattern#matches(IType, MatchKind)
	 */
	// public FuzzyBoolean matches(IType type, MatchKind kind) {
	// return FuzzyBoolean.YES;
	// }
	/**
	 * @see org.aspectj.weaver.patterns.TypePattern#matchesSubtypes(ResolvedType)
	 */
	@Override
	protected boolean matchesSubtypes(ResolvedType type) {
		return false;
	}

	@Override
	public boolean isStar() {
		return false;
	}

	@Override
	public String toString() {
		return "<nothing>";
	}// FIXME AV - bad! toString() cannot be parsed back (not idempotent)

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		return (obj instanceof NoTypePattern);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return 17 * 37 * 37;
	}

	@Override
	public Object accept(PatternNodeVisitor visitor, Object data) {
		return visitor.visit(this, data);
	}

	@Override
	public TypePattern parameterizeWith(Map<String,UnresolvedType> arg0, World w) {
		return this;
	}
}
