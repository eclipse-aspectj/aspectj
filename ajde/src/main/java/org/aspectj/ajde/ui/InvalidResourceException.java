/* *******************************************************************
 * Copyright (c) 1999-2001 Xerox Corporation,
 *               2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *
 * Contributors:
 *     Xerox/PARC     initial implementation
 * ******************************************************************/


package org.aspectj.ajde.ui;

/**
 * @author Mik Kersten
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window&gt;Preferences&gt;Java&gt;Templates.
 */
public class InvalidResourceException extends Exception {

	private static final long serialVersionUID = -5290919159396792978L;

	/**
	 * Constructor for InvalidResourceException.
	 */
	public InvalidResourceException() {
		super();
	}

	/**
	 * Constructor for InvalidResourceException.
	 * @param s
	 */
	public InvalidResourceException(String s) {
		super(s);
	}

}
