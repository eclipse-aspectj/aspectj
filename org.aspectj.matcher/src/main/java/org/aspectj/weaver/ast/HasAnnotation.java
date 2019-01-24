/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.aspectj.weaver.ast;

import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.UnresolvedType;

public class HasAnnotation extends Test {

    private Var v;
    private ResolvedType annType;
    
    public HasAnnotation(Var v, ResolvedType annType) {
        super();
        this.v = v;
        this.annType = annType;
    }
    
    /* (non-Javadoc)
     * @see org.aspectj.weaver.ast.Test#accept(org.aspectj.weaver.ast.ITestVisitor)
     */
    public void accept(ITestVisitor v) {
        v.visit(this);
    }

	public String toString() {
		return "(" + v + " has annotation @" + annType + ")";
	}

	public boolean equals(Object other) {
		if (other instanceof HasAnnotation) {
		    HasAnnotation o = (HasAnnotation) other;
			return o.v.equals(v) && o.annType.equals(annType);
		} else {
			return false;
		}
	}
	
	public int hashCode() {
		return v.hashCode()*37+annType.hashCode();
	}

    public Var getVar() {
        return v;
    }

    public UnresolvedType getAnnotationType() {
        return annType;
    } 
    
}
