public abstract aspect pr115038<Observable, Observer, Event> {
	public boolean Observer.handle(Observable o, Event e) {
		return true;
	}
}
