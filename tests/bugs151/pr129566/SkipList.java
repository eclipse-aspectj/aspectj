
package common;

import java.lang.reflect.Array;
import java.util.*;

/**
 * Class SkipList implements a skip list that uses int primitives for
 * the element keys. The modification methods defined by <code>Set</code>
 * are not supported. Instead, methods with keys are required. Note that
 * the keys have a many to one relationship with the elements (that is,
 * a range of key values will map to a single element).
 *
 * <p><b>Note that this implementation is not synchronized.</b> If multiple
 * threads access a set concurrently, and at least one of the threads modifies
 * the set, it <i>must</i> be synchronized externally.  This is typically
 * accomplished by synchronizing on some object that naturally encapsulates
 * the set.  If no such object exists, the set should be "wrapped" using the
 * <code>Collections.synchronizedList</code> method.  This is best done at
 * creation time, to prevent accidental unsynchronized access to the set:</p>
 *<pre>
 *     List l = Collections.synchronizedList(new DisjointSet(...));
 *</pre>
 *
 * <p>The Iterators returned by this class's <tt>iterator</tt> method are
 * <i>fail-fast</i>: if the set is modified at any time after the iterator is
 * created, in any way except through the iterator's own <tt>remove</tt>
 * method, the iterator will throw a <tt>ConcurrentModificationException</tt>.
 * Thus, in the face of concurrent modification, the iterator fails quickly
 * and cleanly, rather than risking arbitrary, non-deterministic behavior at
 * an undetermined time in the future.</p>
 *
 * @author Original version: Nathan Fiedler (2001)
 * @author Several important bugfixes: Holger Hoffstaette 
 *
 * @version $Revision$ $Date$
 */

public class SkipList<T extends Comparable> extends Object implements Set<T>, Iterable<T>
{
	/** Optimal probability of most skip lists. */
	public static final double OPTIMAL_P = 0.25;

	// MaxLevel = L(N) (where N is an upper bound on the number of
	// elements in a skip list). If p = 0.5, using MaxLevel = 16 is
	// appropriate for data structures containing up to 2^16 elements.
	/** Maximum level of any SkipList instance. */
	protected final int MAX_LEVEL;

	/** Probability value for this skip list. */
	protected final double P;

	/** Tail of this skip list. */
	protected final SkipListElement<T> _NIL;

	/** The level of this skip list. */
	protected int _listLevel;

	/** Header is an element with no data. */
	protected SkipListElement<T> _listHeader;

	/** Number of elements in this skip list. */
	protected int _elementCount;

	/** Increments each time the list changes. */
	protected int _modCount;

	/**
	 * Constructs an empty SkipList using the default probability
	 * and maximum element size.
	 */
	public SkipList()
	{
		this(OPTIMAL_P, (int)Math.ceil(Math.log(Integer.MAX_VALUE) / Math.log(1 / OPTIMAL_P)) - 1);
	}

	/**
	 * Constructs an empty SkipList object using the given probability
	 * and maximum level.
	 *
	 * @param  probability  skip list probability value.
	 * @param  maxLevel     maximum skip list level.
	 */
	public SkipList(double probability, int maxLevel)
	{
		P = probability;
		MAX_LEVEL = maxLevel;

		// Header is the root of our skip list.
		_listHeader = new SkipListElement<T>(MAX_LEVEL, Integer.MIN_VALUE, null);

		// Allocate NIL with a key greater than any valid key;
		// all levels of skip lists terminate on NIL.
		_NIL = new SkipListElement<T>(0, Integer.MAX_VALUE, null);

		this.clear();
	}

	public SkipList(Collection<? extends T> c)
	{
		this();
		this.addAll(c);
	}

	public boolean add(T o)
	{
		this.insert(o.hashCode(), o);
		return true;
	}

	public boolean addAll(Collection<? extends T> c)
	{
		boolean added = false;

		if (!c.isEmpty())
		{
			for (Iterator<? extends T> iter = c.iterator(); iter.hasNext();)
			{
				added |= this.add(iter.next());
			}
		}

		return added;
	}

	public void clear()
	{
		// List level is started at one.
		_listLevel = 1;

		// All forward pointers of list's header point to NIL.
		for (int i = _listHeader._forward.length - 1; i >= 0; i--)
		{
			_listHeader._forward[i] = _NIL;
		}

		// Reset element count.
		_elementCount = 0;
		_modCount++;
	}

	public boolean contains(Object o)
	{
		for (SkipListElement e = _listHeader._forward[0]; e != _NIL; e = e._forward[0])
		{
			if ((e._value == o) || e._value != null && e._value.equals(o))
			{
				return true;
			}
		}

		return false;
	}

