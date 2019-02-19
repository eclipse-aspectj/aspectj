package ma2;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.DeclarePrecedence;

@Aspect
@DeclarePrecedence("ma2.Aspect1, "
        + "ma2.Aspect2, "
        + "ma2.Aspect3")
class Precedence {

}
