
package main;

public class Main {
    public static void main (String[] args) {
        String s = "" + new Target();
    }
}

class Target {
    void run() {}
}
