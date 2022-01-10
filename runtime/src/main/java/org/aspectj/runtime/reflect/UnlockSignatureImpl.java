/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *
 * Contributors:
 *     Andy Clement - initial implementation
 *******************************************************************************/


package org.aspectj.runtime.reflect;

import java.lang.reflect.Modifier;

import org.aspectj.lang.reflect.UnlockSignature;

class UnlockSignatureImpl extends SignatureImpl implements UnlockSignature {
    private Class<?> parameterType;

    UnlockSignatureImpl(Class<?> c) {
        super(Modifier.STATIC, "unlock", c);
        parameterType = c;
    }

    UnlockSignatureImpl(String stringRep) {
        super(stringRep);
    }

    protected String createToString(StringMaker sm) {
        if (parameterType == null) parameterType = extractType(3);
        return "unlock("+sm.makeTypeName(parameterType)+")";
    }

    public Class getParameterType() {
        if (parameterType == null) parameterType = extractType(3);
        return parameterType;
    }
}
