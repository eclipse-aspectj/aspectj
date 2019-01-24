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
import org.aspectj.apache.bcel.classfile.ConstantPool;

/**
 * Abstract super class for branching instructions like GOTO, IFEQ, etc.. Branch instructions may have a variable length, namely
 * GOTO, JSR, LOOKUPSWITCH and TABLESWITCH. A branch instruction may be talking in terms of absolute destination (targetIndex) or
 * about an instruction it doesnt yet know the position if (targetInstruction). targetInstruction (if set) overrides targetIndex
 * 
 * @see InstructionList
 * @version $Id: InstructionBranch.java,v 1.6 2009/10/05 17:35:36 aclement Exp $
 * @author <A HREF="mailto:markus.dahm@berlin.de">M. Dahm</A>
 */
public class InstructionBranch extends Instruction implements InstructionTargeter {
	private static final int UNSET = -1;

	protected int targetIndex = UNSET; // Branch target relative to this
	// instruction
	protected InstructionHandle targetInstruction; // Target object in
	// instruction list
	protected int positionOfThisInstruction; // for calculating relative branch

	// destinations!

	public InstructionBranch(short opcode, InstructionHandle target) {
		super(opcode);
		setTarget(target);
	}

	public InstructionBranch(short opcode, int index) {
		super(opcode);
		this.targetIndex = index;
	}

	public InstructionBranch(short opcode) {
		super(opcode);
	}

	public void dump(DataOutputStream out) throws IOException {
		int target = getTargetOffset();

		if (Math.abs(target) >= 32767 && opcode != GOTO_W && opcode != JSR_W) {
			throw new ClassGenException("Branch target offset too large for short.  Instruction: " + getName().toUpperCase() + "("
					+ opcode + ")");
		}

		out.writeByte(opcode);

		switch (opcode) {

		case GOTO_W:
		case JSR_W:
			out.writeInt(target);
			break;

		case IF_ACMPEQ:
		case IF_ACMPNE:
		case IF_ICMPEQ:
		case IF_ICMPGE:
		case IF_ICMPGT:
		case IF_ICMPLE:
		case IF_ICMPLT:
		case IF_ICMPNE:
		case IFEQ:
		case IFLE:
		case IFLT:
		case IFGT:
		case IFNE:
		case IFGE:
		case IFNULL:
		case IFNONNULL:
		case GOTO:
		case JSR:
			out.writeShort(target);
			break;

		default:
			throw new IllegalStateException("Don't know how to write out " + getName().toUpperCase());
		}

	}

	protected int getTargetOffset() {
		if (targetInstruction == null && targetIndex == UNSET) {
			throw new ClassGenException("Target of " + super.toString(true) + " is unknown");
		}

		if (targetInstruction == null) {
			return targetIndex;
		} else {
			return targetInstruction.getPosition() - positionOfThisInstruction;
		}
	}

	/**
	 * Called by InstructionList.setPositions when setting the position for every instruction. In the presence of variable length
	 * instructions `setPositions' performs multiple passes over the instruction list to calculate the correct (byte) positions and
	 * offsets by calling this function.
	 * 
	 * @param offset additional offset caused by preceding (variable length) instructions
	 * @param max_offset the maximum offset that may be caused by these instructions
	 * @return additional offset caused by possible change of this instruction's length
	 */
	protected int updatePosition(int offset, int max_offset) {
		int i = getTargetOffset();

		positionOfThisInstruction += offset;

		if (Math.abs(i) >= 32767 - max_offset && opcode != JSR_W && opcode != GOTO_W) {
			// Try and promote it to wide if we can
			if (opcode == JSR || opcode == GOTO) {
				if (opcode == JSR) {
					opcode = JSR_W;
				} else {
					opcode = GOTO_W;
				}
				return 2; // instruction jump destination grows from a short to a long
			} else {
				throw new IllegalStateException("Unable to pack method, jump (with opcode=" + opcode + ") is too far: "
						+ Math.abs(i));
			}
		}

		return 0;
	}

