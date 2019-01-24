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
  public void visitStackInstruction(Instruction obj);
  public void visitLocalVariableInstruction(InstructionLV obj);
  public void visitBranchInstruction(InstructionBranch obj);
  public void visitLoadClass(Instruction obj);
  public void visitFieldInstruction(Instruction obj);
  public void visitIfInstruction(Instruction obj);
  public void visitConversionInstruction(Instruction obj);
  public void visitPopInstruction(Instruction obj);
  public void visitStoreInstruction(Instruction obj);
  public void visitTypedInstruction(Instruction obj);
  public void visitSelect(InstructionSelect obj);
  public void visitJsrInstruction(InstructionBranch obj);
  public void visitGotoInstruction(Instruction obj);
  public void visitUnconditionalBranch(Instruction obj);
  public void visitPushInstruction(Instruction obj);
  public void visitArithmeticInstruction(Instruction obj);
  public void visitCPInstruction(Instruction obj);
  public void visitInvokeInstruction(InvokeInstruction obj);
  public void visitArrayInstruction(Instruction obj);
  public void visitAllocationInstruction(Instruction obj);
  public void visitReturnInstruction(Instruction obj);
  public void visitFieldOrMethod(Instruction obj);
  public void visitConstantPushInstruction(Instruction obj);
  public void visitExceptionThrower(Instruction obj);
  public void visitLoadInstruction(Instruction obj);
  public void visitVariableLengthInstruction(Instruction obj);
  public void visitStackProducer(Instruction obj);
  public void visitStackConsumer(Instruction obj);
  public void visitACONST_NULL(Instruction obj);
  public void visitGETSTATIC(FieldInstruction obj);
  public void visitIF_ICMPLT(Instruction obj);
  public void visitMONITOREXIT(Instruction obj);
  public void visitIFLT(Instruction obj);
  public void visitLSTORE(Instruction obj);
  public void visitPOP2(Instruction obj);
  public void visitBASTORE(Instruction obj);
  public void visitISTORE(Instruction obj);
  public void visitCHECKCAST(Instruction obj);
  public void visitFCMPG(Instruction obj);
  public void visitI2F(Instruction obj);
  public void visitATHROW(Instruction obj);
  public void visitDCMPL(Instruction obj);
  public void visitARRAYLENGTH(Instruction obj);
  public void visitDUP(Instruction obj);
  public void visitINVOKESTATIC(InvokeInstruction obj);
  public void visitLCONST(Instruction obj);
  public void visitDREM(Instruction obj);
  public void visitIFGE(Instruction obj);
  public void visitCALOAD(Instruction obj);
  public void visitLASTORE(Instruction obj);
  public void visitI2D(Instruction obj);
  public void visitDADD(Instruction obj);
  public void visitINVOKESPECIAL(InvokeInstruction obj);
  public void visitIAND(Instruction obj);
  public void visitPUTFIELD(FieldInstruction obj);
  public void visitILOAD(Instruction obj);
  public void visitDLOAD(Instruction obj);
  public void visitDCONST(Instruction obj);
  public void visitNEW(Instruction obj);
  public void visitIFNULL(Instruction obj);
  public void visitLSUB(Instruction obj);
  public void visitL2I(Instruction obj);
  public void visitISHR(Instruction obj);
  public void visitTABLESWITCH(TABLESWITCH obj);
  public void visitIINC(IINC obj);
  public void visitDRETURN(Instruction obj);
  public void visitFSTORE(Instruction obj);
  public void visitDASTORE(Instruction obj);
  public void visitIALOAD(Instruction obj);
  public void visitDDIV(Instruction obj);
  public void visitIF_ICMPGE(Instruction obj);
  public void visitLAND(Instruction obj);
  public void visitIDIV(Instruction obj);
  public void visitLOR(Instruction obj);
  public void visitCASTORE(Instruction obj);
  public void visitFREM(Instruction obj);
  public void visitLDC(Instruction obj);
  public void visitBIPUSH(Instruction obj);
  public void visitDSTORE(Instruction obj);
  public void visitF2L(Instruction obj);
  public void visitFMUL(Instruction obj);
  public void visitLLOAD(Instruction obj);
  public void visitJSR(InstructionBranch obj);
  public void visitFSUB(Instruction obj);
  public void visitSASTORE(Instruction obj);
  public void visitALOAD(Instruction obj);
  public void visitDUP2_X2(Instruction obj);
  public void visitRETURN(Instruction obj);
  public void visitDALOAD(Instruction obj);
  public void visitSIPUSH(Instruction obj);
  public void visitDSUB(Instruction obj);
  public void visitL2F(Instruction obj);
  public void visitIF_ICMPGT(Instruction obj);
  public void visitF2D(Instruction obj);
  public void visitI2L(Instruction obj);
  public void visitIF_ACMPNE(Instruction obj);
  public void visitPOP(Instruction obj);
  public void visitI2S(Instruction obj);
  public void visitIFEQ(Instruction obj);
  public void visitSWAP(Instruction obj);
  public void visitIOR(Instruction obj);
  public void visitIREM(Instruction obj);
  public void visitIASTORE(Instruction obj);
  public void visitNEWARRAY(Instruction obj);
  public void visitINVOKEINTERFACE(INVOKEINTERFACE obj);
  public void visitINEG(Instruction obj);
  public void visitLCMP(Instruction obj);
  public void visitJSR_W(InstructionBranch obj);
  public void visitMULTIANEWARRAY(MULTIANEWARRAY obj);
  public void visitDUP_X2(Instruction obj);
  public void visitSALOAD(Instruction obj);
  public void visitIFNONNULL(Instruction obj);
  public void visitDMUL(Instruction obj);
  public void visitIFNE(Instruction obj);
  public void visitIF_ICMPLE(Instruction obj);
  public void visitLDC2_W(Instruction obj);
  public void visitGETFIELD(FieldInstruction obj);
  public void visitLADD(Instruction obj);
  public void visitNOP(Instruction obj);
  public void visitFALOAD(Instruction obj);
  public void visitINSTANCEOF(Instruction obj);
  public void visitIFLE(Instruction obj);
  public void visitLXOR(Instruction obj);
  public void visitLRETURN(Instruction obj);
  public void visitFCONST(Instruction obj);
  public void visitIUSHR(Instruction obj);
  public void visitBALOAD(Instruction obj);
  public void visitDUP2(Instruction obj);
  public void visitIF_ACMPEQ(Instruction obj);
  public void visitIMPDEP1(Instruction obj);
  public void visitMONITORENTER(Instruction obj);
  public void visitLSHL(Instruction obj);
  public void visitDCMPG(Instruction obj);
  public void visitD2L(Instruction obj);
  public void visitIMPDEP2(Instruction obj);
  public void visitL2D(Instruction obj);
  public void visitRET(RET obj);
  public void visitIFGT(Instruction obj);
  public void visitIXOR(Instruction obj);
  public void visitINVOKEVIRTUAL(InvokeInstruction obj);
  public void visitFASTORE(Instruction obj);
  public void visitIRETURN(Instruction obj);
  public void visitIF_ICMPNE(Instruction obj);
  public void visitFLOAD(Instruction obj);
  public void visitLDIV(Instruction obj);
  public void visitPUTSTATIC(FieldInstruction obj);
  public void visitAALOAD(Instruction obj);
  public void visitD2I(Instruction obj);
  public void visitIF_ICMPEQ(Instruction obj);
  public void visitAASTORE(Instruction obj);
  public void visitARETURN(Instruction obj);
  public void visitDUP2_X1(Instruction obj);
  public void visitFNEG(Instruction obj);
  public void visitGOTO_W(Instruction obj);
  public void visitD2F(Instruction obj);
  public void visitGOTO(Instruction obj);
  public void visitISUB(Instruction obj);
  public void visitF2I(Instruction obj);
  public void visitDNEG(Instruction obj);
  public void visitICONST(Instruction obj);
  public void visitFDIV(Instruction obj);
  public void visitI2B(Instruction obj);
  public void visitLNEG(Instruction obj);
  public void visitLREM(Instruction obj);
  public void visitIMUL(Instruction obj);
  public void visitIADD(Instruction obj);
  public void visitLSHR(Instruction obj);
  public void visitLOOKUPSWITCH(LOOKUPSWITCH obj);
  public void visitDUP_X1(Instruction obj);
  public void visitFCMPL(Instruction obj);
  public void visitI2C(Instruction obj);
  public void visitLMUL(Instruction obj);
  public void visitLUSHR(Instruction obj);
  public void visitISHL(Instruction obj);
  public void visitLALOAD(Instruction obj);
  public void visitASTORE(Instruction obj);
  public void visitANEWARRAY(Instruction obj);
  public void visitFRETURN(Instruction obj);
  public void visitFADD(Instruction obj);
  public void visitBREAKPOINT(Instruction obj);
}
