import org.aspectj.testing.*;
import ClassWithStaticInnerInterfaces.*;

public class ImportingStaticInnerInterfacesStars3_PR386 {
    public static void main(String[] args) {
        Tester.checkEqual(ClassWithStaticInnerInterfaces.class.getName(),
                           "ClassWithStaticInnerInterfaces");
        Tester.checkEqual(ClassWithStaticInnerInterfaces.Inner1.class.getName(),
                           "ClassWithStaticInnerInterfaces$Inner1");
        Tester.checkEqual(I1.class.getName(), "I1");
    }
}

class I1 implements Inner1 {}
