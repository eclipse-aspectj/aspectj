import org.aspectj.testing.Tester;

public class ClassLiteralField {  // synthetic class$n set
	public static void main(String[] args) {
		Class c = ClassLiteralField.class;  // synthetic class$n get
		assert c != null;  //synthetic $assert
		new ClassLiteralField().doInner();
	}
	int x=10;
	void doInner() {
		new Runnable() {         // synthetic this$n
			public void run() {
				x+=1;            // synthetic this$n
			}
		}.run();
	}
}

aspect A {
//	before(): within(ClassLiteralField) && get(* *) && !get(* x) {
//		System.out.println("get: " + thisJoinPoint +", " + thisJoinPoint.getSourceLocation());
//	}

	declare error: within(ClassLiteralField) && get(* *) && !get(* x): "unexpected get";
	declare error: within(ClassLiteralField) && set(* *) && !set(* x): "unexpected set";
}