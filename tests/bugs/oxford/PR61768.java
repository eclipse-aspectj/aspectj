/* According to the documentation, the scope rules for
intertype method declarations are interpreted
from the originating aspect. The only exceptions
(I thought) are the use of "this" and "super" which refer to
the target type.

According to that interpretation, the program below is type correct, but
ajc generates two error messages, shown in comments
at the relevant lines. Note that it's ok to access private static
fields of the aspect, but not to use private classes of the aspect.

If this is a feature and not a bug, what are the
precise scope rules for intertype method declarations?
*/

aspect Aspect { 

 private static int y = 5;

 private class A { int x = 3; }
 
 private static class A2 {}

 private static void B.happy() {
System.out.println(y);          // happy accessing private field y
 }

 private static void B.foo(A z) { 
System.out.println(z.x); 
 }
 
 private static void B.foo2(A2 z) {
     System.out.println(z);
 }

 public void B.bar() {
     B.foo(new A());        // CE L37 : no enclosing instance
 }
 
 public void B.bar2() {
     B.foo2(new A2());
 }

}

class B {
}

class IT { 


 public static void main(String[] args) { 
new B().bar();
 } 

}