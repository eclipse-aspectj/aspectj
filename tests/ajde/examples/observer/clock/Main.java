package clock;

import java.util.Observer;
import java.util.Observable;

public class Main {

    public static void main(String[] args) {
        System.err.println("> starting clock...");
        ClockTimer clockTimer = new ClockTimer();
	DigitalClock digitalClock = new DigitalClock(clockTimer);
        AnalogueClock analogueClock = new AnalogueClock(clockTimer);
    }
}

