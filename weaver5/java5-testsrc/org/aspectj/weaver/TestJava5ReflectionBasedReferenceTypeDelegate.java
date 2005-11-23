package org.aspectj.weaver;

import org.aspectj.weaver.reflect.ReflectionBasedReferenceTypeDelegateTest;

public class TestJava5ReflectionBasedReferenceTypeDelegate extends ReflectionBasedReferenceTypeDelegateTest {
	
	public void testResolveGeneric() {
		UnresolvedType collectionType = UnresolvedType.forName("java.util.Collection<E>");
//		ResolvedType rt= world.resolve(collectionType);
//		ResolvedMember[] methods = world.resolve(collectionType).getDeclaredMethods();
//		assertTrue(findMethod("toArray", methods) != -1);		
	}
	
	public void testResolveClass() {
		//stack overflow
		world.resolve("java.lang.Class");		
	}
	
	// just here to override the super one so it doesnt run in this case ;)
	public void testGetDeclaredMethods() {
		
	}


}
