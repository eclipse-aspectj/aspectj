import org.aspectj.testing.*;
import ClassWithStaticInnerInterfaces.Inner1;
import ClassWithStaticInnerInterfaces.Inner1.Inner2;
import ClassWithStaticInnerInterfaces.Inner1.Inner2.*;
public class ImportingStaticInnerInterfacesStars_PR386 {
    public static void main(String[] args) {
        Tester.checkEqual(ClassWithStaticInnerInterfaces.class.getName(),
                           "ClassWithStaticInnerInterfaces");
        Tester.checkEqual(ClassWithStaticInnerInterfaces.Inner1.class.getName(),
                           "ClassWithStaticInnerInterfaces$Inner1");
        Tester.checkEqual(ClassWithStaticInnerInterfaces.Inner1.Inner2.class.getName(),
                           "ClassWithStaticInnerInterfaces$Inner1$Inner2");
        Tester.checkEqual(ClassWithStaticInnerInterfaces.Inner1.Inner2.Inner3.class.getName(),
                           "ClassWithStaticInnerInterfaces$Inner1$Inner2$Inner3");
        Tester.checkEqual(I1.class.getName(), "I1");
        Tester.checkEqual(I2.class.getName(), "I2");
        Tester.checkEqual(I3.class.getName(), "I3");
    }
}

class I1 implements Inner1 {}
class I2 implements Inner2 {}
class I3 implements Inner3 {}
