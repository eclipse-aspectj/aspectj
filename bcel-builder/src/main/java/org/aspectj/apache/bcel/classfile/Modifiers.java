package org.aspectj.apache.bcel.classfile;

/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache BCEL" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache BCEL", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

import org.aspectj.apache.bcel.Constants;

/**
 * Super class for all objects that have modifiers like private, final, ... I.e.
 * classes, fields, and methods.
 * was AccessFlags
 * 
 * @version $Id: Modifiers.java,v 1.2 2008/05/28 23:53:01 aclement Exp $
 * @author <A HREF="mailto:markus.dahm@berlin.de">M. Dahm</A>
 */
public abstract class Modifiers {

	protected int modifiers;

	public Modifiers() { }

	public Modifiers(int a) {
		modifiers = a;
	}

	public final int getModifiers() {
		return modifiers;
	}
	
	public final void setModifiers(int modifiers) {
		this.modifiers = modifiers;
	}

	public final boolean isPublic() {
		return (modifiers & Constants.ACC_PUBLIC) != 0;
	}

	public final boolean isPrivate() {
		return (modifiers & Constants.ACC_PRIVATE) != 0;
	}

	public final boolean isProtected() {
		return (modifiers & Constants.ACC_PROTECTED) != 0;
	}

	public final boolean isStatic() {
		return (modifiers & Constants.ACC_STATIC) != 0;
	}

	public final boolean isFinal() {
		return (modifiers & Constants.ACC_FINAL) != 0;
	}

	public final boolean isSynchronized() {
		return (modifiers & Constants.ACC_SYNCHRONIZED) != 0;
	}

	public final boolean isVolatile() {
		return (modifiers & Constants.ACC_VOLATILE) != 0;
	}

	public final boolean isTransient() {
		return (modifiers & Constants.ACC_TRANSIENT) != 0;
	}

	public final boolean isNative() {
		return (modifiers & Constants.ACC_NATIVE) != 0;
	}

	public final boolean isInterface() {
		return (modifiers & Constants.ACC_INTERFACE) != 0;
	}

	public final boolean isAbstract() {
		return (modifiers & Constants.ACC_ABSTRACT) != 0;
	}

	public final boolean isStrictfp() {
		return (modifiers & Constants.ACC_STRICT) != 0;
	}

	public final boolean isVarargs() {
		return (modifiers & Constants.ACC_VARARGS) != 0;
	}

	public final boolean isBridge() {
		return (modifiers & Constants.ACC_BRIDGE) != 0;
	}
}
