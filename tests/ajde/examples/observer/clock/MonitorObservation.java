package clock;

import java.util.Observer;
import java.util.Observable;

aspect MonitorObserveration {
    before():
              (call(void Observer.update(Observable, Object))
               || call(void Observable.addObserver(Observer)) 
               || call(void Observable.removeObserver(Observer))) {

    }  
}

