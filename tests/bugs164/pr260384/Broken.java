
interface I {
	void getCode();
}

abstract class C1 implements I {
}

abstract class C2 extends C1 {
  public void m() {
	  getCode();
  }
}


aspect X {
	before(int i): args(i) { }// call(* *(..))  && args(i) {}
}
