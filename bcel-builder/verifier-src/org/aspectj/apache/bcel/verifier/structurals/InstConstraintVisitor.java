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
import org.aspectj.apache.bcel.Repository;
import org.aspectj.apache.bcel.classfile.Constant;
import org.aspectj.apache.bcel.classfile.ConstantClass;
import org.aspectj.apache.bcel.classfile.ConstantDouble;
import org.aspectj.apache.bcel.classfile.ConstantInteger;
import org.aspectj.apache.bcel.classfile.ConstantFieldref;
import org.aspectj.apache.bcel.classfile.ConstantFloat;
import org.aspectj.apache.bcel.classfile.ConstantLong;
import org.aspectj.apache.bcel.classfile.ConstantString;
import org.aspectj.apache.bcel.classfile.ConstantPool;
import org.aspectj.apache.bcel.classfile.Field;
import org.aspectj.apache.bcel.classfile.JavaClass;
import org.aspectj.apache.bcel.verifier.EmptyInstVisitor;
import org.aspectj.apache.bcel.verifier.VerificationResult;
import org.aspectj.apache.bcel.verifier.Verifier;
import org.aspectj.apache.bcel.verifier.VerifierFactory;
import org.aspectj.apache.bcel.verifier.exc.*; 
import org.aspectj.apache.bcel.generic.ArrayType;
import org.aspectj.apache.bcel.generic.FieldInstruction;
import org.aspectj.apache.bcel.generic.IINC;
import org.aspectj.apache.bcel.generic.INVOKEINTERFACE;
import org.aspectj.apache.bcel.generic.Instruction;
import org.aspectj.apache.bcel.generic.InstructionBranch;
import org.aspectj.apache.bcel.generic.InstructionLV;
import org.aspectj.apache.bcel.generic.InvokeInstruction;
import org.aspectj.apache.bcel.generic.LOOKUPSWITCH;
import org.aspectj.apache.bcel.generic.MULTIANEWARRAY;
import org.aspectj.apache.bcel.generic.MethodGen;
import org.aspectj.apache.bcel.generic.ObjectType;
import org.aspectj.apache.bcel.generic.RET;
import org.aspectj.apache.bcel.generic.ReferenceType;
import org.aspectj.apache.bcel.generic.ReturnaddressType;
import org.aspectj.apache.bcel.generic.TABLESWITCH;
import org.aspectj.apache.bcel.generic.Type;


/**
 * A Visitor class testing for valid preconditions of JVM instructions.
 * The instance of this class will throw a StructuralCodeConstraintException
 * instance if an instruction is visitXXX()ed which has preconditions that are
 * not satisfied.
 * TODO: Currently, the JVM's behaviour concerning monitors (MONITORENTER,
 * MONITOREXIT) is not modeled in JustIce.
 *
 * @version $Id: InstConstraintVisitor.java,v 1.3 2008/08/28 00:02:13 aclement Exp $
 * @author <A HREF="http://www.inf.fu-berlin.de/~ehaase"/>Enver Haase</A>
 * @see org.aspectj.apache.bcel.verifier.exc.StructuralCodeConstraintException
 * @see org.aspectj.apache.bcel.verifier.exc.LinkingConstraintException
 */
public class InstConstraintVisitor extends EmptyInstVisitor {

	private static ObjectType GENERIC_ARRAY = new ObjectType("org.aspectj.apache.bcel.verifier.structurals.GenericArray");

	/**
	 * The constructor. Constructs a new instance of this class.
	 */
	public InstConstraintVisitor(){}

	/**
	 * The Execution Frame we're working on.
	 *
	 * @see #setFrame(Frame f)
	 * @see #locals()
	 * @see #stack()
	 */
	private Frame frame = null;

	private ConstantPool cpg = null;

	/**
	 * The MethodGen we're working on.
	 * 
	 * @see #setMethodGen(MethodGen mg)
	 */
	private MethodGen mg = null;

	/**
	 * The OperandStack we're working on.
	 *
	 * @see #setFrame(Frame f)
	 */
	private OperandStack stack(){
		return frame.getStack();
	}

	/**
	 * The LocalVariables we're working on.
	 *
	 * @see #setFrame(Frame f)
	 */
	private LocalVariables locals(){
		return frame.getLocals();
	}

	/**
   * This method is called by the visitXXX() to notify the acceptor of this InstConstraintVisitor
   * that a constraint violation has occured. This is done by throwing an instance of a
   * StructuralCodeConstraintException.
   * @throws org.aspectj.apache.bcel.verifier.exc.StructuralCodeConstraintException always.
   */
	private void constraintViolated(Instruction violator, String description){
		String fq_classname = violator.getClass().getName();
		throw new StructuralCodeConstraintException("Instruction "+ fq_classname.substring(fq_classname.lastIndexOf('.')+1) +" constraint violated: " + description);
	}

	/**
	 * This returns the single instance of the InstConstraintVisitor class.
	 * To operate correctly, other values must have been set before actually
	 * using the instance.
	 * Use this method for performance reasons.
	 *
	 * @see #setConstantPoolGen(ConstantPoolGen cpg)
	 * @see #setMethodGen(MethodGen mg)
	 */
	public void setFrame(Frame f){
		this.frame = f;
		//if (singleInstance.mg == null || singleInstance.cpg == null) throw new AssertionViolatedException("Forgot to set important values first.");
	}

	/**
	 * Sets the ConstantPoolGen instance needed for constraint
	 * checking prior to execution.
	 */	
	public void setConstantPoolGen(ConstantPool cpg){
		this.cpg = cpg;
	}

	/**
	 * Sets the MethodGen instance needed for constraint
	 * checking prior to execution.
	 */
	public void setMethodGen(MethodGen mg){
		this.mg = mg;
	}

	/**
	 * Assures index is of type INT.
	 * @throws org.aspectj.apache.bcel.verifier.exc.StructuralCodeConstraintException if the above constraint is not satisfied.
	 */
	private void indexOfInt(Instruction o, Type index){
		if (! index.equals(Type.INT))
				constraintViolated(o, "The 'index' is not of type int but of type "+index+".");
	}

	/**
	 * Assures the ReferenceType r is initialized (or Type.NULL).
	 * Formally, this means (!(r instanceof UninitializedObjectType)), because
	 * there are no uninitialized array types.
	 * @throws org.aspectj.apache.bcel.verifier.exc.StructuralCodeConstraintException if the above constraint is not satisfied.
	 */
	private void referenceTypeIsInitialized(Instruction o, ReferenceType r){
		if (r instanceof UninitializedObjectType){
			constraintViolated(o, "Working on an uninitialized object '"+r+"'.");
		}
	}

	/** Assures value is of type INT. */
	private void valueOfInt(Instruction o, Type value){
		if (! value.equals(Type.INT))
				constraintViolated(o, "The 'value' is not of type int but of type "+value+".");
	}

	/**
	 * Assures arrayref is of ArrayType or NULL;
	 * returns true if and only if arrayref is non-NULL.
	 * @throws org.aspectj.apache.bcel.verifier.exc.StructuralCodeConstraintException if the above constraint is violated.
 	 */
	private boolean arrayrefOfArrayType(Instruction o, Type arrayref){
		if (! ((arrayref instanceof ArrayType) || arrayref.equals(Type.NULL)) )
				constraintViolated(o, "The 'arrayref' does not refer to an array but is of type "+arrayref+".");
		return (arrayref instanceof ArrayType);
	}

	/***************************************************************/
	/* MISC                                                        */
	/***************************************************************/
	/**
	 * Ensures the general preconditions of an instruction that accesses the stack.
	 * This method is here because BCEL has no such superinterface for the stack
	 * accessing instructions; and there are funny unexpected exceptions in the
	 * semantices of the superinterfaces and superclasses provided.
	 * E.g. SWAP is a StackConsumer, but DUP_X1 is not a StackProducer.
	 * Therefore, this method is called by all StackProducer, StackConsumer,
	 * and StackInstruction instances via their visitXXX() method.
	 * Unfortunately, as the superclasses and superinterfaces overlap, some instructions
	 * cause this method to be called two or three times. [TODO: Fix this.]
	 *
	 * @see #visitStackConsumer(StackConsumer o)
	 * @see #visitStackProducer(StackProducer o)
	 * @see #visitStackInstruction(StackInstruction o)
	 */
	private void _visitStackAccessor(Instruction o){
		int consume = o.consumeStack(cpg); // Stack values are always consumed first; then produced.
		if (consume > stack().slotsUsed()){
			constraintViolated((Instruction) o, "Cannot consume "+consume+" stack slots: only "+stack().slotsUsed()+" slot(s) left on stack!\nStack:\n"+stack());
		}

		int produce = o.produceStack(cpg) - ((Instruction) o).consumeStack(cpg); // Stack values are always consumed first; then produced.
		if ( produce + stack().slotsUsed() > stack().maxStack() ){
			constraintViolated((Instruction) o, "Cannot produce "+produce+" stack slots: only "+(stack().maxStack()-stack().slotsUsed())+" free stack slot(s) left.\nStack:\n"+stack());
		}
	}

	/***************************************************************/
	/* "generic"visitXXXX methods where XXXX is an interface       */
	/* therefore, we don't know the order of visiting; but we know */
	/* these methods are called before the visitYYYY methods below */
	/***************************************************************/

	/**
	 * Assures the generic preconditions of a LoadClass instance.
	 * The referenced class is loaded and pass2-verified.
	 */
	public void visitLoadClass(Instruction o){
		ObjectType t = o.getLoadClassType(cpg);
		if (t != null){// null means "no class is loaded"
			Verifier v = VerifierFactory.getVerifier(t.getClassName());
			VerificationResult vr = v.doPass2();
			if (vr.getStatus() != VerificationResult.VERIFIED_OK){
				constraintViolated((Instruction) o, "Class '"+o.getLoadClassType(cpg).getClassName()+"' is referenced, but cannot be loaded and resolved: '"+vr+"'.");
			}
		}
	}

	/**
	 * Ensures the general preconditions of a StackConsumer instance.
	 */
	public void visitStackConsumer(Instruction o){
		_visitStackAccessor(o);
	}
	
	/**
	 * Ensures the general preconditions of a StackProducer instance.
	 */
	public void visitStackProducer(Instruction o){
		_visitStackAccessor(o);
	}


	/***************************************************************/
	/* "generic" visitYYYY methods where YYYY is a superclass.     */
	/* therefore, we know the order of visiting; we know           */
	/* these methods are called after the visitXXXX methods above. */
	/***************************************************************/
	public void visitCPInstruction(Instruction o){
		int idx = o.getIndex();
		if ((idx < 0) || (idx >= cpg.getSize())){
			throw new AssertionViolatedException("Huh?! Constant pool index of instruction '"+o+"' illegal? Pass 3a should have checked this!");
		}
	}

	 public void visitFieldInstruction(Instruction o){
	 	// visitLoadClass(o) has been called before: Every FieldOrMethod
	 	// implements LoadClass.
	 	// visitCPInstruction(o) has been called before.
		// A FieldInstruction may be: GETFIELD, GETSTATIC, PUTFIELD, PUTSTATIC 
			Constant c = cpg.getConstant(o.getIndex());
			if (!(c instanceof ConstantFieldref)){
				constraintViolated(o, "Index '"+o.getIndex()+"' should refer to a CONSTANT_Fieldref_info structure, but refers to '"+c+"'.");
			}
			// the o.getClassType(cpg) type has passed pass 2; see visitLoadClass(o).
			Type t = o.getType(cpg);
			if (t instanceof ObjectType){
				String name = ((ObjectType)t).getClassName();
				Verifier v = VerifierFactory.getVerifier( name );
				VerificationResult vr = v.doPass2();
				if (vr.getStatus() != VerificationResult.VERIFIED_OK){
					constraintViolated((Instruction) o, "Class '"+name+"' is referenced, but cannot be loaded and resolved: '"+vr+"'.");
				}
			}
	 }
	 
