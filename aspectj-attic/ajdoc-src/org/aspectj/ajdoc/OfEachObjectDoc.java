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
package org.aspectj.ajdoc;

/**
 * Representat an 'of eachobject(..)' clause, thought currently
 * out of the language, it could make a comeback.
 *
 * @author Jeff Palm
 */
public interface OfEachObjectDoc extends OfClauseDoc {

    /**
     * Returns the classes for which an instance of
     * this aspect will be made for each.
     *
     * @return an array of com.sun.javadoc.ClassDoc representing
     *         the classes for which an instance of
     *         this aspect will be made for each.
     */
    //public com.sun.javadoc.ClassDoc[] instances();
    public ClassDoc[] instances();
}
