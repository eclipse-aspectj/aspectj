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
import java.io.Serializable;

import org.aspectj.apache.bcel.Constants;
import org.aspectj.apache.bcel.classfile.ConstantPool;
import org.aspectj.apache.bcel.util.ByteSequence;

/** 
 * Abstract super class for all Java byte codes.
 *
 * @version $Id: Instruction.java,v 1.4 2004/11/22 08:31:27 aclement Exp $
 * @author  <A HREF="mailto:markus.dahm@berlin.de">M. Dahm</A>
 */
public abstract class Instruction implements Cloneable, Serializable {
  protected short length = 1;  // Length of instruction in bytes 
  protected short opcode = -1; // Opcode number

  private static InstructionComparator cmp = InstructionComparator.DEFAULT;

  /**
   * Empty constructor needed for the Class.newInstance() statement in
   * Instruction.readInstruction(). Not to be used otherwise.
   */
  Instruction() {}

  public Instruction(short opcode, short length) {
    this.length = length;
    this.opcode = opcode;
  }

  /**
   * Dump instruction as byte code to stream out.
   * @param out Output stream
   */
  public void dump(DataOutputStream out) throws IOException {
    out.writeByte(opcode); // Common for all instructions
  }

  /** @return name of instruction, i.e., opcode name
   */
  public String getName() {
    return Constants.OPCODE_NAMES[opcode];
  }

  /**
   * Long output format:
   *
   * &lt;name of opcode&gt; "["&lt;opcode number&gt;"]" 
   * "("&lt;length of instruction&gt;")"
   *
   * @param verbose long/short format switch
   * @return mnemonic for instruction
   */
  public String toString(boolean verbose) {
    if(verbose)
      return getName() + "[" + opcode + "](" + length + ")";
    else
      return getName();
  }

  /**
   * @return mnemonic for instruction in verbose format
   */
  public String toString() {
    return toString(true);
  }

  /**
   * @return mnemonic for instruction with sumbolic references resolved
   */
  public String toString(ConstantPool cp) {
    return toString(false);
  }

  /**
   * Use with caution, since `BranchInstruction's have a `target' reference which
   * is not copied correctly (only basic types are). This also applies for 
   * `Select' instructions with their multiple branch targets.
   *
   * @see BranchInstruction
   * @return (shallow) copy of an instruction
   */
  public Instruction copy() {
    Instruction i = null;

    // "Constant" instruction, no need to duplicate
    if(InstructionConstants.INSTRUCTIONS[this.getOpcode()] != null)
      i = this;
    else {
      try {
	i = (Instruction)clone();
      } catch(CloneNotSupportedException e) {
	System.err.println(e);
      }
    }

    return i;
  }
  
  /**
   * Read needed data (e.g. index) from file.
   *
   * @param bytes byte sequence to read from
   * @param wide "wide" instruction flag
   */
  protected void initFromFile(ByteSequence bytes, boolean wide)
    throws IOException
  {}  

