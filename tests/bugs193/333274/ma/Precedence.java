package ma;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.DeclarePrecedence;

@Aspect
@DeclarePrecedence("ma.aspect1.Aspect1, "
        + "ma.aspect2.Aspect2, "
        + "ma.aspect3.Aspect3")
class Precedence {

}
