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
 */
public class BindingScope extends SimpleScope {
	private final ResolvedType m_enclosingType;
	private final ISourceContext m_sourceContext;

	public BindingScope(ResolvedType type, ISourceContext sourceContext, FormalBinding[] bindings) {
		super(type.getWorld(), bindings);
		m_enclosingType = type;
		m_sourceContext = sourceContext;
	}

	public ResolvedType getEnclosingType() {
		return m_enclosingType;
	}

	public ISourceLocation makeSourceLocation(IHasPosition location) {
		return m_sourceContext.makeSourceLocation(location);
	}

	public UnresolvedType lookupType(String name, IHasPosition location) {
		// bug 126560
		if (m_enclosingType != null) {
			// add the package we're in to the list of imported
			// prefixes so that we can find types in the same package
			String pkgName = m_enclosingType.getPackageName();
			if (pkgName != null && !pkgName.equals("")) {
				String[] currentImports = getImportedPrefixes();
				String[] newImports = new String[currentImports.length + 1];
				for (int i = 0; i < currentImports.length; i++) {
					newImports[i] = currentImports[i];
				}
				newImports[currentImports.length] = pkgName.concat(".");
				setImportedPrefixes(newImports);
			}
		}
		return super.lookupType(name, location);
	}

}