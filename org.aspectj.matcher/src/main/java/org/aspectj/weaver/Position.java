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

public class Position implements IHasPosition {
	private int start, end;

	public Position(int start, int end) {
		this.start = start;
		this.end = end;
	}

	public int getEnd() {
		return end;
	}

	public int getStart() {
		return start;
	}

}
