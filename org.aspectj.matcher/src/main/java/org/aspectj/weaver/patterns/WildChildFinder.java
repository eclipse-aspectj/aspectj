/* *******************************************************************
 * Copyright (c) 2019 Contributors
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * ******************************************************************/
package org.aspectj.weaver.patterns;

/**
 * @author Tuomas Kiviaho
 */
public class WildChildFinder extends AbstractPatternNodeVisitor {

	private boolean wildChild;

	public WildChildFinder() {
		super();
	}
	
	public boolean containedWildChild() {
		return wildChild;
	}

	@Override
	public Object visit(WildAnnotationTypePattern node, Object data) {
		node.getTypePattern().accept(this, data);
		return node;
	}

	@Override
	public Object visit(WildTypePattern node, Object data) {
		this.wildChild = true;
		return super.visit(node, data);
	}

	@Override
	public Object visit(AndTypePattern node, Object data) {
		node.getLeft().accept(this, data);
		if (!this.wildChild) {
			node.getRight().accept(this, data);
		}
		return node;
	}

	@Override
	public Object visit(OrTypePattern node, Object data) {
		node.getLeft().accept(this, data);
		if (!this.wildChild) {
			node.getRight().accept(this, data);
		}
		return node;
	}

	public Object visit(NotTypePattern node, Object data) {
		node.getNegatedPattern().accept(this, data);
		return node;
	}

	@Override
	public Object visit(AnyWithAnnotationTypePattern node, Object data) {
		node.getAnnotationPattern().accept(this, data);
		return node;
	}

}
