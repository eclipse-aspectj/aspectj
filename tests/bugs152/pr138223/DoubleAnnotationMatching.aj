import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@interface Tx {
  boolean readOnly() default false;
}

public aspect DoubleAnnotationMatching {


 pointcut methodInTxType(Tx tx) : 
   execution(* *(..)) && @this(tx) && if(tx.readOnly());
   
 pointcut txMethod(Tx tx) :
   execution(* *(..)) && @annotation(tx) && if(tx.readOnly());
   
 pointcut transactionalOperation() :
   methodInTxType(Tx) || txMethod(Tx);
   
 before() : transactionalOperation() {
   // do something
 }

}

@Tx class Foo {

  public void foo() {}
  
  @Tx public void bar() {}


}