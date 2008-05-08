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
 * @version $Id: Instruction.java,v 1.4.10.3 2008/05/08 19:26:45 aclement Exp $
 * @author  <A HREF="mailto:markus.dahm@berlin.de">M. Dahm</A>
 */
public  class Instruction implements Cloneable, Serializable, Constants {
  public short opcode = -1;

  private static InstructionComparator cmp = InstructionComparator.DEFAULT;

  Instruction() {}

  public Instruction(short opcode) { this.opcode = opcode; }
  
  public void dump(DataOutputStream out) throws IOException { out.writeByte(opcode); }

  public String getName() { return Constants.OPCODE_NAMES[opcode]; }

  /**
   * Long output format:
   * 'name of opcode' "[" 'opcode number' "]" "(" 'length of instruction' ")"
   */
  public String toString(boolean verbose) {
    if (verbose) {
      StringBuffer sb = new StringBuffer();
      sb.append(getName()).append("[").append(opcode).append("](size").append(Constants.iLen[opcode]).append(")");
      return sb.toString();
    } else {
      return getName();
    }
  }

  public String toString() { return toString(true); }

  public String toString(ConstantPool cp) { return toString(false); }

  /**
   * Use with caution, since `BranchInstruction's have a `target' reference which
   * is not copied correctly (only basic types are). This also applies for 
   * `Select' instructions with their multiple branch targets.
   *
   * @see BranchInstruction
   * @return (shallow) copy of an instruction
   */
  final public Instruction copy() {
    if (InstructionConstants.INSTRUCTIONS[opcode] != null) { // immutable instructions do not need copying
      return this;
    } else {
      Instruction i = null; 
      try {//OPTIMIZE is clone the right thing to do here? it is horrible
    	  i = (Instruction)clone();
      } catch(CloneNotSupportedException e) {
    	  System.err.println(e);
      }
      return i;
    }
  }
  

