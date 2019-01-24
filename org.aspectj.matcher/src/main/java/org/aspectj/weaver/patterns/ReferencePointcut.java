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
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.MessageUtil;
import org.aspectj.util.FuzzyBoolean;
import org.aspectj.weaver.CompressingDataOutputStream;
import org.aspectj.weaver.ISourceContext;
import org.aspectj.weaver.IntMap;
import org.aspectj.weaver.ResolvedPointcutDefinition;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.Shadow;
import org.aspectj.weaver.ShadowMunger;
import org.aspectj.weaver.TypeVariable;
import org.aspectj.weaver.TypeVariableReference;
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.VersionedDataInputStream;
import org.aspectj.weaver.WeaverMessages;
import org.aspectj.weaver.World;
import org.aspectj.weaver.ast.Test;

/**
 */
// XXX needs check that arguments contains no WildTypePatterns
public class ReferencePointcut extends Pointcut {
	public UnresolvedType onType;
	public TypePattern onTypeSymbolic;
	public String name;
	public TypePatternList arguments;

	/**
	 * if this is non-null then when the pointcut is concretized the result will be parameterized too.
	 */
	private Map<String, UnresolvedType> typeVariableMap;

	// public ResolvedPointcut binding;

	public ReferencePointcut(TypePattern onTypeSymbolic, String name, TypePatternList arguments) {
		this.onTypeSymbolic = onTypeSymbolic;
		this.name = name;
		this.arguments = arguments;
		this.pointcutKind = REFERENCE;
	}

	public ReferencePointcut(UnresolvedType onType, String name, TypePatternList arguments) {
		this.onType = onType;
		this.name = name;
		this.arguments = arguments;
		this.pointcutKind = REFERENCE;
	}

	public int couldMatchKinds() {
		return Shadow.ALL_SHADOW_KINDS_BITS;
	}

	// ??? do either of these match methods make any sense???
	public FuzzyBoolean fastMatch(FastMatchInfo type) {
		return FuzzyBoolean.MAYBE;
	}

	/**
	 * Do I really match this shadow?
	 */
	protected FuzzyBoolean matchInternal(Shadow shadow) {
		return FuzzyBoolean.NO;
	}

	public String toString() {
		StringBuffer buf = new StringBuffer();
		if (onType != null) {
			buf.append(onType);
			buf.append(".");
			// for (int i=0, len=fromType.length; i < len; i++) {
			// buf.append(fromType[i]);
			// buf.append(".");
			// }
		}
		buf.append(name);
		buf.append(arguments.toString());
		return buf.toString();
	}

