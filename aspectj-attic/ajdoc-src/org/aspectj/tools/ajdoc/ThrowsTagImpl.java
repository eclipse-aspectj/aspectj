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

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.Doc;
import com.sun.javadoc.ThrowsTag;

import java.util.Locale;

/**
 * Represents a throws tag in the aspectj-world.
 *
 * @author Jeff Palm
 */
public class ThrowsTagImpl extends TagImpl implements ThrowsTag {

    private ClassDoc exception;
    private String exceptionComment;
    private String exceptionName;

    /**
     * Constructs the new tag with given parameters and
     * splits the text.
     *
     * @param doc    the new value for <code>doc</code>.
     * @param name   the new value for <code>name</code>.
     * @param text   the new value for <code>text</code>.    
     * @param locale the new value for <code>locale</code>.
     * @param err    the new value for <code>err</code>.
     */
    public ThrowsTagImpl(Doc doc,
                         String name,
                         String text,
                         Locale loc,
                         ErrPrinter err) {
        super(doc, name, text, loc, err);
        String[] split = split(text);
        exceptionName = split[0];
        exceptionComment = split[1];
        exception = findException();
    }

    /**
     * Returns the exception thrown.
     *
     * @return a ClassDoc that represents the thrown exception.
     */
    public ClassDoc exception() {
        return exception;
    }

    /**
     * Returns the comment text.
     *
     * @return a String of the comment text.
     */
    public String exceptionComment() {
        return exceptionComment;
    }

    /**
     * Returns the name of the type of exception thrown.
     *
     * @return a String name of the type of exception thrown.
     */
    public String exceptionName() {
        return exceptionName;
    }

    /**
     * Returns <code>throws</code>.
     *
     * @return <code>throws</code>.
     */
    public String kind() {
        return "@throws";
    }

    //XXX
    //TODO: implement         FUUUUUUCCCCCCCCCKKKKKKKKKKKK
    private ClassDoc findException() {
        return null;
    }
}
