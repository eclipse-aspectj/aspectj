/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     PARC     initial implementation 
 * ******************************************************************/


package org.aspectj.ajdt.internal.compiler.ast;

import java.lang.reflect.Modifier;
import java.util.*;

import org.aspectj.ajdt.internal.compiler.lookup.*;
import org.aspectj.weaver.*;
import org.aspectj.weaver.patterns.*;
import org.eclipse.jdt.internal.compiler.ClassFile;
import org.eclipse.jdt.internal.compiler.CompilationResult;
//import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Clinit;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
//import org.eclipse.jdt.internal.compiler.ast.ConstructorDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.eclipse.jdt.internal.compiler.codegen.Label;
import org.eclipse.jdt.internal.compiler.lookup.*;
//import org.eclipse.jdt.internal.compiler.parser.Parser;


// (we used to...) making all aspects member types avoids a nasty hierarchy pain
// switched from MemberTypeDeclaration to TypeDeclaration
public class AspectDeclaration extends TypeDeclaration {
	//public IAjDeclaration[] ajDeclarations;
	
	private AjAttribute.Aspect aspectAttribute;
	public PerClause perClause;
	public ResolvedMember aspectOfMethod;
	public ResolvedMember hasAspectMethod;


	public Map accessForInline = new HashMap();
	public Map superAccessForInline = new HashMap();
	
	public boolean isPrivileged;
	
	public EclipseSourceType concreteName;
	
	public ResolvedTypeX.Name typeX;
	
	public EclipseFactory factory;  //??? should use this consistently

    public int adviceCounter = 1; // Used as a part of the generated name for advice methods

	// for better error messages in 1.0 to 1.1 transition
	public TypePattern dominatesPattern;

	public AspectDeclaration(CompilationResult compilationResult) {
		super(compilationResult);
		//perClause = new PerSingleton();
	}
	
	public boolean isAbstract() {
		return (modifiers & AccAbstract) != 0;
	}
	
	public void resolve() {
		if (binding == null) {
			ignoreFurtherInvestigation = true;
			return;
		}
		
		super.resolve();
	}
	
	
	public void checkSpec(ClassScope scope) {
		if (ignoreFurtherInvestigation) return;
		if (dominatesPattern != null) {
			scope.problemReporter().signalError(
					dominatesPattern.getStart(), dominatesPattern.getEnd(), 
					"dominates has changed for 1.1, use 'declare precedence: " +
					new String(this.name) + ", " + dominatesPattern.toString() + ";' " +
					"in the body of the aspect instead");
		}
		
		if (!isAbstract()) {
			MethodBinding[] methods = binding.methods();
			for (int i=0, len = methods.length; i < len; i++) {
				MethodBinding m = methods[i];
				if (m.isConstructor()) {
					// this make all constructors in aspects invisible and thus uncallable
					//XXX this only works for aspects that come from source
					methods[i] = new MethodBinding(m, binding) {
						public boolean canBeSeenBy(
							InvocationSite invocationSite,
							Scope scope) {
							return false;
						}
					};
					
					if (m.parameters != null && m.parameters.length != 0) {
						scope.problemReporter().signalError(m.sourceStart(), m.sourceEnd(),
								"only zero-argument constructors allowed in concrete aspect");
					}
				}
			}
		}
		
		if (this.enclosingType != null) {
			if (!Modifier.isStatic(modifiers)) {
				scope.problemReporter().signalError(sourceStart, sourceEnd,
								"inner aspects must be static");
				ignoreFurtherInvestigation = true;
			    return;
			}
		}
		
		
		EclipseFactory world = EclipseFactory.fromScopeLookupEnvironment(scope);
		ResolvedTypeX myType = typeX;
		//if (myType == null) System.err.println("bad myType for: " + this);
		ResolvedTypeX superType = myType.getSuperclass();		
		
		// can't be Serializable/Cloneable unless -XserializableAspects
		if (!world.isXSerializableAspects()) {
			if (world.getWorld().getCoreType(TypeX.SERIALIZABLE).isAssignableFrom(myType)) {
				scope.problemReporter().signalError(sourceStart, sourceEnd,
								"aspects may not implement Serializable");
				ignoreFurtherInvestigation = true;
			    return;
			}
			if (world.getWorld().getCoreType(TypeX.CLONEABLE).isAssignableFrom(myType)) {
				scope.problemReporter().signalError(sourceStart, sourceEnd,
								"aspects may not implement Cloneable");
				ignoreFurtherInvestigation = true;
			    return;
			}

		}

		if (superType.isAspect()) {
			if (!superType.isAbstract()) {
				scope.problemReporter().signalError(sourceStart, sourceEnd,
								"can not extend a concrete aspect");
				ignoreFurtherInvestigation = true;
				return;
			}
		}
	}
	
