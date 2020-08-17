/* *******************************************************************
 * Copyright (c) 2002-2010 Contributors
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     PARC, Andy Clement (SpringSource)
 * ******************************************************************/
package org.aspectj.ajdt.internal.compiler.ast;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.aspectj.ajdt.internal.compiler.ast.AccessForInlineVisitor.SuperAccessMethodPair;
import org.aspectj.ajdt.internal.compiler.lookup.EclipseFactory;
import org.aspectj.ajdt.internal.compiler.lookup.EclipseScope;
import org.aspectj.ajdt.internal.compiler.lookup.EclipseSourceType;
import org.aspectj.ajdt.internal.compiler.lookup.EclipseTypeMunger;
import org.aspectj.ajdt.internal.compiler.lookup.HelperInterfaceBinding;
import org.aspectj.ajdt.internal.compiler.lookup.InlineAccessFieldBinding;
import org.aspectj.ajdt.internal.compiler.lookup.IntertypeMemberTypeFinder;
import org.aspectj.ajdt.internal.compiler.lookup.PrivilegedHandler;
import org.aspectj.org.eclipse.jdt.core.compiler.CharOperation;
import org.aspectj.org.eclipse.jdt.internal.compiler.ClassFile;
import org.aspectj.org.eclipse.jdt.internal.compiler.CompilationResult;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.Clinit;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.aspectj.org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.aspectj.org.eclipse.jdt.internal.compiler.codegen.BranchLabel;
import org.aspectj.org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.aspectj.org.eclipse.jdt.internal.compiler.codegen.ExceptionLabel;
import org.aspectj.org.eclipse.jdt.internal.compiler.codegen.Opcodes;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.BinaryTypeBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.InvocationSite;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.LocalVariableBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.ParameterizedTypeBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.TagBits;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.UnresolvedReferenceBinding;
import org.aspectj.weaver.AjAttribute;
import org.aspectj.weaver.AjcMemberMaker;
import org.aspectj.weaver.NameMangler;
import org.aspectj.weaver.NewMemberClassTypeMunger;
import org.aspectj.weaver.ReferenceType;
import org.aspectj.weaver.ResolvedMember;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.Shadow;
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.patterns.Declare;
import org.aspectj.weaver.patterns.FormalBinding;
import org.aspectj.weaver.patterns.PerClause;
import org.aspectj.weaver.patterns.PerFromSuper;
import org.aspectj.weaver.patterns.PerSingleton;
import org.aspectj.weaver.patterns.TypePattern;

/**
 * Represents an aspect declaration.
 *
 * @author PARC
 * @author Andy Clement
 */
public class AspectDeclaration extends TypeDeclaration {
	// public IAjDeclaration[] ajDeclarations;

	// private AjAttribute.Aspect aspectAttribute;
	public PerClause perClause;
	public ResolvedMember aspectOfMethod;
	public ResolvedMember ptwGetWithinTypeNameMethod;
	public ResolvedMember hasAspectMethod;
	public Map<ResolvedMember, Binding> accessForInline = new LinkedHashMap<>();
	public Map<ResolvedMember, AccessForInlineVisitor.SuperAccessMethodPair> superAccessForInline = new HashMap<>();
	public boolean isPrivileged;
	public EclipseSourceType concreteName;
	public ReferenceType typeX;
	public EclipseFactory factory; // ??? should use this consistently
	public int adviceCounter = 1; // Used as a part of the generated name for advice methods
	public int declareCounter = 1; // Used as a part of the generated name for methods representing declares
	// for better error messages in 1.0 to 1.1 transition
	public TypePattern dominatesPattern;

	private int declaredModifiers;

	public AspectDeclaration(CompilationResult compilationResult) {
		super(compilationResult);
		// perClause = new PerSingleton();
	}

	public boolean isAbstract() {
		return (modifiers & ClassFileConstants.AccAbstract) != 0;
	}

	public void resolve() {
		declaredModifiers = modifiers; // remember our modifiers, we're going to be public in generateCode
		if (binding == null) {
			ignoreFurtherInvestigation = true;
			return;
		}

		super.resolve();
	}

	public void checkSpec(ClassScope scope) {
		if (ignoreFurtherInvestigation) {
			return;
		}
		if (dominatesPattern != null) {
			scope.problemReporter().signalError(
					dominatesPattern.getStart(),
					dominatesPattern.getEnd(),
					"dominates has changed for 1.1, use 'declare precedence: " + new String(this.name) + ", "
							+ dominatesPattern.toString() + ";' " + "in the body of the aspect instead");
		}

		if (!isAbstract()) {
			MethodBinding[] methods = binding.methods();
			for (int i = 0, len = methods.length; i < len; i++) {
				MethodBinding m = methods[i];
				if (m.isConstructor()) {
					// this make all constructors in aspects invisible and thus uncallable
					// XXX this only works for aspects that come from source
					methods[i] = new MethodBinding(m, binding) {
						public boolean canBeSeenBy(InvocationSite invocationSite, Scope scope) {
							return false;
						}
					};

					if (m.parameters != null && m.parameters.length != 0) {
						scope.problemReporter().signalError(m.sourceStart(), m.sourceEnd(),
								"only zero-argument constructors allowed in concrete aspect");
					}
				}
			}

			// check the aspect was not declared generic, only abstract aspects can have type params
			if (typeParameters != null && typeParameters.length > 0) {
				scope.problemReporter().signalError(sourceStart(), sourceEnd(), "only abstract aspects can have type parameters");
			}
		}

		if (this.enclosingType != null) {
			if (!Modifier.isStatic(modifiers)) {
				scope.problemReporter().signalError(sourceStart, sourceEnd, "inner aspects must be static");
				ignoreFurtherInvestigation = true;
				return;
			}
		}

		EclipseFactory world = EclipseFactory.fromScopeLookupEnvironment(scope);
		ResolvedType myType = typeX;
		// if (myType == null) System.err.println("bad myType for: " + this);
		ResolvedType superType = myType.getSuperclass();

		// can't be Serializable/Cloneable unless -XserializableAspects
		if (!world.isXSerializableAspects()) {
			if (world.getWorld().getCoreType(UnresolvedType.SERIALIZABLE).isAssignableFrom(myType)) {
				scope.problemReporter().signalError(sourceStart, sourceEnd, "aspects may not implement Serializable");
				ignoreFurtherInvestigation = true;
				return;
			}
			if (world.getWorld().getCoreType(UnresolvedType.CLONEABLE).isAssignableFrom(myType)) {
				scope.problemReporter().signalError(sourceStart, sourceEnd, "aspects may not implement Cloneable");
				ignoreFurtherInvestigation = true;
				return;
			}

		}

		if (superType.isAspect()) {
			if (!superType.isAbstract()) {
				scope.problemReporter().signalError(sourceStart, sourceEnd, "can not extend a concrete aspect");
				ignoreFurtherInvestigation = true;
				return;
			}

			// if super type is generic, check that we have fully parameterized it
			if (superType.isRawType()) {
				scope.problemReporter().signalError(sourceStart, sourceEnd,
						"a generic super-aspect must be fully parameterized in an extends clause");
				ignoreFurtherInvestigation = true;
				return;
			}
		}
	}

	private FieldBinding initFailureField = null;

