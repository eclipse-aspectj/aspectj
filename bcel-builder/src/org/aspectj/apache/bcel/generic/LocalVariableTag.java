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

public final class LocalVariableTag extends Tag {
	private Type type; // not always known, in which case signature has to be used
    private final String signature;
    private final String name;
    private int slot;
    private final int startPos;
    boolean remapped = false;

    // AMC - pr101047, two local vars with the same name can share the same slot, but must in that case
    // have different start positions.
    public LocalVariableTag(String sig, String name, int slot, int startPosition) {
        this.signature = sig;
        this.name = name;
        this.slot = slot;
        this.startPos = startPosition;
    }
    
    public LocalVariableTag(Type t,String sig, String name, int slot, int startPosition) {
    	this.type = t;
        this.signature = sig;
        this.name = name;
        this.slot = slot;
        this.startPos = startPosition;
    }


    public String getName()   {return name;}
    public int getSlot()      {return slot;}
    public String getType()   {return signature;}
    public Type getRealType() {return type;}
    
    public void updateSlot(int newSlot) {
    	this.slot = newSlot;
    	this.remapped = true;
    }
    
    public boolean isRemapped() { return this.remapped; }
    
    // ---- from Object
    
    public String toString() {
        return "local " + slot + ": " + signature + " " + name;
    }

    public boolean equals(Object other) {
        if (!(other instanceof LocalVariableTag)) return false;
        LocalVariableTag o = (LocalVariableTag)other;
        return o.slot == slot && o.startPos == startPos && o.signature.equals(signature) && o.name.equals(name); 
    }
    
    private int hashCode = 0;
    public int hashCode() {
        if (hashCode == 0) {
            int ret = 17;
            ret = 37*ret + signature.hashCode();
            ret = 37*ret + name.hashCode();
            ret = 37*ret + slot;
            ret = 37*ret + startPos;
            hashCode = ret;
        }
        return hashCode;
    }
}
