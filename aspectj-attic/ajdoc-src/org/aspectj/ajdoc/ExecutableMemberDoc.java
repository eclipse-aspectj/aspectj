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
 * Represents and executable member in the aspectj-world.
 * The only difference between this and one in the javac-world
 * is that these members can have advice on them
 *
 * @author Jeff Palm
 */
public interface ExecutableMemberDoc extends com.sun.javadoc.ExecutableMemberDoc,
                                             MemberDoc {

    /**
     * Returns the advice placed on this member.
     *
     * @return an array of AdviceDoc representing the
     *         advice placed on this member.
     */
    public AdviceDoc[] advice();
}
