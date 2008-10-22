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
package org.aspectj.weaver.reflect;

import org.aspectj.weaver.tools.JoinPointMatch;
import org.aspectj.weaver.tools.PointcutParameter;

/**
 * @author colyer
 * Implementation of JoinPointMatch for reflection based worlds.
 */
public class JoinPointMatchImpl implements JoinPointMatch {

	public final static JoinPointMatch NO_MATCH = new JoinPointMatchImpl();
	private final static PointcutParameter[] NO_BINDINGS = new PointcutParameter[0];
	
	private boolean match;
	private PointcutParameter[] bindings;
	
	public JoinPointMatchImpl(PointcutParameter[] bindings) {
		this.match = true;
		this.bindings = bindings;
	}
	
	private JoinPointMatchImpl() {
		this.match = false;
		this.bindings = NO_BINDINGS;
	}
	
	/* (non-Javadoc)
	 * @see org.aspectj.weaver.tools.JoinPointMatch#matches()
	 */
	public boolean matches() {
		return match;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.tools.JoinPointMatch#getParameterBindings()
	 */
	public PointcutParameter[] getParameterBindings() {
		return bindings;
	}

}
