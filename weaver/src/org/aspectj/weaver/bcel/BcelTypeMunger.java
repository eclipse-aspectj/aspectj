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


package org.aspectj.weaver.bcel;

import java.lang.reflect.Modifier;
import java.util.Iterator;
import java.util.Set;

import org.apache.bcel.Constants;
import org.apache.bcel.generic.FieldGen;
import org.apache.bcel.generic.InstructionFactory;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.Type;
import org.aspectj.weaver.AjcMemberMaker;
import org.aspectj.weaver.ConcreteTypeMunger;
import org.aspectj.weaver.Member;
import org.aspectj.weaver.NameMangler;
import org.aspectj.weaver.NewConstructorTypeMunger;
import org.aspectj.weaver.NewFieldTypeMunger;
import org.aspectj.weaver.NewMethodTypeMunger;
import org.aspectj.weaver.NewParentTypeMunger;
import org.aspectj.weaver.PerObjectInterfaceTypeMunger;
import org.aspectj.weaver.PrivilegedAccessMunger;
import org.aspectj.weaver.ResolvedMember;
import org.aspectj.weaver.ResolvedTypeMunger;
import org.aspectj.weaver.ResolvedTypeX;
import org.aspectj.weaver.TypeX;
import org.aspectj.weaver.patterns.Pointcut;


//XXX addLazyMethodGen is probably bad everywhere
public class BcelTypeMunger extends ConcreteTypeMunger {

	public BcelTypeMunger(ResolvedTypeMunger munger, ResolvedTypeX aspectType) {
		super(munger, aspectType);
	}

	public String toString() {
		return "(BcelTypeMunger " + getMunger() + ")";
	}

	public boolean munge(BcelClassWeaver weaver) {
		if (munger.getKind() == ResolvedTypeMunger.Field) {
			return mungeNewField(weaver, (NewFieldTypeMunger)munger);
		} else if (munger.getKind() == ResolvedTypeMunger.Method) {
			return mungeNewMethod(weaver, (NewMethodTypeMunger)munger);
		} else if (munger.getKind() == ResolvedTypeMunger.PerObjectInterface) {
			return mungePerObjectInterface(weaver, (PerObjectInterfaceTypeMunger)munger);
		} else if (munger.getKind() == ResolvedTypeMunger.PrivilegedAccess) {
			return mungePrivilegedAccess(weaver, (PrivilegedAccessMunger)munger);
		} else if (munger.getKind() == ResolvedTypeMunger.Constructor) {
			return mungeNewConstructor(weaver, (NewConstructorTypeMunger)munger);
		} else if (munger.getKind() == ResolvedTypeMunger.Parent) {
			return mungeNewParent(weaver, (NewParentTypeMunger)munger);
		} else {
			throw new RuntimeException("unimplemented");
		}
	}


	private boolean mungeNewParent(BcelClassWeaver weaver, NewParentTypeMunger munger) {
		LazyClassGen gen = weaver.getLazyClassGen();
		ResolvedTypeX newParent = munger.getNewParent();
		if (newParent.isClass()) {
			//gen.setSuperClass(newParent);
		} else {
			gen.addInterface(newParent);
		}
		return true;
	}

	private boolean mungePrivilegedAccess(
		BcelClassWeaver weaver,
		PrivilegedAccessMunger munger)
	{
		LazyClassGen gen = weaver.getLazyClassGen();
		ResolvedMember member = munger.getMember();
		
		ResolvedTypeX onType = weaver.getWorld().resolve(member.getDeclaringType());
		//System.out.println("munging: " + gen + " with " + member);
		if (onType.equals(gen.getType())) {
			if (member.getKind() == Member.FIELD) {
				//System.out.println("matched: " + gen);
				addFieldGetter(gen, member,
					AjcMemberMaker.privilegedAccessMethodForFieldGet(aspectType, member));
				addFieldSetter(gen, member,
					AjcMemberMaker.privilegedAccessMethodForFieldSet(aspectType, member));
				return true;
			} else if (member.getKind() == Member.METHOD) {
				addMethodDispatch(gen, member,
					AjcMemberMaker.privilegedAccessMethodForMethod(aspectType, member));
				return true;
			} else if (member.getKind() == Member.CONSTRUCTOR) {
				for (Iterator i = gen.getMethodGens().iterator(); i.hasNext(); ) {
					LazyMethodGen m = (LazyMethodGen)i.next();
					if (m.getMemberView() != null && m.getMemberView().getKind() == Member.CONSTRUCTOR) {
						// m.getMemberView().equals(member)) {
						m.forcePublic();
						//return true;
					}
				}
				return true;
				//throw new BCException("no match for " + member + " in " + gen);
			} else if (member.getKind() == Member.STATIC_INITIALIZATION) {
				gen.forcePublic();
				return true;
			} else {
				throw new RuntimeException("unimplemented");
			}
		}
		return false;
	}

