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
import org.aspectj.bridge.ISourceLocation;
import org.aspectj.bridge.Message;
import org.aspectj.bridge.SourceLocation;
import org.aspectj.weaver.IHasPosition;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.World;

public class SimpleScope implements IScope {

    private World world;
    private ResolvedType enclosingType;
    protected FormalBinding[] bindings;

	private String[] importedPrefixes = javaLangPrefixArray;
	private String[] importedNames = ZERO_STRINGS;
    
    public SimpleScope(World world, FormalBinding[] bindings) {
        super();
        this.world = world;
        this.bindings = bindings;
    }

	// ---- impl

	//XXX doesn't report any problems
	public UnresolvedType lookupType(String name, IHasPosition location) {
		for (int i=0; i<importedNames.length; i++) {
			String importedName = importedNames[i];
//			// make sure we're matching against the
//			// type name rather than part of it
//			if (importedName.endsWith("." + name)) {
			if (importedName.endsWith(name)) {
				return world.resolve(importedName);
			}
		}
		
		for (int i=0; i<importedPrefixes.length; i++) {
			String importedPrefix = importedPrefixes[i];
			ResolvedType tryType = world.resolve(UnresolvedType.forName(importedPrefix + name), true);
			if (!tryType.isMissing()) {
				return tryType;
			}
		}

		return world.resolve(UnresolvedType.forName(name), true);
	}


    public IMessageHandler getMessageHandler() {
        return world.getMessageHandler();
    }
    public FormalBinding lookupFormal(String name) {
        for (int i = 0, len = bindings.length; i < len; i++) {
            if (bindings[i].getName().equals(name)) return bindings[i];
        }
        return null;
    }
    public FormalBinding getFormal(int i) {
        return bindings[i];
    }
    
    public int getFormalCount() {
    	return bindings.length;
    }

	public String[] getImportedNames() {
		return importedNames;
	}
	public String[] getImportedPrefixes() {
		return importedPrefixes;
	}
	
	public void setImportedNames(String[] importedNames) {
		this.importedNames = importedNames;
	}
	public void setImportedPrefixes(String[] importedPrefixes) {
		this.importedPrefixes = importedPrefixes;
	}
	
	public static FormalBinding[] makeFormalBindings(UnresolvedType[] types, String[] names) {
        int len = types.length;
        FormalBinding[] bindings = new FormalBinding[len];
        for (int i = 0; i < len; i++) {
            bindings[i] = new FormalBinding(types[i], names[i], i);
        }
        return bindings;
	}
	
	// ---- fields
	
	public static final String[] ZERO_STRINGS = new String[0];
	
	public static final String[] javaLangPrefixArray =
		new String[] { "java.lang.", };



	public ISourceLocation makeSourceLocation(IHasPosition location) {
		return new SourceLocation(ISourceLocation.NO_FILE, 0);
	}

	public void message(
		IMessage.Kind kind,
		IHasPosition location1,
		IHasPosition location2,
		String message) {
			message(kind, location1, message);
			message(kind, location2, message);
	}

	public void message(
		IMessage.Kind kind,
		IHasPosition location,
		String message) {
			getMessageHandler()
			.handleMessage(new Message(message, kind, null, makeSourceLocation(location)));

	}
	
	public void message(IMessage aMessage) {
		getMessageHandler().handleMessage(aMessage);
	}
	
	public World getWorld() {
		return world;
	}

	public ResolvedType getEnclosingType() {
		return enclosingType;
	}

}
