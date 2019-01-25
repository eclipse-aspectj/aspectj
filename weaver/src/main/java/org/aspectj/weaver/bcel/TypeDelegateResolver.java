/* *******************************************************************
 * Copyright (c) 2010 Contributors
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Andy Clement, SpringSource
 * ******************************************************************/
package org.aspectj.weaver.bcel;

import org.aspectj.weaver.ReferenceType;
import org.aspectj.weaver.ReferenceTypeDelegate;

/**
 * A type delegate resolver is able to create type delegates for a named reference type.  A type delegate will implement
 * ReferenceTypeDelegate.  There are three kind of delegate already in existence: those created for eclipse structures, those
 * created for bytecode structures, and those created based on reflection.  
 * 
 * @author Andy Clement
 */
public interface TypeDelegateResolver {

	ReferenceTypeDelegate getDelegate(ReferenceType referenceType);

}
