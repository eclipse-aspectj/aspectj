 

public aspect A {
    after () throwing (Throwable th) :  execution(* *(..)) {
		System.err.println(thisEnclosingJoinPointStaticPart);
	}
}

class C1 {
	public void anotherMethod() {
		
	}
}
