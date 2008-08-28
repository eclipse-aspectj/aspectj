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
 *    notice, i list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, i list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "i product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, i acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache BCEL" must not be used to endorse or promote products
 *    derived from i software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from i software may not be called "Apache",
 *    "Apache BCEL", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * i SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF i SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * i software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */
package org.aspectj.apache.bcel.verifier;

import org.aspectj.apache.bcel.Constants;
import org.aspectj.apache.bcel.generic.FieldInstruction;
import org.aspectj.apache.bcel.generic.InstVisitor;
import org.aspectj.apache.bcel.generic.Instruction;
import org.aspectj.apache.bcel.generic.InstructionBranch;
import org.aspectj.apache.bcel.generic.InstructionLV;
import org.aspectj.apache.bcel.generic.InvokeInstruction;

/**
 * Traverse an instruction
 * 
 * @author Andy Clement
 */
public class InstructionWalker implements Constants {

	/**
	 * Call corresponding visitor method(s). The order is: Call visitor methods of implemented interfaces first, then call methods
	 * according to the class hierarchy in descending order, i.e., the most specific visitXXX() call comes last.
	 * 
	 * @param i the instruction to visit
	 * @param v Visitor object
	 */
	public static void accept(Instruction i, InstVisitor v) {
		switch (i.opcode) {
		case IMPDEP1:
			v.visitIMPDEP1(i);
			break;
		case IMPDEP2:
			v.visitIMPDEP2(i);
			break;
		case MONITORENTER:
			v.visitExceptionThrower(i);
			v.visitStackConsumer(i);
			v.visitMONITORENTER(i);
			break;
		case MONITOREXIT:
			v.visitExceptionThrower(i);
			v.visitStackConsumer(i);
			v.visitMONITOREXIT(i);
			break;
		case LCMP:
			v.visitTypedInstruction(i);
			v.visitStackProducer(i);
			v.visitStackConsumer(i);
			v.visitLCMP(i);
			break;
		case FCMPL:
			v.visitTypedInstruction(i);
			v.visitStackProducer(i);
			v.visitStackConsumer(i);
			v.visitFCMPL(i);
			break;
		case FCMPG:
			v.visitTypedInstruction(i);
			v.visitStackProducer(i);
			v.visitStackConsumer(i);
			v.visitFCMPG(i);
			break;
		case DCMPL:
			v.visitTypedInstruction(i);
			v.visitStackProducer(i);
			v.visitStackConsumer(i);
			v.visitDCMPL(i);
			break;
		case DCMPG:
			v.visitTypedInstruction(i);
			v.visitStackProducer(i);
			v.visitStackConsumer(i);
			v.visitDCMPG(i);
			break;
		case NOP:
			v.visitNOP(i);
			break;
		case BREAKPOINT:
			v.visitBREAKPOINT(i);
			break;
		case SWAP:
			v.visitStackConsumer(i);
			v.visitStackProducer(i);
			v.visitStackInstruction(i);
			v.visitSWAP(i);
			break;
		case POP:
			v.visitStackConsumer(i);
			v.visitPopInstruction(i);
			v.visitStackInstruction(i);
			v.visitPOP(i);
			break;
		case POP2:
			v.visitStackConsumer(i);
			v.visitPopInstruction(i);
			v.visitStackInstruction(i);
			v.visitPOP2(i);
			break;
		case DUP2_X1:
			v.visitStackInstruction(i);
			v.visitDUP2_X1(i);
			break;
		case DUP2_X2:
			v.visitStackInstruction(i);
			v.visitDUP2_X2(i);
			break;
		case DUP2:
			v.visitStackProducer(i);
			v.visitPushInstruction(i);
			v.visitStackInstruction(i);
			v.visitDUP2(i);
			break;
		case DUP_X1:
			v.visitStackInstruction(i);
			v.visitDUP_X1(i);
			break;
		case DUP_X2:
			v.visitStackInstruction(i);
			v.visitDUP_X2(i);
			break;
		case DUP:
			v.visitStackProducer(i);
			v.visitPushInstruction(i);
			v.visitStackInstruction(i);
			v.visitDUP(i);
			break;
		case BASTORE:
			v.visitStackConsumer(i);
			v.visitExceptionThrower(i);
			v.visitTypedInstruction(i);
			v.visitArrayInstruction(i);
			v.visitBASTORE(i);
			break;
		case CASTORE:
			v.visitStackConsumer(i);
			v.visitExceptionThrower(i);
			v.visitTypedInstruction(i);
			v.visitArrayInstruction(i);
			v.visitCASTORE(i);
			break;
		case SASTORE:
			v.visitStackConsumer(i);
			v.visitExceptionThrower(i);
			v.visitTypedInstruction(i);
			v.visitArrayInstruction(i);
			v.visitSASTORE(i);
			break;
		case DASTORE:
			v.visitStackConsumer(i);
			v.visitExceptionThrower(i);
			v.visitTypedInstruction(i);
			v.visitArrayInstruction(i);
			v.visitDASTORE(i);
			break;
		case FASTORE:
			v.visitStackConsumer(i);
			v.visitExceptionThrower(i);
			v.visitTypedInstruction(i);
			v.visitArrayInstruction(i);
			v.visitFASTORE(i);
			break;
		case LASTORE:
			v.visitStackConsumer(i);
			v.visitExceptionThrower(i);
			v.visitTypedInstruction(i);
			v.visitArrayInstruction(i);
			v.visitLASTORE(i);
			break;
		case IASTORE:
			v.visitStackConsumer(i);
			v.visitExceptionThrower(i);
			v.visitTypedInstruction(i);
			v.visitArrayInstruction(i);
			v.visitIASTORE(i);
			break;
		case AASTORE:
			v.visitStackConsumer(i);
			v.visitExceptionThrower(i);
			v.visitTypedInstruction(i);
			v.visitArrayInstruction(i);
			v.visitAASTORE(i);
			break;
		case SALOAD:
			v.visitStackProducer(i);
			v.visitExceptionThrower(i);
			v.visitTypedInstruction(i);
			v.visitArrayInstruction(i);
			v.visitSALOAD(i);
			break;
		case CALOAD:
			v.visitStackProducer(i);
			v.visitExceptionThrower(i);
			v.visitTypedInstruction(i);
			v.visitArrayInstruction(i);
			v.visitCALOAD(i);
			break;
		case DALOAD:
			v.visitStackProducer(i);
			v.visitExceptionThrower(i);
			v.visitTypedInstruction(i);
			v.visitArrayInstruction(i);
			v.visitDALOAD(i);
			break;
		case FALOAD:
			v.visitStackProducer(i);
			v.visitExceptionThrower(i);
			v.visitTypedInstruction(i);
			v.visitArrayInstruction(i);
			v.visitFALOAD(i);
			break;
		case LALOAD:
			v.visitStackProducer(i);
			v.visitExceptionThrower(i);
			v.visitTypedInstruction(i);
			v.visitArrayInstruction(i);
			v.visitLALOAD(i);
			break;
		case AALOAD:
			v.visitStackProducer(i);
			v.visitExceptionThrower(i);
			v.visitTypedInstruction(i);
			v.visitArrayInstruction(i);
			v.visitAALOAD(i);
			break;
		case ATHROW:
			v.visitUnconditionalBranch(i);
			v.visitExceptionThrower(i);
			v.visitATHROW(i);
			break;
		case ACONST_NULL:
			v.visitStackProducer(i);
			v.visitPushInstruction(i);
			v.visitTypedInstruction(i);
			v.visitACONST_NULL(i);
			break;
		case ICONST_M1:
		case ICONST_0:
		case ICONST_1:
		case ICONST_2:
		case ICONST_3:
		case ICONST_4:
		case ICONST_5:
			v.visitPushInstruction(i);
			v.visitStackProducer(i);
			v.visitTypedInstruction(i);
			v.visitConstantPushInstruction(i);
			v.visitICONST(i);
			break;
		case LCONST_0:
		case LCONST_1:
			v.visitPushInstruction(i);
			v.visitStackProducer(i);
			v.visitTypedInstruction(i);
			v.visitConstantPushInstruction(i);
			v.visitLCONST(i);
			break;
		case FCONST_0:
		case FCONST_1:
		case FCONST_2:
			v.visitPushInstruction(i);
			v.visitStackProducer(i);
			v.visitTypedInstruction(i);
			v.visitConstantPushInstruction(i);
			v.visitFCONST(i);
			break;
		case DCONST_0:
		case DCONST_1:
			v.visitPushInstruction(i);
			v.visitStackProducer(i);
			v.visitTypedInstruction(i);
			v.visitConstantPushInstruction(i);
			v.visitDCONST(i);
			break;
		case BALOAD:
			v.visitStackProducer(i);
			v.visitExceptionThrower(i);
			v.visitTypedInstruction(i);
			v.visitArrayInstruction(i);
			v.visitBALOAD(i);
			break;
		case IALOAD:
			v.visitStackProducer(i);
			v.visitExceptionThrower(i);
			v.visitTypedInstruction(i);
			v.visitArrayInstruction(i);
			v.visitIALOAD(i);
			break;
		case BIPUSH:
			v.visitPushInstruction(i);
			v.visitStackProducer(i);
			v.visitTypedInstruction(i);
			v.visitConstantPushInstruction(i);
			v.visitBIPUSH(i);
			break;
		case SIPUSH:
			v.visitPushInstruction(i);
			v.visitStackProducer(i);
			v.visitTypedInstruction(i);
			v.visitConstantPushInstruction(i);
			v.visitSIPUSH(i);
			break;
		case LDC:
		case LDC_W:
			v.visitStackProducer(i);
			v.visitPushInstruction(i);
			v.visitExceptionThrower(i);
			v.visitTypedInstruction(i);
			v.visitCPInstruction(i);
			v.visitLDC(i);
			break;
		case LDC2_W:
			v.visitStackProducer(i);
			v.visitPushInstruction(i);
			v.visitTypedInstruction(i);
			v.visitCPInstruction(i);
			v.visitLDC2_W(i);
			break;
		case ARRAYLENGTH:
			v.visitExceptionThrower(i);
			v.visitStackProducer(i);
			v.visitARRAYLENGTH(i);
			break;
		case ASTORE_0:
			v.visitStackConsumer(i);
			v.visitPopInstruction(i);
			v.visitStoreInstruction(i);
			v.visitTypedInstruction(i);
			v.visitLocalVariableInstruction((InstructionLV) i);
			v.visitStoreInstruction(i);
			v.visitASTORE(i);
			break;
		case ALOAD_0:
			v.visitStackConsumer(i);
			v.visitPopInstruction(i);
			v.visitStoreInstruction(i);
			v.visitTypedInstruction(i);
			v.visitLocalVariableInstruction((InstructionLV) i);
			v.visitStoreInstruction(i);
			v.visitALOAD(i);
			break;
		// for store instructions: ISTORE > ASTORE_3 - needs to visit the instruction too
		// v.visitStackConsumer(i);
		// v.visitPopInstruction(i);
		// v.visitStoreInstruction(i);
		// v.visitTypedInstruction(i);
		// v.visitLocalVariableInstruction(i);
		// v.visitStoreInstruction(i);
		// for load instructions: ILOAD > ALOAD_3 - needs to visit the instruction too
		// v.visitStackProducer(i);
		// v.visitPushInstruction(i);
		// v.visitTypedInstruction(i);
		// v.visitLocalVariableInstruction(i);
		// v.visitLoadInstruction(i);

		// for conversion instructions: (all 15 of them) - needs to visit conversion instruction too
		// v.visitTypedInstruction(i);
		// v.visitStackProducer(i);
		// v.visitStackConsumer(i);
		// v.visitConversionInstruction(i);

		// arithmetic instructions - need to visit the instructions too (iadd etc)
		// v.visitTypedInstruction(i);
		// v.visitStackProducer(i);
		// v.visitStackConsumer(i);
		// v.visitArithmeticInstruction(i);

		case INVOKESTATIC:
			v.visitExceptionThrower(i);
			v.visitTypedInstruction(i);
			v.visitStackConsumer(i);
			v.visitStackProducer(i);
			v.visitLoadClass(i);
			v.visitCPInstruction(i);
			v.visitFieldOrMethod(i);
			v.visitInvokeInstruction((InvokeInstruction) i);
			v.visitINVOKESTATIC((InvokeInstruction) i);
			break;

		case GOTO:
			v.visitVariableLengthInstruction(i);
			v.visitUnconditionalBranch(i);
			v.visitBranchInstruction((InstructionBranch) i);
			v.visitGotoInstruction(i);
			v.visitGOTO(i);
			break;
		case PUTSTATIC:
			v.visitExceptionThrower(i);
			v.visitStackConsumer(i);
			v.visitPopInstruction(i);
			v.visitTypedInstruction(i);
			v.visitLoadClass(i);
			v.visitCPInstruction(i);
			v.visitFieldOrMethod(i);
			v.visitFieldInstruction(i);
			v.visitPUTSTATIC((FieldInstruction) i);
			break;
		case RETURN:
			v.visitExceptionThrower(i);
			v.visitTypedInstruction(i);
			v.visitStackConsumer(i);
			v.visitReturnInstruction(i);
			v.visitRETURN(i);
			break;
		default:
			throw new IllegalStateException("visiting not yet implemented for " + i.getName().toUpperCase());
		}
	}
}
