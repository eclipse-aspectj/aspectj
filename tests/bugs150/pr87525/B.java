privileged aspect B {

  before(A anA):execution(* a()) && this(anA){   
      switch(1){
        case anA.c: // "case expressions must be constant expressions"
      }
    }

}
