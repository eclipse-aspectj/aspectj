/* *******************************************************************
 * Copyright (c) 2006 Contributors
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andy Clement IBM     initial implementation
 * ******************************************************************/
package org.aspectj.weaver.patterns;


/**
 * Implementors provide a 'verify()' method that is invoked at the end of type
 * binding completion.
 * @see WildTypePattern.VerifyBoundsForTypePattern
 */
public interface IVerificationRequired {
	void verify();
}
