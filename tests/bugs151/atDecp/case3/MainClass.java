package theapp;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.DeclareParents;

import moody.*;

class AnnotationMoodImplementor { }

@Aspect
class AnnotationMoodIndicator {

   public static class MoodyImpl implements Moody {
      private Mood mood = Mood.HAPPY;

      public Mood getMood() { return mood; }
      public void setMood(Mood mood) { this.mood = mood; }
   }

   @DeclareParents(value="theapp.AnnotationMoodImplementor",defaultImpl=MoodyImpl.class)
   private Moody implementedInterface;
}



public class MainClass {
  public static void main(String[] args) {
    AnnotationMoodImplementor ami0 = new AnnotationMoodImplementor();
    AnnotationMoodImplementor ami1 = new AnnotationMoodImplementor();

    System.err.println("ami0's mood is " + ((Moody) ami0).getMood());
    ((Moody) ami1).setMood(Mood.JOLLY);
    System.err.println("ami1's mood is now " + ((Moody) ami1).getMood());
    System.err.println("ami0's mood is still " + ((Moody) ami0).getMood());
  }
}

