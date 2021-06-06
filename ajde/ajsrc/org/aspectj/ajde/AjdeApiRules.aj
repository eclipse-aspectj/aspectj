/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC),
 *               2003 Contributors.
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *
 * Contributors:
 *     Xerox/PARC      initial implementation
 *     AMC 01.20.2003  extended to support new AspectJ 1.1 options,
 * 				       bugzilla #29769
 * ******************************************************************/

public aspect AjdeApiRules {


	declare warning:
		call(* javax.swing..*(..)) && !within(org.aspectj.ajde.ui.swing..*):
		"do not use Swing outside of org.aspectj.ajde.swing";

}
