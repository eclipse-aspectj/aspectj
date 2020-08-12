/* *******************************************************************
 * Copyright (c) 2005 Contributors.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *   Adrian Colyer			Initial implementation
 * ******************************************************************/
package org.aspectj.weaver.patterns;

import java.io.IOException;

import org.aspectj.weaver.CompressingDataOutputStream;
import org.aspectj.weaver.ISourceContext;
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.VersionedDataInputStream;

/**
 * @author colyer Represents a type variable as declared as part of a type declaration, parameter declaration, or type parameter
 *         specification.
 *         <p>
 *         For example:
 *         </p>
 *         <ul>
 *         <li>&lt;T&gt; T genericMethod(T t) {...}</li>
 *         <li>static &lt;T extends Foo&gt; T staticGenericMethod(T t) {...}</li>
 *         <li>Foo&lt;T extends Bar &amp; IGoo&gt;
 *         </ul>
 */
public class TypeVariablePattern extends PatternNode {

	private static final String anything = "?";

	private String name; // eg. "T"
	private TypePattern upperBound; // default is object unless of the form T extends Bar
	private TypePattern[] interfaceBounds; // additional upper bounds (must be interfaces) arising from
	// declarations of the form T extends Bar & IGoo, IDoo
	private TypePattern lowerBound; // only set if type variable is of the form T super Bar

	/**
	 * Create a named type variable with upper bound Object and no lower bounds. Use this constructor for the simple "T" case
	 */
	public TypeVariablePattern(String variableName) {
		this.name = variableName;
		this.upperBound = new ExactTypePattern(UnresolvedType.OBJECT, false, false);
		this.lowerBound = null;
		this.interfaceBounds = null;
	}

	/**
	 * Create a named type variable with the given upper bound and no lower bounds Use this constructor for the T extends Foo case
	 * 
	 * @param variableName
	 * @param upperBound
	 */
	public TypeVariablePattern(String variableName, TypePattern upperBound) {
		this.name = variableName;
		this.upperBound = upperBound;
		this.lowerBound = null;
		this.interfaceBounds = null;
	}

	public TypeVariablePattern(String variableName, TypePattern upperLimit, TypePattern[] interfaceBounds, TypePattern lowerBound) {
		this.name = variableName;
		this.upperBound = upperLimit;
		if (upperBound == null) {
			upperBound = new ExactTypePattern(UnresolvedType.OBJECT, false, false);
		}
		this.interfaceBounds = interfaceBounds;
		this.lowerBound = lowerBound;
	}

	public Object accept(PatternNodeVisitor visitor, Object data) {
		return visitor.visit(this, data);
	}

	public String getName() {
		return name;
	}

	public boolean isAnythingPattern() {
		return name.equals(anything);
	}

	public TypePattern getRawTypePattern() {
		return upperBound;
	}

	public TypePattern getUpperBound() {
		return upperBound;
	}

	public boolean hasLowerBound() {
		return (lowerBound != null);
	}

	public TypePattern getLowerBound() {
		return lowerBound;
	}

	public boolean hasAdditionalInterfaceBounds() {
		return (interfaceBounds != null);
	}

	public TypePattern[] getAdditionalInterfaceBounds() {
		if (interfaceBounds != null) {
			return interfaceBounds;
		} else {
			return new TypePattern[0];
		}
	}

	public boolean equals(Object obj) {
		if (!(obj instanceof TypeVariablePattern)) {
			return false;
		}
		TypeVariablePattern other = (TypeVariablePattern) obj;
		if (!name.equals(other.name)) {
			return false;
		}
		if (!upperBound.equals(other.upperBound)) {
			return false;
		}
		if (lowerBound != null) {
			if (other.lowerBound == null) {
				return false;
			}
			if (!lowerBound.equals(other.lowerBound)) {
				return false;
			}
		} else {
			if (other.lowerBound != null) {
				return false;
			}
		}
		if (interfaceBounds != null) {
			if (other.interfaceBounds == null) {
				return false;
			}
			if (interfaceBounds.length != other.interfaceBounds.length) {
				return false;
			}
			for (int i = 0; i < interfaceBounds.length; i++) {
				if (!interfaceBounds[i].equals(other.interfaceBounds[i])) {
					return false;
				}
			}
		} else {
			if (other.interfaceBounds != null) {
				return false;
			}
		}
		return true;
	}

	public int hashCode() {
		int hashCode = 17 + (37 * name.hashCode());
		hashCode = hashCode * 37 + upperBound.hashCode();
		if (lowerBound != null) {
			hashCode = hashCode * 37 + lowerBound.hashCode();
		}
		if (interfaceBounds != null) {
			for (TypePattern interfaceBound : interfaceBounds) {
				hashCode = 37 * hashCode + interfaceBound.hashCode();
			}
		}
		return hashCode;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(name);
		sb.append(getExtendsClause());
		if (interfaceBounds != null) {
			sb.append(" & ");
			for (int i = 0; i < interfaceBounds.length; i++) {
				sb.append(interfaceBounds[i].toString());
				if (i < interfaceBounds.length) {
					sb.append(",");
				}
			}
		}
		if (lowerBound != null) {
			sb.append(" super ");
			sb.append(lowerBound.toString());
		}
		return sb.toString();
	}

	private String getExtendsClause() {
		if (upperBound instanceof ExactTypePattern) {
			ExactTypePattern bound = (ExactTypePattern) upperBound;
			if (bound.type == UnresolvedType.OBJECT) {
				return "";
			}
		}
		return " extends " + upperBound.toString();
	}

	public void write(CompressingDataOutputStream s) throws IOException {
		s.writeUTF(name);
		upperBound.write(s);
		if (interfaceBounds == null) {
			s.writeInt(0);
		} else {
			s.writeInt(interfaceBounds.length);
			for (TypePattern interfaceBound : interfaceBounds) {
				interfaceBound.write(s);
			}
		}
		s.writeBoolean(hasLowerBound());
		if (hasLowerBound()) {
			lowerBound.write(s);
		}
		writeLocation(s);
	}

	public static TypeVariablePattern read(VersionedDataInputStream s, ISourceContext context) throws IOException {
		TypeVariablePattern tv = null;
		String name = s.readUTF();
		TypePattern upperBound = TypePattern.read(s, context);
		TypePattern[] additionalInterfaceBounds = null;
		int numInterfaceBounds = s.readInt();
		if (numInterfaceBounds > 0) {
			additionalInterfaceBounds = new TypePattern[numInterfaceBounds];
			for (int i = 0; i < additionalInterfaceBounds.length; i++) {
				additionalInterfaceBounds[i] = TypePattern.read(s, context);
			}
		}
		boolean hasLowerBound = s.readBoolean();
		TypePattern lowerBound = null;
		if (hasLowerBound) {
			lowerBound = TypePattern.read(s, context);
		}
		tv = new TypeVariablePattern(name, upperBound, additionalInterfaceBounds, lowerBound);
		tv.readLocation(context, s);
		return tv;
	}

}
