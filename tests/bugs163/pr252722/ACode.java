import org.aspectj.lang.annotation.*;
import org.aspectj.lang.*;


abstract aspect Super {
  void foo(String s,int i) {}
}

public aspect ACode extends Super {

	void around(): execution(* m(..)) {
		super.foo("hello",7);
	}

	public static void main(String []argv) {
		ACode.m();
	}

	public static void m() {}
}

