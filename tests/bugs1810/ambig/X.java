import java.util.List;

aspect F { 
  void A.xx(List<String> x) { }
}
class A {
  //void xx(List<String> x) {}
}
class E { 
  void foo() {
    new A() {}.xx(null); 
  } 
}
