//package debugger;

public class AJDBThreads {
    public static void main(String[] args) {
        new ThreadForker().go();
    }
    public static String currentThread = "none";
}

class ThreadForker {
    public void go() {
        fork(1000);
        fork(500);
        fork(200);
        fork(100);
    }

    void fork(long sleep) {
        new NamedThread(sleep).start();
    }
}

class NamedThread implements Runnable {
    private long sleep;
    private String name;
    private Thread thread;
    private int num = 0;

    public NamedThread(long sleep) {
        this.sleep = sleep;
    }

    public void start() {
        if (thread == null) {
            thread = new Thread(this);
            name = thread.getName();
            thread.start();
        }
    }
    
    public void run() {
        while (true) {
            AJDBThreads.currentThread = name;
            System.out.println("\n********** " + AJDBThreads.currentThread + ":" + (num++) + "\n");
            try {
                Thread.sleep(sleep);
            } catch (Exception e) {
            }                
        }
    }
}