	private void addFieldGetter(
		LazyClassGen gen,
		ResolvedMember field,
		ResolvedMember accessMethod)
	{
		LazyMethodGen mg = makeMethodGen(gen, accessMethod);
		InstructionList il = new InstructionList();
		InstructionFactory fact = gen.getFactory();
		if (field.isStatic()) {
			il.append(fact.createFieldAccess(
				gen.getClassName(), 
				field.getName(),
				BcelWorld.makeBcelType(field.getType()), Constants.GETSTATIC));
		} else {
			il.append(fact.ALOAD_0);
			il.append(fact.createFieldAccess(
				gen.getClassName(), 
				field.getName(),
				BcelWorld.makeBcelType(field.getType()), Constants.GETFIELD));
		}
		il.append(fact.createReturn(BcelWorld.makeBcelType(field.getType())));
		mg.getBody().insert(il);
				
		gen.addMethodGen(mg);
	}
	
	private void addFieldSetter(
		LazyClassGen gen,
		ResolvedMember field,
		ResolvedMember accessMethod)
	{
		LazyMethodGen mg = makeMethodGen(gen, accessMethod);
		InstructionList il = new InstructionList();
		InstructionFactory fact = gen.getFactory();
		Type fieldType = BcelWorld.makeBcelType(field.getType());
		
		if (field.isStatic()) {
			il.append(fact.createLoad(fieldType, 0));
			il.append(fact.createFieldAccess(
				gen.getClassName(), 
				field.getName(),
				fieldType, Constants.PUTSTATIC));
		} else {
			il.append(fact.ALOAD_0);
			il.append(fact.createLoad(fieldType, 1));
			il.append(fact.createFieldAccess(
				gen.getClassName(), 
				field.getName(),
				fieldType, Constants.PUTFIELD));
		}
		il.append(fact.createReturn(Type.VOID));
		mg.getBody().insert(il);
				
		gen.addMethodGen(mg);
	}
	
	private void addMethodDispatch(
		LazyClassGen gen,
		ResolvedMember method,
		ResolvedMember accessMethod)
	{
		LazyMethodGen mg = makeMethodGen(gen, accessMethod);
		InstructionList il = new InstructionList();
		InstructionFactory fact = gen.getFactory();
		//Type fieldType = BcelWorld.makeBcelType(field.getType());
		Type[] paramTypes = BcelWorld.makeBcelTypes(method.getParameterTypes());
		
		int pos = 0;
	
		if (!method.isStatic()) {
			il.append(fact.ALOAD_0);
			pos++;
		}
		for (int i = 0, len = paramTypes.length; i < len; i++) {
			Type paramType = paramTypes[i];
			il.append(fact.createLoad(paramType, pos));
			pos+=paramType.getSize();
		}
		il.append(Utility.createInvoke(fact, (BcelWorld)aspectType.getWorld(), 
				method));
		il.append(fact.createReturn(BcelWorld.makeBcelType(method.getReturnType())));

		mg.getBody().insert(il);
				
		gen.addMethodGen(mg);
	}
	
	
	
	private LazyMethodGen makeMethodGen(LazyClassGen gen, ResolvedMember member) {
		return new LazyMethodGen(
			member.getModifiers(),
			BcelWorld.makeBcelType(member.getReturnType()),
			member.getName(),
			BcelWorld.makeBcelTypes(member.getParameterTypes()),
			TypeX.getNames(member.getExceptions()),
			gen);
	}


	private FieldGen makeFieldGen(LazyClassGen gen, ResolvedMember member) {
		return new FieldGen(
			member.getModifiers(),
			BcelWorld.makeBcelType(member.getReturnType()),
			member.getName(),
			gen.getConstantPoolGen());
	}


	

