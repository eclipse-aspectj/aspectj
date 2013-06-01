package a.b.c;

public class A2 {
  class B {
  }
  class $C {
    class Inner {}
  }
}
class A$$B$$C {
}

aspect X {
  before(): within(*$C+) && staticinitialization(*) {
 
  }
}
