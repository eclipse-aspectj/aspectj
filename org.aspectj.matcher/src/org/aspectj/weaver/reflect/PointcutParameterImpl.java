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

import org.aspectj.weaver.tools.PointcutParameter;

public class PointcutParameterImpl implements PointcutParameter {

	String name;
	Class type;
	Object binding;
	
	public PointcutParameterImpl(String name, Class type) {
		this.name = name;
		this.type = type;
	}
	
	public String getName() {
		return name;
	}

	public Class getType() {
		return type;
	}

	public Object getBinding() {
		return binding;
	}
	
	void setBinding(Object boundValue) {
		this.binding = boundValue;
	}
	
}