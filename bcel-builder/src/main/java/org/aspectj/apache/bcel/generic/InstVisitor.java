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

/**
 * Interface implementing the Visitor pattern programming style.
 * I.e., a class that implements this interface can handle all types of
 * instructions with the properly typed methods just by calling the accept()
 * method.
 *
 * @version $Id: InstVisitor.java,v 1.2 2008/05/28 23:52:59 aclement Exp $
 * @author  <A HREF="mailto:markus.dahm@berlin.de">M. Dahm</A>
 */
public interface InstVisitor {
  void visitStackInstruction(Instruction obj);
  void visitLocalVariableInstruction(InstructionLV obj);
  void visitBranchInstruction(InstructionBranch obj);
  void visitLoadClass(Instruction obj);
  void visitFieldInstruction(Instruction obj);
  void visitIfInstruction(Instruction obj);
  void visitConversionInstruction(Instruction obj);
  void visitPopInstruction(Instruction obj);
  void visitStoreInstruction(Instruction obj);
  void visitTypedInstruction(Instruction obj);
  void visitSelect(InstructionSelect obj);
  void visitJsrInstruction(InstructionBranch obj);
  void visitGotoInstruction(Instruction obj);
  void visitUnconditionalBranch(Instruction obj);
  void visitPushInstruction(Instruction obj);
  void visitArithmeticInstruction(Instruction obj);
  void visitCPInstruction(Instruction obj);
  void visitInvokeInstruction(InvokeInstruction obj);
  void visitArrayInstruction(Instruction obj);
  void visitAllocationInstruction(Instruction obj);
  void visitReturnInstruction(Instruction obj);
  void visitFieldOrMethod(Instruction obj);
  void visitConstantPushInstruction(Instruction obj);
  void visitExceptionThrower(Instruction obj);
  void visitLoadInstruction(Instruction obj);
  void visitVariableLengthInstruction(Instruction obj);
  void visitStackProducer(Instruction obj);
  void visitStackConsumer(Instruction obj);
  void visitACONST_NULL(Instruction obj);
  void visitGETSTATIC(FieldInstruction obj);
  void visitIF_ICMPLT(Instruction obj);
  void visitMONITOREXIT(Instruction obj);
  void visitIFLT(Instruction obj);
  void visitLSTORE(Instruction obj);
  void visitPOP2(Instruction obj);
  void visitBASTORE(Instruction obj);
  void visitISTORE(Instruction obj);
  void visitCHECKCAST(Instruction obj);
  void visitFCMPG(Instruction obj);
  void visitI2F(Instruction obj);
  void visitATHROW(Instruction obj);
  void visitDCMPL(Instruction obj);
  void visitARRAYLENGTH(Instruction obj);
  void visitDUP(Instruction obj);
  void visitINVOKESTATIC(InvokeInstruction obj);
  void visitLCONST(Instruction obj);
  void visitDREM(Instruction obj);
  void visitIFGE(Instruction obj);
  void visitCALOAD(Instruction obj);
  void visitLASTORE(Instruction obj);
  void visitI2D(Instruction obj);
  void visitDADD(Instruction obj);
  void visitINVOKESPECIAL(InvokeInstruction obj);
  void visitIAND(Instruction obj);
  void visitPUTFIELD(FieldInstruction obj);
  void visitILOAD(Instruction obj);
  void visitDLOAD(Instruction obj);
  void visitDCONST(Instruction obj);
  void visitNEW(Instruction obj);
  void visitIFNULL(Instruction obj);
  void visitLSUB(Instruction obj);
  void visitL2I(Instruction obj);
  void visitISHR(Instruction obj);
  void visitTABLESWITCH(TABLESWITCH obj);
  void visitIINC(IINC obj);
  void visitDRETURN(Instruction obj);
  void visitFSTORE(Instruction obj);
  void visitDASTORE(Instruction obj);
  void visitIALOAD(Instruction obj);
  void visitDDIV(Instruction obj);
  void visitIF_ICMPGE(Instruction obj);
  void visitLAND(Instruction obj);
  void visitIDIV(Instruction obj);
  void visitLOR(Instruction obj);
  void visitCASTORE(Instruction obj);
  void visitFREM(Instruction obj);
  void visitLDC(Instruction obj);
  void visitBIPUSH(Instruction obj);
  void visitDSTORE(Instruction obj);
  void visitF2L(Instruction obj);
  void visitFMUL(Instruction obj);
  void visitLLOAD(Instruction obj);
  void visitJSR(InstructionBranch obj);
  void visitFSUB(Instruction obj);
  void visitSASTORE(Instruction obj);
  void visitALOAD(Instruction obj);
  void visitDUP2_X2(Instruction obj);
  void visitRETURN(Instruction obj);
  void visitDALOAD(Instruction obj);
  void visitSIPUSH(Instruction obj);
  void visitDSUB(Instruction obj);
  void visitL2F(Instruction obj);
  void visitIF_ICMPGT(Instruction obj);
  void visitF2D(Instruction obj);
  void visitI2L(Instruction obj);
  void visitIF_ACMPNE(Instruction obj);
  void visitPOP(Instruction obj);
  void visitI2S(Instruction obj);
  void visitIFEQ(Instruction obj);
  void visitSWAP(Instruction obj);
  void visitIOR(Instruction obj);
  void visitIREM(Instruction obj);
  void visitIASTORE(Instruction obj);
  void visitNEWARRAY(Instruction obj);
  void visitINVOKEINTERFACE(INVOKEINTERFACE obj);
  void visitINEG(Instruction obj);
  void visitLCMP(Instruction obj);
  void visitJSR_W(InstructionBranch obj);
  void visitMULTIANEWARRAY(MULTIANEWARRAY obj);
  void visitDUP_X2(Instruction obj);
  void visitSALOAD(Instruction obj);
  void visitIFNONNULL(Instruction obj);
  void visitDMUL(Instruction obj);
  void visitIFNE(Instruction obj);
  void visitIF_ICMPLE(Instruction obj);
  void visitLDC2_W(Instruction obj);
  void visitGETFIELD(FieldInstruction obj);
  void visitLADD(Instruction obj);
  void visitNOP(Instruction obj);
  void visitFALOAD(Instruction obj);
  void visitINSTANCEOF(Instruction obj);
  void visitIFLE(Instruction obj);
  void visitLXOR(Instruction obj);
  void visitLRETURN(Instruction obj);
  void visitFCONST(Instruction obj);
  void visitIUSHR(Instruction obj);
  void visitBALOAD(Instruction obj);
  void visitDUP2(Instruction obj);
  void visitIF_ACMPEQ(Instruction obj);
  void visitIMPDEP1(Instruction obj);
  void visitMONITORENTER(Instruction obj);
  void visitLSHL(Instruction obj);
  void visitDCMPG(Instruction obj);
  void visitD2L(Instruction obj);
  void visitIMPDEP2(Instruction obj);
  void visitL2D(Instruction obj);
  void visitRET(RET obj);
  void visitIFGT(Instruction obj);
  void visitIXOR(Instruction obj);
  void visitINVOKEVIRTUAL(InvokeInstruction obj);
  void visitFASTORE(Instruction obj);
  void visitIRETURN(Instruction obj);
  void visitIF_ICMPNE(Instruction obj);
  void visitFLOAD(Instruction obj);
  void visitLDIV(Instruction obj);
  void visitPUTSTATIC(FieldInstruction obj);
  void visitAALOAD(Instruction obj);
  void visitD2I(Instruction obj);
  void visitIF_ICMPEQ(Instruction obj);
  void visitAASTORE(Instruction obj);
  void visitARETURN(Instruction obj);
  void visitDUP2_X1(Instruction obj);
  void visitFNEG(Instruction obj);
  void visitGOTO_W(Instruction obj);
  void visitD2F(Instruction obj);
  void visitGOTO(Instruction obj);
  void visitISUB(Instruction obj);
  void visitF2I(Instruction obj);
  void visitDNEG(Instruction obj);
  void visitICONST(Instruction obj);
  void visitFDIV(Instruction obj);
  void visitI2B(Instruction obj);
  void visitLNEG(Instruction obj);
  void visitLREM(Instruction obj);
  void visitIMUL(Instruction obj);
  void visitIADD(Instruction obj);
  void visitLSHR(Instruction obj);
  void visitLOOKUPSWITCH(LOOKUPSWITCH obj);
  void visitDUP_X1(Instruction obj);
  void visitFCMPL(Instruction obj);
  void visitI2C(Instruction obj);
  void visitLMUL(Instruction obj);
  void visitLUSHR(Instruction obj);
  void visitISHL(Instruction obj);
  void visitLALOAD(Instruction obj);
  void visitASTORE(Instruction obj);
  void visitANEWARRAY(Instruction obj);
  void visitFRETURN(Instruction obj);
  void visitFADD(Instruction obj);
  void visitBREAKPOINT(Instruction obj);
}
