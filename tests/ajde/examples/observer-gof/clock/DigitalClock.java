
package clock;

public class DigitalClock implements Clock {
  private ClockTimer subject;

  public DigitalClock(ClockTimer subject) {
    super();
    this.subject = subject;
    this.subject.addObserver(this);
  }
  public void removeObserver(Object observer) {
    this.subject.observers.remove(observer);
  }

  public void update(ClockTimer subject, Object args) {
    if (this.subject == subject) {
      this.draw();
    }
  }

  public void draw() {
    int hour = this.subject.getHour();
    int minute = this.subject.getMinute();
  }
}