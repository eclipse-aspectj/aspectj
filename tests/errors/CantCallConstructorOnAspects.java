public class CantCallConstructorOnAspects {
    public static void main(String[] args) {
	//ERROR: can't call new on an aspect/of
	A a = new A();
    }
}

class C {
}

aspect A /*of eachobject(instanceof(C))*/ {
}
