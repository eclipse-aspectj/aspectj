package org.aspectj.apache.bcel.verifier.structurals;

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

import org.aspectj.apache.bcel.Constants;
import org.aspectj.apache.bcel.classfile.Constant;
import org.aspectj.apache.bcel.classfile.ConstantDouble;
import org.aspectj.apache.bcel.classfile.ConstantFloat;
import org.aspectj.apache.bcel.classfile.ConstantInteger;
import org.aspectj.apache.bcel.classfile.ConstantLong;
import org.aspectj.apache.bcel.classfile.ConstantString;
import org.aspectj.apache.bcel.classfile.ConstantPool;
import org.aspectj.apache.bcel.generic.ArrayType;
import org.aspectj.apache.bcel.generic.FieldInstruction;
import org.aspectj.apache.bcel.generic.IINC;
import org.aspectj.apache.bcel.generic.INVOKEINTERFACE;
import org.aspectj.apache.bcel.generic.Instruction;
import org.aspectj.apache.bcel.generic.InstructionBranch;
import org.aspectj.apache.bcel.generic.InstructionByte;
import org.aspectj.apache.bcel.generic.InvokeInstruction;
import org.aspectj.apache.bcel.generic.LOOKUPSWITCH;
import org.aspectj.apache.bcel.generic.MULTIANEWARRAY;
import org.aspectj.apache.bcel.generic.ObjectType;
import org.aspectj.apache.bcel.generic.RET;
import org.aspectj.apache.bcel.generic.ReturnaddressType;
import org.aspectj.apache.bcel.generic.TABLESWITCH;
import org.aspectj.apache.bcel.generic.Type;
import org.aspectj.apache.bcel.verifier.EmptyInstVisitor;

/**
 * This Visitor class may be used for a type-based Java Virtual Machine
 * simulation.
 * It does not check for correct types on the OperandStack or in the
 * LocalVariables; nor does it check their sizes are sufficiently big.
 * Thus, to use this Visitor for bytecode verifying, you have to make sure
 * externally that the type constraints of the Java Virtual Machine instructions
 * are satisfied. An InstConstraintVisitor may be used for this.
 * Anyway, this Visitor does not mandate it. For example, when you
 * visitIADD(IADD o), then there are two stack slots popped and one
 * stack slot containing a Type.INT is pushed (where you could also
 * pop only one slot if you know there are two Type.INT on top of the
 * stack). Monitor-specific behaviour is not simulated.
 * 
 * </P><B>Conventions:</B>
 *
 * Type.VOID will never be pushed onto the stack. Type.DOUBLE and Type.LONG
 * that would normally take up two stack slots (like Double_HIGH and
 * Double_LOW) are represented by a simple single Type.DOUBLE or Type.LONG
 * object on the stack here.
 * If a two-slot type is stored into a local variable, the next variable
 * is given the type Type.UNKNOWN.
 *
 * @version $Id: ExecutionVisitor.java,v 1.3 2008/08/28 00:02:13 aclement Exp $
 * @author <A HREF="http://www.inf.fu-berlin.de/~ehaase"/>Enver Haase</A>
 * @see #visitDSTORE(DSTORE o)
 * @see InstConstraintVisitor
 */
public class ExecutionVisitor extends EmptyInstVisitor {
 
	/**
	 * The executionframe we're operating on.
	 */
	private Frame frame = null;

	/**
	 * The ConstantPoolGen we're working with.
	 * @see #setConstantPoolGen(ConstantPoolGen)
	 */
	private ConstantPool cpg = null;

	/**
	 * Constructor. Constructs a new instance of this class.
	 */
	public ExecutionVisitor(){}

	/**
	 * The OperandStack from the current Frame we're operating on.
	 * @see #setFrame(Frame)
	 */
	private OperandStack stack(){
		return frame.getStack();
	}

	/**
	 * The LocalVariables from the current Frame we're operating on.
	 * @see #setFrame(Frame)
	 */
	private LocalVariables locals(){
		return frame.getLocals();
	}

	/**
	 * Sets the ConstantPoolGen needed for symbolic execution.
	 */
	public void setConstantPoolGen(ConstantPool cpg){
		this.cpg = cpg;
	}
	
	/**
	 * The only method granting access to the single instance of
	 * the ExecutionVisitor class. Before actively using this
	 * instance, <B>SET THE ConstantPoolGen FIRST</B>.
	 * @see #setConstantPoolGen(ConstantPoolGen)
	 */
	public void setFrame(Frame f){
		this.frame = f;
	}

	///** Symbolically executes the corresponding Java Virtual Machine instruction. */ 
	//public void visitWIDE(WIDE o){
	// The WIDE instruction is modelled as a flag
	// of the embedded instructions in BCEL.
	// Therefore BCEL checks for possible errors
	// when parsing in the .class file: We don't
	// have even the possibilty to care for WIDE
	// here.
	//}

