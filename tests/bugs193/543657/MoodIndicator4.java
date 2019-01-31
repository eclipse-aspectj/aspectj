import org.aspectj.lang.annotation.*;
import org.aspectj.lang.*;

enum Mood {HAPPY,SAD}
@Aspect
public class MoodIndicator4 {

  public interface Moody { Mood getMood(); };

  public static class MoodyImpl implements Moody {
    private Mood mood = Mood.HAPPY;

    public Mood getMood() { return mood; }
  }

  @DeclareMixin("MoodIndicator4")
  public static Moody createMoodyImplementation() {
    return new MoodyImpl();
  }

  @Before("execution(* *.run(..)) && this(m)")
  public void feelingMoody(Moody m) {
    System.out.println("I'm feeling " + m.getMood());
  }

  public static void main(String[]argv) {
    Aspects.aspectOf(MoodIndicator4.class).run();
  }

  public void run() {
  }
}
