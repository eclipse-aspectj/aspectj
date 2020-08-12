/* *******************************************************************
 * Copyright (c) 2017 Contributors
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 * ******************************************************************/package org.aspectj.util;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class SoftHashMap<K,V> extends AbstractMap<K,V> {
	private Map<K, SpecialValue> map;
	private ReferenceQueue<? super V> rq = new ReferenceQueue();

	public SoftHashMap() {
		this.map = new HashMap<>();
	}
	
	class SpecialValue extends SoftReference<V> {
		private final K key;

		SpecialValue(K k, V v) {
			super(v, rq);
			this.key = k;
		}
	}

	@SuppressWarnings("unchecked")
	private void processQueue() {
		SpecialValue sv = null;
		while ((sv = (SpecialValue)rq.poll()) != null) {
			map.remove(sv.key);
		}
	}

	@Override
	public V get(Object key) {
		SpecialValue ref = map.get(key);
		if (ref == null) {
			map.remove(key);
			return null;
		}
		V value = ref.get();
		if (value == null) {
			map.remove(ref.key);
			return null;
		}
		return value;
	}

	@Override
	public V put(K k, V v) {
		processQueue();
		SpecialValue sv = new SpecialValue(k, v);
		SpecialValue result = map.put(k, sv);
		return (result == null ? null : result.get());
	}

	@Override
	public java.util.Set<Map.Entry<K,V>> entrySet() {
		if (map.isEmpty()) { return Collections.<K,V>emptyMap().entrySet(); }
		Map<K,V> currentContents = new HashMap<>();
		for (Map.Entry<K,SpecialValue> entry: map.entrySet()) {
			V currentValueForEntry = entry.getValue().get();
			if (currentValueForEntry != null) {
				currentContents.put(entry.getKey(), currentValueForEntry);
			}
		}
		return currentContents.entrySet();
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
		SpecialValue ref = map.remove(k);
		if (ref == null) {
			return null;
		}
		return ref.get();
	}
}
