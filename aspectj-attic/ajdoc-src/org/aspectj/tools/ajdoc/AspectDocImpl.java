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

import org.aspectj.ajdoc.AdviceDoc;
import org.aspectj.ajdoc.AspectDoc;
import org.aspectj.ajdoc.IntroductionDoc;
import org.aspectj.ajdoc.OfClauseDoc;
import org.aspectj.compiler.base.ast.Decs;
import org.aspectj.compiler.crosscuts.ast.AdviceDec;
import org.aspectj.compiler.crosscuts.ast.AspectDec;
import org.aspectj.compiler.crosscuts.ast.IntroducedSuperDec;

import com.sun.javadoc.ClassDoc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

public class AspectDocImpl extends ClassDocImpl implements AspectDoc {

    /** Array of AdviceDoc created from this AspectDecs AdviceDecs. */
    private final Collection advice;

    /** Array of IntroductionDec created from this AspectDecs introductions. */
    private final Collection introductions;

    /** The of clause this aspect has -- may be null. */
    private final OfClauseDoc ofClause;

    /** The aspects that dominate this aspect. */
    private final Collection dominators = new ArrayList();

    /** The aspects that are dominated by this aspect. */
    private final Collection dominatees = new ArrayList();

    /**
     * Constructs an AspectDoc with the containing ClassDoc
     * and underlying AspectDec.
     *
     * @param containingClass contained ClassDoc.
     * @param aspectDec       underlying AspectDec.
     */
    public AspectDocImpl(ClassDoc containingClass, AspectDec aspectDec) {
        super(containingClass, aspectDec);
        introductions = createIntroductions();
        advice = createAdvice();
        ofClause = createOfClause();
    }

    /**
     * Returns an instance of AdviceDocImpl corresponding to
     * the AdviceDec passed in.
     *
     * @param dec the AdviceDec mapping to the desired
     *            AdviceDocImpl.
     * @return    an instance of AdviceDocImpl corresponding to
     *            the AdviceDec passed in.
     */
    public AdviceDocImpl docForDec(AdviceDec dec) {
        for (Iterator i = advice.iterator(); i.hasNext();) {
            AdviceDocImpl ad = (AdviceDocImpl)i.next();
            if (ad.dec() == dec) return ad;
        }
        return null;
    }

    /**
     * Returns an instance of IntroducedSuperDocImpl corresponding to
     * the IntroducedSuperDec passed in.
     *
     * @param  dec the IntroducedSuperDec mapping to the
     *             desired IntroducedSuperDocImpl
     * @return     an instance of IntroducedSuperDocImpl
     *             corresponding to the IntroducedSuperDec
     *             passed in.
     */
    public IntroducedSuperDocImpl introDocForDec(IntroducedSuperDec dec) {
        for (Iterator i = introductions.iterator(); i.hasNext();) {
            ProgramElementDocImpl id = (ProgramElementDocImpl)i.next();
            if (id.dec() == dec) return (IntroducedSuperDocImpl)id;
        }
        return null;
    }

    /**
     * Returns the underlying AspectDec.
     *
     * @return the underlying AspectDec.
     */
    protected AspectDec aspectDec() {
        return (AspectDec)typeDec();
    }

    /**
     * Returns the visible advice in this aspect.
     *
     * @return an array of AdviceDoc representing the
     *         visible advice in this aspect.
     */
    public AdviceDoc[] advice() {
        return (AdviceDocImpl[])advice.toArray
            (new AdviceDocImpl[advice.size()]);
    }

    /**
     * Returns the aspects that are dominated by this aspect.
     *
     * @return an array of AspectDec representing the aspects
     *         that are dominated by this aspect.
     */
    public AspectDoc[] dominatees() {
        return (AspectDoc[])dominatees.toArray
            (new AspectDoc[dominatees.size()]);
    }
   
    /**
     * Return the aspects that dominate this aspect.
     *
     * @return an array of AspectDoc representing the aspects
     *         that dominate this aspect.
     */
    public AspectDoc[] dominators() {
        return (AspectDoc[])dominators.toArray
            (new AspectDoc[dominators.size()]);
    }

