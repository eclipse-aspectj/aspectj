public class PrivilegedParsing {
   private int hidden;
   public int visible;
   public PrivilegedParsing(int priv, int pub) {
      hidden = priv;
      visible = pub;
   }
   public void doSomething() {
      System.out.println("" + hidden + ", " + visible + "");
   }
   public static void main(String[] args) {
      PrivilegedParsing capsule = new PrivilegedParsing(1, 1);
      capsule.doSomething();
   }
}

aspect Outer {
   static privileged //<== JUST TRY TO UNCOMMENT THIS!
   aspect Inner {
      pointcut call2doSomething(PrivilegedParsing capsule):
         call(void PrivilegedParsing.doSomething())
         && target(capsule);
      before(PrivilegedParsing capsule): call2doSomething(capsule) {
         capsule.visible++;
         capsule.hidden++;
      }
   }
}

interface Marker {
   static privileged //<== JUST TRY TO UNCOMMENT THIS!
   aspect Inner {
      pointcut call2doSomething(PrivilegedParsing capsule):
         call(void PrivilegedParsing.doSomething())
         && target(capsule);
      before(PrivilegedParsing capsule): call2doSomething(capsule) {
         capsule.visible++;
         capsule.hidden++;
      }
   }
}