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

import java.io.DataInputStream;
import java.io.IOException;

import org.aspectj.util.TypeSafeEnum;

public class MemberKind extends TypeSafeEnum {
	public MemberKind(String name, int key) {
		super(name, key);
	}

	public static MemberKind read(DataInputStream s) throws IOException {
		int key = s.readByte();
		switch (key) {
		case 1:
			return Member.METHOD;
		case 2:
			return Member.FIELD;
		case 3:
			return Member.CONSTRUCTOR;
		case 4:
			return Member.STATIC_INITIALIZATION;
		case 5:
			return Member.POINTCUT;
		case 6:
			return Member.ADVICE;
		case 7:
			return Member.HANDLER;
		case 8:
			return Member.MONITORENTER;
		case 9:
			return Member.MONITOREXIT;
		}
		throw new BCException("Unexpected memberkind, should be (1-9) but was " + key);
	}
}