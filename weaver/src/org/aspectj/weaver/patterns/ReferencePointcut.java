/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     Xerox/PARC     initial implementation 
 * ******************************************************************/


package org.aspectj.weaver.patterns;

import java.io.*;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.Map;

import org.apache.bcel.classfile.*;
import org.aspectj.weaver.*;
import org.aspectj.weaver.ast.*;
import org.aspectj.bridge.*;
import org.aspectj.bridge.MessageUtil;
import org.aspectj.util.*;

/**
 */

//XXX needs check that arguments contains no WildTypePatterns
public class ReferencePointcut extends Pointcut {
	public TypeX onType; 
	public TypePattern onTypeSymbolic; 
	public String name;
	public TypePatternList arguments;
	
	//public ResolvedPointcut binding;
	
	public ReferencePointcut(TypePattern onTypeSymbolic, String name, TypePatternList arguments) {
		this.onTypeSymbolic = onTypeSymbolic;
		this.name = name;
		this.arguments = arguments;
	}
	
	public ReferencePointcut(TypeX onType, String name, TypePatternList arguments) {
		this.onType = onType;
		this.name = name;
		this.arguments = arguments;
	}
	
	/**
	 * Could I match any shadows in this JavaClass
	 */
	public boolean fastMatch(JavaClass jc) { return true; }
	
	/**
	 * Do I really match this shadow?
	 */
	public FuzzyBoolean match(Shadow shadow) {
		return FuzzyBoolean.NO;
	}
	
	public String toString() {
		StringBuffer buf = new StringBuffer();
		if (onType != null) {
			buf.append(onType);
			buf.append(".");
//			for (int i=0, len=fromType.length; i < len; i++) {
//				buf.append(fromType[i]);
//				buf.append(".");
//			}
		}
		buf.append(name);
		buf.append(arguments.toString());
		return buf.toString();
	}
	

	public void write(DataOutputStream s) throws IOException {
		//XXX ignores onType
		s.writeByte(Pointcut.REFERENCE);
		if (onType != null) {
			s.writeBoolean(true);
			onType.write(s);
		} else {
			s.writeBoolean(false);
		}
		
		s.writeUTF(name);
		arguments.write(s);
		writeLocation(s);
	}
	
	public static Pointcut read(DataInputStream s, ISourceContext context) throws IOException {
		TypeX onType = null;
		if (s.readBoolean()) {
			onType = TypeX.read(s);
		}
		ReferencePointcut ret = new ReferencePointcut(onType, s.readUTF(), 
					TypePatternList.read(s, context));
		ret.readLocation(context, s);
		return ret;
	}
	
	public void resolveBindings(IScope scope, Bindings bindings) {
		if (onTypeSymbolic != null) {
			onType = onTypeSymbolic.resolveExactType(scope, bindings);
		}
		
		ResolvedTypeX searchType;
		if (onType != null) {
			searchType = scope.getWorld().resolve(onType);
		} else {
			searchType = scope.getEnclosingType();
		}
		
		
		arguments.resolveBindings(scope, bindings, true);
		//XXX ensure that arguments has no ..'s in it
		
		// check that I refer to a real pointcut declaration and that I match
		
		ResolvedPointcutDefinition pointcutDef = searchType.findPointcut(name);
		// if we're not a static reference, then do a lookup of outers
		if (onType == null) {
			while (true) {
				TypeX declaringType = searchType.getDeclaringType();
				if (declaringType == null) break;
				searchType = declaringType.resolve(scope.getWorld());
				pointcutDef = searchType.findPointcut(name);
				if (pointcutDef != null) {
					// make this a static reference
					onType = searchType;
					break;
				}
			}
		}
		
		if (pointcutDef == null) {
			scope.message(IMessage.ERROR, this, "can't find referenced pointcut");
			return;
		}
		
		if (Modifier.isAbstract(pointcutDef.getModifiers())) {
			if (onType != null) {
				scope.message(IMessage.ERROR, this, 
								"can't make static reference to abstract pointcut");
				return;
			} else if (!searchType.isAbstract()) {
				scope.message(IMessage.ERROR, this,
								"can't use abstract pointcut in concrete context");
				return;
			}
		}
		
		
		ResolvedTypeX[] parameterTypes = 
			scope.getWorld().resolve(pointcutDef.getParameterTypes());
		
		if (parameterTypes.length != arguments.size()) {
			scope.message(IMessage.ERROR, this, "incompatible number of arguments to pointcut, expected " +
						parameterTypes.length + " found " + arguments.size());
			return;
		}
		
		
		
		for (int i=0,len=arguments.size(); i < len; i++) {
			TypePattern p = arguments.get(i);
			//we are allowed to bind to pointcuts which use subtypes as this is type safe
			if (!p.matchesSubtypes(parameterTypes[i])) {
				scope.message(IMessage.ERROR, p, "incompatible type, expected " +
						parameterTypes[i] + " found " + p);
				return;
			}
		}
	}
	
