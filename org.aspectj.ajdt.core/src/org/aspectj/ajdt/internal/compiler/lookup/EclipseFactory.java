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
 *     Mik Kersten	2004-07-26 extended to allow overloading of 
 * 					hierarchy builder
 * ******************************************************************/


package org.aspectj.ajdt.internal.compiler.lookup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.aspectj.ajdt.internal.compiler.ast.AspectDeclaration;
import org.aspectj.ajdt.internal.compiler.ast.AstUtil;
import org.aspectj.ajdt.internal.core.builder.AjBuildManager;
import org.aspectj.ajdt.internal.core.builder.AsmHierarchyBuilder;
import org.aspectj.bridge.ISourceLocation;
import org.aspectj.bridge.IMessage.Kind;
import org.aspectj.weaver.ConcreteTypeMunger;
import org.aspectj.weaver.IHasPosition;
import org.aspectj.weaver.Member;
import org.aspectj.weaver.ReferenceType;
import org.aspectj.weaver.ResolvedMember;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.Shadow;
import org.aspectj.weaver.TypeFactory;
import org.aspectj.weaver.TypeVariable;
import org.aspectj.weaver.TypeVariableReferenceType;
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.UnresolvedTypeVariableReferenceType;
import org.aspectj.weaver.World;
import org.aspectj.org.eclipse.jdt.core.compiler.CharOperation;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.EmptyStatement;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.aspectj.org.eclipse.jdt.internal.compiler.impl.Constant;
import org.aspectj.org.eclipse.jdt.internal.compiler.impl.ReferenceContext;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.BaseTypes;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.ParameterizedTypeBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.RawTypeBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.TypeVariableBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.WildcardBinding;
 
/**
 * @author Jim Hugunin
 */
public class EclipseFactory {
	public static boolean DEBUG = false;
	
	private AjBuildManager buildManager;
	private LookupEnvironment lookupEnvironment;
	private boolean xSerializableAspects;
	private World world;
	
	private Map/*UnresolvedType, TypeBinding*/ typexToBinding = new HashMap();
	//XXX currently unused
//	private Map/*TypeBinding, ResolvedType*/ bindingToResolvedTypeX = new HashMap();
	
	public static EclipseFactory fromLookupEnvironment(LookupEnvironment env) {
		AjLookupEnvironment aenv = (AjLookupEnvironment)env;
		return aenv.factory;
	}
	
	public static EclipseFactory fromScopeLookupEnvironment(Scope scope) {
		return fromLookupEnvironment(AstUtil.getCompilationUnitScope(scope).environment);
	}
	
	
	public EclipseFactory(LookupEnvironment lookupEnvironment,AjBuildManager buildManager) {
		this.lookupEnvironment = lookupEnvironment;
		this.buildManager = buildManager;
		this.world = buildManager.getWorld();
		this.xSerializableAspects = buildManager.buildConfig.isXserializableAspects();
	}
	
	public EclipseFactory(LookupEnvironment lookupEnvironment, World world, boolean xSer) {
		this.lookupEnvironment = lookupEnvironment;
		this.world = world;
		this.xSerializableAspects = xSer;
		this.buildManager = null;
	}
	
	public World getWorld() {
		return world;
	}
	
	public void showMessage(
		Kind kind,
		String message,
		ISourceLocation loc1,
		ISourceLocation loc2)
	{
		getWorld().showMessage(kind, message, loc1, loc2);
	}

	public ResolvedType fromEclipse(ReferenceBinding binding) {
		if (binding == null) return ResolvedType.MISSING;
		//??? this seems terribly inefficient
		//System.err.println("resolving: " + binding.getClass() + ", name = " + getName(binding));
		ResolvedType ret = getWorld().resolve(fromBinding(binding));
		//System.err.println("      got: " + ret);
		return ret;
	}	
	
	public ResolvedType fromTypeBindingToRTX(TypeBinding tb) {
		if (tb == null) return ResolvedType.MISSING;
		ResolvedType ret = getWorld().resolve(fromBinding(tb));
		return ret;
	}
	
	public ResolvedType[] fromEclipse(ReferenceBinding[] bindings) {
		if (bindings == null) {
			return ResolvedType.NONE;
		}
		int len = bindings.length;
		ResolvedType[] ret = new ResolvedType[len];
		for (int i=0; i < len; i++) {
			ret[i] = fromEclipse(bindings[i]);
		}
		return ret;
	}	
	
