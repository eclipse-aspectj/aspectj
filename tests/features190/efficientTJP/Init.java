public class Init {
	public static void main(String []argv) {
		new A();
		new B();
	}
}

class A {}
class B {}

aspect X {
	before(): preinitialization(A.new(..)) && !within(X) {
		System.out.println(thisJoinPointStaticPart.getSignature());
	}
	before(): preinitialization(A.new(..)) && !within(X) {
		System.out.println(thisEnclosingJoinPointStaticPart.getSignature());
	}
	before(): initialization(B.new(..)) && !within(X) {
		System.out.println(thisJoinPointStaticPart.getSignature());
	}
	before(): initialization(B.new(..)) && !within(X) {
		System.out.println(thisEnclosingJoinPointStaticPart.getSignature());
	}
}