	/**
	 * AMC - this method is called by the AtAspectJVisitor during beforeCompilation processing in the AjCompiler adapter. We use
	 * this hook to add in the @AspectJ annotations.
	 */
	public void addAtAspectJAnnotations() {
		if (ignoreFurtherInvestigation) {
			// perClause likely to be null. This flag used to be checked before we got called (at e33 level, in traverse(ASTVisitor
			// visitor, CompilationUnitScope unitScope))
			// For e37 moved the check down to this level
			return;
		}
		if ((binding.tagBits & TagBits.AnnotationResolved) != 0) {
			// possibly resolution occurred during hasUnsatisfiedDependency()...
			binding.tagBits = (binding.tagBits & ~TagBits.AnnotationResolved);
		}
		Annotation atAspectAnnotation = AtAspectJAnnotationFactory.createAspectAnnotation(perClause.toDeclarationString(),
				declarationSourceStart);
		Annotation privilegedAnnotation = null;
		if (isPrivileged) {
			privilegedAnnotation = AtAspectJAnnotationFactory.createPrivilegedAnnotation(declarationSourceStart);
		}
		Annotation[] toAdd = new Annotation[isPrivileged ? 2 : 1];
		toAdd[0] = atAspectAnnotation;
		if (isPrivileged) {
			toAdd[1] = privilegedAnnotation;
		}
		if (annotations == null) {
			annotations = toAdd;
		} else {
			Annotation[] old = annotations;
			annotations = new Annotation[annotations.length + toAdd.length];
			System.arraycopy(old, 0, annotations, 0, old.length);
			System.arraycopy(toAdd, 0, annotations, old.length, toAdd.length);
		}
		TypeDeclaration.resolveAnnotations(staticInitializerScope, annotations, binding);
	}

	public void generateCode(ClassFile enclosingClassFile) {
		if (ignoreFurtherInvestigation) {
			if (binding == null) {
				return;
			}
			ClassFile.createProblemType(this, scope.referenceCompilationUnit().compilationResult);
			return;
		}

		// make me and my binding public
		this.modifiers = AstUtil.makePublic(this.modifiers);
		this.binding.modifiers = AstUtil.makePublic(this.binding.modifiers);

		if (!isAbstract()) {
			if (perClause != null && perClause.getKind() == PerClause.SINGLETON) {
				initFailureField = factory.createSyntheticFieldBinding(binding, AjcMemberMaker.initFailureCauseField(typeX));
			}
			// binding.addSyntheticField((SyntheticFieldBinding)initFailureField);
			// initFailureField = factory.makeFieldBinding(AjcMemberMaker.initFailureCauseField(typeX));
			// binding.addField(initFailureField);

			if (perClause == null) {
				// we've already produced an error for this
			} else if (perClause.getKind() == PerClause.SINGLETON) {
				factory.createSyntheticFieldBinding(binding, AjcMemberMaker.perSingletonField(typeX));
				// CUSTARD binding.addField(factory.makeFieldBinding(AjcMemberMaker.perSingletonField(typeX)));

				// CUSTARD
				methods[0] = new AspectClinit((Clinit) methods[0], compilationResult, false, true, initFailureField);
			} else if (perClause.getKind() == PerClause.PERCFLOW) {
				factory.createSyntheticFieldBinding(binding, AjcMemberMaker.perCflowField(typeX));
				// CUSTARD binding.addField(factory.makeFieldBinding(AjcMemberMaker.perCflowField(typeX)));
				methods[0] = new AspectClinit((Clinit) methods[0], compilationResult, true, false, null);
			} else if (perClause.getKind() == PerClause.PEROBJECT) {
				// binding.addField(
				// world.makeFieldBinding(
				// AjcMemberMaker.perCflowField(
				// typeX)));
			} else if (perClause.getKind() == PerClause.PERTYPEWITHIN) {
				factory.createSyntheticFieldBinding(binding, AjcMemberMaker.perTypeWithinWithinTypeField(typeX, typeX));
				// PTWIMPL Add field for storing typename in aspect for which the aspect instance exists
				// binding.addField(factory.makeFieldBinding(AjcMemberMaker.perTypeWithinWithinTypeField(typeX,typeX)));
			} else {
				throw new RuntimeException("unimplemented");
			}
		}

		if (EclipseFactory.DEBUG) {
			System.out.println(toString());
		}

		super.generateCode(enclosingClassFile);
	}

	public boolean needClassInitMethod() {
		return true;
	}

	@SuppressWarnings("unchecked")
	protected void generateAttributes(ClassFile classFile) {
		if (!isAbstract()) {
			generatePerSupportMembers(classFile);
		}

		generateInlineAccessMembers(classFile);

		addVersionAttributeIfNecessary(classFile);

		classFile.extraAttributes.add(new EclipseAttributeAdapter(new AjAttribute.Aspect(perClause)));

		if (binding.privilegedHandler != null) {
			// Only build the attribute if necessary
			PrivilegedHandler privilegedHandler = (PrivilegedHandler) binding.privilegedHandler;
			ResolvedMember[] members = privilegedHandler.getMembers();
			if (members.length > 0) {
				classFile.extraAttributes.add(new EclipseAttributeAdapter(new AjAttribute.PrivilegedAttribute(members)));
			}
		}
		if (memberTypes != null) {
			for (TypeDeclaration memberType : memberTypes) {
				if (memberType instanceof IntertypeMemberClassDeclaration) {
					IntertypeMemberClassDeclaration itdMemberClassDeclaration = (IntertypeMemberClassDeclaration) memberType;
					AjAttribute attribute = itdMemberClassDeclaration.getAttribute();
					if (attribute != null) {
						classFile.extraAttributes.add(new EclipseAttributeAdapter(attribute));
					}
				}
			}
		}

		// XXX need to get this attribute on anyone with a pointcut for good errors
		classFile.extraAttributes.add(new EclipseAttributeAdapter(new AjAttribute.SourceContextAttribute(new String(
				compilationResult().getFileName()), compilationResult().lineSeparatorPositions)));

		super.generateAttributes(classFile);
	}

	/**
	 * A pointcut might have already added the attribute, let's not add it again.
	 */
	@SuppressWarnings("unchecked")
	private void addVersionAttributeIfNecessary(ClassFile classFile) {
		for (Object o : classFile.extraAttributes) {
			EclipseAttributeAdapter element = (EclipseAttributeAdapter) o;
			if (CharOperation.equals(element.getNameChars(), weaverVersionChars)) {
				return;
			}
		}
		classFile.extraAttributes.add(new EclipseAttributeAdapter(new AjAttribute.WeaverVersionInfo()));
	}

	private static char[] weaverVersionChars = "org.aspectj.weaver.WeaverVersion".toCharArray();

	private void generateInlineAccessMembers(ClassFile classFile) {
		for (SuperAccessMethodPair pair : superAccessForInline.values()) {
			generateSuperAccessMethod(classFile, pair.accessMethod, pair.originalMethod);
		}
		for (Map.Entry<ResolvedMember, Binding> e : accessForInline.entrySet()) {
			generateInlineAccessMethod(classFile, e.getValue(), e.getKey());
		}
	}

