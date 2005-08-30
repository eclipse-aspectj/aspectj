/*
 * Created on Jan 7, 2005
 *
 * @author Mohan Radhakrishnan
 */
//package com.blueprint.ui.util;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
/*
 * Undo/redo for each shape. This can be used to maintain
 * a list of changes to rollback. Since the calls to the
 * model tier are direct and the reverse calls to update the
 * UI are Commands, this list is for the latter.
 */

public class ShapeCommandMap<K,V> extends AbstractMap<K,V> {
	
	private final Map<K, SoftReference<V>> internalMap = new HashMap<K, SoftReference<V>>();
	
	private final ReferenceQueue<V> queue = new ReferenceQueue<V>();
	
	public V put( K key, V value ){
		//remove stale entries
		SoftReference<V> ref = new SoftReference<V>( value, queue );
		SoftReference<V> s = internalMap.put( key, ref );
		return ( s != null ? s.get() : null );
	}
	
	/*public V get( K key ){
		//remove stale entries
		SoftReference<V> value = internalMap.get( key );
		return ( value != null ? value.get() : null );
	}*/
	
	public Set<Entry<K,V>> entrySet(){
		Set<Entry<K,V>> commands = new LinkedHashSet<Entry<K,V>>();
		for( final Entry<K,SoftReference<V>> entry : internalMap.entrySet() ){
			final V value = entry.getValue().get();
			commands.add( new Entry<K,V>(){
							public K getKey(){
								return entry.getKey();
							}
							public V getValue(){
								return value;
							}
							public V setValue( V v ){
								entry.setValue( 
										new SoftReference<V>( v, queue ) );
								return value;
							}
						});
		}
		return commands;
	}
}

aspect TriggerBug {
	
  public void foo() {
	ShapeCommandMap<String,String> map = new ShapeCommandMap<String,String>();  
	map.put("hi","there");
  }
  
  before() : execution(* getValue(..)) {
	  System.out.println("a matching call");
  }
}
