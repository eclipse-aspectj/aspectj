import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME) @interface Ann {String value();}

aspect Aspect {

  // Methods with out the Ann annotation but in an Ann annotated type get Ann
//  declare @method: !@Ann * (@Ann *).*(..) : @Ann("introduced");

  // Call to an annotated method
  pointcut annotated(Ann b) : call(@Ann * *(..)) && @annotation(b);

  // Top level call to an annotated method
  pointcut annotatedTop(Ann b) : annotated(b) && !cflowbelow(annotated(Ann));

  // Non top level call
  pointcut annotatedNotTop(Ann b, Ann bTopo) : 
    annotated(b) && cflowbelow(annotatedTop(bTopo));

  before(Ann b, Ann bTopo) :
    annotatedNotTop(b, bTopo) {
    System.out.println("Non-top:");
    System.out.println("\tJoin point: " + thisJoinPointStaticPart);
    System.out.println("\tEnclosing join point: " + thisEnclosingJoinPointStaticPart);
    System.out.println("\tAnn: " + b);
    System.out.println("\tTop annotation: " + bTopo);
  }

//  before(Ann b) :
//    annotatedTop(b) {
//    System.out.println("Top:");
//    System.out.println("\tJoin point: " + thisJoinPointStaticPart);
//    System.out.println("\tEnclosing join point: " +
//thisEnclosingJoinPointStaticPart);
//    System.out.println("\tAnn: " + b);
//  }
//  declare @method: !@Ann * (@Ann *).*(..) : @Ann("introduced");
  declare @method: * B.goo() : @Ann("introduced");
}

public class A {
  @Ann("A.foo") void foo() { new B().foo(); new B().goo();}
  public static void main(String[] args) { new A().foo(); }
}

@Ann("B") class B {
  // The Ann is injected here!
  @Ann("B.foo") 
  void foo() { }
  void goo() { }
}
