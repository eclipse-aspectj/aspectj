// PR 52928

public class VisiblePrivateInterfaceITDs {

   public static void main(String[] args) {
   	  VisiblePrivateInterfaceITDs s = new VisiblePrivateInterfaceITDs();
      s.aMethod();
   }

   public void aMethod() {
      // x is introduced by the following aspect as private
      // so it should not be accessible here
      System.out.println("I have " + x);  // CE 13
   }

}

aspect SampleAspect {
   private interface Tag {};
   
   private int Tag.x = 0;
   
   declare parents: VisiblePrivateInterfaceITDs implements Tag;
}