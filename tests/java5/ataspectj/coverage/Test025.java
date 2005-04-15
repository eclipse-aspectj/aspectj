// "@DeclareParents used outside of an Aspect"

import org.aspectj.lang.annotation.*;

class A{
}
interface D{
}
@DeclareParents("A")
class C implements D{
}
