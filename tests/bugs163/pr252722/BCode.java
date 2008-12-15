import org.aspectj.lang.annotation.*;
import org.aspectj.lang.*;


abstract aspect Super {
  void foo(String s) {}
}

public aspect BCode extends Super {

	void around(): execution(* m(..)) {
		super.foo("hello");
	}

	public static void main(String []argv) {
		new C().m();
	}
}

class C { 

	public void m() {}
}