	private void generatePerSupportMembers(ClassFile classFile) {
		if (isAbstract()) {
			return;
		}

		// XXX otherwise we need to have this (error handling?)
		if (aspectOfMethod == null) {
			return;
		}
		if (perClause == null) {
			System.err.println("has null perClause: " + this);
			return;
		}

		// EclipseFactory world = EclipseFactory.fromScopeLookupEnvironment(this.scope);

		if (perClause.getKind() == PerClause.SINGLETON) {
			generatePerSingletonAspectOfMethod(classFile);
			generatePerSingletonHasAspectMethod(classFile);
			generatePerSingletonAjcClinitMethod(classFile);
		} else if (perClause.getKind() == PerClause.PERCFLOW) {
			generatePerCflowAspectOfMethod(classFile);
			generatePerCflowHasAspectMethod(classFile);
			generatePerCflowPushMethod(classFile);
			generatePerCflowAjcClinitMethod(classFile);
		} else if (perClause.getKind() == PerClause.PEROBJECT) {
			TypeBinding interfaceType = generatePerObjectInterface(classFile);
			generatePerObjectAspectOfMethod(classFile, interfaceType);
			generatePerObjectHasAspectMethod(classFile, interfaceType);
			generatePerObjectBindMethod(classFile, interfaceType);
		} else if (perClause.getKind() == PerClause.PERTYPEWITHIN) {
			// PTWIMPL Generate the methods required *in the aspect*
			generatePerTypeWithinAspectOfMethod(classFile); // public static <aspecttype> aspectOf(java.lang.Class)
			generatePerTypeWithinGetInstanceMethod(classFile); // private static <aspecttype> ajc$getInstance(Class c) throws
			// Exception
			generatePerTypeWithinHasAspectMethod(classFile);
			generatePerTypeWithinCreateAspectInstanceMethod(classFile); // generate public static X ajc$createAspectInstance(Class
			// forClass) {
			generatePerTypeWithinGetWithinTypeNameMethod(classFile);
		} else {
			throw new RuntimeException("unimplemented");
		}
	}

	private interface BodyGenerator {
		void generate(CodeStream codeStream);
	}

	private void generateMethod(ClassFile classFile, ResolvedMember member, BodyGenerator gen) {
		final EclipseFactory world = EclipseFactory.fromScopeLookupEnvironment(this.scope);
		generateMethod(classFile, world.makeMethodBinding(member), gen);
	}

	private void generateMethod(ClassFile classFile, MethodBinding methodBinding, BodyGenerator gen) {
		generateMethod(classFile, methodBinding, null, gen);
	}

	protected List<EclipseAttributeAdapter> makeEffectiveSignatureAttribute(ResolvedMember sig, Shadow.Kind kind, boolean weaveBody) {
		List<EclipseAttributeAdapter> l = new ArrayList<>(1);
		l.add(new EclipseAttributeAdapter(new AjAttribute.EffectiveSignatureAttribute(sig, kind, weaveBody)));
		return l;
	}

	public static final char[] HAS_ASPECT = "hasAspect".toCharArray();

	/*
	 * additionalAttributes allows us to pass some optional attributes we want to attach to the method we generate. Currently this
	 * is used for inline accessor methods that have been generated to allow private field references or private method calls to be
	 * inlined (PR71377). In these cases the optional attribute is an effective signature attribute which means calls to these
	 * methods are able to masquerade as any join point (a field set, field get or method call). The effective signature attribute
	 * is 'unwrapped' in BcelClassWeaver.matchInvokeInstruction()
	 */
	private void generateMethod(ClassFile classFile, MethodBinding methodBinding, List additionalAttributes, BodyGenerator gen) {
		// EclipseFactory world = EclipseFactory.fromScopeLookupEnvironment(this.scope);
		classFile.generateMethodInfoHeader(methodBinding);
		int methodAttributeOffset = classFile.contentsOffset;

		int attributeNumber;
		if (additionalAttributes != null) { // mini optimization
			List attrs = new ArrayList();
			attrs.addAll(AstUtil.getAjSyntheticAttribute());
			attrs.addAll(additionalAttributes);
			attributeNumber = classFile.generateMethodInfoAttributes(methodBinding, attrs);
		} else {
			attributeNumber = classFile.generateMethodInfoAttributes(methodBinding, AstUtil.getAjSyntheticAttribute());
		}

		int codeAttributeOffset = classFile.contentsOffset;
		classFile.generateCodeAttributeHeader();
		CodeStream codeStream = classFile.codeStream;

		// Use reset() rather than init()
		// XXX We need a scope to keep reset happy, initializerScope is *not* the right one, but it works !
		// codeStream.init(classFile);
		// codeStream.initializeMaxLocals(methodBinding);
		MethodDeclaration md = AstUtil.makeMethodDeclaration(methodBinding);
		md.scope = initializerScope;
		codeStream.reset(md, classFile);
		// body starts here
		gen.generate(codeStream);
		// body ends here
		if (codeStream.pcToSourceMapSize == 0) {
			codeStream.recordPositionsFrom(0, 1);
		}
		// Seems a dirty hack around some underlying issue...?
		boolean b2 = CharOperation.equals(methodBinding.selector,HAS_ASPECT) &&
		  ((classFile.produceAttributes & ClassFileConstants.ATTR_STACK_MAP_TABLE) != 0 ? true : false);
		if (b2) {
			classFile.produceAttributes &= ~ClassFileConstants.ATTR_STACK_MAP_TABLE;
		}
		boolean b = ((codeStream.generateAttributes & ClassFileConstants.ATTR_VARS) != 0 ? true : false); // pr148693
		if (codeStream.maxLocals == 0) {
			codeStream.generateAttributes &= ~ClassFileConstants.ATTR_VARS;
		}
		classFile.completeCodeAttribute(codeAttributeOffset, md.scope);
		if (b) {
			codeStream.generateAttributes |= ClassFileConstants.ATTR_VARS;
		}
		if (b2) {
			classFile.produceAttributes |= ClassFileConstants.ATTR_STACK_MAP_TABLE;
		}
		attributeNumber++;
		classFile.completeMethodInfo(methodBinding, methodAttributeOffset, attributeNumber);
	}

	private void generatePerCflowAspectOfMethod(ClassFile classFile) {
		final EclipseFactory world = EclipseFactory.fromScopeLookupEnvironment(this.scope);
		generateMethod(classFile, aspectOfMethod, new BodyGenerator() {
			public void generate(CodeStream codeStream) {
				// body starts here
				codeStream.fieldAccess(Opcodes.OPC_getstatic, world.makeFieldBinding(AjcMemberMaker.perCflowField(typeX)), null);

				codeStream.invoke(Opcodes.OPC_invokevirtual,
						world.makeMethodBindingForCall(AjcMemberMaker.cflowStackPeekInstance()), null);
				codeStream.checkcast(binding);
				codeStream.areturn();
				// body ends here
			}
		});

	}

	private void generatePerCflowHasAspectMethod(ClassFile classFile) {
		final EclipseFactory world = EclipseFactory.fromScopeLookupEnvironment(this.scope);
		generateMethod(classFile, hasAspectMethod, new BodyGenerator() {
			public void generate(CodeStream codeStream) {
				// body starts here
				codeStream.fieldAccess(Opcodes.OPC_getstatic, world.makeFieldBinding(AjcMemberMaker.perCflowField(typeX)), null);
				codeStream.invoke(Opcodes.OPC_invokevirtual, world.makeMethodBindingForCall(AjcMemberMaker.cflowStackIsValid()),
						null);
				codeStream.ireturn();
				// body ends here
			}
		});
	}

	private void generatePerCflowPushMethod(ClassFile classFile) {
		final EclipseFactory world = EclipseFactory.fromScopeLookupEnvironment(this.scope);
		generateMethod(classFile, world.makeMethodBinding(AjcMemberMaker.perCflowPush(factory.fromBinding(binding))),
				new BodyGenerator() {
					public void generate(CodeStream codeStream) {
						// body starts here
						codeStream.fieldAccess(Opcodes.OPC_getstatic, world.makeFieldBinding(AjcMemberMaker.perCflowField(typeX)),
								null);
						codeStream.new_(binding);
						codeStream.dup();
						codeStream.invoke(Opcodes.OPC_invokespecial, new MethodBinding(0, "<init>".toCharArray(), TypeBinding.VOID,
								new TypeBinding[0], new ReferenceBinding[0], binding), null);

						codeStream.invoke(Opcodes.OPC_invokevirtual,
								world.makeMethodBindingForCall(AjcMemberMaker.cflowStackPushInstance()), null);
						codeStream.return_();
						// body ends here
					}
				});

	}

