
package clock;

import java.util.Observer;
import java.util.Observable;

public class Main {

  public static void main(String[] args) {
    ClockTimer clockTimer = new ClockTimer();
    DigitalClock digitalClock = new DigitalClock(clockTimer);
    AnalogClock analogClock = new AnalogClock(clockTimer);
  }
}