	/** Symbolically executes the corresponding Java Virtual Machine instruction. */ 
	public void visitAALOAD(Instruction o){
		stack().pop();														// pop the index int
//System.out.print(stack().peek());
		Type t = stack().pop(); // Pop Array type
		if (t == Type.NULL){
			stack().push(Type.NULL);
		}	// Do nothing stackwise --- a NullPointerException is thrown at Run-Time
		else{
			ArrayType at = (ArrayType) t;	
			stack().push(at.getElementType());
		}
	}
	/** Symbolically executes the corresponding Java Virtual Machine instruction. */ 
	public void visitAASTORE(Instruction o){
		stack().pop();
		stack().pop();
		stack().pop();
	}
	/** Symbolically executes the corresponding Java Virtual Machine instruction. */ 
	public void visitACONST_NULL(Instruction o){
		stack().push(Type.NULL);
	}
	/** Symbolically executes the corresponding Java Virtual Machine instruction. */ 
	public void visitALOAD(Instruction o){
		stack().push(locals().get(o.getIndex()));
	}
	/** Symbolically executes the corresponding Java Virtual Machine instruction. */ 
	public void visitANEWARRAY(Instruction o){
		stack().pop(); //count
		stack().push( new ArrayType(o.getType(cpg), 1) );
	}
	/** Symbolically executes the corresponding Java Virtual Machine instruction. */ 
	public void visitARETURN(Instruction o){
		stack().pop();
	}
	/** Symbolically executes the corresponding Java Virtual Machine instruction. */ 
	public void visitARRAYLENGTH(Instruction o){
		stack().pop();
		stack().push(Type.INT);
	}

	/** Symbolically executes the corresponding Java Virtual Machine instruction. */ 
	public void visitASTORE(Instruction o){
		locals().set(o.getIndex(), stack().pop());
		//System.err.println("TODO-DEBUG:	set LV '"+o.getIndex()+"' to '"+locals().get(o.getIndex())+"'.");
	}

	/** Symbolically executes the corresponding Java Virtual Machine instruction. */ 
	public void visitATHROW(Instruction o){
		Type t = stack().pop();
		stack().clear();
		if (t.equals(Type.NULL))
			stack().push(Type.getType("Ljava/lang/NullPointerException;"));
		else
			stack().push(t);
	}

	/** Symbolically executes the corresponding Java Virtual Machine instruction. */ 
	public void visitBALOAD(Instruction o){
		stack().pop();
		stack().pop();
		stack().push(Type.INT);
	}

	/** Symbolically executes the corresponding Java Virtual Machine instruction. */ 
	public void visitBASTORE(Instruction o){
		stack().pop();
		stack().pop();
		stack().pop();
	}

	/** Symbolically executes the corresponding Java Virtual Machine instruction. */ 
	public void visitBIPUSH(Instruction o){
		stack().push(Type.INT);
	}

	/** Symbolically executes the corresponding Java Virtual Machine instruction. */ 
	public void visitCALOAD(Instruction o){
		stack().pop();
		stack().pop();
		stack().push(Type.INT);
	}
	/** Symbolically executes the corresponding Java Virtual Machine instruction. */ 
	public void visitCASTORE(Instruction o){
		stack().pop();
		stack().pop();
		stack().pop();
	}
	/** Symbolically executes the corresponding Java Virtual Machine instruction. */ 
	public void visitCHECKCAST(Instruction o){
		// It's possibly wrong to do so, but SUN's
		// ByteCode verifier seems to do (only) this, too.
		// TODO: One could use a sophisticated analysis here to check
		//       if a type cannot possibly be cated to another and by
		//       so doing predict the ClassCastException at run-time.
		stack().pop();
		stack().push(o.getType(cpg));
	}

