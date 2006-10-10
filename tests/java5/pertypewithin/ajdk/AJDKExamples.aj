package org.xyz.foo;
import java.util.*;
public aspect AJDKExamples pertypewithin(org.xyz..* && !AJDKExamples) {
		     
   public AJDKExamples() {
	   System.out.println("Aspect instance constructed");
   }
   // use WeakHashMap for auto-garbage collection of keys  	 
   private Map<Object,Boolean> instances = new WeakHashMap<Object,Boolean>();

   after(Object o) returning() : execution(new(..)) && this(o) {
     instances.put(o,true);
   }

   public Set<?> getInstances() {
     return instances.keySet();
   }
	       
	
	public static void main(String[] args) {
		A a = new A();
		A a2 = new A();
		B b = new B();
		B b2 = new B();
		B b3 = new B();
		
		System.out.println(AJDKExamples.hasAspect(A.class));
		System.out.println(AJDKExamples.hasAspect(B.class));
		Set<?> as = AJDKExamples.aspectOf(A.class).getInstances();
		Set<?> bs = AJDKExamples.aspectOf(B.class).getInstances();
		System.out.println("There are " + as.size() + " As");
		System.out.println("There are " + bs.size() + " Bs");
	}
}

class A {}

class B {}