
package clock;
import java.util.*;

public class ClockTimer extends Observable {
  List observers;
  public int getHour() {
    return 0;
  }

  public int getMinute() {
    return 0;
  }

  public int getSeconds() {
    return 0;
  }

  public void addObserver(Object observer) {
    this.observers.add(observer);
  }

  public void tick() {
    this.notifyObservers();
  }

  public ClockTimer() {
    super();
    {
      this.observers = new ArrayList();
    }
  }
}