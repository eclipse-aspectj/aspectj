/**
 * PR#538
 * Would be useful to add a Cp version
 */

public class InnerNameConflictsCf {
    class Inner {}
    public static void main(String[] args) {
        new Inner(); //ERR: no this
    }
}

class Base {
    static class Base {} //ERR: repeated name
}

class Outer {
    class Inner {
        class Inner2 {
            class Outer {} //ERR: repeated name
            class Inner {} //ERR: repeated name
        }
    }
}
