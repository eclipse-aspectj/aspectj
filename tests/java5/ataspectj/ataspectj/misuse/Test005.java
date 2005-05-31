// "@Aspect class extending @Aspect class"
package ataspectj.misuse;

import org.aspectj.lang.annotation.*;

public abstract class Test005 {

    @Aspect
    public static class Test005B extends Test005 {
    }


}