	public static String getName(TypeBinding binding) {
		if (binding instanceof TypeVariableBinding) {
			// The first bound may be null - so default to object?
			TypeVariableBinding tvb = (TypeVariableBinding)binding;
			if (tvb.firstBound!=null) {
				return getName(tvb.firstBound);
			} else {
				return getName(tvb.superclass);
			}
		}
		
		if (binding instanceof ReferenceBinding) {
			return new String(
				CharOperation.concatWith(((ReferenceBinding)binding).compoundName, '.'));
		}
		
		String packageName = new String(binding.qualifiedPackageName());
		String className = new String(binding.qualifiedSourceName()).replace('.', '$');
		if (packageName.length() > 0) {
			className = packageName + "." + className;
		}
		//XXX doesn't handle arrays correctly (or primitives?)
		return new String(className);
	}



	/**
	 * Some generics notes:
	 * 
	 * Andy 6-May-05
	 * We were having trouble with parameterized types in a couple of places - due to TypeVariableBindings.  When we
	 * see a TypeVariableBinding now we default to either the firstBound if it is specified or java.lang.Object.  Not
	 * sure when/if this gets us unstuck?  It does mean we forget that it is a type variable when going back
	 * the other way from the UnresolvedType and that would seem a bad thing - but I've yet to see the reason we need to
	 * remember the type variable.
	 * Adrian 10-July-05
	 * When we forget it's a type variable we come unstuck when getting the declared members of a parameterized
	 * type - since we don't know it's a type variable we can't replace it with the type parameter.
	 */
	//??? going back and forth between strings and bindings is a waste of cycles
	public UnresolvedType fromBinding(TypeBinding binding) {
		if (binding instanceof HelperInterfaceBinding) {
			return ((HelperInterfaceBinding) binding).getTypeX();
		}
		if (binding == null || binding.qualifiedSourceName() == null) {
			return ResolvedType.MISSING;
		}
		// first piece of generics support!
		if (binding instanceof TypeVariableBinding) {
			return fromTypeVariableBinding((TypeVariableBinding)binding);
		}
		
		if (binding instanceof ParameterizedTypeBinding) {
			if (binding instanceof RawTypeBinding) {
				// special case where no parameters are specified!
				return UnresolvedType.forRawTypeNames(getName(binding));
			}
			ParameterizedTypeBinding ptb = (ParameterizedTypeBinding) binding;
			
			UnresolvedType[] arguments = null;
			
			if (ptb.arguments!=null) { // null can mean this is an inner type of a Parameterized Type with no bounds of its own (pr100227)
				arguments = new UnresolvedType[ptb.arguments.length];
				for (int i = 0; i < arguments.length; i++) {
					if (ptb.arguments[i] instanceof WildcardBinding) {
						WildcardBinding wcb = (WildcardBinding) ptb.arguments[i];
						arguments[i] = fromTypeVariableBinding(wcb.typeVariable());
					}  else {
						arguments[i] = fromBinding(ptb.arguments[i]);
					}
				}
			}
			ResolvedType baseType = UnresolvedType.forName(getName(binding)).resolve(getWorld());
			return TypeFactory.createParameterizedType(
						baseType,
						arguments,
						getWorld());			
		}
		
		// Convert the source type binding for a generic type into a generic UnresolvedType
		// notice we can easily determine the type variables from the eclipse object
		// and we can recover the generic signature from it too - so we pass those
		// to the forGenericType() method.
		if (binding.isGenericType() && 
		    !binding.isParameterizedType() && 
		    !binding.isRawType()) {
			TypeVariableBinding[] tvbs = binding.typeVariables();
			TypeVariable[] tVars = new TypeVariable[tvbs.length];
			for (int i = 0; i < tvbs.length; i++) {
				TypeVariableBinding eclipseV = tvbs[i];
				String name = CharOperation.charToString(eclipseV.sourceName); 
				tVars[i] = new TypeVariable(name,fromBinding(eclipseV.superclass()),fromBindings(eclipseV.superInterfaces()));
			}
			//TODO asc generics - temporary guard....
			if (!(binding instanceof SourceTypeBinding))
				throw new RuntimeException("Cant get the generic sig for "+binding.debugName());
			return UnresolvedType.forGenericType(getName(binding),tVars,
					CharOperation.charToString(((SourceTypeBinding)binding).genericSignature()));
		} 
		
		return UnresolvedType.forName(getName(binding));
	}