	 public void visitInvokeInstruction(InvokeInstruction o){
	 	// visitLoadClass(o) has been called before: Every FieldOrMethod
	 	// implements LoadClass.
	 	// visitCPInstruction(o) has been called before.
        //TODO
	 }
	 
	public void visitStackInstruction(Instruction o){
		_visitStackAccessor(o);
	}

	/**
	 * Assures the generic preconditions of a LocalVariableInstruction instance.
	 * That is, the index of the local variable must be valid.
	 */
	public void visitLocalVariableInstruction(InstructionLV o){
		if (locals().maxLocals() <= (o.getType(cpg).getSize()==1? o.getIndex() : o.getIndex()+1) ){
			constraintViolated(o, "The 'index' is not a valid index into the local variable array.");
		}
	}
	
	public void visitLoadInstruction(Instruction o){
		//visitLocalVariableInstruction(o) is called before, because it is more generic.

		// LOAD instructions must not read Type.UNKNOWN
		if (locals().get(o.getIndex()) == Type.UNKNOWN){
			constraintViolated(o, "Read-Access on local variable "+o.getIndex()+" with unknown content.");
		}

		// LOAD instructions, two-slot-values at index N must have Type.UNKNOWN
		// as a symbol for the higher halve at index N+1
		// [suppose some instruction put an int at N+1--- our double at N is defective]
		if (o.getType(cpg).getSize() == 2){
			if (locals().get(o.getIndex()+1) != Type.UNKNOWN){
				constraintViolated(o, "Reading a two-locals value from local variables "+o.getIndex()+" and "+(o.getIndex()+1)+" where the latter one is destroyed.");
			}
		}

		// LOAD instructions must read the correct type.
		if (!o.isALOAD()){
			if (locals().get(o.getIndex()) != o.getType(cpg) ){
				constraintViolated(o, "Local Variable type and LOADing Instruction type mismatch: Local Variable: '"+locals().get(o.getIndex())+"'; Instruction type: '"+o.getType(cpg)+"'.");
			}
		}
		else{ // we deal with an ALOAD
			if (!(locals().get(o.getIndex()) instanceof ReferenceType)){
				constraintViolated(o, "Local Variable type and LOADing Instruction type mismatch: Local Variable: '"+locals().get(o.getIndex())+"'; Instruction expects a ReferenceType.");
			}
			// ALOAD __IS ALLOWED__ to put uninitialized objects onto the stack!
			//referenceTypeIsInitialized(o, (ReferenceType) (locals().get(o.getIndex())));
		}

		// LOAD instructions must have enough free stack slots.
		if ((stack().maxStack() - stack().slotsUsed()) < o.getType(cpg).getSize()){
			constraintViolated(o, "Not enough free stack slots to load a '"+o.getType(cpg)+"' onto the OperandStack.");
		}
	}

	public void visitStoreInstruction(Instruction o){
		//visitLocalVariableInstruction(o) is called before, because it is more generic.

		if (stack().isEmpty()){ // Don't bother about 1 or 2 stack slots used. This check is implicitely done below while type checking.
			constraintViolated(o, "Cannot STORE: Stack to read from is empty.");
		}

		if (!o.isASTORE() ){
			if (! (stack().peek() == o.getType(cpg)) ){// the other xSTORE types are singletons in BCEL.
				constraintViolated(o, "Stack top type and STOREing Instruction type mismatch: Stack top: '"+stack().peek()+"'; Instruction type: '"+o.getType(cpg)+"'.");
			}
		}
		else{ // we deal with ASTORE
			Type stacktop = stack().peek();
			if ( (!(stacktop instanceof ReferenceType)) && (!(stacktop instanceof ReturnaddressType)) ){
				constraintViolated(o, "Stack top type and STOREing Instruction type mismatch: Stack top: '"+stack().peek()+"'; Instruction expects a ReferenceType or a ReturnadressType.");
			}
			if (stacktop instanceof ReferenceType){
				referenceTypeIsInitialized(o, (ReferenceType) stacktop);
			}
		}
	}

	public void visitReturnInstruction(Instruction o){
		if (o.getOpcode()==Constants.RETURN){
			return;
		}
		if (o.getOpcode()==Constants.ARETURN){
			if (stack().peek() == Type.NULL){
				return;
			}
			else{
				if (! (stack().peek() instanceof ReferenceType)){
					constraintViolated(o, "Reference type expected on top of stack, but is: '"+stack().peek()+"'.");
				}
				referenceTypeIsInitialized(o, (ReferenceType) (stack().peek()));
				//ReferenceType objectref = (ReferenceType) (stack().peek());
				// TODO: This can only be checked if using Staerk-et-al's "set of object types" instead of a
				// "wider cast object type" created during verification.
				//if (! (objectref.isAssignmentCompatibleWith(mg.getType())) ){
				//	constraintViolated(o, "Type on stack top which should be returned is a '"+stack().peek()+"' which is not assignment compatible with the return type of this method, '"+mg.getType()+"'.");
				//}
			}
		}
		else{
			Type method_type = mg.getType();
			if (method_type == Type.BOOLEAN ||
					method_type == Type.BYTE ||
					method_type == Type.SHORT ||
					method_type == Type.CHAR){
				method_type = Type.INT;
			}
			if (! ( method_type.equals( stack().peek() ))){
				constraintViolated(o, "Current method has return type of '"+mg.getType()+"' expecting a '"+method_type+"' on top of the stack. But stack top is a '"+stack().peek()+"'.");
			}
		}
	}

	/***************************************************************/
	/* "special"visitXXXX methods for one type of instruction each */
	/***************************************************************/

	public void visitAALOAD(Instruction o){
		Type arrayref = stack().peek(1);
		Type index    = stack().peek(0);
		
		indexOfInt(o, index);
		if (arrayrefOfArrayType(o, arrayref)){
			if (! (((ArrayType) arrayref).getElementType() instanceof ReferenceType)){
				constraintViolated(o, "The 'arrayref' does not refer to an array with elements of a ReferenceType but to an array of "+((ArrayType) arrayref).getElementType()+".");
			}	
			referenceTypeIsInitialized(o, (ReferenceType) (((ArrayType) arrayref).getElementType()));
		}
	}

	/**
	 * Ensures the specific preconditions of the said instruction.
	 */
	public void visitAASTORE(Instruction o){
		Type arrayref = stack().peek(2);
		Type index    = stack().peek(1);
		Type value    = stack().peek(0);

		indexOfInt(o, index);
		if (!(value instanceof ReferenceType)){
			constraintViolated(o, "The 'value' is not of a ReferenceType but of type "+value+".");
		}else{
			referenceTypeIsInitialized(o, (ReferenceType) value);
		}
		// Don't bother further with "referenceTypeIsInitialized()", there are no arrays
		// of an uninitialized object type. 
		if (arrayrefOfArrayType(o, arrayref)){
			if (! (((ArrayType) arrayref).getElementType() instanceof ReferenceType)){
				constraintViolated(o, "The 'arrayref' does not refer to an array with elements of a ReferenceType but to an array of "+((ArrayType) arrayref).getElementType()+".");
			}
			if (! ((ReferenceType)value).isAssignmentCompatibleWith((ReferenceType) ((ArrayType) arrayref).getElementType())){
				constraintViolated(o, "The type of 'value' ('"+value+"') is not assignment compatible to the components of the array 'arrayref' refers to. ('"+((ArrayType) arrayref).getElementType()+"')");
			}
		}
	}

	public void visitACONST_NULL(Instruction o){
		// Nothing needs to be done here.
	}

	public void visitALOAD(Instruction o){
		//visitLoadInstruction(LoadInstruction) is called before.
		// Nothing else needs to be done here.
	}

	public void visitANEWARRAY(Instruction o){
		if (!stack().peek().equals(Type.INT))
			constraintViolated(o, "The 'count' at the stack top is not of type '"+Type.INT+"' but of type '"+stack().peek()+"'.");
		// The runtime constant pool item at that index must be a symbolic reference to a class,
		// array, or interface type. See Pass 3a.
	}
	
	/**
	 * Ensures the specific preconditions of the said instruction.
	 */
	public void visitARETURN(Instruction o){
		if (! (stack().peek() instanceof ReferenceType) ){
			constraintViolated(o, "The 'objectref' at the stack top is not of a ReferenceType but of type '"+stack().peek()+"'.");
		}
		ReferenceType objectref = (ReferenceType) (stack().peek());
		referenceTypeIsInitialized(o, objectref);
		
		// The check below should already done via visitReturnInstruction(ReturnInstruction), see there.
		// It cannot be done using Staerk-et-al's "set of object types" instead of a
		// "wider cast object type", anyway.
		//if (! objectref.isAssignmentCompatibleWith(mg.getReturnType() )){
		//	constraintViolated(o, "The 'objectref' type "+objectref+" at the stack top is not assignment compatible with the return type '"+mg.getReturnType()+"' of the method.");
		//}
	}

	/**
	 * Ensures the specific preconditions of the said instruction.
	 */
	public void visitARRAYLENGTH(Instruction o){
		Type arrayref = stack().peek(0);
		arrayrefOfArrayType(o, arrayref);
	}

	/**
	 * Ensures the specific preconditions of the said instruction.
	 */
	public void visitASTORE(Instruction o){
		if (! ( (stack().peek() instanceof ReferenceType) || (stack().peek() instanceof ReturnaddressType) ) ){
			constraintViolated(o, "The 'objectref' is not of a ReferenceType or of ReturnaddressType but of "+stack().peek()+".");
		}
		if (stack().peek() instanceof ReferenceType){
			referenceTypeIsInitialized(o, (ReferenceType) (stack().peek()) );
		}
	}

	/**
	 * Ensures the specific preconditions of the said instruction.
	 */
	public void visitATHROW(Instruction o){
		// It's stated that 'objectref' must be of a ReferenceType --- but since Throwable is
		// not derived from an ArrayType, it follows that 'objectref' must be of an ObjectType or Type.NULL.
		if (! ((stack().peek() instanceof ObjectType) || (stack().peek().equals(Type.NULL))) ){
			constraintViolated(o, "The 'objectref' is not of an (initialized) ObjectType but of type "+stack().peek()+".");
		}
		
		// NULL is a subclass of every class, so to speak.
		if (stack().peek().equals(Type.NULL)) return;
				
		ObjectType exc = (ObjectType) (stack().peek());
		ObjectType throwable = (ObjectType) (Type.getType("Ljava/lang/Throwable;"));
		if ( (! (exc.subclassOf(throwable)) ) && (! (exc.equals(throwable))) ){
			constraintViolated(o, "The 'objectref' is not of class Throwable or of a subclass of Throwable, but of '"+stack().peek()+"'.");
		}
	}

	/**
	 * Ensures the specific preconditions of the said instruction.
	 */
	public void visitBALOAD(Instruction o){
		Type arrayref = stack().peek(1);
		Type index    = stack().peek(0);
		indexOfInt(o, index);
		if (arrayrefOfArrayType(o, arrayref)){
			if (! ( (((ArrayType) arrayref).getElementType().equals(Type.BOOLEAN)) ||
		 	       (((ArrayType) arrayref).getElementType().equals(Type.BYTE)) ) ){
				constraintViolated(o, "The 'arrayref' does not refer to an array with elements of a Type.BYTE or Type.BOOLEAN but to an array of '"+((ArrayType) arrayref).getElementType()+"'.");
			}
		}
	}

