import org.aspectj.testing.Tester;
public class PR405 {
    public static void main(String[] args) {
        new PR405().realMain(args);
    }
    public void realMain(String[] args) {
        Tester.check(false, "Shouldn't have compiled");
    }
    
    public PR405() {
    }
}

class C {
  int foo(){ return 1 }
}
