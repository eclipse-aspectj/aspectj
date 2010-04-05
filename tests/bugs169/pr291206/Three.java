import java.lang.annotation.*;

aspect X {
  declare error: I+ && !hasmethod(* foo(..)): "Missing foo() method in I subtype";
}

interface I {}

class C implements I {
  void foo() {}  
}

class D implements I {
  
}
