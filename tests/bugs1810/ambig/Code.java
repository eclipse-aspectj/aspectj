import java.util.List;

aspect F { void A.xx(List<String> x) { xx(null);this.xx(null);};}
class A {}
class B extends A { void xx(List<String> x) { xx(null); this.xx(null); super.xx(null); }}
class C implements D { public void xx(List<String> x) { xx(null); new A().xx(null); new B().xx(null); }}
interface D { void xx(List<String> x); }
class E { void foo() { new B().xx(null); new A() {}.xx(null); } }
