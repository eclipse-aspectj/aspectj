/* *******************************************************************
 * Copyright (c) 2008 Contributors
 * 
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *   Andy Clement - refactored out of MemberImpl
 * ******************************************************************/
package org.aspectj.weaver;

import java.lang.reflect.Modifier;

public class SignatureUtils {

	public static String getSignatureString(Member m, World world) {
		MemberKind kind = m.getKind();
		if (kind == Member.METHOD) {
			return getMethodSignatureString(m, world);
		} else if (kind == Member.CONSTRUCTOR) {
			return getConstructorSignatureString(m, world);
		} else if (kind == Member.FIELD) {
			return getFieldSignatureString(m, world);
		} else if (kind == Member.HANDLER) {
			return getHandlerSignatureString(m, world);
		} else if (kind == Member.STATIC_INITIALIZATION) {
			return getStaticInitializationSignatureString(m, world);
		} else if (kind == Member.ADVICE) {
			return getAdviceSignatureString(m, world);
		} else if (kind == Member.MONITORENTER || kind == Member.MONITOREXIT) {
			return getMonitorSignatureString(m, world);
		} else {
			throw new BCException("Do not know the signature string for MemberKind " + kind);
		}
	}

	public static String getSignatureMakerName(Member m) {
		MemberKind kind = m.getKind();
		if (kind == Member.METHOD) {
			return "makeMethodSig";
		} else if (kind == Member.CONSTRUCTOR) {
			return "makeConstructorSig";
		} else if (kind == Member.FIELD) {
			return "makeFieldSig";
		} else if (kind == Member.HANDLER) {
			return "makeCatchClauseSig";
		} else if (kind == Member.STATIC_INITIALIZATION) {
			return "makeInitializerSig";
		} else if (kind == Member.ADVICE) {
			return "makeAdviceSig";
		} else if (kind == Member.MONITORENTER) {
			return "makeLockSig";
		} else if (kind == Member.MONITOREXIT) {
			return "makeUnlockSig";
		} else {
			throw new BCException("Do not know the signature maker name for MemberKind " + kind);
		}
	}

	public static String getSignatureType(Member m) {
		MemberKind kind = m.getKind();
		if (m.getName().equals("<clinit>") && kind != Member.STATIC_INITIALIZATION)
			throw new BCException();
		// if (m.getName().equals("<clinit>")) return "org.aspectj.lang.reflect.InitializerSignature";

		if (kind == Member.METHOD) {
			return "org.aspectj.lang.reflect.MethodSignature";
		} else if (kind == Member.CONSTRUCTOR) {
			return "org.aspectj.lang.reflect.ConstructorSignature";
		} else if (kind == Member.FIELD) {
			return "org.aspectj.lang.reflect.FieldSignature";
		} else if (kind == Member.HANDLER) {
			return "org.aspectj.lang.reflect.CatchClauseSignature";
		} else if (kind == Member.STATIC_INITIALIZATION) {
			return "org.aspectj.lang.reflect.InitializerSignature";
		} else if (kind == Member.ADVICE) {
			return "org.aspectj.lang.reflect.AdviceSignature";
		} else if (kind == Member.MONITORENTER) {
			return "org.aspectj.lang.reflect.LockSignature";
		} else if (kind == Member.MONITOREXIT) {
			return "org.aspectj.lang.reflect.UnlockSignature";
		} else {
			throw new BCException("Do not know the signature type for MemberKind " + kind);
		}
	}

	// ---

	private static String getHandlerSignatureString(Member m, World world) {
		StringBuffer buf = new StringBuffer();
		buf.append(makeString(0));
		buf.append('-');
		// buf.append(getName());
		buf.append('-');
		buf.append(makeString(m.getDeclaringType()));
		buf.append('-');
		buf.append(makeString(m.getParameterTypes()[0]));
		buf.append('-');
		String pName = "<missing>";
		String[] names = m.getParameterNames(world);
		if (names != null)
			pName = names[0];
		buf.append(pName);
		buf.append('-');
		return buf.toString();
	}

