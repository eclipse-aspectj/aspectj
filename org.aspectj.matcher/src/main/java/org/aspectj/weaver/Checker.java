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

package org.aspectj.weaver;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.aspectj.bridge.ISourceLocation;
import org.aspectj.weaver.patterns.DeclareErrorOrWarning;
import org.aspectj.weaver.patterns.PerClause;
import org.aspectj.weaver.patterns.Pointcut;

/**
 * Representation of a shadow munger for a declare error or warning declaration.
 * 
 * @author Andy Clement
 */
public class Checker extends ShadowMunger {

	private boolean isError; // if not error then it is a warning
	private String message;
	private volatile int hashCode = -1;

	@SuppressWarnings("unused")
	private Checker() {
	}

	/**
	 * Create a Checker for a declare error or declare warning.
	 * 
	 * @param deow the declare error or declare warning for which to create the checker munger
	 */
	public Checker(DeclareErrorOrWarning deow) {
		super(deow.getPointcut(), deow.getStart(), deow.getEnd(), deow.getSourceContext(), ShadowMungerDeow);
		this.message = deow.getMessage();
		this.isError = deow.isError();
	}

	/**
	 * Only used when filling in a parameterized Checker
	 */
	private Checker(Pointcut pointcut, int start, int end, ISourceContext context, String message, boolean isError) {
		super(pointcut, start, end, context, ShadowMungerDeow);
		this.message = message;
		this.isError = isError;
	}

	public boolean isError() {
		return isError;
	}

	public String getMessage(Shadow shadow) {
		return format(this.message, shadow);
	}

	@Override
	public void specializeOn(Shadow shadow) {
		throw new IllegalStateException("Cannot call specializeOn(...) for a Checker");
	}

	@Override
	public boolean implementOn(Shadow shadow) {
		throw new IllegalStateException("Cannot call implementOn(...) for a Checker");
	}

	/**
	 * Determine if the Checker matches at a shadow. If it does then we can immediately report the message. Currently, there can
	 * never be a non-statically determinable match.
	 * 
	 * @param shadow the shadow which to match against
	 * @param world the world through which to access message handlers
	 */
	@Override
	public boolean match(Shadow shadow, World world) {
		if (super.match(shadow, world)) {
			world.reportCheckerMatch(this, shadow);
		}
		return false;
	}

	// implementation for PartialOrder.PartialComparable
	public int compareTo(Object other) {
		return 0;
	}

	@Override
	public boolean mustCheckExceptions() {
		return true;
	}

	@Override
	public Collection<ResolvedType> getThrownExceptions() {
		return Collections.emptyList();
	}

	// FIXME this perhaps ought to take account of the other fields in advice (use super.equals?)
	@Override
	public boolean equals(Object other) {
		if (!(other instanceof Checker)) {
			return false;
		}
		Checker o = (Checker) other;
		return o.isError == isError && ((o.pointcut == null) ? (pointcut == null) : o.pointcut.equals(pointcut));
	}

	@Override
	public int hashCode() {
		if (hashCode == -1) {
			int result = 17;
			result = 37 * result + (isError ? 1 : 0);
			result = 37 * result + ((pointcut == null) ? 0 : pointcut.hashCode());
			hashCode = result;
		}
		return hashCode;
	}

	/**
	 * Parameterize the Checker by parameterizing the pointcut
	 */
	@Override
	public ShadowMunger parameterizeWith(ResolvedType declaringType, Map<String, UnresolvedType> typeVariableMap) {
		Checker ret = new Checker(this.pointcut.parameterizeWith(typeVariableMap, declaringType.getWorld()), this.start, this.end,
				this.sourceContext, this.message, this.isError);
		return ret;
	}

	/**
	 * Concretize this Checker by concretizing the pointcut
	 */
	@Override
	public ShadowMunger concretize(ResolvedType theAspect, World world, PerClause clause) {
		this.pointcut = this.pointcut.concretize(theAspect, getDeclaringType(), 0, this);
		this.hashCode = -1;
		return this;
	}

	@Override
	public ResolvedType getConcreteAspect() {
		return getDeclaringType();
	}

	// public void write(DataOutputStream stream) throws IOException {
	// super.write(stream);
	// stream.writeBoolean(isError);
	// stream.writeUTF(message);
	// }
	//
	// public static Checker read(DataInputStream stream, World world) throws IOException {
	// Checker checker = new Checker();
	// checker.isError = stream.readBoolean();
	// checker.message = stream.readUTF();
	// return checker;
	// }

