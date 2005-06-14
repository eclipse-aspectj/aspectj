import java.util.*;

class C {
  Set<String> simple_local;
  Set<Set<String>> nested_local;
}

aspect Inter_Type_Injector {
  Set<String> C.simple_intertype;
  Set<Set<String>> C.nested_intertype;

  public void test() {
    Set<String> simple_local = new C().simple_local; // works
    Set<String> simple_intertype = new C().simple_intertype; // works
    Set<Set<String>> nested_local = new C().nested_local; // works
    Set<Set<String>> nested_intertype = new C().nested_intertype; // fails
  }
}
