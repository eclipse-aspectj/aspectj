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
 * Represents an aspectj aspect and extends
 * <code>ClassDoc</code> to provide the extra aspectj-only
 * information not present in that interface.
 *
 * @author Jeff Palm
 */
public interface AspectDoc extends ClassDoc { //, com.sun.javadoc.ClassDoc {

    /**
     * Return advice in aspect.
     *
     * @return an array of AdviceDoc for representing the
     *         visible advice in this aspect.
     */
    public AdviceDoc[] advice();

    /**
     * Return aspects that are dominated by this aspect.
     *
     * @return an array of AspectDoc for representing the
     *         aspects that are dominated by this aspect.
     */
    public AspectDoc[] dominatees();

    /**
     * Return aspects that dominate this aspect.
     *
     * @return an array of AspectDoc for representing the
     *         aspects that dominate this aspect.
     */
    public AspectDoc[] dominators();

    /**
     * Return the introductions made by this aspect on other types.
     *
     * @return an array of IntroductionDoc for representing the
     *         introductions made on other types.
     */
    public IntroductionDoc[] introductions();

    /**
     * Return the of clauses that describe this aspect.
     *
     * @return an array of OfClauseDoc for representing the
     *         of clauses that describe this aspect.
     */
    public OfClauseDoc ofClause();

    /**
     * Returns <code>true</code> if this aspects dominates
     * the passed in aspect.
     *
     * @param other an AspectDoc that represents another
     *              aspect in this world.
     * @return      <code>true</code> if this aspects dominates
     *              the passed in aspect.
     */
    public boolean dominates(AspectDoc other);
}
