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

package org.aspectj.ajdt.internal.compiler.lookup;

import java.util.ArrayList;
import java.util.List;

import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.IMessageHandler;
import org.aspectj.bridge.ISourceLocation;
import org.aspectj.bridge.Message;
import org.aspectj.org.eclipse.jdt.core.compiler.CharOperation;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.CompilationUnitScope;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.ImportBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.aspectj.weaver.IHasPosition;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.World;
import org.aspectj.weaver.patterns.FormalBinding;
import org.aspectj.weaver.patterns.IScope;
import org.aspectj.weaver.patterns.WildTypePattern;

/**
 * Adaptor from org.eclipse.jdt.internal.compiler.lookup.Scope to
 * org.aspectj.weaver.IScope
 * 
 * @author Jim Hugunin
 */
public class EclipseScope implements IScope {
	private static final char[] javaLang = "java.lang".toCharArray();
	private static final String[] JL = new String[] { "java.lang." };
	private static final String[] NONE = new String[0];
	private final Scope scope;
	private final EclipseFactory world;
	private final ResolvedType enclosingType;
	private final FormalBinding[] bindings;

	private String[] importedPrefixes = null;
	private String[] importedNames = null;

	public EclipseScope(FormalBinding[] bindings, Scope scope) {
		this.bindings = bindings;

		this.scope = scope;
		world = EclipseFactory.fromScopeLookupEnvironment(scope);

		enclosingType = world.fromEclipse(scope.enclosingSourceType());
	}

	@Override
	public UnresolvedType lookupType(String name, IHasPosition location) {
		char[][] splitName = WildTypePattern.splitNames(name, true);
		TypeBinding b = scope.getType(splitName, splitName.length);
		// FIXME ??? need reasonable error handling...
		if (!b.isValidBinding()) {
			return ResolvedType.MISSING;
		}

		if (referenceFromAnnotationStylePointcut) { // pr265360
			// it must be fully qualified in the pointcut text or in the same
			// package as the type containing the pointcut
			char[] qualifiedPackageName = b.qualifiedPackageName();
			if (!CharOperation.equals(qualifiedPackageName, javaLang)) {
				String packagePrefix = new String(qualifiedPackageName);
				if (!name.startsWith(packagePrefix)) {
					if (validPackage != null && CharOperation.equals(validPackage, qualifiedPackageName)) {
						// it is OK, found in same package as the aspect
					} else {
						return ResolvedType.MISSING;
					}
				}
			}
		}

		// System.err.println("binding: " + b);
		// Binding(tokens, bits & RestrictiveFlagMASK, this)
		return world.fromBinding(b);

		/*
		 * computeImports();
		 * 
		 * // System.out.println("lookup: " + name + " in " + //
		 * Arrays.asList(importedPrefixes));
		 * 
		 * ResolvedType ret = null; String dotName = "." + name; for (int i=0;
		 * i<importedNames.length; i++) { String importedName =
		 * importedNames[i]; //??? can this be right if
		 * (importedName.endsWith(name) && ((importedName.length() ==
		 * name.length()) || (importedName.endsWith(dotName)))) { ResolvedType
		 * found = resolveVisible(importedName); if (found ==
		 * ResolvedType.MISSING) continue; if (ret != null) {
		 * message(IMessage.ERROR, location, "ambiguous type reference, both " +
		 * ret.getName() + " and " + importedName); return ResolvedType.MISSING;
		 * } else { ret = found; } } }
		 * 
		 * if (ret != null) return ret;
		 * 
		 * //XXX need to handle ambiguous references here for (int i=0;
		 * i<importedPrefixes.length; i++) { String importedPrefix =
		 * importedPrefixes[i]; ResolvedType tryType =
		 * resolveVisible(importedPrefix + name); if (tryType !=
		 * ResolvedType.MISSING) { return tryType; } }
		 * 
		 * return resolveVisible(name);
		 */
	}

	// private ResolvedType resolveVisible(String name) {
	// ResolvedType found =
	// world.getWorld().resolve(UnresolvedType.forName(name), true);
	// if (found == ResolvedType.MISSING) return found;
	// if (ResolvedType.isVisible(found.getModifiers(), found, enclosingType))
	// return found;
	// return ResolvedType.MISSING;
	// }

	// public UnresolvedType lookupType(String name, IHasPosition location) {
	// char[][] namePieces = CharOperation.splitOn('.', name.toCharArray());
	// TypeBinding binding;
	// if (namePieces.length == 1) {
	// binding = scope.getType(namePieces[0]);
	// } else {
	// binding = scope.getType(namePieces);
	// }
	//		
	//		
	// if (!binding.isValidBinding()) {
	// //XXX do we do this always or sometimes
	// System.err.println("error: " + binding);
	//scope.problemReporter().invalidType(EclipseWorld.astForLocation(location),
	// binding);
	// return ResolvedType.MISSING;
	// }
	// //??? do we want this too
	// // if (AstNode.isTypeUseDeprecated(binding, scope))
	// // scope.problemReporter().deprecatedType(binding,
	// EclipseWorld.astForLocation(location));
	//		
	// return EclipseWorld.fromBinding(binding);
	// }

