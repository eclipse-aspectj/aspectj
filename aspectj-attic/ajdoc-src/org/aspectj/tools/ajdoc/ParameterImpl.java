/* -*- Mode: JDE; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 *
 * This file is part of the debugger and core tools for the AspectJ(tm)
 * programming language; see http://aspectj.org
 *
 * The contents of this file are subject to the Mozilla Public License
 * Version 1.1 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * either http://www.mozilla.org/MPL/ or http://aspectj.org/MPL/.
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is AspectJ.
 *
 * The Initial Developer of the Original Code is Xerox Corporation. Portions
 * created by Xerox Corporation are Copyright (C) 1999-2002 Xerox Corporation.
 * All Rights Reserved.
 */
package org.aspectj.tools.ajdoc;

import org.aspectj.compiler.base.ast.FormalDec;

import com.sun.javadoc.Type;

public class ParameterImpl implements org.aspectj.ajdoc.Parameter {

    private final FormalDec formalDec;

    public ParameterImpl(FormalDec formalDec) {
        this.formalDec = formalDec;
    }

    /**
     * Returns the name of this formal.
     *
     * @return the name of this formal.
     */
    public String name() {
        return formalDec.getId();
    }
    
    /**
     * Returns the type of this formal.
     *
     * @return the type of this formal.
     */
    public Type type() {
        return TypeImpl.getInstance(formalDec.getType());
    }

    /**
     * Returns the type name of this formal including
     * any dimension information.
     *
     * @return the type name of this formal including
     *         any dimension information.
     */
    public String typeName() {
        return type().qualifiedTypeName() + type().dimension();
    }

    public boolean equals(Object o) {
        if (!(o instanceof ParameterImpl)) return false;
        ParameterImpl p = (ParameterImpl)o;
        return typeName().equals(p.typeName());
    }
}