	/**
	 * Ensures the specific preconditions of the said instruction.
	 */
	public void visitBASTORE(Instruction o){
		Type arrayref = stack().peek(2);
		Type index    = stack().peek(1);
		Type value    = stack().peek(0);

		indexOfInt(o, index);
		valueOfInt(o, value);
		if (arrayrefOfArrayType(o, arrayref)){
			if (! ( (((ArrayType) arrayref).getElementType().equals(Type.BOOLEAN)) ||
			        (((ArrayType) arrayref).getElementType().equals(Type.BYTE)) ) )
					constraintViolated(o, "The 'arrayref' does not refer to an array with elements of a Type.BYTE or Type.BOOLEAN but to an array of '"+((ArrayType) arrayref).getElementType()+"'.");
		}
	}

	/**
	 * Ensures the specific preconditions of the said instruction.
	 */
	public void visitBIPUSH(Instruction o){
		// Nothing to do...
	}

	/**
	 * Ensures the specific preconditions of the said instruction.
	 */
	public void visitBREAKPOINT(Instruction o){
		throw new AssertionViolatedException("In this JustIce verification pass there should not occur an illegal instruction such as BREAKPOINT.");
	}

	/**
	 * Ensures the specific preconditions of the said instruction.
	 */
	public void visitCALOAD(Instruction o){
		Type arrayref = stack().peek(1);
		Type index = stack().peek(0);
		
		indexOfInt(o, index);
		arrayrefOfArrayType(o, arrayref);
	}

	/**
	 * Ensures the specific preconditions of the said instruction.
	 */
	public void visitCASTORE(Instruction o){
		Type arrayref = stack().peek(2);
		Type index = stack().peek(1);
		Type value = stack().peek(0);
		
		indexOfInt(o, index);
		valueOfInt(o, value);
		if (arrayrefOfArrayType(o, arrayref)){
			if (! ((ArrayType) arrayref).getElementType().equals(Type.CHAR) ){
				constraintViolated(o, "The 'arrayref' does not refer to an array with elements of type char but to an array of type "+((ArrayType) arrayref).getElementType()+".");
			}
		}
	}

	/**
	 * Ensures the specific preconditions of the said instruction.
	 */
	public void visitCHECKCAST(Instruction o){
		// The objectref must be of type reference.
		Type objectref = stack().peek(0);
		if (!(objectref instanceof ReferenceType)){
			constraintViolated(o, "The 'objectref' is not of a ReferenceType but of type "+objectref+".");
		}
		else{
			referenceTypeIsInitialized(o, (ReferenceType) objectref);
		}
		// The unsigned indexbyte1 and indexbyte2 are used to construct an index into the runtime constant pool of the
		// current class (§3.6), where the value of the index is (indexbyte1 << 8) | indexbyte2. The runtime constant
		// pool item at the index must be a symbolic reference to a class, array, or interface type.
		Constant c = cpg.getConstant(o.getIndex());
		if (! (c instanceof ConstantClass)){
			constraintViolated(o, "The Constant at 'index' is not a ConstantClass, but '"+c+"'.");
		}
	}

	public void visitD2F(Instruction o) { checkTop(o,Type.DOUBLE); }
	public void visitD2I(Instruction o) { checkTop(o,Type.DOUBLE); }
	public void visitD2L(Instruction o) { checkTop(o,Type.DOUBLE); }

	public void visitDADD(Instruction o){
		checkTop(o,Type.DOUBLE);
		if (stack().peek(1) != Type.DOUBLE){
			constraintViolated(o, "The value at the stack next-to-top is not of type 'double', but of type '"+stack().peek(1)+"'.");
		}
	}

	public void visitDALOAD(Instruction o){
		indexOfInt(o, stack().peek());
		if (stack().peek(1) == Type.NULL){
			return;
		} 
		if (! (stack().peek(1) instanceof ArrayType)){
			constraintViolated(o, "Stack next-to-top must be of type double[] but is '"+stack().peek(1)+"'.");
		}
		Type t = ((ArrayType) (stack().peek(1))).getBasicType();
		if (t != Type.DOUBLE){
			constraintViolated(o, "Stack next-to-top must be of type double[] but is '"+stack().peek(1)+"'.");
		}
	}

	public void visitDASTORE(Instruction o){
		if (stack().peek() != Type.DOUBLE){
			constraintViolated(o, "The value at the stack top is not of type 'double', but of type '"+stack().peek()+"'.");
		}
		indexOfInt(o, stack().peek(1));
		if (stack().peek(2) == Type.NULL){
			return;
		} 
		if (! (stack().peek(2) instanceof ArrayType)){
			constraintViolated(o, "Stack next-to-next-to-top must be of type double[] but is '"+stack().peek(2)+"'.");
		}
		Type t = ((ArrayType) (stack().peek(2))).getBasicType();
		if (t != Type.DOUBLE){
			constraintViolated(o, "Stack next-to-next-to-top must be of type double[] but is '"+stack().peek(2)+"'.");
		}
	}

	public void visitDCMPG(Instruction o){
		checkTop(o,Type.DOUBLE);
		if (stack().peek(1) != Type.DOUBLE){
			constraintViolated(o, "The value at the stack next-to-top is not of type 'double', but of type '"+stack().peek(1)+"'.");
		}
	}

	public void visitDCMPL(Instruction o){
		checkTop(o,Type.DOUBLE);
		if (stack().peek(1) != Type.DOUBLE){
			constraintViolated(o, "The value at the stack next-to-top is not of type 'double', but of type '"+stack().peek(1)+"'.");
		}
	}

	public void visitDCONST(Instruction o){
		// There's nothing to be done here.
	}
	
	public void visitDDIV(Instruction o){
		checkTop(o,Type.DOUBLE);
		if (stack().peek(1) != Type.DOUBLE){
			constraintViolated(o, "The value at the stack next-to-top is not of type 'double', but of type '"+stack().peek(1)+"'.");
		}
	}

	public void visitDLOAD(Instruction o){
		//visitLoadInstruction(LoadInstruction) is called before.
		
		// Nothing else needs to be done here.
	}

	public void visitDMUL(Instruction o){
		checkTop(o,Type.DOUBLE);
		if (stack().peek(1) != Type.DOUBLE){
			constraintViolated(o, "The value at the stack next-to-top is not of type 'double', but of type '"+stack().peek(1)+"'.");
		}
	}

	public void visitDNEG(Instruction o){
		checkTop(o,Type.DOUBLE);
	}

	public void visitDREM(Instruction o){
		checkTop(o,Type.DOUBLE);
		if (stack().peek(1) != Type.DOUBLE){
			constraintViolated(o, "The value at the stack next-to-top is not of type 'double', but of type '"+stack().peek(1)+"'.");
		}
	}
	
	private void checkTop(Instruction o,Type t) {
		if (stack().peek()!=t) 
			constraintViolated(o, "The value at the stack top is not of type '"+t+"', but of type '"+stack().peek()+"'.");
	}

	/**
	 * Ensures the specific preconditions of the said instruction.
	 */
	public void visitDRETURN(Instruction o){
		checkTop(o,Type.DOUBLE);
	}

	/**
	 * Ensures the specific preconditions of the said instruction.
	 */
	public void visitDSTORE(Instruction o){
		//visitStoreInstruction(StoreInstruction) is called before.
		
		// Nothing else needs to be done here.
	}

	/**
	 * Ensures the specific preconditions of the said instruction.
	 */
	public void visitDSUB(Instruction o){
		if (stack().peek() != Type.DOUBLE){
			constraintViolated(o, "The value at the stack top is not of type 'double', but of type '"+stack().peek()+"'.");
		}
		if (stack().peek(1) != Type.DOUBLE){
			constraintViolated(o, "The value at the stack next-to-top is not of type 'double', but of type '"+stack().peek(1)+"'.");
		}
	}

	/**
	 * Ensures the specific preconditions of the said instruction.
	 */
	public void visitDUP(Instruction o){
		if (stack().peek().getSize() != 1){
			constraintViolated(o, "Won't DUP type on stack top '"+stack().peek()+"' because it must occupy exactly one slot, not '"+stack().peek().getSize()+"'.");
		}
	}

	/**
	 * Ensures the specific preconditions of the said instruction.
	 */
	public void visitDUP_X1(Instruction o){
		if (stack().peek().getSize() != 1){
			constraintViolated(o, "Type on stack top '"+stack().peek()+"' should occupy exactly one slot, not '"+stack().peek().getSize()+"'.");
		}
		if (stack().peek(1).getSize() != 1){
			constraintViolated(o, "Type on stack next-to-top '"+stack().peek(1)+"' should occupy exactly one slot, not '"+stack().peek(1).getSize()+"'.");
		}
	}

	/**
	 * Ensures the specific preconditions of the said instruction.
	 */
	public void visitDUP_X2(Instruction o){
		if (stack().peek().getSize() != 1){
			constraintViolated(o, "Stack top type must be of size 1, but is '"+stack().peek()+"' of size '"+stack().peek().getSize()+"'.");
		}
		if (stack().peek(1).getSize() == 2){
			return; // Form 2, okay.
		}
		else{   //stack().peek(1).getSize == 1.
			if (stack().peek(2).getSize() != 1){
				constraintViolated(o, "If stack top's size is 1 and stack next-to-top's size is 1, stack next-to-next-to-top's size must also be 1, but is: '"+stack().peek(2)+"' of size '"+stack().peek(2).getSize()+"'.");
			}
		}
	}

	/**
	 * Ensures the specific preconditions of the said instruction.
	 */
	public void visitDUP2(Instruction o){
		if (stack().peek().getSize() == 2){
			return; // Form 2, okay.
		}
		else{ //stack().peek().getSize() == 1.
			if (stack().peek(1).getSize() != 1){
				constraintViolated(o, "If stack top's size is 1, then stack next-to-top's size must also be 1. But it is '"+stack().peek(1)+"' of size '"+stack().peek(1).getSize()+"'.");
			}
		}
	}

	/**
	 * Ensures the specific preconditions of the said instruction.
	 */
	public void visitDUP2_X1(Instruction o){
		if (stack().peek().getSize() == 2){
			if (stack().peek(1).getSize() != 1){
				constraintViolated(o, "If stack top's size is 2, then stack next-to-top's size must be 1. But it is '"+stack().peek(1)+"' of size '"+stack().peek(1).getSize()+"'.");
			}
			else{
				return; // Form 2
			}
		}
		else{ // stack top is of size 1
			if ( stack().peek(1).getSize() != 1 ){
				constraintViolated(o, "If stack top's size is 1, then stack next-to-top's size must also be 1. But it is '"+stack().peek(1)+"' of size '"+stack().peek(1).getSize()+"'.");
			}
			if ( stack().peek(2).getSize() != 1 ){
				constraintViolated(o, "If stack top's size is 1, then stack next-to-next-to-top's size must also be 1. But it is '"+stack().peek(2)+"' of size '"+stack().peek(2).getSize()+"'.");
			}
		}
	}

	/**
	 * Ensures the specific preconditions of the said instruction.
	 */
	public void visitDUP2_X2(Instruction o){

		if (stack().peek(0).getSize() == 2){
		 	if (stack().peek(1).getSize() == 2){
				return; // Form 4
			}
			else{// stack top size is 2, next-to-top's size is 1
				if ( stack().peek(2).getSize() != 1 ){
					constraintViolated(o, "If stack top's size is 2 and stack-next-to-top's size is 1, then stack next-to-next-to-top's size must also be 1. But it is '"+stack().peek(2)+"' of size '"+stack().peek(2).getSize()+"'.");
				}
				else{
					return; // Form 2
				}
			}
		}
		else{// stack top is of size 1
			if (stack().peek(1).getSize() == 1){
				if ( stack().peek(2).getSize() == 2 ){
					return; // Form 3
				}
				else{
					if ( stack().peek(3).getSize() == 1){
						return; // Form 1
					}
				}
			}
		}
		constraintViolated(o, "The operand sizes on the stack do not match any of the four forms of usage of this instruction.");
	}

