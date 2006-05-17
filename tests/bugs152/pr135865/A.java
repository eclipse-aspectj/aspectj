import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME) @interface Ann {}


aspect Aspect {

  before() : call(@Ann * *(..)) { 
    System.out.println("\tJoin point: " + thisJoinPointStaticPart);
  }

  // Methods with out the Ann annotation but in an Ann annotated type get Ann
  declare @method: !@Ann * (@Ann *).*(..) : @Ann;
}

public class A {
  void foo() { new B().foo(); }
  public static void main(String[] args) { new A().foo(); }
}

