public class ReturnThis {
    public static void main(String[] args) {
	new C().foo();
    }
}

class C {
    public C foo() {
	return (this);
    }
}
