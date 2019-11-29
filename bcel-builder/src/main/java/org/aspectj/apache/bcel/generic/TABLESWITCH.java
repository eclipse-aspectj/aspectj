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
package org.aspectj.apache.bcel.generic;

import java.io.DataOutputStream;
import java.io.IOException;

import org.aspectj.apache.bcel.Constants;
import org.aspectj.apache.bcel.util.ByteSequence;

/**
 * TABLESWITCH - Switch within given range of values, i.e., low..high
 * 
 * @version $Id: TABLESWITCH.java,v 1.5 2008/08/28 00:05:29 aclement Exp $
 * @author <A HREF="mailto:markus.dahm@berlin.de">M. Dahm</A>
 * @see SWITCH
 */
public class TABLESWITCH extends InstructionSelect {

	/**
	 * @param match sorted array of match values, match[0] must be low value, match[match_length - 1] high value
	 * @param targets where to branch for matched values
	 * @param target default branch
	 */
	public TABLESWITCH(int[] match, InstructionHandle[] targets, InstructionHandle target) {
		super(org.aspectj.apache.bcel.Constants.TABLESWITCH, match, targets, target);

		// if (match_length==0) {
		// throw new RuntimeException("A tableswitch with no targets should be represented as a LOOKUPSWITCH");
		// }

		// Alignment remainder assumed 0 here, until dump time
		length = (short) (13 + matchLength * 4);
		fixedLength = length;
	}

	/**
	 * Dump instruction as byte code to stream out.
	 * 
	 * @param out Output stream
	 */
	public void dump(DataOutputStream out) throws IOException {
		super.dump(out);

		int low = matchLength > 0 ? match[0] : 0;
		out.writeInt(low);

		int high = matchLength > 0 ? match[matchLength - 1] : 0;
		out.writeInt(high);

		// See aj bug pr104720
		// if (match_length==0) out.writeInt(0); // following the switch you need to supply "HIGH-LOW+1" entries

		for (int i = 0; i < matchLength; i++) {
			out.writeInt(indices[i] = getTargetOffset(targets[i]));
		}
	}

	/**
	 * Read needed data (e.g. index) from file.
	 */
	public TABLESWITCH(ByteSequence bytes) throws IOException {
		super(Constants.TABLESWITCH, bytes);

		int low = bytes.readInt();
		int high = bytes.readInt();

		matchLength = high - low + 1;
		fixedLength = (short) (13 + matchLength * 4);
		length = (short) (fixedLength + padding);

		match = new int[matchLength];
		indices = new int[matchLength];
		targets = new InstructionHandle[matchLength];

		for (int i = low; i <= high; i++) {
			match[i - low] = i;
		}

		for (int i = 0; i < matchLength; i++) {
			indices[i] = bytes.readInt();
		}
	}

}
