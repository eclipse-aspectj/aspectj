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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.aspectj.apache.bcel.Constants;
import org.aspectj.apache.bcel.classfile.Attribute;
import org.aspectj.apache.bcel.classfile.Constant;
import org.aspectj.apache.bcel.classfile.ConstantUtf8;
import org.aspectj.apache.bcel.classfile.Field;
import org.aspectj.apache.bcel.classfile.JavaClass;
import org.aspectj.apache.bcel.classfile.LocalVariable;
import org.aspectj.apache.bcel.classfile.LocalVariableTable;
import org.aspectj.apache.bcel.classfile.Method;
import org.aspectj.apache.bcel.classfile.Unknown;
import org.aspectj.apache.bcel.classfile.annotation.AnnotationGen;
import org.aspectj.apache.bcel.classfile.annotation.ArrayElementValue;
import org.aspectj.apache.bcel.classfile.annotation.ClassElementValue;
import org.aspectj.apache.bcel.classfile.annotation.ElementValue;
import org.aspectj.apache.bcel.classfile.annotation.NameValuePair;
import org.aspectj.apache.bcel.classfile.annotation.RuntimeAnnos;
import org.aspectj.apache.bcel.classfile.annotation.RuntimeVisAnnos;
import org.aspectj.apache.bcel.generic.Type;
import org.aspectj.asm.AsmManager;
import org.aspectj.asm.IHierarchy;
import org.aspectj.asm.IProgramElement;
import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.IMessageHandler;
import org.aspectj.bridge.ISourceLocation;
import org.aspectj.bridge.Message;
import org.aspectj.bridge.MessageUtil;
import org.aspectj.weaver.Advice;
import org.aspectj.weaver.AdviceKind;
import org.aspectj.weaver.AjAttribute;
import org.aspectj.weaver.AjAttribute.WeaverVersionInfo;
import org.aspectj.weaver.AjcMemberMaker;
import org.aspectj.weaver.BindingScope;
import org.aspectj.weaver.ISourceContext;
import org.aspectj.weaver.MethodDelegateTypeMunger;
import org.aspectj.weaver.NameMangler;
import org.aspectj.weaver.ReferenceType;
import org.aspectj.weaver.ReferenceTypeDelegate;
import org.aspectj.weaver.ResolvedMember;
import org.aspectj.weaver.ResolvedPointcutDefinition;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.VersionedDataInputStream;
import org.aspectj.weaver.WeaverMessages;
import org.aspectj.weaver.World;
import org.aspectj.weaver.patterns.DeclareErrorOrWarning;
import org.aspectj.weaver.patterns.DeclareParents;
import org.aspectj.weaver.patterns.DeclareParentsMixin;
import org.aspectj.weaver.patterns.DeclarePrecedence;
import org.aspectj.weaver.patterns.FormalBinding;
import org.aspectj.weaver.patterns.IScope;
import org.aspectj.weaver.patterns.ParserException;
import org.aspectj.weaver.patterns.PatternParser;
import org.aspectj.weaver.patterns.PerCflow;
import org.aspectj.weaver.patterns.PerClause;
import org.aspectj.weaver.patterns.PerFromSuper;
import org.aspectj.weaver.patterns.PerObject;
import org.aspectj.weaver.patterns.PerSingleton;
import org.aspectj.weaver.patterns.PerTypeWithin;
import org.aspectj.weaver.patterns.Pointcut;
import org.aspectj.weaver.patterns.TypePattern;

