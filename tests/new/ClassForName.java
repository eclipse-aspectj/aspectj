
import org.aspectj.testing.Tester;
import java.util.*;

public class ClassForName {
    public static void main(String[] args) throws ClassNotFoundException {
    	Class c1 = String[].class;
    	Class c2 = Class.forName("[Ljava.lang.String;");
    	Class c3 = ClassForName.class.getClassLoader().loadClass("[Ljava.lang.String;");
    	
		Tester.checkEqual(c1, c2, "classes c1, c2");
		Tester.checkEqual(c2, c3, "classes c2, c3");
    	
    	Tester.checkEqual(c1.getComponentType(), String.class, "component");
    }
}

aspect A {
	before(): execution(void main(..)) {
		System.out.println(thisJoinPointStaticPart);
	}
}