    /**
     * TODO
     * Returns the visible introductions of this aspect.
     *
     * @return an array of IntroductionDoc representing the
     *         visible introductions in this aspect.
     */
    public IntroductionDoc[] introductions() {
        return (IntroductionDocImpl[])introductions.toArray
            (new IntroductionDocImpl[introductions.size()]);
    }

    /**
     * Returns the <i>of clause</i> of this aspect.
     *
     * @return the <i>of clause</i> of this aspect.
     */
    public OfClauseDoc ofClause() {
        return ofClause;
    }

    /**
     * Returns <code>true</code>.
     *
     * @return <code>true</code>.
     */
    public boolean isAspect() {
        return true;
    }

    /**
     * Returns <code>true</code> if this aspects dominates
     * the passed in aspect.
     *
     * @param other an AspectDoc that represents another
     *              aspect in this world.
     * @return      <code>true</code> if this aspects dominates
     *              the passed in aspect.
     */
    public boolean dominates(AspectDoc other) {
        if (!(other instanceof AspectDocImpl)) {
            return false;
        }
        return aspectDec().dominates(((AspectDocImpl)other).aspectDec());
    }

    /**
     * Adds a dominates relation from <code>dominator</code> to
     * <code>this</code>.  For example, somewhere in the code
     * the line
     * <code>
     *       aspect dominator dominates this { ... }
     * </code>
     * exists.
     *
     * @param dominator an instance of AspectDocImpl that
     *                  dominates this.
     */
    public void addDominator(AspectDoc dominator) {
        dominators.add(dominator);
    }

    /**
     * Adds a dominates relation from <code>dominator</code> to
     * <code>this</code>.  For example, somewhere in the code
     * the line
     * <code>
     *       aspect this dominates dominatee { ... }
     * </code>
     * exists.
     *
     * @param dominatee an instance of AspectDocImpl that
     *                  is dominated by this.
     */
    public void addDominatee(AspectDoc dominatee) {
        dominatees.add(dominatee);
    }

    /**
     * Returns a Collection of IntroductionDocImpl representing
     * the introductions declared in this aspect.
     *
     * @return a Collection of IntroductionDocImpl representing
     *         the introductions declared in this aspect.
     */
    private Collection createIntroductions() {
        Decs decs = aspectDec().getBody();
        if (decs.size() < 1) return Collections.EMPTY_LIST;
        Collection list = new HashSet();
        for (Iterator i = decs.getList().iterator(); i.hasNext();) {
            Object o = IntroductionDocImpl.getInstance(this, i.next());
            if (o != null) list.add(o);
        }
        return list;
    }

    /**
     * Returns a Collection of AdviceDocImpl representing
     * the advice declared in this aspect.
     *
     * @return a Collection of AdviceDocImpl representing
     *         the advice declared in this aspect.
     */
    private Collection createAdvice() {
        // pluck the AdviceDec from the list of JpPlannerMakers
        List decs = aspectDec().getJpPlannerMakers();
        if (null != decs) {
            final int QUIT = 2;
            for (int tries = 0; tries < QUIT; tries++) {
                try {
                    for (Iterator i = decs.iterator(); i.hasNext();) {
                        Object o = i.next();
                        if (!(o instanceof AdviceDec)) {
                            i.remove(); 
                        }
                    }
                    tries = QUIT;
                } catch (UnsupportedOperationException o) {
                    if (0 != tries) {
                        tries = QUIT;
                    } else {
                        ArrayList list = new ArrayList();
                        list.addAll(decs);
                        decs = list;
                    }
                }
            }
        }
        
        if ((null == decs) || (decs.size() < 1)) {
            return Collections.EMPTY_LIST;
        }

        List list = new ArrayList();
        for (Iterator i = decs.iterator(); i.hasNext();) {
            list.add(new AdviceDocImpl(this, (AdviceDec)i.next()));
        }
        return list;
    }

    /**
     * Returns an instance of OfClauseDoc representing
     * the of clause declared by this aspect.
     *
     * @return an instance of OfClauseDoc representing
     *         the of clause declared by this aspect.
     */
    private OfClauseDoc createOfClause() {
        return OfClauseDocImpl.getInstance(aspectDec().getPerClause());
    }
}
