/* *******************************************************************
 * Copyright (c) 2002,2005 Contributors.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     PARC     initial implementation 
 *     Andy Clement - upgrade to support fields targetting generic types
 * ******************************************************************/

package org.aspectj.ajdt.internal.compiler.lookup;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.aspectj.org.eclipse.jdt.core.compiler.CharOperation;
import org.aspectj.org.eclipse.jdt.core.compiler.IProblem;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.CompilationUnitScope;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.IMemberFinder;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.InvocationSite;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.ProblemFieldBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.TypeVariableBinding;

/**
 * The member finder looks after intertype declared members on a type, there is one member finder per type that was hit by an ITD.
 */
public class InterTypeMemberFinder implements IMemberFinder {
	private List<FieldBinding> interTypeFields = new ArrayList<>();
	private List<MethodBinding> interTypeMethods = new ArrayList<>();

	public SourceTypeBinding sourceTypeBinding;

	@Override
	public FieldBinding getField(SourceTypeBinding sourceTypeBinding, char[] fieldName, InvocationSite site, Scope scope) {
		FieldBinding retField = sourceTypeBinding.getFieldBase(fieldName, true); // XXX may need to get the correct value for second
		// parameter in the future (see #55341)
		if (interTypeFields.isEmpty()) {
			return retField;
		}
		int fieldLength = fieldName.length;

		for (FieldBinding field : interTypeFields) {
			if (field.name.length == fieldLength && CharOperation.prefixEquals(field.name, fieldName)) {
				retField = resolveConflicts(sourceTypeBinding, retField, field, site, scope);
			}
		}

		return retField;
	}

	private FieldBinding resolveConflicts(SourceTypeBinding sourceTypeBinding, FieldBinding retField, FieldBinding field,
			InvocationSite site, Scope scope) {
		if (retField == null) {
			return field;
		}
		if (site != null) {
			if (!field.canBeSeenBy(sourceTypeBinding, site, scope)) {
				return retField;
			}
			if (!retField.canBeSeenBy(sourceTypeBinding, site, scope)) {
				return field;
			}
		}
		// XXX need dominates check on aspects
		return new ProblemFieldBinding(retField.declaringClass, retField.name, IProblem.AmbiguousField);// ProblemReporter.Ambiguous);
	}

	// private void reportConflicts(SourceTypeBinding sourceTypeBinding,
	// MethodBinding m1, MethodBinding m2)
	// {
	// if (m1 == m2) {
	// System.err.println("odd that we're ecomparing the same: " + m1);
	// return;
	// }
	//
	// if (!m1.areParametersEqual(m2)) return;

	// if (m1 instanceof InterTypeMethodBinding) {
	// if (m2 instanceof InterTypeMethodBinding) {
	// reportConflictsBoth(sourceTypeBinding,
	// (InterTypeMethodBinding)m1,
	// (InterTypeMethodBinding)m2);
	// } else {
	// reportConflictsOne(sourceTypeBinding,
	// (InterTypeMethodBinding)m1,
	// m2);
	// }
	// } else if (m2 instanceof InterTypeMethodBinding) {
	// reportConflictsOne(sourceTypeBinding,
	// (InterTypeMethodBinding)m2,
	// m1);
	// } else {
	// reportConflictsNone(sourceTypeBinding,
	// m2,
	// m1);
	// }
	// }

	// private void reportConflicts(
	// SourceTypeBinding sourceTypeBinding,
	// MethodBinding m1,
	// MethodBinding m2)
	// {
	// //System.err.println("compare: " + m1 + " with " + m2);
	//
	// if (m1 == m2) {
	// System.err.println("odd that we're ecomparing the same: " + m1);
	// return;
	// }
	//
	// if (!m1.areParametersEqual(m2)) return;
	//
	// //System.err.println("t1: " + getTargetType(m1) + ", " + getTargetType(m2));
	//
	// if (getTargetType(m1) != getTargetType(m2)) return;
	//
	// if (m1.declaringClass == m2.declaringClass) {
	// duplicateMethodBinding(m1, m2);
	// return;
	// }
	//
	//
	// if (m1.isPublic() || m2.isPublic()) {
	// duplicateMethodBinding(m1, m2);
	// return;
	// }
	//
	// // handle the wierd case where the aspect is a subtype of the target
	// if (m2.isProtected()) {
	// if (m2.declaringClass.isSuperclassOf(m1.declaringClass)) {
	// duplicateMethodBinding(m1, m2);
	// }
	// // don't return because we also want to do the package test
	// }
	//
	// if (!m1.isPrivate() || !m2.isPrivate()) {
	// // at least package visible
	// if (m1.declaringClass.getPackage() == m2.declaringClass.getPackage()) {
	// duplicateMethodBinding(m1, m2);
	// }
	// return;
	// }
	//
	// //XXX think about inner types some day
	// }
	// //
	private boolean isVisible(MethodBinding m1, ReferenceBinding s) {
		if (m1.declaringClass == s) {
			return true;
		}

		if (m1.isPublic()) {
			return true;
		}

		// don't need to handle protected
		// if (m1.isProtected()) {

		if (!m1.isPrivate()) {
			// at least package visible
			return (m1.declaringClass.getPackage() == s.getPackage());
		}

		return false;
	}

