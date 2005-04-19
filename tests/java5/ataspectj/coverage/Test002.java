//"@Aspect with codestyle pointcut"

// 1. test name needs changing to @Aspect with codestyle advice declaration
// Probably nothing can be done here because it is at parse time that we
// decide this is a class so can't contain advice - at parse time we probably
// can't go digging round for annotations on the type decl.  
// Documented limitation?

import org.aspectj.lang.annotation.*;

@Aspect
class A{
  before(): call(* *(..)) {
  }
}
