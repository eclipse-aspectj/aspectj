aspect A {
  Object around(): call(* clone(..)) {
    return proceed();
  }
}