	/**
	 * Ensures the specific preconditions of the said instruction.
	 */
	public void visitF2D(Instruction o){
		if (stack().peek() != Type.FLOAT){
			constraintViolated(o, "The value at the stack top is not of type 'float', but of type '"+stack().peek()+"'.");
		}
	}

	public void visitF2I(Instruction o){
		checkTop(o,Type.FLOAT);
	}

	public void visitF2L(Instruction o){
		checkTop(o,Type.FLOAT);
	}
	
	/**
	 * Ensures the specific preconditions of the said instruction.
	 */
	public void visitFADD(Instruction o){
		checkTop(o,Type.FLOAT);
		if (stack().peek(1) != Type.FLOAT){
			constraintViolated(o, "The value at the stack next-to-top is not of type 'float', but of type '"+stack().peek(1)+"'.");
		}
	}

	/**
	 * Ensures the specific preconditions of the said instruction.
	 */
	public void visitFALOAD(Instruction o){
		indexOfInt(o, stack().peek());
		if (stack().peek(1) == Type.NULL){
			return;
		} 
		if (! (stack().peek(1) instanceof ArrayType)){
			constraintViolated(o, "Stack next-to-top must be of type float[] but is '"+stack().peek(1)+"'.");
		}
		Type t = ((ArrayType) (stack().peek(1))).getBasicType();
		if (t != Type.FLOAT){
			constraintViolated(o, "Stack next-to-top must be of type float[] but is '"+stack().peek(1)+"'.");
		}
	}

	/**
	 * Ensures the specific preconditions of the said instruction.
	 */
	public void visitFASTORE(Instruction o){
		checkTop(o,Type.FLOAT);
		indexOfInt(o, stack().peek(1));
		if (stack().peek(2) == Type.NULL){
			return;
		} 
		if (! (stack().peek(2) instanceof ArrayType)){
			constraintViolated(o, "Stack next-to-next-to-top must be of type float[] but is '"+stack().peek(2)+"'.");
		}
		Type t = ((ArrayType) (stack().peek(2))).getBasicType();
		if (t != Type.FLOAT){
			constraintViolated(o, "Stack next-to-next-to-top must be of type float[] but is '"+stack().peek(2)+"'.");
		}
	}

	/**
	 * Ensures the specific preconditions of the said instruction.
	 */
	public void visitFCMPG(Instruction o){
		checkTop(o,Type.FLOAT);
		if (stack().peek(1) != Type.FLOAT){
			constraintViolated(o, "The value at the stack next-to-top is not of type 'float', but of type '"+stack().peek(1)+"'.");
		}
	}

	/**
	 * Ensures the specific preconditions of the said instruction.
	 */
	public void visitFCMPL(Instruction o){
		checkTop(o,Type.FLOAT);
		if (stack().peek(1) != Type.FLOAT){
			constraintViolated(o, "The value at the stack next-to-top is not of type 'float', but of type '"+stack().peek(1)+"'.");
		}
	}

	/**
	 * Ensures the specific preconditions of the said instruction.
	 */
	public void visitFCONST(Instruction o){
		// nothing to do here.
	}

	/**
	 * Ensures the specific preconditions of the said instruction.
	 */
	public void visitFDIV(Instruction o){
		checkTop(o,Type.FLOAT);
		if (stack().peek(1) != Type.FLOAT){
			constraintViolated(o, "The value at the stack next-to-top is not of type 'float', but of type '"+stack().peek(1)+"'.");
		}
	}

	/**
	 * Ensures the specific preconditions of the said instruction.
	 */
	public void visitFLOAD(Instruction o){
		//visitLoadInstruction(LoadInstruction) is called before.
		
		// Nothing else needs to be done here.
	}

	/**
	 * Ensures the specific preconditions of the said instruction.
	 */
	public void visitFMUL(Instruction o){
		checkTop(o,Type.FLOAT);
		if (stack().peek(1) != Type.FLOAT){
			constraintViolated(o, "The value at the stack next-to-top is not of type 'float', but of type '"+stack().peek(1)+"'.");
		}
	}

	/**
	 * Ensures the specific preconditions of the said instruction.
	 */
	public void visitFNEG(Instruction o){
		checkTop(o,Type.FLOAT);
	}

	/**
	 * Ensures the specific preconditions of the said instruction.
	 */
	public void visitFREM(Instruction o){
		checkTop(o,Type.FLOAT);
		if (stack().peek(1) != Type.FLOAT){
			constraintViolated(o, "The value at the stack next-to-top is not of type 'float', but of type '"+stack().peek(1)+"'.");
		}
	}

	/**
	 * Ensures the specific preconditions of the said instruction.
	 */
	public void visitFRETURN(Instruction o){
		checkTop(o,Type.FLOAT);
	}

	/**
	 * Ensures the specific preconditions of the said instruction.
	 */
	public void visitFSTORE(Instruction o){
		//visitStoreInstruction(StoreInstruction) is called before.
		
		// Nothing else needs to be done here.
	}

	/**
	 * Ensures the specific preconditions of the said instruction.
	 */
	public void visitFSUB(Instruction o){
		if (stack().peek() != Type.FLOAT){
			constraintViolated(o, "The value at the stack top is not of type 'float', but of type '"+stack().peek()+"'.");
		}
		if (stack().peek(1) != Type.FLOAT){
			constraintViolated(o, "The value at the stack next-to-top is not of type 'float', but of type '"+stack().peek(1)+"'.");
		}
	}

	/**
	 * Ensures the specific preconditions of the said instruction.
	 */
	public void visitGETFIELD(FieldInstruction o){
		Type objectref = stack().peek();
		if (! ( (objectref instanceof ObjectType) || (objectref == Type.NULL) ) ){
			constraintViolated(o, "Stack top should be an object reference that's not an array reference, but is '"+objectref+"'.");
		}
		
		String field_name = o.getFieldName(cpg);
		
		JavaClass jc = Repository.lookupClass(o.getClassType(cpg).getClassName());
		Field[] fields = jc.getFields();
		Field f = null;
		for (int i=0; i<fields.length; i++){
			if (fields[i].getName().equals(field_name)){
				f = fields[i];
				break;
			}
		}
		if (f == null){
			throw new AssertionViolatedException("Field not found?!?");
		}

		if (f.isProtected()){
			ObjectType classtype = o.getClassType(cpg);
			ObjectType curr = new ObjectType(mg.getClassName());

			if (	classtype.equals(curr) ||
						curr.subclassOf(classtype)	){
				Type t = stack().peek();
				if (t == Type.NULL){
					return;
				}
				if (! (t instanceof ObjectType) ){
					constraintViolated(o, "The 'objectref' must refer to an object that's not an array. Found instead: '"+t+"'.");
				}
				ObjectType objreftype = (ObjectType) t;
				if (! ( objreftype.equals(curr) ||
						    objreftype.subclassOf(curr) ) ){
					//TODO: One day move to Staerk-et-al's "Set of object types" instead of "wider" object types
					//      created during the verification.
					//      "Wider" object types don't allow us to check for things like that below.
					//constraintViolated(o, "The referenced field has the ACC_PROTECTED modifier, and it's a member of the current class or a superclass of the current class. However, the referenced object type '"+stack().peek()+"' is not the current class or a subclass of the current class.");
				}
			} 
		}
		
		// TODO: Could go into Pass 3a.
		if (f.isStatic()){
			constraintViolated(o, "Referenced field '"+f+"' is static which it shouldn't be.");
		}
	}

	/**
	 * Ensures the specific preconditions of the said instruction.
	 */
	public void visitGETSTATIC(FieldInstruction o){
		// Field must be static: see Pass 3a.
	}

	/**
	 * Ensures the specific preconditions of the said instruction.
	 */
	public void visitGOTO(Instruction o){
		// nothing to do here.
	}

	/**
	 * Ensures the specific preconditions of the said instruction.
	 */
	public void visitGOTO_W(Instruction o){
		// nothing to do here.
	}

	/**
	 * Ensures the specific preconditions of the said instruction.
	 */
	public void visitI2B(Instruction o){
		if (stack().peek() != Type.INT){
			constraintViolated(o, "The value at the stack top is not of type 'int', but of type '"+stack().peek()+"'.");
		}
	}

	/**
	 * Ensures the specific preconditions of the said instruction.
	 */
	public void visitI2C(Instruction o){
		if (stack().peek() != Type.INT){
			constraintViolated(o, "The value at the stack top is not of type 'int', but of type '"+stack().peek()+"'.");
		}
	}

	/**
	 * Ensures the specific preconditions of the said instruction.
	 */
	public void visitI2D(Instruction o){
		if (stack().peek() != Type.INT){
			constraintViolated(o, "The value at the stack top is not of type 'int', but of type '"+stack().peek()+"'.");
		}
	}

	public void visitI2F(Instruction o) { checkTop(o,Type.INT); }
	public void visitI2L(Instruction o) { checkTop(o,Type.INT); }
	public void visitI2S(Instruction o) { checkTop(o,Type.INT); }

	public void visitIADD(Instruction o){
		checkTop(o,Type.INT);
		if (stack().peek(1) != Type.INT){
			constraintViolated(o, "The value at the stack next-to-top is not of type 'int', but of type '"+stack().peek(1)+"'.");
		}
	}

	/**
	 * Ensures the specific preconditions of the said instruction.
	 */
	public void visitIALOAD(Instruction o){
		indexOfInt(o, stack().peek());
		if (stack().peek(1) == Type.NULL){
			return;
		} 
		if (! (stack().peek(1) instanceof ArrayType)){
			constraintViolated(o, "Stack next-to-top must be of type int[] but is '"+stack().peek(1)+"'.");
		}
		Type t = ((ArrayType) (stack().peek(1))).getBasicType();
		if (t != Type.INT){
			constraintViolated(o, "Stack next-to-top must be of type int[] but is '"+stack().peek(1)+"'.");
		}
	}

	/**
	 * Ensures the specific preconditions of the said instruction.
	 */
	public void visitIAND(Instruction o){
		if (stack().peek() != Type.INT){
			constraintViolated(o, "The value at the stack top is not of type 'int', but of type '"+stack().peek()+"'.");
		}
		if (stack().peek(1) != Type.INT){
			constraintViolated(o, "The value at the stack next-to-top is not of type 'int', but of type '"+stack().peek(1)+"'.");
		}
	}

	/**
	 * Ensures the specific preconditions of the said instruction.
	 */
	public void visitIASTORE(Instruction o){
		if (stack().peek() != Type.INT){
			constraintViolated(o, "The value at the stack top is not of type 'int', but of type '"+stack().peek()+"'.");
		}
		indexOfInt(o, stack().peek(1));
		if (stack().peek(2) == Type.NULL){
			return;
		} 
		if (! (stack().peek(2) instanceof ArrayType)){
			constraintViolated(o, "Stack next-to-next-to-top must be of type int[] but is '"+stack().peek(2)+"'.");
		}
		Type t = ((ArrayType) (stack().peek(2))).getBasicType();
		if (t != Type.INT){
			constraintViolated(o, "Stack next-to-next-to-top must be of type int[] but is '"+stack().peek(2)+"'.");
		}
	}

	/**
	 * Ensures the specific preconditions of the said instruction.
	 */
	public void visitICONST(Instruction o){
		//nothing to do here.
	}

	/**
	 * Ensures the specific preconditions of the said instruction.
	 */
	public void visitIDIV(Instruction o){
		checkTop(o,Type.INT);
		if (stack().peek(1) != Type.INT){
			constraintViolated(o, "The value at the stack next-to-top is not of type 'int', but of type '"+stack().peek(1)+"'.");
		}
	}

