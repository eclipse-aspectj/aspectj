/* *******************************************************************
 * Copyright (c) 2002 Contributors
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     PARC     initial implementation 
 *   Andy Clement   pushed down into bcel module
 * ******************************************************************/


package org.aspectj.apache.bcel.generic;


/** A tag is an instruction-targeter that doesn't bother remembering its target(s) */
public abstract class Tag implements InstructionTargeter, Cloneable {

    public Tag() {
    }

    // ---- from InstructionTargeter
    public boolean containsTarget(InstructionHandle ih) {
        return false;
    }

    public void updateTarget(InstructionHandle old_ih, InstructionHandle new_ih) {
        old_ih.removeTargeter(this);
        if (new_ih != null)
            new_ih.addTargeter(this);
    }
    
    public Tag copy() {
    	try {
    		return (Tag)clone();
    	} catch (CloneNotSupportedException e) {
    		throw new RuntimeException("Sanity check, can't clone me");
    	}
    }
}
