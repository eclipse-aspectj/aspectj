/* *******************************************************************
 * Copyright (c) 2006 Contributors
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Andy Clement IBM     initial implementation 
 * ******************************************************************/
package org.aspectj.weaver.asm;

import org.aspectj.weaver.AjAttribute;
import org.aspectj.weaver.BCException;
import org.aspectj.org.objectweb.asm.Attribute;
import org.aspectj.org.objectweb.asm.ByteVector;
import org.aspectj.org.objectweb.asm.ClassReader;
import org.aspectj.org.objectweb.asm.ClassWriter;
import org.aspectj.org.objectweb.asm.Label;

class AjASMAttribute extends Attribute {
	
	private boolean unpacked = false;
	private byte[] data;
	
	protected AjASMAttribute(String type) {
		super(type);
	}
	
	protected AjASMAttribute(String type,byte[] data) {
		super(type);
		this.data=data;
	}

	/** 
	 * Initial read of the attribute is super lightweight - no unpacking
	 */
    protected Attribute read(ClassReader cr, int off, int len, char[] buf,
                     int codeOff, Label[] labels) {
             byte[] data = new byte[len];
             System.arraycopy(cr.b, off, data, 0, len);
             return new AjASMAttribute(this.type, data);
    }

    /**
     * These attributes are read only, an attempt to write them violates this fundamental assumption.
     */
    protected ByteVector write(ClassWriter cw, byte[] code, int len, int maxStack, int maxLocals) {
    	throw new BCException("Attempt to write out the AjASMAttribute for "+this.type);
        // return new ByteVector().putByteArray(data, 0, data.length);
    }
     
	 public boolean isUnknown() { return false; }
	 
	 // ---
	 
	 public AjAttribute unpack(AsmDelegate relatedDelegate) {
		 if (unpacked) throw new BCException("Don't unpack an attribute twice!");
		 AjAttribute attr = AjAttribute.read(relatedDelegate.weaverVersion,type,data,relatedDelegate.getSourceContext(),relatedDelegate.getWorld().getMessageHandler());
		 unpacked=true;
		 return attr;
	 }
}