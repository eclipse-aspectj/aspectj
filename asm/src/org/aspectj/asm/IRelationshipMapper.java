/* *******************************************************************
 * Copyright (c) 2003 Contributors.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     Mik Kersten     initial implementation 
 * ******************************************************************/

package org.aspectj.asm;

import java.io.Serializable;
import java.util.List;

import org.aspectj.asm.IRelationship.Kind;

/**
 * @author Mik Kersten
 */
public interface IRelationshipMapper extends Serializable {
 
 	// TODO: return a list
	public IRelationship get(IProgramElement source);
	
	// TODO: return a list 
	public IRelationship get(String handle);
	
	public void put(IProgramElement source, IRelationship relationship);
	
	
 
}
