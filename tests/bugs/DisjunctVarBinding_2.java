class A {
	void m() {
		System.out.println("A");
	}
 }

class B extends A {
		void m() {
		System.out.println("B");
	}
 }



public class DisjunctVarBinding_2 { 

	public static void foo(A a, A b) {
	    a.m();
		b.m();
	}

	public static void main(String[] args) {
		A a = new A();
		B b = new B(); 
		foo(b,a); 
	} 
 
}

/* Example to illustrate a problem with variable
   binding and (||) in pointcuts. When run, this program produces
   java.lang.ClassCastException immediately after
   the call to "foo". 

   The reason is that the instance tests inherent
   in the pointcut are done separately from the
   variable binding.

   Decompiled, the code produced for the relevant call
   to "foo" is as follows:
   
   -------------------------------------------------- 
   DisjunctVarBinding.foo(r5, r4);
   label_0:
        {
            if (r5 instanceof B == false)
            {
                if (r4 instanceof B == false)
                {
                    break label_0;
                }
            }

            IfPointcut.aspectOf().ajc$afterReturning$IfPointcut$26d(r5, (B) r4);
        }
   --------------------------------------------------
   It should however read something like this, using the instance
   tests to determine the appropriate variable binding

   --------------------------------------------------
   DisjunctVarBinding.foo(r5,r4);
   if (r4 instanceof B)
      IfPointcut.aspectOf().ajc$afterReturning$IfPointcut$26d(r5, (B)r4)
   else if (r5 instanceof A)
           IfPointcut.aspectOf().ajc$afterReturning$IfPointcut$26d(r4,(B)r5)
   --------------------------------------------------
*/