	private FieldBinding initFailureField= null;
	
	public void generateCode(ClassFile enclosingClassFile) {
		if (ignoreFurtherInvestigation) {
			if (binding == null)
				return;
			ClassFile.createProblemType(
				this,
				scope.referenceCompilationUnit().compilationResult);
			return;
		}
		
		
		// make me and my binding public
		this.modifiers = AstUtil.makePublic(this.modifiers);
		this.binding.modifiers = AstUtil.makePublic(this.binding.modifiers);
		
		
		if (!isAbstract()) {
			initFailureField = factory.makeFieldBinding(AjcMemberMaker.initFailureCauseField(typeX));
			binding.addField(initFailureField);
			
			if (perClause == null) {
				// we've already produced an error for this
			} else if (perClause.getKind() == PerClause.SINGLETON) {
				binding.addField(factory.makeFieldBinding(AjcMemberMaker.perSingletonField(
						typeX)));
				
				methods[0] = new AspectClinit((Clinit)methods[0], compilationResult, false, true, initFailureField);
			} else if (perClause.getKind() == PerClause.PERCFLOW) {
				binding.addField(
					factory.makeFieldBinding(
						AjcMemberMaker.perCflowField(
							typeX)));
				methods[0] = new AspectClinit((Clinit)methods[0], compilationResult, true, false, null);
			} else if (perClause.getKind() == PerClause.PEROBJECT) {
//				binding.addField(
//					world.makeFieldBinding(
//						AjcMemberMaker.perCflowField(
//							typeX)));
			} else {
				throw new RuntimeException("unimplemented");
			}
		}

		if (EclipseFactory.DEBUG) System.out.println(toString());
		
		super.generateCode(enclosingClassFile);
	}
	
	public boolean needClassInitMethod() {
		return true;
	}
	
	
	protected void generateAttributes(ClassFile classFile) {		
		if (!isAbstract()) generatePerSupportMembers(classFile);
		
		generateInlineAccessMembers(classFile);
		
		classFile.extraAttributes.add(
			new EclipseAttributeAdapter(new AjAttribute.Aspect(perClause)));
			
		if (binding.privilegedHandler != null) {
			ResolvedMember[] members = ((PrivilegedHandler)binding.privilegedHandler).getMembers();
			classFile.extraAttributes.add(
			new EclipseAttributeAdapter(new AjAttribute.PrivilegedAttribute(members)));
		}
		
		//XXX need to get this attribute on anyone with a pointcut for good errors
		classFile.extraAttributes.add(
			new EclipseAttributeAdapter(new AjAttribute.SourceContextAttribute(
				new String(compilationResult().getFileName()),
				compilationResult().lineSeparatorPositions)));

		super.generateAttributes(classFile);		
	}
	
	private void generateInlineAccessMembers(ClassFile classFile) {
		for (Iterator i = superAccessForInline.values().iterator(); i.hasNext(); ) {
			AccessForInlineVisitor.SuperAccessMethodPair pair = (AccessForInlineVisitor.SuperAccessMethodPair)i.next();
			generateSuperAccessMethod(classFile, pair.accessMethod, pair.originalMethod);
		}
		for (Iterator i = accessForInline.entrySet().iterator(); i.hasNext(); ) {
			Map.Entry e = (Map.Entry)i.next();
			generateInlineAccessMethod(classFile, (Binding)e.getValue(), (ResolvedMember)e.getKey());
		}
	}


