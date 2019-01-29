/********************************************************************
 * Copyright (c) 2010 Contributors. All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: Nieraj Singh - initial implementation
 *******************************************************************/
package org.aspectj.org.eclipse.jdt.core.dom;

import java.util.List;

/**
 *
 */
public class HasMemberTypePattern extends TypePattern {

	private SignaturePattern signaturePattern;

	public HasMemberTypePattern(AST ast, SignaturePattern signaturePattern) {
		super(ast, signaturePattern.getDetail());
		this.signaturePattern = signaturePattern;
	}

	List<?> internalStructuralPropertiesForType(int apiLevel) {
		return null;
	}

	public SignaturePattern getSignaturePattern() {
		return signaturePattern;
	}

	ASTNode clone0(AST target) {
		ASTNode cloned = new HasMemberTypePattern(target,
				(SignaturePattern) getSignaturePattern().clone(target));
		cloned.setSourceRange(getStartPosition(), getLength());
		return cloned;
	}

	void accept0(ASTVisitor visitor) {
		if (visitor instanceof AjASTVisitor) {
			AjASTVisitor ajVisitor = (AjASTVisitor) visitor;
			boolean visited = ajVisitor.visit(this);
			if (visited) {
				ajVisitor.visit(getSignaturePattern());
			}
			ajVisitor.endVisit(this);
		}
	}

	boolean subtreeMatch0(ASTMatcher matcher, Object other) {
		if (matcher instanceof AjASTMatcher) {
			AjASTMatcher ajmatcher = (AjASTMatcher) matcher;
			return ajmatcher.match(this, other);
		}
		return false;
	}

	int memSize() {
		return super.memSize() + getSignaturePattern().memSize();
	}

}
