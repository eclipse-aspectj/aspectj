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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.ISourceLocation;
import org.aspectj.bridge.Message;
import org.aspectj.weaver.CompressingDataOutputStream;
import org.aspectj.weaver.ISourceContext;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.VersionedDataInputStream;
import org.aspectj.weaver.WeaverMessages;
import org.aspectj.weaver.World;

public class DeclareParents extends Declare {
	protected TypePattern child;
	protected TypePatternList parents;
	private boolean isWildChild = false;
	protected boolean isExtends = true;

	// private String[] typeVariablesInScope = new String[0]; // AspectJ 5 extension for generic types

	public DeclareParents(TypePattern child, List parents, boolean isExtends) {
		this(child, new TypePatternList(parents), isExtends);
	}

	protected DeclareParents(TypePattern child, TypePatternList parents, boolean isExtends) {
		this.child = child;
		this.parents = parents;
		this.isExtends = isExtends;
		if (child instanceof WildTypePattern) {
			isWildChild = true;
		}
	}

	// public String[] getTypeParameterNames() {
	// return this.typeVariablesInScope;
	// }
	//	
	// public void setTypeParametersInScope(String[] typeParameters) {
	// this.typeVariablesInScope = typeParameters;
	// }

	public boolean match(ResolvedType typeX) {
		if (!child.matchesStatically(typeX)) {
			return false;
		}
		if (typeX.getWorld().getLint().typeNotExposedToWeaver.isEnabled() && !typeX.isExposedToWeaver()) {
			typeX.getWorld().getLint().typeNotExposedToWeaver.signal(typeX.getName(), getSourceLocation());
		}

		return true;
	}

	@Override
	public Object accept(PatternNodeVisitor visitor, Object data) {
		return visitor.visit(this, data);
	}

	@Override
	public Declare parameterizeWith(Map typeVariableBindingMap, World w) {
		DeclareParents ret = new DeclareParents(child.parameterizeWith(typeVariableBindingMap, w), parents.parameterizeWith(
				typeVariableBindingMap, w), isExtends);
		ret.copyLocationFrom(this);
		return ret;
	}