  /**
   * Read an instruction from (byte code) input stream and return the
   * appropiate object.
   *
   * @param file file to read from
   * @return instruction object being read
   */
  public static final Instruction readInstruction(ByteSequence bytes) throws IOException {
    boolean     wide   = false;
    short       opcode = (short)bytes.readUnsignedByte();
    Instruction obj    = null;

    if (opcode == Constants.WIDE) {
      wide = true;
      opcode  = (short)bytes.readUnsignedByte();
    }

    Instruction constantInstruction = InstructionConstants.INSTRUCTIONS[opcode];
    if (constantInstruction != null) return constantInstruction;

    try {
	  switch (opcode) {
		case Constants.BIPUSH  :  obj = new InstructionByte(Constants.BIPUSH,bytes.readByte()); break;
		case Constants.SIPUSH  :  obj = new InstructionShort(Constants.SIPUSH,bytes.readShort()); break;
		case Constants.LDC     :  obj = new InstructionCP(Constants.LDC,bytes.readUnsignedByte()); break;
		case Constants.LDC_W   :  obj = new InstructionCP(Constants.LDC_W,bytes.readUnsignedShort()); break;
		case Constants.LDC2_W:    obj = new InstructionCP(Constants.LDC2_W,bytes.readUnsignedShort()); break;
		case Constants.ILOAD  :  obj = new InstructionLV(Constants.ILOAD,wide?bytes.readUnsignedShort():bytes.readUnsignedByte()); break;
		case Constants.LLOAD  :  obj = new InstructionLV(Constants.LLOAD,wide?bytes.readUnsignedShort():bytes.readUnsignedByte()); break;
		case Constants.FLOAD  :  obj = new InstructionLV(Constants.FLOAD,wide?bytes.readUnsignedShort():bytes.readUnsignedByte()); break;
		case Constants.DLOAD  :  obj = new InstructionLV(Constants.DLOAD,wide?bytes.readUnsignedShort():bytes.readUnsignedByte()); break;
		case Constants.ALOAD  :  obj = new InstructionLV(Constants.ALOAD,wide?bytes.readUnsignedShort():bytes.readUnsignedByte()); break;
		
		// move these to InstructionConstants
		case Constants.ILOAD_0:  obj = new InstructionLV(Constants.ILOAD_0);break;
		case Constants.ILOAD_1:  obj = new InstructionLV(Constants.ILOAD_1);break;
		case Constants.ILOAD_2:  obj = new InstructionLV(Constants.ILOAD_2);break;
		case Constants.ILOAD_3:  obj = new InstructionLV(Constants.ILOAD_3);break;
		case Constants.LLOAD_0:  obj = new InstructionLV(Constants.LLOAD_0);break;
		case Constants.LLOAD_1:  obj = new InstructionLV(Constants.LLOAD_1);break;
		case Constants.LLOAD_2:  obj = new InstructionLV(Constants.LLOAD_2);break;
		case Constants.LLOAD_3:  obj = new InstructionLV(Constants.LLOAD_3);break;
		case Constants.FLOAD_0:  obj = new InstructionLV(Constants.FLOAD_0);break;
		case Constants.FLOAD_1:  obj = new InstructionLV(Constants.FLOAD_1);break;
		case Constants.FLOAD_2:  obj = new InstructionLV(Constants.FLOAD_2);break;
		case Constants.FLOAD_3:  obj = new InstructionLV(Constants.FLOAD_3);break;
		case Constants.DLOAD_0:  obj = new InstructionLV(Constants.DLOAD_0);break;
		case Constants.DLOAD_1:  obj = new InstructionLV(Constants.DLOAD_1);break;
		case Constants.DLOAD_2:  obj = new InstructionLV(Constants.DLOAD_2);break;
		case Constants.DLOAD_3:  obj = new InstructionLV(Constants.DLOAD_3);break;
		case Constants.ALOAD_0:  obj = new InstructionLV(Constants.ALOAD_0);break;
		case Constants.ALOAD_1:  obj = new InstructionLV(Constants.ALOAD_1);break;
		case Constants.ALOAD_2:  obj = new InstructionLV(Constants.ALOAD_2);break;
		case Constants.ALOAD_3:  obj = new InstructionLV(Constants.ALOAD_3);break;

		// move to constants?
		case Constants.ISTORE_0:  obj = new InstructionLV(Constants.ISTORE_0);break;
		case Constants.ISTORE_1:  obj = new InstructionLV(Constants.ISTORE_1);break;
		case Constants.ISTORE_2:  obj = new InstructionLV(Constants.ISTORE_2);break;
		case Constants.ISTORE_3:  obj = new InstructionLV(Constants.ISTORE_3);break;
		case Constants.LSTORE_0:  obj = new InstructionLV(Constants.LSTORE_0);break;
		case Constants.LSTORE_1:  obj = new InstructionLV(Constants.LSTORE_1);break;
		case Constants.LSTORE_2:  obj = new InstructionLV(Constants.LSTORE_2);break;
		case Constants.LSTORE_3:  obj = new InstructionLV(Constants.LSTORE_3);break;
		case Constants.FSTORE_0:  obj = new InstructionLV(Constants.FSTORE_0);break;
		case Constants.FSTORE_1:  obj = new InstructionLV(Constants.FSTORE_1);break;
		case Constants.FSTORE_2:  obj = new InstructionLV(Constants.FSTORE_2);break;
		case Constants.FSTORE_3:  obj = new InstructionLV(Constants.FSTORE_3);break;
		case Constants.DSTORE_0:  obj = new InstructionLV(Constants.DSTORE_0);break;
		case Constants.DSTORE_1:  obj = new InstructionLV(Constants.DSTORE_1);break;
		case Constants.DSTORE_2:  obj = new InstructionLV(Constants.DSTORE_2);break;
		case Constants.DSTORE_3:  obj = new InstructionLV(Constants.DSTORE_3);break;
		case Constants.ASTORE_0:  obj = new InstructionLV(Constants.ASTORE_0);break;
		case Constants.ASTORE_1:  obj = new InstructionLV(Constants.ASTORE_1);break;
		case Constants.ASTORE_2:  obj = new InstructionLV(Constants.ASTORE_2);break;
		case Constants.ASTORE_3:  obj = new InstructionLV(Constants.ASTORE_3);break;	
//	case Constants.IALOAD  :  obj = new IALOAD(); break;
//		case Constants.LALOAD  :  obj = new LALOAD(); break;
//		case Constants.FALOAD  :  obj = new FALOAD(); break;
//		case Constants.DALOAD  :  obj = new DALOAD(); break;
//		case Constants.AALOAD  :  obj = new AALOAD(); break;
//		case Constants.BALOAD  :  obj = new BALOAD(); break;
//		case Constants.CALOAD  :  obj = new CALOAD(); break;
//		case Constants.SALOAD  :  obj = new SALOAD(); break;
	
	case Constants.ISTORE  :  obj = new InstructionLV(Constants.ISTORE,wide?bytes.readUnsignedShort():bytes.readUnsignedByte()); break;
	case Constants.LSTORE  :  obj = new InstructionLV(Constants.LSTORE,wide?bytes.readUnsignedShort():bytes.readUnsignedByte()); break;
	case Constants.FSTORE  :  obj = new InstructionLV(Constants.FSTORE,wide?bytes.readUnsignedShort():bytes.readUnsignedByte()); break;
	case Constants.DSTORE  :  obj = new InstructionLV(Constants.DSTORE,wide?bytes.readUnsignedShort():bytes.readUnsignedByte()); break;
	case Constants.ASTORE  :  obj = new InstructionLV(Constants.ASTORE,wide?bytes.readUnsignedShort():bytes.readUnsignedByte()); break;
	
	
//	case Constants.IASTORE  :  obj = new IASTORE(); break;
//	case Constants.LASTORE  :  obj = new LASTORE(); break;
//	case Constants.FASTORE  :  obj = new FASTORE(); break;
//	case Constants.DASTORE  :  obj = new DASTORE(); break;
//	case Constants.AASTORE  :  obj = new AASTORE(); break;
//	case Constants.BASTORE  :  obj = new BASTORE(); break;
//	case Constants.CASTORE  :  obj = new CASTORE(); break;
//	case Constants.SASTORE  :  obj = new SASTORE(); break;
	
//	case Constants.POP  :  obj = new POP(); break;
//	case Constants.POP2  :  obj = new POP2(); break;
//	case Constants.DUP  :  obj = new DUP(); break;
//	case Constants.DUP_X1:  obj = new DUP_X1(); break;
//	case Constants.DUP_X2:  obj = new DUP_X2(); break;
//	case Constants.DUP2  :  obj = new DUP2(); break;
//	case Constants.DUP2_X1:  obj = new DUP2_X1(); break;
//	case Constants.DUP2_X2:  obj = new DUP2_X2(); break;
//	case Constants.SWAP  :  obj = new SWAP(); break;
	
	case Constants.IINC  :  obj = new IINC(wide?bytes.readUnsignedShort():bytes.readUnsignedByte(),
											wide?bytes.readShort():bytes.readByte(),wide); break;
											
//	case Constants.LCMP  :  obj = new LCMP(); break;
//	case Constants.FCMPL  :  obj = new FCMPL(); break;
//	case Constants.FCMPG  :  obj = new FCMPG(); break;
//	case Constants.DCMPL  :  obj = new DCMPL(); break;
//	case Constants.DCMPG  :  obj = new DCMPG(); break;
//	case Constants.ARRAYLENGTH      :  obj = new Instruction(Constants.ARRAYLENGTH); break;
//	case Constants.ATHROW           :  obj = new ATHROW(); break;
//	case Constants.MONITORENTER     :  obj = new MONITORENTER(); break;
//	case Constants.MONITOREXIT      :  obj = new MONITOREXIT(); break;
	case Constants.IFNULL           :  obj = new InstructionBranch(Constants.IFNULL,bytes.readShort()); break;
	case Constants.IFNONNULL        :  obj = new InstructionBranch(Constants.IFNONNULL,bytes.readShort()); break;
	case Constants.IFEQ  :  obj = new InstructionBranch(Constants.IFEQ,bytes.readShort()); break;
	case Constants.IFNE  :  obj = new InstructionBranch(Constants.IFNE,bytes.readShort()); break;
	case Constants.IFLT  :  obj = new InstructionBranch(Constants.IFLT,bytes.readShort()); break;
	case Constants.IFGE  :  obj = new InstructionBranch(Constants.IFGE,bytes.readShort()); break;
	case Constants.IFGT  :  obj = new InstructionBranch(Constants.IFGT,bytes.readShort()); break;
	case Constants.IFLE  :  obj = new InstructionBranch(Constants.IFLE,bytes.readShort()); break;
	case Constants.IF_ICMPEQ:  obj = new InstructionBranch(Constants.IF_ICMPEQ,bytes.readShort()); break;
	case Constants.IF_ICMPNE:  obj = new InstructionBranch(Constants.IF_ICMPNE,bytes.readShort()); break;
	case Constants.IF_ICMPLT:  obj = new InstructionBranch(Constants.IF_ICMPLT,bytes.readShort()); break;
	case Constants.IF_ICMPGE:  obj = new InstructionBranch(Constants.IF_ICMPGE,bytes.readShort()); break;
	case Constants.IF_ICMPGT:  obj = new InstructionBranch(Constants.IF_ICMPGT,bytes.readShort()); break;
	case Constants.IF_ICMPLE:  obj = new InstructionBranch(Constants.IF_ICMPLE,bytes.readShort()); break;
	case Constants.IF_ACMPEQ:  obj = new InstructionBranch(Constants.IF_ACMPEQ,bytes.readShort()); break;
	case Constants.IF_ACMPNE        :  obj = new InstructionBranch(Constants.IF_ACMPNE,bytes.readShort()); break;
	case Constants.GOTO             :  obj = new InstructionBranch(Constants.GOTO,bytes.readShort()); break;
	case Constants.GOTO_W           :  obj = new InstructionBranch(Constants.GOTO_W,bytes.readInt()); break;
	case Constants.JSR              :  obj = new InstructionBranch(Constants.JSR,bytes.readShort()); break;
	case Constants.JSR_W            :  obj = new InstructionBranch(Constants.JSR_W,bytes.readInt()); break;

	case Constants.TABLESWITCH      :  obj = new TABLESWITCH(bytes); break;
	case Constants.LOOKUPSWITCH     :  obj = new LOOKUPSWITCH(bytes); break;
	
	case Constants.RET              :  obj = new RET(wide?bytes.readUnsignedShort():bytes.readUnsignedByte(),wide); break;

//	case Constants.IRETURN          :  obj = new IRETURN(); break;
//	case Constants.LRETURN          :  obj = new LRETURN(); break;
//	case Constants.FRETURN          :  obj = new FRETURN(); break;
//	case Constants.DRETURN          :  obj = new DRETURN(); break;
//	case Constants.ARETURN          :  obj = new ARETURN(); break;
//	case Constants.RETURN           :  obj = new RETURN(); break;
	case Constants.NEW              :  obj = new InstructionCP(Constants.NEW,bytes.readUnsignedShort()); break;
	
	case Constants.GETSTATIC        :  obj = new FieldInstruction(Constants.GETSTATIC,bytes.readUnsignedShort()); break;
	case Constants.PUTSTATIC        :  obj = new FieldInstruction(Constants.PUTSTATIC,bytes.readUnsignedShort()); break;
	case Constants.GETFIELD         :  obj = new FieldInstruction(Constants.GETFIELD,bytes.readUnsignedShort()); break;
	case Constants.PUTFIELD         :  obj = new FieldInstruction(Constants.PUTFIELD,bytes.readUnsignedShort()); break;
	case Constants.INVOKEVIRTUAL    :  obj = new InvokeInstruction(Constants.INVOKEVIRTUAL,bytes.readUnsignedShort()); break;
	case Constants.INVOKESPECIAL    :  obj = new InvokeInstruction(Constants.INVOKESPECIAL,bytes.readUnsignedShort()); break;
	case Constants.INVOKESTATIC     :  obj = new InvokeInstruction(Constants.INVOKESTATIC,bytes.readUnsignedShort()); break;
	case Constants.INVOKEINTERFACE  :  obj = new INVOKEINTERFACE(bytes.readUnsignedShort(),bytes.readUnsignedByte(),bytes.readByte());break;
	case Constants.NEWARRAY         :  obj = new InstructionByte(Constants.NEWARRAY,bytes.readByte()); break;
	case Constants.ANEWARRAY        :  obj = new InstructionCP(Constants.ANEWARRAY,bytes.readUnsignedShort()); break;
	case Constants.CHECKCAST        :  obj = new InstructionCP(Constants.CHECKCAST,bytes.readUnsignedShort()); break;
	case Constants.INSTANCEOF       :  obj = new InstructionCP(Constants.INSTANCEOF,bytes.readUnsignedShort()); break;
	case Constants.MULTIANEWARRAY   :  obj = new MULTIANEWARRAY(bytes.readUnsignedShort(),bytes.readByte()); break;
	default:
	    throw new ClassGenException("Illegal opcode detected");
	}
    } catch (ClassGenException e) {
    	throw e;
    } catch (Exception e) {
    	throw new ClassGenException(e.toString());
    }
      
    return obj;
  }

