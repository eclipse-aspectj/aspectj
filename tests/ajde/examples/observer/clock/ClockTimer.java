
package clock;

import java.util.Observable;

public class ClockTimer extends Observable {

    public void tick () {
        notifyObservers();
    }

    public int getHour() {
        return 0;
    }

    public int getMinute() {
        return 0;
    }

    public int getSeconds() {
        return 0;
    }
}