	/**
	 * Ensures the specific preconditions of the said instruction.
	 */
	public void visitIF_ACMPEQ(Instruction o){
		if (!(stack().peek() instanceof ReferenceType)){
			constraintViolated(o, "The value at the stack top is not of a ReferenceType, but of type '"+stack().peek()+"'.");
		}
		referenceTypeIsInitialized(o, (ReferenceType) (stack().peek()) );
	
		if (!(stack().peek(1) instanceof ReferenceType)){
			constraintViolated(o, "The value at the stack next-to-top is not of a ReferenceType, but of type '"+stack().peek(1)+"'.");
		}
		referenceTypeIsInitialized(o, (ReferenceType) (stack().peek(1)) );
		
	}

	/**
	 * Ensures the specific preconditions of the said instruction.
	 */
	public void visitIF_ACMPNE(Instruction o){
		if (!(stack().peek() instanceof ReferenceType)){
			constraintViolated(o, "The value at the stack top is not of a ReferenceType, but of type '"+stack().peek()+"'.");
			referenceTypeIsInitialized(o, (ReferenceType) (stack().peek()) );
		}
		if (!(stack().peek(1) instanceof ReferenceType)){
			constraintViolated(o, "The value at the stack next-to-top is not of a ReferenceType, but of type '"+stack().peek(1)+"'.");
			referenceTypeIsInitialized(o, (ReferenceType) (stack().peek(1)) );
		}
	}

	/**
	 * Ensures the specific preconditions of the said instruction.
	 */
	public void visitIF_ICMPEQ(Instruction o){
		if (stack().peek() != Type.INT){
			constraintViolated(o, "The value at the stack top is not of type 'int', but of type '"+stack().peek()+"'.");
		}
		if (stack().peek(1) != Type.INT){
			constraintViolated(o, "The value at the stack next-to-top is not of type 'int', but of type '"+stack().peek(1)+"'.");
		}
	}

	/**
	 * Ensures the specific preconditions of the said instruction.
	 */
	public void visitIF_ICMPGE(Instruction o){
		if (stack().peek() != Type.INT){
			constraintViolated(o, "The value at the stack top is not of type 'int', but of type '"+stack().peek()+"'.");
		}
		if (stack().peek(1) != Type.INT){
			constraintViolated(o, "The value at the stack next-to-top is not of type 'int', but of type '"+stack().peek(1)+"'.");
		}
	}

	/**
	 * Ensures the specific preconditions of the said instruction.
	 */
	public void visitIF_ICMPGT(Instruction o){
		if (stack().peek() != Type.INT){
			constraintViolated(o, "The value at the stack top is not of type 'int', but of type '"+stack().peek()+"'.");
		}
		if (stack().peek(1) != Type.INT){
			constraintViolated(o, "The value at the stack next-to-top is not of type 'int', but of type '"+stack().peek(1)+"'.");
		}
	}

	/**
	 * Ensures the specific preconditions of the said instruction.
	 */
	public void visitIF_ICMPLE(Instruction o){
		if (stack().peek() != Type.INT){
			constraintViolated(o, "The value at the stack top is not of type 'int', but of type '"+stack().peek()+"'.");
		}
		if (stack().peek(1) != Type.INT){
			constraintViolated(o, "The value at the stack next-to-top is not of type 'int', but of type '"+stack().peek(1)+"'.");
		}
	}

	/**
	 * Ensures the specific preconditions of the said instruction.
	 */
	public void visitIF_ICMPLT(Instruction o){
		if (stack().peek() != Type.INT){
			constraintViolated(o, "The value at the stack top is not of type 'int', but of type '"+stack().peek()+"'.");
		}
		if (stack().peek(1) != Type.INT){
			constraintViolated(o, "The value at the stack next-to-top is not of type 'int', but of type '"+stack().peek(1)+"'.");
		}
	}

	/**
	 * Ensures the specific preconditions of the said instruction.
	 */
	public void visitIF_ICMPNE(Instruction o){
		if (stack().peek() != Type.INT){
			constraintViolated(o, "The value at the stack top is not of type 'int', but of type '"+stack().peek()+"'.");
		}
		if (stack().peek(1) != Type.INT){
			constraintViolated(o, "The value at the stack next-to-top is not of type 'int', but of type '"+stack().peek(1)+"'.");
		}
	}

	/**
	 * Ensures the specific preconditions of the said instruction.
	 */
	public void visitIFEQ(Instruction o){
		if (stack().peek() != Type.INT){
			constraintViolated(o, "The value at the stack top is not of type 'int', but of type '"+stack().peek()+"'.");
		}
	}

	/**
	 * Ensures the specific preconditions of the said instruction.
	 */
	public void visitIFGE(Instruction o){
		if (stack().peek() != Type.INT){
			constraintViolated(o, "The value at the stack top is not of type 'int', but of type '"+stack().peek()+"'.");
		}
	}

	/**
	 * Ensures the specific preconditions of the said instruction.
	 */
	public void visitIFGT(Instruction o){
		if (stack().peek() != Type.INT){
			constraintViolated(o, "The value at the stack top is not of type 'int', but of type '"+stack().peek()+"'.");
		}
	}

	/**
	 * Ensures the specific preconditions of the said instruction.
	 */
	public void visitIFLE(Instruction o){
		if (stack().peek() != Type.INT){
			constraintViolated(o, "The value at the stack top is not of type 'int', but of type '"+stack().peek()+"'.");
		}
	}

	/**
	 * Ensures the specific preconditions of the said instruction.
	 */
	public void visitIFLT(Instruction o){
		if (stack().peek() != Type.INT){
			constraintViolated(o, "The value at the stack top is not of type 'int', but of type '"+stack().peek()+"'.");
		}
	}

	/**
	 * Ensures the specific preconditions of the said instruction.
	 */
	public void visitIFNE(Instruction o){
		if (stack().peek() != Type.INT){
			constraintViolated(o, "The value at the stack top is not of type 'int', but of type '"+stack().peek()+"'.");
		}
	}

	/**
	 * Ensures the specific preconditions of the said instruction.
	 */
	public void visitIFNONNULL(Instruction o){
		if (!(stack().peek() instanceof ReferenceType)){
			constraintViolated(o, "The value at the stack top is not of a ReferenceType, but of type '"+stack().peek()+"'.");
		}
		referenceTypeIsInitialized(o, (ReferenceType) (stack().peek()) );	
	}

	/**
	 * Ensures the specific preconditions of the said instruction.
	 */
	public void visitIFNULL(Instruction o){
		if (!(stack().peek() instanceof ReferenceType)){
			constraintViolated(o, "The value at the stack top is not of a ReferenceType, but of type '"+stack().peek()+"'.");
		}
		referenceTypeIsInitialized(o, (ReferenceType) (stack().peek()) );	
	}

	/**
	 * Ensures the specific preconditions of the said instruction.
	 */
	public void visitIINC(IINC o){
		// Mhhh. In BCEL, at this time "IINC" is not a LocalVariableInstruction.
		if (locals().maxLocals() <= (o.getType(cpg).getSize()==1? o.getIndex() : o.getIndex()+1) ){
			constraintViolated(o, "The 'index' is not a valid index into the local variable array.");
		}

		indexOfInt(o, locals().get(o.getIndex()));
	}

	/**
	 * Ensures the specific preconditions of the said instruction.
	 */
	public void visitILOAD(Instruction o){
		// All done by visitLocalVariableInstruction(), visitLoadInstruction()
	}

	/**
	 * Ensures the specific preconditions of the said instruction.
	 */
	public void visitIMPDEP1(Instruction o){
		throw new AssertionViolatedException("In this JustIce verification pass there should not occur an illegal instruction such as IMPDEP1.");
	}

	/**
	 * Ensures the specific preconditions of the said instruction.
	 */
	public void visitIMPDEP2(Instruction o){
		throw new AssertionViolatedException("In this JustIce verification pass there should not occur an illegal instruction such as IMPDEP2.");
	}

	/**
	 * Ensures the specific preconditions of the said instruction.
	 */
	public void visitIMUL(Instruction o){
		if (stack().peek() != Type.INT){
			constraintViolated(o, "The value at the stack top is not of type 'int', but of type '"+stack().peek()+"'.");
		}
		if (stack().peek(1) != Type.INT){
			constraintViolated(o, "The value at the stack next-to-top is not of type 'int', but of type '"+stack().peek(1)+"'.");
		}
	}

	/**
	 * Ensures the specific preconditions of the said instruction.
	 */
	public void visitINEG(Instruction o){
		if (stack().peek() != Type.INT){
			constraintViolated(o, "The value at the stack top is not of type 'int', but of type '"+stack().peek()+"'.");
		}
	}

	/**
	 * Ensures the specific preconditions of the said instruction.
	 */
	public void visitINSTANCEOF(Instruction o){
		// The objectref must be of type reference.
		Type objectref = stack().peek(0);
		if (!(objectref instanceof ReferenceType)){
			constraintViolated(o, "The 'objectref' is not of a ReferenceType but of type "+objectref+".");
		}
		else{
			referenceTypeIsInitialized(o, (ReferenceType) objectref);
		}
		// The unsigned indexbyte1 and indexbyte2 are used to construct an index into the runtime constant pool of the
		// current class (§3.6), where the value of the index is (indexbyte1 << 8) | indexbyte2. The runtime constant
		// pool item at the index must be a symbolic reference to a class, array, or interface type.
		Constant c = cpg.getConstant(o.getIndex());
		if (! (c instanceof ConstantClass)){
			constraintViolated(o, "The Constant at 'index' is not a ConstantClass, but '"+c+"'.");
		}
	}

	/**
	 * Ensures the specific preconditions of the said instruction.
	 */
	public void visitINVOKEINTERFACE(INVOKEINTERFACE o){
		// Method is not native, otherwise pass 3 would not happen.
		
		int count = o.getCount();
		if (count == 0){
			constraintViolated(o, "The 'count' argument must not be 0.");
		}
		// It is a ConstantInterfaceMethodref, Pass 3a made it sure.
		// TODO: Do we want to do anything with it?
        //ConstantInterfaceMethodref cimr = (ConstantInterfaceMethodref) (cpg.getConstant(o.getIndex()));
		
		// the o.getClassType(cpg) type has passed pass 2; see visitLoadClass(o).

		Type t = o.getType(cpg);
		if (t instanceof ObjectType){
			String name = ((ObjectType)t).getClassName();
			Verifier v = VerifierFactory.getVerifier( name );
			VerificationResult vr = v.doPass2();
			if (vr.getStatus() != VerificationResult.VERIFIED_OK){
				constraintViolated((Instruction) o, "Class '"+name+"' is referenced, but cannot be loaded and resolved: '"+vr+"'.");
			}
		}


		Type[] argtypes = o.getArgumentTypes(cpg);
		int nargs = argtypes.length;
		
		for (int i=nargs-1; i>=0; i--){
			Type fromStack = stack().peek( (nargs-1) - i );	// 0 to nargs-1
			Type fromDesc = argtypes[i];
			if (fromDesc == Type.BOOLEAN ||
					fromDesc == Type.BYTE ||
					fromDesc == Type.CHAR ||
					fromDesc == Type.SHORT){
				fromDesc = Type.INT;
			}
			if (! fromStack.equals(fromDesc)){
				if (fromStack instanceof ReferenceType && fromDesc instanceof ReferenceType){
					//ReferenceType rFromStack = (ReferenceType) fromStack;
					//ReferenceType rFromDesc = (ReferenceType) fromDesc;
					// TODO: This can only be checked when using Staerk-et-al's "set of object types"
					// instead of a "wider cast object type" created during verification.
					//if ( ! rFromStack.isAssignmentCompatibleWith(rFromDesc) ){
					//	constraintViolated(o, "Expecting a '"+fromDesc+"' but found a '"+fromStack+"' on the stack (which is not assignment compatible).");
					//}
				}
				else{
					constraintViolated(o, "Expecting a '"+fromDesc+"' but found a '"+fromStack+"' on the stack.");
				}
			}
		}
		
		Type objref = stack().peek(nargs);
		if (objref == Type.NULL){
			return;
		}
		if (! (objref instanceof ReferenceType) ){
			constraintViolated(o, "Expecting a reference type as 'objectref' on the stack, not a '"+objref+"'.");
		}
		referenceTypeIsInitialized(o, (ReferenceType) objref);
		if (!(objref instanceof ObjectType)){
			if (!(objref instanceof ArrayType)){
				constraintViolated(o, "Expecting an ObjectType as 'objectref' on the stack, not a '"+objref+"'."); // could be a ReturnaddressType
			}
			else{
				objref = GENERIC_ARRAY;
			}
		}
		
		// String objref_classname = ((ObjectType) objref).getClassName();
	    // String theInterface = o.getClassName(cpg);
		// TODO: This can only be checked if we're using Staerk-et-al's "set of object types"
		//       instead of "wider cast object types" generated during verification.
		//if ( ! Repository.implementationOf(objref_classname, theInterface) ){
		//	constraintViolated(o, "The 'objref' item '"+objref+"' does not implement '"+theInterface+"' as expected.");
		//}	

		int counted_count = 1; // 1 for the objectref
		for (int i=0; i<nargs; i++){
			counted_count += argtypes[i].getSize();
		}
		if (count != counted_count){
			constraintViolated(o, "The 'count' argument should probably read '"+counted_count+"' but is '"+count+"'.");
		}
	}

