package typeNameConflicts;

public class Driver {
    static int x;

    public Entry getEntry() {
        return new Integer();
    }

    public static void main(String[] args) {
        x = 2;
        Runnable r = new Runnable() { public void run() { System.out.println("running"); } };
        r.run();

        //java.lang.Integer i = new java.lang.Integer(2);
    }

    abstract class Entry {
    }

    class Integer extends Entry {
        int value;

        public void m() {
            value = 3;
            //java.lang.Integer i = new java.lang.Integer(2);
        }
    }
}

