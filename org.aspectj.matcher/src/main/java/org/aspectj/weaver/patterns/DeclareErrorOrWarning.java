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

import org.aspectj.weaver.CompressingDataOutputStream;
import org.aspectj.weaver.ISourceContext;
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.VersionedDataInputStream;
import org.aspectj.weaver.World;

public class DeclareErrorOrWarning extends Declare {
	private boolean isError;
	private Pointcut pointcut;
	private String message;

	public DeclareErrorOrWarning(boolean isError, Pointcut pointcut, String message) {
		this.isError = isError;
		this.pointcut = pointcut;
		this.message = message;
	}

	/**
	 * returns "declare warning: &lt;message&gt;" or "declare error: &lt;message&gt;"
	 */
	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("declare ");
		if (isError) {
			buf.append("error: ");
		} else {
			buf.append("warning: ");
		}
		buf.append(pointcut);
		buf.append(": ");
		buf.append("\"");
		buf.append(message);
		buf.append("\";");
		return buf.toString();
	}

	public boolean equals(Object other) {
		if (!(other instanceof DeclareErrorOrWarning)) {
			return false;
		}
		DeclareErrorOrWarning o = (DeclareErrorOrWarning) other;
		return (o.isError == isError) && o.pointcut.equals(pointcut) && o.message.equals(message);
	}

	public int hashCode() {
		int result = isError ? 19 : 23;
		result = 37 * result + pointcut.hashCode();
		result = 37 * result + message.hashCode();
		return result;
	}

	public Object accept(PatternNodeVisitor visitor, Object data) {
		return visitor.visit(this, data);
	}

	public void write(CompressingDataOutputStream s) throws IOException {
		s.writeByte(Declare.ERROR_OR_WARNING);
		s.writeBoolean(isError);
		pointcut.write(s);
		s.writeUTF(message);
		writeLocation(s);
	}

	public static Declare read(VersionedDataInputStream s, ISourceContext context) throws IOException {
		Declare ret = new DeclareErrorOrWarning(s.readBoolean(), Pointcut.read(s, context), s.readUTF());
		ret.readLocation(context, s);
		return ret;
	}

	public boolean isError() {
		return isError;
	}

	public String getMessage() {
		return message;
	}

	public Pointcut getPointcut() {
		return pointcut;
	}

	public void resolve(IScope scope) {
		pointcut = pointcut.resolve(scope);
	}

	public Declare parameterizeWith(Map<String,UnresolvedType> typeVariableBindingMap, World w) {
		Declare ret = new DeclareErrorOrWarning(isError, pointcut.parameterizeWith(typeVariableBindingMap, w), message);
		ret.copyLocationFrom(this);
		return ret;
	}

	public boolean isAdviceLike() {
		return true;
	}

	public String getNameSuffix() {
		return "eow";
	}

	/**
	 * returns "declare warning" or "declare error"
	 */
	public String getName() {
		StringBuffer buf = new StringBuffer();
		buf.append("declare ");
		if (isError) {
			buf.append("error");
		} else {
			buf.append("warning");
		}
		return buf.toString();
	}
}
