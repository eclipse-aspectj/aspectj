

import org.aspectj.testing.Tester;
import java.util.Iterator;

// PR#294 anonymous inner class

public class Driver {
    
    public static void main(String[] args){
        C c = new C();
        String s = (String)c.result();
        Tester.checkEqual(s, "-anon", "");
    }
}

class C {
    public String result() {
        return getIt(new Iterator() {
            public Object next() {
            return "-anon";
            }
            public boolean hasNext() { return true; }
            public void remove() {} 
            });
    }
    
    public String getIt(Iterator u) {
        return (String)u.next();
    }
}

