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
 * A class or aspect member in the aspectj-world, the difference
 * between this and a javac-world member is that if this member
 * was introduced by an aspect, it has a reference to that
 * introduction.
 *
 * @author Jeff Palm
 */
public interface MemberDoc extends com.sun.javadoc.MemberDoc,
                                   ProgramElementDoc {

    /**
     * Returns the introduction that placed this member on this class
     * if is exists -- this <b>can</b> be <code>null</code>.
     *
     * @return the introduction that placed this member on this class
     *         if is exists -- this <b>can</b> be <code>null</code>.
     */
    public IntroducedDoc introduced();
}