	//
	// private void duplicateMethodBinding(MethodBinding m1, MethodBinding m2) {
	// ReferenceBinding t1 = m1.declaringClass;
	// ReferenceBinding t2 = m2.declaringClass;
	//
	//
	//
	//
	//
	//
	// if (!(t1 instanceof SourceTypeBinding) || !(t2 instanceof SourceTypeBinding)) {
	// throw new RuntimeException("unimplemented");
	// }
	//
	// SourceTypeBinding s1 = (SourceTypeBinding)t1;
	// SourceTypeBinding s2 = (SourceTypeBinding)t2;
	//
	// if (m1.sourceMethod() != null) {
	// s1.scope.problemReporter().duplicateMethodInType(s2, m1.sourceMethod());
	// }
	// if (m2.sourceMethod() != null) {
	// s2.scope.problemReporter().duplicateMethodInType(s1, m2.sourceMethod());
	// }
	// }

	// private void reportConflictsNone(
	// SourceTypeBinding sourceTypeBinding,
	// MethodBinding m2,
	// MethodBinding m1)
	// {
	// throw new RuntimeException("not possible");
	// }

	// ReferenceBinding t1 = getDeclaringClass(m1);
	// //.declaringClass;
	// ReferenceBinding t2 = getDeclaringClass(m2);
	// //.declaringClass;
	//
	// if (t1 == t2) {
	// AbstractMethodDeclaration methodDecl = m2.sourceMethod(); // cannot be retrieved after binding is lost
	// System.err.println("duplicate: " + t1 + ", " + t2);
	// sourceTypeBinding.scope.problemReporter().duplicateMethodInType(sourceTypeBinding, methodDecl);
	// methodDecl.binding = null;
	// //methods[m] = null; //XXX duplicate problem reports
	// return;
	// }
	//
	// if (!(t1 instanceof SourceTypeBinding) || !(t2 instanceof SourceTypeBinding)) {
	// throw new RuntimeException("unimplemented");
	// }
	//
	// SourceTypeBinding s1 = (SourceTypeBinding)t1;
	// SourceTypeBinding s2 = (SourceTypeBinding)t2;
	//
	//
	// if (m1.canBeSeenBy(s1, null, s2.scope) || m2.canBeSeenBy(s2, null, s1.scope)) {
	// s1.scope.problemReporter().duplicateMethodInType(s2, m1.sourceMethod());
	// s2.scope.problemReporter().duplicateMethodInType(s1, m2.sourceMethod());
	// }
	// }

	// private ReferenceBinding getTargetType(MethodBinding m2) {
	// if (m2 instanceof InterTypeMethodBinding) {
	// return ((InterTypeMethodBinding)m2).getTargetType();
	// }
	//
	// return m2.declaringClass;
	// }

	// find all of my methods, including ITDs
	// PLUS: any public ITDs made on interfaces that I implement
	@Override
	public MethodBinding[] methods(SourceTypeBinding sourceTypeBinding) {
		MethodBinding[] orig = sourceTypeBinding.methodsBase();
		// if (interTypeMethods.isEmpty()) return orig;

		List<MethodBinding> ret = new ArrayList<>(Arrays.asList(orig));
		for (MethodBinding method : interTypeMethods) {
			ret.add(method);
		}

		ReferenceBinding[] interfaces = sourceTypeBinding.superInterfaces();
		for (ReferenceBinding anInterface : interfaces) {
			if (anInterface instanceof SourceTypeBinding) {
				SourceTypeBinding intSTB = (SourceTypeBinding) anInterface;
				addPublicITDSFrom(intSTB, ret);
			}
		}

		if (ret.isEmpty()) {
			return Binding.NO_METHODS;
		}
		return ret.toArray(new MethodBinding[0]);
	}

