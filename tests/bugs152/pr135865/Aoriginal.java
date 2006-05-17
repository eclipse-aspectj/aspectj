import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME) @interface Ann {}


aspect Aspect {

  // Call to an annotated method
  pointcut annotated(Ann b) : call(@Ann * *(..)) && @annotation(b);

  // Top level call to an annotated method
  pointcut annotatedTop(Ann b) : annotated(b) && !cflowbelow(annotated(Ann));

  // Non top level call
  pointcut annotatedNotTop(Ann b, Ann bTopo) : 
    annotated(b) && cflowbelow(annotatedTop(bTopo));

  //before(Ann b, Ann bTopo) : annotatedNotTop(b, bTopo) {
  before() : call(@Ann * *(..)) { //(b, bTopo) {
    System.out.println("\tJoin point: " + thisJoinPointStaticPart);
  }

  // Methods with out the Ann annotation but in an Ann annotated type get Ann
  declare @method: !@Ann * (@Ann *).*(..) : @Ann;
}

public class A {
   void foo() { new B().foo(); /*new B().goo();*/}
  public static void main(String[] args) { new A().foo(); }
}

