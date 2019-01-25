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
import org.aspectj.apache.bcel.generic.FieldGen;
import org.aspectj.apache.bcel.generic.InstructionFactory;
import org.aspectj.apache.bcel.generic.InstructionList;
import org.aspectj.apache.bcel.generic.Type;
import org.aspectj.weaver.Member;
import org.aspectj.weaver.NameMangler;
import org.aspectj.weaver.ResolvedMember;
import org.aspectj.weaver.ResolvedType;

public class BcelCflowStackFieldAdder extends BcelTypeMunger {
	private ResolvedMember cflowStackField;

	public BcelCflowStackFieldAdder(ResolvedMember cflowStackField) {
		super(null, (ResolvedType) cflowStackField.getDeclaringType());
		this.cflowStackField = cflowStackField;
	}

	@Override
	public boolean munge(BcelClassWeaver weaver) {
		LazyClassGen gen = weaver.getLazyClassGen();
		if (!gen.getType().equals(cflowStackField.getDeclaringType())) {
			return false;
		}
		FieldGen f = new FieldGen(cflowStackField.getModifiers(), BcelWorld.makeBcelType(cflowStackField.getReturnType()),
				cflowStackField.getName(), gen.getConstantPool());
		gen.addField(f, getSourceLocation());

		LazyMethodGen clinit = gen.getAjcPreClinit(); // StaticInitializer();
		InstructionList setup = new InstructionList();
		InstructionFactory fact = gen.getFactory();

		setup.append(fact.createNew(NameMangler.CFLOW_STACK_TYPE));
		setup.append(InstructionFactory.createDup(1));
		setup.append(fact.createInvoke(NameMangler.CFLOW_STACK_TYPE, "<init>", Type.VOID, Type.NO_ARGS, Constants.INVOKESPECIAL));

		setup.append(Utility.createSet(fact, cflowStackField));
		clinit.getBody().insert(setup);

		return true;
	}

	@Override
	public ResolvedMember getMatchingSyntheticMember(Member member) {
		return null;
	}

	@Override
	public ResolvedMember getSignature() {
		return cflowStackField;
	}

	@Override
	public boolean matches(ResolvedType onType) {
		return onType.equals(cflowStackField.getDeclaringType());
	}

	@Override
	public boolean existsToSupportShadowMunging() {
		return true;
	}

}
