import org.aspectj.testing.*;

public class ClassExtendingAbstractAspectCE {
    public static void main(String[] args) {
        new ClassExtendingAbstractAspectCE().go(args);
    }

    void go(String[] args) {
        Extends e = new Extends();
        Tester.check(false, "shouldn't have compiled!");
    }

}

abstract aspect Aspect {

}


class Extends extends Aspect {     // CE 20
    pointcut p(): call(* *());
}

