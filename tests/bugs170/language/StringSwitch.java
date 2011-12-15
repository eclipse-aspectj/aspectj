public class StringSwitch {
  public static void main(String []argv) {
  }
}

aspect Foo {
  before(): execution(* *(..)) {
String s = "abc";
switch(s) {
 case "quux":
   foo();
    // fall-through

  case "foo":
  case "bar":
 foo();
    break;

  case "baz":
 foo();
    // fall-through

  default:
 foo();
    break;
}

  }
  

  public void foo() {}

}
