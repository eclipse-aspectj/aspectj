package bugs;

class GenericClass<K> {
          public void f(K t) {}
}
class ExtendsGenericHasITD extends GenericClass<String> {}

public aspect VerifyError2 {
          public void ExtendsGenericHasITD.f(String s) {
                   super.f(s);
          }
          public static void main( String[] args ) {
                   new ExtendsGenericHasITD();
          }
}
