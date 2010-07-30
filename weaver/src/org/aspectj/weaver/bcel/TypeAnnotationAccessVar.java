/* *******************************************************************
 * Copyright (c) 2005 IBM
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Andy Clement   initial implementation 
 * ******************************************************************/

package org.aspectj.weaver.bcel;

import org.aspectj.apache.bcel.Constants;
import org.aspectj.apache.bcel.generic.Instruction;
import org.aspectj.apache.bcel.generic.InstructionFactory;
import org.aspectj.apache.bcel.generic.InstructionList;
import org.aspectj.apache.bcel.generic.ObjectType;
import org.aspectj.apache.bcel.generic.Type;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.UnresolvedType;

/**
 * Used for @this() @target() @args() - represents accessing an annotated 'thing'. Main use is to create the instructions that
 * retrieve the annotation from the 'thing' - see createLoadInstructions()
 */
public class TypeAnnotationAccessVar extends BcelVar {

	private BcelVar target;

	public TypeAnnotationAccessVar(ResolvedType type, BcelVar theAnnotatedTargetIsStoredHere) {
		super(type, 0);
		target = theAnnotatedTargetIsStoredHere;
	}

	public String toString() {
		return "TypeAnnotationAccessVar(" + getType() + ")";
	}

	public Instruction createLoad(InstructionFactory fact) {
		throw new RuntimeException("unimplemented");
	}

	public Instruction createStore(InstructionFactory fact) {
		throw new RuntimeException("unimplemented");
	}

	public InstructionList createCopyFrom(InstructionFactory fact, int oldSlot) {
		throw new RuntimeException("unimplemented");
	}

	public void appendLoad(InstructionList il, InstructionFactory fact) {
		il.append(createLoadInstructions(getType(), fact));
	}

	public InstructionList createLoadInstructions(ResolvedType toType, InstructionFactory fact) {
		InstructionList il = new InstructionList();
		Type jlClass = BcelWorld.makeBcelType(UnresolvedType.JL_CLASS);
		Type jlaAnnotation = BcelWorld.makeBcelType(UnresolvedType.ANNOTATION);
		il.append(target.createLoad(fact));
		il.append(fact.createInvoke("java/lang/Object", "getClass", jlClass, new Type[] {}, Constants.INVOKEVIRTUAL));
		il.append(fact.createConstant(new ObjectType(toType.getName())));
		il.append(fact.createInvoke("java/lang/Class", "getAnnotation", jlaAnnotation, new Type[] { jlClass },
				Constants.INVOKEVIRTUAL));
		il.append(Utility.createConversion(fact, jlaAnnotation, BcelWorld.makeBcelType(toType)));
		return il;

	}

	public void appendLoadAndConvert(InstructionList il, InstructionFactory fact, ResolvedType toType) {
		il.append(createLoadInstructions(toType, fact));

	}

	public void insertLoad(InstructionList il, InstructionFactory fact) {
		il.insert(createLoadInstructions(getType(), fact));
	}

}
