/* *******************************************************************
 * Copyright (c) 1999-2001 Xerox Corporation, 
 *               2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     Xerox/PARC     initial implementation 
 * ******************************************************************/


package org.aspectj.runtime.reflect;

import org.aspectj.lang.*;
import org.aspectj.lang.reflect.*;

public final class Factory {    
    Class lexicalClass;
    ClassLoader lookupClassLoader;
    String filename;
    public Factory(String filename, Class lexicalClass) {
        //System.out.println("making
        this.filename = filename;
        this.lexicalClass = lexicalClass;
        lookupClassLoader = lexicalClass.getClassLoader();
    }
    
    public JoinPoint.StaticPart makeSJP(String kind, Signature sig, SourceLocation loc) {
        return new JoinPointImpl.StaticPartImpl(kind, sig, loc);
    }
    
    public JoinPoint.StaticPart makeSJP(String kind, Signature sig, int l, int c) {
        return new JoinPointImpl.StaticPartImpl(kind, sig, makeSourceLoc(l, c));
    }
    
    public JoinPoint.StaticPart makeSJP(String kind, Signature sig, int l) {
        return new JoinPointImpl.StaticPartImpl(kind, sig, makeSourceLoc(l, -1));
    }
    
    public static JoinPoint makeJP(JoinPoint.StaticPart staticPart, 
                        Object _this, Object target, Object[] args)
    {
        return new JoinPointImpl(staticPart, _this, target, args);
    }
    
    public MethodSignature makeMethodSig(String stringRep) {
        MethodSignatureImpl ret = new MethodSignatureImpl(stringRep);
        ret.setLookupClassLoader(lookupClassLoader);
        return ret;
    }

    public ConstructorSignature makeConstructorSig(String stringRep) {
        ConstructorSignatureImpl ret = new ConstructorSignatureImpl(stringRep);
        ret.setLookupClassLoader(lookupClassLoader);
        return ret;
    }

    public FieldSignature makeFieldSig(String stringRep) {
        FieldSignatureImpl ret = new FieldSignatureImpl(stringRep);
        ret.setLookupClassLoader(lookupClassLoader);
        return ret;
    }

    public AdviceSignature makeAdviceSig(String stringRep) {
        AdviceSignatureImpl ret = new AdviceSignatureImpl(stringRep);
        ret.setLookupClassLoader(lookupClassLoader);
        return ret;
    }

    public InitializerSignature makeInitializerSig(String stringRep) {
        InitializerSignatureImpl ret = new InitializerSignatureImpl(stringRep);
        ret.setLookupClassLoader(lookupClassLoader);
        return ret;
    }

    public CatchClauseSignature makeCatchClauseSig(String stringRep) {
        CatchClauseSignatureImpl ret = new CatchClauseSignatureImpl(stringRep);
        ret.setLookupClassLoader(lookupClassLoader);
        return ret;
    }
    

    public SourceLocation makeSourceLoc(int line, int col)
    {
        return new SourceLocationImpl(lexicalClass, this.filename, line, col);
    }
}