	private static Map typeVariableBindingsInProgress = new HashMap();
	private  UnresolvedType fromTypeVariableBinding(TypeVariableBinding aTypeVariableBinding) {
		if (typeVariableBindingsInProgress.containsKey(aTypeVariableBinding)) {
			return (UnresolvedType) typeVariableBindingsInProgress.get(aTypeVariableBinding);
		}
		UnresolvedTypeVariableReferenceType ret = new UnresolvedTypeVariableReferenceType();
		typeVariableBindingsInProgress.put(aTypeVariableBinding,ret);
		// TODO -- what about lower bounds??
		String name = new String(aTypeVariableBinding.sourceName());
		UnresolvedType superclassType = fromBinding(aTypeVariableBinding.superclass());
		UnresolvedType[] superinterfaces = new UnresolvedType[aTypeVariableBinding.superInterfaces.length];
		for (int i = 0; i < superinterfaces.length; i++) {
			superinterfaces[i] = fromBinding(aTypeVariableBinding.superInterfaces[i]);
		}
		TypeVariable tv = new TypeVariable(name,superclassType,superinterfaces);
		tv.resolve(world);
		ret.setTypeVariable(tv);
		typeVariableBindingsInProgress.remove(aTypeVariableBinding);
		return ret;
	}
	
	public  UnresolvedType[] fromBindings(TypeBinding[] bindings) {
		if (bindings == null) return UnresolvedType.NONE;
		int len = bindings.length;
		UnresolvedType[] ret = new UnresolvedType[len];
		for (int i=0; i<len; i++) {
			ret[i] = fromBinding(bindings[i]);
		}
		return ret;
	}

	public static ASTNode astForLocation(IHasPosition location) {
		return new EmptyStatement(location.getStart(), location.getEnd());
	}
	
	public Collection getDeclareParents() {
		return getWorld().getDeclareParents();
	}
	
	public Collection getDeclareAnnotationOnTypes() {
		return getWorld().getDeclareAnnotationOnTypes();
	}
	
	public Collection getDeclareAnnotationOnFields() {
		return getWorld().getDeclareAnnotationOnFields();
	}
	
	public Collection getDeclareAnnotationOnMethods() {
		return getWorld().getDeclareAnnotationOnMethods();
	}
	
	public Collection finishedTypeMungers = null;
	
	public boolean areTypeMungersFinished() {
		return finishedTypeMungers != null;
	}
	
	public void finishTypeMungers() {
		// make sure that type mungers are
		Collection ret = new ArrayList();
		Collection baseTypeMungers = getWorld().getCrosscuttingMembersSet().getTypeMungers();
        baseTypeMungers.addAll(getWorld().getCrosscuttingMembersSet().getLateTypeMungers());

		for (Iterator i = baseTypeMungers.iterator(); i.hasNext(); ) {
			ConcreteTypeMunger munger = (ConcreteTypeMunger) i.next();
			EclipseTypeMunger etm = makeEclipseTypeMunger(munger);
			if (etm != null) ret.add(etm);
		}
		finishedTypeMungers = ret;
	}
	
	public EclipseTypeMunger makeEclipseTypeMunger(ConcreteTypeMunger concrete) {
		//System.err.println("make munger: " + concrete);
		//!!! can't do this if we want incremental to work right
		//if (concrete instanceof EclipseTypeMunger) return (EclipseTypeMunger)concrete;
		//System.err.println("   was not eclipse");
		
		
		if (concrete.getMunger() != null && EclipseTypeMunger.supportsKind(concrete.getMunger().getKind())) {
			AbstractMethodDeclaration method = null;
			if (concrete instanceof EclipseTypeMunger) {
				method = ((EclipseTypeMunger)concrete).getSourceMethod();
			}
			EclipseTypeMunger ret = 
				new EclipseTypeMunger(this, concrete.getMunger(), concrete.getAspectType(), method);
			if (ret.getSourceLocation() == null) {
				ret.setSourceLocation(concrete.getSourceLocation());
			}
			return ret;
		} else {
			return null;
		}
	}

	public Collection getTypeMungers() {
		//??? assert finishedTypeMungers != null
		return finishedTypeMungers;
	}
	
	public ResolvedMember makeResolvedMember(MethodBinding binding) {
		return makeResolvedMember(binding, binding.declaringClass);
	}