	private static String getStaticInitializationSignatureString(Member m, World world) {
		StringBuffer buf = new StringBuffer();
		buf.append(makeString(m.getModifiers(world)));
		buf.append('-');
		// buf.append(getName());
		buf.append('-');
		buf.append(makeString(m.getDeclaringType()));
		buf.append('-');
		return buf.toString();
	}

	protected static String getAdviceSignatureString(Member m, World world) {
		StringBuffer buf = new StringBuffer();
		buf.append(makeString(m.getModifiers(world)));
		buf.append('-');
		buf.append(m.getName());
		buf.append('-');
		buf.append(makeString(m.getDeclaringType()));
		buf.append('-');
		buf.append(makeString(m.getParameterTypes()));
		buf.append('-');
		buf.append(makeString(m.getParameterNames(world)));
		buf.append('-');
		buf.append(makeString(m.getExceptions(world)));
		buf.append('-');
		buf.append(makeString(m.getReturnType()));
		buf.append('-');
		return buf.toString();
	}

	protected static String getMethodSignatureString(Member m, World world) {
		StringBuffer buf = new StringBuffer();
		buf.append(makeString(m.getModifiers(world)));
		buf.append('-');
		buf.append(m.getName());
		buf.append('-');
		buf.append(makeString(m.getDeclaringType()));
		buf.append('-');
		buf.append(makeString(m.getParameterTypes()));
		buf.append('-');
		buf.append(makeString(m.getParameterNames(world)));
		buf.append('-');
		buf.append(makeString(m.getExceptions(world)));
		buf.append('-');
		buf.append(makeString(m.getReturnType()));
		buf.append('-');
		return buf.toString();
	}

	protected static String getMonitorSignatureString(Member m, World world) {
		StringBuffer buf = new StringBuffer();
		buf.append(makeString(Modifier.STATIC)); // modifiers
		buf.append('-');
		buf.append(m.getName()); // name
		buf.append('-');
		buf.append(makeString(m.getDeclaringType())); // Declaring Type
		buf.append('-');
		buf.append(makeString(m.getParameterTypes()[0])); // Parameter Types
		buf.append('-');
		buf.append(""); // Parameter names
		buf.append('-');
		return buf.toString();
	}

	protected static String getConstructorSignatureString(Member m, World world) {
		StringBuffer buf = new StringBuffer();
		buf.append(makeString(m.getModifiers(world)));
		buf.append('-');
		buf.append('-');
		buf.append(makeString(m.getDeclaringType()));
		buf.append('-');
		buf.append(makeString(m.getParameterTypes()));
		buf.append('-');
		buf.append(makeString(m.getParameterNames(world)));
		buf.append('-');
		buf.append(makeString(m.getExceptions(world)));
		buf.append('-');
		return buf.toString();
	}

	protected static String getFieldSignatureString(Member m, World world) {
		StringBuffer buf = new StringBuffer();
		buf.append(makeString(m.getModifiers(world)));
		buf.append('-');
		buf.append(m.getName());
		buf.append('-');
		buf.append(makeString(m.getDeclaringType()));
		buf.append('-');
		buf.append(makeString(m.getReturnType()));
		buf.append('-');
		return buf.toString();
	}

	protected static String makeString(int i) {
		return Integer.toString(i, 16);
	}

	protected static String makeString(UnresolvedType t) {
		// this is the inverse of the odd behavior for Class.forName w/ arrays
		if (t.isArray()) {
			// this behavior matches the string used by the eclipse compiler for Foo.class literals
			return t.getSignature().replace('/', '.');
		} else {
			return t.getName();
		}
	}

	protected static String makeString(UnresolvedType[] types) {
		if (types == null)
			return "";
		StringBuffer buf = new StringBuffer();
		for (int i = 0, len = types.length; i < len; i++) {
			buf.append(makeString(types[i]));
			buf.append(':');
		}
		return buf.toString();
	}

	protected static String makeString(String[] names) {
		if (names == null)
			return "";
		StringBuffer buf = new StringBuffer();
		for (int i = 0, len = names.length; i < len; i++) {
			buf.append(names[i]);
			buf.append(':');
		}
		return buf.toString();
	}

}
