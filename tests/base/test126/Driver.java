public class Driver {
    public static void main(String[] args) { test(); }

    public static void test() {

        // local variable declaration in the init part of a for
        for (int i = 0, j = 0; j < 10 ; i++, j++) {
            j++;
        }
        int m, n, j = 0;

        // init part without local var declaration
        for (m  = 0, n = 0; j < 10 ; m++, n++) {
            j++;
            m++;
        }
    }
}