	private void generatePerCflowAjcClinitMethod(ClassFile classFile) {
		final EclipseFactory world = EclipseFactory.fromScopeLookupEnvironment(this.scope);
		generateMethod(classFile, world.makeMethodBinding(AjcMemberMaker.ajcPreClinitMethod(world.fromBinding(binding))),
				new BodyGenerator() {
					public void generate(CodeStream codeStream) {
						// body starts here
						codeStream.new_(world.makeTypeBinding(AjcMemberMaker.CFLOW_STACK_TYPE));
						codeStream.dup();
						codeStream.invoke(Opcodes.OPC_invokespecial,
								world.makeMethodBindingForCall(AjcMemberMaker.cflowStackInit()), null);
						codeStream.fieldAccess(Opcodes.OPC_putstatic, world.makeFieldBinding(AjcMemberMaker.perCflowField(typeX)),
								null);
						codeStream.return_();
						// body ends here
					}
				});

	}

	private TypeBinding generatePerObjectInterface(ClassFile classFile) {
		final EclipseFactory world = EclipseFactory.fromScopeLookupEnvironment(this.scope);
		UnresolvedType interfaceTypeX = AjcMemberMaker.perObjectInterfaceType(typeX);
		HelperInterfaceBinding interfaceType = new HelperInterfaceBinding(this.binding, interfaceTypeX);
		world.addTypeBindingAndStoreInWorld(interfaceType);
		interfaceType.addMethod(world, AjcMemberMaker.perObjectInterfaceGet(typeX));
		interfaceType.addMethod(world, AjcMemberMaker.perObjectInterfaceSet(typeX));
		interfaceType.generateClass(compilationResult, classFile);
		return interfaceType;
	}

	/*
	 * private void generatePerTypeWithinGetWithinTypeMethod(ClassFile classFile) { final EclipseFactory world =
	 * EclipseFactory.fromScopeLookupEnvironment(this.scope); generateMethod(classFile,ptwGetWithinTypeMethod,new BodyGenerator() {
	 * public void generate(CodeStream codeStream) { codeStream.aload_0();
	 * codeStream.getfield(world.makeFieldBinding(AjcMemberMaker.perTypeWithinWithinTypeField(typeX,typeX))); codeStream.areturn();
	 * }}); }
	 */

	// PTWIMPL Generate aspectOf() method
	private void generatePerTypeWithinAspectOfMethod(ClassFile classFile) {
		final EclipseFactory world = EclipseFactory.fromScopeLookupEnvironment(this.scope);
		generateMethod(classFile, aspectOfMethod, new BodyGenerator() {
			public void generate(CodeStream codeStream) {

				LocalVariableBinding theTypeVar = new LocalVariableBinding("type".toCharArray(), world
						.makeTypeBinding(UnresolvedType.JL_CLASS), Modifier.PUBLIC, true);
				codeStream.record(theTypeVar);
				theTypeVar.recordInitializationStartPC(0);

				BranchLabel instanceFound = new BranchLabel(codeStream);

				TypeBinding exceptionTypeBinding = world.makeTypeBinding(UnresolvedType.JL_EXCEPTION);
				ExceptionLabel anythingGoesWrong = new ExceptionLabel(codeStream, exceptionTypeBinding);
				anythingGoesWrong.placeStart();
				codeStream.aload_0();
				codeStream.invoke(Opcodes.OPC_invokestatic,
						world.makeMethodBindingForCall(AjcMemberMaker.perTypeWithinGetInstance(typeX)), null);
				LocalVariableBinding aspectInstanceVar = new LocalVariableBinding("instance".toCharArray(), world
						.makeTypeBinding(UnresolvedType.OBJECT), Modifier.PUBLIC, false);
				aspectInstanceVar.resolvedPosition = 1;
				codeStream.record(aspectInstanceVar);
				aspectInstanceVar.recordInitializationStartPC(codeStream.position);
				codeStream.astore_1();
				codeStream.aload_1();
				codeStream.ifnonnull(instanceFound);
				codeStream.new_(world.makeTypeBinding(AjcMemberMaker.NO_ASPECT_BOUND_EXCEPTION));
				codeStream.dup();

				codeStream.ldc(typeX.getName());
				codeStream.aconst_null();

				codeStream.invoke(Opcodes.OPC_invokespecial,
						world.makeMethodBindingForCall(AjcMemberMaker.noAspectBoundExceptionInit2()), null);
				codeStream.athrow();
				instanceFound.place();
				codeStream.aload_1();

				codeStream.areturn();
				anythingGoesWrong.placeEnd();
				anythingGoesWrong.place();
				codeStream.pushExceptionOnStack(exceptionTypeBinding); // For stackmap computation to behave
				codeStream.astore_1();
				codeStream.new_(world.makeTypeBinding(AjcMemberMaker.NO_ASPECT_BOUND_EXCEPTION));

				codeStream.dup();

				// Run the simple ctor for NABE
				codeStream.invoke(Opcodes.OPC_invokespecial,
						world.makeMethodBindingForCall(AjcMemberMaker.noAspectBoundExceptionInit()), null);
				codeStream.athrow();

				theTypeVar.recordInitializationEndPC(codeStream.position);
				aspectInstanceVar.recordInitializationEndPC(codeStream.position);
			}
		});
	}

	private void generatePerObjectAspectOfMethod(ClassFile classFile, final TypeBinding interfaceType) {
		final EclipseFactory world = EclipseFactory.fromScopeLookupEnvironment(this.scope);
		generateMethod(classFile, aspectOfMethod, new BodyGenerator() {
			public void generate(CodeStream codeStream) {
				LocalVariableBinding objectVar = new LocalVariableBinding("object".toCharArray(), world
						.makeTypeBinding(UnresolvedType.OBJECT), Modifier.PUBLIC, true);
				codeStream.record(objectVar);
				objectVar.recordInitializationStartPC(0);

				BranchLabel wrongType = new BranchLabel(codeStream);
				BranchLabel returnIt = new BranchLabel(codeStream);
				codeStream.aload_0();
				codeStream.instance_of(interfaceType);
				codeStream.ifeq(wrongType);
				codeStream.aload_0();
				codeStream.checkcast(interfaceType);
				codeStream.invoke(Opcodes.OPC_invokeinterface,
						world.makeMethodBindingForCall(AjcMemberMaker.perObjectInterfaceGet(typeX)), null);

				codeStream.dup();
				codeStream.ifnonnull(returnIt);
				codeStream.pop();
				wrongType.place();
				codeStream.new_(world.makeTypeBinding(AjcMemberMaker.NO_ASPECT_BOUND_EXCEPTION));
				codeStream.dup();
				codeStream.invoke(Opcodes.OPC_invokespecial,
						world.makeMethodBindingForCall(AjcMemberMaker.noAspectBoundExceptionInit()), null);
				codeStream.athrow();

				returnIt.place();
				codeStream.areturn();
				codeStream.locals[0].recordInitializationEndPC(codeStream.position);
				// body ends here
			}
		});

	}

