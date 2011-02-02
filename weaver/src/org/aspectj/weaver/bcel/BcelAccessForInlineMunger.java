/*******************************************************************************
 * Copyright (c) 2005 Contributors.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://eclipse.org/legal/epl-v10.html 
 * 
 * Contributors:
 *   Alexandre Vasseur         initial implementation
 *******************************************************************************/
package org.aspectj.weaver.bcel;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aspectj.apache.bcel.Constants;
import org.aspectj.apache.bcel.classfile.ConstantPool;
import org.aspectj.apache.bcel.generic.FieldInstruction;
import org.aspectj.apache.bcel.generic.Instruction;
import org.aspectj.apache.bcel.generic.InstructionConstants;
import org.aspectj.apache.bcel.generic.InstructionFactory;
import org.aspectj.apache.bcel.generic.InstructionHandle;
import org.aspectj.apache.bcel.generic.InstructionList;
import org.aspectj.apache.bcel.generic.InvokeInstruction;
import org.aspectj.apache.bcel.generic.Type;
import org.aspectj.weaver.AjAttribute;
import org.aspectj.weaver.AjcMemberMaker;
import org.aspectj.weaver.Member;
import org.aspectj.weaver.NameMangler;
import org.aspectj.weaver.ResolvedMember;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.Shadow;
import org.aspectj.weaver.UnresolvedType;

