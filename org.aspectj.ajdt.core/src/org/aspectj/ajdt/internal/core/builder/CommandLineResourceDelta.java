/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     PARC     initial implementation 
 * ******************************************************************/


package org.aspectj.ajdt.internal.core.builder;

import org.eclipse.core.internal.events.ResourceDelta;
import org.eclipse.core.internal.resources.ResourceInfo;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.runtime.IPath;

public class CommandLineResourceDelta extends ResourceDelta {

	private int kind;
	private IResource resource;

	public CommandLineResourceDelta(IResource resource) {
		super(resource.getFullPath(), null);
		setNewInfo(new ResourceInfo());
		setOldInfo(new ResourceInfo());
		children = new ResourceDelta[0];
		this.resource = resource;
	}
	
	public void setKind(int kind) {
		this.kind = kind;	
		if (kind == IResourceDelta.CHANGED) {
			status |= IResourceDelta.CONTENT; // ??? is this alwyas right
		}
	}
	
	public int getKind() {
		return kind;
	}

	public void setChildren(IResourceDelta[] children) {
		this.children = children;
	}
  
	public IResource getResource() {
		return resource;	
	}

	public String toString() {
		return super.toDeepDebugString();
//		StringBuffer s = new StringBuffer();
//		s.append("ResourceDelta(");
//		s.append("path: " + path);
//		s.append(",");
//		s.append("kind: ");
//		s.append(kind);
//		if (children != null) {
//			for (int i = 0; i < children.length; i++) {
//				s.append(children[i].toString());
//			}
//		}
//		s.append(")");
//		return s.toString();
	}
	public IResourceDelta findMember(IPath path) {
		if (resource.getFullPath().equals(path)) {
			return this;	
		} else {
			for (int i = 0; i < children.length; i++) {
				return children[i].findMember(path);
			}
		}
		return null;
	}	
}
