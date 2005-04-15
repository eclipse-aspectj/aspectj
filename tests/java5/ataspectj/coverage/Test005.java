// "@Aspect class extending @Aspect class"

import org.aspectj.lang.annotation.*;

@Aspect
class A{
}
@Aspect
class B extends A{
}
