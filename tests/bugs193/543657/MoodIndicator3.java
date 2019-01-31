import org.aspectj.lang.annotation.*;

@Aspect
class MoodIndicator2 {

  public interface Moody { Mood getMood(); };

  public static class MoodyImpl implements Moody {
    private Mood mood = Mood.SAD;

    public Mood getMood() { return mood; }
  }

  @DeclareMixin("Code*")
  public static Moody createMoodyImplementation() {
    return new MoodyImpl();
  }

  @Before("!within(MoodIndicator*) && execution(* *.run(..)) && this(m)")
  public void feelingMoody(Moody m) {
    System.out.println("I'm feeling " + m.getMood());
  }
}
