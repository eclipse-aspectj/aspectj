/********************************************************************
 * Copyright (c) 2006 Contributors. All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *
 * Contributors: IBM Corporation - initial API and implementation
 * 				 Helen Hawkins   - iniital version
 *******************************************************************/
package org.aspectj.weaver;

public interface IEclipseSourceContext extends ISourceContext {
	void removeUnnecessaryProblems(Member method, int problemLineNumber);
}
