/* *******************************************************************
 * Copyright (c) 2005 Contributors.
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
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
