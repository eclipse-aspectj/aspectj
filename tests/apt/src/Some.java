package test;

public class Some {

    public static void main(String[] args) {
        Some some = new Some();
        some.OnMethod1Event.add((emmiter) -> {
            System.out.println("callback registered from before aspect");
        });
        some.method1();
    }

    @Event(Event.Order.Before)
    public void method1() {
        System.out.println("method1 is invoked");
    }
}
