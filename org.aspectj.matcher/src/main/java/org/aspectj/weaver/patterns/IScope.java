/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     PARC     initial implementation 
 * ******************************************************************/

package org.aspectj.weaver.patterns;

import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.IMessageHandler;
import org.aspectj.weaver.IHasPosition;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.World;

public interface IScope {

	/**
	 * @return the type corresponding to the name in this scope, or ResolvedType.MISSING if no such type exists
	 */
	UnresolvedType lookupType(String name, IHasPosition location);

	World getWorld();

	ResolvedType getEnclosingType();

	// these next three are used to create {@link BindingTypePattern} objects.
	IMessageHandler getMessageHandler();

	/**
	 * @return the formal associated with the name, or null if no such formal exists
	 */
	FormalBinding lookupFormal(String name);

	/**
	 * @return the formal with the index. Throws ArrayOutOfBounds exception if out of bounds
	 */
	FormalBinding getFormal(int i);

	int getFormalCount();

	String[] getImportedPrefixes();

	String[] getImportedNames();

	void message(IMessage.Kind kind, IHasPosition location, String message);

	void message(IMessage.Kind kind, IHasPosition location1, IHasPosition location2, String message);

	void message(IMessage aMessage);

	// ISourceLocation makeSourceLocation(ILocation location);
}
