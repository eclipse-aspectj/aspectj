package pkg;

public class A {

}

aspect X {
  declare warning: staticinitialization(*) && if(!thisEnclosingJoinPoint.toString().equals("abc")): "Foobar {joinpoint}";
  // before(): staticinitialization(*) && if(!thisEnclosingJoinPointStaticPart.getPackage().equals(thisJoinPoint.getPackage())) {}
  //declare warning: staticinitialization(*) && if(true): "Foobar {joinpoint}";
}
