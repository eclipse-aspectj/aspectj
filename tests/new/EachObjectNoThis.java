import org.aspectj.testing.Tester;

public class EachObjectNoThis {
    public static void main(String[] args) {
	new C().foo();
    }
}

class C {
    public void foo() {
    }
}

aspect A /*of eachobject(!instanceof(Object))*/ {
    {
	// This doesn't appy
        //Tester.checkFailed("this aspect shouldn't exist");
    }
    before(): !target(Object) && call(* *(..)) {
  	Tester.checkFailed("this aspect shouldn't exist");
    }
}
    
