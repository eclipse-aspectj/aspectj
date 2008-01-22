package bugs;

class GenericClass< K > {
          public void f() {}
}
class ExtendsGenericHasITD extends GenericClass< Object > {}

public aspect VerifyError {
          public void ExtendsGenericHasITD.f() {
                   super.f();
          }
          public static void main( String[] args ) {
                   new ExtendsGenericHasITD();
          }
}
