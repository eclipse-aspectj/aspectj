import org.xyz.*;
public aspect AJDKExamples {
	
	declare warning : call(* org.xyz.*.*(int, String...)) : "call vararg match";
	
	declare warning : execution(* org.xyz.*.*(Integer...)) : "execution vararg match";
	
	declare warning : initialization(org.xyz.*.new((Foo || Goo)...)) : "init vararg match";
	
	declare warning : execution(* *.*(String...)) : "single vararg";
	
	declare warning : execution(* *.*(String[])) : "single String[]";
	
	before(int i, String[] ss) : call(* foo(int,String...)) && args(i,ss) {
		System.out.println("Matched at " + thisJoinPoint);
	}
	
	public static void main(String[] args) {
		X foo = new X();
		foo.foo(5,"hello");
		foo.bar(5,new String[]{"hello"});
	}
		
}

class X {
	public void foo(String... ss) {}
	public void bar(String[] ss) {}
	public void foo(int i,String... ss) {}
	public void bar(int i,String[] ss) {}
}

