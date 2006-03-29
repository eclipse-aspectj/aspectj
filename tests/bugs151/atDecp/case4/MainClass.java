package theapp;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.DeclareParents;

import moody.*;

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

