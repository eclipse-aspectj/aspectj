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

import org.aspectj.ajdoc.Doc;

import com.sun.javadoc.SeeTag;
import com.sun.javadoc.Tag;

import java.util.Locale;

public abstract class DocImpl
    implements org.aspectj.ajdoc.Doc {

    /** Keep track of whether this is included or not. */
    private boolean isIncluded = true;

    /** The comment for this Doc. */
    private Comment comment;

    /** The error printer for this Doc. */
    private ErrPrinter err;
    
    /** Keep track of whether this is included or not. */
    private boolean isInterface = true;

    /** The locale of the object -- default to <code>Locale.US</code>. */
    private Locale locale = Locale.US; //TODO

    /**
     * Returns the locale.
     *
     * @return the locale.
     */
    public Locale locale() { //XXX
        return locale;
    }

    
    public void setErr(ErrPrinter err) { this.err = err; }
    public ErrPrinter err() { return ErrPrinter.instance; }

    public void setComment(Comment comment) { this.comment = comment; }
    public Comment getComment() { return comment; }

    /**
     * Delegates to {@link Util#compareTo(Object)} to compare
     * with another Object.
     *
     * @return a negative integer, zero, or a positive integer
     *         as this object is less than, equal to, or greater
     *         than the specified object based on name.
     * @see    java.lang.Comparable.compareTo(Object)
     */
    public int compareTo(Object other) {
        return other instanceof Doc
            ?  Util.compareTo(this, (Doc)other)
            :  -1;
    }

    /**
     * Returns the Tags that comprise the first
     * sentence of the comment.
     *
     * @return an array of Tag representing the first
     *         sentence of the comment.
     */
    public Tag[] firstSentenceTags() {
        return getComment() != null
            ?  getComment().firstSentenceTags()
            :  new Tag[0];
    }

    /**
     * Returns the full unprocessed text of the comment.
     *
     * @return the full unprocessed text of the comment.
     */
    public String getRawCommentText() {
        return getComment() != null
            ?  getComment().rawCommentText()
            :  "";
    }

    /**
     * Sets the full unprocessed text of the comment.
     *
     * @param rawCommentText the new full unprocessed text of the comment..
     */
    public void setRawCommentText(String rawCommentText) {
        if (getComment() != null) {
            getComment().setRawCommentText(rawCommentText);
        }
    }

    /**
     * Returns the comment as an array of Tag.
     *
     * @return an array of Tag representing the comment.
     */
    public Tag[] inlineTags() {
        return getComment() != null
            ?  getComment().inlineTags()
            :  new Tag[0];
    }
    
    /**
     * Returns the see tags of the comment.
     *
     * @return an array of SeeTag representing the
     *         see tags of the comment.
     */
    public SeeTag[] seeTags() {
        return getComment() != null
            ?  getComment().seeTags()
            :  new SeeTag[0];
    }

    /**
     * Returns all tags of the comment.
     *
     * @return an array of Tag representing all
     *         tags of the comment.
     */
    public Tag[] tags() {
        return getComment() != null
            ?  getComment().tags()
            :  new Tag[0];
    }

    /**
     * Returns all tags of the comment whose name equals
     * <code>tagname</code>.
     *
     * @return an array of Tag representing all tags of the
     *         comment whose name equals <code>tagname</code>.
     */
    public Tag[] tags(String tagname) {
        return getComment() != null
            ?  getComment().tags(tagname)
            :  new Tag[0];
    }

    /**
     * Returns the commext text for non-null comments,
     * otherwise the empty String.
     *
     * @return non-null comment text.
     */
    public String commentText() {
        return getComment() != null
            ?  getComment().commentText()
            :  "";
    }

    /**
     * Sets <code>isIncluded</code>.
     *
     * @param isIncluded the new value of <code>isIncluded</code>.
     */
    public void setIncluded(boolean isIncluded) {
        this.isIncluded = isIncluded;
    }
    
    /**
     * Returns <code>false</code> by default.
     *
     * @return <code>false</code> by default.
     */
    public boolean isClass() {
        return false;
    }
    
    /**
     * Returns <code>false</code> by default.
     *
     * @return <code>false</code> by default.
     */
    public boolean isConstructor() {
        return false;
    }

    /**
     * Returns <code>false</code> by default.
     *
     * @return <code>false</code> by default.
     */
    public boolean isError() {
        return false;
    }
    
    /**
     * Returns <code>false</code> by default.
     *
     * @return <code>false</code> by default.
     */
    public boolean isException() {
        return false;
    }
    /**
     * Returns <code>false</code> by default.
     *
     * @return <codealse</code> by default./
     */
    public boolean isField() {
        return false;
    }
    /**
     * Returns <code>isIncluded</code> by default.
     *
     * @return <codesIncluded</code> by default./
     */
    public boolean isIncluded() {
        return isIncluded;
    }
    
    /**
     * Returns <code>false</code> by default.
     *
     * @return <codealse</code> by default./
     */
    public boolean isInterface() {
        return false;
    }
    
    /**
     * Returns <code>false</code> by default.
     *
     * @return <codealse</code> by default./
     */
    public boolean isMethod() {
        return false;
    }
    
    /**
     * Returns <code>false</code> by default.
     *
     * @return <codealse</code> by default./
     */
    public boolean isOrdinaryClass() {
        return false;
    }
    
    /**
     * Returns <code>false</code> by default.
     *
     * @return <codealse</code> by default./
     */
    public boolean isPointcut() {
        return false;
    }
    
    /**
     * Returns <code>false</code> by default.
     *
     * @return <codealse</code> by default./
     */
    public boolean isAdvice() {
        return false;
    }
}
