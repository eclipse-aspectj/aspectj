import org.aspectj.testing.*;
import ClassWithInnerInterfaces.*;

public class ImportingInnerInterfacesStars3_PR386 {
    public static void main(String[] args) {
        Tester.checkEqual(ClassWithInnerInterfaces.class.getName(),
                           "ClassWithInnerInterfaces");
        Tester.checkEqual(ClassWithInnerInterfaces.Inner1.class.getName(),
                           "ClassWithInnerInterfaces$Inner1");
        Tester.checkEqual(I1.class.getName(), "I1");
    }
}

class I1 implements Inner1 {}
