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


package org.aspectj.weaver.bcel;

import org.aspectj.weaver.TypeX;

public final class LocalVariableTag extends Tag {
    private final TypeX type;
    private final String name;
    private final int slot;

    public LocalVariableTag(TypeX type, String name, int slot) {
        this.type = type;
        this.name = name;
        this.slot = slot;
    }

    public String getName() {
        return name;
    }
    public int getSlot() {
        return slot;
    }
    public TypeX getType() {
        return type;
    }
    
    // ---- from Object
    
    public String toString() {
        return "local " + slot + ": " + type + " " + name;
    }
    public boolean equals(Object other) {
        if (!(other instanceof LocalVariableTag)) return false;
        LocalVariableTag o = (LocalVariableTag)other;
        return o.type.equals(type) && o.name.equals(name) && o.slot == slot;
    }
    private volatile int hashCode = 0;
    public int hashCode() {
        if (hashCode == 0) {
            int ret = 17;
            ret = 37*ret + type.hashCode();
            ret = 37*ret + name.hashCode();
            ret = 37*ret + slot;
            hashCode = ret;
        }
        return hashCode;
    }
}
