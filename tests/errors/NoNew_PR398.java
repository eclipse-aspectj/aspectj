import org.aspectj.testing.Tester;
/**
 * PR#398
 */
class NoNew {
    
    public static void main(String[] args) {
        try {
            new NoNew().thrower();
        } catch (Exception e) {
        }
    }
    void thrower() throws Exception {
        throw /*new*/ Exception("exception"); // missing new
    }
}

aspect AspectTest of eachJVM() {
    
    pointcut pc3(): executions(void thrower());
    before(): pc3() {
    }
}
