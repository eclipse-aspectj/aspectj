
public abstract aspect AbstractListSupport<I,M extends I> {

	//declare parents : @LinkedListItem * implements M;

	private M M.next;

	public I M.getNext () {
		return next;
	}

	public void M.setNext (M item) {
		next = item;
	}

}