	private void generatePerObjectHasAspectMethod(ClassFile classFile, final TypeBinding interfaceType) {
		final EclipseFactory world = EclipseFactory.fromScopeLookupEnvironment(this.scope);
		generateMethod(classFile, hasAspectMethod, new BodyGenerator() {
			public void generate(CodeStream codeStream) {
				// body starts here
				LocalVariableBinding objectVar = new LocalVariableBinding("object".toCharArray(), world
						.makeTypeBinding(UnresolvedType.OBJECT), Modifier.PUBLIC, true);
				codeStream.record(objectVar);
				objectVar.recordInitializationStartPC(0);
				BranchLabel wrongType = new BranchLabel(codeStream);
				codeStream.aload_0();
				codeStream.instance_of(interfaceType);
				codeStream.ifeq(wrongType);
				codeStream.aload_0();
				codeStream.checkcast(interfaceType);
				codeStream.invoke(Opcodes.OPC_invokeinterface,
						world.makeMethodBindingForCall(AjcMemberMaker.perObjectInterfaceGet(typeX)), null);
				codeStream.ifnull(wrongType);
				codeStream.iconst_1();
				codeStream.ireturn();

				wrongType.place();
				codeStream.iconst_0();
				codeStream.ireturn();
				codeStream.locals[0].recordInitializationEndPC(codeStream.position);
				// body ends here
			}
		});
	}

	// PTWIMPL Generate hasAspect() method
	private void generatePerTypeWithinHasAspectMethod(ClassFile classFile) {
		final EclipseFactory world = EclipseFactory.fromScopeLookupEnvironment(this.scope);
		generateMethod(classFile, hasAspectMethod, new BodyGenerator() {
			public void generate(CodeStream codeStream) {
				LocalVariableBinding classVariable = new LocalVariableBinding("clazz".toCharArray(), world
						.makeTypeBinding(UnresolvedType.JL_CLASS), Modifier.PUBLIC, true);
				codeStream.record(classVariable);
				classVariable.recordInitializationStartPC(codeStream.position);
				ExceptionLabel goneBang = new ExceptionLabel(codeStream, world.makeTypeBinding(UnresolvedType.JL_EXCEPTION));
				BranchLabel noInstanceExists = new BranchLabel(codeStream);
				BranchLabel leave = new BranchLabel(codeStream);
				goneBang.placeStart();
				codeStream.aload_0();
				codeStream.invoke(Opcodes.OPC_invokestatic,
						world.makeMethodBinding(AjcMemberMaker.perTypeWithinGetInstance(typeX)), null);
				codeStream.ifnull(noInstanceExists);
				codeStream.iconst_1();
				codeStream.ireturn();
				// codeStream.goto_(leave);
				noInstanceExists.place();
				codeStream.iconst_0();
				leave.place();
				goneBang.placeEnd();
				codeStream.ireturn();
				goneBang.place();
				//codeStream.astore_1();
				codeStream.pop();
				codeStream.iconst_0();
				codeStream.ireturn();
				codeStream.locals[0].recordInitializationEndPC(codeStream.position);
			}
		});
	}

	private void generatePerObjectBindMethod(ClassFile classFile, final TypeBinding interfaceType) {
		final EclipseFactory world = EclipseFactory.fromScopeLookupEnvironment(this.scope);
		generateMethod(classFile, AjcMemberMaker.perObjectBind(world.fromBinding(binding)), new BodyGenerator() {
			public void generate(CodeStream codeStream) {
				// body starts here

				LocalVariableBinding aspectInstanceVar = new LocalVariableBinding("aspectInstance".toCharArray(), world
						.makeTypeBinding(UnresolvedType.OBJECT), Modifier.PUBLIC, true);
				codeStream.record(aspectInstanceVar);
				aspectInstanceVar.recordInitializationStartPC(0);

				BranchLabel wrongType = new BranchLabel(codeStream);
				codeStream.aload_0();
				codeStream.instance_of(interfaceType);
				codeStream.ifeq(wrongType); // XXX this case might call for screaming
				codeStream.aload_0();
				codeStream.checkcast(interfaceType);
				codeStream.invoke(Opcodes.OPC_invokeinterface,
						world.makeMethodBindingForCall(AjcMemberMaker.perObjectInterfaceGet(typeX)), null);
				// XXX should do a check for null here and throw a NoAspectBound
				codeStream.ifnonnull(wrongType);

				codeStream.aload_0();
				codeStream.checkcast(interfaceType);
				codeStream.new_(binding);
				codeStream.dup();
				codeStream.invoke(Opcodes.OPC_invokespecial, new MethodBinding(0, "<init>".toCharArray(), TypeBinding.VOID,
						new TypeBinding[0], new ReferenceBinding[0], binding), null);
				codeStream.invoke(Opcodes.OPC_invokeinterface,
						world.makeMethodBindingForCall(AjcMemberMaker.perObjectInterfaceSet(typeX)), null);

				wrongType.place();
				codeStream.return_();
				codeStream.locals[0].recordInitializationEndPC(codeStream.position);
				// body ends here
			}
		});
	}

	private void generatePerTypeWithinGetWithinTypeNameMethod(ClassFile classFile) {
		final EclipseFactory world = EclipseFactory.fromScopeLookupEnvironment(this.scope);
		// Code:
		/*
		 * Code: Stack=1, Locals=1, Args_size=1 0: aload_0 1: getfield #14; //Field ajc$withinType:Ljava/lang/String; 4: areturn
		 */
		generateMethod(classFile,
				AjcMemberMaker.perTypeWithinGetWithinTypeNameMethod(world.fromBinding(binding), world.getWorld().isInJava5Mode()),
				new BodyGenerator() {
					public void generate(CodeStream codeStream) {
						ExceptionLabel exc = new ExceptionLabel(codeStream, world.makeTypeBinding(UnresolvedType.JL_EXCEPTION));
						exc.placeStart();
						codeStream.aload_0();
						codeStream.fieldAccess(Opcodes.OPC_getfield,
								world.makeFieldBinding(AjcMemberMaker.perTypeWithinWithinTypeField(typeX, typeX)), null);
						codeStream.areturn();
					}
				});
	}

