/*******************************************************************************
 * Copyright (c) 2005 BEA
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *    BEA - initial API and implementation
 *******************************************************************************/
package org.aspectj.weaver.bcel;

import org.aspectj.apache.bcel.Constants;
import org.aspectj.apache.bcel.generic.BranchInstruction;
import org.aspectj.apache.bcel.generic.InstructionConstants;
import org.aspectj.apache.bcel.generic.InstructionFactory;
import org.aspectj.apache.bcel.generic.InstructionHandle;
import org.aspectj.apache.bcel.generic.InstructionList;
import org.aspectj.apache.bcel.generic.NOP;
import org.aspectj.apache.bcel.generic.ObjectType;
import org.aspectj.apache.bcel.generic.PUSH;
import org.aspectj.apache.bcel.generic.Type;
import org.aspectj.weaver.AjcMemberMaker;
import org.aspectj.weaver.Member;
import org.aspectj.weaver.NameMangler;
import org.aspectj.weaver.ResolvedMember;
import org.aspectj.weaver.ResolvedTypeX;
import org.aspectj.weaver.patterns.PerClause;

/**
 * ALEX
 * Adds aspectOf, hasAspect etc to the annotation defined aspects
 *
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
public class BcelPerClauseAspectAdder extends BcelTypeMunger {

    private PerClause.Kind kind;

    public BcelPerClauseAspectAdder(ResolvedTypeX aspect, PerClause.Kind kind) {
        super(null,aspect);
        this.kind = kind;
    }

    public boolean munge(BcelClassWeaver weaver) {
        LazyClassGen gen = weaver.getLazyClassGen();

        // Only munge the aspect type
        if (!gen.getType().equals(aspectType)) return false;

        generatePerClauseMembers(gen);

        if (kind == PerClause.SINGLETON) {
            generatePerSingletonAspectOfMethod(gen);
            generatePerSingletonHasAspectMethod(gen);
            generatePerSingletonAjcClinitMethod(gen);
        } else {
            throw new RuntimeException("TODO not yet implemented perClause " + kind.getName());
        }
        return true;
    }


    public ResolvedMember getMatchingSyntheticMember(Member member) {
        //TODO is that ok ?
        return null;
    }

    public ResolvedMember getSignature() {
        // TODO what to do here ?
        return null;
        //throw new RuntimeException("not implemented - BcelPerClauseAspectAdder");
    }

    public boolean matches(ResolvedTypeX onType) {
        return onType.equals(aspectType);
    }

    private void generatePerClauseMembers(LazyClassGen classGen) {
        //TODO
        System.err.println("BcelPerClauseAspectAdder.generatePerClauseMembers");

        //TODO handle when field already there - or handle it with / similar to isSlowAspect()
        // for that use aspectType and iterate on the fields.
        ResolvedMember failureFieldInfo = AjcMemberMaker.initFailureCauseField(aspectType);
        classGen.addField(makeFieldGen(classGen, failureFieldInfo).getField(), null);

        if (kind == PerClause.SINGLETON) {
            ResolvedMember singletonFieldInfo = AjcMemberMaker.perSingletonField(aspectType);
            classGen.addField(makeFieldGen(classGen, singletonFieldInfo).getField(), null);
        } else {
            throw new RuntimeException("TODO not implemented yet");
        }
//        } else if (kind == PerClause.PERCFLOW) {
//            binding.addField(
//                factory.makeFieldBinding(
//                    AjcMemberMaker.perCflowField(
//                        typeX)));
//            methods[0] = new AspectClinit((Clinit)methods[0], compilationResult, true, false, null);
//        } else if (kind == PerClause.PEROBJECT) {
//            //ALEX really nothing there ?
//        } else if (kind == PerClause.PERTYPEWITHIN) {
//            //PTWIMPL Add field for storing typename in aspect for which the aspect instance exists
//            binding.addField(factory.makeFieldBinding(AjcMemberMaker.perTypeWithinWithinTypeField(typeX,typeX)));
//        } else {
//            throw new RuntimeException("unimplemented");
//        }
    }

    private void generatePerSingletonAspectOfMethod(LazyClassGen classGen) {
        InstructionFactory factory = classGen.getFactory();
        LazyMethodGen method = makeMethodGen(classGen, AjcMemberMaker.perSingletonAspectOfMethod(aspectType));
        classGen.addMethodGen(method);

        InstructionList il = method.getBody();
        il.append(Utility.createGet(factory, AjcMemberMaker.perSingletonField(aspectType)));
        BranchInstruction ifNotNull = InstructionFactory.createBranchInstruction(Constants.IFNONNULL, null);
        il.append(ifNotNull);
        il.append(factory.createNew(AjcMemberMaker.NO_ASPECT_BOUND_EXCEPTION.getName()));
        il.append(InstructionConstants.DUP);
        il.append(new PUSH(classGen.getConstantPoolGen(), aspectType.getName()));
        il.append(Utility.createGet(factory, AjcMemberMaker.initFailureCauseField(aspectType)));
        il.append(factory.createInvoke(AjcMemberMaker.NO_ASPECT_BOUND_EXCEPTION.getName(), "<init>", Type.VOID, new Type[] { Type.STRING, new ObjectType("java.lang.Throwable") }, Constants.INVOKESPECIAL));
        il.append(InstructionConstants.ATHROW);
        InstructionHandle ifElse = il.append(Utility.createGet(factory, AjcMemberMaker.perSingletonField(aspectType)));
        il.append(InstructionFactory.createReturn(Type.OBJECT));
        ifNotNull.setTarget(ifElse);
    }

    private void generatePerSingletonHasAspectMethod(LazyClassGen classGen) {
        InstructionFactory factory = classGen.getFactory();
        LazyMethodGen method = makeMethodGen(classGen, AjcMemberMaker.perSingletonHasAspectMethod(aspectType));
        classGen.addMethodGen(method);

        InstructionList il = method.getBody();
        il.append(Utility.createGet(factory, AjcMemberMaker.perSingletonField(aspectType)));
        BranchInstruction ifNull = InstructionFactory.createBranchInstruction(Constants.IFNULL, null);
        il.append(ifNull);
        il.append(new PUSH(classGen.getConstantPoolGen(), true));
        il.append(InstructionFactory.createReturn(Type.INT));
        InstructionHandle ifElse = il.append(new PUSH(classGen.getConstantPoolGen(), false));
        il.append(InstructionFactory.createReturn(Type.INT));
        ifNull.setTarget(ifElse);
    }


    private void generatePerSingletonAjcClinitMethod(LazyClassGen classGen) {
        InstructionFactory factory = classGen.getFactory();
        LazyMethodGen method = makeMethodGen(classGen, AjcMemberMaker.ajcPostClinitMethod(aspectType));
        classGen.addMethodGen(method);

        InstructionList il = method.getBody();
        il.append(factory.createNew(aspectType.getName()));
        il.append(InstructionConstants.DUP);
        il.append(factory.createInvoke(aspectType.getName(), "<init>", Type.VOID, Type.NO_ARGS, Constants.INVOKESPECIAL));
        il.append(Utility.createSet(factory,  AjcMemberMaker.perSingletonField(aspectType)));
        il.append(InstructionFactory.createReturn(Type.VOID));

        // patch <clinit> to delegate to ajc$postClinit at the end
        LazyMethodGen clinit = classGen.getStaticInitializer();
        il = new InstructionList();
        InstructionHandle tryStart = il.append(factory.createInvoke(aspectType.getName(), NameMangler.AJC_POST_CLINIT_NAME, Type.VOID, Type.NO_ARGS, Constants.INVOKESTATIC));
        BranchInstruction tryEnd = InstructionFactory.createBranchInstruction(Constants.GOTO, null);
        il.append(tryEnd);
        InstructionHandle handler = il.append(InstructionFactory.createStore(Type.OBJECT, 0));
        il.append(InstructionFactory.createLoad(Type.OBJECT, 0));
        il.append(Utility.createSet(factory, AjcMemberMaker.initFailureCauseField(aspectType)));
        il.append(InstructionFactory.createReturn(Type.VOID));
        tryEnd.setTarget(il.getEnd());

        // replace the original "return" with a "nop"
        clinit.getBody().getEnd().setInstruction(new NOP());
        clinit.getBody().append(il);

        clinit.addExceptionHandler(
            tryStart, handler, handler, new ObjectType("java.lang.Throwable"), false
        );
    }
}