	/**
	 * Ensures the specific preconditions of the said instruction.
	 */
	public void visitINVOKESPECIAL(InvokeInstruction o){
		// Don't init an object twice.
		if ( (o.getMethodName(cpg).equals(Constants.CONSTRUCTOR_NAME)) && (!(stack().peek(o.getArgumentTypes(cpg).length) instanceof UninitializedObjectType)) ){
			constraintViolated(o, "Possibly initializing object twice. A valid instruction sequence must not have an uninitialized object on the operand stack or in a local variable during a backwards branch, or in a local variable in code protected by an exception handler. Please see The Java Virtual Machine Specification, Second Edition, 4.9.4 (pages 147 and 148) for details.");
		}

		// the o.getClassType(cpg) type has passed pass 2; see visitLoadClass(o).

		Type t = o.getType(cpg);
		if (t instanceof ObjectType){
			String name = ((ObjectType)t).getClassName();
			Verifier v = VerifierFactory.getVerifier( name );
			VerificationResult vr = v.doPass2();
			if (vr.getStatus() != VerificationResult.VERIFIED_OK){
				constraintViolated((Instruction) o, "Class '"+name+"' is referenced, but cannot be loaded and resolved: '"+vr+"'.");
			}
		}


		Type[] argtypes = o.getArgumentTypes(cpg);
		int nargs = argtypes.length;
		
		for (int i=nargs-1; i>=0; i--){
			Type fromStack = stack().peek( (nargs-1) - i );	// 0 to nargs-1
			Type fromDesc = argtypes[i];
			if (fromDesc == Type.BOOLEAN ||
					fromDesc == Type.BYTE ||
					fromDesc == Type.CHAR ||
					fromDesc == Type.SHORT){
				fromDesc = Type.INT;
			}
			if (! fromStack.equals(fromDesc)){
				if (fromStack instanceof ReferenceType && fromDesc instanceof ReferenceType){
					ReferenceType rFromStack = (ReferenceType) fromStack;
					ReferenceType rFromDesc = (ReferenceType) fromDesc;
					// TODO: This can only be checked using Staerk-et-al's "set of object types", not
					// using a "wider cast object type".
					if ( ! rFromStack.isAssignmentCompatibleWith(rFromDesc) ){
						constraintViolated(o, "Expecting a '"+fromDesc+"' but found a '"+fromStack+"' on the stack (which is not assignment compatible).");
					}
				}
				else{
					constraintViolated(o, "Expecting a '"+fromDesc+"' but found a '"+fromStack+"' on the stack.");
				}
			}
		}
		
		Type objref = stack().peek(nargs);
		if (objref == Type.NULL){
			return;
		}
		if (! (objref instanceof ReferenceType) ){
			constraintViolated(o, "Expecting a reference type as 'objectref' on the stack, not a '"+objref+"'.");
		}
		String objref_classname = null;
		if ( !(o.getMethodName(cpg).equals(Constants.CONSTRUCTOR_NAME))){
			referenceTypeIsInitialized(o, (ReferenceType) objref);
			if (!(objref instanceof ObjectType)){
				if (!(objref instanceof ArrayType)){
					constraintViolated(o, "Expecting an ObjectType as 'objectref' on the stack, not a '"+objref+"'."); // could be a ReturnaddressType
				}
				else{
					objref = GENERIC_ARRAY;
				}
			}

			objref_classname = ((ObjectType) objref).getClassName();		
		}
		else{
			if (!(objref instanceof UninitializedObjectType)){
				constraintViolated(o, "Expecting an UninitializedObjectType as 'objectref' on the stack, not a '"+objref+"'. Otherwise, you couldn't invoke a method since an array has no methods (not to speak of a return address).");
			}
			objref_classname = ((UninitializedObjectType) objref).getInitialized().getClassName();
		}
		

		String theClass = o.getClassName(cpg);
		if ( ! Repository.instanceOf(objref_classname, theClass) ){
			constraintViolated(o, "The 'objref' item '"+objref+"' does not implement '"+theClass+"' as expected.");
		}	
		
	}

	/**
	 * Ensures the specific preconditions of the said instruction.
	 */
	public void visitINVOKESTATIC(InvokeInstruction o){
		// Method is not native, otherwise pass 3 would not happen.
		
		Type t = o.getType(cpg);
		if (t instanceof ObjectType){
			String name = ((ObjectType)t).getClassName();
			Verifier v = VerifierFactory.getVerifier( name );
			VerificationResult vr = v.doPass2();
			if (vr.getStatus() != VerificationResult.VERIFIED_OK){
				constraintViolated((Instruction) o, "Class '"+name+"' is referenced, but cannot be loaded and resolved: '"+vr+"'.");
			}
		}

		Type[] argtypes = o.getArgumentTypes(cpg);
		int nargs = argtypes.length;
		
		for (int i=nargs-1; i>=0; i--){
			Type fromStack = stack().peek( (nargs-1) - i );	// 0 to nargs-1
			Type fromDesc = argtypes[i];
			if (fromDesc == Type.BOOLEAN ||
					fromDesc == Type.BYTE ||
					fromDesc == Type.CHAR ||
					fromDesc == Type.SHORT){
				fromDesc = Type.INT;
			}
			if (! fromStack.equals(fromDesc)){
				if (fromStack instanceof ReferenceType && fromDesc instanceof ReferenceType){
					ReferenceType rFromStack = (ReferenceType) fromStack;
					ReferenceType rFromDesc = (ReferenceType) fromDesc;
					// TODO: This check can possibly only be done using Staerk-et-al's "set of object types"
					// instead of a "wider cast object type" created during verification.
					if ( ! rFromStack.isAssignmentCompatibleWith(rFromDesc) ){
						constraintViolated(o, "Expecting a '"+fromDesc+"' but found a '"+fromStack+"' on the stack (which is not assignment compatible).");
					}
				}
				else{
					constraintViolated(o, "Expecting a '"+fromDesc+"' but found a '"+fromStack+"' on the stack.");
				}
			}
		}
	}

	/**
	 * Ensures the specific preconditions of the said instruction.
	 */
	public void visitINVOKEVIRTUAL(InvokeInstruction o){
		// the o.getClassType(cpg) type has passed pass 2; see visitLoadClass(o).

		Type t = o.getType(cpg);
		if (t instanceof ObjectType){
			String name = ((ObjectType)t).getClassName();
			Verifier v = VerifierFactory.getVerifier( name );
			VerificationResult vr = v.doPass2();
			if (vr.getStatus() != VerificationResult.VERIFIED_OK){
				constraintViolated((Instruction) o, "Class '"+name+"' is referenced, but cannot be loaded and resolved: '"+vr+"'.");
			}
		}


		Type[] argtypes = o.getArgumentTypes(cpg);
		int nargs = argtypes.length;
		
		for (int i=nargs-1; i>=0; i--){
			Type fromStack = stack().peek( (nargs-1) - i );	// 0 to nargs-1
			Type fromDesc = argtypes[i];
			if (fromDesc == Type.BOOLEAN ||
					fromDesc == Type.BYTE ||
					fromDesc == Type.CHAR ||
					fromDesc == Type.SHORT){
				fromDesc = Type.INT;
			}
			if (! fromStack.equals(fromDesc)){
				if (fromStack instanceof ReferenceType && fromDesc instanceof ReferenceType){
					ReferenceType rFromStack = (ReferenceType) fromStack;
					ReferenceType rFromDesc = (ReferenceType) fromDesc;
					// TODO: This can possibly only be checked when using Staerk-et-al's "set of object types" instead
					// of a single "wider cast object type" created during verification.
					if ( ! rFromStack.isAssignmentCompatibleWith(rFromDesc) ){
						constraintViolated(o, "Expecting a '"+fromDesc+"' but found a '"+fromStack+"' on the stack (which is not assignment compatible).");
					}
				}
				else{
					constraintViolated(o, "Expecting a '"+fromDesc+"' but found a '"+fromStack+"' on the stack.");
				}
			}
		}
		
		Type objref = stack().peek(nargs);
		if (objref == Type.NULL){
			return;
		}
		if (! (objref instanceof ReferenceType) ){
			constraintViolated(o, "Expecting a reference type as 'objectref' on the stack, not a '"+objref+"'.");
		}
		referenceTypeIsInitialized(o, (ReferenceType) objref);
		if (!(objref instanceof ObjectType)){
			if (!(objref instanceof ArrayType)){
				constraintViolated(o, "Expecting an ObjectType as 'objectref' on the stack, not a '"+objref+"'."); // could be a ReturnaddressType
			}
			else{
				objref = GENERIC_ARRAY;
			}
		}
		
		String objref_classname = ((ObjectType) objref).getClassName();

		String theClass = o.getClassName(cpg);
	
		if ( ! Repository.instanceOf(objref_classname, theClass) ){
			constraintViolated(o, "The 'objref' item '"+objref+"' does not implement '"+theClass+"' as expected.");
		}	
	}

	/**
	 * Ensures the specific preconditions of the said instruction.
	 */
	public void visitIOR(Instruction o){
		if (stack().peek() != Type.INT){
			constraintViolated(o, "The value at the stack top is not of type 'int', but of type '"+stack().peek()+"'.");
		}
		if (stack().peek(1) != Type.INT){
			constraintViolated(o, "The value at the stack next-to-top is not of type 'int', but of type '"+stack().peek(1)+"'.");
		}
	}

	/**
	 * Ensures the specific preconditions of the said instruction.
	 */
	public void visitIREM(Instruction o){
		if (stack().peek() != Type.INT){
			constraintViolated(o, "The value at the stack top is not of type 'int', but of type '"+stack().peek()+"'.");
		}
		if (stack().peek(1) != Type.INT){
			constraintViolated(o, "The value at the stack next-to-top is not of type 'int', but of type '"+stack().peek(1)+"'.");
		}
	}