	/** Symbolically executes the corresponding Java Virtual Machine instruction. */ 
	public void visitD2F(Instruction o){
		stack().pop();
		stack().push(Type.FLOAT);
	}
	/** Symbolically executes the corresponding Java Virtual Machine instruction. */ 
	public void visitD2I(Instruction o){
		stack().pop();
		stack().push(Type.INT);
	}
	/** Symbolically executes the corresponding Java Virtual Machine instruction. */ 
	public void visitD2L(Instruction o){
		stack().pop();
		stack().push(Type.LONG);
	}
	/** Symbolically executes the corresponding Java Virtual Machine instruction. */ 
	public void visitDADD(Instruction o){
		stack().pop();
		stack().pop();
		stack().push(Type.DOUBLE);
	}
	/** Symbolically executes the corresponding Java Virtual Machine instruction. */ 
	public void visitDALOAD(Instruction o){
		stack().pop();
		stack().pop();
		stack().push(Type.DOUBLE);
	}
	/** Symbolically executes the corresponding Java Virtual Machine instruction. */ 
	public void visitDASTORE(Instruction o){
		stack().pop();
		stack().pop();
		stack().pop();
	}
	/** Symbolically executes the corresponding Java Virtual Machine instruction. */ 
	public void visitDCMPG(Instruction o){
		stack().pop();
		stack().pop();
		stack().push(Type.INT);
	}
	/** Symbolically executes the corresponding Java Virtual Machine instruction. */ 
	public void visitDCMPL(Instruction o){
		stack().pop();
		stack().pop();
		stack().push(Type.INT);
	}
	/** Symbolically executes the corresponding Java Virtual Machine instruction. */ 
	public void visitDCONST(Instruction o){
		stack().push(Type.DOUBLE);
	}
	/** Symbolically executes the corresponding Java Virtual Machine instruction. */ 
	public void visitDDIV(Instruction o){
		stack().pop();
		stack().pop();
		stack().push(Type.DOUBLE);
	}
	/** Symbolically executes the corresponding Java Virtual Machine instruction. */ 
	public void visitDLOAD(Instruction o){
		stack().push(Type.DOUBLE);
	}
	/** Symbolically executes the corresponding Java Virtual Machine instruction. */ 
	public void visitDMUL(Instruction o){
		stack().pop();
		stack().pop();
		stack().push(Type.DOUBLE);
	}
	/** Symbolically executes the corresponding Java Virtual Machine instruction. */ 
	public void visitDNEG(Instruction o){
		stack().pop();
		stack().push(Type.DOUBLE);
	}
	/** Symbolically executes the corresponding Java Virtual Machine instruction. */ 
	public void visitDREM(Instruction o){
		stack().pop();
		stack().pop();
		stack().push(Type.DOUBLE);
	}
	/** Symbolically executes the corresponding Java Virtual Machine instruction. */ 
	public void visitDRETURN(Instruction o){
		stack().pop();
	}
	/** Symbolically executes the corresponding Java Virtual Machine instruction. */ 
	public void visitDSTORE(Instruction o){
		locals().set(o.getIndex(), stack().pop());
		locals().set(o.getIndex()+1, Type.UNKNOWN);
	}
	/** Symbolically executes the corresponding Java Virtual Machine instruction. */ 
	public void visitDSUB(Instruction o){
		stack().pop();
		stack().pop();
		stack().push(Type.DOUBLE);
	}
	/** Symbolically executes the corresponding Java Virtual Machine instruction. */ 
	public void visitDUP(Instruction o){
		Type t = stack().pop();
		stack().push(t);
		stack().push(t);
	}
	/** Symbolically executes the corresponding Java Virtual Machine instruction. */ 
	public void visitDUP_X1(Instruction o){
		Type w1 = stack().pop();
		Type w2 = stack().pop();
		stack().push(w1);
		stack().push(w2);
		stack().push(w1);
	}
	/** Symbolically executes the corresponding Java Virtual Machine instruction. */ 
	public void visitDUP_X2(Instruction o){
		Type w1 = stack().pop();
		Type w2 = stack().pop();
		if (w2.getSize() == 2){
			stack().push(w1);
			stack().push(w2);
			stack().push(w1);
		}
		else{
			Type w3 = stack().pop();
			stack().push(w1);
			stack().push(w3);
			stack().push(w2);
			stack().push(w1);
		}
	}
	/** Symbolically executes the corresponding Java Virtual Machine instruction. */ 
	public void visitDUP2(Instruction o){
		Type t = stack().pop();
		if (t.getSize() == 2){
			stack().push(t);
			stack().push(t);
		}
		else{ // t.getSize() is 1
			Type u = stack().pop();
			stack().push(u);
			stack().push(t);
			stack().push(u);
			stack().push(t);
		}
	}
	/** Symbolically executes the corresponding Java Virtual Machine instruction. */ 
	public void visitDUP2_X1(Instruction o){
		Type t = stack().pop();
		if (t.getSize() == 2){
			Type u = stack().pop();
			stack().push(t);
			stack().push(u);
			stack().push(t);
		}
		else{ //t.getSize() is1
			Type u = stack().pop();
			Type v = stack().pop();
			stack().push(u);
			stack().push(t);
			stack().push(v);
			stack().push(u);
			stack().push(t);
		}
	}
	/** Symbolically executes the corresponding Java Virtual Machine instruction. */ 
	public void visitDUP2_X2(Instruction o){
		Type t = stack().pop();
		if (t.getSize() == 2){
			Type u = stack().pop();
			if (u.getSize() == 2){
				stack().push(t);
				stack().push(u);
				stack().push(t);
			}else{
				Type v = stack().pop();
				stack().push(t);
				stack().push(v);
				stack().push(u);
				stack().push(t);
			}
		}
		else{ //t.getSize() is 1
			Type u = stack().pop();
			Type v = stack().pop();
			if (v.getSize() == 2){
				stack().push(u);
				stack().push(t);
				stack().push(v);
				stack().push(u);
				stack().push(t);
			}else{
				Type w = stack().pop();
				stack().push(u);
				stack().push(t);
				stack().push(w);
				stack().push(v);
				stack().push(u);
				stack().push(t);
			}
		}
	}
	/** Symbolically executes the corresponding Java Virtual Machine instruction. */ 
	public void visitF2D(Instruction o){
		stack().pop();
		stack().push(Type.DOUBLE);
	}
	/** Symbolically executes the corresponding Java Virtual Machine instruction. */ 
	public void visitF2I(Instruction o){
		stack().pop();
		stack().push(Type.INT);
	}
	/** Symbolically executes the corresponding Java Virtual Machine instruction. */ 
	public void visitF2L(Instruction o){
		stack().pop();
		stack().push(Type.LONG);
	}
	/** Symbolically executes the corresponding Java Virtual Machine instruction. */ 
	public void visitFADD(Instruction o){
		stack().pop();
		stack().pop();
		stack().push(Type.FLOAT);
	}
	/** Symbolically executes the corresponding Java Virtual Machine instruction. */ 
	public void visitFALOAD(Instruction o){
		stack().pop();
		stack().pop();
		stack().push(Type.FLOAT);
	}
	/** Symbolically executes the corresponding Java Virtual Machine instruction. */ 
	public void visitFASTORE(Instruction o){
		stack().pop();
		stack().pop();
		stack().pop();
	}
	/** Symbolically executes the corresponding Java Virtual Machine instruction. */ 
	public void visitFCMPG(Instruction o){
		stack().pop();
		stack().pop();
		stack().push(Type.INT);
	}
	/** Symbolically executes the corresponding Java Virtual Machine instruction. */ 
	public void visitFCMPL(Instruction o){
		stack().pop();
		stack().pop();
		stack().push(Type.INT);
	}
	/** Symbolically executes the corresponding Java Virtual Machine instruction. */ 
	public void visitFCONST(Instruction o){
		stack().push(Type.FLOAT);
	}
	/** Symbolically executes the corresponding Java Virtual Machine instruction. */ 
	public void visitFDIV(Instruction o){
		stack().pop();
		stack().pop();
		stack().push(Type.FLOAT);
	}
	/** Symbolically executes the corresponding Java Virtual Machine instruction. */ 
	public void visitFLOAD(Instruction o){
		stack().push(Type.FLOAT);
	}
	/** Symbolically executes the corresponding Java Virtual Machine instruction. */ 
	public void visitFMUL(Instruction o){
		stack().pop();
		stack().pop();
		stack().push(Type.FLOAT);
	}
	/** Symbolically executes the corresponding Java Virtual Machine instruction. */ 
	public void visitFNEG(Instruction o){
		stack().pop();
		stack().push(Type.FLOAT);
	}
	/** Symbolically executes the corresponding Java Virtual Machine instruction. */ 
	public void visitFREM(Instruction o){
		stack().pop();
		stack().pop();
		stack().push(Type.FLOAT);
	}
	/** Symbolically executes the corresponding Java Virtual Machine instruction. */ 
	public void visitFRETURN(Instruction o){
		stack().pop();
	}
	/** Symbolically executes the corresponding Java Virtual Machine instruction. */ 
	public void visitFSTORE(Instruction o){
		locals().set(o.getIndex(), stack().pop());
	}
	/** Symbolically executes the corresponding Java Virtual Machine instruction. */ 
	public void visitFSUB(Instruction o){
		stack().pop();
		stack().pop();
		stack().push(Type.FLOAT);
	}
	/** Symbolically executes the corresponding Java Virtual Machine instruction. */ 
	public void visitGETFIELD(FieldInstruction o){
		stack().pop();
		Type t = o.getFieldType(cpg);
		if (	t.equals(Type.BOOLEAN)	||
					t.equals(Type.CHAR)			||
					t.equals(Type.BYTE) 		||
					t.equals(Type.SHORT)		)
			t = Type.INT;
		stack().push(t);
	}
	/** Symbolically executes the corresponding Java Virtual Machine instruction. */ 
	public void visitGETSTATIC(FieldInstruction o){
		Type t = o.getFieldType(cpg);
		if (	t.equals(Type.BOOLEAN)	||
					t.equals(Type.CHAR)			||
					t.equals(Type.BYTE) 		||
					t.equals(Type.SHORT)		)
			t = Type.INT;
		stack().push(t);
	}
	/** Symbolically executes the corresponding Java Virtual Machine instruction. */ 
	public void visitGOTO(Instruction o){
		// no stack changes.
	}
	/** Symbolically executes the corresponding Java Virtual Machine instruction. */ 
	public void visitGOTO_W(Instruction o){
		// no stack changes.
	}
	/** Symbolically executes the corresponding Java Virtual Machine instruction. */ 
	public void visitI2B(Instruction o){
		stack().pop();
		stack().push(Type.INT);
	}
	/** Symbolically executes the corresponding Java Virtual Machine instruction. */ 
	public void visitI2C(Instruction o){
		stack().pop();
		stack().push(Type.INT);
	}
	/** Symbolically executes the corresponding Java Virtual Machine instruction. */ 
	public void visitI2D(Instruction o){
		stack().pop();
		stack().push(Type.DOUBLE);
	}
	/** Symbolically executes the corresponding Java Virtual Machine instruction. */ 
	public void visitI2F(Instruction o){
		stack().pop();
		stack().push(Type.FLOAT);
	}
	/** Symbolically executes the corresponding Java Virtual Machine instruction. */ 
	public void visitI2L(Instruction o){
		stack().pop();
		stack().push(Type.LONG);
	}
	/** Symbolically executes the corresponding Java Virtual Machine instruction. */ 
	public void visitI2S(Instruction o){
		stack().pop();
		stack().push(Type.INT);
	}
	/** Symbolically executes the corresponding Java Virtual Machine instruction. */ 
	public void visitIADD(Instruction o){
		stack().pop();
		stack().pop();
		stack().push(Type.INT);
	}
	/** Symbolically executes the corresponding Java Virtual Machine instruction. */ 
	public void visitIALOAD(Instruction o){
		stack().pop();
		stack().pop();
		stack().push(Type.INT);
	}
	/** Symbolically executes the corresponding Java Virtual Machine instruction. */ 
	public void visitIAND(Instruction o){
		stack().pop();
		stack().pop();
		stack().push(Type.INT);
	}
	/** Symbolically executes the corresponding Java Virtual Machine instruction. */ 
	public void visitIASTORE(Instruction o){
		stack().pop();
		stack().pop();
		stack().pop();
	}
	/** Symbolically executes the corresponding Java Virtual Machine instruction. */ 
	public void visitICONST(Instruction o){
		stack().push(Type.INT);
	}
	/** Symbolically executes the corresponding Java Virtual Machine instruction. */ 
	public void visitIDIV(Instruction o){
		stack().pop();
		stack().pop();
		stack().push(Type.INT);
	}
	/** Symbolically executes the corresponding Java Virtual Machine instruction. */ 
	public void visitIF_ACMPEQ(Instruction o){
		stack().pop();
		stack().pop();
	}
	/** Symbolically executes the corresponding Java Virtual Machine instruction. */ 
	public void visitIF_ACMPNE(Instruction o){
		stack().pop();
		stack().pop();
	}
	/** Symbolically executes the corresponding Java Virtual Machine instruction. */ 
	public void visitIF_ICMPEQ(Instruction o){
		stack().pop();
		stack().pop();
	}
	/** Symbolically executes the corresponding Java Virtual Machine instruction. */ 
	public void visitIF_ICMPGE(Instruction o){
		stack().pop();
		stack().pop();
	}
	/** Symbolically executes the corresponding Java Virtual Machine instruction. */ 
	public void visitIF_ICMPGT(Instruction o){
		stack().pop();
		stack().pop();
	}
	/** Symbolically executes the corresponding Java Virtual Machine instruction. */ 
	public void visitIF_ICMPLE(Instruction o){
		stack().pop();
		stack().pop();
	}
	/** Symbolically executes the corresponding Java Virtual Machine instruction. */ 
	public void visitIF_ICMPLT(Instruction o){
		stack().pop();
		stack().pop();
	}
	/** Symbolically executes the corresponding Java Virtual Machine instruction. */ 
	public void visitIF_ICMPNE(Instruction o){
		stack().pop();
		stack().pop();
	}
	/** Symbolically executes the corresponding Java Virtual Machine instruction. */ 
	public void visitIFEQ(Instruction o){
		stack().pop();
	}
	/** Symbolically executes the corresponding Java Virtual Machine instruction. */ 
	public void visitIFGE(Instruction o){
		stack().pop();
	}
	/** Symbolically executes the corresponding Java Virtual Machine instruction. */ 
	public void visitIFGT(Instruction o){
		stack().pop();
	}
	/** Symbolically executes the corresponding Java Virtual Machine instruction. */ 
	public void visitIFLE(Instruction o){
		stack().pop();
	}
	/** Symbolically executes the corresponding Java Virtual Machine instruction. */ 
	public void visitIFLT(Instruction o){
		stack().pop();
	}
	/** Symbolically executes the corresponding Java Virtual Machine instruction. */ 
	public void visitIFNE(Instruction o){
		stack().pop();
	}
	/** Symbolically executes the corresponding Java Virtual Machine instruction. */ 
	public void visitIFNONNULL(Instruction o){
		stack().pop();
	}
	/** Symbolically executes the corresponding Java Virtual Machine instruction. */ 
	public void visitIFNULL(Instruction o){
		stack().pop();
	}
	/** Symbolically executes the corresponding Java Virtual Machine instruction. */ 
	public void visitIINC(IINC o){
		// stack is not changed.
	}
	/** Symbolically executes the corresponding Java Virtual Machine instruction. */ 
	public void visitILOAD(Instruction o){
		stack().push(Type.INT);
	}
	/** Symbolically executes the corresponding Java Virtual Machine instruction. */ 
	public void visitIMUL(Instruction o){
		stack().pop();
		stack().pop();
		stack().push(Type.INT);
	}
	/** Symbolically executes the corresponding Java Virtual Machine instruction. */ 
	public void visitINEG(Instruction o){
		stack().pop();
		stack().push(Type.INT);
	}
	/** Symbolically executes the corresponding Java Virtual Machine instruction. */ 
	public void visitINSTANCEOF(Instruction o){
		stack().pop();
		stack().push(Type.INT);
	}
	/** Symbolically executes the corresponding Java Virtual Machine instruction. */ 
	public void visitINVOKEINTERFACE(INVOKEINTERFACE o){
		stack().pop();	//objectref
		for (int i=0; i<o.getArgumentTypes(cpg).length; i++){
			stack().pop();
		}
		// We are sure the invoked method will xRETURN eventually
		// We simulate xRETURNs functionality here because we
		// don't really "jump into" and simulate the invoked
		// method.
		if (o.getReturnType(cpg) != Type.VOID){
			Type t = o.getReturnType(cpg);
			if (	t.equals(Type.BOOLEAN)	||
						t.equals(Type.CHAR)			||
						t.equals(Type.BYTE) 		||
						t.equals(Type.SHORT)		)
				t = Type.INT;
			stack().push(t);
		}
	}
	/** Symbolically executes the corresponding Java Virtual Machine instruction. */ 
	public void visitINVOKESPECIAL(InvokeInstruction o){
		if (o.getMethodName(cpg).equals(Constants.CONSTRUCTOR_NAME)){
			UninitializedObjectType t = (UninitializedObjectType) stack().peek(o.getArgumentTypes(cpg).length);
			if (t == Frame._this){	
				Frame._this = null;
			}
			stack().initializeObject(t);
			locals().initializeObject(t);
		}
		stack().pop();	//objectref
		for (int i=0; i<o.getArgumentTypes(cpg).length; i++){
			stack().pop();
		}
		// We are sure the invoked method will xRETURN eventually
		// We simulate xRETURNs functionality here because we
		// don't really "jump into" and simulate the invoked
		// method.
		if (o.getReturnType(cpg) != Type.VOID){
			Type t = o.getReturnType(cpg);
			if (	t.equals(Type.BOOLEAN)	||
						t.equals(Type.CHAR)			||
						t.equals(Type.BYTE) 		||
						t.equals(Type.SHORT)		)
				t = Type.INT;
			stack().push(t);
		}
	}
	/** Symbolically executes the corresponding Java Virtual Machine instruction. */ 
	public void visitINVOKESTATIC(InvokeInstruction o){
		for (int i=0; i<o.getArgumentTypes(cpg).length; i++){
			stack().pop();
		}
		// We are sure the invoked method will xRETURN eventually
		// We simulate xRETURNs functionality here because we
		// don't really "jump into" and simulate the invoked
		// method.
		if (o.getReturnType(cpg) != Type.VOID){
			Type t = o.getReturnType(cpg);
			if (	t.equals(Type.BOOLEAN)	||
						t.equals(Type.CHAR)			||
						t.equals(Type.BYTE) 		||
						t.equals(Type.SHORT)		)
				t = Type.INT;
			stack().push(t);
		}
	}
	/** Symbolically executes the corresponding Java Virtual Machine instruction. */ 
	public void visitINVOKEVIRTUAL(InvokeInstruction o){
		stack().pop(); //objectref
		for (int i=0; i<o.getArgumentTypes(cpg).length; i++){
			stack().pop();
		}
		// We are sure the invoked method will xRETURN eventually
		// We simulate xRETURNs functionality here because we
		// don't really "jump into" and simulate the invoked
		// method.
		if (o.getReturnType(cpg) != Type.VOID){
			Type t = o.getReturnType(cpg);
			if (	t.equals(Type.BOOLEAN)	||
						t.equals(Type.CHAR)			||
						t.equals(Type.BYTE) 		||
						t.equals(Type.SHORT)		)
				t = Type.INT;
			stack().push(t);
		}
	}
	/** Symbolically executes the corresponding Java Virtual Machine instruction. */ 
	public void visitIOR(Instruction o){
		stack().pop();
		stack().pop();
		stack().push(Type.INT);
	}
	/** Symbolically executes the corresponding Java Virtual Machine instruction. */ 
	public void visitIREM(Instruction o){
		stack().pop();
		stack().pop();
		stack().push(Type.INT);
	}
	/** Symbolically executes the corresponding Java Virtual Machine instruction. */ 
	public void visitIRETURN(Instruction o){
		stack().pop();
	}
	/** Symbolically executes the corresponding Java Virtual Machine instruction. */ 
	public void visitISHL(Instruction o){
		stack().pop();
		stack().pop();
		stack().push(Type.INT);
	}
	/** Symbolically executes the corresponding Java Virtual Machine instruction. */ 
	public void visitISHR(Instruction o){
		stack().pop();
		stack().pop();
		stack().push(Type.INT);
	}
	/** Symbolically executes the corresponding Java Virtual Machine instruction. */ 
	public void visitISTORE(Instruction o){
		locals().set(o.getIndex(), stack().pop());
	}
	/** Symbolically executes the corresponding Java Virtual Machine instruction. */ 
	public void visitISUB(Instruction o){
		stack().pop();
		stack().pop();
		stack().push(Type.INT);
	}
	/** Symbolically executes the corresponding Java Virtual Machine instruction. */ 
	public void visitIUSHR(Instruction o){
		stack().pop();
		stack().pop();
		stack().push(Type.INT);
	}
	/** Symbolically executes the corresponding Java Virtual Machine instruction. */ 
	public void visitIXOR(Instruction o){
		stack().pop();
		stack().pop();
		stack().push(Type.INT);
	}

