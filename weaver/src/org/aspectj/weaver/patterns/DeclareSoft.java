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

import org.aspectj.bridge.IMessage;
import org.aspectj.weaver.ISourceContext;
import org.aspectj.weaver.ResolvedTypeX;
import org.aspectj.weaver.TypeX;

public class DeclareSoft extends Declare {
	private TypePattern exception;
	private Pointcut pointcut;

	public DeclareSoft(TypePattern exception, Pointcut pointcut) {
		this.exception = exception;
		this.pointcut = pointcut;
	}
	
	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("declare soft: ");
		buf.append(exception);
		buf.append(": ");
		buf.append(pointcut);
		buf.append(";");
		return buf.toString();
	}
	
	public boolean equals(Object other) { 
		if (!(other instanceof DeclareSoft)) return false;
		DeclareSoft o = (DeclareSoft)other;
		return
			o.pointcut.equals(pointcut) &&
			o.exception.equals(exception);
	}
    
    public int hashCode() {
        int result = 19;
        result = 37*result + pointcut.hashCode();
        result = 37*result + exception.hashCode();
        return result;
    }


	public void write(DataOutputStream s) throws IOException {
		s.writeByte(Declare.SOFT);
		exception.write(s);
		pointcut.write(s);
		writeLocation(s);
	}

	public static Declare read(DataInputStream s, ISourceContext context) throws IOException {
		Declare ret = new DeclareSoft(
			TypePattern.read(s, context),
			Pointcut.read(s, context)
		);
		ret.readLocation(context, s);
		return ret;
	}

	public Pointcut getPointcut() {
		return pointcut;
	}
	
	public TypePattern getException() {
		return exception;
	}

    public void resolve(IScope scope) {
    	exception = exception.resolveBindings(scope, null, false, true);
    	TypeX excType = exception.getExactType();
    	if (excType != ResolvedTypeX.MISSING) {
    		if (!scope.getWorld().resolve(TypeX.THROWABLE).isAssignableFrom(excType)) {
    			scope.getWorld().showMessage(IMessage.ERROR,
    					excType.getName() + " is not a subtype of Throwable",
    					exception.getSourceLocation(), null);
    			pointcut = Pointcut.makeMatchesNothing(Pointcut.RESOLVED);
    			return;
    		}
    	}
    	
    	pointcut = pointcut.resolve(scope); 	
    }
    
    public boolean isAdviceLike() {
		return true;
	}
}
