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

import com.sun.javadoc.Doc;
import com.sun.javadoc.ParamTag;

import java.util.Locale;

/**
 * The implementation of a param tag.
 *
 * @author Jeff Palm
 */
public class ParamTagImpl extends TagImpl implements ParamTag {

    private String parameterComment;
    private String parameterName;

    /**
     * Constructs the new tag with given parameters.
     *
     * @param doc    the new value for <code>doc</code>.
     * @param name   the new value for <code>name</code>.
     * @param text   the new value for <code>text</code>.    
     * @param locale the new value for <code>locale</code>.
     * @param err    the new value for <code>err</code>.
     */
    public ParamTagImpl(Doc doc,
                        String name,
                        String text,
                        Locale loc,
                        ErrPrinter err) {
        super(doc, name, null, loc, err);
        String[] split = split(text);
        parameterName = split[0];
        parameterComment = split[1];
        setText(parameterComment);
    }

    /**
     * Returns the parameter comment.
     *
     * @return the parameter comment.
     */
    public String parameterComment() {
        return parameterComment;
    }

    /**
     * Returns the name of the parameter.
     *
     * @return the name of the parameter.
     */
    public String parameterName() {
        return parameterName;
    }

    /**
     * Returns <code>param</code>.
     *
     * @return <code>param</code>.
     */
    public String kind() {
        return "@param";
    }
}
