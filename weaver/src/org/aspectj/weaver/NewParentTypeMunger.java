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


package org.aspectj.weaver;

import java.io.DataOutputStream;
import java.io.IOException;

public class NewParentTypeMunger extends ResolvedTypeMunger {
	ResolvedType newParent;
	
	public NewParentTypeMunger(ResolvedType newParent) {
		super(Parent, null);
		this.newParent = newParent;
	}

	public void write(DataOutputStream s) throws IOException {
		throw new RuntimeException("unimplemented");
	}


	public ResolvedType getNewParent() {
		return newParent;
	}

    public boolean equals(Object other) {
        if (! (other instanceof NewParentTypeMunger)) return false;
        NewParentTypeMunger o = (NewParentTypeMunger) other;
        return newParent.equals(o.newParent);
    }
	   
    private volatile int hashCode = 0;
    public int hashCode() {
	    if (hashCode == 0) {
	    	int result = 17;
	    	result = 37*result + newParent.hashCode();
	    	hashCode = result;
	    }
        return hashCode;
    }
}
