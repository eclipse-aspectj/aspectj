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

import org.aspectj.ajdoc.ClassDoc;
import org.aspectj.ajdoc.OfClauseDoc;
import org.aspectj.ajdoc.OfEachObjectDoc;
import org.aspectj.compiler.crosscuts.ast.Pcd;
import org.aspectj.compiler.crosscuts.ast.PerCFlow;
import org.aspectj.compiler.crosscuts.ast.PerClause;
import org.aspectj.compiler.crosscuts.ast.PerObject;
import org.aspectj.compiler.crosscuts.ast.PerSingleton;

import java.util.Collections;
import java.util.List;

public class OfClauseDocImpl {

    /** TODO */
    public final static OfClauseDoc getInstance(PerClause clause) {
        return factory.getInstance(clause);
    }

    /** The factory used to create the instance. */
    private final static Factory factory = new Factory();

    /** TODO */
    private final static class OfEachObjectDocImpl implements OfEachObjectDoc {
        private final List instances;
        private OfEachObjectDocImpl(PerObject eo) {
            instances = createInstances(eo);
        }
        public ClassDoc[] instances() {
            return (ClassDoc[])instances.toArray(new ClassDoc[instances.size()]);
        }
        public OfEachObjectDoc.Kind kind() {
            return OfClauseDoc.Kind.EACH_OBJECT;
        }
        private List createInstances(PerObject eo) {
            Pcd pc = eo.getPcd();
            if (pc == null) {
                return Collections.EMPTY_LIST;
            }
            return Collections.EMPTY_LIST;
        }
    }

    /** TODO */
    private static class Factory {

        private final static OfClauseDoc EACH_CFLOW = new OfClauseDoc(){
                public OfClauseDoc.Kind kind() {
                    return OfClauseDoc.Kind.EACH_CFLOW;
                }
            };

        private final static OfClauseDoc EACH_JVM = new OfClauseDoc() {
                public OfClauseDoc.Kind kind() {
                    return OfClauseDoc.Kind.EACH_JVM;
                }
            };

        public final OfClauseDoc getInstance(PerClause clause) {
            if (clause instanceof PerCFlow) {
                return EACH_CFLOW;
            }
            if (clause instanceof PerSingleton) {
                return EACH_JVM;
            }
            if (clause instanceof PerObject) {
                return new OfEachObjectDocImpl((PerObject)clause);
            }
            return null; //??? error
        }
    }
}
