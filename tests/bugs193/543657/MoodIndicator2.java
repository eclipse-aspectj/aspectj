import org.aspectj.lang.annotation.*;

@Aspect
public class MoodIndicator2 {

  public interface Moody2 { Mood getMood2(); };

  public static class Moody2Impl implements Moody2 {
    private Mood mood = Mood.SAD;

    public Mood getMood2() { return mood; }
  }

  @DeclareMixin("Code*")
  public static Moody2 createMoodyImplementation() {
    return new Moody2Impl();
  }

  @Before("!within(MoodIndicator*) && execution(* *.run(..)) && this(m)")
  public void feelingMoody(Moody2 m) {
    System.out.println("I'm feeling " + m.getMood2());
  }
}