  /**
   * This method also gives right results for instructions whose
   * effect on the stack depends on the constant pool entry they
   * reference.
   *  @return Number of words consumed from stack by this instruction,
   * or Constants.UNPREDICTABLE, if this can not be computed statically
   */
  public int consumeStack(ConstantPool cpg) {
    return Constants.CONSUME_STACK[opcode];
  }

  /**
   * This method also gives right results for instructions whose
   * effect on the stack depends on the constant pool entry they
   * reference.
   * @return Number of words produced onto stack by this instruction,
   * or Constants.UNPREDICTABLE, if this can not be computed statically
   */
  public int produceStack(ConstantPool cpg) {
    return Constants.stackEntriesProduced[opcode];
  }

  /**
   * @return this instructions opcode
   */
  public short getOpcode()    { return opcode; }

  public int getLength()   { 
	  // if it is zero, it should have been provided by an overriding implementation of getLength()
	  int len = Constants.iLen[opcode];
	  if (len==0) throw new IllegalStateException("Length not right for "+getName().toUpperCase());
	  return len;
  }

  /** Some instructions may be reused, so don't do anything by default */
  void dispose() {}

  /**
   * Call corresponding visitor method(s). The order is:
   * Call visitor methods of implemented interfaces first, then
   * call methods according to the class hierarchy in descending order,
   * i.e., the most specific visitXXX() call comes last.
   *
   * @param v Visitor object
   */
  public void accept(InstVisitor v) {
	  switch (opcode) {
	  case IMPDEP1:v.visitIMPDEP1(this);break;
	  case IMPDEP2:v.visitIMPDEP2(this);break;
	  case MONITORENTER:
	    v.visitExceptionThrower(this);
	    v.visitStackConsumer(this);
	    v.visitMONITORENTER(this);
	    break;
	  case MONITOREXIT:
		    v.visitExceptionThrower(this);
		    v.visitStackConsumer(this);
		    v.visitMONITOREXIT(this);
		    break;
	  case LCMP:
		    v.visitTypedInstruction(this);
		    v.visitStackProducer(this);
		    v.visitStackConsumer(this);
		    v.visitLCMP(this);
		    break;
	  case FCMPL:
		    v.visitTypedInstruction(this);
		    v.visitStackProducer(this);
		    v.visitStackConsumer(this);
		    v.visitFCMPL(this);
		    break;
	  case FCMPG:
		    v.visitTypedInstruction(this);
		    v.visitStackProducer(this);
		    v.visitStackConsumer(this);
		    v.visitFCMPG(this);
		    break;
	  case DCMPL:
		    v.visitTypedInstruction(this);
		    v.visitStackProducer(this);
		    v.visitStackConsumer(this);
		    v.visitDCMPL(this);
		    break;
	  case DCMPG:
		    v.visitTypedInstruction(this);
		    v.visitStackProducer(this);
		    v.visitStackConsumer(this);
		    v.visitDCMPG(this);
		    break;
	    case NOP: 
	      v.visitNOP(this);
		  break;
	    case BREAKPOINT:
	        v.visitBREAKPOINT(this);
	        break;
	    case SWAP:
	        v.visitStackConsumer(this);
	        v.visitStackProducer(this);
	        v.visitStackInstruction(this);
	        v.visitSWAP(this);
	        break;
	    case POP:
	        v.visitStackConsumer(this);
	        v.visitPopInstruction(this);
	        v.visitStackInstruction(this);
	        v.visitPOP(this);
	        break;
	    case POP2:
	        v.visitStackConsumer(this);
	        v.visitPopInstruction(this);
	        v.visitStackInstruction(this);
	        v.visitPOP2(this);
	        break;
	    case DUP2_X1: 
		  v.visitStackInstruction(this);
		  v.visitDUP2_X1(this);
		  break;
	    case DUP2_X2: 
	      v.visitStackInstruction(this);
	      v.visitDUP2_X2(this);
	      break;
	    case DUP2:
	      v.visitStackProducer(this);
	      v.visitPushInstruction(this);
	      v.visitStackInstruction(this);
	      v.visitDUP2(this);
	      break;
	    case DUP_X1: 
		  v.visitStackInstruction(this);
		  v.visitDUP_X1(this);
		  break;
	    case DUP_X2: 
	      v.visitStackInstruction(this);
	      v.visitDUP_X2(this);
	      break;
	    case DUP:
	        v.visitStackProducer(this);
	        v.visitPushInstruction(this);
	        v.visitStackInstruction(this);
	        v.visitDUP(this);
	        break;
	    case BASTORE:
	        v.visitStackConsumer(this);
	        v.visitExceptionThrower(this);
	        v.visitTypedInstruction(this);
	        v.visitArrayInstruction(this);
	        v.visitBASTORE(this);
	        break;
	    case CASTORE:
	        v.visitStackConsumer(this);
	        v.visitExceptionThrower(this);
	        v.visitTypedInstruction(this);
	        v.visitArrayInstruction(this);
	        v.visitCASTORE(this);
	        break;
	    case SASTORE:
	        v.visitStackConsumer(this);
	        v.visitExceptionThrower(this);
	        v.visitTypedInstruction(this);
	        v.visitArrayInstruction(this);
	        v.visitSASTORE(this);
	        break;
	    case DASTORE:
	        v.visitStackConsumer(this);
	        v.visitExceptionThrower(this);
	        v.visitTypedInstruction(this);
	        v.visitArrayInstruction(this);
	        v.visitDASTORE(this);
	        break;
	    case FASTORE:
	        v.visitStackConsumer(this);
	        v.visitExceptionThrower(this);
	        v.visitTypedInstruction(this);
	        v.visitArrayInstruction(this);
	        v.visitFASTORE(this);
	        break;
	    case LASTORE:
	        v.visitStackConsumer(this);
	        v.visitExceptionThrower(this);
	        v.visitTypedInstruction(this);
	        v.visitArrayInstruction(this);
	        v.visitLASTORE(this);
	        break;
	    case IASTORE:
		    v.visitStackConsumer(this);
		    v.visitExceptionThrower(this);
		    v.visitTypedInstruction(this);
		    v.visitArrayInstruction(this);
		    v.visitIASTORE(this);
		    break;
	    case AASTORE:
	        v.visitStackConsumer(this);
	        v.visitExceptionThrower(this);
	        v.visitTypedInstruction(this);
	        v.visitArrayInstruction(this);
	        v.visitAASTORE(this);
	        break;
	    case SALOAD:
	        v.visitStackProducer(this);
	        v.visitExceptionThrower(this);
	        v.visitTypedInstruction(this);
	        v.visitArrayInstruction(this);
	        v.visitSALOAD(this);
	        break;
	    case CALOAD:
	        v.visitStackProducer(this);
	        v.visitExceptionThrower(this);
	        v.visitTypedInstruction(this);
	        v.visitArrayInstruction(this);
	        v.visitCALOAD(this);
	        break;
	    case DALOAD:
	        v.visitStackProducer(this);
	        v.visitExceptionThrower(this);
	        v.visitTypedInstruction(this);
	        v.visitArrayInstruction(this);
	        v.visitDALOAD(this);
	        break;
	    case FALOAD:
	        v.visitStackProducer(this);
	        v.visitExceptionThrower(this);
	        v.visitTypedInstruction(this);
	        v.visitArrayInstruction(this);
	        v.visitFALOAD(this);
	        break;
	    case LALOAD:
	        v.visitStackProducer(this);
	        v.visitExceptionThrower(this);
	        v.visitTypedInstruction(this);
	        v.visitArrayInstruction(this);
	        v.visitLALOAD(this);
	        break;
	    case AALOAD:
	        v.visitStackProducer(this);
	        v.visitExceptionThrower(this);
	        v.visitTypedInstruction(this);
	        v.visitArrayInstruction(this);
	        v.visitAALOAD(this);
	        break;
	    case ATHROW:
	        v.visitUnconditionalBranch(this);
	        v.visitExceptionThrower(this);
	        v.visitATHROW(this);
	        break;
	    case ACONST_NULL:
		    v.visitStackProducer(this);
		    v.visitPushInstruction(this);
		    v.visitTypedInstruction(this);
		    v.visitACONST_NULL(this);
		    break;
	    case ICONST_M1:case ICONST_0:case ICONST_1:case ICONST_2:case ICONST_3:case ICONST_4:case ICONST_5:
		    v.visitPushInstruction(this);
		    v.visitStackProducer(this);
		    v.visitTypedInstruction(this);
		    v.visitConstantPushInstruction(this);
		    v.visitICONST(this);
		    break;
	    case LCONST_0:case LCONST_1: 
	    	v.visitPushInstruction(this);
		    v.visitStackProducer(this);
		    v.visitTypedInstruction(this);
		    v.visitConstantPushInstruction(this);
		    v.visitLCONST(this);
		    break;
	    case FCONST_0:case FCONST_1:case FCONST_2:
		    v.visitPushInstruction(this);
		    v.visitStackProducer(this);
		    v.visitTypedInstruction(this);
		    v.visitConstantPushInstruction(this);
		    v.visitFCONST(this);
		    break;
	    case DCONST_0:case DCONST_1:
		    v.visitPushInstruction(this);
		    v.visitStackProducer(this);
		    v.visitTypedInstruction(this);
		    v.visitConstantPushInstruction(this);
		    v.visitDCONST(this);
	    case BALOAD:
	        v.visitStackProducer(this);
	        v.visitExceptionThrower(this);
	        v.visitTypedInstruction(this);
	        v.visitArrayInstruction(this);
	        v.visitBALOAD(this);
	        break;
	    case IALOAD:
	        v.visitStackProducer(this);
	        v.visitExceptionThrower(this);
	        v.visitTypedInstruction(this);
	        v.visitArrayInstruction(this);
	        v.visitIALOAD(this);
	    case BIPUSH:
	        v.visitPushInstruction(this);
	        v.visitStackProducer(this);
	        v.visitTypedInstruction(this);
	        v.visitConstantPushInstruction(this);
	        v.visitBIPUSH(this);
	        break;
	    case SIPUSH:
	        v.visitPushInstruction(this);
	        v.visitStackProducer(this);
	        v.visitTypedInstruction(this);
	        v.visitConstantPushInstruction(this);
	        v.visitSIPUSH(this);
	        break;
	    case LDC:case LDC_W:
	        v.visitStackProducer(this);
	        v.visitPushInstruction(this);
	        v.visitExceptionThrower(this);
	        v.visitTypedInstruction(this);
	        v.visitCPInstruction(this);
	        v.visitLDC(this);
	        break;
	    case LDC2_W:
	        v.visitStackProducer(this);
	        v.visitPushInstruction(this);
	        v.visitTypedInstruction(this);
	        v.visitCPInstruction(this);
	        v.visitLDC2_W(this);
	        break;
	    case ARRAYLENGTH:
	        v.visitExceptionThrower(this);
	        v.visitStackProducer(this);
	        v.visitARRAYLENGTH(this);
	        break;
	    case ASTORE_0:
	        v.visitStackConsumer(this);
	        v.visitPopInstruction(this);
	        v.visitStoreInstruction(this);
	        v.visitTypedInstruction(this);
	        v.visitLocalVariableInstruction((InstructionLV)this);
	        v.visitStoreInstruction(this);
	        v.visitASTORE(this);
	        break;
	    case ALOAD_0:
	        v.visitStackConsumer(this);
	        v.visitPopInstruction(this);
	        v.visitStoreInstruction(this);
	        v.visitTypedInstruction(this);
	        v.visitLocalVariableInstruction((InstructionLV)this);
	        v.visitStoreInstruction(this);
	        v.visitALOAD(this);
	        break;
	     // for store instructions: ISTORE > ASTORE_3 - needs to visit the instruction too
//	        v.visitStackConsumer(this);
//	        v.visitPopInstruction(this);
//	        v.visitStoreInstruction(this);
//	        v.visitTypedInstruction(this);
//	        v.visitLocalVariableInstruction(this);
//	        v.visitStoreInstruction(this);
		     // for load instructions: ILOAD > ALOAD_3 - needs to visit the instruction too
//	        v.visitStackProducer(this);
//	        v.visitPushInstruction(this);
//	        v.visitTypedInstruction(this);
//	        v.visitLocalVariableInstruction(this);
//	        v.visitLoadInstruction(this);
	        
	        // for conversion instructions: (all 15 of them) - needs to visit conversion instruction too
//	        v.visitTypedInstruction(this);
//	        v.visitStackProducer(this);
//	        v.visitStackConsumer(this);
//	        v.visitConversionInstruction(this);
	        
	        // arithmetic instructions - need to visit the instructions too (iadd etc)
//	        v.visitTypedInstruction(this);
//	        v.visitStackProducer(this);
//	        v.visitStackConsumer(this);
//	        v.visitArithmeticInstruction(this);

	    case INVOKESTATIC:
	        v.visitExceptionThrower(this);
	        v.visitTypedInstruction(this);
	        v.visitStackConsumer(this);
	        v.visitStackProducer(this);
	        v.visitLoadClass(this);
	        v.visitCPInstruction(this);
	        v.visitFieldOrMethod(this);
	        v.visitInvokeInstruction((InvokeInstruction)this);
	        v.visitINVOKESTATIC((InvokeInstruction)this);
	        break;
	        
	    case GOTO:
	        v.visitVariableLengthInstruction(this);
	        v.visitUnconditionalBranch(this);
	        v.visitBranchInstruction((InstructionBranch)this);
	        v.visitGotoInstruction(this);
	        v.visitGOTO(this);
	        break;
	    case PUTSTATIC:
	        v.visitExceptionThrower(this);
	        v.visitStackConsumer(this);
	        v.visitPopInstruction(this);
	        v.visitTypedInstruction(this);
	        v.visitLoadClass(this);
	        v.visitCPInstruction(this);
	        v.visitFieldOrMethod(this);
	        v.visitFieldInstruction(this);
	        v.visitPUTSTATIC((FieldInstruction)this);
	        break;
	    case RETURN:
	        v.visitExceptionThrower(this);
	        v.visitTypedInstruction(this);
	        v.visitStackConsumer(this);
	        v.visitReturnInstruction(this);
	        v.visitRETURN(this);
	        break;
		  default: throw new IllegalStateException("visiting not yet implemented for "+getName().toUpperCase());
	  }
  }

