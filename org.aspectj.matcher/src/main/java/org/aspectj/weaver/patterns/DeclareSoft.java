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

import org.aspectj.bridge.IMessage;
import org.aspectj.weaver.CompressingDataOutputStream;
import org.aspectj.weaver.ISourceContext;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.TypeVariableReferenceType;
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.VersionedDataInputStream;
import org.aspectj.weaver.WeaverMessages;
import org.aspectj.weaver.World;

public class DeclareSoft extends Declare {
	private TypePattern exception;
	private Pointcut pointcut;

	public DeclareSoft(TypePattern exception, Pointcut pointcut) {
		this.exception = exception;
		this.pointcut = pointcut;
	}

	@Override
	public Object accept(PatternNodeVisitor visitor, Object data) {
		return visitor.visit(this, data);
	}

	@Override
	public Declare parameterizeWith(Map typeVariableBindingMap, World w) {
		DeclareSoft ret = new DeclareSoft(exception.parameterizeWith(typeVariableBindingMap, w), pointcut.parameterizeWith(
				typeVariableBindingMap, w));
		ret.copyLocationFrom(this);
		return ret;
	}

	@Override
	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("declare soft: ");
		buf.append(exception);
		buf.append(": ");
		buf.append(pointcut);
		buf.append(";");
		return buf.toString();
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof DeclareSoft)) {
			return false;
		}
		DeclareSoft o = (DeclareSoft) other;
		return o.pointcut.equals(pointcut) && o.exception.equals(exception);
	}

	@Override
	public int hashCode() {
		int result = 19;
		result = 37 * result + pointcut.hashCode();
		result = 37 * result + exception.hashCode();
		return result;
	}

	@Override
	public void write(CompressingDataOutputStream s) throws IOException {
		s.writeByte(Declare.SOFT);
		exception.write(s);
		pointcut.write(s);
		writeLocation(s);
	}

	public static Declare read(VersionedDataInputStream s, ISourceContext context) throws IOException {
		Declare ret = new DeclareSoft(TypePattern.read(s, context), Pointcut.read(s, context));
		ret.readLocation(context, s);
		return ret;
	}

	public Pointcut getPointcut() {
		return pointcut;
	}

	public TypePattern getException() {
		return exception;
	}

	@Override
	public void resolve(IScope scope) {
		exception = exception.resolveBindings(scope, null, false, true);
		ResolvedType excType = exception.getExactType().resolve(scope.getWorld());
		if (!excType.isMissing()) {
			if (excType.isTypeVariableReference()) {
				TypeVariableReferenceType typeVariableRT = (TypeVariableReferenceType) excType;
				// a declare soft in a generic abstract aspect, we need to check the upper bound
				// WIBBLE
				excType = typeVariableRT.getTypeVariable().getFirstBound().resolve(scope.getWorld());
			}
			if (!scope.getWorld().getCoreType(UnresolvedType.THROWABLE).isAssignableFrom(excType)) {
				scope.getWorld()
						.showMessage(IMessage.ERROR, WeaverMessages.format(WeaverMessages.NOT_THROWABLE, excType.getName()),
								exception.getSourceLocation(), null);
				pointcut = Pointcut.makeMatchesNothing(Pointcut.RESOLVED);
				return;
			}
			// ENH 42743 suggests that we don't soften runtime exceptions.
			if (scope.getWorld().getCoreType(UnresolvedType.RUNTIME_EXCEPTION).isAssignableFrom(excType)) {
				scope.getWorld().getLint().runtimeExceptionNotSoftened.signal(new String[] { excType.getName() }, exception
						.getSourceLocation(), null);
				pointcut = Pointcut.makeMatchesNothing(Pointcut.RESOLVED);
				return;
			}
		}

		pointcut = pointcut.resolve(scope);
	}

	@Override
	public boolean isAdviceLike() {
		return false;
	}

	@Override
	public String getNameSuffix() {
		return "soft";
	}
}
