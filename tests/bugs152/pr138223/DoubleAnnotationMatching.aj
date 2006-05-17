import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME) @interface Tx {boolean value() default false;}

public aspect DoubleAnnotationMatching {
  pointcut methodInTxType(Tx tx) : 
    execution(* *(..)) && @this(tx) && if(tx.value());
   
  pointcut txMethod(Tx tx) :
    execution(* *(..)) && @annotation(tx) && if(tx.value());
   
  pointcut transactionalOperation() :
    methodInTxType(Tx) || txMethod(Tx);
   
  before() : transactionalOperation() {
    System.err.println("advice running at "+thisJoinPoint);
  }

  public static void main(String [] argv) {
	  new Foo().a();
	  new Foo().b();
	  new Foo().c();
	  new TxTrueFoo().a();
	  new TxTrueFoo().b();
	  new TxTrueFoo().c();
	  new TxFalseFoo().a();
	  new TxFalseFoo().b();
	  new TxFalseFoo().c();
  }
}

@Tx(true) class TxTrueFoo {
  @Tx(true) public void a() {}
  @Tx(false) public void b() {}
  public void c() {}
}

@Tx(false) class TxFalseFoo {
  @Tx(true) public void a() {}
  @Tx(false) public void b() {}
  public void c() {}
}

class Foo {
  @Tx(true) public void a() {}
  @Tx(false) public void b() {}
  public void c() {}
}