/* *******************************************************************
 * Copyright (c) 2011 Contributors
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Andy Clement - SpringSource/vmware
 * ******************************************************************/
package org.aspectj.weaver.bcel;

import org.aspectj.apache.bcel.Constants;
import org.aspectj.apache.bcel.generic.Instruction;
import org.aspectj.apache.bcel.generic.InstructionFactory;
import org.aspectj.apache.bcel.generic.InstructionList;
import org.aspectj.weaver.ResolvedType;

/**
 * Used to represent a variable reference to an aspect instance. This is used to support the if pointcut usage of
 * 'thisAspectInstance'. This variable does not have a slot, instead on requesting a reference we call aspectOf() on the aspect in
 * question to retrieve it. For now it only works with singleton aspects.
 */
public class AspectInstanceVar extends BcelVar {

	public AspectInstanceVar(ResolvedType type) {
		super(type, -1);
	}

	// fact is used in the subtypes
	public Instruction createLoad(InstructionFactory fact) {

		throw new IllegalStateException();
		// return InstructionFactory.createLoad(BcelWorld.makeBcelType(getType()), slot);
	}

	public Instruction createStore(InstructionFactory fact) {
		throw new IllegalStateException();
		// return InstructionFactory.createStore(BcelWorld.makeBcelType(getType()), slot);
	}

	public void appendStore(InstructionList il, InstructionFactory fact) {
		throw new IllegalStateException();
		// il.append(createStore(fact));
	}

	public void appendLoad(InstructionList il, InstructionFactory fact) {
		throw new IllegalStateException();
		// il.append(createLoad(fact));
	}

	public void appendLoadAndConvert(InstructionList il, InstructionFactory fact, ResolvedType toType) {
		throw new IllegalStateException();
		// il.append(createLoad(fact));
		// Utility.appendConversion(il, fact, getType(), toType);
	}

	public void insertLoad(InstructionList il, InstructionFactory fact) {
		InstructionList loadInstructions = new InstructionList();
		loadInstructions.append(fact.createInvoke(getType().getName(), "aspectOf", "()" + getType().getSignature(),
				Constants.INVOKESTATIC));
		il.insert(loadInstructions);
		// throw new IllegalStateException();
		// il.insert(createLoad(fact));
	}

	public InstructionList createCopyFrom(InstructionFactory fact, int oldSlot) {
		throw new IllegalStateException();
		// InstructionList il = new InstructionList();
		// il.append(InstructionFactory.createLoad(BcelWorld.makeBcelType(getType()), oldSlot));
		// il.append(createStore(fact));
		// return il;
	}

	// this is an array var
	void appendConvertableArrayLoad(InstructionList il, InstructionFactory fact, int index, ResolvedType convertTo) {
		throw new IllegalStateException();
		// ResolvedType convertFromType = getType().getResolvedComponentType();
		// appendLoad(il, fact);
		// il.append(Utility.createConstant(fact, index));
		// il.append(InstructionFactory.createArrayLoad(BcelWorld.makeBcelType(convertFromType)));
		// Utility.appendConversion(il, fact, convertFromType, convertTo);
	}

	void appendConvertableArrayStore(InstructionList il, InstructionFactory fact, int index, BcelVar storee) {
		throw new IllegalStateException();
		// ResolvedType convertToType = getType().getResolvedComponentType();
		// appendLoad(il, fact);
		// il.append(Utility.createConstant(fact, index));
		// storee.appendLoad(il, fact);
		// Utility.appendConversion(il, fact, storee.getType(), convertToType);
		// il.append(InstructionFactory.createArrayStore(BcelWorld.makeBcelType(convertToType)));
	}

	InstructionList createConvertableArrayStore(InstructionFactory fact, int index, BcelVar storee) {
		throw new IllegalStateException();
		// InstructionList il = new InstructionList();
		// appendConvertableArrayStore(il, fact, index, storee);
		// return il;
	}

	InstructionList createConvertableArrayLoad(InstructionFactory fact, int index, ResolvedType convertTo) {
		throw new IllegalStateException();
		// InstructionList il = new InstructionList();
		// appendConvertableArrayLoad(il, fact, index, convertTo);
		// return il;
	}

	public int getPositionInAroundState() {
		throw new IllegalStateException();
		// return positionInAroundState;
	}

	public void setPositionInAroundState(int positionInAroundState) {
		throw new IllegalStateException();
		// this.positionInAroundState = positionInAroundState;
	}
}
