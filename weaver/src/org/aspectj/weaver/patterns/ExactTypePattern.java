/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     PARC     initial implementation 
 * ******************************************************************/


package org.aspectj.weaver.patterns;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.aspectj.util.FuzzyBoolean;
import org.aspectj.weaver.BCException;
import org.aspectj.weaver.ISourceContext;
import org.aspectj.weaver.ResolvedTypeX;
import org.aspectj.weaver.TypeX;

public class ExactTypePattern extends TypePattern {
	protected TypeX type;

	public static final Map primitiveTypesMap;
	public static final Map boxedPrimitivesMap;
	private static final Map boxedTypesMap;
	
	static {
		primitiveTypesMap = new HashMap();
		primitiveTypesMap.put("int",int.class);
		primitiveTypesMap.put("short",short.class);
		primitiveTypesMap.put("long",long.class);
		primitiveTypesMap.put("byte",byte.class);
		primitiveTypesMap.put("char",char.class);
		primitiveTypesMap.put("float",float.class);
		primitiveTypesMap.put("double",double.class);

		boxedPrimitivesMap = new HashMap();
		boxedPrimitivesMap.put("java.lang.Integer",Integer.class);
		boxedPrimitivesMap.put("java.lang.Short",Short.class);
		boxedPrimitivesMap.put("java.lang.Long",Long.class);
		boxedPrimitivesMap.put("java.lang.Byte",Byte.class);
		boxedPrimitivesMap.put("java.lang.Character",Character.class);
		boxedPrimitivesMap.put("java.lang.Float",Float.class);
		boxedPrimitivesMap.put("java.lang.Double",Double.class);

		
		boxedTypesMap = new HashMap();
		boxedTypesMap.put("int",Integer.class);
		boxedTypesMap.put("short",Short.class);
		boxedTypesMap.put("long",Long.class);
		boxedTypesMap.put("byte",Byte.class);
		boxedTypesMap.put("char",Character.class);
		boxedTypesMap.put("float",Float.class);
		boxedTypesMap.put("double",Double.class);

	}
	
	public ExactTypePattern(TypeX type, boolean includeSubtypes,boolean isVarArgs) {
		super(includeSubtypes,isVarArgs);
		this.type = type;
	}
	
	protected boolean matchesExactly(ResolvedTypeX matchType) {
		return this.type.equals(matchType);
	}
	
	public TypeX getType() { return type; }

	public FuzzyBoolean matchesInstanceof(ResolvedTypeX matchType) {
		// in our world, Object is assignable from anything
		if (type.equals(ResolvedTypeX.OBJECT)) return FuzzyBoolean.YES;
		
		if (type.isAssignableFrom(matchType, matchType.getWorld())) {
			return FuzzyBoolean.YES;
		}
		
		// fix for PR 64262 - shouldn't try to coerce primitives
		if (type.isPrimitive()) {
			return FuzzyBoolean.NO;
		} else {
		    return matchType.isCoerceableFrom(type) ? FuzzyBoolean.MAYBE : FuzzyBoolean.NO;
		}
	}
	
	public boolean matchesExactly(Class matchType) {
		try {
			Class toMatchAgainst = getClassFor(type.getName());
			return matchType == toMatchAgainst;
		} catch (ClassNotFoundException cnfEx) {
			return false;			
		}
	}
	
	public FuzzyBoolean matchesInstanceof(Class matchType) {
		if (matchType.equals(Object.class)) return FuzzyBoolean.YES;
		
		try {
			String typeName = type.getName();
			Class toMatchAgainst = getClassFor(typeName);
			FuzzyBoolean ret = FuzzyBoolean.fromBoolean(toMatchAgainst.isAssignableFrom(matchType));
			if (ret == FuzzyBoolean.NO) {
				if (boxedTypesMap.containsKey(typeName)) {
					// try again with 'boxed' alternative
					toMatchAgainst = (Class) boxedTypesMap.get(typeName);
					ret = FuzzyBoolean.fromBoolean(toMatchAgainst.isAssignableFrom(matchType));
				}
			}
			return ret;
		} catch (ClassNotFoundException cnfEx) {
			return FuzzyBoolean.NO;			
		}
	}
	
	/**
	 * Return YES if any subtype of the static type would match,
	 *        MAYBE if some subtypes could match
	 *        NO if there could never be a match
	 * @param staticType
	 * @return
	 */
	public FuzzyBoolean willMatchDynamically(Class staticType) {
		if (matchesExactly(staticType)) return FuzzyBoolean.YES;
		if (matchesInstanceof(staticType) == FuzzyBoolean.YES) return FuzzyBoolean.YES;
		
		try {
			String typeName = type.getName();
			Class toMatchAgainst = getClassFor(typeName);
			if (toMatchAgainst.isInterface()) return FuzzyBoolean.MAYBE;
			if (staticType.isAssignableFrom(toMatchAgainst)) return FuzzyBoolean.MAYBE;
			return FuzzyBoolean.NO;
		} catch (ClassNotFoundException cnfEx) {
			return FuzzyBoolean.NO;			
		}
	}
	
	private Class getClassFor(String typeName) throws ClassNotFoundException {
		Class ret = null;
		ret = (Class) primitiveTypesMap.get(typeName);
		if (ret == null) ret = Class.forName(typeName);
		return ret;
	}
	
    public boolean equals(Object other) {
    	if (!(other instanceof ExactTypePattern)) return false;
    	ExactTypePattern o = (ExactTypePattern)other;
    	return (o.type.equals(this.type) && o.annotationPattern.equals(this.annotationPattern));
    }
    
    public int hashCode() {
        int result = 17;
        result = 37*result + type.hashCode();
        result = 37*result + annotationPattern.hashCode();
        return result;
    }

    private static final byte EXACT_VERSION = 1; // rev if changed
	public void write(DataOutputStream out) throws IOException {
		out.writeByte(TypePattern.EXACT);
		out.writeByte(EXACT_VERSION);
		type.write(out);
		out.writeBoolean(includeSubtypes);
		out.writeBoolean(isVarArgs);
		annotationPattern.write(out);
		writeLocation(out);
	}
	
	public static TypePattern read(DataInputStream s, ISourceContext context) throws IOException {
		byte version = s.readByte();
		if (version > EXACT_VERSION) throw new BCException("ExactTypePattern was written by a more recent version of AspectJ");
		TypePattern ret = new ExactTypePattern(TypeX.read(s), s.readBoolean(), s.readBoolean());
		ret.setAnnotationTypePattern(AnnotationTypePattern.read(s,context));
		ret.readLocation(context, s);
		return ret;
	}

    public String toString() {
    	//Thread.currentThread().dumpStack();
    	return type.toString() + (includeSubtypes ? "+" : "");
    }
	public TypePattern resolveBindings(IScope scope, Bindings bindings, 
    								boolean allowBinding, boolean requireExactType)
    { 
		throw new BCException("trying to re-resolve");
		
	}
	
	public TypePattern resolveBindingsFromRTTI(boolean allowBinding, boolean requireExactType) {
		throw new IllegalStateException("trying to re-resolve");
	}

}
