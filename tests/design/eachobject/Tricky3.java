import org.aspectj.testing.Tester;

public class Tricky3 {
    public static void main(String[] args) {
	C c = new SubC();
    }
}

class C {
}

class SubC extends C {
    void m() { }
}

aspect A1 pertarget(target(SubC)) {
    after() returning (SubC sub): call(new(..)) {
	System.out.println("new " + sub);
    }
}

aspect A2 pertarget(call(void SubC.*())) {
    after() returning (SubC sub): call(new(..)) {
	System.out.println("new " + sub);
    }
}

aspect A3 pertarget(call(void m())) {
    after() returning (SubC sub): call(new(..)) {
	System.out.println("new " + sub);
    }
}
