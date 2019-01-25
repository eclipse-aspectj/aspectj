/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Matthew Webster - initial implementation
 *******************************************************************************/
package org.aspectj.bea.jvm;

import com.bea.jvm.ClassLibrary;
import com.bea.jvm.JVM;

public class JVMImpl implements JVM {

	private ClassLibrary libarary = new ClassLibraryImpl();
	
	public ClassLibrary getClassLibrary() {
		return libarary;
	}

}
