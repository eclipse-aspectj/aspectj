
package coordination;


public class TimeoutException extends Exception {
    long time;
    TimeoutException(long _time) {
	time = _time;
    }
}
