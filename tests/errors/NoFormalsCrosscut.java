import org.aspectj.testing.Tester;

aspect NoFormalsCrosscut {
    public void foo() {}

    pointcut xcut1(): * *(..);
    pointcut xcut2(): NoFormalsCrosscut;
    before(): xcut1 && xcut2() {
            System.out.println("before");
    }
}
 
