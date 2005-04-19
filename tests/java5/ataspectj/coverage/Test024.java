// "@DeclareParents implementing more than one interface"

import org.aspectj.lang.annotation.*;

class A{
}
interface D{
}
interface E{
}
Aspect B{
  @DeclareParents("A")
  class C implements D, E{
  }
}