	public void write(CompressingDataOutputStream s) throws IOException {
		// XXX ignores onType
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

	public static Pointcut read(VersionedDataInputStream s, ISourceContext context) throws IOException {
		UnresolvedType onType = null;
		if (s.readBoolean()) {
			onType = UnresolvedType.read(s);
		}
		ReferencePointcut ret = new ReferencePointcut(onType, s.readUTF(), TypePatternList.read(s, context));
		ret.readLocation(context, s);
		return ret;
	}

	public void resolveBindings(IScope scope, Bindings bindings) {
		if (onTypeSymbolic != null) {
			onType = onTypeSymbolic.resolveExactType(scope, bindings);
			// in this case we've already signaled an error
			if (ResolvedType.isMissing(onType)) {
				return;
			}
		}

		ResolvedType searchType;
		if (onType != null) {
			searchType = scope.getWorld().resolve(onType);
		} else {
			searchType = scope.getEnclosingType();
		}
		if (searchType.isTypeVariableReference()) {
			searchType = ((TypeVariableReference) searchType).getTypeVariable().getFirstBound().resolve(scope.getWorld());
		}

		arguments.resolveBindings(scope, bindings, true, true);
		// XXX ensure that arguments has no ..'s in it

		// check that I refer to a real pointcut declaration and that I match

		ResolvedPointcutDefinition pointcutDef = searchType.findPointcut(name);
		// if we're not a static reference, then do a lookup of outers
		if (pointcutDef == null && onType == null) {
			while (true) {
				UnresolvedType declaringType = searchType.getDeclaringType();
				if (declaringType == null) {
					break;
				}
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
			scope.message(IMessage.ERROR, this, "can't find referenced pointcut " + name);
			return;
		}

		// check visibility
		if (!pointcutDef.isVisible(scope.getEnclosingType())) {
			scope.message(IMessage.ERROR, this, "pointcut declaration " + pointcutDef + " is not accessible");
			return;
		}

		if (Modifier.isAbstract(pointcutDef.getModifiers())) {
			if (onType != null && !onType.isTypeVariableReference()) {
				scope.message(IMessage.ERROR, this, "can't make static reference to abstract pointcut");
				return;
			} else if (!searchType.isAbstract()) {
				scope.message(IMessage.ERROR, this, "can't use abstract pointcut in concrete context");
				return;
			}
		}

		ResolvedType[] parameterTypes = scope.getWorld().resolve(pointcutDef.getParameterTypes());

		if (parameterTypes.length != arguments.size()) {
			scope.message(IMessage.ERROR, this, "incompatible number of arguments to pointcut, expected " + parameterTypes.length
					+ " found " + arguments.size());
			return;
		}

		// if (onType == null) onType = pointcutDef.getDeclaringType();
		if (onType != null) {
			if (onType.isParameterizedType()) {
				// build a type map mapping type variable names in the generic type to
				// the type parameters presented
				typeVariableMap = new HashMap<String, UnresolvedType>();
				ResolvedType underlyingGenericType = ((ResolvedType) onType).getGenericType();
				TypeVariable[] tVars = underlyingGenericType.getTypeVariables();
				ResolvedType[] typeParams = ((ResolvedType) onType).getResolvedTypeParameters();
				for (int i = 0; i < tVars.length; i++) {
					typeVariableMap.put(tVars[i].getName(), typeParams[i]);
				}
			} else if (onType.isGenericType()) {
				scope.message(MessageUtil.error(WeaverMessages.format(WeaverMessages.CANT_REFERENCE_POINTCUT_IN_RAW_TYPE),
						getSourceLocation()));
			}
		}

		for (int i = 0, len = arguments.size(); i < len; i++) {
			TypePattern p = arguments.get(i);
			// we are allowed to bind to pointcuts which use subtypes as this is type safe
			if (typeVariableMap != null) {
				p = p.parameterizeWith(typeVariableMap, scope.getWorld());
			}
			if (p == TypePattern.NO) {
				scope.message(IMessage.ERROR, this, "bad parameter to pointcut reference");
				return;
			}

			boolean reportProblem = false;
			if (parameterTypes[i].isTypeVariableReference() && p.getExactType().isTypeVariableReference()) {
				UnresolvedType One = ((TypeVariableReference) parameterTypes[i]).getTypeVariable().getFirstBound();
				UnresolvedType Two = ((TypeVariableReference) p.getExactType()).getTypeVariable().getFirstBound();
				reportProblem = !One.resolve(scope.getWorld()).isAssignableFrom(Two.resolve(scope.getWorld()));
			} else {
				reportProblem = !p.matchesSubtypes(parameterTypes[i]) && !p.getExactType().equals(UnresolvedType.OBJECT);
			}
			if (reportProblem) {
				scope.message(IMessage.ERROR, this, "incompatible type, expected " + parameterTypes[i].getName() + " found " + p
						+ ".  Check the type specified in your pointcut");
				return;
			}
		}

	}

	public void postRead(ResolvedType enclosingType) {
		arguments.postRead(enclosingType);
	}

	protected Test findResidueInternal(Shadow shadow, ExposedState state) {
		throw new RuntimeException("shouldn't happen");
	}

	// ??? This is not thread safe, but this class is not designed for multi-threading
	private boolean concretizing = false;

	// declaring type is the type that declared the member referencing this pointcut.
	// If it declares a matching private pointcut, then that pointcut should be used
	// and not one in a subtype that happens to have the same name.
	public Pointcut concretize1(ResolvedType searchStart, ResolvedType declaringType, IntMap bindings) {
		if (concretizing) {
			// Thread.currentThread().dumpStack();
			searchStart
					.getWorld()
					.getMessageHandler()
					.handleMessage(
							MessageUtil.error(WeaverMessages.format(WeaverMessages.CIRCULAR_POINTCUT, this), getSourceLocation()));
			Pointcut p = Pointcut.makeMatchesNothing(Pointcut.CONCRETE);
			p.sourceContext = sourceContext;
			return p;
		}

		try {
			concretizing = true;

			ResolvedPointcutDefinition pointcutDec;
			if (onType != null) {
				searchStart = onType.resolve(searchStart.getWorld());
				if (searchStart.isMissing()) {
					return Pointcut.makeMatchesNothing(Pointcut.CONCRETE);
				}

				if (onType.isTypeVariableReference()) {
					// need to replace on type with the binding for the type variable
					// in the declaring type
					if (declaringType.isParameterizedType()) {
						TypeVariable[] tvs = declaringType.getGenericType().getTypeVariables();
						String typeVariableName = ((TypeVariableReference) onType).getTypeVariable().getName();
						for (int i = 0; i < tvs.length; i++) {
							if (tvs[i].getName().equals(typeVariableName)) {
								ResolvedType realOnType = declaringType.getTypeParameters()[i].resolve(declaringType.getWorld());
								onType = realOnType;
								searchStart = realOnType;
								break;
							}
						}
					}
				}

			}

			if (declaringType == null) {
				declaringType = searchStart;
			}
			pointcutDec = declaringType.findPointcut(name);
			boolean foundMatchingPointcut = (pointcutDec != null && Modifier.isPrivate(pointcutDec.getModifiers()));
			if (!foundMatchingPointcut) {
				pointcutDec = searchStart.findPointcut(name);
				if (pointcutDec == null) {
					searchStart
							.getWorld()
							.getMessageHandler()
							.handleMessage(
									MessageUtil.error(
											WeaverMessages.format(WeaverMessages.CANT_FIND_POINTCUT, name, searchStart.getName()),
											getSourceLocation()));
					return Pointcut.makeMatchesNothing(Pointcut.CONCRETE);
				}
			}

			if (pointcutDec.isAbstract()) {
				// Thread.currentThread().dumpStack();
				ShadowMunger enclosingAdvice = bindings.getEnclosingAdvice();
				searchStart.getWorld().showMessage(IMessage.ERROR,
						WeaverMessages.format(WeaverMessages.ABSTRACT_POINTCUT, pointcutDec), getSourceLocation(),
						(null == enclosingAdvice) ? null : enclosingAdvice.getSourceLocation());
				return Pointcut.makeMatchesNothing(Pointcut.CONCRETE);
			}

			// System.err.println("start: " + searchStart);
			// ResolvedType[] parameterTypes = searchStart.getWorld().resolve(pointcutDec.getParameterTypes());

			TypePatternList arguments = this.arguments.resolveReferences(bindings);

			IntMap newBindings = new IntMap();
			for (int i = 0, len = arguments.size(); i < len; i++) {
				TypePattern p = arguments.get(i);
				if (p == TypePattern.NO) {
					continue;
				}
				// we are allowed to bind to pointcuts which use subtypes as this is type safe
				// this will be checked in ReferencePointcut.resolveBindings(). Can't check it here
				// as we don't know about any new parents added via decp.
				if (p instanceof BindingTypePattern) {
					newBindings.put(i, ((BindingTypePattern) p).getFormalIndex());
				}
			}

			if (searchStart.isParameterizedType()) {
				// build a type map mapping type variable names in the generic type to
				// the type parameters presented
				typeVariableMap = new HashMap<String, UnresolvedType>();
				ResolvedType underlyingGenericType = searchStart.getGenericType();
				TypeVariable[] tVars = underlyingGenericType.getTypeVariables();
				ResolvedType[] typeParams = searchStart.getResolvedTypeParameters();
				for (int i = 0; i < tVars.length; i++) {
					typeVariableMap.put(tVars[i].getName(), typeParams[i]);
				}
			}

			newBindings.copyContext(bindings);
			newBindings.pushEnclosingDefinition(pointcutDec);
			try {
				Pointcut ret = pointcutDec.getPointcut();
				if (typeVariableMap != null && !hasBeenParameterized) {
					ret = ret.parameterizeWith(typeVariableMap, searchStart.getWorld());
					ret.hasBeenParameterized = true;
				}
				return ret.concretize(searchStart, declaringType, newBindings);
			} finally {
				newBindings.popEnclosingDefinitition();
			}

		} finally {
			concretizing = false;
		}
	}

	/**
	 * make a version of this pointcut with any refs to typeVariables replaced by their entry in the map. Tricky thing is, we can't
	 * do this at the point in time this method will be called, so we make a version that will parameterize the pointcut it
	 * ultimately resolves to.
	 */
	public Pointcut parameterizeWith(Map<String, UnresolvedType> typeVariableMap, World w) {
		ReferencePointcut ret = new ReferencePointcut(onType, name, arguments);
		ret.onTypeSymbolic = onTypeSymbolic;
		ret.typeVariableMap = typeVariableMap;
		return ret;
	}

	// We want to keep the original source location, not the reference location
	protected boolean shouldCopyLocationForConcretize() {
		return false;
	}

	public boolean equals(Object other) {
		if (!(other instanceof ReferencePointcut)) {
			return false;
		}
		if (this == other) {
			return true;
		}
		ReferencePointcut o = (ReferencePointcut) other;
		return o.name.equals(name) && o.arguments.equals(arguments)
				&& ((o.onType == null) ? (onType == null) : o.onType.equals(onType));
	}

	public int hashCode() {
		int result = 17;
		result = 37 * result + ((onType == null) ? 0 : onType.hashCode());
		result = 37 * result + arguments.hashCode();
		result = 37 * result + name.hashCode();
		return result;
	}

	public Object accept(PatternNodeVisitor visitor, Object data) {
		return visitor.visit(this, data);
	}
}
