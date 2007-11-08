import org.aspectj.lang.annotation.*;

public aspect Bug2 {
  pointcut pc(int i): args(i) && if(i<0);

  before(): pc(*) {
  }

  public void trigger(int i) {}
}
