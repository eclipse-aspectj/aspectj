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


package org.aspectj.weaver;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Set;

public class NewParentTypeMunger extends ResolvedTypeMunger {
	ResolvedTypeX newParent;
	
	public NewParentTypeMunger(ResolvedTypeX newParent) {
		super(Parent, null);
		this.newParent = newParent;
	}

	public void write(DataOutputStream s) throws IOException {
		throw new RuntimeException("unimplemented");
	}


	public ResolvedTypeX getNewParent() {
		return newParent;
	}


}