	public boolean containsAll(Collection<?> c)
	{
		if ((this.size() < c.size()) || (this.isEmpty() && c.isEmpty()))
		{
			return false;
		}

		for (Iterator<?> iter = c.iterator(); iter.hasNext();)
		{
			if (!this.contains(iter.next()))
			{
				return false;
			}
		}

		return true;
	}

	/**
	 * Removes the element with the given key from the list.
	 *
	 * @param  searchKey  key of element to remove.
	 * @return  <code>true</code> if element was found and removed.
	 */
	public boolean remove(int searchKey)
	{
		SkipListElement[] update = new SkipListElement[MAX_LEVEL];
		SkipListElement e = _listHeader;

		for (int i = _listLevel; i >= 0; i--)
		{
			while (e._forward[i]._key < searchKey)
			{
				e = e._forward[i];
			}

			update[i] = e;
		}

		e = e._forward[0];

		if (e._key == searchKey)
		{
			for (int i = 0; i < _listLevel; i++)
			{
				if (update[i]._forward[i] != e)
				{
					break;
				}

				update[i]._forward[i] = e._forward[i];
			}

			while (_listLevel > 0 && _listHeader._forward[_listLevel] == _NIL)
			{
				_listLevel--;
			}

			_elementCount--;
			_modCount++;
			return true;
		}

		return false;
	}

	/**
	 * Inserts the element using the given search key. If an element
	 * with the same key already exists in the skip lists, its value
	 * will be replaced with <code>newValue</code>.
	 *
	 * @param  searchKey  key for element.
	 * @param  newValue   new element to insert.
	 */
	@SuppressWarnings("unchecked")
	public void insert(int searchKey, T newValue)
	{
		SkipListElement<T>[] update = new SkipListElement[MAX_LEVEL];
		SkipListElement<T> e = _listHeader;

		for (int i = _listLevel - 1; i >= 0; i--)
		{
			while (e._forward[i]._key < searchKey)
			{
				e = e._forward[i];
			}

			update[i] = e;
		}

		e = e._forward[0];

		if (e._key == searchKey)
		{
			e._value = newValue;
		}
		else
		{
			int lvl = randomLevel();
			if (lvl > _listLevel)
			{
				for (int i = _listLevel; i <= lvl; i++)
				{
					update[i] = _listHeader;
				}

				_listLevel = lvl;
			}

			e = new SkipListElement<T>(lvl, searchKey, newValue);

			for (int i = 0; i < lvl; i++)
			{
				e._forward[i] = update[i]._forward[i];
				update[i]._forward[i] = e;
			}
		}

		_elementCount++;
		_modCount++;
	}

	public boolean isEmpty()
	{
		return (_elementCount == 0);
	}

	public Iterator<T> iterator()
	{
		return new SkipListIterator<T>();
	}

	/**
	 * Return a random level.
	 *
	 * @return  level selected randomly.
	 */
	protected int randomLevel()
	{
		int lvl = 1;

		while (lvl < MAX_LEVEL && Math.random() < P)
		{
			lvl++;
		}

		return lvl;
	}

	public boolean remove(Object o)
	{
		return this.remove(o.hashCode());
	}

	public boolean removeAll(Collection c)
	{
		boolean removed = false;

		for (Iterator iter = c.iterator(); iter.hasNext();)
		{
			removed |= this.remove(iter.next());
		}

		return removed;
	}

	public boolean retainAll(Collection c)
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * Searches for the element with the given key.
	 *
	 * @param  searchKey  key to look for.
	 * @return  element if found, null if not found. Note that you may
	 *          not want to store nulls in this list as it would then
	 *          be difficult to know the difference.
	 */
	public T get(int searchKey)
	{
		SkipListElement<T> e = _listHeader;

		for (int i = _listLevel - 1; i >= 0; i--)
		{
			while (e._forward[i]._key < searchKey)
			{
				e = e._forward[i];
			}
		}

		e = e._forward[0];

		if (e._key == searchKey)
		{
			return e._value;
		}
		else
		{
			return null;
		}
	}

	/**
	 * Searches for the element with a key that is the least smaller
	 * value of the given key.
	 *
	 * @param  searchKey  key to look for.
	 * @return  element if found, null if not found.
	 */
	public T searchLeastSmaller(int searchKey)
	{
		SkipListElement<T> e = _listHeader;

		for (int i = _listLevel - 1; i >= 0; i--)
		{
			while (e._forward[i]._key < searchKey)
			{
				e = e._forward[i];
			}
		}

		if (e._forward[0]._key == searchKey)
		{
			return e._forward[0]._value;
		}
		else
		{
			return e._value;
		}
	}

