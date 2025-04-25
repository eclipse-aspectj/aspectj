/* *******************************************************************
 * Copyright (c) 2010 Contributors
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *
 * Contributors:
 *   Andy Clement, SpringSource
 * ******************************************************************/
package org.aspectj.weaver;

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
