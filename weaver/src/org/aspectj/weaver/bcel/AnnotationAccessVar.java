/* *******************************************************************
 * Copyright (c) 2005-2008 Contributors
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Andy Clement     initial implementation 
 * ******************************************************************/

package org.aspectj.weaver.bcel;

import org.aspectj.apache.bcel.Constants;
import org.aspectj.apache.bcel.classfile.Field;
import org.aspectj.apache.bcel.generic.Instruction;
import org.aspectj.apache.bcel.generic.InstructionBranch;
import org.aspectj.apache.bcel.generic.InstructionConstants;
import org.aspectj.apache.bcel.generic.InstructionFactory;
import org.aspectj.apache.bcel.generic.InstructionHandle;
import org.aspectj.apache.bcel.generic.InstructionList;
import org.aspectj.apache.bcel.generic.ObjectType;
import org.aspectj.apache.bcel.generic.Type;
import org.aspectj.weaver.Member;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.Shadow;
import org.aspectj.weaver.Shadow.Kind;
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.ast.Var;

/**
 * Represents access to an annotation on an element, relating to some kinded pointcut. Depending on the kind of pointcut the element
 * might be a field or a method and the code generators in here can retrieve the annotation from the element.
 */
public class AnnotationAccessVar extends BcelVar {

	private BcelShadow shadow;
	private Kind kind; // What kind of shadow are we at?
	private UnresolvedType containingType; // The type upon which we want to ask for 'member'
	private Member member; // Holds the member that has the annotations (for method/field join points)
	private boolean isWithin; // implies @within() or @withincode(). If false, that implies @annotation()

	public AnnotationAccessVar(BcelShadow shadow, Kind kind, ResolvedType annotationType, UnresolvedType theTargetIsStoredHere,
			Member sig, boolean isWithin) {
		super(annotationType, 0);
		this.shadow = shadow;
		this.kind = kind;
		this.containingType = theTargetIsStoredHere;
		this.member = sig;
		this.isWithin = isWithin;
	}

	public Kind getKind() {
		return kind;
	}

	@Override
	public String toString() {
		return "AnnotationAccessVar(" + getType() + ")";
	}

	@Override
	public Instruction createLoad(InstructionFactory fact) {
		throw new IllegalStateException("unimplemented");
	}

	@Override
	public Instruction createStore(InstructionFactory fact) {
		throw new IllegalStateException("unimplemented");
	}

	@Override
	public InstructionList createCopyFrom(InstructionFactory fact, int oldSlot) {
		throw new IllegalStateException("unimplemented");
	}

	@Override
	public void appendLoad(InstructionList il, InstructionFactory fact) {
		il.append(createLoadInstructions(getType(), fact));
	}

	@Override
	public void appendLoadAndConvert(InstructionList il, InstructionFactory fact, ResolvedType toType) {
		il.append(createLoadInstructions(toType, fact));
	}

	@Override
	public void insertLoad(InstructionList il, InstructionFactory fact) {
		il.insert(createLoadInstructions(getType(), fact));
	}

