public class Code {
  public void m() {
  }

  public void n() {
  }
  
}

aspect X {
  before(): execution(* Code.*(..)) {System.out.println(thisJoinPointStaticPart);}
}
