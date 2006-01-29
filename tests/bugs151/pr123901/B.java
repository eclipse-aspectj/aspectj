package a;
public aspect B {
   void around():call(void *(..)){ 
      A a = new A();
      a.A.a.x(); // This line raises the NPE    
      proceed();    
   }
}
