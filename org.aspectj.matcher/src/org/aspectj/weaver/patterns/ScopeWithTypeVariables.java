/* *******************************************************************
 * Copyright (c) 2005 Contributors.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *   Adrian Colyer			Initial implementation
 * ******************************************************************/
package org.aspectj.weaver.patterns;

import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.IMessageHandler;
import org.aspectj.bridge.IMessage.Kind;
import org.aspectj.weaver.IHasPosition;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.TypeVariable;
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.UnresolvedTypeVariableReferenceType;
import org.aspectj.weaver.World;

/**
 * A scope that also considers type variables when looking up a type.
 *
 */
public class ScopeWithTypeVariables implements IScope {

	private IScope delegateScope;
	private String[] typeVariableNames;
	private UnresolvedTypeVariableReferenceType[] typeVarTypeXs;
	
	public ScopeWithTypeVariables(String[] typeVarNames, IScope delegate) {
		this.delegateScope = delegate;
		this.typeVariableNames = typeVarNames;
		this.typeVarTypeXs = new UnresolvedTypeVariableReferenceType[typeVarNames.length];
	}
	
	/* (non-Javadoc)
	 * @see org.aspectj.weaver.patterns.IScope#lookupType(java.lang.String, org.aspectj.weaver.IHasPosition)
	 */
	public UnresolvedType lookupType(String name, IHasPosition location) {
		for (int i = 0; i < typeVariableNames.length; i++) {
			if (typeVariableNames[i].equals(name)) {
				if (typeVarTypeXs[i] == null) {
					typeVarTypeXs[i] = new UnresolvedTypeVariableReferenceType(new TypeVariable(name));
				}
				return typeVarTypeXs[i];
			}
		}
		return delegateScope.lookupType(name, location);
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.patterns.IScope#getWorld()
	 */
	public World getWorld() {
		return delegateScope.getWorld();
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.patterns.IScope#getEnclosingType()
	 */
	public ResolvedType getEnclosingType() {
		return delegateScope.getEnclosingType();
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.patterns.IScope#getMessageHandler()
	 */
	public IMessageHandler getMessageHandler() {
		return delegateScope.getMessageHandler();
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.patterns.IScope#lookupFormal(java.lang.String)
	 */
	public FormalBinding lookupFormal(String name) {
		return delegateScope.lookupFormal(name);
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.patterns.IScope#getFormal(int)
	 */
	public FormalBinding getFormal(int i) {
		return delegateScope.getFormal(i);
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.patterns.IScope#getFormalCount()
	 */
	public int getFormalCount() {
		return delegateScope.getFormalCount();
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.patterns.IScope#getImportedPrefixes()
	 */
	public String[] getImportedPrefixes() {
		return delegateScope.getImportedPrefixes();
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.patterns.IScope#getImportedNames()
	 */
	public String[] getImportedNames() {
		return delegateScope.getImportedNames();
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.patterns.IScope#message(org.aspectj.bridge.IMessage.Kind, org.aspectj.weaver.IHasPosition, java.lang.String)
	 */
	public void message(Kind kind, IHasPosition location, String message) {
		delegateScope.message(kind, location, message);
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.patterns.IScope#message(org.aspectj.bridge.IMessage.Kind, org.aspectj.weaver.IHasPosition, org.aspectj.weaver.IHasPosition, java.lang.String)
	 */
	public void message(Kind kind, IHasPosition location1,
			IHasPosition location2, String message) {
		delegateScope.message(kind,location1,location2,message);
	}

	public void message(IMessage aMessage) {
		delegateScope.message(aMessage);
	}

}
