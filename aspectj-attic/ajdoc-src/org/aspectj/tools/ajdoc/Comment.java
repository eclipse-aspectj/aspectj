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
import com.sun.javadoc.SeeTag;
import com.sun.javadoc.SerialFieldTag;
import com.sun.javadoc.Tag;
import com.sun.javadoc.ThrowsTag;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class Comment {
    
    /** The parsed comment text. */
    private String commentText;

    /** The raw comment text. */
    private String rawCommentText;

    /** The list of tags. */
    private List tags;

    /** The Doc to which the Comment belongs. */
    private Doc doc;

    /** The Locale in which this comment resides. */
    private Locale loc;

    /** The ErrPrinter used by this Comment to output messages. */
    private ErrPrinter err;

    public Comment(Doc doc, String formalComment) {
        this(doc, formalComment, ErrPrinter.instance);
    }

    public Comment(Doc doc, String formalComment, ErrPrinter err) {
        this(doc, formalComment, err, Locale.US);
    }

    public Comment(Doc doc, String formalComment, ErrPrinter err, Locale loc) {
        this.rawCommentText = Util.rawCommentText(formalComment);
        this.commentText = Util.commentText(rawCommentText);
        this.doc = doc;
        this.err = err;
        this.loc = loc;
    }

    /**
     * Returns the parsed comment text.
     *
     * @return the parsed comment text.
     */
    public String commentText() {
        return commentText;
    }

    /**
     * Returns the full unprocessed text of the comment.
     *
     * @return the full unprocessed text of the comment.
     */
    public String getRawCommentText() {
        return rawCommentText;
    }

    /**
     * Sets the comment text.
     *
     * @param commentText the new comment text.
     */
    public void setCommentText(String commentText) {
        this.commentText = commentText;
    }
    
    /**
     * Returns the raw comment text.
     *
     * @return the raw comment text.
     */
    public String rawCommentText() {
        return rawCommentText;
    }

    /**
     * Sets the raw comment text.
     *
     * @param rawCommentText the new raw comment text.
     */
    public void setRawCommentText(String rawCommentText) {
        this.rawCommentText = rawCommentText;
    }

    /**
     * Returns all this comment's tags.
     *
     * @return a List of tags whose elements are Comment instances
     */
    public List getTags() {
        if (tags == null) {
            tags = findTags();
        }
        return tags;
    }
    
    /**
     * Sets the Doc for this comment.
     *
     * @param doc the new Doc.
     */
    public void setDoc(Doc doc) {
        this.doc = doc;
    }

    /**
     * Returns the Doc for this comment.
     *
     * @return the Doc for this comment.
     */
    public Doc doc() {
        return doc;
    }

    /**
     * Sets the locale for this comment.
     *
     * @param loc the new locale for this comment.
     */
    public void setLocale(Locale loc) {
        this.loc = loc;
    }

    /**
     * Returns the Locale for this comment.
     *
     * @return the Locale for this comment.
     */
    public Locale locale() {
        return loc;
    }
    
    /**
     * Sets the ErrPrinter for this comment.
     *
     * @param err the new ErrPrinter for this comment.
     */
    public void setErr(ErrPrinter err) {
        this.err = err;
    }

    /**
     * Returns the ErrPrinter for this comment.
     *
     * @return the ErrPrinter for this comment.
     */
    public ErrPrinter err() {
        return err;
    }

    /**
     * Initializes the Doc, Locale, and ErrPrinter.
     *
     * @param doc the new Doc.
     * @param loc the new Locale.
     * @param err the new ErrPrinter.
     */
    public void init(Doc doc, Locale loc, ErrPrinter err) {
        setDoc(doc);
        setLocale(loc);
        setErr(err);
    }
   
    /**
     * Returns the comment as an array of Tag.
     *
     * @return an array of Tag representing the comment.
     */
    public Tag[] inlineTags() {
        return Util.inlineTags(doc(),
                               commentText(),
                               locale(),
                               err());
    }

    /**
     * Returns all tags of the comment whose name equals
     * <code>tagname</code>.
     *
     * @return an array of Tag representing all tags of the
     *         comment whose name equals <code>tagname</code>.
     */
    public Tag[] tags(String type) {
        type = type.startsWith("@") ? type : "@"+type;
        List result = new ArrayList();
        Tag tag;
        for (Iterator i = getTags().iterator(); i.hasNext();) {
            if ((tag = (Tag)i.next()).kind().equals(type)) {
                result.add(tag);
            }
        }
        return (Tag[])result.toArray(new Tag[result.size()]);
    }

    /**
     * Returns the param tags describing parameters taken
     * by this code.
     *
     * @return an array of ParamTag representing the
     *         parameters taken by this code.
     */
    public ParamTag[] paramTags() {
        List result = new ArrayList();
        Tag tag;
        for (Iterator i = getTags().iterator(); i.hasNext();) {
            if ((tag = (Tag)i.next()) instanceof ParamTag) {
                result.add((ParamTag)tag);
            }
        }
        return (ParamTag[])result.toArray(new ParamTag[result.size()]);
    }
    
    /**
     * Returns the see tags of the comment.
     *
     * @return an array of SeeTag representing the
     *         see tags of the comment.
     */
    public SeeTag[] seeTags() {
        List result = new ArrayList();
        Tag tag;
        for (Iterator i = getTags().iterator(); i.hasNext();) {
            if ((tag = (Tag)i.next()) instanceof SeeTag) {
                result.add((SeeTag)tag);
            }
        }
        return (SeeTag[])result.toArray(new SeeTag[result.size()]);
    }

    /**
     * Returns the serial field tags for this field.
     *
     * @return an array of SerialFieldTag representing the
     *         serial field tags for this field.
     */
    public SerialFieldTag[] serialFieldTags() {
        List result = new ArrayList();
        Tag tag;
        for (Iterator i = getTags().iterator(); i.hasNext();) {
            if ((tag = (Tag)i.next()) instanceof SerialFieldTag) {
                result.add((SerialFieldTag)tag);
            }
        }
        return (SerialFieldTag[])result.toArray
            (new SerialFieldTag[result.size()]);
    }

    /**
     * Returns the throw tags describing exceptions thrown
     * declared by this code.
     *
     * @return an array of ThrowsTag representing the exception
     *         this code declares to throw.
     */
    public ThrowsTag[] throwsTags() {
        List result = new ArrayList();
        Tag tag;
        for (Iterator i = getTags().iterator(); i.hasNext();) {
            if ((tag = (Tag)i.next()) instanceof ThrowsTag) {
                result.add((ThrowsTag)tag);
            }
        }
        return (ThrowsTag[])result.toArray
            (new ThrowsTag[result.size()]);
    }

    /**
     * Returns all tags of the comment.
     *
     * @return an array of Tag representing all
     *         tags of the comment.
     */
    public Tag[] tags() {
        return (Tag[])getTags().toArray
            (new Tag[getTags().size()]);
    }

    /**
     * Returns the Tags that comprise the first
     * sentence of the comment.
     *
     * @return an array of Tag representing the first
     *         sentence of the comment.
     */
    public Tag[] firstSentenceTags() {
        return Util.firstSentenceTags(doc(),
                                      commentText(),
                                      locale(),
                                      err());
    }

    /**
     * Used to lazily initialize the tags of this comment.
     *
     * @return a List of tags whose elements of Tag instances
     *         and each represent a tag in this comment.
     */
    private List findTags() {
        return Util.findTags(doc(),
                             rawCommentText(),
                             locale(),
                             err());
    }
}
