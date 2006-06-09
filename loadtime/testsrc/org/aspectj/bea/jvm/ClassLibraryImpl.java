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
import com.bea.jvm.ClassPreProcessor;
import com.bea.jvm.NotAvailableException;

public class ClassLibraryImpl implements ClassLibrary {

	private ClassPreProcessor preProcessor;
	
	public ClassPreProcessor getClassPreProcessor() throws NotAvailableException {
		return preProcessor;
	}
	
	public void setClassPreProcessor(ClassPreProcessor classPreProcessor) {
		this.preProcessor = classPreProcessor;
	}

}