	private boolean mungePerObjectInterface(
		BcelClassWeaver weaver,
		PerObjectInterfaceTypeMunger munger)
	{
		LazyClassGen gen = weaver.getLazyClassGen();
		
		if (couldMatch(gen.getBcelObjectType(), munger.getTestPointcut())) {
			FieldGen fg = makeFieldGen(gen, 
				AjcMemberMaker.perObjectField(gen.getType(), aspectType));

	    	gen.addField(fg.getField());
	    	
	    	
	    	Type fieldType = BcelWorld.makeBcelType(aspectType);
			LazyMethodGen mg = new LazyMethodGen(
				Modifier.PUBLIC,
				fieldType,
    			NameMangler.perObjectInterfaceGet(aspectType),
				new Type[0], new String[0],
				gen);
			InstructionList il = new InstructionList();
			InstructionFactory fact = gen.getFactory();
			il.append(fact.ALOAD_0);
			il.append(fact.createFieldAccess(
				gen.getClassName(), 
				fg.getName(),
				fieldType, Constants.GETFIELD));
			il.append(fact.createReturn(fieldType));
			mg.getBody().insert(il);
				
			gen.addMethodGen(mg);
			
			LazyMethodGen mg1 = new LazyMethodGen(
				Modifier.PUBLIC,
				Type.VOID,
				NameMangler.perObjectInterfaceSet(aspectType),
				
				new Type[]{fieldType,}, new String[0],
				gen);
			InstructionList il1 = new InstructionList();
			il1.append(fact.ALOAD_0);
			il1.append(fact.createLoad(fieldType, 1));
			il1.append(fact.createFieldAccess(
				gen.getClassName(), 
				fg.getName(), 
				fieldType, Constants.PUTFIELD));
			il1.append(fact.createReturn(Type.VOID));
			mg1.getBody().insert(il1);
				
			gen.addMethodGen(mg1);
			
			gen.addInterface(munger.getInterfaceType());

			return true;
		} else {
			return false;
		}
	}

	private boolean couldMatch(
		BcelObjectType bcelObjectType,
		Pointcut pointcut) {
		return !bcelObjectType.isInterface();
	}
	
	private boolean mungeNewMethod(BcelClassWeaver weaver, NewMethodTypeMunger munger) {
		ResolvedMember signature = munger.getSignature();
		ResolvedMember dispatchMethod = munger.getDispatchMethod(aspectType);
		
		LazyClassGen gen = weaver.getLazyClassGen();
		
		ResolvedTypeX onType = weaver.getWorld().resolve(signature.getDeclaringType());
		boolean onInterface = onType.isInterface();
		
		if (onType.equals(gen.getType())) {
			ResolvedMember introMethod = 
					AjcMemberMaker.interMethod(signature, aspectType, onInterface);
			
			LazyMethodGen mg = makeMethodGen(gen, introMethod);

			if (!onInterface && !Modifier.isAbstract(introMethod.getModifiers())) {
				InstructionList body = mg.getBody();
				InstructionFactory fact = gen.getFactory();
				int pos = 0;
	
				if (!signature.isStatic()) {
					body.append(fact.createThis());
					pos++;
				}
				Type[] paramTypes = BcelWorld.makeBcelTypes(introMethod.getParameterTypes());
				for (int i = 0, len = paramTypes.length; i < len; i++) {
					Type paramType = paramTypes[i];
					body.append(fact.createLoad(paramType, pos));
					pos+=paramType.getSize();
				}
				body.append(Utility.createInvoke(fact, weaver.getWorld(), dispatchMethod));
				body.append(fact.createReturn(BcelWorld.makeBcelType(introMethod.getReturnType())));
			} else {
				//??? this is okay
				//if (!(mg.getBody() == null)) throw new RuntimeException("bas");
			}
			

			// XXX make sure to check that we set exceptions properly on this guy.
			weaver.addLazyMethodGen(mg);
			
			Set neededSuperCalls = munger.getSuperMethodsCalled();

			for (Iterator iter = neededSuperCalls.iterator(); iter.hasNext(); ) {
				ResolvedMember superMethod = (ResolvedMember) iter.next();
				if (weaver.addDispatchTarget(superMethod)) {
					//System.err.println("super type: " + superMethod.getDeclaringType() + ", " + gen.getType());
					boolean isSuper = !superMethod.getDeclaringType().equals(gen.getType());
					String dispatchName;
					if (isSuper) dispatchName = NameMangler.superDispatchMethod(onType, superMethod.getName());
					else dispatchName = NameMangler.protectedDispatchMethod(onType, superMethod.getName());
					LazyMethodGen dispatcher = makeDispatcher(gen, dispatchName, superMethod, weaver.getWorld(), isSuper);

					weaver.addLazyMethodGen(dispatcher);
				}
			}
			
    		return true;
		} else if (onInterface && gen.getType().isTopmostImplementor(onType) && 
						!Modifier.isAbstract(signature.getModifiers()))
		{
			ResolvedMember introMethod = 
					AjcMemberMaker.interMethod(signature, aspectType, false);
			
			LazyMethodGen mg = makeMethodGen(gen, introMethod);
			
			// 
						
			Type[] paramTypes = BcelWorld.makeBcelTypes(introMethod.getParameterTypes());
			Type returnType = BcelWorld.makeBcelType(introMethod.getReturnType());
			
			InstructionList body = mg.getBody();
			InstructionFactory fact = gen.getFactory();
			int pos = 0;

			if (!introMethod.isStatic()) {
				body.append(fact.createThis());
				pos++;
			}
			for (int i = 0, len = paramTypes.length; i < len; i++) {
				Type paramType = paramTypes[i];
				body.append(fact.createLoad(paramType, pos));
				pos+=paramType.getSize();
			}
			body.append(Utility.createInvoke(fact, weaver.getWorld(), dispatchMethod));
			body.append(fact.createReturn(returnType));
			mg.definingType = onType;
			
			weaver.addOrReplaceLazyMethodGen(mg);
			
			return true;
		} else {
			return false;
		}
	}


