import org.aspectj.testing.Tester;

public class MultiArrays {
    static int[][] data = {{11,12,13}, {21,22,23}};

    public static void test() {
        Tester.checkEqual(data[0][0], 11, "0,0");
        Tester.checkEqual(data[1][2], 23, "1,2");
    }

    public static void main(String[] args) { test(); }
}
