/* *******************************************************************
 * Copyright (c) 2003,2010 Contributors
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Mik Kersten     initial implementation 
 *     Andy Clement    Extensions for better IDE representation
 * ******************************************************************/
package org.aspectj.asm.internal;

import java.util.List;

import org.aspectj.asm.IRelationship;

/**
 * @author Mik Kersten
 * @author Andy Clement
 */
public class Relationship implements IRelationship {

	private static final long serialVersionUID = 3855166397957609120L;

	private String name;
	private Kind kind;
	private boolean isAffects;
	private String sourceHandle;
	private List<String> targets;
	private boolean hasRuntimeTest;

	public Relationship(String name, Kind kind, String sourceHandle, List<String> targets, boolean runtimeTest) {
		this.name = name;
		this.isAffects = name.equals("advises") || name.equals("declares on") || name.equals("softens")
				|| name.equals("matched by") || name.equals("declared on") || name.equals("annotates");
		this.kind = kind;
		this.sourceHandle = sourceHandle;
		this.targets = targets;
		this.hasRuntimeTest = runtimeTest;
	}

	public String getName() {
		return name;
	}

	public Kind getKind() {
		return kind;
	}

	public String toString() {
		return name;
	}

	public String getSourceHandle() {
		return sourceHandle;
	}

	// TODO should be a Set and not a list
	public List<String> getTargets() {
		return targets;
	}

	public void addTarget(String handle) {
		if (targets.contains(handle)) {
			return;
		}
		targets.add(handle);
	}

	public boolean hasRuntimeTest() {
		return hasRuntimeTest;
	}

	/**
	 * Return the direction of the relationship. It might be affects or affected-by. The direction enables the incremental model
	 * repair code to do the right thing.
	 * 
	 * @return true if is an affects relationship: advises/declareson/softens/matchedby/declaredon/annotates
	 */
	public boolean isAffects() {
		return isAffects;
	}

}
