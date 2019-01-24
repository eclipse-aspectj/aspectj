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

import org.aspectj.apache.bcel.util.ByteSequence;

/**
 * Select - Abstract super class for LOOKUPSWITCH and TABLESWITCH instructions.
 * 
 * @version $Id: InstructionSelect.java,v 1.4 2009/10/05 17:35:36 aclement Exp $
 * @author <A HREF="mailto:markus.dahm@berlin.de">M. Dahm</A>
 * @see LOOKUPSWITCH
 * @see TABLESWITCH
 * @see InstructionList
 */
public abstract class InstructionSelect extends InstructionBranch {
	protected int[] match; // matches, i.e., case 1: ...
	protected int[] indices; // target offsets
	protected InstructionHandle[] targets; // target objects in instruction list
	protected int fixedLength; // fixed length defined by subclasses
	protected int matchLength; // number of cases
	protected int padding = 0; // number of pad bytes for alignment

	protected short length;

	/**
	 * (Match, target) pairs for switch. `Match' and `targets' must have the same length of course.
	 * 
	 * @param match array of matching values
	 * @param targets instruction targets
	 * @param target default instruction target
	 */
	InstructionSelect(short opcode, int[] match, InstructionHandle[] targets, InstructionHandle target) {
		super(opcode, target);

		this.targets = targets;
		for (int i = 0; i < targets.length; i++) {
			notifyTarget(null, targets[i], this);
		}

		this.match = match;

		if ((matchLength = match.length) != targets.length) {
			throw new ClassGenException("Match and target array have not the same length");
		}

		indices = new int[matchLength];
	}

	protected int getTargetOffset(InstructionHandle target) {
		if (target == null) {
			throw new ClassGenException("Target of " + super.toString(true) + " is invalid null handle");
		}

		int t = target.getPosition();

		if (t < 0) {
			throw new ClassGenException("Invalid branch target position offset for " + super.toString(true) + ":" + t + ":"
					+ target);
		}

		return t - positionOfThisInstruction;
	}

	/**
	 * Since this is a variable length instruction, it may shift the following instructions which then need to update their
	 * position.
	 * 
	 * Called by InstructionList.setPositions when setting the position for every instruction. In the presence of variable length
	 * instructions `setPositions' performs multiple passes over the instruction list to calculate the correct (byte) positions and
	 * offsets by calling this function.
	 * 
	 * @param offset additional offset caused by preceding (variable length) instructions
	 * @param max_offset the maximum offset that may be caused by these instructions
	 * @return additional offset caused by possible change of this instruction's length
	 */
	protected int updatePosition(int offset, int max_offset) {
		positionOfThisInstruction += offset; // Additional offset caused by
		// preceding SWITCHs, GOTOs,
		// etc.

		short old_length = length;

		/*
		 * Alignment on 4-byte-boundary, + 1, because of tag byte.
		 */
		padding = (4 - (positionOfThisInstruction + 1) % 4) % 4;
		length = (short) (fixedLength + padding); // Update length

		return length - old_length;
	}

	/**
	 * Dump instruction as byte code to stream out.
	 * 
	 * @param out Output stream
	 */
	public void dump(DataOutputStream out) throws IOException {
		out.writeByte(opcode);

		for (int i = 0; i < padding; i++) {
			out.writeByte(0);
		}

		targetIndex = getTargetOffset(); // Write default target offset
		out.writeInt(targetIndex);
	}

	public InstructionSelect(short opcode, ByteSequence bytes) throws IOException {
		super(opcode);
		padding = (4 - bytes.getIndex() % 4) % 4; // Compute number of pad bytes

		for (int i = 0; i < padding; i++) {
			bytes.readByte();
		}

		// Default branch target common for both cases (TABLESWITCH,
		// LOOKUPSWITCH)
		targetIndex = bytes.readInt();
	}

	/**
	 * @return mnemonic for instruction
	 */
	public String toString(boolean verbose) {
		StringBuffer buf = new StringBuffer(super.toString(verbose));

		if (verbose) {
			for (int i = 0; i < matchLength; i++) {
				String s = "null";

				if (targets[i] != null) {
					s = targets[i].getInstruction().toString();
				}

				buf.append("(" + match[i] + ", " + s + " = {" + indices[i] + "})");
			}
		} else {
			buf.append(" ...");
		}

		return buf.toString();
	}

	/**
	 * Set branch target for `i'th case
	 */
	public void setTarget(int i, InstructionHandle target) {
		notifyTarget(targets[i], target, this);
		targets[i] = target;
	}

	/**
	 * @param old_ih old target
	 * @param new_ih new target
	 */
	public void updateTarget(InstructionHandle old_ih, InstructionHandle new_ih) {
		boolean targeted = false;

		if (targetInstruction == old_ih) {
			targeted = true;
			setTarget(new_ih);
		}

		for (int i = 0; i < targets.length; i++) {
			if (targets[i] == old_ih) {
				targeted = true;
				setTarget(i, new_ih);
			}
		}

		if (!targeted) {
			throw new ClassGenException("Not targeting " + old_ih);
		}
	}

	/**
	 * @return true, if ih is target of this instruction
	 */
	public boolean containsTarget(InstructionHandle ih) {
		if (targetInstruction == ih) {
			return true;
		}

		for (int i = 0; i < targets.length; i++) {
			if (targets[i] == ih) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Inform targets that they're not targeted anymore.
	 */
	void dispose() {
		super.dispose();

		for (int i = 0; i < targets.length; i++) {
			targets[i].removeTargeter(this);
		}
	}

	/**
	 * @return array of match indices
	 */
	public int[] getMatchs() {
		return match;
	}

	/**
	 * @return array of match target offsets
	 */
	public int[] getIndices() {
		return indices;
	}

	public boolean equals(Object other) {
		return this == other;
	}

	public int hashCode() {
		return opcode * 37;
	}

	/**
	 * @return array of match targets
	 */
	public InstructionHandle[] getTargets() {
		return targets;
	}

	public int getLength() {
		return length;
	}
}
