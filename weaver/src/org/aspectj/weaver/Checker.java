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

import java.util.Collection;
import java.util.Collections;

import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.Message;
import org.aspectj.weaver.patterns.DeclareErrorOrWarning;
import org.aspectj.weaver.patterns.PerClause;


public class Checker extends ShadowMunger {

	private String msg;
	private boolean isError;

	public Checker(DeclareErrorOrWarning deow) {
		super(deow.getPointcut(), deow.getStart(), deow.getEnd(), deow.getSourceContext());
		this.msg = deow.getMessage();
		this.isError = deow.isError();
	}		 

    public ShadowMunger concretize(ResolvedTypeX fromType, World world, PerClause clause) {
        pointcut = pointcut.concretize(fromType, 0, this);
        return this;
    }

	public void specializeOn(Shadow shadow) {
		throw new RuntimeException("illegal state");
	}

	public void implementOn(Shadow shadow) {
		throw new RuntimeException("illegal state");
	}

	public boolean match(Shadow shadow, World world) {
		if (super.match(shadow, world)) {
			world.getMessageHandler().handleMessage(
				new Message(msg,
							isError ? IMessage.ERROR : IMessage.WARNING,
							null,
							shadow.getSourceLocation()));
		}
		return false;
	}
	
	
	public int compareTo(Object other) {
		return 0;
	}
	
	public Collection getThrownExceptions() { return Collections.EMPTY_LIST; }

}
