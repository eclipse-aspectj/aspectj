package sample;

public aspect Logging {
	declare parents: sample.* && !Logging implements Loggable;
	public interface Loggable {}
	public Object Loggable.getLogger() { return null; }
}