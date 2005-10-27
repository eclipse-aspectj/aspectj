
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class LinkedList implements List<ListItem> {

	public boolean add (ListItem item) {
		MutableListItem listItem = (MutableListItem)item;
		listItem.setNext(null);
		
		return true;
	}

	public ListItem get (int i) {
		return new Item();
	}

	public void add(int index, ListItem element) {
		// TODO Auto-generated method stub
		
	}

	public boolean addAll(Collection<? extends ListItem> c) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean addAll(int index, Collection<? extends ListItem> c) {
		// TODO Auto-generated method stub
		return false;
	}

	public void clear() {
		// TODO Auto-generated method stub
		
	}

	public boolean contains(Object o) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean containsAll(Collection<?> c) {
		// TODO Auto-generated method stub
		return false;
	}

	public int indexOf(Object o) {
		// TODO Auto-generated method stub
		return 0;
	}

	public boolean isEmpty() {
		// TODO Auto-generated method stub
		return false;
	}

	public Iterator<ListItem> iterator() {
		// TODO Auto-generated method stub
		return null;
	}

	public int lastIndexOf(Object o) {
		// TODO Auto-generated method stub
		return 0;
	}

	public ListIterator<ListItem> listIterator() {
		// TODO Auto-generated method stub
		return null;
	}

	public ListIterator<ListItem> listIterator(int index) {
		// TODO Auto-generated method stub
		return null;
	}

	public ListItem remove(int index) {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean remove(Object o) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean removeAll(Collection<?> c) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean retainAll(Collection<?> c) {
		// TODO Auto-generated method stub
		return false;
	}

	public ListItem set(int index, ListItem element) {
		// TODO Auto-generated method stub
		return null;
	}

	public int size() {
		// TODO Auto-generated method stub
		return 0;
	}

	public List<ListItem> subList(int fromIndex, int toIndex) {
		// TODO Auto-generated method stub
		return null;
	}

	public Object[] toArray() {
		// TODO Auto-generated method stub
		return null;
	}

	public <T> T[] toArray(T[] a) {
		// TODO Auto-generated method stub
		return null;
	}
	
	private interface MutableListItem extends ListItem {
		public void setNext (MutableListItem o);
	}
	
	private static aspect LinkedListSupport {
		declare parents : Item implements MutableListItem;
		declare parents : @LinkedListItem * implements MutableListItem;

		private MutableListItem MutableListItem.next;
		
		public ListItem MutableListItem.getNext () {
			return next;
		}
		
		public void MutableListItem.setNext (MutableListItem item) {
			next = item;
		}
	}
	
	
}