	@Override
	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("declare parents: ");
		buf.append(child);
		buf.append(isExtends ? " extends " : " implements "); // extends and implements are treated equivalently
		buf.append(parents);
		buf.append(";");
		return buf.toString();
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof DeclareParents)) {
			return false;
		}
		DeclareParents o = (DeclareParents) other;
		return o.child.equals(child) && o.parents.equals(parents);
	}

	// ??? cache this
	@Override
	public int hashCode() {
		int result = 23;
		result = 37 * result + child.hashCode();
		result = 37 * result + parents.hashCode();
		return result;
	}

	@Override
	public void write(CompressingDataOutputStream s) throws IOException {
		s.writeByte(Declare.PARENTS);
		child.write(s);
		parents.write(s);
		// s.writeInt(typeVariablesInScope.length);
		// for (int i = 0; i < typeVariablesInScope.length; i++) {
		// s.writeUTF(typeVariablesInScope[i]);
		// }
		writeLocation(s);
	}

	public static Declare read(VersionedDataInputStream s, ISourceContext context) throws IOException {
		DeclareParents ret = new DeclareParents(TypePattern.read(s, context), TypePatternList.read(s, context), true);
		// if (s.getMajorVersion()>=AjAttribute.WeaverVersionInfo.WEAVER_VERSION_MAJOR_AJ150) {
		// int numTypeVariablesInScope = s.readInt();
		// ret.typeVariablesInScope = new String[numTypeVariablesInScope];
		// for (int i = 0; i < numTypeVariablesInScope; i++) {
		// ret.typeVariablesInScope[i] = s.readUTF();
		// }
		// }
		ret.readLocation(context, s);
		return ret;
	}

	public boolean parentsIncludeInterface(World w) {
		for (int i = 0; i < parents.size(); i++) {
			if (parents.get(i).getExactType().resolve(w).isInterface()) {
				return true;
			}
		}
		return false;
	}

	public boolean parentsIncludeClass(World w) {
		for (int i = 0; i < parents.size(); i++) {
			if (parents.get(i).getExactType().resolve(w).isClass()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void resolve(IScope scope) {
		// ScopeWithTypeVariables resolutionScope = new ScopeWithTypeVariables(typeVariablesInScope,scope);
		child = child.resolveBindings(scope, Bindings.NONE, false, false);
		isWildChild = (child instanceof WildTypePattern);
		parents = parents.resolveBindings(scope, Bindings.NONE, false, true);

		// Could assert this ...
		// for (int i=0; i < parents.size(); i++) {
		// parents.get(i).assertExactType(scope.getMessageHandler());
		// }
	}

	public TypePatternList getParents() {
		return parents;
	}

	public TypePattern getChild() {
		return child;
	}

	// note - will always return true after deserialization, this doesn't affect weaver
	public boolean isExtends() {
		return this.isExtends;
	}

	@Override
	public boolean isAdviceLike() {
		return false;
	}

	private ResolvedType maybeGetNewParent(ResolvedType targetType, TypePattern typePattern, World world, boolean reportErrors) {
		if (typePattern == TypePattern.NO) {
			return null; // already had an error here
		}

		// isWildChild = (child instanceof WildTypePattern);
		UnresolvedType iType = typePattern.getExactType();
		ResolvedType parentType = iType.resolve(world);

		if (targetType.equals(world.getCoreType(UnresolvedType.OBJECT))) {
			world.showMessage(IMessage.ERROR, WeaverMessages.format(WeaverMessages.DECP_OBJECT), this.getSourceLocation(), null);
			return null;
		}

		// Ensure the target doesn't already have an
		// alternate parameterization of the generic type on it
		if (parentType.isParameterizedType() || parentType.isRawType()) {
			// Let's take a look at the parents we already have
			boolean isOK = verifyNoInheritedAlternateParameterization(targetType, parentType, world);
			if (!isOK) {
				return null;
			}
		}

		if (parentType.isAssignableFrom(targetType)) {
			return null; // already a parent
		}

		// Enum types that are targetted for decp through a wild type pattern get linted
		if (reportErrors && isWildChild && targetType.isEnum()) {
			world.getLint().enumAsTargetForDecpIgnored.signal(targetType.toString(), getSourceLocation());
		}

		// Annotation types that are targetted for decp through a wild type pattern get linted
		if (reportErrors && isWildChild && targetType.isAnnotation()) {
			world.getLint().annotationAsTargetForDecpIgnored.signal(targetType.toString(), getSourceLocation());
		}

		// 1. Can't use decp to make an enum/annotation type implement an interface
		if (targetType.isEnum() && parentType.isInterface()) {
			if (reportErrors && !isWildChild) {
				world.showMessage(IMessage.ERROR, WeaverMessages.format(WeaverMessages.CANT_DECP_ON_ENUM_TO_IMPL_INTERFACE,
						targetType), getSourceLocation(), null);
			}
			return null;
		}
		if (targetType.isAnnotation() && parentType.isInterface()) {
			if (reportErrors && !isWildChild) {
				world.showMessage(IMessage.ERROR, WeaverMessages.format(WeaverMessages.CANT_DECP_ON_ANNOTATION_TO_IMPL_INTERFACE,
						targetType), getSourceLocation(), null);
			}
			return null;
		}

		// 2. Can't use decp to change supertype of an enum/annotation
		if (targetType.isEnum() && parentType.isClass()) {
			if (reportErrors && !isWildChild) {
				world.showMessage(IMessage.ERROR, WeaverMessages.format(WeaverMessages.CANT_DECP_ON_ENUM_TO_EXTEND_CLASS,
						targetType), getSourceLocation(), null);
			}
			return null;
		}
		if (targetType.isAnnotation() && parentType.isClass()) {
			if (reportErrors && !isWildChild) {
				world.showMessage(IMessage.ERROR, WeaverMessages.format(WeaverMessages.CANT_DECP_ON_ANNOTATION_TO_EXTEND_CLASS,
						targetType), getSourceLocation(), null);
			}
			return null;
		}

		// 3. Can't use decp to declare java.lang.Enum/java.lang.annotation.Annotation as the parent of a type
		if (parentType.getSignature().equals(UnresolvedType.ENUM.getSignature())) {
			if (reportErrors && !isWildChild) {
				world.showMessage(IMessage.ERROR, WeaverMessages
						.format(WeaverMessages.CANT_DECP_TO_MAKE_ENUM_SUPERTYPE, targetType), getSourceLocation(), null);
			}
			return null;
		}
		if (parentType.getSignature().equals(UnresolvedType.ANNOTATION.getSignature())) {
			if (reportErrors && !isWildChild) {
				world.showMessage(IMessage.ERROR, WeaverMessages.format(WeaverMessages.CANT_DECP_TO_MAKE_ANNOTATION_SUPERTYPE,
						targetType), getSourceLocation(), null);
			}
			return null;
		}

		if (parentType.isAssignableFrom(targetType)) {
			return null; // already a parent
		}

		if (targetType.isAssignableFrom(parentType)) {
			world.showMessage(IMessage.ERROR, WeaverMessages.format(WeaverMessages.CANT_EXTEND_SELF, targetType.getName()), this
					.getSourceLocation(), null);
			return null;
		}

		if (parentType.isClass()) {
			if (targetType.isInterface()) {
				world.showMessage(IMessage.ERROR, WeaverMessages.format(WeaverMessages.INTERFACE_CANT_EXTEND_CLASS), this
						.getSourceLocation(), null);
				return null;
				// how to handle xcutting errors???
			}

			if (!targetType.getSuperclass().isAssignableFrom(parentType)) {
				world.showMessage(IMessage.ERROR, WeaverMessages.format(WeaverMessages.DECP_HIERARCHY_ERROR, iType.getName(),
						targetType.getSuperclass().getName()), this.getSourceLocation(), null);
				return null;
			} else {
				return parentType;
			}
		} else {
			return parentType;
		}
	}

	/**
	 * This method looks through the type hierarchy for some target type - it is attempting to find an existing parameterization
	 * that clashes with the new parent that the user wants to apply to the type. If it finds an existing parameterization that
	 * matches the new one, it silently completes, if it finds one that clashes (e.g. a type already has A<String> when the user
	 * wants to add A<Number>) then it will produce an error.
	 * 
	 * It uses recursion and exits recursion on hitting 'jlObject'
	 * 
	 * Related bugzilla entries: pr110788
	 */
	private boolean verifyNoInheritedAlternateParameterization(ResolvedType typeToVerify, ResolvedType newParent, World world) {

		if (typeToVerify.equals(ResolvedType.OBJECT)) {
			return true;
		}

		ResolvedType newParentGenericType = newParent.getGenericType();
		Iterator iter = typeToVerify.getDirectSupertypes();
		while (iter.hasNext()) {
			ResolvedType supertype = (ResolvedType) iter.next();
			if (((supertype.isRawType() && newParent.isParameterizedType()) || (supertype.isParameterizedType() && newParent
					.isRawType()))
					&& newParentGenericType.equals(supertype.getGenericType())) {
				// new parent is a parameterized type, but this is a raw type
				world.getMessageHandler().handleMessage(
						new Message(WeaverMessages.format(WeaverMessages.CANT_DECP_MULTIPLE_PARAMETERIZATIONS, newParent.getName(),
								typeToVerify.getName(), supertype.getName()), getSourceLocation(), true,
								new ISourceLocation[] { typeToVerify.getSourceLocation() }));
				return false;
			}
			if (supertype.isParameterizedType()) {
				ResolvedType generictype = supertype.getGenericType();

				// If the generic types are compatible but the parameterizations aren't then we have a problem
				if (generictype.isAssignableFrom(newParentGenericType) && !supertype.isAssignableFrom(newParent)) {
					world.getMessageHandler().handleMessage(
							new Message(WeaverMessages.format(WeaverMessages.CANT_DECP_MULTIPLE_PARAMETERIZATIONS, newParent
									.getName(), typeToVerify.getName(), supertype.getName()), getSourceLocation(), true,
									new ISourceLocation[] { typeToVerify.getSourceLocation() }));
					return false;
				}
			}
			if (!verifyNoInheritedAlternateParameterization(supertype, newParent, world)) {
				return false;
			}
		}
		return true;
	}

	public List<ResolvedType> findMatchingNewParents(ResolvedType onType, boolean reportErrors) {
		if (onType.isRawType()) {
			onType = onType.getGenericType();
		}
		if (!match(onType)) {
			return Collections.emptyList();
		}

		List<ResolvedType> ret = new ArrayList<ResolvedType>();
		for (int i = 0; i < parents.size(); i++) {
			ResolvedType t = maybeGetNewParent(onType, parents.get(i), onType.getWorld(), reportErrors);
			if (t != null) {
				ret.add(t);
			}
		}

		return ret;
	}

	@Override
	public String getNameSuffix() {
		return "parents";
	}

	public boolean isMixin() {
		return false;
	}
}