  /** Check for equality, delegated to comparator
   * @return true if that is an Instruction and has the same opcode
   */
  public boolean equals(Object that) {
    return (that instanceof Instruction)?
      cmp.equals(this, (Instruction)that) : false;
  }

  public Type getType() { return getType(null); }
  
  public Type getType(ConstantPool cp) {
	 //  if (types[opcode]==null) throw new RuntimeException(getName()+" is not a typed instruction");
	  Type t = Constants.types[opcode];
	  if (t!=null) return t;
	  switch (opcode) {	        
      case Constants.IRETURN: return Type.INT;
      case Constants.LRETURN: return Type.LONG;
      case Constants.FRETURN: return Type.FLOAT;
      case Constants.DRETURN: return Type.DOUBLE;
      case Constants.ARETURN: return Type.OBJECT;
      case Constants.RETURN:  return Type.VOID;
	  case LCMP: return Type.LONG;
	  case DCMPG: case DCMPL: return Type.DOUBLE;
	  case FCMPG: case FCMPL: return Type.FLOAT;
	    case ACONST_NULL: 
	    	return Type.NULL;
	    case ICONST_M1:case ICONST_0:case ICONST_1:case ICONST_2:case ICONST_3:case ICONST_4:case ICONST_5:
	    	return Type.INT;
        case IALOAD: case IASTORE: 
          return Type.INT;
        case CALOAD: case CASTORE: 
          return Type.CHAR;
        case BALOAD: case BASTORE:
          return Type.BYTE;
        case SALOAD: case SASTORE:
          return Type.SHORT;
        case LALOAD: case LASTORE: 
          return Type.LONG;
        case DALOAD: case DASTORE: 
          return Type.DOUBLE;
        case FALOAD: case FASTORE: 
          return Type.FLOAT;
        case AALOAD: case AASTORE:
          return Type.OBJECT;
          
        case ASTORE: 
        case ALOAD: 
        	return Type.OBJECT;
          
          case Constants.D2I: case Constants.F2I: case Constants.L2I:
            return Type.INT;   
          case Constants.D2F: case Constants.I2F: case Constants.L2F:
            return Type.FLOAT;
          case Constants.D2L: case Constants.F2L: case Constants.I2L:
            return Type.LONG;
          case Constants.F2D:  case Constants.I2D: case Constants.L2D:
              return Type.DOUBLE;
          case Constants.I2B:
            return Type.BYTE;
          case Constants.I2C:
            return Type.CHAR;
          case Constants.I2S:
            return Type.SHORT;

            // arithmetic instructions
          case Constants.IADD: case Constants.IAND: case Constants.IDIV: case Constants.IMUL: 
          case Constants.INEG: case Constants.IOR: case Constants.IREM: case Constants.ISHL: 
          case Constants.ISHR: case Constants.ISUB: case Constants.IUSHR: case Constants.IXOR:
            return Type.INT;
            
          case Constants.DADD: case Constants.DDIV: case Constants.DMUL: case Constants.DNEG: 
          case Constants.DREM: case Constants.DSUB:
            return Type.DOUBLE;

          case Constants.FADD: case Constants.FDIV: case Constants.FMUL:	            
          case Constants.FNEG: case Constants.FREM: case Constants.FSUB:
            return Type.FLOAT;


          case Constants.LADD: case Constants.LAND: case Constants.LDIV:
          case Constants.LMUL: case Constants.LNEG: case Constants.LOR: 
          case Constants.LREM: case Constants.LSHL: case Constants.LSHR:         	  
          case Constants.LSUB:case Constants.LUSHR: case Constants.LXOR:
            return Type.LONG;
	    default:
	    	throw new IllegalStateException("Not implemented yet for "+getName().toUpperCase());
	  }
  }
  
