public class PR353e {

   public static void main(String[] args){
     new PR353e().go();
   }
   
   void go(){
        C c;
//        System.out.println("\nwith C...");
//        c = new C();
//        c.foo(this);
//        c.bar(this);
//        System.out.println("\nwith D...");
//        c = new D();
//        c.foo(this);
//        c.bar(this);
//        System.out.println("\nwith CE...");
//        c = new E();
//        c.foo(this);
      System.out.println("\nwith E...");
      E e = new E();
      e.foo(this);
      System.out.println("\nwith E2...");
      E2 e2 = new E2();
      e2.foo(this);
      e2.bar(this);
   }
}

interface I { }
class C{
   void foo(PR353e a){ System.out.println("foo"); }
}
class E extends C implements I { } 
class E2 extends C implements I { void foo(PR353e a) { System.out.println("foo2"); } }

aspect A {
 
    pointcut p(C c): receptions(* *(PR353e)) && instanceof(c) && !instanceof(E);
    static before(C c): p(c) {
        System.out.println("1 before A " + thisJoinPoint.methodName + " with:" + c + ":" + thisJoinPoint.className);
    }
    
    pointcut p3(): receptions(* *(PR353e)) && !instanceof(E);
    static before(): p3() {
        System.out.println("3 before A " + thisJoinPoint.methodName + " with:" + thisJoinPoint.className);
    }  
    pointcut p4(): receptions(* *(PR353e)) && !instanceof(E2);  
    static before(): p4() {
        System.out.println("4 before A " + thisJoinPoint.methodName + " with:" + thisJoinPoint.className);
    }                      
}

aspect B {
 
    pointcut p(C c): receptions(* *(PR353e)) && instanceof(c) && !instanceof(E);
    static before(C c): p(c) {
        System.out.println("1 before B " + thisJoinPoint.methodName + " with:" + c + ":" + thisJoinPoint.className);
    }
    
    pointcut p3(): receptions(* *(PR353e)) && !instanceof(E);
    static before(): p3() {
        System.out.println("3 before B " + thisJoinPoint.methodName + " with:" + thisJoinPoint.className);
    }  
}
