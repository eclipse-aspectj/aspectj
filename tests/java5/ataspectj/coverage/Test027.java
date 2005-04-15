// "@DeclareParents on an @Aspect with @DeclarePrecidence"

import org.aspectj.lang.annotation.*;

class A{
}
interface D{
}
aspect B{
  @DeclareParents("A")
  @Aspect
  @DeclarePrecidence("")
  class C implements D{
  }
}
