aspect Aspect pertarget(target(Foo)) {

  public String toString() {return "The Aspect";}

  before (Foo f): target(f) && call(* foo(..)) {
      // do some incrementing and reading of f's variables:
      // f.PRIVATECONST;          
      // f.privateClassVar++;      
      // f.privateInstanceVar++;
      f.protectedClassVar++;     
      f.protectedInstanceVar++;  
      f.publicClassVar++;        
      f.publicInstanceVar++;     
      f.ClassVar++;              
      f.InstanceVar++;           

      // do some calling of f's methods

      //f.privateClassMethod();
      //f.privateInstanceMethod();
      f.protectedClassMethod();
      f.protectedInstanceMethod();
      f.publicClassMethod();
      f.publicInstanceMethod();
      f.ClassMethod();
      f.InstanceMethod();
  }
}
