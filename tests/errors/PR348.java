public class Mark {
    public static void main(String[] args) {
        new Mark().realMain(args);
    }
    public void realMain(String[] args) {
        new Bug().go(null);
        org.aspectj.testing.Tester.check(false, "shouldn't have compiled");
    }
}

class Bug {
    void go(String s){}
}
aspect A {
    pointcut p1(String s, int y): calls (* *.go(s)) && within(*);
    before(String y2, int y): p1(y2, int) {}
}
