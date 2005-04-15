//"The Moody example"

import org.aspectj.lang.annotation.*;

class Mood{
}
@Aspect
class MoodIndicator {
 
   public interface Moody {
     Mood getMood();
   };
   
   @DeclareParents("org.xzy..*")
   class MoodyImpl implements Moody {
      private Mood mood = new Mood();
      
      public Mood getMood() {
        return mood;
      }
   }

   @Before("execution(* *.*(..)) && this(m)")     
   void feelingMoody(Moody m) {
      System.out.println("I'm feeling " + m.getMood());
   }
}
