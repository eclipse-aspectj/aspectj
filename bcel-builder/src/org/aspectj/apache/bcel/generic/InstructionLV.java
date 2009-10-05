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

/**
 * Abstract super class for instructions dealing with local variables.
 * 
 * @version $Id: InstructionLV.java,v 1.5 2009/10/05 17:35:36 aclement Exp $
 * @author <A HREF="mailto:markus.dahm@berlin.de">M. Dahm</A>
 */
public class InstructionLV extends Instruction {
	protected int lvar = -1;

	public InstructionLV(short opcode, int lvar) {
		super(opcode);
		this.lvar = lvar;
	}

	public InstructionLV(short opcode) {
		super(opcode);
	}

	public void dump(DataOutputStream out) throws IOException {
		if (lvar == -1) {
			out.writeByte(opcode);
		} else {
			if (lvar < 4) {
				if (opcode == ALOAD) {
					out.writeByte(ALOAD_0 + lvar);
				} else if (opcode == ASTORE) {
					out.writeByte(ASTORE_0 + lvar);
				} else if (opcode == ILOAD) {
					out.writeByte(ILOAD_0 + lvar);
				} else if (opcode == ISTORE) {
					out.writeByte(ISTORE_0 + lvar);
				} else if (opcode == DLOAD) {
					out.writeByte(DLOAD_0 + lvar);
				} else if (opcode == DSTORE) {
					out.writeByte(DSTORE_0 + lvar);
				} else if (opcode == FLOAD) {
					out.writeByte(FLOAD_0 + lvar);
				} else if (opcode == FSTORE) {
					out.writeByte(FSTORE_0 + lvar);
				} else if (opcode == LLOAD) {
					out.writeByte(LLOAD_0 + lvar);
				} else if (opcode == LSTORE) {
					out.writeByte(LSTORE_0 + lvar);
				} else {
					if (wide()) {
						out.writeByte(Constants.WIDE);
					}
					out.writeByte(opcode);
					if (wide()) {
						out.writeShort(lvar);
					} else {
						out.writeByte(lvar);
					}
				}
			} else {
				if (wide()) {
					out.writeByte(Constants.WIDE);
				}
				out.writeByte(opcode);
				if (wide()) {
					out.writeShort(lvar);
				} else {
					out.writeByte(lvar);
				}
			}
		}
	}

	/**
	 * Long output format:
	 * 
	 * 'name of opcode' "[" 'opcode number' "]" "(" 'length of instruction' ")" "<" 'local variable index' ">"
	 */
	public String toString(boolean verbose) {
		if (opcode >= Constants.ILOAD_0 && opcode <= Constants.ALOAD_3 || opcode >= Constants.ISTORE_0
				&& opcode <= Constants.ASTORE_3) {
			return super.toString(verbose);
		} else {
			return super.toString(verbose) + (lvar != -1 && lvar < 4 ? "_" : " ") + lvar;
		}
	}

	public boolean isALOAD() {
		return opcode == ALOAD || opcode >= ALOAD_0 && opcode <= ALOAD_3;
	}

	public boolean isASTORE() {
		return opcode == ASTORE || opcode >= ASTORE_0 && opcode <= ASTORE_3;
	}

	public int getBaseOpcode() {
		if (opcode >= ILOAD && opcode <= ALOAD || opcode >= ISTORE && opcode <= ASTORE) {
			// not an optimized instruction
			return opcode;
		}
		if (opcode >= Constants.ILOAD_0 && opcode <= Constants.ALOAD_3) {
			int ret = opcode - ILOAD_0;
			ret = ret - ret % 4;
			ret = ret / 4;
			return ret + ILOAD;
		}
		int ret = opcode - ISTORE_0;
		ret = ret - ret % 4;
		ret = ret / 4;
		return ret + ISTORE;
	}

	/**
	 * @return local variable index referred by this instruction.
	 */
	// optimize!
	public final int getIndex() {
		if (lvar != -1) {
			return lvar;
		}
		if (opcode >= Constants.ILOAD_0 && opcode <= Constants.ALOAD_3) {
			return (opcode - Constants.ILOAD_0) % 4;
		} else if (opcode >= Constants.ISTORE_0 && opcode <= Constants.ASTORE_3) {
			return (opcode - Constants.ISTORE_0) % 4;
		}
		return -1;
	}

	public void setIndex(int i) {
		// Switching the index for a load/store without a current index specified (ie. an aload_1 or istore_2)
		// means we need to should adjust to a normal aload/istore opcode
		if (getIndex() != i) {
			if (opcode >= Constants.ILOAD_0 && opcode <= Constants.ALOAD_3) {
				opcode = (short) (ILOAD + (opcode - ILOAD_0) / 4);
			} else if (opcode >= Constants.ISTORE_0 && opcode <= Constants.ASTORE_3) {
				opcode = (short) (ISTORE + (opcode - ISTORE_0) / 4);
			}
			this.lvar = i;
		}
	}

	public boolean canSetIndex() {
		return true;
	}

	public InstructionLV setIndexAndCopyIfNecessary(int newIndex) {
		if (canSetIndex()) {
			setIndex(newIndex);
			return this;
		} else {
			if (getIndex() == newIndex) {
				return this;
			}
			InstructionLV newInstruction = null;
			int baseOpCode = getBaseOpcode();
			if (newIndex < 4) {
				if (isStoreInstruction()) {
					newInstruction = (InstructionLV) InstructionConstants.INSTRUCTIONS[(baseOpCode - Constants.ISTORE) * 4
							+ Constants.ISTORE_0 + newIndex];
				} else {
					newInstruction = (InstructionLV) InstructionConstants.INSTRUCTIONS[(baseOpCode - Constants.ILOAD) * 4
							+ Constants.ILOAD_0 + newIndex];
				}
			} else {
				newInstruction = new InstructionLV((short) baseOpCode, newIndex);
			}
			// if (getBaseOpcode()!=newInstruction.getBaseOpcode() || newInstruction.getIndex()!=newIndex) {
			// throw new
			// RuntimeException("New Instruction created does not appear to be valid: originalBaseOpcode="+getBaseOpcode()+
			// " newBaseOpcode="+newInstruction.getBaseOpcode());
			// }
			return newInstruction;
		}
	}

	public int getLength() {
		int size = Constants.iLen[opcode];
		if (lvar == -1) {
			return size;
		} else {
			if (lvar < 4) {
				if (opcode == ALOAD || opcode == ASTORE) {
					return 1;
				} else if (opcode == ILOAD || opcode == ISTORE) {
					return 1;
				} else if (opcode == DLOAD || opcode == DSTORE) {
					return 1;
				} else if (opcode == FLOAD || opcode == FSTORE) {
					return 1;
				} else if (opcode == LLOAD || opcode == LSTORE) {
					return 1;
				} else {
					if (wide()) {
						return size + 2;
					}
					return size;
				}
			} else {
				if (wide()) {
					return size + 2;
				}
				return size;
			}
		}
	}

	private final boolean wide() {
		return lvar > Constants.MAX_BYTE;
	}

	public boolean equals(Object other) {
		if (!(other instanceof InstructionLV)) {
			return false;
		}
		InstructionLV o = (InstructionLV) other;
		return o.opcode == opcode && o.lvar == lvar;
	}

	public int hashCode() {
		return opcode * 37 + lvar;
	}

}
