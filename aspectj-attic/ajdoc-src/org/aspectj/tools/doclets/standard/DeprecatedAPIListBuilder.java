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
package org.aspectj.tools.doclets.standard;

import org.aspectj.ajdoc.AspectDoc;
import org.aspectj.ajdoc.IntroducedDoc;
import org.aspectj.ajdoc.IntroductionDoc;
import org.aspectj.tools.ajdoc.Util;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.MemberDoc;
import com.sun.javadoc.RootDoc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DeprecatedAPIListBuilder
    extends com.sun.tools.doclets.standard.DeprecatedAPIListBuilder
{
    
    private List deprecatedadvice = new ArrayList();
    private List deprecatedpointcuts = new ArrayList();
    private List deprecatedfieldintroductions = new ArrayList();
    private List deprecatedmethodintroductions = new ArrayList();
    private List deprecatedconstructorintroductions = new ArrayList();
    private List deprecatedsuperintroductions = new ArrayList();
    
    public DeprecatedAPIListBuilder(RootDoc root) {
        super(root);
        buildDeprecatedAPIInfo(root);
    }

    protected void buildDeprecatedAPIInfo(RootDoc root) {
        ClassDoc[] cs = root.classes();
        for (int i = 0; i < cs.length; i++) {
            org.aspectj.ajdoc.ClassDoc c = (org.aspectj.ajdoc.ClassDoc)cs[i];
            _composeDeprecatedList(deprecatedpointcuts, c.pointcuts());
            if (c instanceof AspectDoc) {
                AspectDoc ad = (AspectDoc)c;
                _composeDeprecatedList(deprecatedadvice, ad.advice());
                IntroductionDoc[] intros = ad.introductions();
                for (int j = 0; j < intros.length; j++) {
                    if (intros[j] instanceof IntroducedDoc) {
                        MemberDoc md = ((IntroducedDoc)intros[j]).member();
                        if (md == null) continue;
                        if (md.isField()) {
                            _composeDeprecatedList(deprecatedfieldintroductions,
                                                   intros[j]);
                        } else if (md.isMethod()) {
                            _composeDeprecatedList(deprecatedmethodintroductions,
                                                   intros[j]);
                        } else {
                            _composeDeprecatedList(deprecatedconstructorintroductions,
                                                   intros[j]);
                        }
                    } else {
                        _composeDeprecatedList(deprecatedsuperintroductions,
                                               intros[j]);
                    }
                }
            }
        }
        Collections.sort(deprecatedadvice);
        Collections.sort(deprecatedpointcuts);
        Collections.sort(deprecatedfieldintroductions);
        Collections.sort(deprecatedmethodintroductions);
        Collections.sort(deprecatedconstructorintroductions);
        Collections.sort(deprecatedsuperintroductions);
    }

    protected void _composeDeprecatedList(List list, MemberDoc member) {
        _composeDeprecatedList(list, new MemberDoc[]{member});
    }
    protected void _composeDeprecatedList(List list, MemberDoc[] members) { 
        Util.invoke(com.sun.tools.doclets.standard.DeprecatedAPIListBuilder.class,
                    this, "composeDeprecatedList",
                    new Class[]{java.util.List.class,
                                com.sun.javadoc.MemberDoc[].class},
                    new Object[]{list, members});
    }

    public List getDeprecatedAdivce() {
        return deprecatedadvice;
    }
    public List getDeprecatedPointcuts() {
        return deprecatedpointcuts;
    }
    public List getDeprecatedFieldIntroductions() {
        return deprecatedfieldintroductions;
    }
    public List getDeprecatedMethodIntroductions() {
        return deprecatedmethodintroductions;
    }
    public List getDeprecatedConstructorIntroductions() {
        return deprecatedconstructorintroductions;
    }
    public List getDeprecatedSuperIntroductions() {
        return deprecatedsuperintroductions;
    }
}
