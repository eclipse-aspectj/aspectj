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

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Set;

import org.aspectj.bridge.MessageUtil;
import org.aspectj.util.FuzzyBoolean;
import org.aspectj.weaver.ISourceContext;
import org.aspectj.weaver.ResolvedTypeX;
import org.aspectj.weaver.Shadow;
import org.aspectj.weaver.VersionedDataInputStream;
import org.aspectj.weaver.WeaverMessages;
import org.aspectj.weaver.ast.Test;

public class PerFromSuper extends PerClause {
	private PerClause.Kind kind;
	
	public PerFromSuper(PerClause.Kind kind) {
		this.kind = kind;
	}

	public Set couldMatchKinds() {
		return Shadow.ALL_SHADOW_KINDS;
	}

	public FuzzyBoolean fastMatch(FastMatchInfo type) {
		throw new RuntimeException("unimplemented");
	}
	
    protected FuzzyBoolean matchInternal(Shadow shadow) {
        throw new RuntimeException("unimplemented");
    }

    public void resolveBindings(IScope scope, Bindings bindings) {
    	// this method intentionally left blank
    }

    protected Test findResidueInternal(Shadow shadow, ExposedState state) {
    	throw new RuntimeException("unimplemented");
    }


	public PerClause concretize(ResolvedTypeX inAspect) {
		PerClause p = lookupConcretePerClause(inAspect.getSuperclass());
		if (p == null) {
			inAspect.getWorld().getMessageHandler().handleMessage(
			  MessageUtil.error(WeaverMessages.format(WeaverMessages.MISSING_PER_CLAUSE,inAspect.getSuperclass()), getSourceLocation())
			);
		}
		if (p.getKind() != kind) {
			inAspect.getWorld().getMessageHandler().handleMessage(
			  MessageUtil.error(WeaverMessages.format(WeaverMessages.WRONG_PER_CLAUSE,kind,p.getKind()),
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
    
	public static PerClause readPerClause(VersionedDataInputStream s, ISourceContext context) throws IOException {
		PerFromSuper ret = new PerFromSuper(Kind.read(s));
		ret.readLocation(context, s);
		return ret;
	}
	
	public String toString() {
		return "perFromSuper(" + kind + ", " + inAspect + ")";
	}
	
	public String toDeclarationString() {
		return "";
	}

	public PerClause.Kind getKind() {
		return kind;
	}

}
