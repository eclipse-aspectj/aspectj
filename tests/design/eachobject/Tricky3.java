import org.aspectj.testing.Tester;

public class Tricky3 {
    public static void main(String[] args) {
    	C c = new SubC();
    	((SubC)c).m();
    }
}

class C {
}

class SubC extends C {
    void m() { }
}

aspect A1 pertarget(target(SubC)) {
    after(SubC sub) returning: call(* m(..)) && target(sub) {
    	System.out.println("Called m() on " + sub.getClass().getName());
    }
}

aspect A2 pertarget(call(void SubC.*())) {
    after(SubC sub) returning: call(* m(..)) && target(sub) {
    	System.out.println("Called m() on " + sub.getClass().getName());
    }
}

aspect A3 pertarget(call(void m())) {
    after(SubC sub) returning: call(* m(..)) && target(sub) {
    	System.out.println("Called m() on " + sub.getClass().getName());
    }
}
