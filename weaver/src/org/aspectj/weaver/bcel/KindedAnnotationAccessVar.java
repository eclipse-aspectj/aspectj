/* *******************************************************************
 * Copyright (c) 2005 IBM
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
import org.aspectj.apache.bcel.generic.Instruction;
import org.aspectj.apache.bcel.generic.InstructionConstants;
import org.aspectj.apache.bcel.generic.InstructionFactory;
import org.aspectj.apache.bcel.generic.InstructionList;
import org.aspectj.apache.bcel.generic.ObjectType;
import org.aspectj.apache.bcel.generic.Type;
import org.aspectj.weaver.Member;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.Shadow;
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.Shadow.Kind;


/**
 * Represents access to an annotation on an element, relating to some 'kinded' pointcut.  
 * Depending on the kind of pointcut the element might be a field or a method or a ...
 */
public class KindedAnnotationAccessVar extends BcelVar {

	private Kind   kind;           // What kind of shadow are we at?
	private UnresolvedType  containingType; // The type upon which we want to ask for 'member'
	private Member member;         // For method call/execution and field get/set contains the member that has the annotation
	
	public KindedAnnotationAccessVar(Kind kind, ResolvedType type,UnresolvedType theTargetIsStoredHere,Member sig) {
		super(type,0);
		this.kind = kind;
		this.containingType = theTargetIsStoredHere; 
		this.member = sig;
	}

	public String toString() {
		return "KindedAnnotationAccessVar(" + getType() +")";
	}

    public Instruction createLoad(InstructionFactory fact) {
		throw new RuntimeException("unimplemented");
    }
    public Instruction createStore(InstructionFactory fact) {
    	throw new RuntimeException("unimplemented");
    }

    public InstructionList createCopyFrom(InstructionFactory fact, int oldSlot) {
        throw new RuntimeException("unimplemented");
    }
    
	public void appendLoad(InstructionList il, InstructionFactory fact) {
		il.append(createLoadInstructions(getType(), fact));
	}

	// FIXME asc Refactor all this stuff - there is a lot of commonality	
	public InstructionList createLoadInstructions(ResolvedType toType, InstructionFactory fact) {
		
		// FIXME asc Decide on whether we ought to build an exception handler for the NoSuchMethodException that can be thrown
		// by getDeclaredMethod()... right now we don't but no-one seems to care...
//		LocalVariableGen var_ex = mg.addLocalVariable("ex",Type.getType("Ljava.io.IOException;"),null,null);
//		int var_ex_slot = var_ex.getIndex();
//		
//		InstructionHandle handler = il.append(new ASTORE(var_ex_slot));
//		var_ex.setStart(handler);
//		var_ex.setEnd(il.append(InstructionConstants.RETURN));
//		
//		mg.addExceptionHandler(try_start, try_end, handler,
//				new ObjectType("java.io.IOException"));
		
		
		InstructionList il = new InstructionList();
		Type jlClass = BcelWorld.makeBcelType(UnresolvedType.JAVA_LANG_CLASS);

		Type jlString = BcelWorld.makeBcelType(UnresolvedType.forSignature("Ljava.lang.String;"));
		Type jlClassArray = BcelWorld.makeBcelType(UnresolvedType.forSignature("[Ljava.lang.Class;"));
		
		Type jlaAnnotation = BcelWorld.makeBcelType(UnresolvedType.forSignature("Ljava.lang.annotation.Annotation;"));
		
		if (kind==Shadow.MethodCall || kind==Shadow.MethodExecution || 
			kind==Shadow.PreInitialization || kind==Shadow.Initialization || 
			kind==Shadow.ConstructorCall || kind==Shadow.ConstructorExecution ||
			kind==Shadow.AdviceExecution || 
			// annotations for fieldset/fieldget when an ITD is involved are stored against a METHOD 
			((kind==Shadow.FieldGet || kind==Shadow.FieldSet) && member.getKind()==Member.METHOD)) { 
			
			Type jlrMethod = BcelWorld.makeBcelType(UnresolvedType.forSignature("Ljava.lang.reflect.Method;"));
		
		// Calls getClass
			
		// Variant (1) Use the target directly
		    il.append(fact.createConstant(BcelWorld.makeBcelType(containingType)));
				
		// Variant (2) Ask the target for its class (could give a different answer at runtime)
        // il.append(target.createLoad(fact));
        // il.append(fact.createInvoke("java/lang/Object","getClass",jlClass,new Type[]{},Constants.INVOKEVIRTUAL));
    
	    // il.append(fact.createConstant(new ObjectType(toType.getClassName())));
       
      
		  if (kind==Shadow.MethodCall || kind==Shadow.MethodExecution || kind==Shadow.AdviceExecution || 
			  // annotations for fieldset/fieldget when an ITD is involved are stored against a METHOD 
			  ((kind==Shadow.FieldGet || kind==Shadow.FieldSet) && member.getKind()==Member.METHOD) ||
			  ((kind==Shadow.ConstructorCall || kind==Shadow.ConstructorExecution) && member.getKind()==Member.METHOD)) {
			il.append(fact.createConstant(member.getName()));
		    Type[] paramTypes = null;
			paramTypes = BcelWorld.makeBcelTypes(member.getParameterTypes());
			buildArray(il,fact,jlClass,paramTypes,1);
			// Calls getDeclaredMethod
			il.append(fact.createInvoke("java/lang/Class","getDeclaredMethod",jlrMethod,new Type[]{jlString,jlClassArray},Constants.INVOKEVIRTUAL));
			// FIXME asc perf Cache the result of getDeclaredMethod() and use it 
	        // again for other annotations on the same signature at this join point
	        // Calls getAnnotation
	        String ss = toType.getName();
	        il.append(fact.createConstant(new ObjectType(toType.getName())));		
			il.append(fact.createInvoke("java/lang/reflect/Method","getAnnotation",jlaAnnotation,new Type[]{jlClass},Constants.INVOKEVIRTUAL));
			il.append(Utility.createConversion(fact,jlaAnnotation,BcelWorld.makeBcelType(toType)));
		  } else { // init/preinit/ctor-call/ctor-exec
			Type[] paramTypes = BcelWorld.makeBcelTypes(member.getParameterTypes());
			buildArray(il,fact,jlClass,paramTypes,1);
			Type jlrCtor = BcelWorld.makeBcelType(UnresolvedType.forSignature("Ljava.lang.reflect.Constructor;"));
			il.append(fact.createInvoke("java/lang/Class","getDeclaredConstructor",jlrCtor,new Type[]{jlClassArray},Constants.INVOKEVIRTUAL));
		
			// !!! OPTIMIZATION: Cache the result of getDeclaredMethod() and use it 
			// again for other annotations on the same signature at this join point
			// Calls getAnnotation
			String ss = toType.getName();
			il.append(fact.createConstant(new ObjectType(toType.getName())));		
			il.append(fact.createInvoke("java/lang/reflect/Constructor","getAnnotation",jlaAnnotation,new Type[]{jlClass},Constants.INVOKEVIRTUAL));
			il.append(Utility.createConversion(fact,jlaAnnotation,BcelWorld.makeBcelType(toType)));
		  }
		} else if (kind == Shadow.FieldSet || kind == Shadow.FieldGet) {
			Type jlrField = BcelWorld.makeBcelType(UnresolvedType.forSignature("Ljava.lang.reflect.Field;"));
			il.append(fact.createConstant(BcelWorld.makeBcelType(containingType))); // Stick the target on the stack
			il.append(fact.createConstant(member.getName())); // Stick what we are after on the stack
			il.append(fact.createInvoke("java/lang/Class","getDeclaredField",jlrField,new Type[]{jlString},Constants.INVOKEVIRTUAL));
			String ss = toType.getName();
	        il.append(fact.createConstant(new ObjectType(toType.getName())));		
			il.append(fact.createInvoke("java/lang/reflect/Field","getAnnotation",jlaAnnotation,new Type[]{jlClass},Constants.INVOKEVIRTUAL));
			il.append(Utility.createConversion(fact,jlaAnnotation,BcelWorld.makeBcelType(toType)));
		} else if (kind == Shadow.StaticInitialization || kind==Shadow.ExceptionHandler) {
			il.append(fact.createConstant(BcelWorld.makeBcelType(containingType)));
			String ss = toType.getName();
	        il.append(fact.createConstant(new ObjectType(toType.getName())));		
			il.append(fact.createInvoke("java/lang/Class","getAnnotation",jlaAnnotation,new Type[]{jlClass},Constants.INVOKEVIRTUAL));
			il.append(Utility.createConversion(fact,jlaAnnotation,BcelWorld.makeBcelType(toType)));
		} else {
			throw new RuntimeException("Don't understand this kind "+kind);
		}
		return il;
		
	}
	

