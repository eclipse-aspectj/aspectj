import org.aspectj.testing.Tester;
import java.util.*;

public class AspectInheritance {
    public static void main(String[] args) {
	new C().m(2);

	Tester.checkEqual(Base.aspectNames, "FullConcrete ConcreteFlow", "names");
    }
}


abstract aspect Base {
    public static Set aspectNames = new HashSet();

    abstract pointcut targets(int i, C c);

    after(int i, C c): targets(i, c) {
	Base.aspectNames.add(this.getClass().getName());
    }
}

aspect EmptyConcrete extends Base {
    pointcut targets(int i, C c);
}

aspect FullConcrete extends Base {
    pointcut targets(int i, C c): target(c) && call(void C.m(int)) && args(i);
}

abstract aspect Flow percflow(entries()) {
    abstract pointcut entries();

    {
	Base.aspectNames.add(this.getClass().getName());
    }
}

abstract aspect MainFlow extends Flow {
    pointcut entries(): execution(void C.m(..));
}

aspect ConcreteFlow extends MainFlow { }



abstract aspect AbstractWorld {
    before(): execution(void C.m(..)) {
	System.out.println("before C.m");
    }
}



class C {
    public void m(int i) { }
}