	private void addPublicITDSFrom(SourceTypeBinding anInterface, List<MethodBinding> accumulator) {
		if (anInterface.memberFinder != null) {
			InterTypeMemberFinder finder = (InterTypeMemberFinder) anInterface.memberFinder;
			for (MethodBinding aBinding: finder.interTypeMethods) {
				if (Modifier.isPublic(aBinding.modifiers)) {
					accumulator.add(aBinding);
				}
			}
		}
		ReferenceBinding superType = anInterface.superclass;
		if (superType instanceof SourceTypeBinding && superType.isInterface()) {
			addPublicITDSFrom((SourceTypeBinding) superType, accumulator);
		}
	}

	// XXX conflicts
	@Override
	public MethodBinding[] getMethods(SourceTypeBinding sourceTypeBinding, char[] selector) {
		// System.err.println("getMethods: " + new String(sourceTypeBinding.signature()) +
		// ", " + new String(selector));

		MethodBinding[] orig = sourceTypeBinding.getMethodsBase(selector);
		if (interTypeMethods.isEmpty()) {
			return orig;
		}

		Set<MethodBinding> ret = new HashSet<>(Arrays.asList(orig));
		// System.err.println("declared method: " + ret + " inters = " + interTypeMethods);

		for (MethodBinding method : interTypeMethods) {
			if (CharOperation.equals(selector, method.selector)) {
				ret.add(method);
			}
		}

		if (ret.isEmpty()) {
			return Binding.NO_METHODS;
		}

		// System.err.println("method: " + ret);

		// check for conflicts
		// int len = ret.size();
		// if (len > 1) {
		// for (int i=0; i <len; i++) {
		// MethodBinding m1 = (MethodBinding)ret.get(i);
		// for (int j=i+1; j < len; j++) {
		// MethodBinding m2 = (MethodBinding)ret.get(j);
		// //reportConflicts(sourceTypeBinding, m1, m2);
		// }
		// }
		// }

		// System.err.println("got methods: " + ret + " on " + sourceTypeBinding);

		return ret.toArray(new MethodBinding[0]);
	}

	@Override
	public MethodBinding getExactMethod(SourceTypeBinding sourceTypeBinding, char[] selector, TypeBinding[] argumentTypes,
			CompilationUnitScope refScope) {
		MethodBinding ret = sourceTypeBinding.getExactMethodBase(selector, argumentTypes, refScope);

		// An intertype declaration may override an inherited member (Bug#50776)
		for (MethodBinding im : interTypeMethods) {
			if (matches(im, selector, argumentTypes)) {
				return im;
			}
		}
		return ret;
	}

	// if (isVisible(im, sourceTypeBinding)) {
	// if (ret == null) {
	// ret = im;
	// } else {
	// ret = resolveOverride(ret, im);
	// }
	// }
	// }
	// }
	// return ret;
	// }

	// private MethodBinding resolveOverride(MethodBinding m1, MethodBinding m2) {
	// ReferenceBinding t1 = getTargetType(m1);
	// ReferenceBinding t2 = getTargetType(m2);
	// if (t1 == t2) {
	// //XXX also need a test for completely matching sigs
	// if (m1.isAbstract()) return m2;
	// else if (m2.isAbstract()) return m1;
	//
	//
	// if (m1 instanceof InterTypeMethodBinding) {
	// //XXX need to handle dominates here
	// EclipseWorld world = EclipseWorld.fromScopeLookupEnvironment(sourceTypeBinding.scope);
	// int cmp = compareAspectPrecedence(world.fromEclipse(m1.declaringClass),
	// world.fromEclipse(m2.declaringClass));
	// if (cmp < 0) return m2;
	// else if (cmp > 0) return m1;
	// }
	//
	// duplicateMethodBinding(m1, m2);
	// return null;
	// }
	// if (t1.isSuperclassOf(t2)) {
	// return m2;
	// }
	// if (t2.isSuperclassOf(t1)) {
	// return m1;
	// }
	//
	// duplicateMethodBinding(m1, m2);
	// return null;
	// }

	// private int compareAspectPrecedence(ResolvedType a1, ResolvedType a2) {
	// World world = a1.getWorld();
	// int ret = world.compareByDominates(a1, a2);
	// if (ret == 0) {
	// if (a1.isAssignableFrom(a2)) return -1;
	// if (a2.isAssignableFrom(a1)) return +1;
	// }
	// return ret;
	// }

