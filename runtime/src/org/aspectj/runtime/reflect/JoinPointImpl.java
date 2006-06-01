/* *******************************************************************
 * Copyright (c) 1999-2001 Xerox Corporation, 
 *               2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Xerox/PARC     initial implementation 
 * ******************************************************************/


package org.aspectj.runtime.reflect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.SourceLocation;
import org.aspectj.runtime.internal.AroundClosure;

class JoinPointImpl implements ProceedingJoinPoint {
    static class StaticPartImpl implements JoinPoint.StaticPart {
        String kind;
        Signature signature;
        SourceLocation sourceLocation;

        public StaticPartImpl(String kind, Signature signature, SourceLocation sourceLocation) {
            this.kind = kind;
            this.signature = signature;
            this.sourceLocation = sourceLocation;
        }

        public String getKind() { return kind; }
        public Signature getSignature() { return signature; }
        public SourceLocation getSourceLocation() { return sourceLocation; }

        String toString(StringMaker sm) {
            StringBuffer buf = new StringBuffer();
            buf.append(sm.makeKindName(getKind()));
            buf.append("(");
            buf.append(((SignatureImpl)getSignature()).toString(sm));
            buf.append(")");
            return buf.toString();
        }

        public final String toString() { return toString(StringMaker.middleStringMaker); }
        public final String toShortString() { return toString(StringMaker.shortStringMaker); }
        public final String toLongString() { return toString(StringMaker.longStringMaker); }
    }

    static class EnclosingStaticPartImpl extends StaticPartImpl implements EnclosingStaticPart {
        public EnclosingStaticPartImpl(String kind, Signature signature, SourceLocation sourceLocation) {
            super(kind, signature, sourceLocation);
        }
    }

    Object _this;
    Object target;
    Object[] args;
    org.aspectj.lang.JoinPoint.StaticPart staticPart;

    public JoinPointImpl(org.aspectj.lang.JoinPoint.StaticPart staticPart, Object _this, Object target, Object[] args) {
        this.staticPart = staticPart;
        this._this = _this;
        this.target = target;
        this.args = args;
    }

    public Object getThis() { return _this; }
    public Object getTarget() { return target; }
    public Object[] getArgs() {
    	if (args == null) { args = new Object[0]; }
    	Object[] argsCopy = new Object[args.length];
    	System.arraycopy(args,0,argsCopy,0,args.length);
    	return argsCopy; 
    }

    public org.aspectj.lang.JoinPoint.StaticPart getStaticPart() { return staticPart; }

    public String getKind() { return staticPart.getKind(); }
    public Signature getSignature() { return staticPart.getSignature(); }
    public SourceLocation getSourceLocation() { return staticPart.getSourceLocation(); }

    public final String toString() { return staticPart.toString(); }
    public final String toShortString() { return staticPart.toShortString(); }
    public final String toLongString() { return staticPart.toLongString(); }

    // To proceed we need a closure to proceed on
    private AroundClosure arc;
    public void set$AroundClosure(AroundClosure arc) {
        this.arc = arc;
    }

    public Object proceed() throws Throwable {
        // when called from a before advice, but be a no-op
        if (arc == null)
            return null;
        else
            return arc.run(arc.getState());
    }

    public Object proceed(Object[] adviceBindings) throws Throwable {
        // when called from a before advice, but be a no-op
        if (arc == null)
            return null;
        else {
            // state is always consistent with caller?,callee?,formals...,jp
            Object[] state = arc.getState();
            for (int i = state.length-2; i >= 0; i--) {
                int formalIndex = (adviceBindings.length - 1) - (state.length-2) + i;
                if (formalIndex >= 0 && formalIndex < adviceBindings.length) {
                    state[i] = adviceBindings[formalIndex];
                }
            }
            return arc.run(state);
        }
    }


}
