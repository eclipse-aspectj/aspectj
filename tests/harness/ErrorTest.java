

public class ErrorTest {
    static Integer i() { return Integer.valueOf("0"); }
    int i = i();            // CE 5 always
    int j = i();            // CE 6 always
}