	public ResolvedMember makeResolvedMember(MethodBinding binding, TypeBinding declaringType) {
		//System.err.println("member for: " + binding + ", " + new String(binding.declaringClass.sourceName));
		// AMC these next two lines shouldn't be needed once we sort out generic types properly in the world map
		ResolvedType realDeclaringType = world.resolve(fromBinding(declaringType));
		if (realDeclaringType.isRawType()) realDeclaringType = realDeclaringType.getGenericType();
		ResolvedMember ret =  new ResolvedMember(
			binding.isConstructor() ? Member.CONSTRUCTOR : Member.METHOD,
			realDeclaringType,
			binding.modifiers,
			world.resolve(fromBinding(binding.returnType)),
			new String(binding.selector),
			world.resolve(fromBindings(binding.parameters)),
			world.resolve(fromBindings(binding.thrownExceptions)));
		return ret;
	}

	public ResolvedMember makeResolvedMember(FieldBinding binding) {
		return makeResolvedMember(binding, binding.declaringClass);
	}
	
	public ResolvedMember makeResolvedMember(FieldBinding binding, TypeBinding receiverType) {
		// AMC these next two lines shouldn't be needed once we sort out generic types properly in the world map
		ResolvedType realDeclaringType = world.resolve(fromBinding(receiverType));
		if (realDeclaringType.isRawType()) realDeclaringType = realDeclaringType.getGenericType();
		return new ResolvedMember(
			Member.FIELD,
			realDeclaringType,
			binding.modifiers,
			world.resolve(fromBinding(binding.type)),
			new String(binding.name),
			UnresolvedType.NONE);
	}
	
	public TypeBinding makeTypeBinding(UnresolvedType typeX) {
		TypeBinding ret = (TypeBinding)typexToBinding.get(typeX);
		if (ret == null) {
			ret = makeTypeBinding1(typeX);
			typexToBinding.put(typeX, ret);
		}
		if (ret == null) {
			System.out.println("can't find: " + typeX);
		}
		return ret;
	}
	
	private TypeBinding makeTypeBinding1(UnresolvedType typeX) {
		if (typeX.isPrimitiveType()) { 
			if (typeX == ResolvedType.BOOLEAN) return BaseTypes.BooleanBinding;
			if (typeX == ResolvedType.BYTE) return BaseTypes.ByteBinding;
			if (typeX == ResolvedType.CHAR) return BaseTypes.CharBinding;
			if (typeX == ResolvedType.DOUBLE) return BaseTypes.DoubleBinding;
			if (typeX == ResolvedType.FLOAT) return BaseTypes.FloatBinding;
			if (typeX == ResolvedType.INT) return BaseTypes.IntBinding;
			if (typeX == ResolvedType.LONG) return BaseTypes.LongBinding;
			if (typeX == ResolvedType.SHORT) return BaseTypes.ShortBinding;
			if (typeX == ResolvedType.VOID) return BaseTypes.VoidBinding;
			throw new RuntimeException("weird primitive type " + typeX);
		} else if (typeX.isArray()) {
			int dim = 0;
			while (typeX.isArray()) {
				dim++;
				typeX = typeX.getComponentType();
			}
			return lookupEnvironment.createArrayType(makeTypeBinding(typeX), dim);
		} else if (typeX.isParameterizedType()) {
		    // Converting back to a binding from a UnresolvedType
		    UnresolvedType[] typeParameters = typeX.getTypeParameters();
			ReferenceBinding baseTypeBinding = lookupBinding(typeX.getBaseName());
			TypeBinding[] argumentBindings = new TypeBinding[typeParameters.length];
			for (int i = 0; i < argumentBindings.length; i++) {
				argumentBindings[i] = makeTypeBinding(typeParameters[i]);
			}
			ParameterizedTypeBinding ptb = 
				lookupEnvironment.createParameterizedType(baseTypeBinding,argumentBindings,baseTypeBinding.enclosingType());
			return ptb;
		} else if (typeX.isRawType()) {
			ReferenceBinding baseTypeBinding = lookupBinding(typeX.getBaseName());
			RawTypeBinding rtb = lookupEnvironment.createRawType(baseTypeBinding,baseTypeBinding.enclosingType());
			return rtb;
		} else {
			return lookupBinding(typeX.getName());
		}
	}
	
	private ReferenceBinding lookupBinding(String sname) {
		char[][] name = CharOperation.splitOn('.', sname.toCharArray());
		ReferenceBinding rb = lookupEnvironment.getType(name);
		// XXX We do this because the pertypewithin aspectOf(Class) generated method needs it.  Without this
		// we don't get a 'rawtype' as the argument type for a messagesend to aspectOf() and this leads to 
		// a compile error if some client class calls aspectOf(A.class) or similar as it says Class<A> isn't
		// compatible with Class<T>
		if (sname.equals("java.lang.Class")) 
			rb = lookupEnvironment.createRawType(rb,rb.enclosingType());
		return rb;		
	}
	
