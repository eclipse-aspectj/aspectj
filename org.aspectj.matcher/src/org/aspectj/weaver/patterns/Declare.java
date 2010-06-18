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

package org.aspectj.weaver.patterns;

import java.io.IOException;
import java.util.Map;

import org.aspectj.weaver.ISourceContext;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.VersionedDataInputStream;
import org.aspectj.weaver.World;

public abstract class Declare extends PatternNode {
	public static final byte ERROR_OR_WARNING = 1;
	public static final byte PARENTS = 2;
	public static final byte SOFT = 3;
	public static final byte DOMINATES = 4;
	public static final byte ANNOTATION = 5;
	public static final byte PARENTSMIXIN = 6;
	public static final byte TYPE_ERROR_OR_WARNING = 7;

	// set when reading declare from aspect
	private ResolvedType declaringType;

	public static Declare read(VersionedDataInputStream s, ISourceContext context) throws IOException {
		byte kind = s.readByte();
		switch (kind) {
		case ERROR_OR_WARNING:
			return DeclareErrorOrWarning.read(s, context);
		case DOMINATES:
			return DeclarePrecedence.read(s, context);
		case PARENTS:
			return DeclareParents.read(s, context);
		case SOFT:
			return DeclareSoft.read(s, context);
		case ANNOTATION:
			return DeclareAnnotation.read(s, context);
		case PARENTSMIXIN:
			return DeclareParentsMixin.read(s, context);
		case TYPE_ERROR_OR_WARNING:
			return DeclareTypeErrorOrWarning.read(s, context);
		default:
			throw new RuntimeException("unimplemented");
		}
	}

	/**
	 * Returns this declare mutated
	 */
	public abstract void resolve(IScope scope);

	/**
	 * Returns a version of this declare element in which all references to type variables are replaced with their bindings given in
	 * the map.
	 */
	public abstract Declare parameterizeWith(Map<String, UnresolvedType> typeVariableBindingMap, World w);

	/**
	 * Indicates if this declare should be treated like advice. If true, the declare will have no effect in an abstract aspect. It
	 * will be inherited by any concrete aspects and will have an effect for each concrete aspect it is ultimately inherited by.
	 */
	public abstract boolean isAdviceLike();

	/**
	 * Declares have methods in the .class file against which info can be stored (for example, the annotation in the case of declare
	 * annotation). The name is of the form ajc$declare_XXX_NNN where XXX can optionally be set in this 'getNameSuffix()' method -
	 * depending on whether, at weave time, we want to easily differentiate between the declare methods.
	 */
	public abstract String getNameSuffix();

	public void setDeclaringType(ResolvedType aType) {
		this.declaringType = aType;
	}

	public ResolvedType getDeclaringType() {
		return declaringType;
	}
}
