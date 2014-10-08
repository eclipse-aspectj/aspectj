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

import java.io.IOException;
import java.util.Map;

import org.aspectj.bridge.MessageUtil;
import org.aspectj.util.FuzzyBoolean;
import org.aspectj.weaver.CompressingDataOutputStream;
import org.aspectj.weaver.ISourceContext;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.Shadow;
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.VersionedDataInputStream;
import org.aspectj.weaver.WeaverMessages;
import org.aspectj.weaver.World;
import org.aspectj.weaver.ast.Test;

public class PerFromSuper extends PerClause {
	private PerClause.Kind kind;

	public PerFromSuper(PerClause.Kind kind) {
		this.kind = kind;
	}

	public Object accept(PatternNodeVisitor visitor, Object data) {
		return visitor.visit(this, data);
	}

	public int couldMatchKinds() {
		return Shadow.ALL_SHADOW_KINDS_BITS;
	}

	public FuzzyBoolean fastMatch(FastMatchInfo type) {
		throw new RuntimeException("unimplemented");
	}

	protected FuzzyBoolean matchInternal(Shadow shadow) {
		throw new RuntimeException("unimplemented");
	}

	public void resolveBindings(IScope scope, Bindings bindings) {
		// this method intentionally left blank
	}

	protected Test findResidueInternal(Shadow shadow, ExposedState state) {
		throw new RuntimeException("unimplemented");
	}

	public PerClause concretize(ResolvedType inAspect) {
		PerClause p = lookupConcretePerClause(inAspect.getSuperclass());
		if (p == null) {
			inAspect.getWorld().getMessageHandler().handleMessage(
					MessageUtil.error(WeaverMessages.format(WeaverMessages.MISSING_PER_CLAUSE, inAspect.getSuperclass()),
							getSourceLocation()));
			return new PerSingleton().concretize(inAspect);// AV: fallback on something else NPE in AJDT
		} else {
			if (p.getKind() != kind) {
				inAspect.getWorld().getMessageHandler().handleMessage(
						MessageUtil.error(WeaverMessages.format(WeaverMessages.WRONG_PER_CLAUSE, kind, p.getKind()),
								getSourceLocation()));
			}
			return p.concretize(inAspect);
		}
	}

	public Pointcut parameterizeWith(Map<String,UnresolvedType> typeVariableMap, World w) {
		return this;
	}

	public PerClause lookupConcretePerClause(ResolvedType lookupType) {
		PerClause ret = lookupType.getPerClause();
		if (ret == null) {
			return null;
		}
		if (ret instanceof PerFromSuper) {
			return lookupConcretePerClause(lookupType.getSuperclass());
		}
		return ret;
	}

	public void write(CompressingDataOutputStream s) throws IOException {
		FROMSUPER.write(s);
		kind.write(s);
		writeLocation(s);
	}

	public static PerClause readPerClause(VersionedDataInputStream s, ISourceContext context) throws IOException {
		PerFromSuper ret = new PerFromSuper(Kind.read(s));
		ret.readLocation(context, s);
		return ret;
	}

	public String toString() {
		return "perFromSuper(" + kind + ", " + inAspect + ")";
	}

	public String toDeclarationString() {
		return "";
	}

	public PerClause.Kind getKind() {
		return kind;
	}

	public boolean equals(Object other) {
		if (!(other instanceof PerFromSuper)) {
			return false;
		}
		PerFromSuper pc = (PerFromSuper) other;
		return pc.kind.equals(kind) && ((pc.inAspect == null) ? (inAspect == null) : pc.inAspect.equals(inAspect));
	}

	public int hashCode() {
		int result = 17;
		result = 37 * result + kind.hashCode();
		result = 37 * result + ((inAspect == null) ? 0 : inAspect.hashCode());
		return result;
	}

}
