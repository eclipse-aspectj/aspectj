/* *******************************************************************
 * Copyright (c) 2005 Contributors.
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
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
