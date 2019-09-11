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

import java.util.List;

import org.aspectj.apache.bcel.Constants;
import org.aspectj.apache.bcel.generic.InstructionBranch;
import org.aspectj.apache.bcel.generic.InstructionConstants;
import org.aspectj.apache.bcel.generic.InstructionFactory;
import org.aspectj.apache.bcel.generic.InstructionHandle;
import org.aspectj.apache.bcel.generic.InstructionList;
import org.aspectj.apache.bcel.generic.ObjectType;
import org.aspectj.apache.bcel.generic.ReferenceType;
import org.aspectj.apache.bcel.generic.Type;
import org.aspectj.weaver.AjAttribute;
import org.aspectj.weaver.AjcMemberMaker;
import org.aspectj.weaver.Member;
import org.aspectj.weaver.NameMangler;
import org.aspectj.weaver.ResolvedMember;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.patterns.PerClause;

/**
 * Adds aspectOf(), hasAspect() etc to the annotation defined aspects
 *
 * @author Alexandre Vasseur
 * @author Andy Clement
 */
public class BcelPerClauseAspectAdder extends BcelTypeMunger {

	private final PerClause.Kind kind;

	private boolean hasGeneratedInner = false;

	public BcelPerClauseAspectAdder(ResolvedType aspect, PerClause.Kind kind) {
		super(null, aspect);
		this.kind = kind;
		if (kind == PerClause.SINGLETON || kind == PerClause.PERTYPEWITHIN || kind == PerClause.PERCFLOW) {
			// no inner needed
			hasGeneratedInner = true;
		}
	}

	public boolean munge(BcelClassWeaver weaver) {
		LazyClassGen gen = weaver.getLazyClassGen();

		doAggressiveInner(gen);

		// Only munge the aspect type
		if (!gen.getType().equals(aspectType)) {
			return false;
		}

		return doMunge(gen, true);
	}

	public boolean forceMunge(LazyClassGen gen, boolean checkAlreadyThere) {
		doAggressiveInner(gen);
		return doMunge(gen, checkAlreadyThere);
	}

	private void doAggressiveInner(LazyClassGen gen) {
		// agressively generate the inner interface if any
		// Note: we do so because of the bug #75442 that leads to have this interface implemented by all classes and not
		// only those matched by the per clause, which fails under LTW since the very first class
		// gets weaved and impl this interface that is still not defined.
		if (!hasGeneratedInner) {
			if (kind == PerClause.PEROBJECT) {// redundant test - see constructor, but safer
				// inner class
				UnresolvedType interfaceTypeX = AjcMemberMaker.perObjectInterfaceType(aspectType);
				LazyClassGen interfaceGen = new LazyClassGen(interfaceTypeX.getName(), "java.lang.Object", null,
						Constants.ACC_INTERFACE + Constants.ACC_PUBLIC + Constants.ACC_ABSTRACT, new String[0], getWorld());
				interfaceGen.addMethodGen(makeMethodGen(interfaceGen, AjcMemberMaker.perObjectInterfaceGet(aspectType)));
				interfaceGen.addMethodGen(makeMethodGen(interfaceGen, AjcMemberMaker.perObjectInterfaceSet(aspectType)));
				// not really an inner class of it but that does not matter, we pass back to the LTW
				gen.addGeneratedInner(interfaceGen);
			}
			hasGeneratedInner = true;
		}
	}

	private boolean doMunge(LazyClassGen gen, boolean checkAlreadyThere) {
		if (checkAlreadyThere && hasPerClauseMembersAlready(gen)) {
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
			// these will be added by the PerObjectInterface munger that affects the type - pr144602
			// generatePerObjectGetSetMethods(gen);
		} else if (kind == PerClause.PERCFLOW) {
			generatePerCflowAspectOfMethod(gen);
			generatePerCflowHasAspectMethod(gen);
			generatePerCflowPushMethod(gen);
			generatePerCflowAjcClinitMethod(gen);
		} else if (kind == PerClause.PERTYPEWITHIN) {
			generatePerTWAspectOfMethod(gen);
			generatePerTWHasAspectMethod(gen);
			generatePerTWGetInstanceMethod(gen);
			generatePerTWCreateAspectInstanceMethod(gen);
			generatePerTWGetWithinTypeNameMethod(gen);
		} else {
			throw new Error("should not happen - not such kind " + kind.getName());
		}
		return true;
	}

