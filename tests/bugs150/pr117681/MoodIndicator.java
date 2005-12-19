import org.aspectj.lang.annotation.*;

class Mood {
  public final static Mood HAPPY=new Mood();
}
   // this interface can be outside of the aspect
    interface Moody {
     Mood getMood(int i);
   };

   // this implementation can be outside of the aspect
    class MoodyImpl implements Moody {
      private Mood mood = Mood.HAPPY;

      public Mood getMood(int i) {
        return mood;
      }
   }
@Aspect
public class MoodIndicator {


   // here is the actual ITD syntax when using @AspectJ
   // public static is mandatory
   // the field type must be the introduced interface. It can't be a class.
   @DeclareParents(value="C", defaultImpl=MoodyImpl.class)
   Moody introduced;

//   @Before("execution(* *.*(..)) && this(m)")
//   public void feelingMoody(Moody m) {
//      System.out.println("I'm feeling " + m.getMood());
//   }

  public static void main(String []argv) {
    ((Moody)new C()).getMood(7);
  }
}


class C {
  
}