	// PTWIMPL Generate getInstance method
	private void generatePerTypeWithinGetInstanceMethod(ClassFile classFile) {
		final EclipseFactory world = EclipseFactory.fromScopeLookupEnvironment(this.scope);
		generateMethod(classFile, AjcMemberMaker.perTypeWithinGetInstance(world.fromBinding(binding)), new BodyGenerator() {
			public void generate(CodeStream codeStream) {
				LocalVariableBinding theTypeVar = new LocalVariableBinding("type".toCharArray(), world
						.makeTypeBinding(UnresolvedType.JL_CLASS), Modifier.PUBLIC, true);
				codeStream.record(theTypeVar);
				theTypeVar.recordInitializationStartPC(0);
				TypeBinding exceptionTypeBinding = world.makeTypeBinding(UnresolvedType.JL_EXCEPTION);
				ExceptionLabel exc = new ExceptionLabel(codeStream, exceptionTypeBinding);
				exc.placeStart();
				codeStream.aload_0();
				codeStream.ldc(NameMangler.perTypeWithinLocalAspectOf(typeX));
				codeStream.aconst_null();
				TypeBinding methodTypeBinding = world.makeTypeBinding(UnresolvedType.forSignature("Ljava/lang/reflect/Method;"));
				codeStream.invoke(
						Opcodes.OPC_invokevirtual,
						new MethodBinding(0, "getDeclaredMethod".toCharArray(),
								methodTypeBinding, // return type
								new TypeBinding[] { world.makeTypeBinding(UnresolvedType.forSignature("Ljava/lang/String;")),
										world.makeTypeBinding(UnresolvedType.forSignature("[Ljava/lang/Class;")) },
								new ReferenceBinding[0], (ReferenceBinding) world.makeTypeBinding(UnresolvedType.JL_CLASS)), null);
				codeStream.astore_1();
				LocalVariableBinding methodVariable = new LocalVariableBinding("method".toCharArray(), methodTypeBinding,
						Modifier.PUBLIC, true);
				codeStream.record(methodVariable);
				methodVariable.resolvedPosition = 1;
				methodVariable.recordInitializationStartPC(codeStream.position);
				codeStream.aload_1();
				codeStream.aconst_null();
				codeStream.aconst_null();
				codeStream.invoke(
						Opcodes.OPC_invokevirtual,
						new MethodBinding(0, "invoke".toCharArray(), world.makeTypeBinding(UnresolvedType.OBJECT),
								new TypeBinding[] { world.makeTypeBinding(UnresolvedType.OBJECT),
										world.makeTypeBinding(UnresolvedType.forSignature("[Ljava/lang/Object;")) },
								new ReferenceBinding[0], (ReferenceBinding) world
										.makeTypeBinding(UnresolvedType.JAVA_LANG_REFLECT_METHOD)), null);
				codeStream.checkcast(world.makeTypeBinding(typeX));
				codeStream.astore_2();
				LocalVariableBinding aspectInstanceVariable = new LocalVariableBinding("aspectInstance".toCharArray(), world
						.makeTypeBinding(typeX), Modifier.PUBLIC, true);
				codeStream.record(aspectInstanceVariable);
				aspectInstanceVariable.resolvedPosition = 2;
				aspectInstanceVariable.recordInitializationStartPC(codeStream.position);
				codeStream.aload_2();
				exc.placeEnd();
				codeStream.areturn();
				exc.place();
				codeStream.pushExceptionOnStack(exceptionTypeBinding); // For stackmap computation to behave
				codeStream.astore_1();
				// this just returns null now - the old version used to throw the caught exception!
				codeStream.aconst_null();
				codeStream.areturn();
				codeStream.locals[0].recordInitializationEndPC(codeStream.position);
				codeStream.locals[1].recordInitializationEndPC(codeStream.position);
				codeStream.locals[2].recordInitializationEndPC(codeStream.position);
			}
		});
	}

	private void generatePerTypeWithinCreateAspectInstanceMethod(ClassFile classFile) {
		final EclipseFactory world = EclipseFactory.fromScopeLookupEnvironment(this.scope);
		generateMethod(classFile, AjcMemberMaker.perTypeWithinCreateAspectInstance(world.fromBinding(binding)),
				new BodyGenerator() {
					public void generate(CodeStream codeStream) {

						codeStream.new_(world.makeTypeBinding(typeX));
						codeStream.dup();
						codeStream.invoke(Opcodes.OPC_invokespecial, new MethodBinding(0, "<init>".toCharArray(), TypeBinding.VOID,
								new TypeBinding[0], new ReferenceBinding[0], binding), null);
						codeStream.astore_1();
						codeStream.aload_1();
						codeStream.aload_0();
						codeStream.fieldAccess(Opcodes.OPC_putfield,
								world.makeFieldBinding(AjcMemberMaker.perTypeWithinWithinTypeField(typeX, typeX)), null);
						codeStream.aload_1();
						codeStream.areturn();
					}
				});
	}

	private void generatePerSingletonAspectOfMethod(ClassFile classFile) {
		final EclipseFactory world = EclipseFactory.fromScopeLookupEnvironment(this.scope);
		generateMethod(classFile, aspectOfMethod, new BodyGenerator() {
			public void generate(CodeStream codeStream) {
				// Old style aspectOf() method which confused decompilers
				// // body starts here
				// codeStream.getstatic(world.makeFieldBinding(AjcMemberMaker.perSingletonField(
				// typeX)));
				// Label isNull = new Label(codeStream);
				// codeStream.dup();
				// codeStream.ifnull(isNull);
				// codeStream.areturn();
				// isNull.place();
				//
				// codeStream.incrStackSize(+1); // the dup trick above confuses the stack counter
				// codeStream.new_(world.makeTypeBinding(AjcMemberMaker.NO_ASPECT_BOUND_EXCEPTION));
				// codeStream.dup();
				// codeStream.ldc(typeX.getNameAsIdentifier());
				// codeStream.getstatic(initFailureField);
				// codeStream.invokespecial(world.makeMethodBindingForCall(
				// AjcMemberMaker.noAspectBoundExceptionInitWithCause()
				// ));
				// codeStream.athrow();
				// // body ends here

				// The stuff below generates code that looks like this:
				/*
				 * if (ajc$perSingletonInstance == null) throw new NoAspectBoundException("A", ajc$initFailureCause); else return
				 * ajc$perSingletonInstance;
				 */
				// body starts here (see end of each line for what it is doing!)
				FieldBinding fb = world.makeFieldBinding(AjcMemberMaker.perSingletonField(typeX));
				codeStream.fieldAccess(Opcodes.OPC_getstatic, fb, null);
				BranchLabel isNonNull = new BranchLabel(codeStream);
				codeStream.ifnonnull(isNonNull); // IFNONNULL
				codeStream.new_(world.makeTypeBinding(AjcMemberMaker.NO_ASPECT_BOUND_EXCEPTION)); // NEW
				codeStream.dup(); // DUP
				codeStream.ldc(typeX.getNameAsIdentifier()); // LDC
				codeStream.fieldAccess(Opcodes.OPC_getstatic, initFailureField, null);
				codeStream.invoke(Opcodes.OPC_invokespecial,
						world.makeMethodBindingForCall(AjcMemberMaker.noAspectBoundExceptionInitWithCause()), null); // INVOKESPECIAL
				codeStream.athrow(); // ATHROW
				isNonNull.place();
				codeStream.fieldAccess(Opcodes.OPC_getstatic, fb, null);
				codeStream.areturn(); // ARETURN
				// body ends here
			}
		});
	}

	private void generatePerSingletonHasAspectMethod(ClassFile classFile) {
		final EclipseFactory world = EclipseFactory.fromScopeLookupEnvironment(this.scope);
		generateMethod(classFile, hasAspectMethod, new BodyGenerator() {
			public void generate(CodeStream codeStream) {
				// body starts here
				codeStream.fieldAccess(Opcodes.OPC_getstatic, world.makeFieldBinding(AjcMemberMaker.perSingletonField(typeX)), null);
				BranchLabel isNull = new BranchLabel(codeStream);
				codeStream.ifnull(isNull);
				codeStream.iconst_1();
				codeStream.ireturn();
				isNull.place();
				codeStream.iconst_0();
				codeStream.ireturn();
				// body ends here
			}
		});
	}

	private void generatePerSingletonAjcClinitMethod(ClassFile classFile) {
		final EclipseFactory world = EclipseFactory.fromScopeLookupEnvironment(this.scope);
		generateMethod(classFile, world.makeMethodBinding(AjcMemberMaker.ajcPostClinitMethod(world.fromBinding(binding))),
				new BodyGenerator() {
					public void generate(CodeStream codeStream) {
						// body starts here
						codeStream.new_(binding);
						codeStream.dup();
						codeStream.invoke(Opcodes.OPC_invokespecial, new MethodBinding(0, "<init>".toCharArray(), TypeBinding.VOID,
								new TypeBinding[0], new ReferenceBinding[0], binding), null);

						codeStream.fieldAccess(Opcodes.OPC_putstatic,
								world.makeFieldBinding(AjcMemberMaker.perSingletonField(typeX)), null);
						codeStream.return_();
						// body ends here
					}
				});

	}