	private void generatePerSupportMembers(ClassFile classFile) {
		if (isAbstract()) return;
		
		//XXX otherwise we need to have this (error handling?)
		if (aspectOfMethod == null) return;
		if (perClause == null) {
			System.err.println("has null perClause: " + this);
			return;
		}
		
		EclipseFactory world = EclipseFactory.fromScopeLookupEnvironment(this.scope);
		
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
			TypeBinding interfaceType = 
				generatePerObjectInterface(classFile);
			world.addTypeBinding(interfaceType);
			generatePerObjectAspectOfMethod(classFile, interfaceType);
			generatePerObjectHasAspectMethod(classFile, interfaceType);
			generatePerObjectBindMethod(classFile, interfaceType);
		} else {
			throw new RuntimeException("unimplemented");
		}
	}


	private static interface BodyGenerator {
		public void generate(CodeStream codeStream);
	}
	
	
	private void generateMethod(ClassFile classFile, ResolvedMember member, BodyGenerator gen) {
		final EclipseFactory world = EclipseFactory.fromScopeLookupEnvironment(this.scope);
		generateMethod(classFile, world.makeMethodBinding(member), gen);
	}
	
	private void generateMethod(ClassFile classFile, MethodBinding methodBinding, BodyGenerator gen) {
//		EclipseFactory world = EclipseFactory.fromScopeLookupEnvironment(this.scope);
		classFile.generateMethodInfoHeader(methodBinding);
		int methodAttributeOffset = classFile.contentsOffset;
		int attributeNumber = classFile.generateMethodInfoAttribute(methodBinding, AstUtil.getAjSyntheticAttribute());
		int codeAttributeOffset = classFile.contentsOffset;
		classFile.generateCodeAttributeHeader();
		CodeStream codeStream = classFile.codeStream;
		
		// Use reset() rather than init()
		// XXX We need a scope to keep reset happy, initializerScope is *not* the right one, but it works !
//		 codeStream.init(classFile);
//		 codeStream.initializeMaxLocals(methodBinding);
		MethodDeclaration md = AstUtil.makeMethodDeclaration(methodBinding);
		md.scope = initializerScope;
		codeStream.reset(md,classFile);
		
		// body starts here
		gen.generate(codeStream);
		// body ends here
		classFile.completeCodeAttribute(codeAttributeOffset);
		attributeNumber++;
		classFile.completeMethodInfo(methodAttributeOffset, attributeNumber);
	}		


	private void generatePerCflowAspectOfMethod(
		ClassFile classFile) 
	{
		final EclipseFactory world = EclipseFactory.fromScopeLookupEnvironment(this.scope);
		generateMethod(classFile, aspectOfMethod, new BodyGenerator() {
			public void generate(CodeStream codeStream) {
				// body starts here
				codeStream.getstatic(
					world.makeFieldBinding(
								AjcMemberMaker.perCflowField(
									typeX)));
				codeStream.invokevirtual(world.makeMethodBindingForCall(
								AjcMemberMaker.cflowStackPeekInstance()));
				codeStream.checkcast(binding);
				codeStream.areturn();
				// body ends here
			}});

	}


	private void generatePerCflowHasAspectMethod(ClassFile classFile) {
		final EclipseFactory world = EclipseFactory.fromScopeLookupEnvironment(this.scope);
		generateMethod(classFile, hasAspectMethod, new BodyGenerator() {
			public void generate(CodeStream codeStream) {
				// body starts here
				codeStream.getstatic(
					world.makeFieldBinding(
								AjcMemberMaker.perCflowField(
									typeX)));
				codeStream.invokevirtual(world.makeMethodBindingForCall(
								AjcMemberMaker.cflowStackIsValid()));
				codeStream.ireturn();
				// body ends here
			}});
	}
	
	private void generatePerCflowPushMethod(
		ClassFile classFile) 
	{
		final EclipseFactory world = EclipseFactory.fromScopeLookupEnvironment(this.scope);
		generateMethod(classFile, world.makeMethodBinding(AjcMemberMaker.perCflowPush(
				EclipseFactory.fromBinding(binding))), 
		new BodyGenerator() {
			public void generate(CodeStream codeStream) {
				// body starts here
				codeStream.getstatic(
					world.makeFieldBinding(
								AjcMemberMaker.perCflowField(
									typeX)));
				codeStream.new_(binding);
				codeStream.dup();
				codeStream.invokespecial(
					new MethodBinding(0, "<init>".toCharArray(), 
						BaseTypes.VoidBinding, new TypeBinding[0],
						new ReferenceBinding[0], binding));
					

				codeStream.invokevirtual(world.makeMethodBindingForCall(
								AjcMemberMaker.cflowStackPushInstance()));					
			    codeStream.return_();
				// body ends here
			}});

	}

		


	private void generatePerCflowAjcClinitMethod(
		ClassFile classFile) 
	{
		final EclipseFactory world = EclipseFactory.fromScopeLookupEnvironment(this.scope);
		generateMethod(classFile, world.makeMethodBinding(AjcMemberMaker.ajcPreClinitMethod(
				EclipseFactory.fromBinding(binding))), 
		new BodyGenerator() {
			public void generate(CodeStream codeStream) {
				// body starts here
				codeStream.new_(world.makeTypeBinding(AjcMemberMaker.CFLOW_STACK_TYPE));
				codeStream.dup();
				codeStream.invokespecial(world.makeMethodBindingForCall(AjcMemberMaker.cflowStackInit()));
				codeStream.putstatic(
					world.makeFieldBinding(
								AjcMemberMaker.perCflowField(
									typeX)));
			    codeStream.return_();
				// body ends here
			}});

	}
	
	
	private TypeBinding generatePerObjectInterface(
		ClassFile classFile)
	{
		final EclipseFactory world = EclipseFactory.fromScopeLookupEnvironment(this.scope);
		TypeX interfaceTypeX = 
		    AjcMemberMaker.perObjectInterfaceType(typeX);
		HelperInterfaceBinding interfaceType =
			new HelperInterfaceBinding(this.binding, interfaceTypeX);
		world.addTypeBinding(interfaceType);
		interfaceType.addMethod(world, AjcMemberMaker.perObjectInterfaceGet(typeX));
		interfaceType.addMethod(world, AjcMemberMaker.perObjectInterfaceSet(typeX));
		interfaceType.generateClass(compilationResult, classFile);
		return interfaceType;
	}
	
	
	private void generatePerObjectAspectOfMethod(
		ClassFile classFile,
		final TypeBinding interfaceType) 
	{
		final EclipseFactory world = EclipseFactory.fromScopeLookupEnvironment(this.scope);
		generateMethod(classFile, aspectOfMethod, new BodyGenerator() {
			public void generate(CodeStream codeStream) {
				// body starts here				
				Label wrongType = new Label(codeStream);
				Label popWrongType = new Label(codeStream);
				codeStream.aload_0();
				codeStream.instance_of(interfaceType);
				codeStream.ifeq(wrongType);
				codeStream.aload_0();
				codeStream.checkcast(interfaceType);
				codeStream.invokeinterface(world.makeMethodBindingForCall(
					AjcMemberMaker.perObjectInterfaceGet(typeX)));
					
				codeStream.dup();
				codeStream.ifnull(popWrongType);
				codeStream.areturn();
				
				popWrongType.place();
				codeStream.pop();
				
				wrongType.place();
				codeStream.new_(world.makeTypeBinding(AjcMemberMaker.NO_ASPECT_BOUND_EXCEPTION));
				codeStream.dup();
				codeStream.invokespecial(world.makeMethodBindingForCall(
					AjcMemberMaker.noAspectBoundExceptionInit()
				));
				codeStream.athrow();
				// body ends here
			}});

	}


	private void generatePerObjectHasAspectMethod(ClassFile classFile, 
		final TypeBinding interfaceType) {
		final EclipseFactory world = EclipseFactory.fromScopeLookupEnvironment(this.scope);
		generateMethod(classFile, hasAspectMethod, new BodyGenerator() {
			public void generate(CodeStream codeStream) {
				// body starts here
				Label wrongType = new Label(codeStream);
				codeStream.aload_0();
				codeStream.instance_of(interfaceType);
				codeStream.ifeq(wrongType);
				codeStream.aload_0();
				codeStream.checkcast(interfaceType);
				codeStream.invokeinterface(world.makeMethodBindingForCall(
					AjcMemberMaker.perObjectInterfaceGet(typeX)));
				codeStream.ifnull(wrongType);
				codeStream.iconst_1();
				codeStream.ireturn();
				
				wrongType.place();
				codeStream.iconst_0();
				codeStream.ireturn();
				// body ends here
			}});
	}
	
	private void generatePerObjectBindMethod(
		ClassFile classFile,
		final TypeBinding interfaceType) 
	{
		final EclipseFactory world = EclipseFactory.fromScopeLookupEnvironment(this.scope);
		generateMethod(classFile, AjcMemberMaker.perObjectBind(EclipseFactory.fromBinding(binding)), 
		new BodyGenerator() {
			public void generate(CodeStream codeStream) {
				// body starts here
				Label wrongType = new Label(codeStream);
				codeStream.aload_0();
				codeStream.instance_of(interfaceType);
				codeStream.ifeq(wrongType);  //XXX this case might call for screaming
				codeStream.aload_0();
				codeStream.checkcast(interfaceType);
				codeStream.invokeinterface(world.makeMethodBindingForCall(
					AjcMemberMaker.perObjectInterfaceGet(typeX)));
				//XXX should do a check for null here and throw a NoAspectBound
				codeStream.ifnonnull(wrongType);
				
				codeStream.aload_0();
				codeStream.checkcast(interfaceType);
				codeStream.new_(binding);
				codeStream.dup();
				codeStream.invokespecial(
					new MethodBinding(0, "<init>".toCharArray(), 
						BaseTypes.VoidBinding, new TypeBinding[0],
						new ReferenceBinding[0], binding));
				codeStream.invokeinterface(world.makeMethodBindingForCall(
					AjcMemberMaker.perObjectInterfaceSet(typeX)));
				
				wrongType.place();
				codeStream.return_();
				// body ends here
			}});
	}
	

		
	private void generatePerSingletonAspectOfMethod(ClassFile classFile) {
		final EclipseFactory world = EclipseFactory.fromScopeLookupEnvironment(this.scope);
		generateMethod(classFile, aspectOfMethod, new BodyGenerator() {
			public void generate(CodeStream codeStream) {
				// Old style aspectOf() method which confused decompilers
//				// body starts here
//				codeStream.getstatic(world.makeFieldBinding(AjcMemberMaker.perSingletonField(
//						typeX)));
//				Label isNull = new Label(codeStream);
//				codeStream.dup();
//				codeStream.ifnull(isNull);
//				codeStream.areturn();
//				isNull.place();
//				
//				codeStream.incrStackSize(+1);  // the dup trick above confuses the stack counter
//				codeStream.new_(world.makeTypeBinding(AjcMemberMaker.NO_ASPECT_BOUND_EXCEPTION));
//				codeStream.dup();
//				codeStream.ldc(typeX.getNameAsIdentifier());
//				codeStream.getstatic(initFailureField);
//				codeStream.invokespecial(world.makeMethodBindingForCall(
//					AjcMemberMaker.noAspectBoundExceptionInitWithCause()
//				));
//				codeStream.athrow();
//				// body ends here

				// The stuff below generates code that looks like this:
				/*
				 * if (ajc$perSingletonInstance == null)
				 *   throw new NoAspectBoundException("A", ajc$initFailureCause);
				 * else
				 *   return ajc$perSingletonInstance;
				 */
                // body starts here (see end of each line for what it is doing!)
				FieldBinding fb = world.makeFieldBinding(AjcMemberMaker.perSingletonField(typeX));
				codeStream.getstatic(fb);                                                              // GETSTATIC
				Label isNonNull = new Label(codeStream);
				codeStream.ifnonnull(isNonNull);                                                       // IFNONNULL
				codeStream.new_(world.makeTypeBinding(AjcMemberMaker.NO_ASPECT_BOUND_EXCEPTION));      // NEW
				codeStream.dup();                                                                      // DUP
				codeStream.ldc(typeX.getNameAsIdentifier());                                           // LDC
				codeStream.getstatic(initFailureField);                                                // GETSTATIC
				codeStream.invokespecial(world.makeMethodBindingForCall(
									AjcMemberMaker.noAspectBoundExceptionInitWithCause()));            // INVOKESPECIAL
				codeStream.athrow();                                                                   // ATHROW
				isNonNull.place();
				codeStream.getstatic(fb);                                                              // GETSTATIC
				codeStream.areturn();                                                                  // ARETURN
				// body ends here
			}});
	}
	
	private void generatePerSingletonHasAspectMethod(ClassFile classFile) {
		final EclipseFactory world = EclipseFactory.fromScopeLookupEnvironment(this.scope);
		generateMethod(classFile, hasAspectMethod, new BodyGenerator() {
			public void generate(CodeStream codeStream) {
				// body starts here
				codeStream.getstatic(world.makeFieldBinding(AjcMemberMaker.perSingletonField(
						typeX)));
				Label isNull = new Label(codeStream);
				codeStream.ifnull(isNull);
				codeStream.iconst_1();
				codeStream.ireturn();
				isNull.place();
				codeStream.iconst_0();
				codeStream.ireturn();
				// body ends here
			}});
	}
	
	
	private void generatePerSingletonAjcClinitMethod(
		ClassFile classFile) 
	{
		final EclipseFactory world = EclipseFactory.fromScopeLookupEnvironment(this.scope);
		generateMethod(classFile, world.makeMethodBinding(AjcMemberMaker.ajcPostClinitMethod(
				EclipseFactory.fromBinding(binding))), 
		new BodyGenerator() {
			public void generate(CodeStream codeStream) {
				// body starts here
				codeStream.new_(binding);
				codeStream.dup();
				codeStream.invokespecial(
					new MethodBinding(0, "<init>".toCharArray(), 
						BaseTypes.VoidBinding, new TypeBinding[0],
						new ReferenceBinding[0], binding));
					
				codeStream.putstatic(
					world.makeFieldBinding(
								AjcMemberMaker.perSingletonField(
									typeX)));
			    codeStream.return_();
				// body ends here
			}});

	}
	
	private void generateSuperAccessMethod(ClassFile classFile, final MethodBinding accessMethod, final ResolvedMember method) {
		generateMethod(classFile, accessMethod, 
		new BodyGenerator() {
			public void generate(CodeStream codeStream) {
				// body starts here
				codeStream.aload_0();
				AstUtil.generateParameterLoads(accessMethod.parameters, codeStream);
				codeStream.invokespecial(
					factory.makeMethodBinding(method));
				AstUtil.generateReturn(accessMethod.returnType, codeStream);
				// body ends here
			}});

	}
	

	private void generateInlineAccessMethod(ClassFile classFile, final Binding binding, final ResolvedMember member) {
		if (binding instanceof InlineAccessFieldBinding) {
			generateInlineAccessors(classFile, (InlineAccessFieldBinding)binding, member);
		} else {
			generateInlineAccessMethod(classFile, (MethodBinding)binding, member);
		}
	}
	
	private void generateInlineAccessors(ClassFile classFile, final InlineAccessFieldBinding accessField, final ResolvedMember field) {
		final FieldBinding fieldBinding = factory.makeFieldBinding(field);
		generateMethod(classFile, accessField.reader, 
		new BodyGenerator() {
			public void generate(CodeStream codeStream) {
				// body starts here
				if (field.isStatic()) {
					codeStream.getstatic(fieldBinding);
				} else {
					codeStream.aload_0();
					codeStream.getfield(fieldBinding);
				}
					
				AstUtil.generateReturn(accessField.reader.returnType, codeStream);
				// body ends here
			}});
			
		generateMethod(classFile, accessField.writer, 
		new BodyGenerator() {
			public void generate(CodeStream codeStream) {
				// body starts here
				if (field.isStatic()) {
					codeStream.load(fieldBinding.type, 0);
					codeStream.putstatic(fieldBinding);
				} else {
					codeStream.aload_0();
					codeStream.load(fieldBinding.type, 1);
					codeStream.putfield(fieldBinding);
				}	
					
				codeStream.return_();
				// body ends here
			}});

	}
	

	private void generateInlineAccessMethod(ClassFile classFile, final MethodBinding accessMethod, final ResolvedMember method) {
		generateMethod(classFile, accessMethod, 
		new BodyGenerator() {
			public void generate(CodeStream codeStream) {
				// body starts here
				
				AstUtil.generateParameterLoads(accessMethod.parameters, codeStream);
				
				if (method.isStatic()) {
					codeStream.invokestatic(factory.makeMethodBinding(method));
				} else {
				    codeStream.invokevirtual(factory.makeMethodBinding(method));
				}
					
				AstUtil.generateReturn(accessMethod.returnType, codeStream);
				// body ends here
			}});
	}
	

	
	private PerClause.Kind lookupPerClauseKind(ReferenceBinding binding) {
        PerClause perClause;
        if (binding instanceof BinaryTypeBinding) {
            ResolvedTypeX superTypeX = factory.fromEclipse(binding);
            perClause = superTypeX.getPerClause();
        } else if (binding instanceof SourceTypeBinding ) {
			SourceTypeBinding sourceSc = (SourceTypeBinding)binding;
			if (sourceSc.scope.referenceContext instanceof AspectDeclaration) {
				perClause = ((AspectDeclaration)sourceSc.scope.referenceContext).perClause;
			} else {
				return null;
			}
		} else {
			//XXX need to handle this too
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
		
		aspectAttribute = new AjAttribute.Aspect(perClause);
		
		if (ignoreFurtherInvestigation) return; //???
		
		
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
			} else {
				throw new RuntimeException("bad per clause: " + perClause);	
			}
			
			binding.addMethod(world.makeMethodBinding(aspectOfMethod));
			binding.addMethod(world.makeMethodBinding(hasAspectMethod));
		}
		resolvePerClause(); //XXX might be too soon for some error checking
	}


	private PerClause resolvePerClause() {        
        EclipseScope iscope = new EclipseScope(new FormalBinding[0], scope);
		perClause.resolve(iscope);
		return perClause;
	}



	public void buildInterTypeAndPerClause(ClassScope classScope) {
		factory = EclipseFactory.fromScopeLookupEnvironment(scope);
		if (isPrivileged) {
			binding.privilegedHandler = new PrivilegedHandler(this);
		}
		
		checkSpec(classScope);
		if (ignoreFurtherInvestigation) return;
		
		buildPerClause(scope);
		
		if (methods != null) {
			for (int i = 0; i < methods.length; i++) {
				if (methods[i] instanceof InterTypeDeclaration) {
					EclipseTypeMunger m = ((InterTypeDeclaration)methods[i]).build(classScope);
					if (m != null) concreteName.typeMungers.add(m);
				} else if (methods[i] instanceof DeclareDeclaration) {
					Declare d = ((DeclareDeclaration)methods[i]).build(classScope);
					if (d != null) concreteName.declares.add(d);
				}
			}
		}
        
        concreteName.getDeclaredPointcuts();
	}


