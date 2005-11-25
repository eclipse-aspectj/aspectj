package org.aspectj.weaver;

import org.aspectj.weaver.reflect.ReflectionBasedReferenceTypeDelegateTest;

public class TestJava5ReflectionBasedReferenceTypeDelegate extends ReflectionBasedReferenceTypeDelegateTest {
	
	/**
	 * Let's play about with a generic type and ensure we can work with it in a reflective world.
	 */
	public void testResolveGeneric() {
		UnresolvedType collectionType = UnresolvedType.forName("java.util.Collection");
		ResolvedType rt= world.resolve(collectionType).getRawType().resolve(world);
		ResolvedMember[] methods = world.resolve(collectionType).getDeclaredMethods();
		int i = findMethod("toArray", methods);
		assertTrue("Couldn't find 'toArray' in the set of methods? "+methods,i != -1);
		String expectedSignature = "T[] java.util.Collection.toArray(T[])";
		assertTrue("Expected signature of '"+expectedSignature+"' but it was '"+methods[i],methods[i].toString().equals(expectedSignature));
	}

	/**
	 * Can we resolve the dreaded Enum type...
	 */
	public void testResolveEnum() {
		ResolvedType enumType = world.resolve("java.lang.Enum");
		assertTrue("Should be the raw type but is "+enumType.typeKind,enumType.isRawType());
		ResolvedType theGenericEnumType = enumType.getGenericType();
		assertTrue("Should have a type variable ",theGenericEnumType.getTypeVariables().length>0);
		TypeVariable tv = theGenericEnumType.getTypeVariables()[0];
		String expected = "TypeVar E extends java.lang.Enum<E>";
		assertTrue("Type variable should be '"+expected+"' but is '"+tv+"'",tv.toString().equals(expected));
	}
	
	public void testResolveClass() {
		world.resolve("java.lang.Class").getGenericType();		
	}
	
	
}
