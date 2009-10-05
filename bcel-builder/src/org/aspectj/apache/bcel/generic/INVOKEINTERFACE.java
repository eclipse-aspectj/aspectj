package org.aspectj.apache.bcel.generic;

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
import java.io.DataOutputStream;
import java.io.IOException;

import org.aspectj.apache.bcel.Constants;
import org.aspectj.apache.bcel.classfile.ConstantPool;

/**
 * INVOKEINTERFACE - Invoke interface method
 * 
 * <PRE>
 * Stack: ..., objectref, [arg1, [arg2 ...]] -&gt; ...
 * </PRE>
 * 
 * @version $Id: INVOKEINTERFACE.java,v 1.4 2009/10/05 17:35:36 aclement Exp $
 * @author <A HREF="mailto:markus.dahm@berlin.de">M. Dahm</A>
 */
public final class INVOKEINTERFACE extends InvokeInstruction {
	private int nargs; // Number of arguments on stack (number of stack slots), called "count" in vmspec2

	public INVOKEINTERFACE(int index, int nargs, int zerobyte) {
		super(Constants.INVOKEINTERFACE, index);

		if (nargs < 1) {
			throw new ClassGenException("Number of arguments must be > 0 " + nargs);
		}

		this.nargs = nargs;
	}

	/**
	 * Dump instruction as byte code to stream out.
	 * 
	 * @param out Output stream
	 */
	public void dump(DataOutputStream out) throws IOException {
		out.writeByte(opcode);
		out.writeShort(index);
		out.writeByte(nargs);
		out.writeByte(0);
	}

	/**
	 * The <B>count</B> argument according to the Java Language Specification, Second Edition.
	 */
	public int getCount() {
		return nargs;
	}

	/**
	 * @return mnemonic for instruction with symbolic references resolved
	 */
	public String toString(ConstantPool cp) {
		return super.toString(cp) + " " + nargs;
	}

	public int consumeStack(ConstantPool cpg) { // nargs is given in byte-code
		return nargs; // nargs includes this reference
	}

	public boolean equals(Object other) {
		if (!(other instanceof INVOKEINTERFACE)) {
			return false;
		}
		INVOKEINTERFACE o = (INVOKEINTERFACE) other;
		return o.opcode == opcode && o.index == index && o.nargs == nargs;
	}

	public int hashCode() {
		return opcode * 37 + index * (nargs + 17);
	}

}
