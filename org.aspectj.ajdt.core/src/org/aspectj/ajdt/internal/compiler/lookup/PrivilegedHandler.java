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


package org.aspectj.ajdt.internal.compiler.lookup;

import java.util.*;

import org.aspectj.ajdt.internal.compiler.ast.AspectDeclaration;
import org.aspectj.weaver.*;
import org.eclipse.jdt.internal.compiler.lookup.*;



public class PrivilegedHandler implements IPrivilegedHandler {
	private AspectDeclaration inAspect;
	private Map accessors = new HashMap();

	public PrivilegedHandler(AspectDeclaration inAspect) {
		this.inAspect = inAspect;
	}

	public FieldBinding getPrivilegedAccessField(FieldBinding baseField) {
		ResolvedMember key = inAspect.world.makeResolvedMember(baseField);
		if (accessors.containsKey(key)) return (FieldBinding)accessors.get(key);
		FieldBinding ret = new PrivilegedFieldBinding(inAspect, baseField);
		accessors.put(key, ret);
		return ret;
	}

	public MethodBinding getPrivilegedAccessMethod(MethodBinding baseMethod) {
		ResolvedMember key = inAspect.world.makeResolvedMember(baseMethod);
		if (accessors.containsKey(key)) return (MethodBinding)accessors.get(key);
		MethodBinding ret = inAspect.world.makeMethodBinding(
			AjcMemberMaker.privilegedAccessMethodForMethod(inAspect.typeX, key)
		);
		
		//new PrivilegedMethodBinding(inAspect, baseMethod);
		accessors.put(key, ret);
		return ret;
	}
	
	public ResolvedMember[] getMembers() {
		Collection m = accessors.keySet();
		int len = m.size();
		ResolvedMember[] ret = new ResolvedMember[len];
		int index = 0;
		for (Iterator i = m.iterator(); i.hasNext(); ) {
			ret[index++] = (ResolvedMember)i.next();
		}
		return ret;
	}

}
