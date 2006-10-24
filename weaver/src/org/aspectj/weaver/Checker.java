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

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.aspectj.asm.AsmManager;
import org.aspectj.asm.IRelationship;
import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.ISourceLocation;
import org.aspectj.bridge.Message;
import org.aspectj.weaver.patterns.DeclareErrorOrWarning;
import org.aspectj.weaver.patterns.PerClause;
import org.aspectj.weaver.patterns.Pointcut;


public class Checker extends ShadowMunger {

	private String msg;
	private boolean isError;

	public Checker(DeclareErrorOrWarning deow) {
		super(deow.getPointcut(), deow.getStart(), deow.getEnd(), deow.getSourceContext());
		this.msg = deow.getMessage();
		this.isError = deow.isError();
	}		
	
	private Checker(Pointcut pc, int start, int end, ISourceContext context) {
		super(pc,start,end,context);
	}

    public ShadowMunger concretize(ResolvedType fromType, World world, PerClause clause) {
        pointcut = pointcut.concretize(fromType, getDeclaringType(), 0, this);
        return this;
    }

	public void specializeOn(Shadow shadow) {
		throw new RuntimeException("illegal state");
	}

	public void implementOn(Shadow shadow) {
		throw new RuntimeException("illegal state");
	}
	
	public ShadowMunger parameterizeWith(ResolvedType declaringType,Map typeVariableMap) {
		Checker ret = new Checker(
							getPointcut().parameterizeWith(typeVariableMap,declaringType.getWorld()),
							getStart(),
							getEnd(),
							this.sourceContext);
		ret.msg = this.msg;
		ret.isError = this.isError;
		return ret;
	}

	public boolean match(Shadow shadow, World world) {
		if (super.match(shadow, world)) {
			IMessage message = new Message(
				msg,
				shadow.toString(),
				isError ? IMessage.ERROR : IMessage.WARNING,
				shadow.getSourceLocation(),
                null,
                new ISourceLocation[]{this.getSourceLocation()},true,
				0, // id
				-1,-1); // source start/end
            
            world.getMessageHandler().handleMessage(message);
			
			if (world.getCrossReferenceHandler() != null) {
				world.getCrossReferenceHandler().addCrossReference(this.getSourceLocation(),
				  shadow.getSourceLocation(),
				  (this.isError?IRelationship.Kind.DECLARE_ERROR:IRelationship.Kind.DECLARE_WARNING),false);
			
			}
			
			if (world.getModel() != null) {
				AsmRelationshipProvider.getDefault().checkerMunger(world.getModel(), shadow, this);
			}
		}
		return false;
	}

	public int compareTo(Object other) {
		return 0;
	}
	
	public Collection getThrownExceptions() { return Collections.EMPTY_LIST; }

    /**
     * Default to true
     * FIXME Alex: ATAJ is that ok in all cases ? 
     * @return
     */
    public boolean mustCheckExceptions() { return true; }


	// XXX this perhaps ought to take account of the other fields in advice ...
    public boolean equals(Object other) {
        if (! (other instanceof Checker)) return false;
        Checker o = (Checker) other;
        return  
          o.isError == isError &&
        	 ((o.pointcut == null) ? (pointcut == null) : o.pointcut.equals(pointcut)) &&
          (AsmManager.getDefault().getHandleProvider().dependsOnLocation()
        		  ?((o.getSourceLocation()==null) ? (getSourceLocation()==null): o.getSourceLocation().equals(getSourceLocation())):true) // pr134471 - remove when handles are improved to be independent of location
        	;
    }

	private volatile int hashCode = -1;
    public int hashCode() {
    	if (hashCode == -1) {
            int result = 17;
            result = 37*result + (isError?1:0);
            result = 37*result + ((pointcut == null) ? 0 : pointcut.hashCode());
            hashCode = result;			
		}
        return hashCode;
    }

	public boolean isError() {
		return isError;
	}

}
