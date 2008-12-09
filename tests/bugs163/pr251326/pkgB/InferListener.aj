package pkgB;

import pkgA.Listener;
import pkgA.Target;

public aspect InferListener {
	
	public Listener Target.listenB() {
		return new Listener() {
			public void happened(String event) {
				System.out.println(event);
			}
		};
	}
	
}
