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


package org.aspectj.weaver;

import java.io.*;
import java.util.Set;

import org.aspectj.weaver.ResolvedTypeMunger.Kind;
import org.aspectj.weaver.patterns.Pointcut;

public class PrivilegedAccessMunger extends ResolvedTypeMunger {
	public PrivilegedAccessMunger(ResolvedMember member) {
		super(PrivilegedAccess, member);
	}
	

	public void write(DataOutputStream s) throws IOException {
		throw new RuntimeException("shouldn't be serialized");
	}

	public ResolvedMember getMember() {
		return getSignature();
	}

}
