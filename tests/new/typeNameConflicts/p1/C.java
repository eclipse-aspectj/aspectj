package typeNameConflicts.p1;

public class C {
    static class Inner {
        public Runnable makeRunnable() {
            return new Runnable() {
                    public void run() { System.out.println("running"); }
                };
        }
    }
}
