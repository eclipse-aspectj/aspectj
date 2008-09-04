/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     PARC     initial implementation 
 * ******************************************************************/

package org.aspectj.weaver.bcel;

import org.aspectj.apache.bcel.Constants;
import org.aspectj.apache.bcel.generic.Instruction;
import org.aspectj.apache.bcel.generic.InstructionFactory;
import org.aspectj.apache.bcel.generic.InstructionList;
import org.aspectj.apache.bcel.generic.Type;
import org.aspectj.weaver.Member;
import org.aspectj.weaver.NameMangler;
import org.aspectj.weaver.ResolvedType;

/**
 * XXX Erik and I need to discuss this hierarchy. Having FieldRef extend Var is convenient, but hopefully there's a better design.
 * 
 * This is always a static reference.
 */
public class BcelCflowAccessVar extends BcelVar {

	private Member stackField;
	private int index;

	/**
	 * @param type The type to convert to from Object
	 * @param stackField the member containing the CFLOW_STACK_TYPE
	 * @param index yeah yeah
	 */
	public BcelCflowAccessVar(ResolvedType type, Member stackField, int index) {
		super(type, 0);
		this.stackField = stackField;
		this.index = index;
	}

	public String toString() {
		return "BcelCflowAccessVar(" + getType() + " " + stackField + "." + index + ")";
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

		il.append(Utility.createGet(fact, stackField));
		il.append(Utility.createConstant(fact, index));
		il.append(fact.createInvoke(NameMangler.CFLOW_STACK_TYPE, "get", Type.OBJECT, new Type[] { Type.INT },
				Constants.INVOKEVIRTUAL));
		il.append(Utility.createConversion(fact, Type.OBJECT, BcelWorld.makeBcelType(toType)));

		return il;

	}

	public void appendLoadAndConvert(InstructionList il, InstructionFactory fact, ResolvedType toType) {
		il.append(createLoadInstructions(toType, fact));

	}

	public void insertLoad(InstructionList il, InstructionFactory fact) {
		il.insert(createLoadInstructions(getType(), fact));
	}

}