	/**
	 * Searches for the element just after the one found using the
	 * given key (where the key value may be the least smaller of
	 * the given key).
	 *
	 * @param  searchKey  key to look for.
	 * @return  next element if found, null if not found.
	 */
	public T searchNextLarger(int searchKey)
	{
		SkipListElement<T> e = _listHeader;

		for (int i = _listLevel - 1; i >= 0; i--)
		{
			while (e._forward[i]._key < searchKey)
			{
				e = e._forward[i];
			}
		}

		SkipListElement<T> t = null;

		if (e._forward[0]._key == searchKey)
		{
			t = e._forward[0];
		}
		else
		{
			t = e;
		}

		if (t._forward[0] == _NIL)
		{
			return null;
		}
		else
		{
			return t._forward[0]._value;
		}
	}

	/**
	 * Returns the number of elements in this list. If this list contains
	 * more than <tt>Integer.MAX_VALUE</tt> elements, returns
	 * <tt>Integer.MAX_VALUE</tt>.
	 *
	 * @return the number of elements in this list.
	 */
	public int size()
	{
		return _elementCount;
	}

	/**
	 * Returns an array containing all of the elements in this list in
	 * proper sequence. Obeys the general contract of the
	 * <tt>Collection.toArray()</tt> method.
	 *
	 * @return an array containing all of the elements in this list in
	 *         proper sequence.
	 * @see Arrays#asList(Object[])
	 */
	public Object[] toArray()
	{
		return toArray(new Object[_elementCount]);
	}

	/**
	 * Returns an array containing all of the elements in this list in
	 * proper sequence; the runtime type of the returned array is that
	 * of the specified array. Obeys the general contract of the
	 * <tt>Collection.toArray(Object[])</tt> method.
	 *
	 * @param  a  the array into which the elements of this list are to
	 *            be stored, if it is big enough; otherwise, a new array
	 *            of the same runtime type is allocated for this purpose.
	 * @return  an array containing the elements of this list.
	 * @exception  ArrayStoreException
	 *             Throw if the runtime type of the specified array is not
	 *             a supertype of the runtime type of every element in this
	 *             list.
	 */
	@SuppressWarnings({"unchecked","hiding"})
	public <T> T[] toArray(T[] a)
	{
		int size = this.size();

		if (a.length < size)
		{
			a = (T[])Array.newInstance(a.getClass().getComponentType(), size);
		}

		SkipListElement e = _listHeader;

		for (int i = 0; i < _elementCount; i++)
		{
			a[i] = (T)e._forward[0]._value;
			e = e._forward[0];
		}

		return a;
	}

	/**
	 * Class Element represents an element of a skip list.
	 */
	protected class SkipListElement<E> extends Object
	{
		/** Key of element. */
		int _key;

		/** Value of element. */
		E _value;

		/** List of forward pointers. */
		SkipListElement<E>[] _forward;

		/**
		 * Constructs an Element for the given key and value.
		 *
		 * @param  level  level for this node (number of forward pointers).
		 * @param  key    key for element.
		 * @param  value  value for element.
		 */
		@SuppressWarnings("unchecked")
		public SkipListElement(int level, int key, E value)
		{
			_key = key;
			_value = value;
			_forward = new SkipListElement[level];
		}
	}

	/**
	 * An iterator over a skip list.
	 */
	protected class SkipListIterator<E> implements Iterator<T>
	{
		/** Index into the skip list. */
		protected int _index;

		/** The modCount of the list at the time we were instantiated. */
		protected int _listModCount;

		/** Current element being examined. */
		protected SkipListElement<T> _elem;

		/**
		 * Constructs a skip list iterator.
		 */
		public SkipListIterator()
		{
			_listModCount = SkipList.this._modCount;
			_elem = _listHeader;
		}

		/**
		 * Returns <tt>true</tt> if the iteration has more elements. (In
		 * other words, returns <tt>true</tt> if <tt>next</tt> would return
		 * an element rather than throwing an exception.)
		 *
		 * @return  <tt>true</tt> if the iterator has more elements.
		 */
		public boolean hasNext()
		{
			if (this._listModCount != SkipList.this._modCount)
			{
				throw new ConcurrentModificationException();
			}

			return _elem._forward[0] != _NIL;
		}

		/**
		 * Returns the next element in the iteration.
		 *
		 * @return  the next element in the iteration.
		 * @exception  NoSuchElementException
		 *             iteration has no more elements.
		 */
		public T next()
		{
			if (this._listModCount != SkipList.this._modCount)
			{
				throw new ConcurrentModificationException();
			}

			if (this.hasNext())
			{
				_elem = _elem._forward[0];
				return _elem._value;
			}
			else
			{
				throw new NoSuchElementException();
			}
		}

		public void remove()
		{
			throw new UnsupportedOperationException();
		}
	}

}
