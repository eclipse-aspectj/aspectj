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

import java.io.DataInputStream;
import java.io.IOException;

import org.aspectj.weaver.ISourceContext;

public abstract class Declare extends PatternNode {
	public static final byte ERROR_OR_WARNING = 1;
	public static final byte PARENTS = 2;
	public static final byte SOFT = 3;
	public static final byte DOMINATES = 4;

	public static Declare read(DataInputStream s, ISourceContext context) throws IOException {
		byte kind = s.readByte();
		switch (kind) {
			case ERROR_OR_WARNING:
				return DeclareErrorOrWarning.read(s, context);
			case DOMINATES:
				return DeclarePrecedence.read(s, context);
			case PARENTS:
				return DeclareParents.read(s, context);
			case SOFT:
				return DeclareSoft.read(s, context);
			default:
				throw new RuntimeException("unimplemented");
		}
	}
	
    /**
     * Returns this declare mutated
     */
    public abstract void resolve(IScope scope);
    
    /**
     * Indicates if this declare should be treated like advice.  If true, the
     * declare will have no effect in an abstract aspect.  It will be inherited by
     * any concrete aspects and will have an effect for each concrete aspect it
     * is ultimately inherited by.
     */
    public abstract boolean isAdviceLike();
}
