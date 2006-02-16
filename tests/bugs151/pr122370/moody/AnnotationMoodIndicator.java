package moody;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.DeclareParents;

@Aspect
public class AnnotationMoodIndicator {
   public interface Moody {
      Mood getMood();
      void setMood(Mood mood);
   }

   public static class MoodyImpl implements Moody {
      private Mood mood = Mood.HAPPY;

      public Mood getMood() { return mood; }
      public void setMood(Mood mood) { this.mood = mood; }
   }

  
@DeclareParents(value="moody.AnnotationMoodImplementor",defaultImpl=MoodyImpl.class)
   private Moody implementedInterface;
}