/**
 * Looks for all access to method or field that are not public within the body of the around advices and replace the invocations to
 * a wrapper call so that the around advice can further be inlined.
 * <p/>
 * This munger is used for @AJ aspects for which inlining wrapper is not done at compile time.
 * <p/>
 * Specific state and logic is kept in the munger ala ITD so that call/get/set pointcuts can still be matched on the wrapped member
 * thanks to the EffectiveSignature attribute.
 * 
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
public class BcelAccessForInlineMunger extends BcelTypeMunger {

	/**
	 * Wrapper member cache, key is wrapper name. This structure is queried when regular shadow matching in the advice body
	 * (call/get/set) occurs
	 */
	private Map<String, ResolvedMember> m_inlineAccessorBcelMethods;

	/**
	 * The aspect we act for
	 */
	private LazyClassGen m_aspectGen;

	/**
	 * The wrapper method we need to add. Those are added at the end of the munging
	 */
	private Set<LazyMethodGen> inlineAccessorMethodGens;

	public BcelAccessForInlineMunger(ResolvedType aspectType) {
		super(null, aspectType);
		if (aspectType.getWorld().isXnoInline()) {
			throw new Error("This should not happen");
		}
	}

	@Override
	public boolean munge(BcelClassWeaver weaver) {
		m_aspectGen = weaver.getLazyClassGen();
		m_inlineAccessorBcelMethods = new HashMap<String, ResolvedMember>(0);
		inlineAccessorMethodGens = new HashSet<LazyMethodGen>();

		// look for all @Around advices
		for (LazyMethodGen methodGen : m_aspectGen.getMethodGens()) {
			if (methodGen.hasAnnotation(UnresolvedType.forName("org/aspectj/lang/annotation/Around"))) {
				openAroundAdvice(methodGen);
			}
		}

		// add the accessors
		for (LazyMethodGen lazyMethodGen : inlineAccessorMethodGens) {
			m_aspectGen.addMethodGen(lazyMethodGen);
		}

		// flush some
		inlineAccessorMethodGens = null;
		// we keep m_inlineAccessorsResolvedMembers for shadow matching

		return true;
	}

	/**
	 * Looks in the wrapper we have added so that we can find their effective signature if needed
	 * 
	 * @param member
	 * @return
	 */
	@Override
	public ResolvedMember getMatchingSyntheticMember(Member member) {
		return m_inlineAccessorBcelMethods.get(member.getName());
	}

	@Override
	public ResolvedMember getSignature() {
		return null;
	}

	/**
	 * Match only the aspect for which we act
	 * 
	 * @param onType
	 * @return
	 */
	@Override
	public boolean matches(ResolvedType onType) {
		return aspectType.equals(onType);
	}

	/**
	 * Prepare the around advice, flag it as cannot be inlined if it can't be
	 * 
	 * @param aroundAdvice
	 */
	private void openAroundAdvice(LazyMethodGen aroundAdvice) {
		InstructionHandle curr = aroundAdvice.getBody().getStart();
		InstructionHandle end = aroundAdvice.getBody().getEnd();
		ConstantPool cpg = aroundAdvice.getEnclosingClass().getConstantPool();
		InstructionFactory factory = aroundAdvice.getEnclosingClass().getFactory();

		boolean realizedCannotInline = false;
		while (curr != end) {
			if (realizedCannotInline) {
				// we know we cannot inline this advice so no need for futher handling
				break;
			}
			InstructionHandle next = curr.getNext();
			Instruction inst = curr.getInstruction();

			// open-up method call
			if ((inst instanceof InvokeInstruction)) {
				InvokeInstruction invoke = (InvokeInstruction) inst;
				ResolvedType callee = m_aspectGen.getWorld().resolve(UnresolvedType.forName(invoke.getClassName(cpg)));

				// look in the whole method list and not just declared for super calls and alike
				List<ResolvedMember> methods = callee.getMethodsWithoutIterator(false, true, false);
				for (ResolvedMember resolvedMember : methods) {
					if (invoke.getName(cpg).equals(resolvedMember.getName())
							&& invoke.getSignature(cpg).equals(resolvedMember.getSignature()) && !resolvedMember.isPublic()) {
						if ("<init>".equals(invoke.getName(cpg))) {
							// skipping open up for private constructor
							// can occur when aspect new a private inner type
							// too complex to handle new + dup + .. + invokespecial here.
							aroundAdvice.setCanInline(false);
							realizedCannotInline = true;
						} else {
							// specific handling for super.foo() calls, where foo is non public
							ResolvedType memberType = m_aspectGen.getWorld().resolve(resolvedMember.getDeclaringType());
							if (!aspectType.equals(memberType) && memberType.isAssignableFrom(aspectType)) {
								// old test was...
								// if (aspectType.getSuperclass() != null
								// && aspectType.getSuperclass().getName().equals(resolvedMember.getDeclaringType().getName())) {
								ResolvedMember accessor = createOrGetInlineAccessorForSuperDispatch(resolvedMember);
								InvokeInstruction newInst = factory.createInvoke(aspectType.getName(), accessor.getName(),
										BcelWorld.makeBcelType(accessor.getReturnType()),
										BcelWorld.makeBcelTypes(accessor.getParameterTypes()), Constants.INVOKEVIRTUAL);
								curr.setInstruction(newInst);
							} else {
								ResolvedMember accessor = createOrGetInlineAccessorForMethod(resolvedMember);
								InvokeInstruction newInst = factory.createInvoke(aspectType.getName(), accessor.getName(),
										BcelWorld.makeBcelType(accessor.getReturnType()),
										BcelWorld.makeBcelTypes(accessor.getParameterTypes()), Constants.INVOKESTATIC);
								curr.setInstruction(newInst);
							}
						}

						break;// ok we found a matching callee member and swapped the instruction with the accessor
					}
				}
			} else if (inst instanceof FieldInstruction) {
				FieldInstruction invoke = (FieldInstruction) inst;
				ResolvedType callee = m_aspectGen.getWorld().resolve(UnresolvedType.forName(invoke.getClassName(cpg)));
				for (int i = 0; i < callee.getDeclaredJavaFields().length; i++) {
					ResolvedMember resolvedMember = callee.getDeclaredJavaFields()[i];
					if (invoke.getName(cpg).equals(resolvedMember.getName())
							&& invoke.getSignature(cpg).equals(resolvedMember.getSignature()) && !resolvedMember.isPublic()) {
						final ResolvedMember accessor;
						if ((inst.opcode == Constants.GETFIELD) || (inst.opcode == Constants.GETSTATIC)) {
							accessor = createOrGetInlineAccessorForFieldGet(resolvedMember);
						} else {
							accessor = createOrGetInlineAccessorForFieldSet(resolvedMember);
						}
						InvokeInstruction newInst = factory.createInvoke(aspectType.getName(), accessor.getName(),
								BcelWorld.makeBcelType(accessor.getReturnType()),
								BcelWorld.makeBcelTypes(accessor.getParameterTypes()), Constants.INVOKESTATIC);
						curr.setInstruction(newInst);

						break;// ok we found a matching callee member and swapped the instruction with the accessor
					}
				}
			}

			curr = next;
		}

		// no reason for not inlining this advice
		// since it is used for @AJ advice that cannot be inlined by defauilt
		// make sure we set inline to true since we have done this analysis
		if (!realizedCannotInline) {
			aroundAdvice.setCanInline(true);
		}
	}

	/**
	 * Add an inline wrapper for a non public method call
	 * 
	 * @param resolvedMember
	 * @return
	 */
	private ResolvedMember createOrGetInlineAccessorForMethod(ResolvedMember resolvedMember) {
		String accessor = NameMangler.inlineAccessMethodForMethod(resolvedMember.getName(), resolvedMember.getDeclaringType(),
				aspectType);
		ResolvedMember inlineAccessor = m_inlineAccessorBcelMethods.get(accessor);
		if (inlineAccessor == null) {
			// add static method to aspect
			inlineAccessor = AjcMemberMaker.inlineAccessMethodForMethod(aspectType, resolvedMember);

			// add new accessor method to aspect bytecode
			InstructionFactory factory = m_aspectGen.getFactory();
			LazyMethodGen method = makeMethodGen(m_aspectGen, inlineAccessor);
			// flag it synthetic, AjSynthetic
			method.makeSynthetic();
			List<AjAttribute> methodAttributes = new ArrayList<AjAttribute>();
			methodAttributes.add(new AjAttribute.AjSynthetic());
			methodAttributes.add(new AjAttribute.EffectiveSignatureAttribute(resolvedMember, Shadow.MethodCall, false));
			method.addAttribute(Utility.bcelAttribute(methodAttributes.get(0), m_aspectGen.getConstantPool()));
			// flag the effective signature, so that we can deobfuscate the signature to apply method call pointcut
			method.addAttribute(Utility.bcelAttribute(methodAttributes.get(1), m_aspectGen.getConstantPool()));

			inlineAccessorMethodGens.add(method);

			InstructionList il = method.getBody();
			int register = 0;
			for (int i = 0; i < inlineAccessor.getParameterTypes().length; i++) {
				UnresolvedType typeX = inlineAccessor.getParameterTypes()[i];
				Type type = BcelWorld.makeBcelType(typeX);
				il.append(InstructionFactory.createLoad(type, register));
				register += type.getSize();
			}
			il.append(Utility.createInvoke(factory, Modifier.isStatic(resolvedMember.getModifiers()) ? Constants.INVOKESTATIC
					: Constants.INVOKEVIRTUAL, resolvedMember));
			il.append(InstructionFactory.createReturn(BcelWorld.makeBcelType(inlineAccessor.getReturnType())));

			m_inlineAccessorBcelMethods.put(accessor, new BcelMethod(m_aspectGen.getBcelObjectType(), method.getMethod(),
					methodAttributes));
		}
		return inlineAccessor;
	}

	/**
	 * Add an inline wrapper for a non public super.method call
	 * 
	 * @param resolvedMember
	 * @return
	 */
	private ResolvedMember createOrGetInlineAccessorForSuperDispatch(ResolvedMember resolvedMember) {
		String accessor = NameMangler.superDispatchMethod(aspectType, resolvedMember.getName());
		ResolvedMember inlineAccessor = m_inlineAccessorBcelMethods.get(accessor);
		if (inlineAccessor == null) {
			// add super accessor method to class:
			inlineAccessor = AjcMemberMaker.superAccessMethod(aspectType, resolvedMember);

			// add new accessor method to aspect bytecode
			InstructionFactory factory = m_aspectGen.getFactory();
			LazyMethodGen method = makeMethodGen(m_aspectGen, inlineAccessor);
			// flag it synthetic, AjSynthetic
			method.makeSynthetic();
			List<AjAttribute> methodAttributes = new ArrayList<AjAttribute>();
			methodAttributes.add(new AjAttribute.AjSynthetic());
			methodAttributes.add(new AjAttribute.EffectiveSignatureAttribute(resolvedMember, Shadow.MethodCall, false));
			method.addAttribute(Utility.bcelAttribute(methodAttributes.get(0), m_aspectGen.getConstantPool()));
			// flag the effective signature, so that we can deobfuscate the signature to apply method call pointcut
			method.addAttribute(Utility.bcelAttribute(methodAttributes.get(1), m_aspectGen.getConstantPool()));

			inlineAccessorMethodGens.add(method);

			InstructionList il = method.getBody();
			il.append(InstructionConstants.ALOAD_0);
			int register = 1;
			for (int i = 0; i < inlineAccessor.getParameterTypes().length; i++) {
				UnresolvedType typeX = inlineAccessor.getParameterTypes()[i];
				Type type = BcelWorld.makeBcelType(typeX);
				il.append(InstructionFactory.createLoad(type, register));
				register += type.getSize();
			}
			il.append(Utility.createInvoke(factory, Constants.INVOKESPECIAL, resolvedMember));
			il.append(InstructionFactory.createReturn(BcelWorld.makeBcelType(inlineAccessor.getReturnType())));

			m_inlineAccessorBcelMethods.put(accessor, new BcelMethod(m_aspectGen.getBcelObjectType(), method.getMethod(),
					methodAttributes));
		}
		return inlineAccessor;
	}

	/**
	 * Add an inline wrapper for a non public field get
	 * 
	 * @param resolvedMember
	 * @return
	 */
	private ResolvedMember createOrGetInlineAccessorForFieldGet(ResolvedMember resolvedMember) {
		String accessor = NameMangler.inlineAccessMethodForFieldGet(resolvedMember.getName(), resolvedMember.getDeclaringType(),
				aspectType);
		ResolvedMember inlineAccessor = m_inlineAccessorBcelMethods.get(accessor);
		if (inlineAccessor == null) {
			// add static method to aspect
			inlineAccessor = AjcMemberMaker.inlineAccessMethodForFieldGet(aspectType, resolvedMember);

			// add new accessor method to aspect bytecode
			InstructionFactory factory = m_aspectGen.getFactory();
			LazyMethodGen method = makeMethodGen(m_aspectGen, inlineAccessor);
			// flag it synthetic, AjSynthetic
			method.makeSynthetic();
			List<AjAttribute> methodAttributes = new ArrayList<AjAttribute>();
			methodAttributes.add(new AjAttribute.AjSynthetic());
			methodAttributes.add(new AjAttribute.EffectiveSignatureAttribute(resolvedMember, Shadow.FieldGet, false));
			// flag the effective signature, so that we can deobfuscate the signature to apply method call pointcut
			method.addAttribute(Utility.bcelAttribute(methodAttributes.get(0), m_aspectGen.getConstantPool()));
			method.addAttribute(Utility.bcelAttribute(methodAttributes.get(1), m_aspectGen.getConstantPool()));

			inlineAccessorMethodGens.add(method);

			InstructionList il = method.getBody();
			if (Modifier.isStatic(resolvedMember.getModifiers())) {
				// field accessed is static so no "this" as accessor sole parameter
			} else {
				il.append(InstructionConstants.ALOAD_0);
			}
			il.append(Utility.createGet(factory, resolvedMember));
			il.append(InstructionFactory.createReturn(BcelWorld.makeBcelType(inlineAccessor.getReturnType())));

			m_inlineAccessorBcelMethods.put(accessor, new BcelMethod(m_aspectGen.getBcelObjectType(), method.getMethod(),
					methodAttributes));
		}
		return inlineAccessor;
	}

	/**
	 * Add an inline wrapper for a non public field set
	 * 
	 * @param resolvedMember
	 * @return
	 */
	private ResolvedMember createOrGetInlineAccessorForFieldSet(ResolvedMember resolvedMember) {
		String accessor = NameMangler.inlineAccessMethodForFieldSet(resolvedMember.getName(), resolvedMember.getDeclaringType(),
				aspectType);
		ResolvedMember inlineAccessor = m_inlineAccessorBcelMethods.get(accessor);
		if (inlineAccessor == null) {
			// add static method to aspect
			inlineAccessor = AjcMemberMaker.inlineAccessMethodForFieldSet(aspectType, resolvedMember);

			// add new accessor method to aspect bytecode
			InstructionFactory factory = m_aspectGen.getFactory();
			LazyMethodGen method = makeMethodGen(m_aspectGen, inlineAccessor);
			// flag it synthetic, AjSynthetic
			method.makeSynthetic();
			List<AjAttribute> methodAttributes = new ArrayList<AjAttribute>();
			methodAttributes.add(new AjAttribute.AjSynthetic());
			methodAttributes.add(new AjAttribute.EffectiveSignatureAttribute(resolvedMember, Shadow.FieldSet, false));
			method.addAttribute(Utility.bcelAttribute(methodAttributes.get(0), m_aspectGen.getConstantPool()));
			// flag the effective signature, so that we can deobfuscate the signature to apply method call pointcut
			method.addAttribute(Utility.bcelAttribute(methodAttributes.get(1), m_aspectGen.getConstantPool()));

			inlineAccessorMethodGens.add(method);

			InstructionList il = method.getBody();
			if (Modifier.isStatic(resolvedMember.getModifiers())) {
				// field accessed is static so sole parameter is field value to be set
				il.append(InstructionFactory.createLoad(BcelWorld.makeBcelType(resolvedMember.getReturnType()), 0));
			} else {
				il.append(InstructionConstants.ALOAD_0);
				il.append(InstructionFactory.createLoad(BcelWorld.makeBcelType(resolvedMember.getReturnType()), 1));
			}
			il.append(Utility.createSet(factory, resolvedMember));
			il.append(InstructionConstants.RETURN);
			m_inlineAccessorBcelMethods.put(accessor, new BcelMethod(m_aspectGen.getBcelObjectType(), method.getMethod(),
					methodAttributes));
		}
		return inlineAccessor;
	}
}