	/**
	 * Ensures the specific preconditions of the said instruction.
	 */
	public void visitIRETURN(Instruction o){
		if (stack().peek() != Type.INT){
			constraintViolated(o, "The value at the stack top is not of type 'int', but of type '"+stack().peek()+"'.");
		}
	}

	/**
	 * Ensures the specific preconditions of the said instruction.
	 */
	public void visitISHL(Instruction o){
		checkTop(o,Type.INT);
		if (stack().peek(1) != Type.INT){
			constraintViolated(o, "The value at the stack next-to-top is not of type 'int', but of type '"+stack().peek(1)+"'.");
		}
	}

	/**
	 * Ensures the specific preconditions of the said instruction.
	 */
	public void visitISHR(Instruction o){
		checkTop(o,Type.INT);
		if (stack().peek(1) != Type.INT){
			constraintViolated(o, "The value at the stack next-to-top is not of type 'int', but of type '"+stack().peek(1)+"'.");
		}
	}

	/**
	 * Ensures the specific preconditions of the said instruction.
	 */
	public void visitISTORE(Instruction o){
		//visitStoreInstruction(StoreInstruction) is called before.
		
		// Nothing else needs to be done here.
	}

	/**
	 * Ensures the specific preconditions of the said instruction.
	 */
	public void visitISUB(Instruction o){
		checkTop(o,Type.INT);
		if (stack().peek(1) != Type.INT){
			constraintViolated(o, "The value at the stack next-to-top is not of type 'int', but of type '"+stack().peek(1)+"'.");
		}
	}

	/**
	 * Ensures the specific preconditions of the said instruction.
	 */
	public void visitIUSHR(Instruction o){
		checkTop(o,Type.INT);
		if (stack().peek(1) != Type.INT){
			constraintViolated(o, "The value at the stack next-to-top is not of type 'int', but of type '"+stack().peek(1)+"'.");
		}
	}

	/**
	 * Ensures the specific preconditions of the said instruction.
	 */
	public void visitIXOR(Instruction o){
		if (stack().peek() != Type.INT){
			constraintViolated(o, "The value at the stack top is not of type 'int', but of type '"+stack().peek()+"'.");
		}
		if (stack().peek(1) != Type.INT){
			constraintViolated(o, "The value at the stack next-to-top is not of type 'int', but of type '"+stack().peek(1)+"'.");
		}
	}

	/**
	 * Ensures the specific preconditions of the said instruction.
	 */
	public void visitJSR(InstructionBranch o){
		// nothing to do here.
	}

	/**
	 * Ensures the specific preconditions of the said instruction.
	 */
	public void visitJSR_W(InstructionBranch o){
		// nothing to do here.
	}

	/**
	 * Ensures the specific preconditions of the said instruction.
	 */
	public void visitL2D(Instruction o){
		if (stack().peek() != Type.LONG){
			constraintViolated(o, "The value at the stack top is not of type 'long', but of type '"+stack().peek()+"'.");
		}
	}

	/**
	 * Ensures the specific preconditions of the said instruction.
	 */
	public void visitL2F(Instruction o){
		if (stack().peek() != Type.LONG){
			constraintViolated(o, "The value at the stack top is not of type 'long', but of type '"+stack().peek()+"'.");
		}
	}

	/**
	 * Ensures the specific preconditions of the said instruction.
	 */
	public void visitL2I(Instruction o){
		if (stack().peek() != Type.LONG){
			constraintViolated(o, "The value at the stack top is not of type 'long', but of type '"+stack().peek()+"'.");
		}
	}

	/**
	 * Ensures the specific preconditions of the said instruction.
	 */
	public void visitLADD(Instruction o){
		if (stack().peek() != Type.LONG){
			constraintViolated(o, "The value at the stack top is not of type 'long', but of type '"+stack().peek()+"'.");
		}
		if (stack().peek(1) != Type.LONG){
			constraintViolated(o, "The value at the stack next-to-top is not of type 'long', but of type '"+stack().peek(1)+"'.");
		}
	}

	/**
	 * Ensures the specific preconditions of the said instruction.
	 */
	public void visitLALOAD(Instruction o){
		indexOfInt(o, stack().peek());
		if (stack().peek(1) == Type.NULL){
			return;
		} 
		if (! (stack().peek(1) instanceof ArrayType)){
			constraintViolated(o, "Stack next-to-top must be of type long[] but is '"+stack().peek(1)+"'.");
		}
		Type t = ((ArrayType) (stack().peek(1))).getBasicType();
		if (t != Type.LONG){
			constraintViolated(o, "Stack next-to-top must be of type long[] but is '"+stack().peek(1)+"'.");
		}
	}

	/**
	 * Ensures the specific preconditions of the said instruction.
	 */
	public void visitLAND(Instruction o){
		if (stack().peek() != Type.LONG){
			constraintViolated(o, "The value at the stack top is not of type 'long', but of type '"+stack().peek()+"'.");
		}
		if (stack().peek(1) != Type.LONG){
			constraintViolated(o, "The value at the stack next-to-top is not of type 'long', but of type '"+stack().peek(1)+"'.");
		}
	}

	/**
	 * Ensures the specific preconditions of the said instruction.
	 */
	public void visitLASTORE(Instruction o){
		if (stack().peek() != Type.LONG){
			constraintViolated(o, "The value at the stack top is not of type 'long', but of type '"+stack().peek()+"'.");
		}
		indexOfInt(o, stack().peek(1));
		if (stack().peek(2) == Type.NULL){
			return;
		} 
		if (! (stack().peek(2) instanceof ArrayType)){
			constraintViolated(o, "Stack next-to-next-to-top must be of type long[] but is '"+stack().peek(2)+"'.");
		}
		Type t = ((ArrayType) (stack().peek(2))).getBasicType();
		if (t != Type.LONG){
			constraintViolated(o, "Stack next-to-next-to-top must be of type long[] but is '"+stack().peek(2)+"'.");
		}
	}

	/**
	 * Ensures the specific preconditions of the said instruction.
	 */
	public void visitLCMP(Instruction o){
		if (stack().peek() != Type.LONG){
			constraintViolated(o, "The value at the stack top is not of type 'long', but of type '"+stack().peek()+"'.");
		}
		if (stack().peek(1) != Type.LONG){
			constraintViolated(o, "The value at the stack next-to-top is not of type 'long', but of type '"+stack().peek(1)+"'.");
		}
	}

	/**
	 * Ensures the specific preconditions of the said instruction.
	 */
	public void visitLCONST(Instruction o){
		// Nothing to do here.
	}

	/**
	 * Ensures the specific preconditions of the said instruction.
	 */
	public void visitLDC(Instruction o){
		// visitCPInstruction is called first.
		
		Constant c = cpg.getConstant(o.getIndex());
		if 	(!	(	( c instanceof ConstantInteger) ||
							( c instanceof ConstantFloat	)	||
							( c instanceof ConstantString )	)	){
			constraintViolated(o, "Referenced constant should be a CONSTANT_Integer, a CONSTANT_Float or a CONSTANT_String, but is '"+c+"'.");
		}
	}

	/**
	 * Ensures the specific preconditions of the said instruction.
	 */
	public void visitLDC_W(Instruction o){
		// visitCPInstruction is called first.
		
		Constant c = cpg.getConstant(o.getIndex());
		if 	(!	(	( c instanceof ConstantInteger) ||
							( c instanceof ConstantFloat	)	||
							( c instanceof ConstantString )	)	){
			constraintViolated(o, "Referenced constant should be a CONSTANT_Integer, a CONSTANT_Float or a CONSTANT_String, but is '"+c+"'.");
		}
	}

	/**
	 * Ensures the specific preconditions of the said instruction.
	 */
	public void visitLDC2_W(Instruction o){
		// visitCPInstruction is called first.
		
		Constant c = cpg.getConstant(o.getIndex());
		if 	(!	(	( c instanceof ConstantLong) ||
							( c instanceof ConstantDouble )	)	){
			constraintViolated(o, "Referenced constant should be a CONSTANT_Integer, a CONSTANT_Float or a CONSTANT_String, but is '"+c+"'.");
		}
	}

	/**
	 * Ensures the specific preconditions of the said instruction.
	 */
	public void visitLDIV(Instruction o){
		if (stack().peek() != Type.LONG){
			constraintViolated(o, "The value at the stack top is not of type 'long', but of type '"+stack().peek()+"'.");
		}
		if (stack().peek(1) != Type.LONG){
			constraintViolated(o, "The value at the stack next-to-top is not of type 'long', but of type '"+stack().peek(1)+"'.");
		}
	}

	/**
	 * Ensures the specific preconditions of the said instruction.
	 */
	public void visitLLOAD(Instruction o){
		//visitLoadInstruction(LoadInstruction) is called before.
		
		// Nothing else needs to be done here.
	}

	/**
	 * Ensures the specific preconditions of the said instruction.
	 */
	public void visitLMUL(Instruction o){
		if (stack().peek() != Type.LONG){
			constraintViolated(o, "The value at the stack top is not of type 'long', but of type '"+stack().peek()+"'.");
		}
		if (stack().peek(1) != Type.LONG){
			constraintViolated(o, "The value at the stack next-to-top is not of type 'long', but of type '"+stack().peek(1)+"'.");
		}
	}

	public void visitLNEG(Instruction o){
		checkTop(o,Type.LONG);
	}
	
	public void visitLOOKUPSWITCH(LOOKUPSWITCH o){
		checkTop(o,Type.INT);
		// See also pass 3a.
	}

	public void visitLOR(Instruction o){
		if (stack().peek() != Type.LONG){
			constraintViolated(o, "The value at the stack top is not of type 'long', but of type '"+stack().peek()+"'.");
		}
		if (stack().peek(1) != Type.LONG){
			constraintViolated(o, "The value at the stack next-to-top is not of type 'long', but of type '"+stack().peek(1)+"'.");
		}
	}

	public void visitLREM(Instruction o){
		checkTop(o,Type.LONG);
		if (stack().peek(1) != Type.LONG){
			constraintViolated(o, "The value at the stack next-to-top is not of type 'long', but of type '"+stack().peek(1)+"'.");
		}
	}

	public void visitLRETURN(Instruction o) { checkTop(o,Type.LONG); }
	public void visitLSHL(Instruction o){
		checkTop(o,Type.INT);
		if (stack().peek(1) != Type.LONG){
			constraintViolated(o, "The value at the stack next-to-top is not of type 'long', but of type '"+stack().peek(1)+"'.");
		}
	}

	/**
	 * Ensures the specific preconditions of the said instruction.
	 */
	public void visitLSHR(Instruction o){
		if (stack().peek() != Type.INT){
			constraintViolated(o, "The value at the stack top is not of type 'int', but of type '"+stack().peek()+"'.");
		}
		if (stack().peek(1) != Type.LONG){
			constraintViolated(o, "The value at the stack next-to-top is not of type 'long', but of type '"+stack().peek(1)+"'.");
		}
	}
	
	/**
	 * Ensures the specific preconditions of the said instruction.
	 */
	public void visitLSTORE(Instruction o){
		//visitStoreInstruction(StoreInstruction) is called before.
		
		// Nothing else needs to be done here.
	}

	/**
	 * Ensures the specific preconditions of the said instruction.
	 */
	public void visitLSUB(Instruction o){
		checkTop(o,Type.LONG);
		if (stack().peek(1) != Type.LONG){
			constraintViolated(o, "The value at the stack next-to-top is not of type 'long', but of type '"+stack().peek(1)+"'.");
		}
	}

	/**
	 * Ensures the specific preconditions of the said instruction.
	 */
	public void visitLUSHR(Instruction o){
		checkTop(o,Type.INT);
		if (stack().peek(1) != Type.LONG){
			constraintViolated(o, "The value at the stack next-to-top is not of type 'long', but of type '"+stack().peek(1)+"'.");
		}
	}

