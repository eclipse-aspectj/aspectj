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


package org.aspectj.ajdt.internal.core.builder;

import java.util.*;

import org.aspectj.ajdt.internal.compiler.ast.*;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.internal.core.builder.*;

public class AjNameEnvironment extends NameEnvironment {
	private Set aspectDeclarations = new HashSet();

	public AjNameEnvironment(ClasspathLocation[] classpathLocations) {
		super(classpathLocations);
	}

	public AjNameEnvironment(IJavaProject javaProject) {
		super(javaProject);
	}

	//??? do I want this or a more general getAspects???
	public Collection getAspectDeclarations() { return aspectDeclarations; }
	
	public void addAspectDeclaration(AspectDeclaration dec) {
		aspectDeclarations.add(dec);
	}

}