	//
	public static boolean matches(MethodBinding m1, MethodBinding m2) {
		return matches(m1, m2.selector, m2.parameters);
		// &&
		// (isVisible(m1, m2.declaringClass) || isVisible(m2, m1.declaringClass));
	}

	public static boolean matches2(MethodBinding newBinding, MethodBinding existingBinding) {
		if (!CharOperation.equals(existingBinding.selector, newBinding.selector)) {
			return false;
		}
		int argCount = existingBinding.parameters.length;
		if (newBinding.parameters.length != argCount) {
			return false;
		}
		TypeBinding[] toMatch = newBinding.parameters;
		boolean matches = true;
		for (int p = 0; p < argCount; p++) {
			if (toMatch[p] != existingBinding.parameters[p]) {
				matches = false;
				break;
			}
		}
		if (!matches) {
			// could be due to generics differences. For example:
			// existingBinding is <T extends Foo> T bar(T t)
			// newBinding is <T extends Foo> T bar(T t)
			// the '==' check isn't going to pass, we need to do a deeper check
			TypeVariableBinding[] existingTVBs = existingBinding.typeVariables;
			TypeVariableBinding[] newTVBs = newBinding.typeVariables;
			if (existingTVBs.length != 0 && existingTVBs.length == newTVBs.length) {
				// Check them
				boolean genericsMatch = true;
				for (int t = 0, max = existingTVBs.length; t < max; t++) {
					char[] existingSig = existingTVBs[t].genericSignature();
					char[] newSig = newTVBs[t].genericSignature();
					if (!CharOperation.equals(existingSig, newSig)) {
						genericsMatch = false;
						break;
					}
				}
				if (genericsMatch) {
					for (int a = 0; a < argCount; a++) {
						char[] existingSig = existingBinding.parameters[a].genericTypeSignature();
						char[] newSig = toMatch[a].genericTypeSignature();
						if (!CharOperation.equals(existingSig, newSig)) {
							genericsMatch = false;
							break;
						}
					}
				}
				if (genericsMatch) {
					matches = true;
				}
			}
		}
		return matches;
	}

	private static boolean matches(MethodBinding method, char[] selector, TypeBinding[] argumentTypes) {
		if (!CharOperation.equals(selector, method.selector)) {
			return false;
		}
		int argCount = argumentTypes.length;
		if (method.parameters.length != argCount) {
			return false;
		}
		TypeBinding[] toMatch = method.parameters;
		for (int p = 0; p < argCount; p++) {
			if (toMatch[p] != argumentTypes[p]) {
				return false;
			}
		}
		return true;
	}

	public void addInterTypeField(FieldBinding binding) {
		// System.err.println("adding: " + binding + " to " + this);
		interTypeFields.add(binding);
	}

	public void addInterTypeMethod(MethodBinding binding) {
		// check for conflicts with existing methods, should really check type as well...
		// System.err.println("adding: " + binding + " to " + sourceTypeBinding);
		// pr284862 - this can leave a broken EclipseResolvedMember with an orphan method binding inside...ought to repair it
		if (isVisible(binding, sourceTypeBinding)) {
			MethodBinding[] baseMethods = sourceTypeBinding.methods;
			for (int i = 0, len = baseMethods.length; i < len; i++) {
				MethodBinding b = baseMethods[i];
				sourceTypeBinding.resolveTypesFor(b); // this will return fast if its already been done.
				if (matches2(binding, b)) {
					// this always means we should remove the existing method
					if (b.sourceMethod() != null) {
						b.sourceMethod().binding = null;
					}
					sourceTypeBinding.removeMethod(i);
					// System.err.println("    left: " + Arrays.asList(sourceTypeBinding.methods));
					break;
				}
			}
		}

		interTypeMethods.add(binding);
	}

	/**
	 * @param name the name of the field
	 * @return true if already know about an intertype field declaration for that field
	 */
	public boolean definesField(String name) {
		List<FieldBinding> l = interTypeFields;
		if (l.size() > 0) {
			char[] nameChars = name.toCharArray();
			for (int i = 0; i < l.size(); i++) {
				FieldBinding fieldBinding = interTypeFields.get(i);
				if (CharOperation.equals(fieldBinding.name, nameChars)) {
					return true;
				}
			}
		}
		return false;
	}

}