	private boolean mungeNewConstructor(
		BcelClassWeaver weaver,
		NewConstructorTypeMunger newConstructorTypeMunger) 
	{
		final LazyClassGen currentClass = weaver.getLazyClassGen();
		final InstructionFactory fact = currentClass.getFactory();

		ResolvedMember newConstructorMember = newConstructorTypeMunger.getSyntheticConstructor();
		TypeX          onType = newConstructorMember.getDeclaringType();
		
		
		if (! onType.equals(currentClass.getType())) return false;

		ResolvedMember explicitConstructor = newConstructorTypeMunger.getExplicitConstructor();
		//int declaredParameterCount = newConstructorTypeMunger.getDeclaredParameterCount();
		LazyMethodGen freshConstructor = 
			makeMethodGen(currentClass, newConstructorMember);
		currentClass.addMethodGen(freshConstructor);
		//weaver.addLazyMethodGen(freshConstructor);
		
		InstructionList body = freshConstructor.getBody();
		
		// add to body:  push arts for call to pre, from actual args starting at 1 (skipping this), going to 
		//               declared argcount + 1
		TypeX[] declaredParams = newConstructorTypeMunger.getSignature().getParameterTypes();
		Type[] paramTypes = freshConstructor.getArgumentTypes();
		int frameIndex = 1;
		for (int i = 0, len = declaredParams.length; i < len; i++) {
			body.append(fact.createLoad(paramTypes[i], frameIndex));
			frameIndex += paramTypes[i].getSize();
		}
		// do call to pre
		Member preMethod = AjcMemberMaker.preIntroducedConstructor(aspectType, onType, declaredParams);
		body.append(Utility.createInvoke(fact, null, preMethod));
		
		// create a local, and store return pre stuff into it.
		int arraySlot = freshConstructor.allocateLocal(1);
		body.append(fact.createStore(Type.OBJECT, arraySlot));
		
		// put this on the stack
		body.append(fact.ALOAD_0);
		
		// unpack pre args onto stack
		TypeX[] superParamTypes = explicitConstructor.getParameterTypes();
		
		for (int i = 0, len = superParamTypes.length; i < len; i++) {
			body.append(fact.createLoad(Type.OBJECT, arraySlot));
			body.append(Utility.createConstant(fact, i));
			body.append(fact.createArrayLoad(Type.OBJECT));
			body.append(Utility.createConversion(fact, Type.OBJECT, BcelWorld.makeBcelType(superParamTypes[i])));
		}

		// call super/this
		
		body.append(Utility.createInvoke(fact, null, explicitConstructor));
		
		// put this back on the stack

		body.append(fact.ALOAD_0);
		
		// unpack params onto stack
		Member postMethod = AjcMemberMaker.postIntroducedConstructor(aspectType, onType, declaredParams);
		TypeX[] postParamTypes = postMethod.getParameterTypes();
		
		for (int i = 1, len = postParamTypes.length; i < len; i++) {
			body.append(fact.createLoad(Type.OBJECT, arraySlot));
			body.append(Utility.createConstant(fact, superParamTypes.length + i-1));
			body.append(fact.createArrayLoad(Type.OBJECT));
			body.append(Utility.createConversion(fact, Type.OBJECT, BcelWorld.makeBcelType(postParamTypes[i])));
		}		
		
		// call post
		body.append(Utility.createInvoke(fact, null, postMethod));
		
		// don't forget to return!!
		
		body.append(fact.RETURN);
		
		return true;		
	}


