import org.aspectj.testing.Tester;

introduction (String) {}

public class BadIntroduction {        
    public static void main(String[] args) {        
        String s;
        Tester.check(false, "the compiler should have given an error");
    }
}