	private InstructionList createLoadInstructions(ResolvedType toType, InstructionFactory fact) {

		InstructionList il = new InstructionList();

		Type jlClass = BcelWorld.makeBcelType(UnresolvedType.JL_CLASS);
		Type jlString = BcelWorld.makeBcelType(UnresolvedType.JL_STRING);
		Type jlClassArray = BcelWorld.makeBcelType(UnresolvedType.JAVA_LANG_CLASS_ARRAY);
		Type jlaAnnotation = BcelWorld.makeBcelType(UnresolvedType.JAVA_LANG_ANNOTATION);

		Instruction pushConstant = fact.createConstant(new ObjectType(toType.getName()));

		if (kind == Shadow.MethodCall || kind == Shadow.MethodExecution || kind == Shadow.PreInitialization
				|| kind == Shadow.Initialization || kind == Shadow.ConstructorCall || kind == Shadow.ConstructorExecution
				|| kind == Shadow.AdviceExecution ||
				// annotations for fieldset/fieldget when an ITD is involved are stored against a METHOD
				((kind == Shadow.FieldGet || kind == Shadow.FieldSet) && member.getKind() == Member.METHOD)) {

			Type jlrMethod = BcelWorld.makeBcelType(UnresolvedType.forSignature("Ljava/lang/reflect/Method;"));
			Type jlAnnotation = BcelWorld.makeBcelType(UnresolvedType.forSignature("Ljava/lang/annotation/Annotation;"));
			Type[] paramTypes = BcelWorld.makeBcelTypes(member.getParameterTypes());

			// il.append(fact.createConstant(BcelWorld.makeBcelType(containingType)));

			if (kind == Shadow.MethodCall
					|| kind == Shadow.MethodExecution
					|| kind == Shadow.AdviceExecution
					||
					// annotations for fieldset/fieldget when an ITD is involved are stored against a METHOD
					((kind == Shadow.FieldGet || kind == Shadow.FieldSet) && member.getKind() == Member.METHOD)
					|| ((kind == Shadow.ConstructorCall || kind == Shadow.ConstructorExecution) && member.getKind() == Member.METHOD)) {

				// Need to look at the cached annotation before going to fetch it again
				Field annotationCachingField = shadow.getEnclosingClass().getAnnotationCachingField(shadow, toType, isWithin);

				// Basic idea here is to check if the cached field is null, if it is then initialize it, otherwise use it
				il.append(fact.createGetStatic(shadow.getEnclosingClass().getName(), annotationCachingField.getName(), jlAnnotation));
				il.append(InstructionConstants.DUP);
				InstructionBranch ifNonNull = InstructionFactory.createBranchInstruction(Constants.IFNONNULL, null);
				il.append(ifNonNull);
				il.append(InstructionConstants.POP);
				il.append(fact.createConstant(BcelWorld.makeBcelType(containingType)));

				il.append(fact.createConstant(member.getName()));
				buildArray(il, fact, jlClass, paramTypes, 1);
				// OPTIMIZE cache result of getDeclaredMethod?
				il.append(fact.createInvoke("java/lang/Class", "getDeclaredMethod", jlrMethod,
						new Type[] { jlString, jlClassArray }, Constants.INVOKEVIRTUAL));
				il.append(pushConstant);// fact.createConstant(new ObjectType(toType.getName())));
				il.append(fact.createInvoke("java/lang/reflect/Method", "getAnnotation", jlaAnnotation, new Type[] { jlClass },
						Constants.INVOKEVIRTUAL));
				il.append(InstructionConstants.DUP);
				il.append(fact.createPutStatic(shadow.getEnclosingClass().getName(), annotationCachingField.getName(), jlAnnotation));
				InstructionHandle ifNullElse = il.append(InstructionConstants.NOP);
				ifNonNull.setTarget(ifNullElse);

			} else { // init/preinit/ctor-call/ctor-exec
				il.append(fact.createConstant(BcelWorld.makeBcelType(containingType)));
				buildArray(il, fact, jlClass, paramTypes, 1);
				Type jlrCtor = BcelWorld.makeBcelType(UnresolvedType.JAVA_LANG_REFLECT_CONSTRUCTOR);
				// OPTIMIZE cache result of getDeclaredConstructor and getAnnotation? Might be able to use it again if someone else
				// needs the same annotations?
				il.append(fact.createInvoke("java/lang/Class", "getDeclaredConstructor", jlrCtor, new Type[] { jlClassArray },
						Constants.INVOKEVIRTUAL));
				il.append(pushConstant);
				il.append(fact.createInvoke("java/lang/reflect/Constructor", "getAnnotation", jlaAnnotation,
						new Type[] { jlClass }, Constants.INVOKEVIRTUAL));
			}
		} else if (kind == Shadow.FieldSet || kind == Shadow.FieldGet) {
			generateBytecodeToAccessAnnotationAtFieldGetSetShadow(toType, fact, il, pushConstant);
		} else if (kind == Shadow.StaticInitialization || kind == Shadow.ExceptionHandler) {
			il.append(fact.createConstant(BcelWorld.makeBcelType(containingType)));
			il.append(pushConstant);
			il.append(fact.createInvoke("java/lang/Class", "getAnnotation", jlaAnnotation, new Type[] { jlClass },
					Constants.INVOKEVIRTUAL));
		} else {
			throw new RuntimeException("Don't understand this kind " + kind);
		}
		il.append(Utility.createConversion(fact, jlaAnnotation, BcelWorld.makeBcelType(toType)));
		return il;
	}

