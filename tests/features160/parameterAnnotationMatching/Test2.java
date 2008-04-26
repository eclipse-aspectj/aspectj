 import java.lang.annotation.*;
 import java.lang.annotation.Target;

 public aspect Test2 {

     declare warning : execution(* *(@A (!(Object+)), ..)) : "mOne"; // f1
     declare warning : execution(* *(@A !String, ..)) : "mTwo"; // f3/f4
     
     void f1(@A int i) {} // 9 

     void f2(int i) {} // 11

     void f3(@A P i) {}

     void f4(P i) {}

     void f5(Integer i) {}
     
     void f6(@A Integer i) {}

     @Retention(RetentionPolicy.RUNTIME)
     private static @interface A { }

     @A static class P {}
 }