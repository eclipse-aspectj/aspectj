
import org.aspectj.testing.Tester;

// PR#271 instanceof used without a class

public class InstanceofWithoutClass {
  
    public static void main(String[] args){
        new InstanceofWithoutClass().go();
    }

    void go() { }

    pointcut t(): this() && call(void go());
}