  public Number getValue() {
	  if ((instFlags[opcode]&CONSTANT_INST)==0) throw new RuntimeException(getName()+" is not a constant instruction");
	  switch (opcode) {
	    case ICONST_M1:case ICONST_0:case ICONST_1:case ICONST_2:case ICONST_3:case ICONST_4:case ICONST_5:
	    	return new Integer(opcode-ICONST_0);
	    default:
	    	throw new IllegalStateException("Not implemented yet for "+getName());
	  }
  }
  
  public int getIndex() { return -1; }
  public void setIndex(int i) {
	  throw new IllegalStateException("Shouldnt be asking "+getName().toUpperCase());
  }
  
  public Object getValue(ConstantPool cpg) {
	  throw new IllegalStateException("Shouldnt be asking "+getName().toUpperCase());
  }

	public boolean isLoadInstruction() {
		return (Constants.instFlags[opcode]&LOAD_INST)!=0;
	}

	public boolean isALOAD() {
		return false;
	}

	public boolean isStoreInstruction() {
		return (Constants.instFlags[opcode]&STORE_INST)!=0;
	}

	public boolean isASTORE() {
		return false;
	}
	public java.lang.Class[] getExceptions() {
		// fixme
		return Constants.instExcs[opcode];
	}

	public boolean containsTarget(InstructionHandle ih) { throw new IllegalStateException("Dont ask!!");}
	public void updateTarget(InstructionHandle old_ih, InstructionHandle new_ih) { throw new IllegalStateException("Dont ask!!");}

