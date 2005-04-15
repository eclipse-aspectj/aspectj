// "@DeclareParents on an @Aspect"

import org.aspectj.lang.annotation.*;

class A{
}
interface D{
}
aspect B{
  @Aspect
  @DeclareParents("A")
  class C implements D{
  }
}