	// Return the next non-escaped (with a '\') open curly
	private int nextCurly(String string, int pos) {
		do {
			int curlyIndex = string.indexOf('{', pos);
			if (curlyIndex == -1) {
				return -1;
			}
			if (curlyIndex == 0) {
				return 0;
			}
			if (string.charAt(curlyIndex - 1) != '\\') {
				return curlyIndex;
			}
			pos = curlyIndex + 1;
		} while (pos < string.length());
		return -1;
	}

	private String format(String msg, Shadow shadow) {
		int pos = 0;
		int curlyIndex = nextCurly(msg, 0);
		if (curlyIndex == -1) {
			// was there an escaped one?
			if (msg.indexOf('{') != -1) {
				return msg.replace("\\{", "{");
			} else {
				return msg;
			}
		}
		StringBuffer ret = new StringBuffer();
		while (curlyIndex >= 0) {
			if (curlyIndex > 0) {
				ret.append(msg.substring(pos, curlyIndex).replace("\\{", "{"));
			}
			int endCurly = msg.indexOf('}', curlyIndex);
			if (endCurly == -1) {
				// wasn't closed properly - ignore it
				ret.append('{');
				pos = curlyIndex + 1;
			} else {
				ret.append(getValue(msg.substring(curlyIndex + 1, endCurly), shadow));
			}
			pos = endCurly + 1;
			curlyIndex = nextCurly(msg, pos);
		}
		ret.append(msg.substring(pos, msg.length()));
		return ret.toString();
	}

	/**
	 * @param buf the buffer in which to insert the substitution
	 * @param shadow shadow from which to draw context info
	 * @param c the substitution character
	 */
	private String getValue(String key, Shadow shadow) {
		if (key.equalsIgnoreCase("joinpoint")) {
			return shadow.toString();
		} else if (key.equalsIgnoreCase("joinpoint.kind")) {
			return shadow.getKind().getName();
		} else if (key.equalsIgnoreCase("joinpoint.enclosingclass")) {
			return shadow.getEnclosingType().getName();
		} else if (key.equalsIgnoreCase("joinpoint.enclosingmember.name")) {
			Member member = shadow.getEnclosingCodeSignature();
			if (member==null) {
				return "";
			} else {
				return member.getName();
			}
		} else if (key.equalsIgnoreCase("joinpoint.enclosingmember")) {
			Member member = shadow.getEnclosingCodeSignature();
			if (member==null) {
				return "";
			} else {
				return member.toString();
			}
		} else if (key.equalsIgnoreCase("joinpoint.signature")) {
			return shadow.getSignature().toString();
		} else if (key.equalsIgnoreCase("joinpoint.signature.declaringtype")) {
			return shadow.getSignature().getDeclaringType().toString();
		} else if (key.equalsIgnoreCase("joinpoint.signature.name")) {
			return shadow.getSignature().getName();
		} else if (key.equalsIgnoreCase("joinpoint.sourcelocation.sourcefile")) {
			ISourceLocation loc = shadow.getSourceLocation();
			if ((loc != null) && (loc.getSourceFile() != null)) {
				return loc.getSourceFile().toString();
			} else {
				return "UNKNOWN";
			}
		} else if (key.equalsIgnoreCase("joinpoint.sourcelocation.line")) {
			ISourceLocation loc = shadow.getSourceLocation();
			if (loc != null) {
				return Integer.toString(loc.getLine());
			} else {
				return "-1";
			}
		} else if (key.equalsIgnoreCase("advice.aspecttype")) {
			return getDeclaringType().getName();
		} else if (key.equalsIgnoreCase("advice.sourcelocation.line")) {
			ISourceLocation loc = getSourceLocation();
			if ((loc != null) && (loc.getSourceFile() != null)) {
				return Integer.toString(loc.getLine());
			} else {
				return "-1";
			}
		} else if (key.equalsIgnoreCase("advice.sourcelocation.sourcefile")) {
			ISourceLocation loc = getSourceLocation();
			if ((loc != null) && (loc.getSourceFile() != null)) {
				return loc.getSourceFile().toString();
			} else {
				return "UNKNOWN";
			}
		} else {
			return "UNKNOWN_KEY{" + key + "}";
		}
	}
}
