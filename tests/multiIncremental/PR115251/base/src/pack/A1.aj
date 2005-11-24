package pack;

public abstract aspect A1<Target> pertypewithin(Target) {

	abstract protected pointcut creation();

	Target around() : creation() {
		return null;
	}
}
