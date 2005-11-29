public class Simple {
  public static void main(String []argv) {
    new Simple().a();
  }

  public void a() {}
}

aspect X {
   before():execution(* a(..)) {
     System.err.println(thisJoinPoint);
     System.err.println(thisJoinPointStaticPart);
     System.err.println(thisEnclosingJoinPointStaticPart);
   }

   before():execution(Simple.new(..)) {
     System.err.println(thisJoinPoint);
     System.err.println(thisEnclosingJoinPointStaticPart);
     System.err.println(thisJoinPointStaticPart);
   }
}