//	public String toString(int tab) {
//		return tabString(tab) + toStringHeader() + toStringBody(tab);
//	}
//
//	public String toStringBody(int tab) {
//
//		String s = " {"; //$NON-NLS-1$
//		
//
//		if (memberTypes != null) {
//			for (int i = 0; i < memberTypes.length; i++) {
//				if (memberTypes[i] != null) {
//					s += "\n" + memberTypes[i].toString(tab + 1); //$NON-NLS-1$
//				}
//			}
//		}
//		if (fields != null) {
//			for (int fieldI = 0; fieldI < fields.length; fieldI++) {
//				if (fields[fieldI] != null) {
//					s += "\n" + fields[fieldI].toString(tab + 1); //$NON-NLS-1$
//					if (fields[fieldI].isField())
//						s += ";"; //$NON-NLS-1$
//				}
//			}
//		}
//		if (methods != null) {
//			for (int i = 0; i < methods.length; i++) {
//				if (methods[i] != null) {
//					s += "\n" + methods[i].toString(tab + 1); //$NON-NLS-1$
//				}
//			}
//		}
//		s += "\n" + tabString(tab) + "}"; //$NON-NLS-2$ //$NON-NLS-1$
//		return s;
//	}

	public StringBuffer printHeader(int indent, StringBuffer output) {
		printModifiers(this.modifiers, output);
		output.append("aspect " ); 
		output.append(name);
		if (superclass != null) {
			output.append(" extends ");  //$NON-NLS-1$
			superclass.print(0, output);
		}
		if (superInterfaces != null && superInterfaces.length > 0) {
			output.append(isInterface() ? " extends " : " implements ");//$NON-NLS-2$ //$NON-NLS-1$
			for (int i = 0; i < superInterfaces.length; i++) {
				if (i > 0) output.append( ", "); //$NON-NLS-1$
				superInterfaces[i].print(0, output);
			}
		}
		return output;		
		//XXX we should append the per-clause
	}
}