	public TypeBinding[] makeTypeBindings(UnresolvedType[] types) {
		int len = types.length;
		TypeBinding[] ret = new TypeBinding[len];
		
		for (int i = 0; i < len; i++) {
			ret[i] = makeTypeBinding(types[i]);
		}
		return ret;
	}
	
	// just like the code above except it returns an array of ReferenceBindings
	private ReferenceBinding[] makeReferenceBindings(UnresolvedType[] types) {
		int len = types.length;
		ReferenceBinding[] ret = new ReferenceBinding[len];
		
		for (int i = 0; i < len; i++) {
			ret[i] = (ReferenceBinding)makeTypeBinding(types[i]);
		}
		return ret;
	}

	
	public FieldBinding makeFieldBinding(ResolvedMember member) {
		return new FieldBinding(member.getName().toCharArray(),
				makeTypeBinding(member.getReturnType()),
				member.getModifiers(),
				(ReferenceBinding)makeTypeBinding(member.getDeclaringType()),
				Constant.NotAConstant);
	}


	public MethodBinding makeMethodBinding(ResolvedMember member) {
		return new MethodBinding(member.getModifiers(),
				member.getName().toCharArray(),
				makeTypeBinding(member.getReturnType()),
				makeTypeBindings(member.getParameterTypes()),
				makeReferenceBindings(member.getExceptions()),
				(ReferenceBinding)makeTypeBinding(member.getDeclaringType()));
	}


	
	public MethodBinding makeMethodBindingForCall(Member member) {
		return new MethodBinding(member.getCallsiteModifiers(),
				member.getName().toCharArray(),
				makeTypeBinding(member.getReturnType()),
				makeTypeBindings(member.getParameterTypes()),
				new ReferenceBinding[0],
				(ReferenceBinding)makeTypeBinding(member.getDeclaringType()));
	}

	public void finishedCompilationUnit(CompilationUnitDeclaration unit) {
		if ((buildManager != null) && buildManager.doGenerateModel()) {
		    AjBuildManager.getAsmHierarchyBuilder().buildStructureForCompilationUnit(unit, buildManager.getStructureModel(), buildManager.buildConfig);
		}
	}

	public void addTypeBinding(TypeBinding binding) {
		typexToBinding.put(fromBinding(binding), binding);
	}


	public Shadow makeShadow(ASTNode location, ReferenceContext context) {
		return EclipseShadow.makeShadow(this, location, context);
	}
	
	public Shadow makeShadow(ReferenceContext context) {
		return EclipseShadow.makeShadow(this, (ASTNode) context, context);
	}
	
	public void addSourceTypeBinding(SourceTypeBinding binding) {
		TypeDeclaration decl = binding.scope.referenceContext;
		
		// Deal with the raw/basic type to give us an entry in the world type map
		UnresolvedType simpleTx = null;
		if (binding.isGenericType()) {
		    simpleTx  = UnresolvedType.forRawTypeNames(getName(binding)); 
		} else {
			simpleTx  = UnresolvedType.forName(getName(binding)); 
		}
		ReferenceType name  = getWorld().lookupOrCreateName(simpleTx);
		EclipseSourceType t = new EclipseSourceType(name, this, binding, decl);
		
		// For generics, go a bit further - build a typex for the generic type
		// give it the same delegate and link it to the raw type
		if (binding.isGenericType()) {
			UnresolvedType complexTx = fromBinding(binding); // fully aware of any generics info
			ReferenceType complexName = new ReferenceType(complexTx,world);//getWorld().lookupOrCreateName(complexTx);
			name.setGenericType(complexName);
			complexName.setDelegate(t);
			complexName.setSourceContext(t.getResolvedTypeX().getSourceContext());
		}
				
		name.setDelegate(t);
		if (decl instanceof AspectDeclaration) {
			((AspectDeclaration)decl).typeX = name;
			((AspectDeclaration)decl).concreteName = t;
		}
		
		ReferenceBinding[] memberTypes = binding.memberTypes;
		for (int i = 0, length = memberTypes.length; i < length; i++) {
			addSourceTypeBinding((SourceTypeBinding) memberTypes[i]);
		}
	}
	
	// XXX this doesn't feel like it belongs here, but it breaks a hard dependency on
	// exposing AjBuildManager (needed by AspectDeclaration).
	public boolean isXSerializableAspects() {
		return xSerializableAspects;
	}
}
