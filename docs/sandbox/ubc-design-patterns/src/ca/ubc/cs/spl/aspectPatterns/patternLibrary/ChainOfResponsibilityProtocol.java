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

/**
 * Defines the general behavior of the chain of responsibility design pattern.
 *
 * Each concrete sub-aspect of this aspect defines one particular instance of
 * the pattern, with an arbitrary number of <i>Handler</i> classes involved.
 *
 * The sub-aspect defines three things: <ol>
 *
 *   <li> what types can be <i>Handlers</i>
 * 
 *   <li> what types can be <i>Request</i>s
 *
 *   <li> what triggers a <i>Request</i>
 *
 *   <li> how each <i>Handler</i> treats a particular request. The default
 *        implementation in this aspect here makes it only necessary to code
 *        those cases where an event is actually handled.
 * </ol>
 *
 * Note that in this implementation, a default behavior for <i>Handler</i>s is
 * defined: unless a <i>Handler</i> explicitly handles an event, it is passed
 * to its successor. In addition to that, the implementation allows to define
 * a default bahavior in case no <i>Handler</i> in the chain handles an event.
 * Here, an exception is raised.  <p>
 *
 * Note further that this version, as all approaches that utilize role 
 * interfaces (empty interfaces identifying the roles in the pattern), has the
 * following limitations: role interfaces only work on types that the weaver 
 * has access to. Further, without generics support for Java/AspectJ, casting
 * is required at some points. 
 *
 * @author  Jan Hannemann
 * @author  Gregor Kiczales
 * @version 1.1, 01/27/04
 *
 */

public abstract aspect ChainOfResponsibilityProtocol {
        
    /**
     * This interface is used by extending aspects to say what types
     * handle requests. It models the <i>Handler</i> role.
     */

	protected interface Handler {}    

	/**
	 * This interface is used by extending aspects to say what types
	 * represent requests. It models the <i>Request</i> role.
	 */

	protected interface Request {}    

    /**
     * Stores the mapping between <code>Handler</code>s and its<i>
     * Successor</i>. For each handler, its <i>Successor</i>
     * is stored.
     */
	
	private WeakHashMap successors = new WeakHashMap();
	
    
    /**
     * Implements the abstracted CoR behavior. Assumption: If the request is
     * not handled, the last receiver handles it by default ( that is true
     * for this implementnation of the protocol). 
     *
     * The current handler gets asked if it wants to handle the request. If
     * not, the request is forwarded to its successor
     */

	protected void receiveRequest(Handler handler, Request request) {          
		if (handler.acceptRequest(request)) {
		    handler.handleRequest(request);
		} else {    
  		    Handler successor = getSuccessor(handler);         
		    if (successor == null) {
				throw new ChainOfResponsibilityException("request unhandled (end of chain reached)\n");
				// This is one way to deal with unhandled requests.
		    } else {
   	    	    receiveRequest(successor, request);
   	    	}
		}
	}  

    /**
     * Method defined for Handlers: returns true if a Handler wants to hanlde 
     * that request. By default, requests are rejected.
     *
     * @param request the request to be handled
     */
		
	public boolean Handler.acceptRequest(Request request) {  
	    return false;
    }
	
    /**
     * Method defined for Handlers: handles a request. Default implementation,
     * does nothing. It is possible to extend this library implementation to
     * shows an error message if this method is not overwritten.
     *
     * @param request the request to be handled
     */
		
	public void Handler.handleRequest(Request request) {}


    /**
     * The join points after which a request is raised.
     * It replaces the normally scattered calls to <i>notify()</i>. To be
     * concretized by sub-aspects.
     */ 
     
    protected abstract pointcut eventTrigger(Handler handler, Request request); 
    

    /**
     * Calls receiveRequest() after a request was started.
     *
     * @param s the subject on which the change occured
     */

	after(Handler handler, Request request): eventTrigger(handler, request) { 
		receiveRequest(handler, request);
	}
 
 
    /**
     * Adds a successor to a <i>Handler</i>.  
     *
     * @param handler the handler to add a new successor to
     * @param successor the new successor to attach
     */ 
     
	public void setSuccessor(Handler handler, Handler successor) {
		successors.put(handler, successor);
	}  

	
    /**
     * Returns the successor of a handler.  
     *
     * @param handler the handler in question
     * @return the successor of the handler
     */ 
     
	public Handler getSuccessor(Handler handler) { 
	    return ((Handler) successors.get(handler));
	}
}