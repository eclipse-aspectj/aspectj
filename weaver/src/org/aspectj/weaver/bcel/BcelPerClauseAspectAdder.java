/*******************************************************************************
 * Copyright (c) 2005 Contributors.
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * initial implementation              Alexandre Vasseur
 *******************************************************************************/
package org.aspectj.weaver.bcel;

import org.aspectj.apache.bcel.Constants;
import org.aspectj.apache.bcel.generic.ALOAD;
import org.aspectj.apache.bcel.generic.ATHROW;
import org.aspectj.apache.bcel.generic.BranchInstruction;
import org.aspectj.apache.bcel.generic.DUP;
import org.aspectj.apache.bcel.generic.ICONST;
import org.aspectj.apache.bcel.generic.InstructionConstants;
import org.aspectj.apache.bcel.generic.InstructionFactory;
import org.aspectj.apache.bcel.generic.InstructionHandle;
import org.aspectj.apache.bcel.generic.InstructionList;
import org.aspectj.apache.bcel.generic.NOP;
import org.aspectj.apache.bcel.generic.ObjectType;
import org.aspectj.apache.bcel.generic.POP;
import org.aspectj.apache.bcel.generic.PUSH;
import org.aspectj.apache.bcel.generic.ReferenceType;
import org.aspectj.apache.bcel.generic.Type;
import org.aspectj.apache.bcel.generic.MethodGen;
import org.aspectj.apache.bcel.generic.RETURN;
import org.aspectj.apache.bcel.generic.NEW;
import org.aspectj.apache.bcel.generic.INVOKESPECIAL;
import org.aspectj.apache.bcel.generic.ASTORE;
import org.aspectj.apache.bcel.generic.ACONST_NULL;
import org.aspectj.apache.bcel.generic.IRETURN;
import org.aspectj.weaver.AjcMemberMaker;
import org.aspectj.weaver.Member;
import org.aspectj.weaver.NameMangler;
import org.aspectj.weaver.ResolvedMember;
import org.aspectj.weaver.ResolvedTypeX;
import org.aspectj.weaver.TypeX;
import org.aspectj.weaver.patterns.PerClause;

