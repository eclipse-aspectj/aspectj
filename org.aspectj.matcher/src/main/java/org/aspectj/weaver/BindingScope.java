/* *******************************************************************
 * Copyright (c) 2006-2008 Contributors
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * ******************************************************************/
package org.aspectj.weaver;

import org.aspectj.bridge.ISourceLocation;
import org.aspectj.weaver.patterns.FormalBinding;
import org.aspectj.weaver.patterns.SimpleScope;

/**
 * BindingScope that knows the enclosingType, which is needed for pointcut reference resolution
 * 
 * @author Alexandre Vasseur
 * @author Andy Clement
 */
public class BindingScope extends SimpleScope {
	private final ResolvedType enclosingType;
	private final ISourceContext sourceContext;
	private boolean importsUpdated = false;

	public BindingScope(ResolvedType type, ISourceContext sourceContext, FormalBinding[] bindings) {
		super(type.getWorld(), bindings);
		this.enclosingType = type;
		this.sourceContext = sourceContext;
	}

	public ResolvedType getEnclosingType() {
		return enclosingType;
	}

	public ISourceLocation makeSourceLocation(IHasPosition location) {
		return sourceContext.makeSourceLocation(location);
	}

	public UnresolvedType lookupType(String name, IHasPosition location) {
		// bug 126560
		if (enclosingType != null && !importsUpdated) {
			// add the package we're in to the list of imported
			// prefixes so that we can find types in the same package
			String pkgName = enclosingType.getPackageName();
			if (pkgName != null && !pkgName.equals("")) {
				String[] existingImports = getImportedPrefixes();
				String pkgNameWithDot = pkgName.concat(".");
				boolean found = false;
				for (String existingImport : existingImports) {
					if (existingImport.equals(pkgNameWithDot)) {
						found = true;
						break;
					}
				}
				if (!found) {
					String[] newImports = new String[existingImports.length + 1];
					System.arraycopy(existingImports, 0, newImports, 0, existingImports.length);
					newImports[existingImports.length] = pkgNameWithDot;
					setImportedPrefixes(newImports);
				}
			}
			importsUpdated = true;
		}
		return super.lookupType(name, location);
	}
}