public class SomeBaseClass<Type extends Object> {

 public void tag_someBaseMethod() {
  System.out.println("some base method");
 }

 public static void main(String[] args) {
  new SomeBaseClass<Object>().tag_someBaseMethod();
  new SomeSubClass().tag_someMethod(); // this does not match correctly...
//  System.out.println("");

  new SomeBaseClass2<Object>().tag_someBaseMethod();
  new SomeSubClass2().tag_someMethod();
//  System.out.println("");

  new SomeBaseClass3().tag_someBaseMethod();
  new SomeSubClass3().tag_someMethod();
 }
}

