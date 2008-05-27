interface X {
   static aspect IMPL {
      public void X.test() { System.out.println("X.test()"); }
   }
}

interface Y {
   static aspect IMPL {
      declare precedence : X.IMPL, Y.IMPL;

      public void Y.test() { System.out.println("Y.test()"); }
   }
}

public class Z implements X, Y {
   public static void main(String[] args) throws Exception { new Z().test(); }
}
