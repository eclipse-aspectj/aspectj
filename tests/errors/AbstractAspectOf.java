import org.aspectj.testing.Tester;

// PR#286 aspect of abstract class

public class AbstractAspectOf { }

abstract aspect AbstractAspect /*of eachobject(instanceof(AbstractError))*/ { }



class C {
    aspect InnerAspect {
    }
}