	public boolean isJsrInstruction() {
		return (Constants.instFlags[opcode]&JSR_INSTRUCTION)!=0;
	}

	public boolean isConstantInstruction() {
		return (Constants.instFlags[opcode]&CONSTANT_INST)!=0;
	}

	public boolean isConstantPoolInstruction() {
		return (Constants.instFlags[opcode]&CP_INST)!=0;
	}
	public boolean isStackProducer() {
		return (Constants.stackEntriesProduced[opcode]!=0);
//		return ((Constants.instFlags[opcode]&STACK_PRODUCER)!=0);
	}
	public boolean isStackConsumer() {
		return (Constants.CONSUME_STACK[opcode]!=0);
//		return ((Constants.instFlags[opcode]&STACK_CONSUMER)!=0);
	}

	public boolean isIndexedInstruction() {
		return (Constants.instFlags[opcode]&INDEXED)!=0;
	}
	public boolean isArrayCreationInstruction() {
		return (opcode==NEWARRAY || opcode==ANEWARRAY || opcode==MULTIANEWARRAY);
	}
	
	// Asserts a property of an instruction - can be commented out after a lot of testing ;)
	public void assertSomething(long flag) {
		if ((Constants.instFlags[opcode]&flag)==0) throw new IllegalStateException("This opcode "+opcode+" does not have the property "+Long.toHexString(flag));
	}
	
	public ObjectType getLoadClassType(ConstantPool cpg) {
		assertSomething(Constants.LOADCLASS_INST);
		Type t = getType(cpg);
		if (t instanceof ArrayType) t = ((ArrayType)t).getBasicType();
		return (t instanceof ObjectType)?(ObjectType)t:null;
	}
	
	public boolean isReturnInstruction() {
		return (Constants.instFlags[opcode]&RET_INST)!=0;
	}
	public boolean isGoto() {
		return opcode==GOTO || opcode==GOTO_W;
	}
	public boolean isLocalVariableInstruction() {
		return (Constants.instFlags[opcode]&LV_INST)!=0;
	}
}
