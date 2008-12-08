package patterntesting.check.runtime;


public class NotNullTest {
    
    private String s2;
    private static final String nullString = null;
    
    public NotNullTest() {
    }
    
    public NotNullTest(@NotNull String s) {
        s2 = s;
    }
    
    public static void main(String []argv) {
        new NotNullTest("something");
        new NotNullTest(nullString);
    }

}
