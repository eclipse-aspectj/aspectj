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
import java.io.IOException;

import org.aspectj.util.TypeSafeEnum;

public class WeaverStateKind extends TypeSafeEnum {
	private WeaverStateKind(String name, int key) {
		super(name, key);
	}
	
	public static final WeaverStateKind read(DataInputStream s) throws IOException {
		byte b = s.readByte();
		switch(b) {
			case 0: return Untouched;
			case 2: return Woven;
		}
		throw new RuntimeException("bad WeaverState.Kind: " + b);
	}

	
	public static final WeaverStateKind Untouched = new WeaverStateKind("Untouched", 0);
	public static final WeaverStateKind Woven = new WeaverStateKind("Woven", 2);
	
	
	public byte[] getBytes() {
		return new byte[] { getKey(), };
	}

	public boolean isWoven() {
		return this == Woven;
	}

}
