/* *******************************************************************
 * Copyright (c) 2010 Contributors
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Andy Clement - SpringSource
 * ******************************************************************/
package org.aspectj.weaver.patterns;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.aspectj.weaver.ISourceContext;
import org.aspectj.weaver.Member;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.VersionedDataInputStream;
import org.aspectj.weaver.World;

/**
 * Represents the NOT of a signature pattern
 * 
 * @author Andy Clement
 * @since 1.6.9
 */
public class NotSignaturePattern extends AbstractSignaturePattern {

	private ISignaturePattern negatedSp;

	public NotSignaturePattern(ISignaturePattern negatedSp) {
		this.negatedSp = negatedSp;
	}

	public boolean couldEverMatch(ResolvedType type) {
		if (negatedSp.getExactDeclaringTypes().size() == 0) {
			return true;
		}
		return !negatedSp.couldEverMatch(type);
	}

	public List<ExactTypePattern> getExactDeclaringTypes() {
		return negatedSp.getExactDeclaringTypes();
	}

	public boolean isMatchOnAnyName() {
		return negatedSp.isMatchOnAnyName();
	}

	public boolean isStarAnnotation() {
		return negatedSp.isStarAnnotation();
	}

	public boolean matches(Member member, World world, boolean b) {
		return !negatedSp.matches(member, world, b);
	}

	public ISignaturePattern parameterizeWith(Map<String, UnresolvedType> typeVariableBindingMap, World world) {
		return new NotSignaturePattern(negatedSp.parameterizeWith(typeVariableBindingMap, world));
	}

	public ISignaturePattern resolveBindings(IScope scope, Bindings bindings) {
		// Whilst the real SignaturePattern returns 'this' we are safe to return 'this' here rather than build a new
		// AndSignaturePattern
		negatedSp.resolveBindings(scope, bindings);
		return this;
	}

	public static ISignaturePattern readNotSignaturePattern(VersionedDataInputStream s, ISourceContext context) throws IOException {
		NotSignaturePattern ret = new NotSignaturePattern(readCompoundSignaturePattern(s, context));
		// ret.readLocation(context, s); // TODO output position currently useless so dont need to do this
		s.readInt();
		s.readInt();
		return ret;
	}

	public ISignaturePattern getNegated() {
		return negatedSp;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("!").append(negatedSp.toString());
		return sb.toString();
	}

}