	/**
	 * Long output format:
	 * 
	 * @param verbose long/short format switch
	 * @return mnemonic for instruction
	 */
	public String toString(boolean verbose) {
		String s = super.toString(verbose);
		String t = "null";

		if (verbose) {
			if (targetInstruction != null) {
				if (targetInstruction.getInstruction() == this) {
					t = "<points to itself>";
				} else if (targetInstruction.getInstruction() == null) {
					t = "<null destination>";
				} else {
					t = targetInstruction.getInstruction().toString(false);
				}
			}
		} else {
			if (targetInstruction != null) {
				targetIndex = getTargetOffset();
				t = "" + (targetIndex + positionOfThisInstruction);
			}
		}

		return s + " -> " + t;
	}

	/**
	 * @return target offset in byte code
	 */
	public final int getIndex() {
		return targetIndex;
	}

	/**
	 * @return target of branch instruction
	 */
	public InstructionHandle getTarget() {
		return targetInstruction;
	}

	/**
	 * Set branch target
	 * 
	 * @param target branch target
	 */
	public void setTarget(InstructionHandle target) {
		notifyTarget(this.targetInstruction, target, this);
		this.targetInstruction = target;
	}

	/**
	 * Used by BranchInstruction, LocalVariableGen, CodeExceptionGen
	 */
	static final void notifyTarget(InstructionHandle oldHandle, InstructionHandle newHandle, InstructionTargeter t) {
		if (oldHandle != null) {
			oldHandle.removeTargeter(t);
		}
		if (newHandle != null) {
			newHandle.addTargeter(t);
		}
	}

	/**
	 * Update the target destination for this instruction. If an oldHandle is provided it is checked to verify that is where the
	 * target currently points to before changing it.
	 * 
	 * @param oldHandle old target
	 * @param newHandle new target
	 */
	public void updateTarget(InstructionHandle oldHandle, InstructionHandle newHandle) {
		if (targetInstruction == oldHandle) {
			setTarget(newHandle);
		} else {
			throw new ClassGenException("Not targeting " + oldHandle + ", but " + targetInstruction);
		}
	}

	/**
	 * @return true, if ih is target of this instruction
	 */
	public boolean containsTarget(InstructionHandle ih) {
		return targetInstruction == ih;
	}

	/**
	 * Inform target that it's not targeted anymore.
	 */
	void dispose() {
		setTarget(null);
		targetIndex = -1;
		positionOfThisInstruction = -1;
	}

	public Type getType(ConstantPool cp) {
		if ((Constants.instFlags[opcode] & Constants.JSR_INSTRUCTION) != 0) {
			return new ReturnaddressType(physicalSuccessor());
		}
		return super.getType(cp);
	}

	/**
	 * Returns an InstructionHandle to the physical successor of this JsrInstruction. <B>For this method to work, this
	 * JsrInstruction object must not be shared between multiple InstructionHandle objects!</B> Formally, there must not be
	 * InstructionHandle objects i, j where i != j and i.getInstruction() == this == j.getInstruction().
	 * 
	 * @return an InstructionHandle to the "next" instruction that will be executed when RETurned from a subroutine.
	 */
	public InstructionHandle physicalSuccessor() {
		InstructionHandle ih = this.targetInstruction;

		// Rewind!
		while (ih.getPrev() != null) {
			ih = ih.getPrev();
		}

		// Find the handle for "this" JsrInstruction object.
		while (ih.getInstruction() != this) {
			ih = ih.getNext();
		}

		InstructionHandle toThis = ih;

		while (ih != null) {
			ih = ih.getNext();
			if (ih != null && ih.getInstruction() == this) {
				throw new RuntimeException("physicalSuccessor() called on a shared JsrInstruction.");
			}
		}

		// Return the physical successor
		return toThis.getNext();
	}

	public boolean isIfInstruction() {
		return (Constants.instFlags[opcode] & Constants.IF_INST) != 0;
	}

	/**
	 * Only equal if they are the same branch instruction - otherwise too risky as the targets may only temporarily be pointing at
	 * the same destination.
	 */
	public boolean equals(Object other) {
		return this == other;
	}

	public int hashCode() {
		int result = 17;
		result = opcode * 37 + result;
		return result;
	}
}