	public ResolvedMember getMatchingSyntheticMember(Member member) {
		return null;
	}

	public ResolvedMember getSignature() {
		return null;
	}

	public boolean matches(ResolvedType onType) {
		// cannot always do the right thing because may need to eagerly generate ajcMightHaveAspect interface for LTW (says Alex)
		if (hasGeneratedInner) { // pr237419 - not always going to generate the marker interface
			return aspectType.equals(onType);
		} else {
			return true;
		}
	}

	private boolean hasPerClauseMembersAlready(LazyClassGen classGen) {
		List<LazyMethodGen> methodGens = classGen.getMethodGens();
		for (LazyMethodGen method: methodGens) {
			if ("aspectOf".equals(method.getName())) {
				if ("()".equals(method.getParameterSignature()) && (kind == PerClause.SINGLETON || kind == PerClause.PERCFLOW)) {
					return true;
				} else if ("(Ljava/lang/Object;)".equals(method.getParameterSignature()) && kind == PerClause.PEROBJECT) {
					return true;
				} else if ("(Ljava/lang/Class;)".equals(method.getParameterSignature()) && kind == PerClause.PERTYPEWITHIN) {
					return true;
				}
			}
		}
		return false;
	}

	private void generatePerClauseMembers(LazyClassGen classGen) {
		// FIXME Alex handle when field already there - or handle it with / similar to isAnnotationDefinedAspect()
		// for that use aspectType and iterate on the fields.

		// FIXME Alex percflowX is not using this one but AJ code style does generate it so..
		ResolvedMember failureFieldInfo = AjcMemberMaker.initFailureCauseField(aspectType);
		if (kind == PerClause.SINGLETON) {
			classGen.addField(makeFieldGen(classGen, failureFieldInfo), null);
		}

		if (kind == PerClause.SINGLETON) {
			ResolvedMember perSingletonFieldInfo = AjcMemberMaker.perSingletonField(aspectType);
			classGen.addField(makeFieldGen(classGen, perSingletonFieldInfo), null);
			// pr144602 - don't need to do this, PerObjectInterface munger will do it
			// } else if (kind == PerClause.PEROBJECT) {
			// ResolvedMember perObjectFieldInfo = AjcMemberMaker.perObjectField(aspectType, aspectType);
			// classGen.addField(makeFieldGen(classGen, perObjectFieldInfo).(), null);
			// // if lazy generation of the inner interface MayHaveAspect works on LTW (see previous note)
			// // it should be done here.
		} else if (kind == PerClause.PERCFLOW) {
			ResolvedMember perCflowFieldInfo = AjcMemberMaker.perCflowField(aspectType);
			classGen.addField(makeFieldGen(classGen, perCflowFieldInfo), null);
		} else if (kind == PerClause.PERTYPEWITHIN) {
			ResolvedMember perTypeWithinForField = AjcMemberMaker.perTypeWithinWithinTypeField(aspectType, aspectType);
			classGen.addField(makeFieldGen(classGen, perTypeWithinForField), null);
		}
	}

	private void generatePerSingletonAspectOfMethod(LazyClassGen classGen) {
		InstructionFactory factory = classGen.getFactory();
		LazyMethodGen method = makeMethodGen(classGen, AjcMemberMaker.perSingletonAspectOfMethod(aspectType));
		flagAsSynthetic(method, false);
		classGen.addMethodGen(method);

		InstructionList il = method.getBody();
		il.append(Utility.createGet(factory, AjcMemberMaker.perSingletonField(aspectType)));
		InstructionBranch ifNotNull = InstructionFactory.createBranchInstruction(Constants.IFNONNULL, null);
		il.append(ifNotNull);
		il.append(factory.createNew(AjcMemberMaker.NO_ASPECT_BOUND_EXCEPTION.getName()));
		il.append(InstructionConstants.DUP);
		il.append(InstructionFactory.PUSH(classGen.getConstantPool(), aspectType.getName()));
		il.append(Utility.createGet(factory, AjcMemberMaker.initFailureCauseField(aspectType)));
		il.append(factory.createInvoke(AjcMemberMaker.NO_ASPECT_BOUND_EXCEPTION.getName(), "<init>", Type.VOID, new Type[] {
				Type.STRING, new ObjectType("java.lang.Throwable") }, Constants.INVOKESPECIAL));
		il.append(InstructionConstants.ATHROW);
		InstructionHandle ifElse = il.append(Utility.createGet(factory, AjcMemberMaker.perSingletonField(aspectType)));
		il.append(InstructionFactory.createReturn(Type.OBJECT));
		ifNotNull.setTarget(ifElse);
	}