/**
 * Annotation defined aspect reader. Reads the Java 5 annotations and turns them into AjAttributes
 * 
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
public class AtAjAttributes {

	private final static List<AjAttribute> NO_ATTRIBUTES = Collections.emptyList();
	private final static String[] EMPTY_STRINGS = new String[0];
	private final static String VALUE = "value";
	private final static String ARGNAMES = "argNames";
	private final static String POINTCUT = "pointcut";
	private final static String THROWING = "throwing";
	private final static String RETURNING = "returning";
	private final static String STRING_DESC = "Ljava/lang/String;";

	/**
	 * A struct that allows to add extra arguments without always breaking the API
	 */
	private static class AjAttributeStruct {

		/**
		 * The list of AjAttribute.XXX that we are populating from the @AJ read
		 */
		List<AjAttribute> ajAttributes = new ArrayList<AjAttribute>();

		/**
		 * The resolved type (class) for which we are reading @AJ for (be it class, method, field annotations)
		 */
		final ResolvedType enclosingType;

		final ISourceContext context;
		final IMessageHandler handler;

		public AjAttributeStruct(ResolvedType type, ISourceContext sourceContext, IMessageHandler messageHandler) {
			enclosingType = type;
			context = sourceContext;
			handler = messageHandler;
		}
	}

	/**
	 * A struct when we read @AJ on method
	 * 
	 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
	 */
	private static class AjAttributeMethodStruct extends AjAttributeStruct {

		// argument names used for formal binding
		private String[] m_argumentNamesLazy = null;
		public String unparsedArgumentNames = null; // Set only if discovered as
		// argNames attribute of
		// annotation

		final Method method;
		final BcelMethod bMethod;

		public AjAttributeMethodStruct(Method method, BcelMethod bMethod, ResolvedType type, ISourceContext sourceContext,
				IMessageHandler messageHandler) {
			super(type, sourceContext, messageHandler);
			this.method = method;
			this.bMethod = bMethod;
		}

		public String[] getArgumentNames() {
			if (m_argumentNamesLazy == null) {
				m_argumentNamesLazy = getMethodArgumentNames(method, unparsedArgumentNames, this);
			}
			return m_argumentNamesLazy;
		}
	}

	/**
	 * A struct when we read @AJ on field
	 */
	private static class AjAttributeFieldStruct extends AjAttributeStruct {

		final Field field;

		// final BcelField bField;

		public AjAttributeFieldStruct(Field field, BcelField bField, ResolvedType type, ISourceContext sourceContext,
				IMessageHandler messageHandler) {
			super(type, sourceContext, messageHandler);
			this.field = field;
			// this.bField = bField;
		}
	}

	/**
	 * Annotations are RuntimeVisible only. This allow us to not visit RuntimeInvisible ones.
	 * 
	 * @param attribute
	 * @return true if runtime visible annotation
	 */
	public static boolean acceptAttribute(Attribute attribute) {
		return (attribute instanceof RuntimeVisAnnos);
	}

	/**
	 * Extract class level annotations and turn them into AjAttributes.
	 * 
	 * @param javaClass
	 * @param type
	 * @param context
	 * @param msgHandler
	 * @return list of AjAttributes
	 */
	public static List<AjAttribute> readAj5ClassAttributes(AsmManager model, JavaClass javaClass, ReferenceType type,
			ISourceContext context, IMessageHandler msgHandler, boolean isCodeStyleAspect) {
		boolean ignoreThisClass = javaClass.getClassName().charAt(0) == 'o'
				&& javaClass.getClassName().startsWith("org.aspectj.lang.annotation");
		if (ignoreThisClass) {
			return NO_ATTRIBUTES;
		}
		boolean containsPointcut = false;
		boolean containsAnnotationClassReference = false;
		Constant[] cpool = javaClass.getConstantPool().getConstantPool();
		for (int i = 0; i < cpool.length; i++) {
			Constant constant = cpool[i];
			if (constant != null && constant.getTag() == Constants.CONSTANT_Utf8) {
				String constantValue = ((ConstantUtf8) constant).getValue();
				if (constantValue.length() > 28 && constantValue.charAt(1) == 'o') {
					if (constantValue.startsWith("Lorg/aspectj/lang/annotation")) {
						containsAnnotationClassReference = true;
						if ("Lorg/aspectj/lang/annotation/DeclareAnnotation;".equals(constantValue)) {
							msgHandler.handleMessage(new Message(
									"Found @DeclareAnnotation while current release does not support it (see '" + type.getName()
											+ "')", IMessage.WARNING, null, type.getSourceLocation()));
						}
						if ("Lorg/aspectj/lang/annotation/Pointcut;".equals(constantValue)) {
							containsPointcut = true;
						}
					}

				}
			}
		}
		if (!containsAnnotationClassReference) {
			return NO_ATTRIBUTES;
		}

		AjAttributeStruct struct = new AjAttributeStruct(type, context, msgHandler);
		Attribute[] attributes = javaClass.getAttributes();
		boolean hasAtAspectAnnotation = false;
		boolean hasAtPrecedenceAnnotation = false;

		WeaverVersionInfo wvinfo = null;
		for (int i = 0; i < attributes.length; i++) {
			Attribute attribute = attributes[i];
			if (acceptAttribute(attribute)) {
				RuntimeAnnos rvs = (RuntimeAnnos) attribute;
				// we don't need to look for several attribute occurrences since
				// it cannot happen as per JSR175
				if (!isCodeStyleAspect && !javaClass.isInterface()) {
					hasAtAspectAnnotation = handleAspectAnnotation(rvs, struct);
					// TODO AV - if put outside the if isCodeStyleAspect then we
					// would enable mix style
					hasAtPrecedenceAnnotation = handlePrecedenceAnnotation(rvs, struct);
				}
				// there can only be one RuntimeVisible bytecode attribute
				break;
			}
		}
		for (int i = attributes.length - 1; i >= 0; i--) {
			Attribute attribute = attributes[i];
			if (attribute.getName().equals(WeaverVersionInfo.AttributeName)) {
				try {
					VersionedDataInputStream s = new VersionedDataInputStream(new ByteArrayInputStream(
							((Unknown) attribute).getBytes()), null);
					wvinfo = WeaverVersionInfo.read(s);
					struct.ajAttributes.add(0, wvinfo);
				} catch (IOException ioe) {
					ioe.printStackTrace();
				}
			}
		}
		if (wvinfo == null) {
			// If we are in here due to a resetState() call (presumably because of reweavable state processing), the
			// original type delegate will have been set with a version but that version will be missing from
			// the new set of attributes (looks like a bug where the version attribute was not included in the
			// data compressed into the attribute). So rather than 'defaulting' to current, we should use one
			// if it set on the delegate for the type.
			ReferenceTypeDelegate delegate = type.getDelegate();
			if (delegate instanceof BcelObjectType) {
				wvinfo = ((BcelObjectType) delegate).getWeaverVersionAttribute();
				if (wvinfo != null) {
					if (wvinfo.getMajorVersion() != WeaverVersionInfo.WEAVER_VERSION_MAJOR_UNKNOWN) {
						// use this one
						struct.ajAttributes.add(0, wvinfo);
					} else {
						wvinfo = null;
					}
				}
			}
			if (wvinfo == null) {
				struct.ajAttributes.add(0, wvinfo = new AjAttribute.WeaverVersionInfo());
			}
		}

		// basic semantic check
		if (hasAtPrecedenceAnnotation && !hasAtAspectAnnotation) {
			msgHandler.handleMessage(new Message("Found @DeclarePrecedence on a non @Aspect type '" + type.getName() + "'",
					IMessage.WARNING, null, type.getSourceLocation()));
			// bypass what we have read
			return NO_ATTRIBUTES;
		}

		// the following block will not detect @Pointcut in non @Aspect types
		// for optimization purpose
		if (!(hasAtAspectAnnotation || isCodeStyleAspect) && !containsPointcut) {
			return NO_ATTRIBUTES;
		}

		// FIXME AV - turn on when ajcMightHaveAspect
		// if (hasAtAspectAnnotation && type.isInterface()) {
		// msgHandler.handleMessage(
		// new Message(
		// "Found @Aspect on an interface type '" + type.getName() + "'",
		// IMessage.WARNING,
		// null,
		// type.getSourceLocation()
		// )
		// );
		// // bypass what we have read
		// return EMPTY_LIST;
		// }

		// semantic check: @Aspect must be public
		// FIXME AV - do we really want to enforce that?
		// if (hasAtAspectAnnotation && !javaClass.isPublic()) {
		// msgHandler.handleMessage(
		// new Message(
		// "Found @Aspect annotation on a non public class '" +
		// javaClass.getClassName() + "'",
		// IMessage.ERROR,
		// null,
		// type.getSourceLocation()
		// )
		// );
		// return EMPTY_LIST;
		// }

		// code style pointcuts are class attributes
		// we need to gather the @AJ pointcut right now and not at method level
		// annotation extraction time
		// in order to be able to resolve the pointcut references later on
		// we don't need to look in super class, the pointcut reference in the
		// grammar will do it

		for (int i = 0; i < javaClass.getMethods().length; i++) {
			Method method = javaClass.getMethods()[i];
			if (method.getName().startsWith(NameMangler.PREFIX)) {
				continue; // already dealt with by ajc...
			}
			// FIXME alex optimize, this method struct will gets recreated for
			// advice extraction
			AjAttributeMethodStruct mstruct = null;
			boolean processedPointcut = false;
			Attribute[] mattributes = method.getAttributes();
			for (int j = 0; j < mattributes.length; j++) {
				Attribute mattribute = mattributes[j];
				if (acceptAttribute(mattribute)) {
					// TODO speed all this nonsense up rather than looking
					// through all the annotations every time
					// same for fields
					mstruct = new AjAttributeMethodStruct(method, null, type, context, msgHandler);
					processedPointcut = handlePointcutAnnotation((RuntimeAnnos) mattribute, mstruct);
					if (!processedPointcut) {
						processedPointcut = handleDeclareMixinAnnotation((RuntimeAnnos) mattribute, mstruct);
					}
					// there can only be one RuntimeVisible bytecode attribute
					break;
				}
			}
			if (processedPointcut) {
				struct.ajAttributes.addAll(mstruct.ajAttributes);
			}
		}

		// code style declare error / warning / implements / parents are field
		// attributes
		Field[] fs = javaClass.getFields();
		for (int i = 0; i < fs.length; i++) {
			Field field = fs[i];
			if (field.getName().startsWith(NameMangler.PREFIX)) {
				continue; // already dealt with by ajc...
			}
			// FIXME alex optimize, this method struct will gets recreated for
			// advice extraction
			AjAttributeFieldStruct fstruct = new AjAttributeFieldStruct(field, null, type, context, msgHandler);
			Attribute[] fattributes = field.getAttributes();

			for (int j = 0; j < fattributes.length; j++) {
				Attribute fattribute = fattributes[j];
				if (acceptAttribute(fattribute)) {
					RuntimeAnnos frvs = (RuntimeAnnos) fattribute;
					if (handleDeclareErrorOrWarningAnnotation(model, frvs, fstruct)
							|| handleDeclareParentsAnnotation(frvs, fstruct)) {
						// semantic check - must be in an @Aspect [remove if
						// previous block bypassed in advance]
						if (!type.isAnnotationStyleAspect() && !isCodeStyleAspect) {
							msgHandler.handleMessage(new Message("Found @AspectJ annotations in a non @Aspect type '"
									+ type.getName() + "'", IMessage.WARNING, null, type.getSourceLocation()));
							// go ahead
						}
					}
					// there can only be one RuntimeVisible bytecode attribute
					break;
				}
			}
			struct.ajAttributes.addAll(fstruct.ajAttributes);
		}

		return struct.ajAttributes;
	}

	/**
	 * Extract method level annotations and turn them into AjAttributes.
	 * 
	 * @param method
	 * @param type
	 * @param context
	 * @param msgHandler
	 * @return list of AjAttributes
	 */
	public static List<AjAttribute> readAj5MethodAttributes(Method method, BcelMethod bMethod, ResolvedType type,
			ResolvedPointcutDefinition preResolvedPointcut, ISourceContext context, IMessageHandler msgHandler) {
		if (method.getName().startsWith(NameMangler.PREFIX)) {
			return Collections.emptyList(); // already dealt with by ajc...
		}

		AjAttributeMethodStruct struct = new AjAttributeMethodStruct(method, bMethod, type, context, msgHandler);
		Attribute[] attributes = method.getAttributes();

		// we remember if we found one @AJ annotation for minimal semantic error
		// reporting
		// the real reporting beeing done thru AJDT and the compiler mapping @AJ
		// to AjAtttribute
		// or thru APT
		//
		// Note: we could actually skip the whole thing if type is not itself an
		// @Aspect
		// but then we would not see any warning. We do bypass for pointcut but
		// not for advice since it would
		// be too silent.
		boolean hasAtAspectJAnnotation = false;
		boolean hasAtAspectJAnnotationMustReturnVoid = false;
		for (int i = 0; i < attributes.length; i++) {
			Attribute attribute = attributes[i];
			try {
				if (acceptAttribute(attribute)) {
					RuntimeAnnos rvs = (RuntimeAnnos) attribute;
					hasAtAspectJAnnotationMustReturnVoid = hasAtAspectJAnnotationMustReturnVoid
							|| handleBeforeAnnotation(rvs, struct, preResolvedPointcut);
					hasAtAspectJAnnotationMustReturnVoid = hasAtAspectJAnnotationMustReturnVoid
							|| handleAfterAnnotation(rvs, struct, preResolvedPointcut);
					hasAtAspectJAnnotationMustReturnVoid = hasAtAspectJAnnotationMustReturnVoid
							|| handleAfterReturningAnnotation(rvs, struct, preResolvedPointcut, bMethod);
					hasAtAspectJAnnotationMustReturnVoid = hasAtAspectJAnnotationMustReturnVoid
							|| handleAfterThrowingAnnotation(rvs, struct, preResolvedPointcut, bMethod);
					hasAtAspectJAnnotation = hasAtAspectJAnnotation || handleAroundAnnotation(rvs, struct, preResolvedPointcut);
					// there can only be one RuntimeVisible bytecode attribute
					break;
				}
			} catch (ReturningFormalNotDeclaredInAdviceSignatureException e) {
				msgHandler.handleMessage(new Message(WeaverMessages.format(WeaverMessages.RETURNING_FORMAL_NOT_DECLARED_IN_ADVICE,
						e.getFormalName()), IMessage.ERROR, null, bMethod.getSourceLocation()));
			} catch (ThrownFormalNotDeclaredInAdviceSignatureException e) {
				msgHandler.handleMessage(new Message(WeaverMessages.format(WeaverMessages.THROWN_FORMAL_NOT_DECLARED_IN_ADVICE,
						e.getFormalName()), IMessage.ERROR, null, bMethod.getSourceLocation()));
			}
		}
		hasAtAspectJAnnotation = hasAtAspectJAnnotation || hasAtAspectJAnnotationMustReturnVoid;

		// semantic check - must be in an @Aspect [remove if previous block
		// bypassed in advance]
		if (hasAtAspectJAnnotation && !type.isAspect()) { // isAnnotationStyleAspect())
			// {
			msgHandler.handleMessage(new Message("Found @AspectJ annotations in a non @Aspect type '" + type.getName() + "'",
					IMessage.WARNING, null, type.getSourceLocation()));
			// go ahead
		}
		// semantic check - advice must be public
		if (hasAtAspectJAnnotation && !struct.method.isPublic()) {
			msgHandler.handleMessage(new Message("Found @AspectJ annotation on a non public advice '"
					+ methodToString(struct.method) + "'", IMessage.ERROR, null, type.getSourceLocation()));
			// go ahead
		}

		// semantic check - advice must not be static
		if (hasAtAspectJAnnotation && struct.method.isStatic()) {
			msgHandler.handleMessage(MessageUtil.error("Advice cannot be declared static '" + methodToString(struct.method) + "'",
					type.getSourceLocation()));
			// new Message(
			// "Advice cannot be declared static '" +
			// methodToString(struct.method) + "'",
			// IMessage.ERROR,
			// null,
			// type.getSourceLocation()
			// )
			// );
			// go ahead
		}

		// semantic check for non around advice must return void
		if (hasAtAspectJAnnotationMustReturnVoid && !Type.VOID.equals(struct.method.getReturnType())) {
			msgHandler.handleMessage(new Message("Found @AspectJ annotation on a non around advice not returning void '"
					+ methodToString(struct.method) + "'", IMessage.ERROR, null, type.getSourceLocation()));
			// go ahead
		}

		return struct.ajAttributes;
	}

	/**
	 * Extract field level annotations and turn them into AjAttributes.
	 * 
	 * @param field
	 * @param type
	 * @param context
	 * @param msgHandler
	 * @return list of AjAttributes, always empty for now
	 */
	public static List<AjAttribute> readAj5FieldAttributes(Field field, BcelField bField, ResolvedType type,
			ISourceContext context, IMessageHandler msgHandler) {
		// Note: field annotation are for ITD and DEOW - processed at class
		// level directly
		return Collections.emptyList();
	}

	/**
	 * Read @Aspect
	 * 
	 * @param runtimeAnnotations
	 * @param struct
	 * @return true if found
	 */
	private static boolean handleAspectAnnotation(RuntimeAnnos runtimeAnnotations, AjAttributeStruct struct) {
		AnnotationGen aspect = getAnnotation(runtimeAnnotations, AjcMemberMaker.ASPECT_ANNOTATION);
		if (aspect != null) {
			// semantic check for inheritance (only one level up)
			boolean extendsAspect = false;
			if (!"java.lang.Object".equals(struct.enclosingType.getSuperclass().getName())) {
				if (!struct.enclosingType.getSuperclass().isAbstract() && struct.enclosingType.getSuperclass().isAspect()) {
					reportError("cannot extend a concrete aspect", struct);
					return false;
				}
				extendsAspect = struct.enclosingType.getSuperclass().isAspect();
			}

			NameValuePair aspectPerClause = getAnnotationElement(aspect, VALUE);
			final PerClause perClause;
			if (aspectPerClause == null) {
				// empty value means singleton unless inherited
				if (!extendsAspect) {
					perClause = new PerSingleton();
				} else {
					perClause = new PerFromSuper(struct.enclosingType.getSuperclass().getPerClause().getKind());
				}
			} else {
				String perX = aspectPerClause.getValue().stringifyValue();
				if (perX == null || perX.length() <= 0) {
					perClause = new PerSingleton();
				} else {
					perClause = parsePerClausePointcut(perX, struct);
				}
			}
			if (perClause == null) {
				// could not parse it, ignore the aspect
				return false;
			} else {
				perClause.setLocation(struct.context, -1, -1);// struct.context.getOffset(),
				// struct.context.getOffset()+1);//FIXME
				// AVASM
				// Not setting version here
				// struct.ajAttributes.add(new AjAttribute.WeaverVersionInfo());
				AjAttribute.Aspect aspectAttribute = new AjAttribute.Aspect(perClause);
				struct.ajAttributes.add(aspectAttribute);
				FormalBinding[] bindings = new org.aspectj.weaver.patterns.FormalBinding[0];
				final IScope binding;
				binding = new BindingScope(struct.enclosingType, struct.context, bindings);

				// // we can't resolve here since the perclause typically refers
				// to pointcuts
				// // defined in the aspect that we haven't told the
				// BcelObjectType about yet.
				//
				// perClause.resolve(binding);

				// so we prepare to do it later...
				aspectAttribute.setResolutionScope(binding);
				return true;
			}
		}
		return false;
	}

	/**
	 * Read a perClause, returns null on failure and issue messages
	 * 
	 * @param perClauseString like "pertarget(.....)"
	 * @param struct for which we are parsing the per clause
	 * @return a PerClause instance
	 */
	private static PerClause parsePerClausePointcut(String perClauseString, AjAttributeStruct struct) {
		final String pointcutString;
		Pointcut pointcut = null;
		TypePattern typePattern = null;
		final PerClause perClause;
		if (perClauseString.startsWith(PerClause.KindAnnotationPrefix.PERCFLOW.getName())) {
			pointcutString = PerClause.KindAnnotationPrefix.PERCFLOW.extractPointcut(perClauseString);
			pointcut = parsePointcut(pointcutString, struct, false);
			perClause = new PerCflow(pointcut, false);
		} else if (perClauseString.startsWith(PerClause.KindAnnotationPrefix.PERCFLOWBELOW.getName())) {
			pointcutString = PerClause.KindAnnotationPrefix.PERCFLOWBELOW.extractPointcut(perClauseString);
			pointcut = parsePointcut(pointcutString, struct, false);
			perClause = new PerCflow(pointcut, true);
		} else if (perClauseString.startsWith(PerClause.KindAnnotationPrefix.PERTARGET.getName())) {
			pointcutString = PerClause.KindAnnotationPrefix.PERTARGET.extractPointcut(perClauseString);
			pointcut = parsePointcut(pointcutString, struct, false);
			perClause = new PerObject(pointcut, false);
		} else if (perClauseString.startsWith(PerClause.KindAnnotationPrefix.PERTHIS.getName())) {
			pointcutString = PerClause.KindAnnotationPrefix.PERTHIS.extractPointcut(perClauseString);
			pointcut = parsePointcut(pointcutString, struct, false);
			perClause = new PerObject(pointcut, true);
		} else if (perClauseString.startsWith(PerClause.KindAnnotationPrefix.PERTYPEWITHIN.getName())) {
			pointcutString = PerClause.KindAnnotationPrefix.PERTYPEWITHIN.extractPointcut(perClauseString);
			typePattern = parseTypePattern(pointcutString, struct);
			perClause = new PerTypeWithin(typePattern);
		} else if (perClauseString.equalsIgnoreCase(PerClause.SINGLETON.getName() + "()")) {
			perClause = new PerSingleton();
		} else {
			// could not parse the @AJ perclause - fallback to singleton and
			// issue an error
			reportError("@Aspect per clause cannot be read '" + perClauseString + "'", struct);
			return null;
		}

		if (!PerClause.SINGLETON.equals(perClause.getKind()) && !PerClause.PERTYPEWITHIN.equals(perClause.getKind())
				&& pointcut == null) {
			// we could not parse the pointcut
			return null;
		}
		if (PerClause.PERTYPEWITHIN.equals(perClause.getKind()) && typePattern == null) {
			// we could not parse the type pattern
			return null;
		}
		return perClause;
	}

	/**
	 * Read @DeclarePrecedence
	 * 
	 * @param runtimeAnnotations
	 * @param struct
	 * @return true if found
	 */
	private static boolean handlePrecedenceAnnotation(RuntimeAnnos runtimeAnnotations, AjAttributeStruct struct) {
		AnnotationGen aspect = getAnnotation(runtimeAnnotations, AjcMemberMaker.DECLAREPRECEDENCE_ANNOTATION);
		if (aspect != null) {
			NameValuePair precedence = getAnnotationElement(aspect, VALUE);
			if (precedence != null) {
				String precedencePattern = precedence.getValue().stringifyValue();
				PatternParser parser = new PatternParser(precedencePattern);
				DeclarePrecedence ajPrecedence = parser.parseDominates();
				struct.ajAttributes.add(new AjAttribute.DeclareAttribute(ajPrecedence));
				return true;
			}
		}
		return false;
	}

	// /**
	// * Read @DeclareImplements
	// *
	// * @param runtimeAnnotations
	// * @param struct
	// * @return true if found
	// */
	// private static boolean
	// handleDeclareImplementsAnnotation(RuntimeAnnotations runtimeAnnotations,
	// AjAttributeFieldStruct
	// struct) {//, ResolvedPointcutDefinition preResolvedPointcut) {
	// Annotation deci = getAnnotation(runtimeAnnotations,
	// AjcMemberMaker.DECLAREIMPLEMENTS_ANNOTATION);
	// if (deci != null) {
	// ElementNameValuePairGen deciPatternNVP = getAnnotationElement(deci,
	// VALUE);
	// String deciPattern = deciPatternNVP.getValue().stringifyValue();
	// if (deciPattern != null) {
	// TypePattern typePattern = parseTypePattern(deciPattern, struct);
	// ResolvedType fieldType =
	// UnresolvedType.forSignature(struct.field.getSignature()).resolve(struct.enclosingType.getWorld());
	// if (fieldType.isPrimitiveType()) {
	// return false;
	// } else if (fieldType.isInterface()) {
	// TypePattern parent = new
	// ExactTypePattern(UnresolvedType.forSignature(struct.field.getSignature()),
	// false, false);
	// parent.resolve(struct.enclosingType.getWorld());
	// List parents = new ArrayList(1);
	// parents.add(parent);
	// //TODO kick ISourceLocation sl = struct.bField.getSourceLocation(); ??
	// struct.ajAttributes.add(
	// new AjAttribute.DeclareAttribute(
	// new DeclareParents(
	// typePattern,
	// parents,
	// false
	// )
	// )
	// );
	// return true;
	// } else {
	// reportError("@DeclareImplements: can only be used on field whose type is an interface",
	// struct);
	// return false;
	// }
	// }
	// }
	// return false;
	// }

	/**
	 * Read @DeclareParents
	 * 
	 * @param runtimeAnnotations
	 * @param struct
	 * @return true if found
	 */
	private static boolean handleDeclareParentsAnnotation(RuntimeAnnos runtimeAnnotations, AjAttributeFieldStruct struct) {// ,
		// ResolvedPointcutDefinition
		// preResolvedPointcut)
		// {
		AnnotationGen decp = getAnnotation(runtimeAnnotations, AjcMemberMaker.DECLAREPARENTS_ANNOTATION);
		if (decp != null) {
			NameValuePair decpPatternNVP = getAnnotationElement(decp, VALUE);
			String decpPattern = decpPatternNVP.getValue().stringifyValue();
			if (decpPattern != null) {
				TypePattern typePattern = parseTypePattern(decpPattern, struct);
				ResolvedType fieldType = UnresolvedType.forSignature(struct.field.getSignature()).resolve(
						struct.enclosingType.getWorld());
				if (fieldType.isInterface()) {
					TypePattern parent = parseTypePattern(fieldType.getName(), struct);
					FormalBinding[] bindings = new org.aspectj.weaver.patterns.FormalBinding[0];
					IScope binding = new BindingScope(struct.enclosingType, struct.context, bindings);
					// first add the declare implements like
					List<TypePattern> parents = new ArrayList<TypePattern>(1);
					parents.add(parent);
					DeclareParents dp = new DeclareParents(typePattern, parents, false);
					dp.resolve(binding); // resolves the parent and child parts of the decp

					// resolve this so that we can use it for the
					// MethodDelegateMungers below.
					// eg. '@Coloured *' will change from a WildTypePattern to
					// an 'AnyWithAnnotationTypePattern' after this resolution
					typePattern = dp.getChild(); // this retrieves the resolved version
					// TODO kick ISourceLocation sl =
					// struct.bField.getSourceLocation(); ??
					// dp.setLocation(dp.getDeclaringType().getSourceContext(),
					// dp.getDeclaringType().getSourceLocation().getOffset(),
					// dp.getDeclaringType().getSourceLocation().getOffset());
					dp.setLocation(struct.context, -1, -1); // not ideal...
					struct.ajAttributes.add(new AjAttribute.DeclareAttribute(dp));

					// do we have a defaultImpl=xxx.class (ie implementation)
					String defaultImplClassName = null;
					NameValuePair defaultImplNVP = getAnnotationElement(decp, "defaultImpl");
					if (defaultImplNVP != null) {
						ClassElementValue defaultImpl = (ClassElementValue) defaultImplNVP.getValue();
						defaultImplClassName = UnresolvedType.forSignature(defaultImpl.getClassString()).getName();
						if (defaultImplClassName.equals("org.aspectj.lang.annotation.DeclareParents")) {
							defaultImplClassName = null;
						} else {
							// check public no arg ctor
							ResolvedType impl = struct.enclosingType.getWorld().resolve(defaultImplClassName, false);
							ResolvedMember[] mm = impl.getDeclaredMethods();
							int implModifiers = impl.getModifiers();
							boolean defaultVisibilityImpl = !(Modifier.isPrivate(implModifiers)
									|| Modifier.isProtected(implModifiers) || Modifier.isPublic(implModifiers));
							boolean hasNoCtorOrANoArgOne = true;
							ResolvedMember foundOneOfIncorrectVisibility = null;
							for (int i = 0; i < mm.length; i++) {
								ResolvedMember resolvedMember = mm[i];
								if (resolvedMember.getName().equals("<init>")) {
									hasNoCtorOrANoArgOne = false;

									if (resolvedMember.getParameterTypes().length == 0) {
										if (defaultVisibilityImpl) { // default
											// visibility
											// implementation
											if (resolvedMember.isPublic() || resolvedMember.isDefault()) {
												hasNoCtorOrANoArgOne = true;
											} else {
												foundOneOfIncorrectVisibility = resolvedMember;
											}
										} else if (Modifier.isPublic(implModifiers)) { // public
											// implementation
											if (resolvedMember.isPublic()) {
												hasNoCtorOrANoArgOne = true;
											} else {
												foundOneOfIncorrectVisibility = resolvedMember;
											}
										}
									}
								}
								if (hasNoCtorOrANoArgOne) {
									break;
								}
							}
							if (!hasNoCtorOrANoArgOne) {
								if (foundOneOfIncorrectVisibility != null) {
									reportError(
											"@DeclareParents: defaultImpl=\""
													+ defaultImplClassName
													+ "\" has a no argument constructor, but it is of incorrect visibility.  It must be at least as visible as the type.",
											struct);
								} else {
									reportError("@DeclareParents: defaultImpl=\"" + defaultImplClassName
											+ "\" has no public no-arg constructor", struct);
								}
							}
							if (!fieldType.isAssignableFrom(impl)) {
								reportError("@DeclareParents: defaultImpl=\"" + defaultImplClassName
										+ "\" does not implement the interface '" + fieldType.toString() + "'", struct);
							}
						}

					}

					// then iterate on field interface hierarchy (not object)
					boolean hasAtLeastOneMethod = false;
					Iterator<ResolvedMember> methodIterator = fieldType.getMethodsIncludingIntertypeDeclarations(false, true);
					while (methodIterator.hasNext()) {
						ResolvedMember method = methodIterator.next();

						// ResolvedMember[] methods = fieldType.getMethodsWithoutIterator(true, false, false).toArray(
						// new ResolvedMember[0]);
						// for (int i = 0; i < methods.length; i++) {
						// ResolvedMember method = methods[i];
						if (method.isAbstract()) {
							// moved to be detected at weave time if the target
							// doesnt implement the methods
							// if (defaultImplClassName == null) {
							// // non marker interface with no default impl
							// provided
							// reportError("@DeclareParents: used with a non marker interface and no defaultImpl=\"...\" provided",
							// struct);
							// return false;
							// }
							hasAtLeastOneMethod = true;
							// What we are saying here:
							// We have this method 'method' and we want to put a
							// forwarding method into a type that matches
							// typePattern that should delegate to the version
							// of the method in 'defaultImplClassName'

							// Now the method may be from a supertype but the
							// declaring type of the method we pass into the
							// type
							// munger is what is used to determine the type of
							// the field that hosts the delegate instance.
							// So here we create a modified method with an
							// alternative declaring type so that we lookup
							// the right field. See pr164016.
							MethodDelegateTypeMunger mdtm = new MethodDelegateTypeMunger(method, struct.enclosingType,
									defaultImplClassName, typePattern);
							mdtm.setFieldType(fieldType);
							mdtm.setSourceLocation(struct.enclosingType.getSourceLocation());
							struct.ajAttributes.add(new AjAttribute.TypeMunger(mdtm));
						}
					}
					// successfull so far, we thus need a bcel type munger to
					// have
					// a field hosting the mixin in the target type
					if (hasAtLeastOneMethod && defaultImplClassName != null) {
						ResolvedMember fieldHost = AjcMemberMaker.itdAtDeclareParentsField(null, fieldType, struct.enclosingType);
						struct.ajAttributes.add(new AjAttribute.TypeMunger(new MethodDelegateTypeMunger.FieldHostTypeMunger(
								fieldHost, struct.enclosingType, typePattern)));
					}

					return true;
				} else {
					reportError("@DeclareParents: can only be used on a field whose type is an interface", struct);
					return false;
				}
			}
		}
		return false;
	}

	/**
	 * Return a nicely formatted method string, for example: int X.foo(java.lang.String)
	 */
	public static String getMethodForMessage(AjAttributeMethodStruct methodstructure) {
		StringBuffer sb = new StringBuffer();
		sb.append("Method '");
		sb.append(methodstructure.method.getReturnType().toString());
		sb.append(" ").append(methodstructure.enclosingType).append(".").append(methodstructure.method.getName());
		sb.append("(");
		Type[] args = methodstructure.method.getArgumentTypes();
		if (args != null) {
			for (int t = 0; t < args.length; t++) {
				if (t > 0) {
					sb.append(",");
				}
				sb.append(args[t].toString());
			}
		}
		sb.append(")'");
		return sb.toString();
	}

	/**
	 * Process any @DeclareMixin annotation.
	 * 
	 * Example Declaration <br>
	 * 
	 * @DeclareMixin("Foo+") public I createImpl(Object o) { return new Impl(o); }
	 * 
	 * <br>
	 * @param runtimeAnnotations
	 * @param struct
	 * @return true if found
	 */
	private static boolean handleDeclareMixinAnnotation(RuntimeAnnos runtimeAnnotations, AjAttributeMethodStruct struct) {
		AnnotationGen declareMixinAnnotation = getAnnotation(runtimeAnnotations, AjcMemberMaker.DECLAREMIXIN_ANNOTATION);
		if (declareMixinAnnotation == null) {
			// No annotation found
			return false;
		}

		Method annotatedMethod = struct.method;
		World world = struct.enclosingType.getWorld();
		NameValuePair declareMixinPatternNameValuePair = getAnnotationElement(declareMixinAnnotation, VALUE);

		// declareMixinPattern could be of the form "Bar*" or "A || B" or "Foo+"
		String declareMixinPattern = declareMixinPatternNameValuePair.getValue().stringifyValue();
		TypePattern targetTypePattern = parseTypePattern(declareMixinPattern, struct);

		// Return value of the annotated method is the interface or class that the mixin delegate should have
		ResolvedType methodReturnType = UnresolvedType.forSignature(annotatedMethod.getReturnType().getSignature()).resolve(world);

		if (methodReturnType.isPrimitiveType()) {
			reportError(getMethodForMessage(struct) + ":  factory methods for a mixin cannot return void or a primitive type",
					struct);
			return false;
		}

		if (annotatedMethod.getArgumentTypes().length > 1) {
			reportError(getMethodForMessage(struct) + ": factory methods for a mixin can take a maximum of one parameter", struct);
			return false;
		}

		// The set of interfaces to be mixed in is either:
		// supplied as a list in the 'Class[] interfaces' value in the annotation value
		// supplied as just the interface return value of the annotated method
		// supplied as just the class return value of the annotated method
		NameValuePair interfaceListSpecified = getAnnotationElement(declareMixinAnnotation, "interfaces");

		List<TypePattern> newParents = new ArrayList<TypePattern>(1);
		List<ResolvedType> newInterfaceTypes = new ArrayList<ResolvedType>(1);
		if (interfaceListSpecified != null) {
			ArrayElementValue arrayOfInterfaceTypes = (ArrayElementValue) interfaceListSpecified.getValue();
			int numberOfTypes = arrayOfInterfaceTypes.getElementValuesArraySize();
			ElementValue[] theTypes = arrayOfInterfaceTypes.getElementValuesArray();
			for (int i = 0; i < numberOfTypes; i++) {
				ClassElementValue interfaceType = (ClassElementValue) theTypes[i];
				// Check: needs to be resolvable
				// TODO crappy replace required
				ResolvedType ajInterfaceType = UnresolvedType.forSignature(interfaceType.getClassString().replace("/", "."))
						.resolve(world);
				if (ajInterfaceType.isMissing() || !ajInterfaceType.isInterface()) {
					reportError(
							"Types listed in the 'interfaces' DeclareMixin annotation value must be valid interfaces. This is invalid: "
									+ ajInterfaceType.getName(), struct); // TODO better error location, use the method position
					return false;
				}
				if (!ajInterfaceType.isAssignableFrom(methodReturnType)) {
					reportError(getMethodForMessage(struct) + ": factory method does not return something that implements '"
							+ ajInterfaceType.getName() + "'", struct);
					return false;
				}
				newInterfaceTypes.add(ajInterfaceType);
				// Checking that it is a superinterface of the methods return value is done at weave time
				TypePattern newParent = parseTypePattern(ajInterfaceType.getName(), struct);
				newParents.add(newParent);
			}
		} else {
			if (methodReturnType.isClass()) {
				reportError(
						getMethodForMessage(struct)
								+ ": factory methods for a mixin must either return an interface type or specify interfaces in the annotation and return a class",
						struct);
				return false;
			}
			// Use the method return type: this might be a class or an interface
			TypePattern newParent = parseTypePattern(methodReturnType.getName(), struct);
			newInterfaceTypes.add(methodReturnType);
			newParents.add(newParent);
		}
		if (newParents.size() == 0) {
			// Warning: did they foolishly put @DeclareMixin(value="Bar+",interfaces={})
			// TODO output warning
			return false;
		}

		// Create the declare parents that will add the interfaces to matching targets
		FormalBinding[] bindings = new org.aspectj.weaver.patterns.FormalBinding[0];
		IScope binding = new BindingScope(struct.enclosingType, struct.context, bindings);
		// how do we mark this as a decp due to decmixin?
		DeclareParents dp = new DeclareParentsMixin(targetTypePattern, newParents);
		dp.resolve(binding);
		targetTypePattern = dp.getChild();

		dp.setLocation(struct.context, -1, -1); // not ideal...
		struct.ajAttributes.add(new AjAttribute.DeclareAttribute(dp));

		// The factory method for building the implementation is the
		// one attached to the annotation:
		// Method implementationFactory = struct.method;

		boolean hasAtLeastOneMethod = false;

		for (Iterator<ResolvedType> iterator = newInterfaceTypes.iterator(); iterator.hasNext();) {
			ResolvedType typeForDelegation = iterator.next();
			// TODO check for overlapping interfaces. Eg. A implements I, I extends J - if they specify interfaces={I,J} we dont
			// want to do any methods twice
			ResolvedMember[] methods = typeForDelegation.getMethodsWithoutIterator(true, false, false).toArray(
					new ResolvedMember[0]);
			for (int i = 0; i < methods.length; i++) {
				ResolvedMember method = methods[i];
				if (method.isAbstract()) {
					hasAtLeastOneMethod = true;
					if (method.hasBackingGenericMember()) {
						method = method.getBackingGenericMember();
					}
					MethodDelegateTypeMunger mdtm = new MethodDelegateTypeMunger(method, struct.enclosingType, "",
							targetTypePattern, struct.method.getName(), struct.method.getSignature());
					mdtm.setFieldType(methodReturnType);
					mdtm.setSourceLocation(struct.enclosingType.getSourceLocation());
					struct.ajAttributes.add(new AjAttribute.TypeMunger(mdtm));
				}
			}
		}
		// if any method delegate was created then a field to hold the delegate instance must also be added
		if (hasAtLeastOneMethod) {
			ResolvedMember fieldHost = AjcMemberMaker.itdAtDeclareParentsField(null, methodReturnType, struct.enclosingType);
			struct.ajAttributes.add(new AjAttribute.TypeMunger(new MethodDelegateTypeMunger.FieldHostTypeMunger(fieldHost,
					struct.enclosingType, targetTypePattern)));
		}
		return true;
	}

	/**
	 * Read @Before
	 * 
	 * @param runtimeAnnotations
	 * @param struct
	 * @return true if found
	 */
	private static boolean handleBeforeAnnotation(RuntimeAnnos runtimeAnnotations, AjAttributeMethodStruct struct,
			ResolvedPointcutDefinition preResolvedPointcut) {
		AnnotationGen before = getAnnotation(runtimeAnnotations, AjcMemberMaker.BEFORE_ANNOTATION);
		if (before != null) {
			NameValuePair beforeAdvice = getAnnotationElement(before, VALUE);
			if (beforeAdvice != null) {
				// this/target/args binding
				String argumentNames = getArgNamesValue(before);
				if (argumentNames != null) {
					struct.unparsedArgumentNames = argumentNames;
				}
				FormalBinding[] bindings = new org.aspectj.weaver.patterns.FormalBinding[0];
				try {
					bindings = extractBindings(struct);
				} catch (UnreadableDebugInfoException unreadableDebugInfoException) {
					return false;
				}
				IScope binding = new BindingScope(struct.enclosingType, struct.context, bindings);

				// joinpoint, staticJoinpoint binding
				int extraArgument = extractExtraArgument(struct.method);

				Pointcut pc = null;
				if (preResolvedPointcut != null) {
					pc = preResolvedPointcut.getPointcut();
					// pc.resolve(binding);
				} else {
					pc = parsePointcut(beforeAdvice.getValue().stringifyValue(), struct, false);
					if (pc == null) {
						return false;// parse error
					}
					pc = pc.resolve(binding);
				}
				setIgnoreUnboundBindingNames(pc, bindings);

				ISourceLocation sl = struct.context.makeSourceLocation(struct.bMethod.getDeclarationLineNumber(),
						struct.bMethod.getDeclarationOffset());
				struct.ajAttributes.add(new AjAttribute.AdviceAttribute(AdviceKind.Before, pc, extraArgument, sl.getOffset(), sl
						.getOffset() + 1,// FIXME AVASM
						struct.context));
				return true;
			}
		}
		return false;
	}

	/**
	 * Read @After
	 * 
	 * @param runtimeAnnotations
	 * @param struct
	 * @return true if found
	 */
	private static boolean handleAfterAnnotation(RuntimeAnnos runtimeAnnotations, AjAttributeMethodStruct struct,
			ResolvedPointcutDefinition preResolvedPointcut) {
		AnnotationGen after = getAnnotation(runtimeAnnotations, AjcMemberMaker.AFTER_ANNOTATION);
		if (after != null) {
			NameValuePair afterAdvice = getAnnotationElement(after, VALUE);
			if (afterAdvice != null) {
				// this/target/args binding
				FormalBinding[] bindings = new org.aspectj.weaver.patterns.FormalBinding[0];
				String argumentNames = getArgNamesValue(after);
				if (argumentNames != null) {
					struct.unparsedArgumentNames = argumentNames;
				}
				try {
					bindings = extractBindings(struct);
				} catch (UnreadableDebugInfoException unreadableDebugInfoException) {
					return false;
				}
				IScope binding = new BindingScope(struct.enclosingType, struct.context, bindings);

				// joinpoint, staticJoinpoint binding
				int extraArgument = extractExtraArgument(struct.method);

				Pointcut pc = null;
				if (preResolvedPointcut != null) {
					pc = preResolvedPointcut.getPointcut();
				} else {
					pc = parsePointcut(afterAdvice.getValue().stringifyValue(), struct, false);
					if (pc == null) {
						return false;// parse error
					}
					pc.resolve(binding);
				}
				setIgnoreUnboundBindingNames(pc, bindings);

				ISourceLocation sl = struct.context.makeSourceLocation(struct.bMethod.getDeclarationLineNumber(),
						struct.bMethod.getDeclarationOffset());
				struct.ajAttributes.add(new AjAttribute.AdviceAttribute(AdviceKind.After, pc, extraArgument, sl.getOffset(), sl
						.getOffset() + 1,// FIXME AVASM
						struct.context));
				return true;
			}
		}
		return false;
	}

	/**
	 * Read @AfterReturning
	 * 
	 * @param runtimeAnnotations
	 * @param struct
	 * @return true if found
	 */
	private static boolean handleAfterReturningAnnotation(RuntimeAnnos runtimeAnnotations, AjAttributeMethodStruct struct,
			ResolvedPointcutDefinition preResolvedPointcut, BcelMethod owningMethod)
			throws ReturningFormalNotDeclaredInAdviceSignatureException {
		AnnotationGen after = getAnnotation(runtimeAnnotations, AjcMemberMaker.AFTERRETURNING_ANNOTATION);
		if (after != null) {
			NameValuePair annValue = getAnnotationElement(after, VALUE);
			NameValuePair annPointcut = getAnnotationElement(after, POINTCUT);
			NameValuePair annReturned = getAnnotationElement(after, RETURNING);

			// extract the pointcut and returned type/binding - do some checks
			String pointcut = null;
			String returned = null;
			if ((annValue != null && annPointcut != null) || (annValue == null && annPointcut == null)) {
				reportError("@AfterReturning: either 'value' or 'poincut' must be provided, not both", struct);
				return false;
			}
			if (annValue != null) {
				pointcut = annValue.getValue().stringifyValue();
			} else {
				pointcut = annPointcut.getValue().stringifyValue();
			}
			if (isNullOrEmpty(pointcut)) {
				reportError("@AfterReturning: either 'value' or 'poincut' must be provided, not both", struct);
				return false;
			}
			if (annReturned != null) {
				returned = annReturned.getValue().stringifyValue();
				if (isNullOrEmpty(returned)) {
					returned = null;
				} else {
					// check that thrownFormal exists as the last parameter in
					// the advice
					String[] pNames = owningMethod.getParameterNames();
					if (pNames == null || pNames.length == 0 || !Arrays.asList(pNames).contains(returned)) {
						throw new ReturningFormalNotDeclaredInAdviceSignatureException(returned);
					}
				}
			}
			String argumentNames = getArgNamesValue(after);
			if (argumentNames != null) {
				struct.unparsedArgumentNames = argumentNames;
			}
			// this/target/args binding
			// exclude the return binding from the pointcut binding since it is
			// an extraArg binding
			FormalBinding[] bindings = new org.aspectj.weaver.patterns.FormalBinding[0];
			try {
				bindings = (returned == null ? extractBindings(struct) : extractBindings(struct, returned));
			} catch (UnreadableDebugInfoException unreadableDebugInfoException) {
				return false;
			}
			IScope binding = new BindingScope(struct.enclosingType, struct.context, bindings);

			// joinpoint, staticJoinpoint binding
			int extraArgument = extractExtraArgument(struct.method);

			// return binding
			if (returned != null) {
				extraArgument |= Advice.ExtraArgument;
			}

			Pointcut pc = null;
			if (preResolvedPointcut != null) {
				pc = preResolvedPointcut.getPointcut();
			} else {
				pc = parsePointcut(pointcut, struct, false);
				if (pc == null) {
					return false;// parse error
				}
				pc.resolve(binding);
			}
			setIgnoreUnboundBindingNames(pc, bindings);

			ISourceLocation sl = struct.context.makeSourceLocation(struct.bMethod.getDeclarationLineNumber(),
					struct.bMethod.getDeclarationOffset());
			struct.ajAttributes.add(new AjAttribute.AdviceAttribute(AdviceKind.AfterReturning, pc, extraArgument, sl.getOffset(),
					sl.getOffset() + 1,// FIXME AVASM
					struct.context));
			return true;
		}
		return false;
	}

	/**
	 * Read @AfterThrowing
	 * 
	 * @param runtimeAnnotations
	 * @param struct
	 * @return true if found
	 */
	private static boolean handleAfterThrowingAnnotation(RuntimeAnnos runtimeAnnotations, AjAttributeMethodStruct struct,
			ResolvedPointcutDefinition preResolvedPointcut, BcelMethod owningMethod)
			throws ThrownFormalNotDeclaredInAdviceSignatureException {
		AnnotationGen after = getAnnotation(runtimeAnnotations, AjcMemberMaker.AFTERTHROWING_ANNOTATION);
		if (after != null) {
			NameValuePair annValue = getAnnotationElement(after, VALUE);
			NameValuePair annPointcut = getAnnotationElement(after, POINTCUT);
			NameValuePair annThrown = getAnnotationElement(after, THROWING);

			// extract the pointcut and throwned type/binding - do some checks
			String pointcut = null;
			String thrownFormal = null;
			if ((annValue != null && annPointcut != null) || (annValue == null && annPointcut == null)) {
				reportError("@AfterThrowing: either 'value' or 'poincut' must be provided, not both", struct);
				return false;
			}
			if (annValue != null) {
				pointcut = annValue.getValue().stringifyValue();
			} else {
				pointcut = annPointcut.getValue().stringifyValue();
			}
			if (isNullOrEmpty(pointcut)) {
				reportError("@AfterThrowing: either 'value' or 'poincut' must be provided, not both", struct);
				return false;
			}
			if (annThrown != null) {
				thrownFormal = annThrown.getValue().stringifyValue();
				if (isNullOrEmpty(thrownFormal)) {
					thrownFormal = null;
				} else {
					// check that thrownFormal exists as the last parameter in
					// the advice
					String[] pNames = owningMethod.getParameterNames();
					if (pNames == null || pNames.length == 0 || !Arrays.asList(pNames).contains(thrownFormal)) {
						throw new ThrownFormalNotDeclaredInAdviceSignatureException(thrownFormal);
					}
				}
			}
			String argumentNames = getArgNamesValue(after);
			if (argumentNames != null) {
				struct.unparsedArgumentNames = argumentNames;
			}
			// this/target/args binding
			// exclude the throwned binding from the pointcut binding since it
			// is an extraArg binding
			FormalBinding[] bindings = new org.aspectj.weaver.patterns.FormalBinding[0];
			try {
				bindings = (thrownFormal == null ? extractBindings(struct) : extractBindings(struct, thrownFormal));
			} catch (UnreadableDebugInfoException unreadableDebugInfoException) {
				return false;
			}
			IScope binding = new BindingScope(struct.enclosingType, struct.context, bindings);

			// joinpoint, staticJoinpoint binding
			int extraArgument = extractExtraArgument(struct.method);

			// return binding
			if (thrownFormal != null) {
				extraArgument |= Advice.ExtraArgument;
			}

			Pointcut pc = null;
			if (preResolvedPointcut != null) {
				pc = preResolvedPointcut.getPointcut();
			} else {
				pc = parsePointcut(pointcut, struct, false);
				if (pc == null) {
					return false;// parse error
				}
				pc.resolve(binding);
			}
			setIgnoreUnboundBindingNames(pc, bindings);

			ISourceLocation sl = struct.context.makeSourceLocation(struct.bMethod.getDeclarationLineNumber(),
					struct.bMethod.getDeclarationOffset());
			struct.ajAttributes.add(new AjAttribute.AdviceAttribute(AdviceKind.AfterThrowing, pc, extraArgument, sl.getOffset(), sl
					.getOffset() + 1, struct.context));
			return true;
		}
		return false;
	}

	/**
	 * Read @Around
	 * 
	 * @param runtimeAnnotations
	 * @param struct
	 * @return true if found
	 */
	private static boolean handleAroundAnnotation(RuntimeAnnos runtimeAnnotations, AjAttributeMethodStruct struct,
			ResolvedPointcutDefinition preResolvedPointcut) {
		AnnotationGen around = getAnnotation(runtimeAnnotations, AjcMemberMaker.AROUND_ANNOTATION);
		if (around != null) {
			NameValuePair aroundAdvice = getAnnotationElement(around, VALUE);
			if (aroundAdvice != null) {
				// this/target/args binding
				String argumentNames = getArgNamesValue(around);
				if (argumentNames != null) {
					struct.unparsedArgumentNames = argumentNames;
				}
				FormalBinding[] bindings = new org.aspectj.weaver.patterns.FormalBinding[0];
				try {
					bindings = extractBindings(struct);
				} catch (UnreadableDebugInfoException unreadableDebugInfoException) {
					return false;
				}
				IScope binding = new BindingScope(struct.enclosingType, struct.context, bindings);

				// joinpoint, staticJoinpoint binding
				int extraArgument = extractExtraArgument(struct.method);

				Pointcut pc = null;
				if (preResolvedPointcut != null) {
					pc = preResolvedPointcut.getPointcut();
				} else {
					pc = parsePointcut(aroundAdvice.getValue().stringifyValue(), struct, false);
					if (pc == null) {
						return false;// parse error
					}
					pc.resolve(binding);
				}
				setIgnoreUnboundBindingNames(pc, bindings);

				ISourceLocation sl = struct.context.makeSourceLocation(struct.bMethod.getDeclarationLineNumber(),
						struct.bMethod.getDeclarationOffset());
				struct.ajAttributes.add(new AjAttribute.AdviceAttribute(AdviceKind.Around, pc, extraArgument, sl.getOffset(), sl
						.getOffset() + 1,// FIXME AVASM
						struct.context));
				return true;
			}
		}
		return false;
	}

	/**
	 * Read @Pointcut and handle the resolving in a lazy way to deal with pointcut references
	 * 
	 * @param runtimeAnnotations
	 * @param struct
	 * @return true if a pointcut was handled
	 */
	private static boolean handlePointcutAnnotation(RuntimeAnnos runtimeAnnotations, AjAttributeMethodStruct struct) {
		AnnotationGen pointcut = getAnnotation(runtimeAnnotations, AjcMemberMaker.POINTCUT_ANNOTATION);
		if (pointcut == null) {
			return false;
		}
		NameValuePair pointcutExpr = getAnnotationElement(pointcut, VALUE);

		// semantic check: the method must return void, or be
		// "public static boolean" for if() support
		if (!(Type.VOID.equals(struct.method.getReturnType()) || (Type.BOOLEAN.equals(struct.method.getReturnType())
				&& struct.method.isStatic() && struct.method.isPublic()))) {
			reportWarning("Found @Pointcut on a method not returning 'void' or not 'public static boolean'", struct);
			// no need to stop
		}

		// semantic check: the method must not throw anything
		if (struct.method.getExceptionTable() != null) {
			reportWarning("Found @Pointcut on a method throwing exception", struct);
			// no need to stop
		}

		String argumentNames = getArgNamesValue(pointcut);
		if (argumentNames != null) {
			struct.unparsedArgumentNames = argumentNames;
		}
		// this/target/args binding
		final IScope binding;
		try {
			if (struct.method.isAbstract()) {
				binding = null;
			} else {
				binding = new BindingScope(struct.enclosingType, struct.context, extractBindings(struct));
			}
		} catch (UnreadableDebugInfoException e) {
			return false;
		}

		UnresolvedType[] argumentTypes = new UnresolvedType[struct.method.getArgumentTypes().length];
		for (int i = 0; i < argumentTypes.length; i++) {
			argumentTypes[i] = UnresolvedType.forSignature(struct.method.getArgumentTypes()[i].getSignature());
		}

		Pointcut pc = null;
		if (struct.method.isAbstract()) {
			if ((pointcutExpr != null && isNullOrEmpty(pointcutExpr.getValue().stringifyValue())) || pointcutExpr == null) {
				// abstract pointcut
				// leave pc = null
			} else {
				reportError("Found defined @Pointcut on an abstract method", struct);
				return false;// stop
			}
		} else {
			if (pointcutExpr == null || isNullOrEmpty(pointcutExpr.getValue().stringifyValue())) {
				// the matches nothing pointcut (125475/125480) - perhaps not as
				// cleanly supported as it could be.
			} else {
				// if (pointcutExpr != null) {
				// use a LazyResolvedPointcutDefinition so that the pointcut is
				// resolved lazily
				// since for it to be resolved, we will need other pointcuts to
				// be registered as well
				pc = parsePointcut(pointcutExpr.getValue().stringifyValue(), struct, true);
				if (pc == null) {
					return false;// parse error
				}
				pc.setLocation(struct.context, -1, -1);// FIXME AVASM !! bMethod
				// is null here..
				// } else {
				// reportError("Found undefined @Pointcut on a non-abstract method",
				// struct);
				// return false;
				// }
			}
		}
		// do not resolve binding now but lazily
		struct.ajAttributes.add(new AjAttribute.PointcutDeclarationAttribute(new LazyResolvedPointcutDefinition(
				struct.enclosingType, struct.method.getModifiers(), struct.method.getName(), argumentTypes, UnresolvedType
						.forSignature(struct.method.getReturnType().getSignature()), pc,// can
				// be
				// null
				// for
				// abstract
				// pointcut
				binding // can be null for abstract pointcut
				)));
		return true;
	}

	/**
	 * Read @DeclareError, @DeclareWarning
	 * 
	 * @param runtimeAnnotations
	 * @param struct
	 * @return true if found
	 */
	private static boolean handleDeclareErrorOrWarningAnnotation(AsmManager model, RuntimeAnnos runtimeAnnotations,
			AjAttributeFieldStruct struct) {
		AnnotationGen error = getAnnotation(runtimeAnnotations, AjcMemberMaker.DECLAREERROR_ANNOTATION);
		boolean hasError = false;
		if (error != null) {
			NameValuePair declareError = getAnnotationElement(error, VALUE);
			if (declareError != null) {
				if (!STRING_DESC.equals(struct.field.getSignature()) || struct.field.getConstantValue() == null) {
					reportError("@DeclareError used on a non String constant field", struct);
					return false;
				}
				Pointcut pc = parsePointcut(declareError.getValue().stringifyValue(), struct, false);
				if (pc == null) {
					hasError = false;// cannot parse pointcut
				} else {
					DeclareErrorOrWarning deow = new DeclareErrorOrWarning(true, pc, struct.field.getConstantValue().toString());
					setDeclareErrorOrWarningLocation(model, deow, struct);
					struct.ajAttributes.add(new AjAttribute.DeclareAttribute(deow));
					hasError = true;
				}
			}
		}
		AnnotationGen warning = getAnnotation(runtimeAnnotations, AjcMemberMaker.DECLAREWARNING_ANNOTATION);
		boolean hasWarning = false;
		if (warning != null) {
			NameValuePair declareWarning = getAnnotationElement(warning, VALUE);
			if (declareWarning != null) {
				if (!STRING_DESC.equals(struct.field.getSignature()) || struct.field.getConstantValue() == null) {
					reportError("@DeclareWarning used on a non String constant field", struct);
					return false;
				}
				Pointcut pc = parsePointcut(declareWarning.getValue().stringifyValue(), struct, false);
				if (pc == null) {
					hasWarning = false;// cannot parse pointcut
				} else {
					DeclareErrorOrWarning deow = new DeclareErrorOrWarning(false, pc, struct.field.getConstantValue().toString());
					setDeclareErrorOrWarningLocation(model, deow, struct);
					struct.ajAttributes.add(new AjAttribute.DeclareAttribute(deow));
					return hasWarning = true;
				}
			}
		}
		return hasError || hasWarning;
	}

	/**
	 * Sets the location for the declare error / warning using the corresponding IProgramElement in the structure model. This will
	 * only fix bug 120356 if compiled with -emacssym, however, it does mean that the cross references view in AJDT will show the
	 * correct information.
	 * 
	 * Other possibilities for fix: 1. using the information in ajcDeclareSoft (if this is set correctly) which will fix the problem
	 * if compiled with ajc but not if compiled with javac. 2. creating an AjAttribute called FieldDeclarationLineNumberAttribute
	 * (much like MethodDeclarationLineNumberAttribute) which we can ask for the offset. This will again only fix bug 120356 when
	 * compiled with ajc.
	 * 
	 * @param deow
	 * @param struct
	 */
	private static void setDeclareErrorOrWarningLocation(AsmManager model, DeclareErrorOrWarning deow, AjAttributeFieldStruct struct) {
		IHierarchy top = (model == null ? null : model.getHierarchy());
		if (top != null && top.getRoot() != null) {
			IProgramElement ipe = top.findElementForLabel(top.getRoot(), IProgramElement.Kind.FIELD, struct.field.getName());
			if (ipe != null && ipe.getSourceLocation() != null) {
				ISourceLocation sourceLocation = ipe.getSourceLocation();
				int start = sourceLocation.getOffset();
				int end = start + struct.field.getName().length();
				deow.setLocation(struct.context, start, end);
				return;
			}
		}
		deow.setLocation(struct.context, -1, -1);
	}

	/**
	 * Returns a readable representation of a method. Method.toString() is not suitable.
	 * 
	 * @param method
	 * @return a readable representation of a method
	 */
	private static String methodToString(Method method) {
		StringBuffer sb = new StringBuffer();
		sb.append(method.getName());
		sb.append(method.getSignature());
		return sb.toString();
	}

	/**
	 * Build the bindings for a given method (pointcut / advice)
	 * 
	 * @param struct
	 * @return null if no debug info is available
	 */
	private static FormalBinding[] extractBindings(AjAttributeMethodStruct struct) throws UnreadableDebugInfoException {
		Method method = struct.method;
		String[] argumentNames = struct.getArgumentNames();

		// assert debug info was here
		if (argumentNames.length != method.getArgumentTypes().length) {
			reportError(
					"Cannot read debug info for @Aspect to handle formal binding in pointcuts (please compile with 'javac -g' or '<javac debug='true'.../>' in Ant)",
					struct);
			throw new UnreadableDebugInfoException();
		}

		List<FormalBinding> bindings = new ArrayList<FormalBinding>();
		for (int i = 0; i < argumentNames.length; i++) {
			String argumentName = argumentNames[i];
			UnresolvedType argumentType = UnresolvedType.forSignature(method.getArgumentTypes()[i].getSignature());

			// do not bind JoinPoint / StaticJoinPoint /
			// EnclosingStaticJoinPoint
			// TODO solve me : this means that the JP/SJP/ESJP cannot appear as
			// binding
			// f.e. when applying advice on advice etc
			if ((AjcMemberMaker.TYPEX_JOINPOINT.equals(argumentType)
					|| AjcMemberMaker.TYPEX_PROCEEDINGJOINPOINT.equals(argumentType)
					|| AjcMemberMaker.TYPEX_STATICJOINPOINT.equals(argumentType)
					|| AjcMemberMaker.TYPEX_ENCLOSINGSTATICJOINPOINT.equals(argumentType) || AjcMemberMaker.AROUND_CLOSURE_TYPE
						.equals(argumentType))) {
				// continue;// skip
				bindings.add(new FormalBinding.ImplicitFormalBinding(argumentType, argumentName, i));
			} else {
				bindings.add(new FormalBinding(argumentType, argumentName, i));
			}
		}

		return bindings.toArray(new FormalBinding[] {});
	}

	// FIXME alex deal with exclude index
	private static FormalBinding[] extractBindings(AjAttributeMethodStruct struct, String excludeFormal)
			throws UnreadableDebugInfoException {
		FormalBinding[] bindings = extractBindings(struct);
		// int excludeIndex = -1;
		for (int i = 0; i < bindings.length; i++) {
			FormalBinding binding = bindings[i];
			if (binding.getName().equals(excludeFormal)) {
				// excludeIndex = i;
				bindings[i] = new FormalBinding.ImplicitFormalBinding(binding.getType(), binding.getName(), binding.getIndex());
				break;
			}
		}
		return bindings;
		//
		// if (excludeIndex >= 0) {
		// FormalBinding[] bindingsFiltered = new
		// FormalBinding[bindings.length-1];
		// int k = 0;
		// for (int i = 0; i < bindings.length; i++) {
		// if (i == excludeIndex) {
		// ;
		// } else {
		// bindingsFiltered[k] = new FormalBinding(bindings[i].getType(),
		// bindings[i].getName(), k);
		// k++;
		// }
		// }
		// return bindingsFiltered;
		// } else {
		// return bindings;
		// }
	}

	/**
	 * Compute the flag for the xxxJoinPoint extra argument
	 * 
	 * @param method
	 * @return extra arg flag
	 */
	private static int extractExtraArgument(Method method) {
		Type[] methodArgs = method.getArgumentTypes();
		String[] sigs = new String[methodArgs.length];
		for (int i = 0; i < methodArgs.length; i++) {
			sigs[i] = methodArgs[i].getSignature();
		}
		return extractExtraArgument(sigs);
	}

	/**
	 * Compute the flag for the xxxJoinPoint extra argument
	 * 
	 * @param argumentSignatures
	 * @return extra arg flag
	 */
	public static int extractExtraArgument(String[] argumentSignatures) {
		int extraArgument = 0;
		for (int i = 0; i < argumentSignatures.length; i++) {
			if (AjcMemberMaker.TYPEX_JOINPOINT.getSignature().equals(argumentSignatures[i])) {
				extraArgument |= Advice.ThisJoinPoint;
			} else if (AjcMemberMaker.TYPEX_PROCEEDINGJOINPOINT.getSignature().equals(argumentSignatures[i])) {
				extraArgument |= Advice.ThisJoinPoint;
			} else if (AjcMemberMaker.TYPEX_STATICJOINPOINT.getSignature().equals(argumentSignatures[i])) {
				extraArgument |= Advice.ThisJoinPointStaticPart;
			} else if (AjcMemberMaker.TYPEX_ENCLOSINGSTATICJOINPOINT.getSignature().equals(argumentSignatures[i])) {
				extraArgument |= Advice.ThisEnclosingJoinPointStaticPart;
			}
		}
		return extraArgument;
	}

	/**
	 * Returns the runtime (RV/RIV) annotation of type annotationType or null if no such annotation
	 * 
	 * @param rvs
	 * @param annotationType
	 * @return annotation
	 */
	private static AnnotationGen getAnnotation(RuntimeAnnos rvs, UnresolvedType annotationType) {
		final String annotationTypeName = annotationType.getName();
		for (AnnotationGen rv : rvs.getAnnotations()) {
			if (annotationTypeName.equals(rv.getTypeName())) {
				return rv;
			}
		}
		return null;
	}

	/**
	 * Returns the value of a given element of an annotation or null if not found Caution: Does not handles default value.
	 * 
	 * @param annotation
	 * @param elementName
	 * @return annotation NVP
	 */
	private static NameValuePair getAnnotationElement(AnnotationGen annotation, String elementName) {
		for (NameValuePair element : annotation.getValues()) {
			if (elementName.equals(element.getNameString())) {
				return element;
			}
		}
		return null;
	}

	/**
	 * Return the argNames set for an annotation or null if it is not specified.
	 */
	private static String getArgNamesValue(AnnotationGen anno) {
		List<NameValuePair> elements = anno.getValues();
		for (NameValuePair element : elements) {
			if (ARGNAMES.equals(element.getNameString())) {
				return element.getValue().stringifyValue();
			}
		}
		return null;
	}

	private static String lastbit(String fqname) {
		int i = fqname.lastIndexOf(".");
		if (i == -1) {
			return fqname;
		} else {
			return fqname.substring(i + 1);
		}
	}

	/**
	 * Extract the method argument names. First we try the debug info attached to the method (the LocalVariableTable) - if we cannot
	 * find that we look to use the argNames value that may have been supplied on the associated annotation. If that fails we just
	 * don't know and return an empty string.
	 * 
	 * @param method
	 * @param argNamesFromAnnotation
	 * @param methodStruct
	 * @return method argument names
	 */
	private static String[] getMethodArgumentNames(Method method, String argNamesFromAnnotation,
			AjAttributeMethodStruct methodStruct) {
		if (method.getArgumentTypes().length == 0) {
			return EMPTY_STRINGS;
		}

		final int startAtStackIndex = method.isStatic() ? 0 : 1;
		final List<MethodArgument> arguments = new ArrayList<MethodArgument>();
		LocalVariableTable lt = method.getLocalVariableTable();
		if (lt != null) {
			LocalVariable[] lvt = lt.getLocalVariableTable();
			for (int j = 0; j < lvt.length; j++) {
				LocalVariable localVariable = lvt[j];
				if (localVariable != null) { // pr348488
					if (localVariable.getStartPC() == 0) {
						if (localVariable.getIndex() >= startAtStackIndex) {
							arguments.add(new MethodArgument(localVariable.getName(), localVariable.getIndex()));
						}
					}
				} else {
					String typename = (methodStruct.enclosingType != null ? methodStruct.enclosingType.getName() : "");
					System.err.println("AspectJ: 348488 debug: unusual local variable table for method " + typename + "."
							+ method.getName());
				}
			}
			if (arguments.size() == 0) {
				// could be cobertura code where some extra bytecode has been stuffed in at the start of the method
				// but the local variable table hasn't been repaired - for example:
				// LocalVariable(start_pc = 6, length = 40, index = 0:com.example.ExampleAspect this)
				// LocalVariable(start_pc = 6, length = 40, index = 1:org.aspectj.lang.ProceedingJoinPoint pjp)
				// LocalVariable(start_pc = 6, length = 40, index = 2:int __cobertura__line__number__)
				// LocalVariable(start_pc = 6, length = 40, index = 3:int __cobertura__branch__number__)
				LocalVariable localVariable = lvt[0];
				if (localVariable != null) { // pr348488
					if (localVariable.getStartPC() != 0) {
						// looks suspicious so let's use this information
						for (int j = 0; j < lvt.length && arguments.size() < method.getArgumentTypes().length; j++) {
							localVariable = lvt[j];
							if (localVariable.getIndex() >= startAtStackIndex) {
								arguments.add(new MethodArgument(localVariable.getName(), localVariable.getIndex()));
							}
						}
					}
				}
			}
		} else {
			// No debug info, do we have an annotation value we can rely on?
			if (argNamesFromAnnotation != null) {
				StringTokenizer st = new StringTokenizer(argNamesFromAnnotation, " ,");
				List<String> args = new ArrayList<String>();
				while (st.hasMoreTokens()) {
					args.add(st.nextToken());
				}
				if (args.size() != method.getArgumentTypes().length) {
					StringBuffer shortString = new StringBuffer().append(lastbit(method.getReturnType().toString())).append(" ")
							.append(method.getName());
					if (method.getArgumentTypes().length > 0) {
						shortString.append("(");
						for (int i = 0; i < method.getArgumentTypes().length; i++) {
							shortString.append(lastbit(method.getArgumentTypes()[i].toString()));
							if ((i + 1) < method.getArgumentTypes().length) {
								shortString.append(",");
							}

						}
						shortString.append(")");
					}
					reportError("argNames annotation value does not specify the right number of argument names for the method '"
							+ shortString.toString() + "'", methodStruct);
					return EMPTY_STRINGS;
				}
				return args.toArray(new String[] {});
			}
		}

		if (arguments.size() != method.getArgumentTypes().length) {
			return EMPTY_STRINGS;
		}

		// sort by index
		Collections.sort(arguments, new Comparator<MethodArgument>() {
			public int compare(MethodArgument mo, MethodArgument mo1) {
				if (mo.indexOnStack == mo1.indexOnStack) {
					return 0;
				} else if (mo.indexOnStack > mo1.indexOnStack) {
					return 1;
				} else {
					return -1;
				}
			}
		});
		String[] argumentNames = new String[arguments.size()];
		int i = 0;
		for (MethodArgument methodArgument : arguments) {
			argumentNames[i++] = methodArgument.name;
		}
		return argumentNames;
	}

	/**
	 * A method argument, used for sorting by indexOnStack (ie order in signature)
	 * 
	 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
	 */
	private static class MethodArgument {
		String name;
		int indexOnStack;

		public MethodArgument(String name, int indexOnStack) {
			this.name = name;
			this.indexOnStack = indexOnStack;
		}
	}

	/**
	 * LazyResolvedPointcutDefinition lazyly resolve the pointcut so that we have time to register all pointcut referenced before
	 * pointcut resolution happens
	 * 
	 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
	 */
	public static class LazyResolvedPointcutDefinition extends ResolvedPointcutDefinition {
		private final Pointcut m_pointcutUnresolved; // null for abstract
		// pointcut
		private final IScope m_binding;

		private Pointcut m_lazyPointcut = null;

		public LazyResolvedPointcutDefinition(UnresolvedType declaringType, int modifiers, String name,
				UnresolvedType[] parameterTypes, UnresolvedType returnType, Pointcut pointcut, IScope binding) {
			super(declaringType, modifiers, name, parameterTypes, returnType, Pointcut.makeMatchesNothing(Pointcut.RESOLVED));
			m_pointcutUnresolved = pointcut;
			m_binding = binding;
		}

		@Override
		public Pointcut getPointcut() {
			if (m_lazyPointcut == null && m_pointcutUnresolved == null) {
				m_lazyPointcut = Pointcut.makeMatchesNothing(Pointcut.CONCRETE);
			}
			if (m_lazyPointcut == null && m_pointcutUnresolved != null) {
				m_lazyPointcut = m_pointcutUnresolved.resolve(m_binding);
				m_lazyPointcut.copyLocationFrom(m_pointcutUnresolved);
			}
			return m_lazyPointcut;
		}
	}

	/**
	 * Helper to test empty strings
	 * 
	 * @param s
	 * @return true if empty or null
	 */
	private static boolean isNullOrEmpty(String s) {
		return (s == null || s.length() <= 0);
	}

	/**
	 * Set the pointcut bindings for which to ignore unbound issues, so that we can implicitly bind xxxJoinPoint for @AJ advices
	 * 
	 * @param pointcut
	 * @param bindings
	 */
	private static void setIgnoreUnboundBindingNames(Pointcut pointcut, FormalBinding[] bindings) {
		// register ImplicitBindings as to be ignored since unbound
		// TODO is it likely to fail in a bad way if f.e. this(jp) etc ?
		List<String> ignores = new ArrayList<String>();
		for (int i = 0; i < bindings.length; i++) {
			FormalBinding formalBinding = bindings[i];
			if (formalBinding instanceof FormalBinding.ImplicitFormalBinding) {
				ignores.add(formalBinding.getName());
			}
		}
		pointcut.m_ignoreUnboundBindingForNames = ignores.toArray(new String[ignores.size()]);
	}

	/**
	 * A check exception when we cannot read debug info (needed for formal binding)
	 */
	private static class UnreadableDebugInfoException extends Exception {
	}

	/**
	 * Report an error
	 * 
	 * @param message
	 * @param location
	 */
	private static void reportError(String message, AjAttributeStruct location) {
		if (!location.handler.isIgnoring(IMessage.ERROR)) {
			location.handler.handleMessage(new Message(message, location.enclosingType.getSourceLocation(), true));
		}
	}

	// private static void reportError(String message, IMessageHandler handler, ISourceLocation sourceLocation) {
	// if (!handler.isIgnoring(IMessage.ERROR)) {
	// handler.handleMessage(new Message(message, sourceLocation, true));
	// }
	// }

	/**
	 * Report a warning
	 * 
	 * @param message
	 * @param location
	 */
	private static void reportWarning(String message, AjAttributeStruct location) {
		if (!location.handler.isIgnoring(IMessage.WARNING)) {
			location.handler.handleMessage(new Message(message, location.enclosingType.getSourceLocation(), false));
		}
	}

	/**
	 * Parse the given pointcut, return null on failure and issue an error
	 * 
	 * @param pointcutString
	 * @param struct
	 * @param allowIf
	 * @return pointcut, unresolved
	 */
	private static Pointcut parsePointcut(String pointcutString, AjAttributeStruct struct, boolean allowIf) {
		try {
			PatternParser parser = new PatternParser(pointcutString, struct.context);
			Pointcut pointcut = parser.parsePointcut();
			parser.checkEof();
			pointcut.check(null, struct.enclosingType.getWorld());
			if (!allowIf && pointcutString.indexOf("if()") >= 0 && hasIf(pointcut)) {
				reportError("if() pointcut is not allowed at this pointcut location '" + pointcutString + "'", struct);
				return null;
			}
			pointcut.setLocation(struct.context, -1, -1);// FIXME -1,-1 is not
			// good enough
			return pointcut;
		} catch (ParserException e) {
			reportError("Invalid pointcut '" + pointcutString + "': " + e.toString()
					+ (e.getLocation() == null ? "" : " at position " + e.getLocation().getStart()), struct);
			return null;
		}
	}

	private static boolean hasIf(Pointcut pointcut) {
		IfFinder visitor = new IfFinder();
		pointcut.accept(visitor, null);
		return visitor.hasIf;
	}

	/**
	 * Parse the given type pattern, return null on failure and issue an error
	 * 
	 * @param patternString
	 * @param location
	 * @return type pattern
	 */
	private static TypePattern parseTypePattern(String patternString, AjAttributeStruct location) {
		try {
			TypePattern typePattern = new PatternParser(patternString).parseTypePattern();
			typePattern.setLocation(location.context, -1, -1);// FIXME -1,-1 is
			// not good
			// enough
			return typePattern;
		} catch (ParserException e) {
			reportError("Invalid type pattern'" + patternString + "' : " + e.getLocation(), location);
			return null;
		}
	}

	static class ThrownFormalNotDeclaredInAdviceSignatureException extends Exception {

		private final String formalName;

		public ThrownFormalNotDeclaredInAdviceSignatureException(String formalName) {
			this.formalName = formalName;
		}

		public String getFormalName() {
			return formalName;
		}
	}

	static class ReturningFormalNotDeclaredInAdviceSignatureException extends Exception {

		private final String formalName;

		public ReturningFormalNotDeclaredInAdviceSignatureException(String formalName) {
			this.formalName = formalName;
		}

		public String getFormalName() {
			return formalName;
		}
	}
}