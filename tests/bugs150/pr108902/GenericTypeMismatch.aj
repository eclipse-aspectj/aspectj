import java.util.*;

abstract aspect ObserverProtocol {
	
	private Collection Subject.observers = new ArrayList();
	
	public Collection Subject.getObservers() {
		return observers;
	}
}

aspect XYZ extends ObserverProtocol {}