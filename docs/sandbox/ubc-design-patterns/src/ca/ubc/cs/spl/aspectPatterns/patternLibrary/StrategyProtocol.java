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

import java.util.Hashtable;
 
/**
 * Implements the the abstract Strategy design pattern protocol<p>
 *
 * Defines the <i>Strategy</i> and <i>Context</i> role. Also provides
 * methods for setting and retrieving the <i>Strategy</i> for a
 * given <i>Context</i>. 
 * 
 * It is also possible (although not shown here) to provide abstract 
 * pointcuts for setting or removing <i>Strategy</i>s from 
 * <i>Context</>s. This would allow <i>Client</i> implementations 
 * to be free of pattern code. For an example of how to do that, 
 * see the implementation of the abstract CommandProtocol aspect. 
 *
 * @author  Jan Hannemann
 * @author  Gregor Kiczales
 * @version 1.1, 02/17/04
 */

 
 
public abstract aspect StrategyProtocol {
	
    /**
     * Stores the current strategy for each context
     * 
     * @param numbers the int array to sort
     */
  	Hashtable strategyPerContext = new Hashtable();
    
    /**
     * Defines the <i>Strategy</i> role
     */

	protected interface Strategy { }

    /**
     * Defines the <i>Context</i> role
     */

    protected interface Context  { }   

    /**
     * Sets the strategy for a given context
     * 
     * @param c the context to set the strategy for 
     * @param s the new strategy
     */
 	public void setConcreteStrategy(Context c, Strategy s) {
 		strategyPerContext.put(c, s); 
 	}
    
    /**
     * Returns the strategy for a given context
     * 
     * @param c the context object
     * @return the strategy object for that context
     */
 	public Strategy getConcreteStrategy(Context c) {
 		return (Strategy) strategyPerContext.get(c);
 	}
}