
package main;

/** @testcase PUREJAVA package class access not enforced outside of package */
public class Main {
    public static void main(String[] args) {
        pack1.Target.publicStaticMethod(); // expecting CE here - Target not public
    }
}
