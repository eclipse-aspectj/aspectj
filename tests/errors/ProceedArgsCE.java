public class ProceedArgsCE {
     public static void main(String[] args){
         new ProceedArgsCE().go();
     }

     void go() {
         System.out.println("... ");
     }
}

aspect A {
    void around(Object o): this(o) {
        proceed();  // CE
    }
    void around(Object o): this(o) {
        proceed(2);  // CE
    }
    void around(Object o): this(o) {
        proceed("hi", 2);  //CE
    }
}
