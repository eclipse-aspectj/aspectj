/* *******************************************************************
 * Copyright (c) 2005 Contributors.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *   Adrian Colyer			Initial implementation
 * ******************************************************************/
package org.aspectj.weaver.tools;

import java.util.HashMap;
import java.util.Map;

/**
 * Default implementation of MatchingContext, backed
 * by a Map.
 */
public class DefaultMatchingContext implements MatchingContext {

	private Map contextMap = new HashMap();
	
	/* (non-Javadoc)
	 * @see org.aspectj.weaver.tools.MatchingContext#hasContextParameter(java.lang.String)
	 */
	public boolean hasContextBinding(String contextParameterName) {
		return this.contextMap.containsKey(contextParameterName);
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.tools.MatchingContext#get(java.lang.String)
	 */
	public Object getBinding(String contextParameterName) {
		return this.contextMap.get(contextParameterName);
	}
	
	/**
	 * Add a context binding with the given name and value
	 * @param name
	 * @param value
	 */
	public void addContextBinding(String name, Object value) {
		this.contextMap.put(name, value);
	}
	
	/**
	 * Remove the context binding with the given name
	 * @param name
	 */
	public void removeContextBinding(String name) {
		this.contextMap.remove(name);
	}

}
