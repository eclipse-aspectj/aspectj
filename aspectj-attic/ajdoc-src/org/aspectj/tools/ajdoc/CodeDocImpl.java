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

import org.aspectj.compiler.base.ast.ASTObject;
import org.aspectj.compiler.base.ast.CodeDec;
import org.aspectj.compiler.base.ast.Dec;
import org.aspectj.compiler.base.ast.DummySourceLocation;
import org.aspectj.compiler.base.ast.Formals;
import org.aspectj.compiler.base.ast.NameType;
import org.aspectj.compiler.base.ast.SourceLocation;
import org.aspectj.compiler.base.ast.TypeDec;
import org.aspectj.compiler.base.ast.TypeDs;
import org.aspectj.compiler.crosscuts.ast.AdviceDec;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public abstract class CodeDocImpl extends ExecutableMemberDocImpl {

    /** The CodeDec to which we delegate. */
    private final CodeDec codeDec;
    
    public CodeDocImpl(com.sun.javadoc.ClassDoc containingClass, CodeDec codeDec) {
        super(containingClass);
        this.codeDec = codeDec;
    }

    protected Collection createAdvice() {
        Set affectedBy = ajc().getCorrespondences().getAffectedBy(codeDec());
        if (affectedBy.size() < 1) return Collections.EMPTY_LIST;
        List list = new ArrayList();
        for (Iterator i = affectedBy.iterator(); i.hasNext();) {
            AdviceDec adec = (AdviceDec)i.next();
            TypeDec owner = ((NameType)adec.getDeclaringType()).getTypeDec();
            AspectDocImpl ad = (AspectDocImpl)ClassDocImpl.getInstance(owner);
            AdviceDocImpl adoc = ad.docForDec(adec);
            list.add(adoc);
        }
        return list;
    }
    
    protected Dec dec() {
        return codeDec();
    }

    protected Formals getFormals() {
        return codeDec().getFormals();
    }

    protected TypeDs getThrows() {
        return codeDec().getThrows();
    }

    protected CodeDec codeDec() {
        return codeDec;
    }
}

