public class PR83563_1 {
    public static void main(String[] args) {
        new NestedTest().run();
        int c = PertypewithinTest.aspectOf(PR83563_1.class).cnt;
        if (c!=2)
          throw new RuntimeException("Expected 2 advice executions: "+c);
    }
	
    static class NestedTest implements Runnable {
        public void run() {
            System.out.println("Running...");
        }
    }
}
 
aspect PertypewithinTest pertypewithin(PR83563_1) {
    public static int cnt = 0;

    before() : execution(* *.*(..)) {
        cnt++;
        System.out.println(thisJoinPointStaticPart);
    }	
}