	private void generateSuperAccessMethod(ClassFile classFile, final MethodBinding accessMethod, final ResolvedMember method) {
		generateMethod(classFile, accessMethod, new BodyGenerator() {
			public void generate(CodeStream codeStream) {
				// body starts here
				codeStream.aload_0();
				AstUtil.generateParameterLoads(accessMethod.parameters, codeStream, 1);
				codeStream.invoke(Opcodes.OPC_invokespecial, factory.makeMethodBinding(method), null);
				AstUtil.generateReturn(accessMethod.returnType, codeStream);
				// body ends here
			}
		});

	}

	private void generateInlineAccessMethod(ClassFile classFile, final Binding binding, final ResolvedMember member) {
		if (binding instanceof InlineAccessFieldBinding) {
			generateInlineAccessors(classFile, (InlineAccessFieldBinding) binding, member);
		} else {
			generateInlineAccessMethod(classFile, (MethodBinding) binding, member);
		}
	}

	private void generateInlineAccessors(ClassFile classFile, final InlineAccessFieldBinding accessField, final ResolvedMember field) {
		final FieldBinding fieldBinding = factory.makeFieldBinding(field);

		generateMethod(classFile, accessField.reader, makeEffectiveSignatureAttribute(field, Shadow.FieldGet, false),
				new BodyGenerator() {
					public void generate(CodeStream codeStream) {
						// body starts here
						if (Modifier.isStatic(field.getModifiers())) {
							codeStream.fieldAccess(Opcodes.OPC_getstatic, fieldBinding, null);
						} else {
							codeStream.aload_0();
							codeStream.fieldAccess(Opcodes.OPC_getfield, fieldBinding, null);
						}

						AstUtil.generateReturn(accessField.reader.returnType, codeStream);
						// body ends here
					}
				});

		generateMethod(classFile, accessField.writer, makeEffectiveSignatureAttribute(field, Shadow.FieldSet, false),
				new BodyGenerator() {
					public void generate(CodeStream codeStream) {
						// body starts here
						if (Modifier.isStatic(field.getModifiers())) {
							codeStream.load(fieldBinding.type, 0);
							codeStream.fieldAccess(Opcodes.OPC_putstatic, fieldBinding, null);
						} else {
							codeStream.aload_0();
							codeStream.load(fieldBinding.type, 1);
							codeStream.fieldAccess(Opcodes.OPC_putfield, fieldBinding, null);
						}

						codeStream.return_();
						// body ends here
					}
				});

	}

	private void generateInlineAccessMethod(ClassFile classFile, final MethodBinding accessMethod, final ResolvedMember method) {
		generateMethod(classFile, accessMethod, makeEffectiveSignatureAttribute(method, Shadow.MethodCall, false),
				new BodyGenerator() {
					public void generate(CodeStream codeStream) {
						// body starts here

						AstUtil.generateParameterLoads(accessMethod.parameters, codeStream);

						if (Modifier.isStatic(method.getModifiers())) {
							codeStream.invoke(Opcodes.OPC_invokestatic, factory.makeMethodBinding(method), null);
						} else {
							codeStream.invoke(Opcodes.OPC_invokevirtual, factory.makeMethodBinding(method), null);
						}

						AstUtil.generateReturn(accessMethod.returnType, codeStream);
						// body ends here
					}
				});
	}

	private PerClause.Kind lookupPerClauseKind(ReferenceBinding binding) {
		PerClause perClause;
		if (binding instanceof BinaryTypeBinding) {
			ResolvedType superTypeX = factory.fromEclipse(binding);
			perClause = superTypeX.getPerClause();
		} else if (binding instanceof SourceTypeBinding) {
			SourceTypeBinding sourceSc = (SourceTypeBinding) binding;
			if (sourceSc.scope.referenceContext instanceof AspectDeclaration) {
				perClause = ((AspectDeclaration) sourceSc.scope.referenceContext).perClause;
			} else {
				return null;
			}
		} else if (binding instanceof ParameterizedTypeBinding) {
			ParameterizedTypeBinding pBinding = (ParameterizedTypeBinding) binding;
			if (pBinding.type instanceof SourceTypeBinding) {
				SourceTypeBinding sourceSc = (SourceTypeBinding) pBinding.type;
				if (sourceSc.scope != null && sourceSc.scope.referenceContext instanceof AspectDeclaration) {
					perClause = ((AspectDeclaration) sourceSc.scope.referenceContext).perClause;
				} else {
					return null;
				}
			} else {
				perClause = null;
			}
		} else {
			// XXX need to handle this too
			return null;
		}
		if (perClause == null) {
			return lookupPerClauseKind(binding.superclass());
		} else {
			return perClause.getKind();
		}
	}

	private void buildPerClause(ClassScope scope) {
		EclipseFactory world = EclipseFactory.fromScopeLookupEnvironment(scope);

		if (perClause == null) {
			PerClause.Kind kind = lookupPerClauseKind(binding.superclass);
			if (kind == null) {
				perClause = new PerSingleton();
			} else {
				perClause = new PerFromSuper(kind);
			}
		}

		// aspectAttribute = new AjAttribute.Aspect(perClause);

		if (ignoreFurtherInvestigation) {
			return; // ???
		}

		if (!isAbstract()) {
			if (perClause.getKind() == PerClause.SINGLETON) {
				aspectOfMethod = AjcMemberMaker.perSingletonAspectOfMethod(typeX);
				hasAspectMethod = AjcMemberMaker.perSingletonHasAspectMethod(typeX);
			} else if (perClause.getKind() == PerClause.PERCFLOW) {
				aspectOfMethod = AjcMemberMaker.perCflowAspectOfMethod(typeX);
				hasAspectMethod = AjcMemberMaker.perCflowHasAspectMethod(typeX);
			} else if (perClause.getKind() == PerClause.PEROBJECT) {
				aspectOfMethod = AjcMemberMaker.perObjectAspectOfMethod(typeX);
				hasAspectMethod = AjcMemberMaker.perObjectHasAspectMethod(typeX);
			} else if (perClause.getKind() == PerClause.PERTYPEWITHIN) {
				// PTWIMPL Use these variants of aspectOf()/hasAspect()
				aspectOfMethod = AjcMemberMaker.perTypeWithinAspectOfMethod(typeX, world.getWorld().isInJava5Mode());
				hasAspectMethod = AjcMemberMaker.perTypeWithinHasAspectMethod(typeX, world.getWorld().isInJava5Mode());
				ptwGetWithinTypeNameMethod = AjcMemberMaker.perTypeWithinGetWithinTypeNameMethod(typeX, world.getWorld()
						.isInJava5Mode());
				binding.addMethod(world.makeMethodBinding(ptwGetWithinTypeNameMethod));
			} else {
				throw new RuntimeException("bad per clause: " + perClause);
			}

			binding.addMethod(world.makeMethodBinding(aspectOfMethod));
			binding.addMethod(world.makeMethodBinding(hasAspectMethod));
		}
		resolvePerClause(); // XXX might be too soon for some error checking
	}

	private PerClause resolvePerClause() {
		EclipseScope iscope = new EclipseScope(new FormalBinding[0], scope);
		perClause.resolve(iscope);
		return perClause;
	}

