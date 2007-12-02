/* *******************************************************************
 * Copyright (c) 2007 Contributors
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Linton Ye https://bugs.eclipse.org/bugs/show_bug.cgi?id=193065
 * ******************************************************************/

package org.aspectj.weaver;

import java.util.Collection;

public interface CustomMungerFactory {
	public Collection/*ShadowMunger*/ createCustomShadowMungers(ResolvedType aspectType);
	public Collection/*ConcreteTypeMunger*/ createCustomTypeMungers(ResolvedType aspectType);
//	public Collection<Declare> createCustomDeclares(ResolvedType aspectType);
}
