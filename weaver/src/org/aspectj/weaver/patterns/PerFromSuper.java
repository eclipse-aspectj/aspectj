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
import java.io.DataOutputStream;
import java.io.IOException;

import org.aspectj.bridge.MessageUtil;
import org.aspectj.util.FuzzyBoolean;
import org.aspectj.weaver.ISourceContext;
import org.aspectj.weaver.ResolvedTypeX;
import org.aspectj.weaver.Shadow;
import org.aspectj.weaver.ast.Test;

public class PerFromSuper extends PerClause {
	private PerClause.Kind kind;
	
	public PerFromSuper(PerClause.Kind kind) {
		this.kind = kind;
	}
	
	public FuzzyBoolean fastMatch(ResolvedTypeX type) {
		throw new RuntimeException("unimplemented");
	}
	
    public FuzzyBoolean match(Shadow shadow) {
        throw new RuntimeException("unimplemented");
    }

    public void resolveBindings(IScope scope, Bindings bindings) {
    	// this method intentionally left blank
    }

    public Test findResidue(Shadow shadow, ExposedState state) {
    	throw new RuntimeException("unimplemented");
    }


	public PerClause concretize(ResolvedTypeX inAspect) {
		PerClause p = lookupConcretePerClause(inAspect.getSuperclass());
		if (p == null) {
			inAspect.getWorld().getMessageHandler().handleMessage(
			  MessageUtil.error("expected per clause on super aspect not found on " + 
			  					inAspect.getSuperclass(), getSourceLocation())
			);
		}
		if (p.getKind() != kind) {
			inAspect.getWorld().getMessageHandler().handleMessage(
			  MessageUtil.error("wrong kind of per clause on super, expected " + 
			  					kind + " but found " + p.getKind(),
			  					getSourceLocation())
			);
		}
		return p.concretize(inAspect);
	}
	
	
	
	private PerClause lookupConcretePerClause(ResolvedTypeX lookupType) {
		PerClause ret = lookupType.getPerClause();
		if (ret == null) return null;
		if (ret instanceof PerFromSuper) {
			return lookupConcretePerClause(lookupType.getSuperclass());
		}
		return ret;
	}
	

    public void write(DataOutputStream s) throws IOException {
    	FROMSUPER.write(s);
    	kind.write(s);
    	writeLocation(s);
    }
    
	public static PerClause readPerClause(DataInputStream s, ISourceContext context) throws IOException {
		PerFromSuper ret = new PerFromSuper(Kind.read(s));
		ret.readLocation(context, s);
		return ret;
	}
	
	public String toString() {
		return "perFromSuper(" + kind + ", " + inAspect + ")";
	}

	public PerClause.Kind getKind() {
		return kind;
	}

}