	/** Symbolically executes the corresponding Java Virtual Machine instruction. */ 
	public void visitJSR(InstructionBranch o){
		stack().push(new ReturnaddressType(o.physicalSuccessor()));
//System.err.println("TODO-----------:"+o.physicalSuccessor());
	}

	/** Symbolically executes the corresponding Java Virtual Machine instruction. */ 
	public void visitJSR_W(InstructionBranch o){
		stack().push(new ReturnaddressType(o.physicalSuccessor()));
	}

	/** Symbolically executes the corresponding Java Virtual Machine instruction. */ 
	public void visitL2D(Instruction o){
		stack().pop();
		stack().push(Type.DOUBLE);
	}
	/** Symbolically executes the corresponding Java Virtual Machine instruction. */ 
	public void visitL2F(Instruction o){
		stack().pop();
		stack().push(Type.FLOAT);
	}
	/** Symbolically executes the corresponding Java Virtual Machine instruction. */ 
	public void visitL2I(Instruction o){
		stack().pop();
		stack().push(Type.INT);
	}
	/** Symbolically executes the corresponding Java Virtual Machine instruction. */ 
	public void visitLADD(Instruction o){
		stack().pop();
		stack().pop();
		stack().push(Type.LONG);
	}
	/** Symbolically executes the corresponding Java Virtual Machine instruction. */ 
	public void visitLALOAD(Instruction o){
		stack().pop();
		stack().pop();
		stack().push(Type.LONG);
	}
	/** Symbolically executes the corresponding Java Virtual Machine instruction. */ 
	public void visitLAND(Instruction o){
		stack().pop();
		stack().pop();
		stack().push(Type.LONG);
	}
	/** Symbolically executes the corresponding Java Virtual Machine instruction. */ 
	public void visitLASTORE(Instruction o){
		stack().pop();
		stack().pop();
		stack().pop();
	}
	/** Symbolically executes the corresponding Java Virtual Machine instruction. */ 
	public void visitLCMP(Instruction o){
		stack().pop();
		stack().pop();
		stack().push(Type.INT);
	}
	/** Symbolically executes the corresponding Java Virtual Machine instruction. */ 
	public void visitLCONST(Instruction o){
		stack().push(Type.LONG);
	}
	/** Symbolically executes the corresponding Java Virtual Machine instruction. */ 
	public void visitLDC(Instruction o){
		Constant c = cpg.getConstant(o.getIndex());
		if (c instanceof ConstantInteger){
			stack().push(Type.INT);
		}
		if (c instanceof ConstantFloat){
			stack().push(Type.FLOAT);
		}
		if (c instanceof ConstantString){
			stack().push(Type.STRING);
		}
	}
	/** Symbolically executes the corresponding Java Virtual Machine instruction. */ 
	public void visitLDC_W(Instruction o){
		Constant c = cpg.getConstant(o.getIndex());
		if (c instanceof ConstantInteger){
			stack().push(Type.INT);
		}
		if (c instanceof ConstantFloat){
			stack().push(Type.FLOAT);
		}
		if (c instanceof ConstantString){
			stack().push(Type.STRING);
		}
	}
	/** Symbolically executes the corresponding Java Virtual Machine instruction. */ 
	public void visitLDC2_W(Instruction o){
		Constant c = cpg.getConstant(o.getIndex());
		if (c instanceof ConstantLong){
			stack().push(Type.LONG);
		}
		if (c instanceof ConstantDouble){
			stack().push(Type.DOUBLE);
		}
	}
	/** Symbolically executes the corresponding Java Virtual Machine instruction. */ 
	public void visitLDIV(Instruction o){
		stack().pop();
		stack().pop();
		stack().push(Type.LONG);
	}
	/** Symbolically executes the corresponding Java Virtual Machine instruction. */ 
	public void visitLLOAD(Instruction o){
		stack().push(locals().get(o.getIndex()));
	}
	/** Symbolically executes the corresponding Java Virtual Machine instruction. */ 
	public void visitLMUL(Instruction o){
		stack().pop();
		stack().pop();
		stack().push(Type.LONG);
	}
	/** Symbolically executes the corresponding Java Virtual Machine instruction. */ 
	public void visitLNEG(Instruction o){
		stack().pop();
		stack().push(Type.LONG);
	}
	/** Symbolically executes the corresponding Java Virtual Machine instruction. */ 
	public void visitLOOKUPSWITCH(LOOKUPSWITCH o){
		stack().pop(); //key
	}
	/** Symbolically executes the corresponding Java Virtual Machine instruction. */ 
	public void visitLOR(Instruction o){
		stack().pop();
		stack().pop();
		stack().push(Type.LONG);
	}
	/** Symbolically executes the corresponding Java Virtual Machine instruction. */ 
	public void visitLREM(Instruction o){
		stack().pop();
		stack().pop();
		stack().push(Type.LONG);
	}
	/** Symbolically executes the corresponding Java Virtual Machine instruction. */ 
	public void visitLRETURN(Instruction o){
		stack().pop();
	}
	/** Symbolically executes the corresponding Java Virtual Machine instruction. */ 
	public void visitLSHL(Instruction o){
		stack().pop();
		stack().pop();
		stack().push(Type.LONG);
	}
	/** Symbolically executes the corresponding Java Virtual Machine instruction. */ 
	public void visitLSHR(Instruction o){
		stack().pop();
		stack().pop();
		stack().push(Type.LONG);
	}
	/** Symbolically executes the corresponding Java Virtual Machine instruction. */ 
	public void visitLSTORE(Instruction o){
		locals().set(o.getIndex(), stack().pop());
		locals().set(o.getIndex()+1, Type.UNKNOWN);		
	}
	/** Symbolically executes the corresponding Java Virtual Machine instruction. */ 
	public void visitLSUB(Instruction o){
		stack().pop();
		stack().pop();
		stack().push(Type.LONG);
	}
	/** Symbolically executes the corresponding Java Virtual Machine instruction. */ 
	public void visitLUSHR(Instruction o){
		stack().pop();
		stack().pop();
		stack().push(Type.LONG);
	}
	/** Symbolically executes the corresponding Java Virtual Machine instruction. */ 
	public void visitLXOR(Instruction o){
		stack().pop();
		stack().pop();
		stack().push(Type.LONG);
	}
	/** Symbolically executes the corresponding Java Virtual Machine instruction. */ 
	public void visitMONITORENTER(Instruction o){
		stack().pop();
	}
	/** Symbolically executes the corresponding Java Virtual Machine instruction. */ 
	public void visitMONITOREXIT(Instruction o){
		stack().pop();
	}
	/** Symbolically executes the corresponding Java Virtual Machine instruction. */ 
	public void visitMULTIANEWARRAY(MULTIANEWARRAY o){
		for (int i=0; i<o.getDimensions(); i++){
			stack().pop();
		}
		stack().push(o.getType(cpg));
	}
	/** Symbolically executes the corresponding Java Virtual Machine instruction. */ 
	public void visitNEW(Instruction o){
		stack().push(new UninitializedObjectType((ObjectType) (o.getType(cpg))));
	}
	/** Symbolically executes the corresponding Java Virtual Machine instruction. */ 
	public void visitNEWARRAY(Instruction o){
		stack().pop();
		stack().push(((InstructionByte)o).getType());
	}
	/** Symbolically executes the corresponding Java Virtual Machine instruction. */ 
	public void visitNOP(Instruction o){
	}
	/** Symbolically executes the corresponding Java Virtual Machine instruction. */ 
	public void visitPOP(Instruction o){
		stack().pop();
	}
	/** Symbolically executes the corresponding Java Virtual Machine instruction. */ 
	public void visitPOP2(Instruction o){
		Type t = stack().pop();
		if (t.getSize() == 1){
			stack().pop();
		}		
	}
	/** Symbolically executes the corresponding Java Virtual Machine instruction. */ 
	public void visitPUTFIELD(FieldInstruction o){
		stack().pop();
		stack().pop();
	}
	/** Symbolically executes the corresponding Java Virtual Machine instruction. */ 
	public void visitPUTSTATIC(FieldInstruction o){
		stack().pop();
	}
	/** Symbolically executes the corresponding Java Virtual Machine instruction. */ 
	public void visitRET(RET o){
		// do nothing, return address
		// is in in the local variables.
	}
	/** Symbolically executes the corresponding Java Virtual Machine instruction. */ 
	public void visitRETURN(Instruction o){
		// do nothing.
	}
	/** Symbolically executes the corresponding Java Virtual Machine instruction. */ 
	public void visitSALOAD(Instruction o){
		stack().pop();
		stack().pop();
		stack().push(Type.INT);
	}
	/** Symbolically executes the corresponding Java Virtual Machine instruction. */ 
	public void visitSASTORE(Instruction o){
		stack().pop();
		stack().pop();
		stack().pop();
	}
	/** Symbolically executes the corresponding Java Virtual Machine instruction. */ 
	public void visitSIPUSH(Instruction o){
		stack().push(Type.INT);
	}
	/** Symbolically executes the corresponding Java Virtual Machine instruction. */ 
	public void visitSWAP(Instruction o){
		Type t = stack().pop();
		Type u = stack().pop();
		stack().push(t);
		stack().push(u);
	}
	/** Symbolically executes the corresponding Java Virtual Machine instruction. */ 
	public void visitTABLESWITCH(TABLESWITCH o){
		stack().pop();
	}
}
