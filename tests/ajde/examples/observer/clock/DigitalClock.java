
package clock;

import java.util.Observer;
import java.util.Observable;

public class DigitalClock implements Observer {

    private ClockTimer subject;

    public DigitalClock (ClockTimer subject) {
        this.subject = subject;
        this.subject.addObserver(this);
    }

    public void removeObserver() {
        subject.deleteObserver(this);
    }

    public void update(Observable theChangedSubject, Object args) {
        if (theChangedSubject == subject)  {
            draw();
        }
    }

    public void draw () {
        int hour = subject.getHour();
        int minute = subject.getMinute();
    }
}
