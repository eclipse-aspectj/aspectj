package com.ihg.dec.framework.commons.utils.cow;

import java.util.AbstractSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Map.Entry;

public class AspectJBugTestCase {
	class Value<V> {
		private V value;
		public Value(V value) {
			this.value = value;
		}
		public V getValue() {
			return value;
		}
		public void setValue(V value) {
			this.value = value;
		}
	}

	class EntrySetEntry<K, V> implements Entry<K, V> {
		private Entry<K, Value<V>> wrapped;

		public EntrySetEntry(Entry<K, Value<V>> wrapped) {
			this.wrapped = wrapped;
		}

		public K getKey() {
			return wrapped.getKey();
		}

		public V getValue() {
			return wrapped.getValue().getValue();
		}

		public V setValue(V value) {
			Value<V> old = wrapped.setValue(new Value<V>(value));
			return old == null ? null : old.getValue();
		}

	}

	class EntrySetIterator<K, V> implements Iterator<Entry<K, V>> {
		private Iterator<Entry<K, Value<V>>> wrapped;

		public EntrySetIterator(Iterator<Entry<K, Value<V>>> wrapped) {
			this.wrapped = wrapped;
		}

		public boolean hasNext() {
			return wrapped.hasNext();
		}

		public Entry<K, V> next() {
			return new EntrySetEntry<K, V>(wrapped.next());
		}

		public void remove() {
			throw new UnsupportedOperationException("Not implemented.");
		}
	}

	class EntrySet<K, V> extends AbstractSet<Entry<K, V>> implements
			Set<Entry<K, V>> {
		private Set<Entry<K, Value<V>>> wrapped;

		public EntrySet(Set<Entry<K, Value<V>>> wrapped) {
			this.wrapped = wrapped;
		}

		@Override
		public Iterator<Entry<K, V>> iterator() {
			return new EntrySetIterator<K, V>(wrapped.iterator());
		}

		@Override
		public int size() {
			return wrapped.size();
		}
	}

	public void testIt() {
		new EntrySet<String, String>( new HashSet<Entry<String, Value<String>>>());
	}
}

aspect X {
  declare parents: *.Entry* implements java.io.Serializable;
}