	public void postRead(ResolvedTypeX enclosingType) {
		arguments.postRead(enclosingType);
	}

	public Test findResidue(Shadow shadow, ExposedState state) {
		throw new RuntimeException("shouldn't happen");
	}


	//??? This is not thread safe, but this class is not designed for multi-threading
	private boolean concretizing = false;
	public Pointcut concretize1(ResolvedTypeX searchStart, IntMap bindings) {
		if (concretizing) {
			Thread.currentThread().dumpStack();
			searchStart.getWorld().getMessageHandler().handleMessage(
				MessageUtil.error("circular pointcut declaration involving: " + this,
									getSourceLocation()));
			return Pointcut.makeMatchesNothing(Pointcut.CONCRETE);
		}
		
		try {
			concretizing = true;
		
			ResolvedPointcutDefinition pointcutDec;
			if (onType != null) {
				searchStart = onType.resolve(searchStart.getWorld());
				if (searchStart == ResolvedTypeX.MISSING) {
					return Pointcut.makeMatchesNothing(Pointcut.CONCRETE);
				}
			}
			pointcutDec = searchStart.findPointcut(name);
			if (pointcutDec == null) {
				searchStart.getWorld().getMessageHandler().handleMessage(
					MessageUtil.error("can't find pointcut \'" + name + "\' on " + searchStart.getName(), 
									getSourceLocation())
				);
				return Pointcut.makeMatchesNothing(Pointcut.CONCRETE);
			}
			//??? error if we don't find
					
			//System.err.println("start: " + searchStart);
			ResolvedTypeX[] parameterTypes = searchStart.getWorld().resolve(pointcutDec.getParameterTypes());
			
			arguments = arguments.resolveReferences(bindings);
			
			IntMap newBindings = new IntMap();
			for (int i=0,len=arguments.size(); i < len; i++) {
				TypePattern p = arguments.get(i);
				//we are allowed to bind to pointcuts which use subtypes as this is type safe
				if (!p.matchesSubtypes(parameterTypes[i])) {
					throw new BCException("illegal change to pointcut declaration: " + this);
				}
				
			    if (p instanceof BindingTypePattern) {
			    	newBindings.put(i, ((BindingTypePattern)p).getFormalIndex());
			    }
			}
			
			newBindings.copyContext(bindings);
			newBindings.pushEnclosingDefinition(pointcutDec);
			try {
				return pointcutDec.getPointcut().concretize1(searchStart, newBindings);
			} finally {
				newBindings.popEnclosingDefinitition();
			}
			
		} finally {
			concretizing = false;
		}
	}

    public boolean equals(Object other) { 
        if (!(other instanceof ReferencePointcut)) return false;
        ReferencePointcut o = (ReferencePointcut)other;
        return o.name.equals(name) && o.arguments.equals(arguments)
            && ((o.onType == null) ? (onType == null) : o.onType.equals(onType));
    }
    public int hashCode() {
        int result = 17;
        result = 37*result + ((onType == null) ? 0 : onType.hashCode());
        result = 37*result + arguments.hashCode();
        result = 37*result + name.hashCode();
        return result;
    }


}
