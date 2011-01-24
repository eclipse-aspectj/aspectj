package ma2;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.DeclarePrecedence;

@Aspect
@DeclarePrecedence("ma2.aspect1.Aspect1, ma2.aspect3.Aspect3")
class Precedence {

}