	private void buildArray(InstructionList il, InstructionFactory fact, Type arrayElementType, Type[] arrayEntries,int dim) {
        il.append(fact.createConstant(new Integer(arrayEntries==null?0:arrayEntries.length)));
        il.append(fact.createNewArray(arrayElementType,(short)dim));
        if (arrayEntries == null) return;
        for (int i = 0; i < arrayEntries.length; i++) {
			il.append(InstructionFactory.createDup(1));
			il.append(fact.createConstant(new Integer(i)));
        	switch (arrayEntries[i].getType()) {
        	  case Constants.T_ARRAY:
        	    il.append(fact.createConstant(new ObjectType(arrayEntries[i].getSignature())));
        	    break;
        	  case Constants.T_BOOLEAN: il.append(fact.createGetStatic("java/lang/Boolean","TYPE",arrayElementType)); break;
        	  case Constants.T_BYTE:il.append(fact.createGetStatic("java/lang/Byte","TYPE",arrayElementType)); break;
        	  case Constants.T_CHAR:il.append(fact.createGetStatic("java/lang/Character","TYPE",arrayElementType)); break;
        	  case Constants.T_INT:il.append(fact.createGetStatic("java/lang/Integer","TYPE",arrayElementType)); break;
        	  case Constants.T_LONG:il.append(fact.createGetStatic("java/lang/Long","TYPE",arrayElementType)); break;
        	  case Constants.T_DOUBLE:il.append(fact.createGetStatic("java/lang/Double","TYPE",arrayElementType)); break;
        	  case Constants.T_FLOAT:il.append(fact.createGetStatic("java/lang/Float","TYPE",arrayElementType)); break;
        	  case Constants.T_SHORT:il.append(fact.createGetStatic("java/lang/Short","TYPE",arrayElementType)); break;
        	  default:
        	    il.append(fact.createConstant(arrayEntries[i]));
        	}
			il.append(InstructionConstants.AASTORE);
		}
	}

	public void appendLoadAndConvert(
		InstructionList il,
		InstructionFactory fact,
		ResolvedType toType) {
		il.append(createLoadInstructions(toType, fact));				

	}

	public void insertLoad(InstructionList il, InstructionFactory fact) {
		il.insert(createLoadInstructions(getType(), fact));
	}

}
