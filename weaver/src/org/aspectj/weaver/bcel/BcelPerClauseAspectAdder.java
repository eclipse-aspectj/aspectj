/*******************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/
package org.aspectj.weaver.bcel;

import org.aspectj.weaver.ResolvedTypeMunger;
import org.aspectj.weaver.ResolvedTypeX;
import org.aspectj.weaver.NameMangler;
import org.aspectj.weaver.ResolvedMember;
import org.aspectj.weaver.Member;
import org.aspectj.weaver.AjcMemberMaker;
import org.aspectj.weaver.World;
import org.aspectj.weaver.TypeX;
import org.aspectj.weaver.patterns.PerClause;
import org.aspectj.apache.bcel.classfile.Field;
import org.aspectj.apache.bcel.classfile.Method;
import org.aspectj.apache.bcel.generic.FieldGen;
import org.aspectj.apache.bcel.generic.InstructionList;
import org.aspectj.apache.bcel.generic.GETSTATIC;
import org.aspectj.apache.bcel.generic.InstructionFactory;
import org.aspectj.apache.bcel.generic.BranchInstruction;
import org.aspectj.apache.bcel.generic.IFEQ;
import org.aspectj.apache.bcel.generic.IFNONNULL;
import org.aspectj.apache.bcel.generic.InstructionHandle;
import org.aspectj.apache.bcel.generic.InstructionConstants;
import org.aspectj.apache.bcel.generic.PUSH;
import org.aspectj.apache.bcel.generic.ObjectType;
import org.aspectj.apache.bcel.generic.Type;
import org.aspectj.apache.bcel.generic.MethodGen;
import org.aspectj.apache.bcel.generic.NOP;
import org.aspectj.apache.bcel.generic.ALOAD;
import org.aspectj.apache.bcel.generic.ReferenceType;
import org.aspectj.apache.bcel.generic.DUP;
import org.aspectj.apache.bcel.generic.POP;
import org.aspectj.apache.bcel.generic.ATHROW;
import org.aspectj.apache.bcel.generic.ICONST;
import org.aspectj.apache.bcel.Constants;

import java.lang.reflect.Modifier;

/**
 * Adds aspectOf, hasAspect etc to the annotation defined aspects
 *
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
public class BcelPerClauseAspectAdder extends BcelTypeMunger {

    private PerClause.Kind kind;

    private boolean hasGeneratedInner = false;

    public BcelPerClauseAspectAdder(ResolvedTypeX aspect, PerClause.Kind kind) {
        super(null,aspect);
        this.kind = kind;
        if (kind == PerClause.SINGLETON) {
            // no inner needed
            hasGeneratedInner = true;
        }
    }

    public boolean munge(BcelClassWeaver weaver) {
        LazyClassGen gen = weaver.getLazyClassGen();

        // agressively generate the inner interface
        if (!hasGeneratedInner) {
            //if (kind == PerClause.PEROBJECT || kind == PerClause.PERCFLOW) {
                //inner class
                TypeX interfaceTypeX = AjcMemberMaker.perObjectInterfaceType(aspectType);
                LazyClassGen interfaceGen = new LazyClassGen(
                        interfaceTypeX.getName(),
                        "java.lang.Object",
                        null,
                        Constants.ACC_INTERFACE + Constants.ACC_PUBLIC + Constants.ACC_ABSTRACT,
                        new String[0]
                );
                interfaceGen.addMethodGen(makeMethodGen(interfaceGen, AjcMemberMaker.perObjectInterfaceGet(aspectType)));
                interfaceGen.addMethodGen(makeMethodGen(interfaceGen, AjcMemberMaker.perObjectInterfaceSet(aspectType)));
                //not really an inner class of it but that does not matter, we pass back to the LTW
                gen.addGeneratedInner(interfaceGen);

                hasGeneratedInner = true;
        }

        // Only munge the aspect type
         if (!gen.getType().equals(aspectType)) {
             return false;
         }

        generatePerClauseMembers(gen);

        if (kind == PerClause.SINGLETON) {
            generatePerSingletonAspectOfMethod(gen);
            generatePerSingletonHasAspectMethod(gen);
            generatePerSingletonAjcClinitMethod(gen);
        } else if (kind == PerClause.PEROBJECT) {
            generatePerObjectAspectOfMethod(gen);
            generatePerObjectHasAspectMethod(gen);
            generatePerObjectBindMethod(gen);
            generatePerObjectGetSetMethods(gen);
        } else if (kind == PerClause.PERCFLOW) {
            generatePerCflowAspectOfMethod(gen);
            generatePerCflowHasAspectMethod(gen);
            generatePerCflowPushMethod(gen);
            generatePerCflowAjcClinitMethod(gen);
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
        return true;//onType.equals(aspectType);
    }

    private void generatePerClauseMembers(LazyClassGen classGen) {
        //TODO handle when field already there - or handle it with / similar to isAnnotationDefinedAspect()
        // for that use aspectType and iterate on the fields.

        //TODO: percflowX is not using this one but AJ code style does generate it so..
        ResolvedMember failureFieldInfo = AjcMemberMaker.initFailureCauseField(aspectType);
        classGen.addField(makeFieldGen(classGen, failureFieldInfo).getField(), null);

        if (kind == PerClause.SINGLETON) {
            ResolvedMember perSingletonFieldInfo = AjcMemberMaker.perSingletonField(aspectType);
            classGen.addField(makeFieldGen(classGen, perSingletonFieldInfo).getField(), null);
        } else if (kind == PerClause.PEROBJECT) {
            ResolvedMember perObjectFieldInfo = AjcMemberMaker.perObjectField(aspectType, aspectType);
            classGen.addField(makeFieldGen(classGen, perObjectFieldInfo).getField(), null);

//            //inner class
//            TypeX interfaceTypeX = AjcMemberMaker.perObjectInterfaceType(aspectType);
//            LazyClassGen interfaceGen = new LazyClassGen(
//                    interfaceTypeX.getName(),
//                    "java.lang.Object",
//                    null,
//                    Constants.ACC_PRIVATE + Constants.ACC_ABSTRACT,
//                    new String[0]
//            );
//            interfaceGen.addMethodGen(makeMethodGen(interfaceGen, AjcMemberMaker.perObjectInterfaceGet(aspectType)));
//            interfaceGen.addMethodGen(makeMethodGen(interfaceGen, AjcMemberMaker.perObjectInterfaceSet(aspectType)));
//            classGen.addGeneratedInner(interfaceGen);
        } else if (kind == PerClause.PERCFLOW) {
            ResolvedMember perCflowFieldInfo = AjcMemberMaker.perCflowField(aspectType);
            classGen.addField(makeFieldGen(classGen, perCflowFieldInfo).getField(), null);
        } else {
            throw new RuntimeException("TODO not implemented yet");
        }
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

    private void generatePerObjectAspectOfMethod(LazyClassGen classGen) {
        InstructionFactory factory = classGen.getFactory();
        ReferenceType interfaceType = (ReferenceType) BcelWorld.makeBcelType(AjcMemberMaker.perObjectInterfaceType(aspectType));
        LazyMethodGen method = makeMethodGen(classGen, AjcMemberMaker.perObjectAspectOfMethod(aspectType));
        classGen.addMethodGen(method);

        InstructionList il = method.getBody();
        il.append(new ALOAD(0));
        il.append(factory.createInstanceOf(interfaceType));
        BranchInstruction ifEq = InstructionFactory.createBranchInstruction(Constants.IFEQ, null);
        il.append(ifEq);
        il.append(new ALOAD(0));
        il.append(factory.createCheckCast(interfaceType));
        il.append(Utility.createInvoke(factory, Constants.INVOKEINTERFACE, AjcMemberMaker.perObjectInterfaceGet(aspectType)));
        il.append(new DUP());
        BranchInstruction ifNull = InstructionFactory.createBranchInstruction(Constants.IFNULL, null);
        il.append(ifNull);
        il.append(InstructionFactory.createReturn(BcelWorld.makeBcelType(aspectType)));
        InstructionHandle ifNullElse = il.append(new POP());
        ifNull.setTarget(ifNullElse);
        InstructionHandle ifEqElse = il.append(factory.createNew(AjcMemberMaker.NO_ASPECT_BOUND_EXCEPTION.getName()));
        ifEq.setTarget(ifEqElse);
        il.append(new DUP());
        il.append(factory.createInvoke(AjcMemberMaker.NO_ASPECT_BOUND_EXCEPTION.getName(), "<init>", Type.VOID, Type.NO_ARGS, Constants.INVOKESPECIAL));
        il.append(new ATHROW());
    }

    private void generatePerObjectHasAspectMethod(LazyClassGen classGen) {
        InstructionFactory factory = classGen.getFactory();
        ReferenceType interfaceType = (ReferenceType) BcelWorld.makeBcelType(AjcMemberMaker.perObjectInterfaceType(aspectType));
        LazyMethodGen method = makeMethodGen(classGen, AjcMemberMaker.perObjectHasAspectMethod(aspectType));
        classGen.addMethodGen(method);

        InstructionList il = method.getBody();
        il.append(new ALOAD(0));
        il.append(factory.createInstanceOf(interfaceType));
        BranchInstruction ifEq = InstructionFactory.createBranchInstruction(Constants.IFEQ, null);
        il.append(ifEq);
        il.append(new ALOAD(0));
        il.append(factory.createCheckCast(interfaceType));
        il.append(Utility.createInvoke(factory, Constants.INVOKEINTERFACE, AjcMemberMaker.perObjectInterfaceGet(aspectType)));
        BranchInstruction ifNull = InstructionFactory.createBranchInstruction(Constants.IFNULL, null);
        il.append(ifNull);
        il.append(new ICONST(1));//TODO is ok ? else Push boolean
        il.append(InstructionFactory.createReturn(Type.INT));
        InstructionHandle ifEqElse = il.append(new ICONST(0));
        ifEq.setTarget(ifEqElse);
        ifNull.setTarget(ifEqElse);//??
        il.append(InstructionFactory.createReturn(Type.INT));
    }

    private void generatePerObjectBindMethod(LazyClassGen classGen) {
        InstructionFactory factory = classGen.getFactory();
        ReferenceType interfaceType = (ReferenceType) BcelWorld.makeBcelType(AjcMemberMaker.perObjectInterfaceType(aspectType));
        LazyMethodGen method = makeMethodGen(classGen, AjcMemberMaker.perObjectBind(aspectType));
        classGen.addMethodGen(method);

        InstructionList il = method.getBody();
        il.append(new ALOAD(0));
        il.append(factory.createInstanceOf(interfaceType));
        BranchInstruction ifEq = InstructionFactory.createBranchInstruction(Constants.IFEQ, null);
        il.append(ifEq);
        il.append(new ALOAD(0));
        il.append(factory.createCheckCast(interfaceType));
        il.append(Utility.createInvoke(factory, Constants.INVOKEINTERFACE, AjcMemberMaker.perObjectInterfaceGet(aspectType)));
        BranchInstruction ifNonNull = InstructionFactory.createBranchInstruction(Constants.IFNONNULL, null);
        il.append(ifNonNull);
        il.append(new ALOAD(0));
        il.append(factory.createCheckCast(interfaceType));
        il.append(factory.createNew(aspectType.getName()));
        il.append(InstructionConstants.DUP);
        il.append(factory.createInvoke(aspectType.getName(), "<init>", Type.VOID, Type.NO_ARGS, Constants.INVOKESPECIAL));
        il.append(Utility.createInvoke(factory, Constants.INVOKEINTERFACE, AjcMemberMaker.perObjectInterfaceSet(aspectType)));
        InstructionHandle end = il.append(InstructionFactory.createReturn(Type.VOID));
        ifEq.setTarget(end);
        ifNonNull.setTarget(end);
    }

    private void generatePerObjectGetSetMethods(LazyClassGen classGen) {
        InstructionFactory factory = classGen.getFactory();

        LazyMethodGen methodGet = makeMethodGen(classGen, AjcMemberMaker.perObjectInterfaceGet(aspectType));
        classGen.addMethodGen(methodGet);
        InstructionList ilGet = methodGet.getBody();
        ilGet = new InstructionList();
        ilGet.append(new ALOAD(0));
        ilGet.append(Utility.createGet(factory, AjcMemberMaker.perObjectField(aspectType, aspectType)));
        ilGet.append(InstructionFactory.createReturn(Type.OBJECT));

        LazyMethodGen methodSet = makeMethodGen(classGen, AjcMemberMaker.perObjectInterfaceSet(aspectType));
        classGen.addMethodGen(methodSet);
        InstructionList ilSet = methodSet.getBody();
        ilSet = new InstructionList();
        ilSet.append(new ALOAD(0));
        ilSet.append(new ALOAD(1));
        ilSet.append(Utility.createSet(factory, AjcMemberMaker.perObjectField(aspectType, aspectType)));
        ilSet.append(InstructionFactory.createReturn(Type.VOID));
    }

    private void generatePerCflowAspectOfMethod(LazyClassGen classGen) {
        InstructionFactory factory = classGen.getFactory();
        LazyMethodGen method = makeMethodGen(classGen, AjcMemberMaker.perCflowAspectOfMethod(aspectType));
        classGen.addMethodGen(method);

        InstructionList il = method.getBody();
        il.append(Utility.createGet(factory, AjcMemberMaker.perCflowField(aspectType)));
        il.append(Utility.createInvoke(factory, Constants.INVOKEVIRTUAL, AjcMemberMaker.cflowStackPeekInstance()));
        il.append(factory.createCheckCast((ReferenceType)BcelWorld.makeBcelType(aspectType)));
        il.append(InstructionFactory.createReturn(Type.OBJECT));
    }

    private void generatePerCflowHasAspectMethod(LazyClassGen classGen) {
        InstructionFactory factory = classGen.getFactory();
        LazyMethodGen method = makeMethodGen(classGen, AjcMemberMaker.perCflowHasAspectMethod(aspectType));
        classGen.addMethodGen(method);

        InstructionList il = method.getBody();
        il.append(Utility.createGet(factory, AjcMemberMaker.perCflowField(aspectType)));
        il.append(Utility.createInvoke(factory, Constants.INVOKEVIRTUAL, AjcMemberMaker.cflowStackIsValid()));
        il.append(InstructionFactory.createReturn(Type.INT));
    }

    private void generatePerCflowPushMethod(LazyClassGen classGen) {
        InstructionFactory factory = classGen.getFactory();
        LazyMethodGen method = makeMethodGen(classGen, AjcMemberMaker.perCflowPush(aspectType));
        classGen.addMethodGen(method);

        InstructionList il = method.getBody();
        il.append(Utility.createGet(factory, AjcMemberMaker.perCflowField(aspectType)));
        il.append(factory.createNew(aspectType.getName()));
        il.append(InstructionConstants.DUP);
        il.append(factory.createInvoke(aspectType.getName(), "<init>", Type.VOID, Type.NO_ARGS, Constants.INVOKESPECIAL));
        il.append(Utility.createInvoke(factory, Constants.INVOKEVIRTUAL, AjcMemberMaker.cflowStackPushInstance()));
        il.append(InstructionFactory.createReturn(Type.VOID));
    }

    private void generatePerCflowAjcClinitMethod(LazyClassGen classGen) {
        InstructionFactory factory = classGen.getFactory();
        LazyMethodGen method = makeMethodGen(classGen, AjcMemberMaker.ajcPreClinitMethod(aspectType));
        classGen.addMethodGen(method);

        InstructionList il = method.getBody();
        il.append(factory.createNew(AjcMemberMaker.CFLOW_STACK_TYPE.getName()));
        il.append(InstructionConstants.DUP);
        il.append(factory.createInvoke(AjcMemberMaker.CFLOW_STACK_TYPE.getName(), "<init>", Type.VOID, Type.NO_ARGS, Constants.INVOKESPECIAL));
        il.append(Utility.createSet(factory,  AjcMemberMaker.perCflowField(aspectType)));
        il.append(InstructionFactory.createReturn(Type.VOID));

        // patch <clinit> to delegate to ajc$preClinit at the beginning
        LazyMethodGen clinit = classGen.getStaticInitializer();
        il = new InstructionList();
        il.append(factory.createInvoke(aspectType.getName(), NameMangler.AJC_PRE_CLINIT_NAME, Type.VOID, Type.NO_ARGS, Constants.INVOKESTATIC));
        clinit.getBody().insert(il);
    }

}
