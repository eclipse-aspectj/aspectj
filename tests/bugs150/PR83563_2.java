public class PR83563_2 {
    public void bar() {
        new Runnable() {
            public void run() {
                System.out.println("Running...");
            }
        }.run();
    }
        
    public static void main(String[] args) {
        new PR83563_2().bar();
        int c = PertypewithinTest.aspectOf(PR83563_2.class).cnt;
        if (c!=3)
          throw new RuntimeException("Expected 3 advice executions but got:"+c);
    }
}
 
aspect PertypewithinTest pertypewithin(PR83563_2) {
    public static int cnt = 0;

    before() : execution(* *.*(..)) {
        cnt++;
        System.out.println(thisJoinPoint);
    }    
}