	private void computeImports() {
		if (importedNames != null)
			return;

		List<String> importedNamesList = new ArrayList<>();
		List<String> importedPrefixesList = new ArrayList<>();

		Scope currentScope = scope;
		// add any enclosing types to this list
		while (!(currentScope instanceof CompilationUnitScope)) {
			if (currentScope == null) {
				throw new RuntimeException("unimplemented");
			}
			if (currentScope instanceof ClassScope) {
				addClassAndParentsToPrefixes(((ClassScope) currentScope).referenceType().binding, importedPrefixesList);
			}
			currentScope = currentScope.parent;
		}

		CompilationUnitScope cuScope = (CompilationUnitScope) currentScope;

		String packageName = new String(CharOperation.concatWith(cuScope.currentPackageName, '.'));
		// System.err.println("package: " + packageName);
		if (packageName.length() > 0) {
			importedPrefixesList.add(packageName + ".");
		}

		ImportBinding[] imports = cuScope.imports;
		for (ImportBinding importBinding : imports) {
			String importName = new String(CharOperation.concatWith(importBinding.compoundName, '.'));

			// XXX wrong behavior for java.util.Map.*
			if (importBinding.onDemand) {
				importedPrefixesList.add(importName + ".");
			} else {
				importedNamesList.add(importName);
			}
		}

		TypeBinding[] topTypes = cuScope.topLevelTypes;
		for (TypeBinding topType : topTypes) {
			importedNamesList.add(world.fromBinding(topType).getName());
		}

		importedNames = importedNamesList.toArray(new String[0]);

		importedPrefixes = importedPrefixesList.toArray(new String[0]);
	}

	private void addClassAndParentsToPrefixes(ReferenceBinding binding, List<String> importedPrefixesList) {
		if (binding == null)
			return;
		importedPrefixesList.add(world.fromBinding(binding).getName() + "$");

		addClassAndParentsToPrefixes(binding.superclass(), importedPrefixesList);
		ReferenceBinding[] superinterfaces = binding.superInterfaces();
		if (superinterfaces != null) {
			for (ReferenceBinding superinterface : superinterfaces) {
				addClassAndParentsToPrefixes(superinterface, importedPrefixesList);
			}
		}
	}

	@Override
	public String[] getImportedNames() {
		computeImports();
		return importedNames;
	}

	@Override
	public String[] getImportedPrefixes() {
		computeImports();
		// System.err.println("prefixes: " + Arrays.asList(importedPrefixes));
		return importedPrefixes;
	}

	// XXX add good errors when would bind to extra parameters
	@Override
	public FormalBinding lookupFormal(String name) {
		for (FormalBinding binding : bindings) {
			if (binding.getName().equals(name))
				return binding;
		}
		return null;
	}

	@Override
	public FormalBinding getFormal(int i) {
		return bindings[i];
	}

	@Override
	public int getFormalCount() {
		return bindings.length;
	}

	public ISourceLocation makeSourceLocation(IHasPosition location) {
		return new EclipseSourceLocation(scope.problemReporter().referenceContext.compilationResult(), location.getStart(),
				location.getEnd());
	}

	@Override
	public IMessageHandler getMessageHandler() {
		return world.getWorld().getMessageHandler();
	}

	@Override
	public void message(IMessage.Kind kind, IHasPosition location1, IHasPosition location2, String message) {
		message(kind, location1, message);
		message(kind, location2, message);
	}

	@Override
	public void message(IMessage.Kind kind, IHasPosition location, String message) {
		// System.out.println("message: " + message + " loc: " +
		// makeSourceLocation(location));
		getMessageHandler().handleMessage(new Message(message, kind, null, makeSourceLocation(location)));

	}

	@Override
	public void message(IMessage aMessage) {
		getMessageHandler().handleMessage(aMessage);
	}

	@Override
	public World getWorld() {
		return world.getWorld();
	}

	@Override
	public ResolvedType getEnclosingType() {
		return enclosingType;
	}

	private boolean referenceFromAnnotationStylePointcut = false;
	private char[] validPackage;

	/**
	 * Mark this scope as only allowing limited support for imports. This is to
	 * ensure that references in annotation style pointcuts are accidentally
	 * resolved against import statements. They won't be if javac is used (and
	 * the resulting .class file will contain 'bad pointcuts') so this method
	 * enables it to also be policed when compiling with ajc.
	 * 
	 * @param validPackage unqualified references can be resolved if the type is
	 *            in the same package as the type containing the pointcut
	 *            declaration.
	 */
	public void setLimitedImports(char[] validPackage) {
		referenceFromAnnotationStylePointcut = true;
		this.validPackage = validPackage;
		importedPrefixes = JL; // Consider only java.lang. as an import
		importedNames = NONE; // No imported names
	}

}
