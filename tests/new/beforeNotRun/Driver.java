import org.aspectj.testing.Tester;

// PR#265 before advice not run when abstract aspect has "of" clause

public class Driver {
  
    public static String result = "";
    
    public static void main(String[] args) { test(); }
    
    public static void test() {
        new Driver();  
        Tester.checkEqual(result, "-before-init-after", "before and after advice run");
    }

    public Driver() {
        init();
    }
    
    public void init() {
        result += "-init";        
    }
}

// when "of" clause is removed test passes
abstract aspect AbstractAspect /*of eachobject(instanceof(Driver))*/ {
    pointcut init(Driver tc): call(* init(..)) && this(tc);

    after(Driver tc): init(tc) {
        Driver.result += "-after";
    }
}

aspect BeforeAspectA extends AbstractAspect {
    before(Driver tc): init(tc) {
        Driver.result += "-before";
    }
}