	private static LazyMethodGen makeDispatcher(
		LazyClassGen onGen,
		String dispatchName,
		ResolvedMember superMethod,
		BcelWorld world,
		boolean isSuper) 
	{
		Type[] paramTypes = BcelWorld.makeBcelTypes(superMethod.getParameterTypes());
		Type returnType = BcelWorld.makeBcelType(superMethod.getReturnType());
				
		LazyMethodGen mg = 
				new LazyMethodGen(
					Modifier.PUBLIC,
					returnType,
					dispatchName,
					paramTypes,
					TypeX.getNames(superMethod.getExceptions()),
					onGen);
		InstructionList body = mg.getBody();
		
		// assert (!superMethod.isStatic())
		InstructionFactory fact = onGen.getFactory();
		int pos = 0;
		
		body.append(fact.createThis());
		pos++;
		for (int i = 0, len = paramTypes.length; i < len; i++) {
			Type paramType = paramTypes[i];
			body.append(fact.createLoad(paramType, pos));
			pos+=paramType.getSize();
		}
		if (isSuper) {
			body.append(Utility.createSuperInvoke(fact, world, superMethod));
		} else {
			body.append(Utility.createInvoke(fact, world, superMethod));
		}
		body.append(fact.createReturn(returnType));

		return mg;
	}	
	
	private boolean mungeNewField(BcelClassWeaver weaver, NewFieldTypeMunger munger) {
		ResolvedMember initMethod = munger.getInitMethod(aspectType);
		
		LazyClassGen gen = weaver.getLazyClassGen();
		ResolvedMember field = munger.getSignature();
		
		
		ResolvedTypeX onType = weaver.getWorld().resolve(field.getDeclaringType());
		boolean onInterface = onType.isInterface();
		
		if (onType.equals(gen.getType())) {
			if (onInterface) {
				LazyMethodGen mg = makeMethodGen(gen, 
					AjcMemberMaker.interFieldInterfaceGetter(field, onType, aspectType));
				gen.addMethodGen(mg);
				
				LazyMethodGen mg1 = makeMethodGen(gen, 
					AjcMemberMaker.interFieldInterfaceSetter(field, onType, aspectType));
				gen.addMethodGen(mg1);
			} else {
				weaver.addInitializer(this);
				FieldGen fg = makeFieldGen(gen,
					AjcMemberMaker.interFieldClassField(field, aspectType));
	    		gen.addField(fg.getField());
			}
    		return true;
		} else if (onInterface && gen.getType().isTopmostImplementor(onType)) {
			// wew know that we can't be static since we don't allow statics on interfaces
			if (field.isStatic()) throw new RuntimeException("unimplemented");
			weaver.addInitializer(this);
			//System.err.println("impl body on " + gen.getType() + " for " + munger);
			Type fieldType = 	BcelWorld.makeBcelType(field.getType());
			
			FieldGen fg = makeFieldGen(gen,
					AjcMemberMaker.interFieldInterfaceField(field, onType, aspectType));
	    	gen.addField(fg.getField());
			
	    	//this uses a shadow munger to add init method to constructors
	    	//weaver.getShadowMungers().add(makeInitCallShadowMunger(initMethod));
	    	
			LazyMethodGen mg = makeMethodGen(gen, 
					AjcMemberMaker.interFieldInterfaceGetter(field, gen.getType(), aspectType));
			InstructionList il = new InstructionList();
			InstructionFactory fact = gen.getFactory();
			if (field.isStatic()) {
				il.append(fact.createFieldAccess(
					gen.getClassName(), 
					fg.getName(),
					fieldType, Constants.GETSTATIC));
			} else {
				il.append(fact.ALOAD_0);
				il.append(fact.createFieldAccess(
					gen.getClassName(), 
					fg.getName(),
					fieldType, Constants.GETFIELD));
			}
			il.append(fact.createReturn(fieldType));
			mg.getBody().insert(il);
				
			gen.addMethodGen(mg);
			
			LazyMethodGen mg1 = makeMethodGen(gen, 
					AjcMemberMaker.interFieldInterfaceSetter(field, gen.getType(), aspectType));
			InstructionList il1 = new InstructionList();
			if (field.isStatic()) {
				il1.append(fact.createLoad(fieldType, 0));
				il1.append(fact.createFieldAccess(
					gen.getClassName(), 
					fg.getName(),
					fieldType, Constants.PUTSTATIC));
			} else {
				il1.append(fact.ALOAD_0);
				il1.append(fact.createLoad(fieldType, 1));
				il1.append(fact.createFieldAccess(
					gen.getClassName(), 
					fg.getName(), 
					fieldType, Constants.PUTFIELD));
			}
			il1.append(fact.createReturn(Type.VOID));
			mg1.getBody().insert(il1);
				
			gen.addMethodGen(mg1);

			return true;
		} else {
			return false;
		}
	}
}