	private void generatePerSingletonHasAspectMethod(LazyClassGen classGen) {
		InstructionFactory factory = classGen.getFactory();
		LazyMethodGen method = makeMethodGen(classGen, AjcMemberMaker.perSingletonHasAspectMethod(aspectType));
		flagAsSynthetic(method, false);
		classGen.addMethodGen(method);

		InstructionList il = method.getBody();
		il.append(Utility.createGet(factory, AjcMemberMaker.perSingletonField(aspectType)));
		InstructionBranch ifNull = InstructionFactory.createBranchInstruction(Constants.IFNULL, null);
		il.append(ifNull);
		il.append(InstructionFactory.PUSH(classGen.getConstantPool(), true));
		il.append(InstructionFactory.createReturn(Type.INT));
		InstructionHandle ifElse = il.append(InstructionFactory.PUSH(classGen.getConstantPool(), false));
		il.append(InstructionFactory.createReturn(Type.INT));
		ifNull.setTarget(ifElse);
	}

	private void generatePerSingletonAjcClinitMethod(LazyClassGen classGen) {
		InstructionFactory factory = classGen.getFactory();
		LazyMethodGen method = makeMethodGen(classGen, AjcMemberMaker.ajcPostClinitMethod(aspectType));
		flagAsSynthetic(method, true);
		classGen.addMethodGen(method);

		InstructionList il = method.getBody();
		il.append(factory.createNew(aspectType.getName()));
		il.append(InstructionConstants.DUP);
		il.append(factory.createInvoke(aspectType.getName(), "<init>", Type.VOID, Type.NO_ARGS, Constants.INVOKESPECIAL));
		il.append(Utility.createSet(factory, AjcMemberMaker.perSingletonField(aspectType)));
		il.append(InstructionFactory.createReturn(Type.VOID));

		// patch <clinit> to delegate to ajc$postClinit at the end
		LazyMethodGen clinit = classGen.getStaticInitializer();
		il = new InstructionList();
		InstructionHandle tryStart = il.append(factory.createInvoke(aspectType.getName(), NameMangler.AJC_POST_CLINIT_NAME,
				Type.VOID, Type.NO_ARGS, Constants.INVOKESTATIC));
		InstructionBranch tryEnd = InstructionFactory.createBranchInstruction(Constants.GOTO, null);
		il.append(tryEnd);
		InstructionHandle handler = il.append(InstructionConstants.ASTORE_0);
		il.append(InstructionConstants.ALOAD_0);
		il.append(Utility.createSet(factory, AjcMemberMaker.initFailureCauseField(aspectType)));
		il.append(InstructionFactory.createReturn(Type.VOID));
		tryEnd.setTarget(il.getEnd());

		// replace the original "return" with a "nop"
		// TODO AV - a bit odd, looks like Bcel alters bytecode and has a IMPDEP1 in its representation
		if (clinit.getBody().getEnd().getInstruction().opcode == Constants.IMPDEP1) {
			clinit.getBody().getEnd().getPrev().setInstruction(InstructionConstants.NOP);
		}
		clinit.getBody().getEnd().setInstruction(InstructionConstants.NOP);
		clinit.getBody().append(il);

		clinit.addExceptionHandler(tryStart, handler.getPrev(), handler, new ObjectType("java.lang.Throwable"), false);
	}

