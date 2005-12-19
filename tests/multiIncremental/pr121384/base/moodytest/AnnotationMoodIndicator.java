package moodytest;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.DeclareParents;

@Aspect
public class AnnotationMoodIndicator {
  public interface Moody {
     Mood getMood();
     void setMood(Mood mood);
  }

  public static class MoodyImpl implements Moody {
     Mood mood = Mood.HAPPY;

     public Mood getMood() { return mood; }
     public void setMood(Mood mood) { this.mood = mood; }
  }

  @DeclareParents(value="moodytest.AnnotationMoodyImplementor",defaultImpl=MoodyImpl.class)
  private Moody introduced;
}