	public void processIntertypeMemberTypes(ClassScope classScope) {
		factory = EclipseFactory.fromScopeLookupEnvironment(scope);
		if (memberTypes != null) {
			for (TypeDeclaration memberType : memberTypes) {
				if (memberType instanceof IntertypeMemberClassDeclaration) {
					EclipseTypeMunger m = ((IntertypeMemberClassDeclaration) memberType).build(classScope);
					if (m != null) {
						mungeNewInnerClass(m, factory);
						concreteName.typeMungers.add(m);
					}
				}
			}
		}
	}

	public void buildInterTypeAndPerClause(ClassScope classScope) {
		factory = EclipseFactory.fromScopeLookupEnvironment(scope);
		if (isPrivileged) {
			binding.privilegedHandler = new PrivilegedHandler(this);
		}

		checkSpec(classScope);
		if (ignoreFurtherInvestigation) {
			return;
		}

		buildPerClause(scope);

		if (methods != null) {
			for (org.aspectj.org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration method : methods) {
				if (method instanceof InterTypeDeclaration) {
					EclipseTypeMunger m = ((InterTypeDeclaration) method).build(classScope);
					if (m != null) {
						concreteName.typeMungers.add(m);
					}
				} else if (method instanceof DeclareDeclaration) {
					Declare d = ((DeclareDeclaration) method).build(classScope);
					if (d != null) {
						concreteName.declares.add(d);
					}
				}
			}
		}

		concreteName.getDeclaredPointcuts();
	}

	// public String toString(int tab) {
	// return tabString(tab) + toStringHeader() + toStringBody(tab);
	// }
	//
	// public String toStringBody(int tab) {
	//
	//		String s = " {"; //$NON-NLS-1$
	//
	//
	// if (memberTypes != null) {
	// for (int i = 0; i < memberTypes.length; i++) {
	// if (memberTypes[i] != null) {
	//					s += "\n" + memberTypes[i].toString(tab + 1); //$NON-NLS-1$
	// }
	// }
	// }
	// if (fields != null) {
	// for (int fieldI = 0; fieldI < fields.length; fieldI++) {
	// if (fields[fieldI] != null) {
	//					s += "\n" + fields[fieldI].toString(tab + 1); //$NON-NLS-1$
	// if (fields[fieldI].isField())
	//						s += ";"; //$NON-NLS-1$
	// }
	// }
	// }
	// if (methods != null) {
	// for (int i = 0; i < methods.length; i++) {
	// if (methods[i] != null) {
	//					s += "\n" + methods[i].toString(tab + 1); //$NON-NLS-1$
	// }
	// }
	// }
	//		s += "\n" + tabString(tab) + "}"; //$NON-NLS-2$ //$NON-NLS-1$
	// return s;
	// }

	// very similar to code in EclipseTypeMunger
	private void mungeNewInnerClass(EclipseTypeMunger m, EclipseFactory world) {

		NewMemberClassTypeMunger munger = (NewMemberClassTypeMunger) m.getMunger();

		// private boolean mungeNewInnerClass(SourceTypeBinding sourceType, ResolvedType onType, NewMemberClassTypeMunger munger,
		// boolean isExactTargetType) {

		SourceTypeBinding aspectTypeBinding = (SourceTypeBinding) world.makeTypeBinding(m.getAspectType());

		char[] mungerMemberTypeName = ("$" + munger.getMemberTypeName()).toCharArray();
		ReferenceBinding innerTypeBinding = null;
		for (ReferenceBinding innerType : aspectTypeBinding.memberTypes) {
			char[] compounded = CharOperation.concatWith(innerType.compoundName, '.');
			if (org.aspectj.org.eclipse.jdt.core.compiler.CharOperation.endsWith(compounded, mungerMemberTypeName)) {
				innerTypeBinding = innerType;
				break;
			}
		}
		// may be unresolved if the aspect type binding was a BinaryTypeBinding
		if (innerTypeBinding instanceof UnresolvedReferenceBinding) {
			innerTypeBinding = (ReferenceBinding) BinaryTypeBinding.resolveType(innerTypeBinding, world.getLookupEnvironment(),
					true);
		}
		if (innerTypeBinding == null) {
			throw new IllegalStateException("Could not find inner type binding for '" + munger.getMemberTypeName() + "'");
		}

		// TODO adjust modifier?
		// TODO deal with itd of it onto an interface

		SourceTypeBinding targetSourceTypeBinding = (SourceTypeBinding) world.makeTypeBinding(munger.getTargetType());

		// if it is a binary type binding it is likely to be something we ITD'd on before
		// TODO should probably avoid putting it onto BTBs at all (since already there)
		if (!(targetSourceTypeBinding instanceof BinaryTypeBinding)) {
			ReferenceBinding[] existingMemberTypes = targetSourceTypeBinding.memberTypes();
			for (ReferenceBinding existingMemberType : existingMemberTypes) {
				char[] compounded = CharOperation.concatWith(existingMemberType.compoundName, '.');
				if (CharOperation.endsWith(compounded, mungerMemberTypeName)) {
					scope.problemReporter().signalError(sourceStart(), sourceEnd(),
							"target type already declares a member type with the name '" + munger.getMemberTypeName() + "'");
					return;
				}
			}
		}
		/*
		 * char[][] className = CharOperation.deepCopy(targetSourceTypeBinding.compoundName); className[className.length - 1] =
		 * CharOperation.concat(className[className.length - 1], munger.getMemberTypeName() .toCharArray(), '$'); //
		 * ReferenceBinding existingType = packageBinding.getType0(className[className.length - 1]); innerTypeBinding.compoundName =
		 * className; innerTypeBinding.fPackage = targetSourceTypeBinding.fPackage;
		 */
		findOrCreateInterTypeMemberClassFinder(targetSourceTypeBinding).addInterTypeMemberType(innerTypeBinding);
	}

	private IntertypeMemberTypeFinder findOrCreateInterTypeMemberClassFinder(SourceTypeBinding sourceType) {
		IntertypeMemberTypeFinder finder = (IntertypeMemberTypeFinder) sourceType.typeFinder;
		if (finder == null) {
			finder = new IntertypeMemberTypeFinder();
			sourceType.typeFinder = finder;
			finder.targetTypeBinding = sourceType;
			sourceType.tagBits &= ~TagBits.HasNoMemberTypes; // ensure it thinks it has one
		}
		return finder;
	}

	public StringBuffer printHeader(int indent, StringBuffer output) {
		// since all aspects are made public we want to print the
		// modifiers that were supplied in the original source code
		printModifiers(this.declaredModifiers, output);
		output.append("aspect ");
		output.append(name);
		if (superclass != null) {
			output.append(" extends "); //$NON-NLS-1$
			superclass.print(0, output);
		}
		if (superInterfaces != null && superInterfaces.length > 0) {
			output.append((TypeDeclaration.kind(this.modifiers) == TypeDeclaration.INTERFACE_DECL) ? " extends " : " implements ");//$NON-NLS-2$ //$NON-NLS-1$
			for (int i = 0; i < superInterfaces.length; i++) {
				if (i > 0) {
					output.append(", "); //$NON-NLS-1$
				}
				superInterfaces[i].print(0, output);
			}
		}
		return output;
		// XXX we should append the per-clause
	}

	/**
	 * All aspects are made public after type checking etc. and before generating code (so that the advice can be called!). This
	 * method returns the modifiers as specified in the original source code declaration so that the structure model sees the right
	 * thing.
	 */
	public int getDeclaredModifiers() {
		return declaredModifiers;
	}

	public char[] getLocalTypeNameSuffix() {
		return name;
	}
}
