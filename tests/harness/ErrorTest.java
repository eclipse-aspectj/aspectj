

public class ErrorTest {
    static String i() { return "0"; }
    int i = i();            // CE 5 always
    int j = i();            // CE 6 always
}


