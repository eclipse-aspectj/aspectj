package a.b.c;

public class A {
  class B {
  }
  class $C {
  }
}
class A$$B$$C {
}

aspect X {
  before(): within(A+) && staticinitialization(*) {
 
  }
}