  /**
   * Read an instruction from (byte code) input stream and return the
   * appropiate object.
   *
   * @param file file to read from
   * @return instruction object being read
   */
  public static final Instruction readInstruction(ByteSequence bytes)
    throws IOException
  {
    boolean     wide   = false;
    short       opcode = (short)bytes.readUnsignedByte();
    Instruction obj    = null;

    if(opcode == Constants.WIDE) { // Read next opcode after wide byte
      wide = true;
      opcode  = (short)bytes.readUnsignedByte();
    }

    if(InstructionConstants.INSTRUCTIONS[opcode] != null)
      return InstructionConstants.INSTRUCTIONS[opcode]; // Used predefined immutable object, if available

    /* Find appropiate class, instantiate an (empty) instruction object
     * and initialize it by hand.
     */
    Class clazz;

    try {
	switch(opcode) {
	case Constants.NOP:  obj = new NOP(); break;
	case Constants.ACONST_NULL:  obj = new ACONST_NULL(); break;
	case Constants.ICONST_M1: 
	case Constants.ICONST_0:  
	case Constants.ICONST_1:  
	case Constants.ICONST_2:  
	case Constants.ICONST_3:  
	case Constants.ICONST_4:  
	case Constants.ICONST_5:  obj = new ICONST(); break;
	case Constants.LCONST_0:  
	case Constants.LCONST_1:  obj = new LCONST(); break;
	case Constants.FCONST_0:  
	case Constants.FCONST_1:  
	case Constants.FCONST_2:  obj = new FCONST(); break;
	case Constants.DCONST_0:  
	case Constants.DCONST_1:  obj = new DCONST(); break;
	case Constants.BIPUSH  :  obj = new BIPUSH(); break;
	case Constants.SIPUSH  :  obj = new SIPUSH(); break;
	case Constants.LDC  :  obj = new LDC(); break;
	case Constants.LDC_W:  obj = new LDC_W(); break;
	case Constants.LDC2_W:  obj = new LDC2_W(); break;
	case Constants.ILOAD  :  obj = new ILOAD(); break;
	case Constants.LLOAD  :  obj = new LLOAD(); break;
	case Constants.FLOAD  :  obj = new FLOAD(); break;
	case Constants.DLOAD  :  obj = new DLOAD(); break;
	case Constants.ALOAD  :  obj = new ALOAD(); break;
	case Constants.ILOAD_0:  
	case Constants.ILOAD_1:  
	case Constants.ILOAD_2:  
	case Constants.ILOAD_3:  obj = new ILOAD(); break;
	case Constants.LLOAD_0:  
	case Constants.LLOAD_1:  
	case Constants.LLOAD_2:  
	case Constants.LLOAD_3:  obj = new LLOAD(); break;
	case Constants.FLOAD_0:  
	case Constants.FLOAD_1:  
	case Constants.FLOAD_2:  
	case Constants.FLOAD_3:  obj = new FLOAD(); break;
	case Constants.DLOAD_0:  
	case Constants.DLOAD_1:  
	case Constants.DLOAD_2:  
	case Constants.DLOAD_3:  obj = new DLOAD(); break;
	case Constants.ALOAD_0:  
	case Constants.ALOAD_1:  
	case Constants.ALOAD_2:  
	case Constants.ALOAD_3:  obj = new ALOAD(); break;
	case Constants.IALOAD  :  obj = new IALOAD(); break;
	case Constants.LALOAD  :  obj = new LALOAD(); break;
	case Constants.FALOAD  :  obj = new FALOAD(); break;
	case Constants.DALOAD  :  obj = new DALOAD(); break;
	case Constants.AALOAD  :  obj = new AALOAD(); break;
	case Constants.BALOAD  :  obj = new BALOAD(); break;
	case Constants.CALOAD  :  obj = new CALOAD(); break;
	case Constants.SALOAD  :  obj = new SALOAD(); break;
	case Constants.ISTORE  :  obj = new ISTORE(); break;
	case Constants.LSTORE  :  obj = new LSTORE(); break;
	case Constants.FSTORE  :  obj = new FSTORE(); break;
	case Constants.DSTORE  :  obj = new DSTORE(); break;
	case Constants.ASTORE  :  obj = new ASTORE(); break;
	case Constants.ISTORE_0:  
	case Constants.ISTORE_1:  
	case Constants.ISTORE_2:  
	case Constants.ISTORE_3:  obj = new ISTORE(); break;
	case Constants.LSTORE_0:  
	case Constants.LSTORE_1:  
	case Constants.LSTORE_2:  
	case Constants.LSTORE_3:  obj = new LSTORE(); break;
	case Constants.FSTORE_0:  
	case Constants.FSTORE_1:  
	case Constants.FSTORE_2:  
	case Constants.FSTORE_3:  obj = new FSTORE(); break;
	case Constants.DSTORE_0:  
	case Constants.DSTORE_1:  
	case Constants.DSTORE_2:  
	case Constants.DSTORE_3:  obj = new DSTORE(); break;
	case Constants.ASTORE_0:  
	case Constants.ASTORE_1:  
	case Constants.ASTORE_2:  
	case Constants.ASTORE_3:  obj = new ASTORE(); break;
	case Constants.IASTORE  :  obj = new IASTORE(); break;
	case Constants.LASTORE  :  obj = new LASTORE(); break;
	case Constants.FASTORE  :  obj = new FASTORE(); break;
	case Constants.DASTORE  :  obj = new DASTORE(); break;
	case Constants.AASTORE  :  obj = new AASTORE(); break;
	case Constants.BASTORE  :  obj = new BASTORE(); break;
	case Constants.CASTORE  :  obj = new CASTORE(); break;
	case Constants.SASTORE  :  obj = new SASTORE(); break;
	case Constants.POP  :  obj = new POP(); break;
	case Constants.POP2  :  obj = new POP2(); break;
	case Constants.DUP  :  obj = new DUP(); break;
	case Constants.DUP_X1:  obj = new DUP_X1(); break;
	case Constants.DUP_X2:  obj = new DUP_X2(); break;
	case Constants.DUP2  :  obj = new DUP2(); break;
	case Constants.DUP2_X1:  obj = new DUP2_X1(); break;
	case Constants.DUP2_X2:  obj = new DUP2_X2(); break;
	case Constants.SWAP  :  obj = new SWAP(); break;
	case Constants.IADD  :  obj = new IADD(); break;
	case Constants.LADD  :  obj = new LADD(); break;
	case Constants.FADD  :  obj = new FADD(); break;
	case Constants.DADD  :  obj = new DADD(); break;
	case Constants.ISUB  :  obj = new ISUB(); break;
	case Constants.LSUB  :  obj = new LSUB(); break;
	case Constants.FSUB  :  obj = new FSUB(); break;
	case Constants.DSUB  :  obj = new DSUB(); break;
	case Constants.IMUL  :  obj = new IMUL(); break;
	case Constants.LMUL  :  obj = new LMUL(); break;
	case Constants.FMUL  :  obj = new FMUL(); break;
	case Constants.DMUL  :  obj = new DMUL(); break;
	case Constants.IDIV  :  obj = new IDIV(); break;
	case Constants.LDIV  :  obj = new LDIV(); break;
	case Constants.FDIV  :  obj = new FDIV(); break;
	case Constants.DDIV  :  obj = new DDIV(); break;
	case Constants.IREM  :  obj = new IREM(); break;
	case Constants.LREM  :  obj = new LREM(); break;
	case Constants.FREM  :  obj = new FREM(); break;
	case Constants.DREM  :  obj = new DREM(); break;
	case Constants.INEG  :  obj = new INEG(); break;
	case Constants.LNEG  :  obj = new LNEG(); break;
	case Constants.FNEG  :  obj = new FNEG(); break;
	case Constants.DNEG  :  obj = new DNEG(); break;
	case Constants.ISHL  :  obj = new ISHL(); break;
	case Constants.LSHL  :  obj = new LSHL(); break;
	case Constants.ISHR  :  obj = new ISHR(); break;
	case Constants.LSHR  :  obj = new LSHR(); break;
	case Constants.IUSHR  :  obj = new IUSHR(); break;
	case Constants.LUSHR  :  obj = new LUSHR(); break;
	case Constants.IAND  :  obj = new IAND(); break;
	case Constants.LAND  :  obj = new LAND(); break;
	case Constants.IOR  :  obj = new IOR(); break;
	case Constants.LOR  :  obj = new LOR(); break;
	case Constants.IXOR  :  obj = new IXOR(); break;
	case Constants.LXOR  :  obj = new LXOR(); break;
	case Constants.IINC  :  obj = new IINC(); break;
	case Constants.I2L  :  obj = new I2L(); break;
	case Constants.I2F  :  obj = new I2F(); break;
	case Constants.I2D  :  obj = new I2D(); break;
	case Constants.L2I  :  obj = new L2I(); break;
	case Constants.L2F  :  obj = new L2F(); break;
	case Constants.L2D  :  obj = new L2D(); break;
	case Constants.F2I  :  obj = new F2I(); break;
	case Constants.F2L  :  obj = new F2L(); break;
	case Constants.F2D  :  obj = new F2D(); break;
	case Constants.D2I  :  obj = new D2I(); break;
	case Constants.D2L  :  obj = new D2L(); break;
	case Constants.D2F  :  obj = new D2F(); break;
	case Constants.I2B  :  obj = new I2B(); break;
	case Constants.I2C  :  obj = new I2C(); break;
	case Constants.I2S  :  obj = new I2S(); break;
	case Constants.LCMP  :  obj = new LCMP(); break;
	case Constants.FCMPL  :  obj = new FCMPL(); break;
	case Constants.FCMPG  :  obj = new FCMPG(); break;
	case Constants.DCMPL  :  obj = new DCMPL(); break;
	case Constants.DCMPG  :  obj = new DCMPG(); break;
	case Constants.IFEQ  :  obj = new IFEQ(); break;
	case Constants.IFNE  :  obj = new IFNE(); break;
	case Constants.IFLT  :  obj = new IFLT(); break;
	case Constants.IFGE  :  obj = new IFGE(); break;
	case Constants.IFGT  :  obj = new IFGT(); break;
	case Constants.IFLE  :  obj = new IFLE(); break;
	case Constants.IF_ICMPEQ:  obj = new IF_ICMPEQ(); break;
	case Constants.IF_ICMPNE:  obj = new IF_ICMPNE(); break;
	case Constants.IF_ICMPLT:  obj = new IF_ICMPLT(); break;
	case Constants.IF_ICMPGE:  obj = new IF_ICMPGE(); break;
	case Constants.IF_ICMPGT:  obj = new IF_ICMPGT(); break;
	case Constants.IF_ICMPLE:  obj = new IF_ICMPLE(); break;
	case Constants.IF_ACMPEQ:  obj = new IF_ACMPEQ(); break;
	case Constants.IF_ACMPNE        :  obj = new IF_ACMPNE(); break;
	case Constants.GOTO             :  obj = new GOTO(); break;
	case Constants.JSR              :  obj = new JSR(); break;
	case Constants.RET              :  obj = new RET(); break;
	case Constants.TABLESWITCH      :  obj = new TABLESWITCH(); break;
	case Constants.LOOKUPSWITCH     :  obj = new LOOKUPSWITCH(); break;
	case Constants.IRETURN          :  obj = new IRETURN(); break;
	case Constants.LRETURN          :  obj = new LRETURN(); break;
	case Constants.FRETURN          :  obj = new FRETURN(); break;
	case Constants.DRETURN          :  obj = new DRETURN(); break;
	case Constants.ARETURN          :  obj = new ARETURN(); break;
	case Constants.RETURN           :  obj = new RETURN(); break;
	case Constants.GETSTATIC        :  obj = new GETSTATIC(); break;
	case Constants.PUTSTATIC        :  obj = new PUTSTATIC(); break;
	case Constants.GETFIELD         :  obj = new GETFIELD(); break;
	case Constants.PUTFIELD         :  obj = new PUTFIELD(); break;
	case Constants.INVOKEVIRTUAL    :  obj = new INVOKEVIRTUAL(); break;
	case Constants.INVOKESPECIAL    :  obj = new INVOKESPECIAL(); break;
	case Constants.INVOKESTATIC     :  obj = new INVOKESTATIC(); break;
	case Constants.INVOKEINTERFACE  :  obj = new INVOKEINTERFACE(); break;
	case Constants.NEW              :  obj = new NEW(); break;
	case Constants.NEWARRAY         :  obj = new NEWARRAY(); break;
	case Constants.ANEWARRAY        :  obj = new ANEWARRAY(); break;
	case Constants.ARRAYLENGTH      :  obj = new ARRAYLENGTH(); break;
	case Constants.ATHROW           :  obj = new ATHROW(); break;
	case Constants.CHECKCAST        :  obj = new CHECKCAST(); break;
	case Constants.INSTANCEOF       :  obj = new INSTANCEOF(); break;
	case Constants.MONITORENTER     :  obj = new MONITORENTER(); break;
	case Constants.MONITOREXIT      :  obj = new MONITOREXIT(); break;
	case Constants.MULTIANEWARRAY   :  obj = new MULTIANEWARRAY(); break;
	case Constants.IFNULL           :  obj = new IFNULL(); break;
	case Constants.IFNONNULL        :  obj = new IFNONNULL(); break;
	case Constants.GOTO_W           :  obj = new GOTO_W(); break;
	case Constants.JSR_W            :  obj = new JSR_W(); break;
	default:
	    throw new ClassGenException("Illegal opcode detected");
	}
    } catch (ClassGenException e) {
	throw e;
    } catch (Exception e) {
	throw new ClassGenException(e.toString());
    }
      if(wide && !((obj instanceof LocalVariableInstruction) ||
		   (obj instanceof IINC) ||
		   (obj instanceof RET)))
	throw new ClassGenException("Illegal opcode after wide: " + opcode);

      obj.setOpcode(opcode);
      obj.initFromFile(bytes, wide); // Do further initializations, if any
      // Byte code offset set in InstructionList
    return obj;
  }

