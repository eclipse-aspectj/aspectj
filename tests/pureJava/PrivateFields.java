public class PrivateFields {
    public static void main(String[] args) {
    }
}

class Outer {
    private static int one = 1;

    static class Inner extends Outer{
        private static int two = 2;
        int m() { return one; }
    }
}