	private void generatePerObjectAspectOfMethod(LazyClassGen classGen) {
		InstructionFactory factory = classGen.getFactory();
		ReferenceType interfaceType = (ReferenceType) BcelWorld.makeBcelType(AjcMemberMaker.perObjectInterfaceType(aspectType));
		LazyMethodGen method = makeMethodGen(classGen, AjcMemberMaker.perObjectAspectOfMethod(aspectType));
		flagAsSynthetic(method, false);
		classGen.addMethodGen(method);

		InstructionList il = method.getBody();
		il.append(InstructionConstants.ALOAD_0);
		il.append(factory.createInstanceOf(interfaceType));
		InstructionBranch ifEq = InstructionFactory.createBranchInstruction(Constants.IFEQ, null);
		il.append(ifEq);
		il.append(InstructionConstants.ALOAD_0);
		il.append(factory.createCheckCast(interfaceType));
		il.append(Utility.createInvoke(factory, Constants.INVOKEINTERFACE, AjcMemberMaker.perObjectInterfaceGet(aspectType)));
		il.append(InstructionConstants.DUP);
		InstructionBranch ifNull = InstructionFactory.createBranchInstruction(Constants.IFNULL, null);
		il.append(ifNull);
		il.append(InstructionFactory.createReturn(BcelWorld.makeBcelType(aspectType)));
		InstructionHandle ifNullElse = il.append(InstructionConstants.POP);
		ifNull.setTarget(ifNullElse);
		InstructionHandle ifEqElse = il.append(factory.createNew(AjcMemberMaker.NO_ASPECT_BOUND_EXCEPTION.getName()));
		ifEq.setTarget(ifEqElse);
		il.append(InstructionConstants.DUP);
		il.append(factory.createInvoke(AjcMemberMaker.NO_ASPECT_BOUND_EXCEPTION.getName(), "<init>", Type.VOID, Type.NO_ARGS,
				Constants.INVOKESPECIAL));
		il.append(InstructionConstants.ATHROW);
	}

	private void generatePerObjectHasAspectMethod(LazyClassGen classGen) {
		InstructionFactory factory = classGen.getFactory();
		ReferenceType interfaceType = (ReferenceType) BcelWorld.makeBcelType(AjcMemberMaker.perObjectInterfaceType(aspectType));
		LazyMethodGen method = makeMethodGen(classGen, AjcMemberMaker.perObjectHasAspectMethod(aspectType));
		flagAsSynthetic(method, false);
		classGen.addMethodGen(method);

		InstructionList il = method.getBody();
		il.append(InstructionConstants.ALOAD_0);
		il.append(factory.createInstanceOf(interfaceType));
		InstructionBranch ifEq = InstructionFactory.createBranchInstruction(Constants.IFEQ, null);
		il.append(ifEq);
		il.append(InstructionConstants.ALOAD_0);
		il.append(factory.createCheckCast(interfaceType));
		il.append(Utility.createInvoke(factory, Constants.INVOKEINTERFACE, AjcMemberMaker.perObjectInterfaceGet(aspectType)));
		InstructionBranch ifNull = InstructionFactory.createBranchInstruction(Constants.IFNULL, null);
		il.append(ifNull);
		il.append(InstructionConstants.ICONST_1);
		il.append(InstructionFactory.createReturn(Type.INT));
		InstructionHandle ifEqElse = il.append(InstructionConstants.ICONST_0);
		ifEq.setTarget(ifEqElse);
		ifNull.setTarget(ifEqElse);
		il.append(InstructionFactory.createReturn(Type.INT));
	}

