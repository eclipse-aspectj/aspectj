package test;

public class A {
	public void a(String s) {}
	public void b(@A1 String s) {}
	public void c(@A1 @A2 String s) {}
	public void d(@A1 String s,@A2 String t) {}
	
	public void e(A1AnnotatedType s) {}
	public void f(A2AnnotatedType s) {}
	public void g(@A2 A1AnnotatedType s) {}
	public void h(@A1 A1AnnotatedType s) {}
	public void i(A1AnnotatedType s,@A2 String t) {}
	public void j(@A1 @A2 String s) {}

}