	/**
	 * Ensures the specific preconditions of the said instruction.
	 */
	public void visitLXOR(Instruction o){
		checkTop(o,Type.LONG);
		if (stack().peek(1) != Type.LONG){
			constraintViolated(o, "The value at the stack next-to-top is not of type 'long', but of type '"+stack().peek(1)+"'.");
		}
	}

	/**
	 * Ensures the specific preconditions of the said instruction.
	 */
	public void visitMONITORENTER(Instruction o){
		if (! ((stack().peek()) instanceof ReferenceType)){
			constraintViolated(o, "The stack top should be of a ReferenceType, but is '"+stack().peek()+"'.");
		}
		referenceTypeIsInitialized(o, (ReferenceType) (stack().peek()) );
	}

	/**
	 * Ensures the specific preconditions of the said instruction.
	 */
	public void visitMONITOREXIT(Instruction o){
		if (! ((stack().peek()) instanceof ReferenceType)){
			constraintViolated(o, "The stack top should be of a ReferenceType, but is '"+stack().peek()+"'.");
		}
		referenceTypeIsInitialized(o, (ReferenceType) (stack().peek()) );
	}

	/**
	 * Ensures the specific preconditions of the said instruction.
	 */
	public void visitMULTIANEWARRAY(MULTIANEWARRAY o){
		int dimensions = o.getDimensions();
		// Dimensions argument is okay: see Pass 3a.
		for (int i=0; i<dimensions; i++){
			if (stack().peek(i) != Type.INT){
				constraintViolated(o, "The '"+dimensions+"' upper stack types should be 'int' but aren't.");
			}
		}
		// The runtime constant pool item at that index must be a symbolic reference to a class,
		// array, or interface type. See Pass 3a.
	}

	/**
	 * Ensures the specific preconditions of the said instruction.
	 */
	public void visitNEW(Instruction o){
		//visitCPInstruction(CPInstruction) has been called before.
		//visitLoadClass(LoadClass) has been called before.
		
		Type t = o.getType(cpg);
		if (! (t instanceof ReferenceType)){
			throw new AssertionViolatedException("NEW.getType() returning a non-reference type?!");
		}
		if (! (t instanceof ObjectType)){
			constraintViolated(o, "Expecting a class type (ObjectType) to work on. Found: '"+t+"'.");
		}
		ObjectType obj = (ObjectType) t;

		//e.g.: Don't instantiate interfaces
		if (! obj.referencesClass()){
			constraintViolated(o, "Expecting a class type (ObjectType) to work on. Found: '"+obj+"'.");
		}		
	}

	public void visitNEWARRAY(Instruction o) { checkTop(o,Type.INT); }
	public void visitNOP(Instruction o){ /* nothing is to be done here */ }

	public void visitPOP(Instruction o){
		if (stack().peek().getSize() != 1){
			constraintViolated(o, "Stack top size should be 1 but stack top is '"+stack().peek()+"' of size '"+stack().peek().getSize()+"'.");
		}
	}

	/**
	 * Ensures the specific preconditions of the said instruction.
	 */
	public void visitPOP2(Instruction o){
		if (stack().peek().getSize() != 2){
			constraintViolated(o, "Stack top size should be 2 but stack top is '"+stack().peek()+"' of size '"+stack().peek().getSize()+"'.");
		}
	}

	/**
	 * Ensures the specific preconditions of the said instruction.
	 */
	public void visitPUTFIELD(FieldInstruction o){

		Type objectref = stack().peek(1);
		if (! ( (objectref instanceof ObjectType) || (objectref == Type.NULL) ) ){
			constraintViolated(o, "Stack next-to-top should be an object reference that's not an array reference, but is '"+objectref+"'.");
		}
		
		String field_name = o.getFieldName(cpg);
		
		JavaClass jc = Repository.lookupClass(o.getClassType(cpg).getClassName());
		Field[] fields = jc.getFields();
		Field f = null;
		for (int i=0; i<fields.length; i++){
			if (fields[i].getName().equals(field_name)){
				f = fields[i];
				break;
			}
		}
		if (f == null){
			throw new AssertionViolatedException("Field not found?!?");
		}

		Type value = stack().peek();
		Type t = Type.getType(f.getSignature());
		Type shouldbe = t;
		if (shouldbe == Type.BOOLEAN ||
				shouldbe == Type.BYTE ||
				shouldbe == Type.CHAR ||
				shouldbe == Type.SHORT){
			shouldbe = Type.INT;
		}
		if (t instanceof ReferenceType){
			ReferenceType rvalue = null;
			if (value instanceof ReferenceType){
				rvalue = (ReferenceType) value;
				referenceTypeIsInitialized(o, rvalue);
			}
			else{
				constraintViolated(o, "The stack top type '"+value+"' is not of a reference type as expected.");
			}
			// TODO: This can possibly only be checked using Staerk-et-al's "set-of-object types", not
			// using "wider cast object types" created during verification.
			// Comment it out if you encounter problems. See also the analogon at visitPUTSTATIC.
			if (!(rvalue.isAssignmentCompatibleWith(shouldbe))){
				constraintViolated(o, "The stack top type '"+value+"' is not assignment compatible with '"+shouldbe+"'.");
			}
		}
		else{
			if (shouldbe != value){
				constraintViolated(o, "The stack top type '"+value+"' is not of type '"+shouldbe+"' as expected.");
			}
		}
		
		if (f.isProtected()){
			ObjectType classtype = o.getClassType(cpg);
			ObjectType curr = new ObjectType(mg.getClassName());

			if (	classtype.equals(curr) ||
						curr.subclassOf(classtype)	){
				Type tp = stack().peek(1);
				if (tp == Type.NULL){
					return;
				}
				if (! (tp instanceof ObjectType) ){
					constraintViolated(o, "The 'objectref' must refer to an object that's not an array. Found instead: '"+tp+"'.");
				}
				ObjectType objreftype = (ObjectType) tp;
				if (! ( objreftype.equals(curr) ||
						    objreftype.subclassOf(curr) ) ){
					constraintViolated(o, "The referenced field has the ACC_PROTECTED modifier, and it's a member of the current class or a superclass of the current class. However, the referenced object type '"+stack().peek()+"' is not the current class or a subclass of the current class.");
				}
			} 
		}

		// TODO: Could go into Pass 3a.
		if (f.isStatic()){
			constraintViolated(o, "Referenced field '"+f+"' is static which it shouldn't be.");
		}
	}

	/**
	 * Ensures the specific preconditions of the said instruction.
	 */
	public void visitPUTSTATIC(FieldInstruction o){
		String field_name = o.getFieldName(cpg);
		JavaClass jc = Repository.lookupClass(o.getClassType(cpg).getClassName());
		Field[] fields = jc.getFields();
		Field f = null;
		for (int i=0; i<fields.length; i++){
			if (fields[i].getName().equals(field_name)){
				f = fields[i];
				break;
			}
		}
		if (f == null){
			throw new AssertionViolatedException("Field not found?!?");
		}
		Type value = stack().peek();
		Type t = Type.getType(f.getSignature());
		Type shouldbe = t;
		if (shouldbe == Type.BOOLEAN ||
				shouldbe == Type.BYTE ||
				shouldbe == Type.CHAR ||
				shouldbe == Type.SHORT){
			shouldbe = Type.INT;
		}
		if (t instanceof ReferenceType){
			ReferenceType rvalue = null;
			if (value instanceof ReferenceType){
				rvalue = (ReferenceType) value;
				referenceTypeIsInitialized(o, rvalue);
			}
			else{
				constraintViolated(o, "The stack top type '"+value+"' is not of a reference type as expected.");
			}
			// TODO: This can possibly only be checked using Staerk-et-al's "set-of-object types", not
			// using "wider cast object types" created during verification.
			// Comment it out if you encounter problems. See also the analogon at visitPUTFIELD.
			if (!(rvalue.isAssignmentCompatibleWith(shouldbe))){
				constraintViolated(o, "The stack top type '"+value+"' is not assignment compatible with '"+shouldbe+"'.");
			}
		}
		else{
			if (shouldbe != value){
				constraintViolated(o, "The stack top type '"+value+"' is not of type '"+shouldbe+"' as expected.");
			}
		}
		// TODO: Interface fields may be assigned to only once. (Hard to implement in
		//       JustIce's execution model). This may only happen in <clinit>, see Pass 3a.
	}

	/**
	 * Ensures the specific preconditions of the said instruction.
	 */
	public void visitRET(RET o){
		if (! (locals().get(o.getIndex()) instanceof ReturnaddressType)){
			constraintViolated(o, "Expecting a ReturnaddressType in local variable "+o.getIndex()+".");
		}
		if (locals().get(o.getIndex()) == ReturnaddressType.NO_TARGET){
			throw new AssertionViolatedException("Oops: RET expecting a target!");
		}
		// Other constraints such as non-allowed overlapping subroutines are enforced
		// while building the Subroutines data structure.
	}

	/**
	 * Ensures the specific preconditions of the said instruction.
	 */
	public void visitRETURN(Instruction o){
		if (mg.getName().equals(Constants.CONSTRUCTOR_NAME)){// If we leave an <init> method
			if ((Frame._this != null) && (!(mg.getClassName().equals(Type.OBJECT.getClassName()))) ) {
				constraintViolated(o, "Leaving a constructor that itself did not call a constructor.");
			}
		}
	}

	/**
	 * Ensures the specific preconditions of the said instruction.
	 */
	public void visitSALOAD(Instruction o){
		indexOfInt(o, stack().peek());
		if (stack().peek(1) == Type.NULL){
			return;
		} 
		if (! (stack().peek(1) instanceof ArrayType)){
			constraintViolated(o, "Stack next-to-top must be of type short[] but is '"+stack().peek(1)+"'.");
		}
		Type t = ((ArrayType) (stack().peek(1))).getBasicType();
		if (t != Type.SHORT){
			constraintViolated(o, "Stack next-to-top must be of type short[] but is '"+stack().peek(1)+"'.");
		}
	}

	/**
	 * Ensures the specific preconditions of the said instruction.
	 */
	public void visitSASTORE(Instruction o){
		if (stack().peek() != Type.INT){
			constraintViolated(o, "The value at the stack top is not of type 'int', but of type '"+stack().peek()+"'.");
		}
		indexOfInt(o, stack().peek(1));
		if (stack().peek(2) == Type.NULL){
			return;
		} 
		if (! (stack().peek(2) instanceof ArrayType)){
			constraintViolated(o, "Stack next-to-next-to-top must be of type short[] but is '"+stack().peek(2)+"'.");
		}
		Type t = ((ArrayType) (stack().peek(2))).getBasicType();
		if (t != Type.SHORT){
			constraintViolated(o, "Stack next-to-next-to-top must be of type short[] but is '"+stack().peek(2)+"'.");
		}
	}

	/**
	 * Ensures the specific preconditions of the said instruction.
	 */
	public void visitSIPUSH(Instruction o){
		// nothing to do here. Generic visitXXX() methods did the trick before.
	}

	/**
	 * Ensures the specific preconditions of the said instruction.
	 */
	public void visitSWAP(Instruction o){
		if (stack().peek().getSize() != 1){
			constraintViolated(o, "The value at the stack top is not of size '1', but of size '"+stack().peek().getSize()+"'.");
		}
		if (stack().peek(1).getSize() != 1){
			constraintViolated(o, "The value at the stack next-to-top is not of size '1', but of size '"+stack().peek(1).getSize()+"'.");
		}
	}

	/**
	 * Ensures the specific preconditions of the said instruction.
	 */
	public void visitTABLESWITCH(TABLESWITCH o){
		indexOfInt(o, stack().peek());
		// See Pass 3a.
	}

}