	private void generatePerObjectBindMethod(LazyClassGen classGen) {
		InstructionFactory factory = classGen.getFactory();
		ReferenceType interfaceType = (ReferenceType) BcelWorld.makeBcelType(AjcMemberMaker.perObjectInterfaceType(aspectType));
		LazyMethodGen method = makeMethodGen(classGen, AjcMemberMaker.perObjectBind(aspectType));
		flagAsSynthetic(method, true);
		classGen.addMethodGen(method);

		InstructionList il = method.getBody();
		il.append(InstructionConstants.ALOAD_0);
		il.append(factory.createInstanceOf(interfaceType));
		InstructionBranch ifEq = InstructionFactory.createBranchInstruction(Constants.IFEQ, null);
		il.append(ifEq);
		il.append(InstructionConstants.ALOAD_0);
		il.append(factory.createCheckCast(interfaceType));
		il.append(Utility.createInvoke(factory, Constants.INVOKEINTERFACE, AjcMemberMaker.perObjectInterfaceGet(aspectType)));
		InstructionBranch ifNonNull = InstructionFactory.createBranchInstruction(Constants.IFNONNULL, null);
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

	// private void generatePerObjectGetSetMethods(LazyClassGen classGen) {
	// InstructionFactory factory = classGen.getFactory();
	//
	// LazyMethodGen methodGet = makeMethodGen(classGen, AjcMemberMaker.perObjectInterfaceGet(aspectType));
	// flagAsSynthetic(methodGet, true);
	// classGen.addMethodGen(methodGet);
	// InstructionList ilGet = methodGet.getBody();
	// ilGet = new InstructionList();
	// ilGet.append(InstructionConstants.ALOAD_0);
	// ilGet.append(Utility.createGet(factory, AjcMemberMaker.perObjectField(aspectType, aspectType)));
	// ilGet.append(InstructionFactory.createReturn(Type.OBJECT));
	//
	// LazyMethodGen methodSet = makeMethodGen(classGen, AjcMemberMaker.perObjectInterfaceSet(aspectType));
	// flagAsSynthetic(methodSet, true);
	// classGen.addMethodGen(methodSet);
	// InstructionList ilSet = methodSet.getBody();
	// ilSet = new InstructionList();
	// ilSet.append(InstructionConstants.ALOAD_0);
	// ilSet.append(InstructionConstants.ALOAD_1);
	// ilSet.append(Utility.createSet(factory, AjcMemberMaker.perObjectField(aspectType, aspectType)));
	// ilSet.append(InstructionFactory.createReturn(Type.VOID));
	// }

	private void generatePerCflowAspectOfMethod(LazyClassGen classGen) {
		InstructionFactory factory = classGen.getFactory();
		LazyMethodGen method = makeMethodGen(classGen, AjcMemberMaker.perCflowAspectOfMethod(aspectType));
		flagAsSynthetic(method, false);
		classGen.addMethodGen(method);

		InstructionList il = method.getBody();
		il.append(Utility.createGet(factory, AjcMemberMaker.perCflowField(aspectType)));
		il.append(Utility.createInvoke(factory, Constants.INVOKEVIRTUAL, AjcMemberMaker.cflowStackPeekInstance()));
		il.append(factory.createCheckCast((ReferenceType) BcelWorld.makeBcelType(aspectType)));
		il.append(InstructionFactory.createReturn(Type.OBJECT));
	}

	private void generatePerCflowHasAspectMethod(LazyClassGen classGen) {
		InstructionFactory factory = classGen.getFactory();
		LazyMethodGen method = makeMethodGen(classGen, AjcMemberMaker.perCflowHasAspectMethod(aspectType));
		flagAsSynthetic(method, false);
		classGen.addMethodGen(method);

		InstructionList il = method.getBody();
		il.append(Utility.createGet(factory, AjcMemberMaker.perCflowField(aspectType)));
		il.append(Utility.createInvoke(factory, Constants.INVOKEVIRTUAL, AjcMemberMaker.cflowStackIsValid()));
		il.append(InstructionFactory.createReturn(Type.INT));
	}

	private void generatePerCflowPushMethod(LazyClassGen classGen) {
		InstructionFactory factory = classGen.getFactory();
		LazyMethodGen method = makeMethodGen(classGen, AjcMemberMaker.perCflowPush(aspectType));
		flagAsSynthetic(method, true);
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

		LazyMethodGen method = classGen.getAjcPreClinit(); // Creates a clinit if there isn't one

		InstructionList il = new InstructionList();
		il.append(factory.createNew(AjcMemberMaker.CFLOW_STACK_TYPE.getName()));
		il.append(InstructionConstants.DUP);
		il.append(factory.createInvoke(AjcMemberMaker.CFLOW_STACK_TYPE.getName(), "<init>", Type.VOID, Type.NO_ARGS,
				Constants.INVOKESPECIAL));
		il.append(Utility.createSet(factory, AjcMemberMaker.perCflowField(aspectType)));
		method.getBody().insert(il);
	}

	private void generatePerTWAspectOfMethod(LazyClassGen classGen) {
		InstructionFactory factory = classGen.getFactory();
		LazyMethodGen method = makeMethodGen(classGen, AjcMemberMaker.perTypeWithinAspectOfMethod(aspectType, classGen.getWorld()
				.isInJava5Mode()));
		flagAsSynthetic(method, false);
		classGen.addMethodGen(method);

		InstructionList il = method.getBody();
		InstructionHandle tryStart = il.append(InstructionConstants.ALOAD_0);

		il.append(Utility.createInvoke(factory, Constants.INVOKESTATIC, AjcMemberMaker.perTypeWithinGetInstance(aspectType)));
		il.append(InstructionConstants.ASTORE_1);
		il.append(InstructionConstants.ALOAD_1);
		InstructionBranch ifNonNull = InstructionFactory.createBranchInstruction(Constants.IFNONNULL, null);
		il.append(ifNonNull);
		il.append(factory.createNew(AjcMemberMaker.NO_ASPECT_BOUND_EXCEPTION.getName()));
		il.append(InstructionConstants.DUP);
		il.append(InstructionFactory.PUSH(classGen.getConstantPool(), aspectType.getName()));
		il.append(InstructionConstants.ACONST_NULL);
		il.append(factory.createInvoke(AjcMemberMaker.NO_ASPECT_BOUND_EXCEPTION.getName(), "<init>", Type.VOID, new Type[] {
				Type.STRING, new ObjectType("java.lang.Throwable") }, Constants.INVOKESPECIAL));
		il.append(InstructionConstants.ATHROW);
		InstructionHandle ifElse = il.append(InstructionConstants.ALOAD_1);
		ifNonNull.setTarget(ifElse);
		il.append(InstructionFactory.createReturn(Type.OBJECT));

		InstructionHandle handler = il.append(InstructionConstants.ASTORE_1);
		il.append(factory.createNew(AjcMemberMaker.NO_ASPECT_BOUND_EXCEPTION.getName()));
		il.append(InstructionConstants.DUP);
		il.append(factory.createInvoke(AjcMemberMaker.NO_ASPECT_BOUND_EXCEPTION.getName(), "<init>", Type.VOID, Type.NO_ARGS,
				Constants.INVOKESPECIAL));
		il.append(InstructionConstants.ATHROW);

		method.addExceptionHandler(tryStart, handler.getPrev(), handler, new ObjectType("java.lang.Exception"), false);
	}

	// Create 'public String getWithinTypeName() { return ajc$withinType;}'
	private void generatePerTWGetWithinTypeNameMethod(LazyClassGen classGen) {
		InstructionFactory factory = classGen.getFactory();
		LazyMethodGen method = makeMethodGen(classGen, AjcMemberMaker.perTypeWithinGetWithinTypeNameMethod(aspectType, classGen
				.getWorld().isInJava5Mode()));
		flagAsSynthetic(method, false);
		classGen.addMethodGen(method);
		// 0: aload_0
		// 1: getfield #14; //Field ajc$withinType:Ljava/lang/String;
		// 4: areturn
		InstructionList il = method.getBody();
		il.append(InstructionConstants.ALOAD_0);
		il.append(Utility.createGet(factory, AjcMemberMaker.perTypeWithinWithinTypeField(aspectType, aspectType)));
		il.append(InstructionConstants.ARETURN);
	}

	private void generatePerTWHasAspectMethod(LazyClassGen classGen) {
		InstructionFactory factory = classGen.getFactory();
		LazyMethodGen method = makeMethodGen(classGen, AjcMemberMaker.perTypeWithinHasAspectMethod(aspectType, classGen.getWorld()
				.isInJava5Mode()));
		flagAsSynthetic(method, false);
		classGen.addMethodGen(method);

		InstructionList il = method.getBody();
		InstructionHandle tryStart = il.append(InstructionConstants.ALOAD_0);
		il.append(Utility.createInvoke(factory, Constants.INVOKESTATIC, AjcMemberMaker.perTypeWithinGetInstance(aspectType)));
		InstructionBranch ifNull = InstructionFactory.createBranchInstruction(Constants.IFNULL, null);
		il.append(ifNull);
		il.append(InstructionConstants.ICONST_1);
		il.append(InstructionConstants.IRETURN);
		InstructionHandle ifElse = il.append(InstructionConstants.ICONST_0);
		ifNull.setTarget(ifElse);
		il.append(InstructionConstants.IRETURN);

		InstructionHandle handler = il.append(InstructionConstants.ASTORE_1);
		il.append(InstructionConstants.ICONST_0);
		il.append(InstructionConstants.IRETURN);

		method.addExceptionHandler(tryStart, handler.getPrev(), handler, new ObjectType("java.lang.Exception"), false);
	}

	private void generatePerTWGetInstanceMethod(LazyClassGen classGen) {
		InstructionFactory factory = classGen.getFactory();
		LazyMethodGen method = makeMethodGen(classGen, AjcMemberMaker.perTypeWithinGetInstance(aspectType));
		flagAsSynthetic(method, true);
		classGen.addMethodGen(method);

		InstructionList il = method.getBody();
		InstructionHandle tryStart = il.append(InstructionConstants.ALOAD_0);
		il.append(InstructionFactory.PUSH(factory.getConstantPool(), NameMangler.perTypeWithinLocalAspectOf(aspectType)));
		il.append(InstructionConstants.ACONST_NULL);// Class[] for "getDeclaredMethod"
		il.append(factory.createInvoke("java/lang/Class", "getDeclaredMethod", Type.getType("Ljava/lang/reflect/Method;"),
				new Type[] { Type.getType("Ljava/lang/String;"), Type.getType("[Ljava/lang/Class;") }, Constants.INVOKEVIRTUAL));
		il.append(InstructionConstants.ACONST_NULL);// Object for "invoke", static method
		il.append(InstructionConstants.ACONST_NULL);// Object[] for "invoke", no arg
		il.append(factory.createInvoke("java/lang/reflect/Method", "invoke", Type.OBJECT, new Type[] {
				Type.getType("Ljava/lang/Object;"), Type.getType("[Ljava/lang/Object;") }, Constants.INVOKEVIRTUAL));
		il.append(factory.createCheckCast((ReferenceType) BcelWorld.makeBcelType(aspectType)));
		il.append(InstructionConstants.ARETURN);

		InstructionHandle handler = il.append(InstructionConstants.ASTORE_1);
		il.append(InstructionConstants.ACONST_NULL);
		il.append(InstructionConstants.ARETURN);

		method.addExceptionHandler(tryStart, handler.getPrev(), handler, new ObjectType("java.lang.Exception"), false);
	}

	private void generatePerTWCreateAspectInstanceMethod(LazyClassGen classGen) {
		InstructionFactory factory = classGen.getFactory();
		LazyMethodGen method = makeMethodGen(classGen, AjcMemberMaker.perTypeWithinCreateAspectInstance(aspectType));
		flagAsSynthetic(method, true);
		classGen.addMethodGen(method);

		InstructionList il = method.getBody();
		il.append(factory.createNew(aspectType.getName()));
		il.append(InstructionConstants.DUP);
		il.append(factory.createInvoke(aspectType.getName(), "<init>", Type.VOID, Type.NO_ARGS, Constants.INVOKESPECIAL));
		il.append(InstructionConstants.ASTORE_1);
		il.append(InstructionConstants.ALOAD_1);
		il.append(InstructionConstants.ALOAD_0);
		il.append(Utility.createSet(factory, AjcMemberMaker.perTypeWithinWithinTypeField(aspectType, aspectType)));
		il.append(InstructionConstants.ALOAD_1);
		il.append(InstructionConstants.ARETURN);
	}

	/**
	 * Add standard Synthetic (if wished) and AjSynthetic (always) attributes
	 *
	 * @param methodGen
	 * @param makeJavaSynthetic true if standard Synthetic attribute must be set as well (invisible to user)
	 */
	private static void flagAsSynthetic(LazyMethodGen methodGen, boolean makeJavaSynthetic) {
		if (makeJavaSynthetic) {
			methodGen.makeSynthetic();
		}
		methodGen.addAttribute(Utility
				.bcelAttribute(new AjAttribute.AjSynthetic(), methodGen.getEnclosingClass().getConstantPool()));
	}

	// public boolean isLateTypeMunger() {
	// return true;
	// }
}
