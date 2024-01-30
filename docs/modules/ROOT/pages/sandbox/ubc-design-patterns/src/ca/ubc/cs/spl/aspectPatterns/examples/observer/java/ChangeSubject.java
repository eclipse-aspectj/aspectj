package ca.ubc.cs.spl.aspectPatterns.examples.observer.java;

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
 
/**
 * Declares methods to attach and detach <i>Observer<i>s to/from 
 * <i>Subject</i>s, and the <code>notifyObservers()</code> method.
 *
 * @author  Jan Hannemann
 * @author  Gregor Kiczales
 * @version 1.11, 04/01/04
 */
  
public interface ChangeSubject {
    
    /**
     * Attaches an <i>Observer</i> to this <i>Subject</i>.
     * 
     * @param o the <i>Observer</i> to add
     */
     
    public void addObserver(ChangeObserver o);

    /**
     * Detaches an <i>Observer</i> from this <i>Subject</i>.
     * 
     * @param o the <i>Observer</i> to remove
     */
     
    public void removeObserver(ChangeObserver o);

    /**
     * Notifies all <i>Observer</i>s.
     */
   
    public void notifyObservers();
}