	/**
	 * At a FieldGet or FieldSet shadow, generate the bytecode to access the annotation for that field. The annotation is cached so
	 * the code checks that cached value before proceeding.
	 */
	private void generateBytecodeToAccessAnnotationAtFieldGetSetShadow(ResolvedType toType, InstructionFactory fact,
			InstructionList il, Instruction pushConstantAnnotationType) {
		Type jlClass = BcelWorld.makeBcelType(UnresolvedType.JL_CLASS);
		Type jlString = BcelWorld.makeBcelType(UnresolvedType.JL_STRING);
		Type jlaAnnotation = BcelWorld.makeBcelType(UnresolvedType.JAVA_LANG_ANNOTATION);
		Type jlrField = BcelWorld.makeBcelType(UnresolvedType.JAVA_LANG_REFLECT_FIELD);

		LazyClassGen shadowEnclosingClass = shadow.getEnclosingClass();

		// The annotation for the field of interest is cached, check cached value before fetching it
		Field annotationCachingField = shadowEnclosingClass.getAnnotationCachingField(shadow, toType, isWithin);
		String annotationCachingFieldName = annotationCachingField.getName();

		// Basic idea here is to check if the cached field is null, if it is then initialize it, otherwise use it
		il.append(fact.createGetStatic(shadowEnclosingClass.getName(), annotationCachingFieldName, jlaAnnotation));
		il.appendDUP();
		InstructionBranch ifNonNull = new InstructionBranch(Constants.IFNONNULL, null);
		il.append(ifNonNull);
		il.appendPOP();

		// get the field of interest
		il.append(fact.createConstant(BcelWorld.makeBcelType(containingType)));
		il.append(fact.createConstant(member.getName()));
		il.append(fact.createInvoke("java/lang/Class", "getDeclaredField", jlrField, new Type[] { jlString },
				Constants.INVOKEVIRTUAL));
		il.append(pushConstantAnnotationType);
		il.append(fact.createInvoke("java/lang/reflect/Field", "getAnnotation", jlaAnnotation, new Type[] { jlClass },
				Constants.INVOKEVIRTUAL));
		il.appendDUP();
		il.append(fact.createPutStatic(shadowEnclosingClass.getName(), annotationCachingFieldName, jlaAnnotation));
		InstructionHandle ifNullElse = il.appendNOP();
		ifNonNull.setTarget(ifNullElse);
	}

	private void buildArray(InstructionList il, InstructionFactory fact, Type arrayElementType, Type[] arrayEntries, int dim) {
		il.append(fact.createConstant(Integer.valueOf(arrayEntries == null ? 0 : arrayEntries.length)));
		il.append(fact.createNewArray(arrayElementType, (short) dim));
		if (arrayEntries == null) {
			return;
		}
		for (int i = 0; i < arrayEntries.length; i++) {
			il.append(InstructionFactory.createDup(1));
			il.append(fact.createConstant(Integer.valueOf(i)));
			switch (arrayEntries[i].getType()) {
			case Constants.T_ARRAY:
				il.append(fact.createConstant(new ObjectType(arrayEntries[i].getSignature()))); // FIXME should be getName() and not
				// getSignature()?
				break;
			case Constants.T_BOOLEAN:
				il.append(fact.createGetStatic("java/lang/Boolean", "TYPE", arrayElementType));
				break;
			case Constants.T_BYTE:
				il.append(fact.createGetStatic("java/lang/Byte", "TYPE", arrayElementType));
				break;
			case Constants.T_CHAR:
				il.append(fact.createGetStatic("java/lang/Character", "TYPE", arrayElementType));
				break;
			case Constants.T_INT:
				il.append(fact.createGetStatic("java/lang/Integer", "TYPE", arrayElementType));
				break;
			case Constants.T_LONG:
				il.append(fact.createGetStatic("java/lang/Long", "TYPE", arrayElementType));
				break;
			case Constants.T_DOUBLE:
				il.append(fact.createGetStatic("java/lang/Double", "TYPE", arrayElementType));
				break;
			case Constants.T_FLOAT:
				il.append(fact.createGetStatic("java/lang/Float", "TYPE", arrayElementType));
				break;
			case Constants.T_SHORT:
				il.append(fact.createGetStatic("java/lang/Short", "TYPE", arrayElementType));
				break;
			default:
				il.append(fact.createConstant(arrayEntries[i]));
			}
			il.append(InstructionConstants.AASTORE);
		}
	}

	public Member getMember() {
		return member;
	}

	/**
	 * Return an object that can access a particular value of this annotation.
	 * 
	 * @param valueType The type from the annotation that is of interest
	 * @param the formal name expressed in the pointcut, can be used to disambiguate
	 * @return a variable that represents access to that annotation value
	 */
	@Override
	public Var getAccessorForValue(ResolvedType valueType, String formalName) {
		return new AnnotationAccessFieldVar(this, valueType, formalName);
	}
}
