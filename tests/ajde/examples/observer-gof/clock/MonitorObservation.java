package clock;

aspect MonitorObservation {

    before(): call(void Clock.update(..))
              || execution(void *.addObserver(..))
              || execution(void *.removeObserver(..)) {
    }
}