  private static final String className(short opcode) {
    String name = Constants.OPCODE_NAMES[opcode].toUpperCase();

    /* ICONST_0, etc. will be shortened to ICONST, etc., since ICONST_0 and the like
     * are not implemented (directly).
     */
    try {
      int  len = name.length();
      char ch1 = name.charAt(len - 2), ch2 = name.charAt(len - 1);

      if((ch1 == '_') && (ch2 >= '0')  && (ch2 <= '5'))
	name = name.substring(0, len - 2);
      
      if(name.equals("ICONST_M1")) // Special case
	name = "ICONST";
    } catch(StringIndexOutOfBoundsException e) { System.err.println(e); }

    return "org.aspectj.apache.bcel.generic." + name;
  }

  /**
   * This method also gives right results for instructions whose
   * effect on the stack depends on the constant pool entry they
   * reference.
   *  @return Number of words consumed from stack by this instruction,
   * or Constants.UNPREDICTABLE, if this can not be computed statically
   */
  public int consumeStack(ConstantPoolGen cpg) {
    return Constants.CONSUME_STACK[opcode];
  }

  /**
   * This method also gives right results for instructions whose
   * effect on the stack depends on the constant pool entry they
   * reference.
   * @return Number of words produced onto stack by this instruction,
   * or Constants.UNPREDICTABLE, if this can not be computed statically
   */
  public int produceStack(ConstantPoolGen cpg) {
    return Constants.PRODUCE_STACK[opcode];
  }

  /**
   * @return this instructions opcode
   */
  public short getOpcode()    { return opcode; }

  /**
   * @return length (in bytes) of instruction
   */
  public int getLength()   { return length; }

  /**
   * Needed in readInstruction.
   */
  private void setOpcode(short opcode) { this.opcode = opcode; }

  /** Some instructions may be reused, so don't do anything by default.
   */
  void dispose() {}

  /**
   * Call corresponding visitor method(s). The order is:
   * Call visitor methods of implemented interfaces first, then
   * call methods according to the class hierarchy in descending order,
   * i.e., the most specific visitXXX() call comes last.
   *
   * @param v Visitor object
   */
  public abstract void accept(Visitor v);

  /** Get Comparator object used in the equals() method to determine
   * equality of instructions.
   *
   * @return currently used comparator for equals()
   */
  public static InstructionComparator getComparator() { return cmp; }

  /** Set comparator to be used for equals().
   */
  public static void setComparator(InstructionComparator c) { cmp = c; }

  /** Check for equality, delegated to comparator
   * @return true if that is an Instruction and has the same opcode
   */
  public boolean equals(Object that) {
    return (that instanceof Instruction)?
      cmp.equals(this, (Instruction)that) : false;
  }
}
