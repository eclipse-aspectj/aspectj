import org.aspectj.testing.*;

public class ClassExtendingAspect {
    public static void main(String[] args) {
        new ClassExtendingAspect().go(args);
    }

    void go(String[] args) {
        Extends e = new Extends();
        Tester.check(false, "shouldn't have compiled!");
    }

}

aspect Aspect {

}

class Extends extends Aspect {
    pointcut p(): call(* *());
}

