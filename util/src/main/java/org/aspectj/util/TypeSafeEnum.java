/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Xerox/PARC     initial implementation 
 * ******************************************************************/

package org.aspectj.util;

import java.io.*;

public class TypeSafeEnum {
	private byte key;
	private String name;

	public TypeSafeEnum(String name, int key) {
		this.name = name;
		if (key > Byte.MAX_VALUE || key < Byte.MIN_VALUE) {
			throw new IllegalArgumentException("key doesn't fit into a byte: " + key);
		}
		this.key = (byte) key;
	}

	public String toString() {
		return name;
	}

	public String getName() {
		return name;
	}

	public byte getKey() {
		return key;
	}

	public void write(DataOutputStream s) throws IOException {
		s.writeByte(key);
	}
}
