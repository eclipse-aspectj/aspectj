package ca.ubc.cs.spl.aspectPatterns.patternLibrary;

/* -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 *
 * This file is part of the design patterns project at UBC
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
 * The Original Code is ca.ubc.cs.spl.aspectPatterns.
 * 
 * For more details and the latest version of this code, please see:
 * http://www.cs.ubc.ca/labs/spl/projects/aodps.html
 *
 * Contributor(s):   
 */

import java.util.WeakHashMap;
import java.util.List;
import java.util.LinkedList;
import java.util.Iterator;

/**
 * Defines the general behavior of the Observer design pattern.
 *
 * Each concrete sub-aspect of ObserverProtocol defines one kind of observing
 * relationship.  Within that kind of relationship, there can be any number
 * of <i>Subject</i>s, each with any number of <i>Observer</i>s.
 *
 * The sub-aspect defines three things: <ol>
 *
 *   <li> what types can be <i>Subject</i>s or observers <br>
 *        this is done using +implements
 *
 *   <li> what operations on the <i>Subject</i> require updating the observers <br>
 *        this is done by concretizing the changes(Subject) pointcut
 *
 *   <li> how to update the observers <br>
 *        this is done by defining a method on
 *        updateObserver(Subject, Observer) 
 * </ol>
 *
 * Note that in this implementation, the work of updating is a method
 * on the sub-aspect, not a method introduced on the observer.  This
 * allows one class of object to be the observer in different kinds of
 * observing relationships, each of which has a different updating
 * behavior.  For observers that just have a single generic update
 * behavior, the method on updateObserver will just be a simple call
 * that generic updater.
 *
 * @author  Jan Hannemann
 * @author  Gregor Kiczales
 * @version 1.1, 02/13/04
 */
 
public abstract aspect ObserverProtocol {  
    
    
    /**
     * This interface is used by extending aspects to say what types
     * can be <i>Subject</i>s. It models the <i>Subject</i> role.
     */

    protected interface Subject  { }    


    /**
     * This interface is used by extending aspects to say what types
     * can be <i>Observer</i>s. It models the <i>Observer</i> role.
     */

    protected interface Observer { }


    /**
     * Stores the mapping between <i>Subject</i>s and <i>
     * Observer</i>s. For each <i>Subject</i>, a <code>LinkedList</code>
     * is of its <i>Observer</i>s is stored.
     */
    
    private WeakHashMap perSubjectObservers;


    /**
     * Returns a <code>Collection</code> of the <i>Observer</i>s of 
     * a particular subject. Used internally.
     *
     * @param subject the <i>subject</i> for which to return the <i>Observer</i>s
     * @return a <code>Collection</code> of s's <i>Observer</i>s
     */

    protected List getObservers(Subject subject) { 
        if (perSubjectObservers == null) {
            perSubjectObservers = new WeakHashMap();
        }
        List observers = (List)perSubjectObservers.get(subject);
        if ( observers == null ) {
            observers = new LinkedList();
            perSubjectObservers.put(subject, observers);
        }
        return observers;
    }

    
    /**
     * Adds an <i>Observer</i> to a <i>Subject</i>. This is the equivalent of <i>
     * attach()</i>, but is a method on the pattern aspect, not the 
     * <i>Subject</i>. 
     *
     * @param s the <i>Subject</i> to attach a new <i>Observer</i> to
     * @param o the new <i>Observer</i> to attach
     */ 
     
    public void    addObserver(Subject subject, Observer observer) { 
        getObservers(subject).add(observer);    
    }
    
    /**
     * Removes an observer from a <i>Subject</i>. This is the equivalent of <i>
     * detach()</i>, but is a method on the pattern aspect, not the <i>Subject</i>. 
     *
     * @param s the <i>Subject</i> to remove the <i>Observer</i> from
     * @param o the <i>Observer</i> to remove
     */ 
    
    public void removeObserver(Subject subject, Observer observer) { 
        getObservers(subject).remove(observer); 
    }

    /**
     * The join points after which to do the update.
     * It replaces the normally scattered calls to <i>notify()</i>. To be
     * concretized by sub-aspects.
     */ 
     
    protected abstract pointcut subjectChange(Subject s);

    /**
     * Calls <code>updateObserver(..)</code> after a change of interest to 
     * update each <i>Observer</i>.
     *
     * @param subject the <i>Subject</i> on which the change occured
     */

    after(Subject subject): subjectChange(subject) {
        Iterator iter = getObservers(subject).iterator();
        while ( iter.hasNext() ) {
            updateObserver(subject, ((Observer)iter.next()));
        }
    } 
    
   /**
     * Defines how each <i>Observer</i> is to be updated when a change
     * to a <i>Subject</i> occurs. To be concretized by sub-aspects.
     *
     * @param subject the <i>Subject</i> on which a change of interest occured
     * @param observer the <i>Observer</i> to be notifed of the change  
     */

    protected abstract void updateObserver(Subject subject, Observer observer);
}
