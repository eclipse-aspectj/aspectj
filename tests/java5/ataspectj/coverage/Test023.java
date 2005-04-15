// "@DeclareParents with interface extending interface"

import org.aspectj.lang.annotation.*;

class A{
}
interface D{
}
aspect B{
  @DeclareParents("A")
  interface C extends D{
  }
}
