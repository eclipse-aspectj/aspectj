
import org.aspectj.testing.Tester;
import java.util.*;

/** @testcase PR#829 declare array field using postfix */
public class ArrayFieldDeclaration {

    public static void main(String[] args) {
        //Tester.check(null != new C().f[0], "null != new C().f[0]");
    }
}

class C { 
}

aspect A {
    Object C.f[] = new Object[] { "hello" };  //CE postfix [] syntax is illegal in inter-type decs
}
