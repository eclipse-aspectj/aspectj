/* *******************************************************************
 * Copyright (c) 2005 Contributors.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *   Adrian Colyer			Initial implementation
 * ******************************************************************/
package org.aspectj.weaver;

import org.aspectj.weaver.bcel.BcelWorld;

import junit.framework.TestCase;

/**
 * @author colyer
 * @author clement
 */
public class Member15Test extends TestCase {

	  public void testCanBeParameterizedRegularMethod() {
	    	BcelWorld world = new BcelWorld();
	    	ResolvedType javaLangClass = world.resolve(UnresolvedType.forName("java/lang/Class"));
	    	ResolvedMember[] methods = javaLangClass.getDeclaredMethods();
	    	ResolvedMember getAnnotations = null;
		  for (ResolvedMember method : methods) {
			  if (method.getName().equals("getAnnotations")) {
				  getAnnotations = method;
				  break;
			  }
		  }
	    	if (getAnnotations != null) { // so can run on non-Java 5
//	    		System.out.println("got it");
	    		assertFalse(getAnnotations.canBeParameterized());
	    	}
	    }
	    
	    public void testCanBeParameterizedGenericMethod() {
	    	BcelWorld world = new BcelWorld();
	    	world.setBehaveInJava5Way(true);
	    	ResolvedType javaLangClass = world.resolve(UnresolvedType.forName("java.lang.Class"));
	    	javaLangClass = javaLangClass.getGenericType();
	    	if (javaLangClass == null) return;  // for < 1.5
	    	ResolvedMember[] methods = javaLangClass.getDeclaredMethods();
	    	ResolvedMember asSubclass = null;
			for (ResolvedMember method : methods) {
				if (method.getName().equals("asSubclass")) {
					asSubclass = method;
					break;
				}
			}
	    	if (asSubclass != null) { // so can run on non-Java 5
//	    		System.out.println("got it");
	    		assertTrue(asSubclass.canBeParameterized());
	    	}    	
	    }
	    
	    public void testCanBeParameterizedMethodInGenericType() {
	       	BcelWorld world = new BcelWorld();
	       	world.setBehaveInJava5Way(true);
	    	ResolvedType javaUtilList = world.resolve(UnresolvedType.forName("java.util.List"));
	    	javaUtilList = javaUtilList.getGenericType();
	    	if (javaUtilList == null) return;  // for < 1.5
	    	ResolvedMember[] methods = javaUtilList.getDeclaredMethods();
	    	ResolvedMember add = null;
			for (ResolvedMember method : methods) {
				if (method.getName().equals("add")) {
					add = method;
					break;
				}
			}
	    	if (add != null) { // so can run on non-Java 5
//	    		System.out.println("got it");
	    		assertTrue(add.canBeParameterized());
	    	}    	    	
	    }
	    /*
	    public void testGenericReferenceTypeCreation() {
	    	UnresolvedType genericType = UnresolvedType.forGenericTypeSignature("Lorg/aspectj/weaver/MemberTestCase15$One;","<T:Ljava/lang/Object;>Ljava/lang/Object;");
	    	assertEquals("Porg/aspectj/weaver/MemberTestCase15$One<TT;>;",genericType.getSignature());
	    	assertEquals("Lorg/aspectj/weaver/MemberTestCase15$One;",genericType.getErasureSignature());
	    }
	    
	    public void testMemberSignatureCreation() {
			World world = new BcelWorld("../weaver5/bin/");
	    	//new ReflectionWorld(false, getClass().getClassLoader());
	       	world.setBehaveInJava5Way(true);
	       	ResolvedType one = world.resolve("org.aspectj.weaver.MemberTestCase15$One<java.lang.String>");
	       	assertNotNull(one);
	       	assertFalse(one.isMissing());
	       	
	       	// Look at the methods on the parameterized type One<String>
	       	ResolvedMember member = findMethod("getter",one);      	
	       	String erasedSignature = MemberImpl.typesToSignature(member.getReturnType(),member.getParameterTypes(),true);
	       	assertEquals("()Ljava/lang/String;",erasedSignature);
	       	String nonErasedSignature = MemberImpl.typesToSignature(member.getReturnType(),member.getParameterTypes(),false);
	       	assertEquals("()Ljava/lang/String;",nonErasedSignature);
	       	erasedSignature = MemberImpl.typesToSignature(member.getReturnType(),member.getParameterTypes(),true);
	       	assertEquals("()Ljava/lang/String;",erasedSignature);
	       	nonErasedSignature = MemberImpl.typesToSignature(member.getReturnType(),member.getParameterTypes(),false);
	       	assertEquals("()Ljava/lang/String;",nonErasedSignature);

	       	member = findMethod("getterTwo",one);
	       	erasedSignature = MemberImpl.typesToSignature(member.getReturnType(),member.getParameterTypes(),true);
	       	assertEquals("()Ljava/util/List;",erasedSignature);
	       	nonErasedSignature = MemberImpl.typesToSignature(member.getReturnType(),member.getParameterTypes(),false);
	       	assertEquals("()Pjava/util/List<Ljava/lang/String;>;",nonErasedSignature);
	       	nonErasedSignature = MemberImpl.typesToSignature(member.getGenericReturnType(),member.getGenericParameterTypes(),true);
	       	assertEquals("()Ljava/util/List;",nonErasedSignature);
	       	nonErasedSignature = MemberImpl.typesToSignature(member.getGenericReturnType(),member.getGenericParameterTypes(),false);
	       	assertEquals("()Pjava/util/List<Ljava/lang/String;>;",nonErasedSignature);
	       	
	       	// Grab the generic type backing the parameterized type
	       	ResolvedType oneGeneric = one.getGenericType();
	       	assertTrue(oneGeneric.isGenericType());
	       	member = findMethod("getterTwo",oneGeneric);
	       	erasedSignature = MemberImpl.typesToSignature(member.getReturnType(),member.getParameterTypes(),true);
	       	assertEquals("()Ljava/util/List;",erasedSignature);
	       	erasedSignature = MemberImpl.typesToSignature(member.getReturnType(),member.getParameterTypes(),false);
	       	assertEquals("()Ljava/util/List;",erasedSignature);
	       	nonErasedSignature = MemberImpl.typesToSignature(member.getReturnType(),member.getParameterTypes(),false);
	       	assertEquals("()Pjava/util/List<TT;>;",nonErasedSignature);
	       	nonErasedSignature = MemberImpl.typesToSignature(member.getGenericReturnType(),member.getGenericParameterTypes(),false);
	       	assertEquals("()Ljava/util/List;",nonErasedSignature);
	       	
	       	
	       	ResolvedType oneRaw = oneGeneric.getRawType();
	       	member = findMethod("getterTwo",oneRaw);
	    }
	    
	    private ResolvedMember findMethod(String name, ResolvedType type) {
	       	ResolvedMember[] members = type.getDeclaredMethods();
	       	for (ResolvedMember member: members) {
	       		if (member.getName().equals(name)) {
	       			return member;
	       		}
	       	}
	       	return null;
	    }
	    
	    // testcode
	    class One<T> {
	    	T t;
	    	T getter() {
	    		return null;
	    	}
	    	List<T> getterTwo() {
	    		return null;
	    	}
	    }
	    */
}