import java.util.Iterator;

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

        // agressively generate the inner interface if any
        // Note: we do so because of the bug that leads to have this interface implemented by all classes and not
        // only those matched by the per clause, which fails under LTW since the very first class
        // gets weaved and impl this interface that is still not defined.
        if (!hasGeneratedInner) {
            //FIXME AV - restore test below or ?? + add test to detect such side effect
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
        } else if (kind == PerClause.PERTYPEWITHIN) {
            generatePerTWGetInstancesMethod(gen);
            generatePerTWAspectOfMethod(gen);
            generatePerTWHasAspectMethod(gen);
            generatePerTWGetInstanceMethod(gen);
            generatePerTWCreateAspectInstanceMethod(gen);
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
        //FIXME Alex handle when field already there - or handle it with / similar to isAnnotationDefinedAspect()
        // for that use aspectType and iterate on the fields.

        //FIXME Alex percflowX is not using this one but AJ code style does generate it so..
        ResolvedMember failureFieldInfo = AjcMemberMaker.initFailureCauseField(aspectType);
        classGen.addField(makeFieldGen(classGen, failureFieldInfo).getField(), null);

        if (kind == PerClause.SINGLETON) {
            ResolvedMember perSingletonFieldInfo = AjcMemberMaker.perSingletonField(aspectType);
            classGen.addField(makeFieldGen(classGen, perSingletonFieldInfo).getField(), null);
        } else if (kind == PerClause.PEROBJECT) {
            ResolvedMember perObjectFieldInfo = AjcMemberMaker.perObjectField(aspectType, aspectType);
            classGen.addField(makeFieldGen(classGen, perObjectFieldInfo).getField(), null);
            // if lazy generation of the inner interface MayHaveAspect works on LTW (see previous note)
            // it should be done here.
        } else if (kind == PerClause.PERCFLOW) {
            ResolvedMember perCflowFieldInfo = AjcMemberMaker.perCflowField(aspectType);
            classGen.addField(makeFieldGen(classGen, perCflowFieldInfo).getField(), null);
        } else if (kind == PerClause.PERTYPEWITHIN) {
            ResolvedMember perTypeWithinForField = AjcMemberMaker.perTypeWithinWithinTypeField(aspectType, aspectType);
            classGen.addField(makeFieldGen(classGen, perTypeWithinForField).getField(), null);
            ResolvedMember perTypeWithinPerClassMapField = AjcMemberMaker.perTypeWithinPerClassMapField(aspectType);
            classGen.addField(makeFieldGen(classGen, perTypeWithinPerClassMapField).getField(), null);
            // we need to initialize this map as a WeakHashMap in the aspect constructor(s)
            InstructionFactory factory = classGen.getFactory();
            for (Iterator iterator = classGen.getMethodGens().iterator(); iterator.hasNext();) {
                LazyMethodGen methodGen = (LazyMethodGen) iterator.next();
                if ("<init>".equals(methodGen.getName())) {
                    InstructionList il = new InstructionList();
                    il.append(InstructionConstants.ALOAD_0);
                    il.append(Utility.createGet(factory, perTypeWithinPerClassMapField));
                    BranchInstruction ifNotNull = InstructionFactory.createBranchInstruction(Constants.IFNONNULL, null);
                    il.append(ifNotNull);
                    il.append(InstructionConstants.ALOAD_0);
                    il.append(factory.createNew("java/util/WeakHashMap"));
                    il.append(new DUP());
                    il.append(factory.createInvoke(
                            "java/util/WeakHashMap",
                            "<init>",
                            Type.VOID,
                            Type.NO_ARGS,
                            Constants.INVOKESPECIAL
                    ));
                    il.append(Utility.createSet(factory, perTypeWithinPerClassMapField));
                    InstructionHandle currentEnd = methodGen.getBody().getEnd();
                    ifNotNull.setTarget(currentEnd);
                    methodGen.getBody().insert(currentEnd, il);
                }
            }
        } else {
            throw new Error("Should not happen - no such kind " + kind.toString());
        }
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
        InstructionHandle handler = il.append(InstructionConstants.ASTORE_0);
        il.append(InstructionConstants.ALOAD_0);
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
        il.append(InstructionConstants.ALOAD_0);
        il.append(factory.createInstanceOf(interfaceType));
        BranchInstruction ifEq = InstructionFactory.createBranchInstruction(Constants.IFEQ, null);
        il.append(ifEq);
        il.append(InstructionConstants.ALOAD_0);
        il.append(factory.createCheckCast(interfaceType));
        il.append(Utility.createInvoke(factory, Constants.INVOKEINTERFACE, AjcMemberMaker.perObjectInterfaceGet(aspectType)));
        il.append(InstructionConstants.DUP);
        BranchInstruction ifNull = InstructionFactory.createBranchInstruction(Constants.IFNULL, null);
        il.append(ifNull);
        il.append(InstructionFactory.createReturn(BcelWorld.makeBcelType(aspectType)));
        InstructionHandle ifNullElse = il.append(new POP());
        ifNull.setTarget(ifNullElse);
        InstructionHandle ifEqElse = il.append(factory.createNew(AjcMemberMaker.NO_ASPECT_BOUND_EXCEPTION.getName()));
        ifEq.setTarget(ifEqElse);
        il.append(InstructionConstants.DUP);
        il.append(factory.createInvoke(AjcMemberMaker.NO_ASPECT_BOUND_EXCEPTION.getName(), "<init>", Type.VOID, Type.NO_ARGS, Constants.INVOKESPECIAL));
        il.append(new ATHROW());
    }

    private void generatePerObjectHasAspectMethod(LazyClassGen classGen) {
        InstructionFactory factory = classGen.getFactory();
        ReferenceType interfaceType = (ReferenceType) BcelWorld.makeBcelType(AjcMemberMaker.perObjectInterfaceType(aspectType));
        LazyMethodGen method = makeMethodGen(classGen, AjcMemberMaker.perObjectHasAspectMethod(aspectType));
        classGen.addMethodGen(method);

        InstructionList il = method.getBody();
        il.append(InstructionConstants.ALOAD_0);
        il.append(factory.createInstanceOf(interfaceType));
        BranchInstruction ifEq = InstructionFactory.createBranchInstruction(Constants.IFEQ, null);
        il.append(ifEq);
        il.append(InstructionConstants.ALOAD_0);
        il.append(factory.createCheckCast(interfaceType));
        il.append(Utility.createInvoke(factory, Constants.INVOKEINTERFACE, AjcMemberMaker.perObjectInterfaceGet(aspectType)));
        BranchInstruction ifNull = InstructionFactory.createBranchInstruction(Constants.IFNULL, null);
        il.append(ifNull);
        il.append(InstructionConstants.ICONST_1);
        il.append(InstructionFactory.createReturn(Type.INT));
        InstructionHandle ifEqElse = il.append(InstructionConstants.ICONST_0);
        ifEq.setTarget(ifEqElse);
        ifNull.setTarget(ifEqElse);//??//FIXME AV - ok or what ?
        il.append(InstructionFactory.createReturn(Type.INT));
    }

    private void generatePerObjectBindMethod(LazyClassGen classGen) {
        InstructionFactory factory = classGen.getFactory();
        ReferenceType interfaceType = (ReferenceType) BcelWorld.makeBcelType(AjcMemberMaker.perObjectInterfaceType(aspectType));
        LazyMethodGen method = makeMethodGen(classGen, AjcMemberMaker.perObjectBind(aspectType));
        classGen.addMethodGen(method);

        InstructionList il = method.getBody();
        il.append(InstructionConstants.ALOAD_0);
        il.append(factory.createInstanceOf(interfaceType));
        BranchInstruction ifEq = InstructionFactory.createBranchInstruction(Constants.IFEQ, null);
        il.append(ifEq);
        il.append(InstructionConstants.ALOAD_0);
        il.append(factory.createCheckCast(interfaceType));
        il.append(Utility.createInvoke(factory, Constants.INVOKEINTERFACE, AjcMemberMaker.perObjectInterfaceGet(aspectType)));
        BranchInstruction ifNonNull = InstructionFactory.createBranchInstruction(Constants.IFNONNULL, null);
        il.append(ifNonNull);
        il.append(InstructionConstants.ALOAD_0);
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
        ilGet.append(InstructionConstants.ALOAD_0);
        ilGet.append(Utility.createGet(factory, AjcMemberMaker.perObjectField(aspectType, aspectType)));
        ilGet.append(InstructionFactory.createReturn(Type.OBJECT));

        LazyMethodGen methodSet = makeMethodGen(classGen, AjcMemberMaker.perObjectInterfaceSet(aspectType));
        classGen.addMethodGen(methodSet);
        InstructionList ilSet = methodSet.getBody();
        ilSet = new InstructionList();
        ilSet.append(InstructionConstants.ALOAD_0);
        ilSet.append(InstructionConstants.ALOAD_1);
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

    private void generatePerTWGetInstancesMethod(LazyClassGen classGen) {
        InstructionFactory factory = classGen.getFactory();
        LazyMethodGen method = makeMethodGen(classGen, AjcMemberMaker.perTypeWithinGetInstancesSet(aspectType));
        classGen.addMethodGen(method);

        InstructionList il = method.getBody();
        il.append(InstructionConstants.ALOAD_0);
        il.append(Utility.createGet(factory, AjcMemberMaker.perTypeWithinPerClassMapField(aspectType)));
        il.append(factory.createInvoke(
                "java/util/Map", "keySet", Type.getType("Ljava/util/Set;"), Type.NO_ARGS, Constants.INVOKEINTERFACE
        ));
        il.append(InstructionFactory.createReturn(Type.OBJECT));
    }

    private void generatePerTWAspectOfMethod(LazyClassGen classGen) {
        InstructionFactory factory = classGen.getFactory();
        LazyMethodGen method = makeMethodGen(classGen, AjcMemberMaker.perTypeWithinAspectOfMethod(aspectType));
        classGen.addMethodGen(method);

        InstructionList il = method.getBody();
        InstructionHandle tryStart = il.append(InstructionConstants.ALOAD_0);

        il.append(Utility.createInvoke(
                factory,
                Constants.INVOKESTATIC,
                AjcMemberMaker.perTypeWithinGetInstance(aspectType)
        ));
        il.append(InstructionConstants.ASTORE_1);
        il.append(InstructionConstants.ALOAD_1);
        BranchInstruction ifNonNull = InstructionFactory.createBranchInstruction(Constants.IFNONNULL, null);
        il.append(ifNonNull);
        il.append(factory.createNew(AjcMemberMaker.NO_ASPECT_BOUND_EXCEPTION.getName()));
        il.append(InstructionConstants.DUP);
        il.append(new PUSH(classGen.getConstantPoolGen(), aspectType.getName()));
        il.append(InstructionConstants.ACONST_NULL);
        il.append(factory.createInvoke(AjcMemberMaker.NO_ASPECT_BOUND_EXCEPTION.getName(), "<init>", Type.VOID, new Type[] { Type.STRING, new ObjectType("java.lang.Throwable") }, Constants.INVOKESPECIAL));
        il.append(InstructionConstants.ATHROW);
        InstructionHandle ifElse = il.append(InstructionConstants.ALOAD_1);
        ifNonNull.setTarget(ifElse);
        il.append(InstructionFactory.createReturn(Type.OBJECT));

        InstructionHandle handler = il.append(InstructionConstants.ASTORE_1);
        il.append(factory.createNew(AjcMemberMaker.NO_ASPECT_BOUND_EXCEPTION.getName()));
        il.append(InstructionConstants.DUP);
        il.append(factory.createInvoke(AjcMemberMaker.NO_ASPECT_BOUND_EXCEPTION.getName(), "<init>", Type.VOID, Type.NO_ARGS, Constants.INVOKESPECIAL));
        il.append(InstructionConstants.ATHROW);

        method.addExceptionHandler(
            tryStart, handler.getPrev(), handler, new ObjectType("java.lang.Exception"), false
        );
    }

    private void generatePerTWHasAspectMethod(LazyClassGen classGen) {
        InstructionFactory factory = classGen.getFactory();
        LazyMethodGen method = makeMethodGen(classGen, AjcMemberMaker.perTypeWithinHasAspectMethod(aspectType));
        classGen.addMethodGen(method);

        InstructionList il = method.getBody();
        InstructionHandle tryStart = il.append(InstructionConstants.ALOAD_0);
        il.append(Utility.createInvoke(
                factory,
                Constants.INVOKESTATIC,
                AjcMemberMaker.perTypeWithinGetInstance(aspectType)
        ));
        BranchInstruction ifNull = InstructionFactory.createBranchInstruction(Constants.IFNULL, null);
        il.append(ifNull);
        il.append(InstructionConstants.ICONST_1);
        il.append(InstructionConstants.IRETURN);
        InstructionHandle ifElse = il.append(InstructionConstants.ICONST_0);
        ifNull.setTarget(ifElse);
        il.append(InstructionConstants.IRETURN);

        InstructionHandle handler = il.append(InstructionConstants.ASTORE_1);
        il.append(InstructionConstants.ICONST_0);
        il.append(InstructionConstants.IRETURN);

        method.addExceptionHandler(
            tryStart, handler.getPrev(), handler, new ObjectType("java.lang.Exception"), false
        );
    }

    private void generatePerTWGetInstanceMethod(LazyClassGen classGen) {
        InstructionFactory factory = classGen.getFactory();
        LazyMethodGen method = makeMethodGen(classGen, AjcMemberMaker.perTypeWithinGetInstance(aspectType));
        classGen.addMethodGen(method);

        InstructionList il = method.getBody();
        InstructionHandle tryStart = il.append(InstructionConstants.ALOAD_0);
        il.append(new PUSH(factory.getConstantPool(), NameMangler.perTypeWithinLocalAspectOf(aspectType)));
        il.append(InstructionConstants.ACONST_NULL);//Class[] for "getDeclaredMethod"
        il.append(factory.createInvoke(
                "java/lang/Class",
                "getDeclaredMethod",
                Type.getType("Ljava/lang/reflect/Method;"),
                new Type[]{Type.getType("Ljava/lang/String;"), Type.getType("[Ljava/lang/Class;")},
                Constants.INVOKEVIRTUAL
        ));
        il.append(InstructionConstants.ACONST_NULL);//Object for "invoke", static method
        il.append(InstructionConstants.ACONST_NULL);//Object[] for "invoke", no arg
        il.append(factory.createInvoke(
                "java/lang/reflect/Method",
                "invoke",
                Type.OBJECT,
                new Type[]{Type.getType("Ljava/lang/Object;"), Type.getType("[Ljava/lang/Object;")},
                Constants.INVOKEVIRTUAL
        ));
        il.append(factory.createCheckCast((ReferenceType) BcelWorld.makeBcelType(aspectType)));
        il.append(InstructionConstants.ARETURN);

        InstructionHandle handler = il.append(InstructionConstants.ASTORE_1);
        il.append(InstructionConstants.ALOAD_1);
        il.append(InstructionConstants.ATHROW);

        method.addExceptionHandler(
            tryStart, handler.getPrev(), handler, new ObjectType("java.lang.Exception"), false
        );
    }

    private void generatePerTWCreateAspectInstanceMethod(LazyClassGen classGen) {
        InstructionFactory factory = classGen.getFactory();
        LazyMethodGen method = makeMethodGen(classGen, AjcMemberMaker.perTypeWithinCreateAspectInstance(aspectType));
        classGen.addMethodGen(method);

        InstructionList il = method.getBody();
        il.append(factory.createNew(aspectType.getName()));
        il.append(InstructionConstants.DUP);
        il.append(factory.createInvoke(
                aspectType.getName(), "<init>", Type.VOID, Type.NO_ARGS, Constants.INVOKESPECIAL
        ));
        il.append(InstructionConstants.ASTORE_1);
        il.append(InstructionConstants.ALOAD_1);
        il.append(InstructionConstants.ALOAD_0);
        il.append(Utility.createSet(
                factory,
                AjcMemberMaker.perTypeWithinWithinTypeField(aspectType, aspectType)
        ));
        il.append(InstructionConstants.ALOAD_1);
        il.append(InstructionConstants.ARETURN);
    }

}
