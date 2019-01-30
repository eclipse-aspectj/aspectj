/* *******************************************************************
 * Copyright (c) 2005 Contributors.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Andrew Huff     initial implementation 
 * ******************************************************************/
package org.aspectj.tools.ajdoc;

class DocException extends Exception {
	private static final long serialVersionUID = 3257284725490857778L;

	DocException(String message){
		super(message);
	}
}
