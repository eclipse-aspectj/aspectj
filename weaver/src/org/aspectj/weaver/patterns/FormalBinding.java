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


package org.aspectj.weaver.patterns;

import org.aspectj.weaver.IHasPosition;
import org.aspectj.weaver.TypeX;

public class FormalBinding implements IHasPosition {
	private final TypeX type;
    private final String name;
	private final int index;
	private final int start, end;
	private final String fileName;
	
	public FormalBinding(TypeX type, String name, int index, int start, int end, String fileName) {
		this.type = type;
        this.name = name;
		this.index = index;
		this.start = start;
		this.end = end;
        this.fileName = fileName;
	}
	
    public FormalBinding(TypeX type, int index) {
        this(type, "unknown", index, 0, 0, "unknown");
    }

    public FormalBinding(TypeX type, String name, int index) {
        this(type, name, index, 0, 0, "unknown");
    }
	
    // ----
    
	public String toString() {
		return type.toString() + ":" + index;
	}
	
	public String getFileName() {
		return fileName; 
    }

	public int getEnd() {
		return end;
	}

	public int getStart() {
		return start;
	}

    public int getIndex() {
        return index;
    }

    public String getName() {
        return name;
    }

    public TypeX getType() {
        return type;
    }

    // ----
    
    public static final FormalBinding[] NONE = new FormalBinding[0];

}
