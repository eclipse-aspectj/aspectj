public class pr108377 {
    public static void main(String[] args) {
    	System.out.println(new pr108377().foo());
    }
}

aspect Aspect1 {
    public pr108377 pr108377.a;
    public String pr108377.value;

    public String pr108377.foo() {
    	if (a == null) a = this;
    	return a.value;
    }
}