/* *******************************************************************
 * Copyright (c) 2017 Contributors
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 * ******************************************************************/
package org.aspectj.apache.bcel.util;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A map that holds references but each reference maintains a connection to its key,
 * so that when that reference is GC'd we can remove the key from the map.
 */
public class SoftHashMap<K,V> extends AbstractMap<K,V> {
	private Map<K, SoftReferenceWithKey<K,V>> map;
	private ReferenceQueue<V> rq = new ReferenceQueue<>();
	/** How often get() found an entry whose value the GC had reclaimed. Read by ClassLoaderRepository for its statistics. */
	private final AtomicLong evictedMisses = new AtomicLong();

	public SoftHashMap() {
		this(new HashMap<>());
	}

	public SoftHashMap(Map<K, SoftReferenceWithKey<K,V>> map) {
		this.map = map;
	}

	protected static class SoftReferenceWithKey<K,V> extends SoftReference<V> {
		private final K key;
		SoftReferenceWithKey(K k, V v, ReferenceQueue<? super V> rq) {
			super(v, rq);
			this.key = k;
		}
	}

	private void processQueue() {
		Reference<? extends V> ref;
		while ((ref = rq.poll()) != null) {
			map.remove(((SoftReferenceWithKey<?,?>)ref).key);
		}
	}

	@Override
	public V get(Object key) {
		SoftReferenceWithKey<K,V> value = map.get(key);
		if (value == null)
			return null;
		if (value.get() == null) {
			// it got GC'd
			map.remove(value.key);
			evictedMisses.incrementAndGet();
			return null;
		} else {
			return value.get();
		}
	}

	@Override
	public V put(K k, V v) {
		processQueue();
		SoftReferenceWithKey<K,V> svalue = new SoftReferenceWithKey<>(k,v, rq);
		svalue = map.put(k, svalue);
		return (svalue==null?null:svalue.get());
	}

	/**
	 * @return a snapshot of the entries whose values are still there. Changing the snapshot does not change the map.
	 */
	@Override
	public Set<Entry<K,V>> entrySet() {
		Set<Entry<K,V>> result = new HashSet<>();
		for (Entry<K,SoftReferenceWithKey<K,V>> entry: map.entrySet()) {
			V value = entry.getValue().get();
			if (value != null) { // the GC may have reclaimed the value without the entry having been removed yet
				result.add(new SimpleEntry<>(entry.getKey(), value));
			}
		}
		return result;
	}

	@Override
	public void clear() {
		processQueue();
		map.clear();
	}

	@Override
	public int size() {
		processQueue();
		return map.size();
	}

	@Override
	public V remove(Object k) {
		processQueue();
		SoftReferenceWithKey<K,V> value = map.remove(k);
		if (value == null) {
			return null;
		}
		if (value.get() != null) {
			return value.get();
		}
		return null;
	}

	/** @return how many times get() found an entry whose value had been reclaimed by the GC (and so dropped it) */
	public long evictedMisses() {
		return evictedMisses.get();
	}

	public void resetEvictedMisses() {
		evictedMisses.set(0);
	}

	/**
	 * Does to up to {@code howMany} of the values what the GC does when it reclaims the value of a soft reference: the reference is
	 * cleared and put on the reference queue. For testing code that uses a SoftHashMap.
	 */
	public void discardValueReferences(int howMany) {
		int count = 0;
		for (SoftReferenceWithKey<K,V> value: map.values()) {
			if (count >= howMany) {
				break;
			}
			value.clear();
			value.enqueue();
			count++;
		}
	}
}