/* *******************************************************************
 * Copyright (c) 2005 Contributors.
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *
 * Contributors:
 *   Adrian Colyer			Initial implementation
 * ******************************************************************/
package org.aspectj.weaver.patterns;

/**
 * @author colyer
 * usage : new HasMemberTypePatternFinder(pattern).hasMemberTypePattern()
 */
public class HasMemberTypePatternFinder extends AbstractPatternNodeVisitor {

	private boolean hasMemberTypePattern = false;

	public HasMemberTypePatternFinder(TypePattern aPattern) {
		aPattern.traverse(this, null);
	}

	public Object visit(HasMemberTypePattern node, Object data) {
		hasMemberTypePattern = true;
		return null;
	}

	public boolean hasMemberTypePattern() { return hasMemberTypePattern; }

}
