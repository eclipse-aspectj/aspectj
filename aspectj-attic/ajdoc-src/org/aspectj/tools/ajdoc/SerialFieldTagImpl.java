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
import com.sun.javadoc.SerialFieldTag;

import java.util.Locale;

/**
 * Represents a serial field tag in the aspectj-world.
 *
 * @author Jeff Palm
 */
public class SerialFieldTagImpl
    extends TagImpl implements SerialFieldTag,
                               Comparable {

    private String description;
    private String fieldName;
    private String fieldType;
    private ClassDoc fieldTypeDoc;

    /**
     * Constructs the new tag with given parameters and
     * then tries to resolve the names of the text.
     *
     * @param doc    the new value for <code>doc</code>.
     * @param name   the new value for <code>name</code>.
     * @param text   the new value for <code>text</code>.    
     * @param locale the new value for <code>locale</code>.
     * @param err    the new value for <code>err</code>.
     */
    public SerialFieldTagImpl(Doc doc,
                              String name,
                              String text,
                              Locale loc,
                              ErrPrinter err) {
        super(doc, name, text, loc, err);
        resolveNames(text);
    }

    /**
     * Returns the description.
     *
     * @return the description.
     */
    public String description() {
        return description;
    }

    /**
     * Returns the field name.
     *
     * @return the field name.
     */
    public String fieldName() {
        return fieldName;
    }

    /**
     * Returns the field type.
     *
     * @return the field type.
     */
    public String fieldType() {
        return fieldType;
    }

    /**
     * Returns the class of the field type.
     *
     * @return a ClassDoc with type name fieldType.
     */
    public ClassDoc fieldTypeDoc() {
        return fieldTypeDoc;
    }

    //XXX
    //TODO: implement
    public int compareTo(Object other) {
        return -1;
    }
    
    /**
     * Returns <code>serialField</code>.
     *
     * @return <code>serialField</code>.
     */
    public String kind() {
        return "@serialField";
    }
    
    
    private void resolveNames(String str) {
        //
        // @serialField  field-name  field-type  field-description
        //
        if (str == null || (str = str.trim()).length() < 1) return;
        final int N = str.length();
        
        int i = 0;
        int start;

        // Find the first char in the field-name
        start = i;
        if (i < N && !start(str.charAt(i++))) {
            err().error("serialField_tag_invalid_field_name_start",
                        ""+str.charAt(i));
            return;
        }

        // Find the rest of the field-name
        while (i < N && !space(str.charAt(i))) {
            if (!ident(str.charAt(i))) {
                err().error("serialField_tag_invalid_field_name_part",
                            ""+str.charAt(i));
                return;
            }
            i++;
        }

        // Found the field-name
        fieldName = str.substring(start, i).trim();

        // Find the first char in the field-type
        start = i;
        if (i < N && !start(str.charAt(i++))) {
            err().error("serialField_tag_invalid_type_name_start",
                        ""+str.charAt(i));
            return;
        }

        // Find the rest of the field-name
        while (i < N && !space(str.charAt(i))) {
            if (!(str.charAt(i) == '[' ||
                  str.charAt(i) == ']' ||
                  ident(str.charAt(i)))) {
                err().error("serialField_tag_invalid_type_name_part",
                            ""+str.charAt(i));
                return;
            }
        }

        // Found the field-type
        fieldType = str.substring(start, i).trim();

        // The rest is the field-description
        if (i < N) {
            description = str.substring(i).trim();
        }
    }    
}
