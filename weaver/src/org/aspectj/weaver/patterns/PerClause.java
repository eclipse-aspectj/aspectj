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

import org.aspectj.util.TypeSafeEnum;
import org.aspectj.weaver.*;

public abstract class PerClause extends Pointcut {
	protected ResolvedTypeX inAspect;

	public static PerClause readPerClause(DataInputStream s, ISourceContext context) throws IOException {
		Kind kind = Kind.read(s);
		if (kind == SINGLETON) return PerSingleton.readPerClause(s, context);
		else if (kind == PERCFLOW) return PerCflow.readPerClause(s, context);
		else if (kind == PEROBJECT) return PerObject.readPerClause(s, context);
		else if (kind == FROMSUPER) return PerFromSuper.readPerClause(s, context);
			
		throw new BCException("unknown kind: " + kind);
	}

    public final Pointcut concretize1(ResolvedTypeX inAspect, IntMap bindings) {
    	throw new RuntimeException("unimplemented: wrong concretize");
    }

	public abstract PerClause concretize(ResolvedTypeX inAspect);
	
	public abstract PerClause.Kind getKind();
	
	public static class Kind extends TypeSafeEnum {
        public Kind(String name, int key) { super(name, key); }
        
        public static Kind read(DataInputStream s) throws IOException {
            int key = s.readByte();
            switch(key) {
                case 1: return SINGLETON;
                case 2: return PERCFLOW;
                case 3: return PEROBJECT;
                case 4: return FROMSUPER;
            }
            throw new BCException("weird kind " + key);
        }
    }
    
	public static final Kind SINGLETON = new Kind("issingleton", 1);
	public static final Kind PERCFLOW  = new Kind("percflow", 2);
	public static final Kind PEROBJECT  = new Kind("perobject", 3);
	public static final Kind FROMSUPER  = new Kind("fromsuper", 4);
}
