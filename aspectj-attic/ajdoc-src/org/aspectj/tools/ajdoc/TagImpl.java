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
import com.sun.javadoc.Tag;

import java.util.Locale;

public class TagImpl implements Tag {

    private Locale locale;
    private ErrPrinter err;
    private String name;
    private String text;
    private Doc doc;

    /**
     * Constructs the new tag with given parameters.
     *
     * @param doc    the new value for <code>doc</code>.
     * @param name   the new value for <code>name</code>.
     * @param text   the new value for <code>text</code>.    
     * @param locale the new value for <code>locale</code>.
     * @param err    the new value for <code>err</code>.
     */
    public TagImpl(Doc doc,
                   String name,
                   String text,
                   Locale locale,
                   ErrPrinter err) {
        this.doc = doc;
        this.name = name;
        this.text = text;
        this.locale = locale;
        this.err = err;
    }

    /**
     * Returns the Doc associated with this tag.
     *
     * @return the Doc associated with this tag.
     */
    protected final Doc doc() {
        return doc;
    }

    /**
     * Returns the ErrPrinter associated with this tag.
     *
     * @return the ErrPrinter associated with this tag.
     */
    protected final ErrPrinter err() {
        return err == null ? ErrPrinter.instance : err;
    }

    /**
     * Delegates to {@link Util#start(char)}.
     *
     * @see Util#start(char)
     */
    protected final static boolean start(char c) { return Util.start(c); }

    /**
     * Delegates to {@link Util#ident(char)}.
     *
     * @see Util#ident(char)
     */
    protected final static boolean ident(char c) { return Util.ident(c); }

    /**
     * Delegates to {@link Util#space(char)}.
     *
     * @see Util#space(char)
     */
    protected final static boolean space(char c) { return Util.space(c); }

    /**
     * Delegates to {@link Util#split(char)}.
     *
     * @see Util#split(char)
     */
    protected final static String[] split(String s) { return Util.split(s); }

    /**
     * Returns the name.
     *
     * @return the name.
     */
    public String name() {
        return name;
    }

    /**
     * Returns the kind followed by the String text.
     *
     * @return kind followed by the String text.
     */
    public String toString() {
        return kind() + " " + text;
    }

    /**
     * Returns the kind of tag.
     *
     * @return kind of the tag.
     */
    public String kind() {
        return name;
    }

    /**
     * Returns the String text of this tag.
     *
     * @return String text of this tag.
     */
    public String text() {
        return text;
    }

    /**
     * Sets the String text.
     *
     * @param text the new value for <code>text</code>.
     */
    protected void setText(String text) {
        this.text = text;
    }

    /**
     * Returns the locale.
     *
     * @return the locale.
     */
    public Locale locale() {
        return locale;
    }

    /**
     * Returns the inline tags in this tag.
     *
     * @return an array of Tag representing the inline
     *         tags in this tag.
     * @see    Util#inlineTags
     */
    public Tag[] inlineTags() {
        return Util.inlineTags(doc(), text(), locale(), err()); //doc());
    }

    /**
     * Returns the first sentence tags in this tag.
     *
     * @return an array of Tag representing the first sentence
     *         tags in this tag.
     * @see    Util#firstSentenceTags
     */
    public Tag[] firstSentenceTags() {
        return Util.firstSentenceTags(doc(), text(), locale(), err());
    }


}
