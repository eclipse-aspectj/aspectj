//allow one argument calls even when there's a comma in the arglist (PR#384)

public class OneArgCallsIsOkay {
    public static void main(String[] args) {}
    static void foo(int x, int y) {}
    pointcut cut(): call(void OneArgCallsIsOkay.foo(int, int));
}
